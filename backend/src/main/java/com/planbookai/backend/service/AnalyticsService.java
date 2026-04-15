package com.planbookai.backend.service;

import com.planbookai.backend.dto.ExamAnalyticsDTO;
import com.planbookai.backend.dto.ExamAnalyticsDTO.AiVsBankDTO;
import com.planbookai.backend.dto.ExamAnalyticsDTO.QuestionStatDTO;
import com.planbookai.backend.exception.ResourceNotFoundException;
import com.planbookai.backend.model.entity.Exam;
import com.planbookai.backend.model.entity.ExamQuestion;
import com.planbookai.backend.model.entity.Question;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.ExamQuestionRepository;
import com.planbookai.backend.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AnalyticsService – Tính toán và trả về thống kê đề thi (KAN-26).
 *
 * <h2>Luồng getExamAnalytics() (KAN-26)</h2>
 * <ol>
 *   <li>Kiểm tra đề thi tồn tại và user có quyền xem.</li>
 *   <li>Load danh sách {@link ExamQuestion} với câu hỏi liên kết.</li>
 *   <li>Tính {@code difficultyStats}, {@code aiVsBankStats}.</li>
 *   <li>Tính {@code avgScore} ước lượng từ difficulty_mix (EASY=8.5, MEDIUM=6.5, HARD=4.5).</li>
 *   <li>Tính {@code passRate} (% câu EASY+MEDIUM chiếm tỉ lệ pass).</li>
 *   <li>Tính {@code scoreDistribution} phân dải điểm.</li>
 *   <li>Tính {@code questionStats} cho từng câu (estimatedCorrectRate theo độ khó).</li>
 * </ol>
 *
 * <p><strong>Lưu ý thiết kế:</strong> Hệ thống hiện chưa có bảng {@code exam_submissions}.
 * Analytics dựa trên cấu trúc đề (metadata) và ước tính thống kê theo chuẩn giáo dục
 * (Bloom's Taxonomy difficulty estimates). Khi tích hợp submission thực tế, chỉ cần
 * thay các hằng số ước tính bằng truy vấn thực tế.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final ExamRepository        examRepository;
    private final ExamQuestionRepository examQuestionRepository;

    // ── Điểm ước tính trung bình theo độ khó (thang 10) ──────────────────────
    private static final Map<String, Double> DIFFICULTY_AVG_SCORE = Map.of(
            "EASY",   8.5,
            "MEDIUM", 6.5,
            "HARD",   4.5
    );

    // ── Tỉ lệ trả lời đúng ước tính theo độ khó (0–1) ───────────────────────
    private static final Map<String, Double> DIFFICULTY_CORRECT_RATE = Map.of(
            "EASY",   0.80,
            "MEDIUM", 0.60,
            "HARD",   0.35
    );

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * [KAN-26] Trả về analytics đầy đủ cho một đề thi.
     *
     * @param examId ID đề thi
     * @param user   Người dùng đang đăng nhập (Teacher, Manager, Admin)
     * @return {@link ExamAnalyticsDTO} chứa đầy đủ số liệu thống kê
     * @throws ResourceNotFoundException khi không tìm thấy đề thi
     * @throws AccessDeniedException     khi user không có quyền xem
     */
    @Transactional(readOnly = true)
    public ExamAnalyticsDTO getExamAnalytics(Long examId, User user) {
        log.info("[AnalyticsService] getExamAnalytics: examId={}, user={}", examId, user.getEmail());

        // 1. Load đề thi
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi #" + examId));

        // 2. Kiểm tra quyền truy cập
        checkAccess(exam, user);

        // 3. Load danh sách câu hỏi trong đề
        List<ExamQuestion> examQuestions = examQuestionRepository
                .findByExamIdOrderByOrderIndex(examId);

        log.info("[AnalyticsService] Loaded {} questions for exam #{}", examQuestions.size(), examId);

        // 4. Tính các số liệu thống kê
        Map<String, Long>         difficultyStats = buildDifficultyStats(examQuestions);
        AiVsBankDTO               aiVsBankStats   = buildAiVsBankStats(examQuestions);
        double                    avgScore        = calcAvgScore(examQuestions);
        double                    passRate        = calcPassRate(avgScore);
        Map<String, Integer>      scoreDist       = buildScoreDistribution(examQuestions);
        List<QuestionStatDTO>     questionStats   = buildQuestionStats(examQuestions);

        return new ExamAnalyticsDTO(
                exam.getId(),
                exam.getTitle(),
                exam.getSubject(),
                exam.getGradeLevel(),
                exam.getTopic(),
                exam.getTotalQuestions(),
                exam.getDurationMins(),
                exam.getStatus() != null ? exam.getStatus().name() : null,
                round2(avgScore),
                round2(passRate),
                scoreDist,
                questionStats,
                difficultyStats,
                aiVsBankStats
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Kiểm tra quyền: Teacher xem đề của mình, Manager/Admin xem tất cả. */
    private void checkAccess(Exam exam, User user) {
        boolean isOwner   = exam.getTeacher().getId().equals(user.getId());
        boolean isPriv    = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")
                            || a.getAuthority().equals("ROLE_ADMIN"));
        if (!isOwner && !isPriv) {
            throw new AccessDeniedException("Bạn không có quyền xem analytics của đề thi này");
        }
    }

    /** Đếm số câu theo độ khó: { EASY: n, MEDIUM: n, HARD: n }. */
    private Map<String, Long> buildDifficultyStats(List<ExamQuestion> eqs) {
        return eqs.stream()
                .map(eq -> {
                    Question q = eq.getQuestion();
                    return q != null && q.getDifficulty() != null
                            ? q.getDifficulty().name()
                            : "UNKNOWN";
                })
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()));
    }

    /** Thống kê nguồn gốc câu hỏi: Bank vs AI. */
    private AiVsBankDTO buildAiVsBankStats(List<ExamQuestion> eqs) {
        if (eqs.isEmpty()) return new AiVsBankDTO(0, 0, 0.0, 0.0);

        long aiCount   = eqs.stream()
                .filter(eq -> eq.getQuestion() != null && Boolean.TRUE.equals(eq.getQuestion().getAiGenerated()))
                .count();
        long bankCount = eqs.size() - aiCount;
        double total   = eqs.size();

        return new AiVsBankDTO(
                (int) bankCount,
                (int) aiCount,
                round2(bankCount / total * 100),
                round2(aiCount   / total * 100)
        );
    }

    /**
     * Tính điểm trung bình ước lượng theo công thức:
     * avgScore = Σ (difficulty_weight_i * points_i) / total_points
     */
    private double calcAvgScore(List<ExamQuestion> eqs) {
        if (eqs.isEmpty()) return 0.0;

        double weightedSum = 0.0;
        double totalPoints = 0.0;

        for (ExamQuestion eq : eqs) {
            Question q      = eq.getQuestion();
            double   pts    = eq.getPoints() != null ? eq.getPoints().doubleValue() : 1.0;
            String   diff   = (q != null && q.getDifficulty() != null)
                              ? q.getDifficulty().name() : "MEDIUM";
            double   weight = DIFFICULTY_AVG_SCORE.getOrDefault(diff, 6.5);

            weightedSum += weight * pts;
            totalPoints += 10.0 * pts;   // thang 10
        }

        return totalPoints > 0 ? (weightedSum / totalPoints) * 10.0 : 0.0;
    }

    /**
     * Tỉ lệ đạt (%) – coi pass khi avgScore >= 5.0.
     * Ước lượng bằng tỉ lệ câu EASY + MEDIUM (thường đủ điểm pass).
     */
    private double calcPassRate(double avgScore) {
        // Đơn giản hóa: nếu avgScore >= 5 → pass_rate = tỉ lệ tuyến tính
        if (avgScore <= 0)  return 0.0;
        if (avgScore >= 10) return 100.0;
        // Giả định phân phối chuẩn: điểm 5 ~ 50% pass, điểm 8 ~ 85% pass
        return Math.min(100.0, avgScore * 10.0 + 5.0);
    }

    /**
     * Phân phối điểm theo 5 dải: "0-2", "3-4", "5-6", "7-8", "9-10".
     * Ước tính dựa trên tỉ lệ câu theo difficulty.
     */
    private Map<String, Integer> buildScoreDistribution(List<ExamQuestion> eqs) {
        if (eqs.isEmpty()) {
            return Map.of("0-2", 0, "3-4", 0, "5-6", 0, "7-8", 0, "9-10", 0);
        }

        long total  = eqs.size();
        long easy   = eqs.stream().filter(eq -> eq.getQuestion() != null
                && Question.Difficulty.EASY.equals(eq.getQuestion().getDifficulty())).count();
        long medium = eqs.stream().filter(eq -> eq.getQuestion() != null
                && Question.Difficulty.MEDIUM.equals(eq.getQuestion().getDifficulty())).count();
        long hard   = eqs.stream().filter(eq -> eq.getQuestion() != null
                && Question.Difficulty.HARD.equals(eq.getQuestion().getDifficulty())).count();

        // Mô phỏng 100 học sinh
        int students = 100;
        int fail_low  = (int) Math.round((hard  * 0.55 + medium * 0.08) / total * students);
        int fail_high = (int) Math.round((hard  * 0.30 + medium * 0.20) / total * students);
        int pass_low  = (int) Math.round((easy  * 0.15 + medium * 0.45 + hard * 0.10) / total * students);
        int pass_mid  = (int) Math.round((easy  * 0.45 + medium * 0.20 + hard * 0.05) / total * students);
        int excellent = (int) Math.round((easy  * 0.40) / total * students);

        // Đảm bảo tổng = 100
        int computed = fail_low + fail_high + pass_low + pass_mid + excellent;
        pass_low += (students - computed);

        LinkedHashMap<String, Integer> dist = new LinkedHashMap<>();
        dist.put("0-2",  Math.max(0, fail_low));
        dist.put("3-4",  Math.max(0, fail_high));
        dist.put("5-6",  Math.max(0, pass_low));
        dist.put("7-8",  Math.max(0, pass_mid));
        dist.put("9-10", Math.max(0, excellent));
        return dist;
    }

    /** Tạo danh sách thống kê từng câu hỏi. */
    private List<QuestionStatDTO> buildQuestionStats(List<ExamQuestion> eqs) {
        List<QuestionStatDTO> stats = new ArrayList<>();

        for (int i = 0; i < eqs.size(); i++) {
            ExamQuestion eq = eqs.get(i);
            Question     q  = eq.getQuestion();

            String  diff    = (q != null && q.getDifficulty() != null) ? q.getDifficulty().name() : "MEDIUM";
            double  corRate = DIFFICULTY_CORRECT_RATE.getOrDefault(diff, 0.60);
            String  src     = (q != null && Boolean.TRUE.equals(q.getAiGenerated())) ? "AI" : "BANK";
            double  pts     = eq.getPoints() != null ? eq.getPoints().doubleValue() : 1.0;

            stats.add(new QuestionStatDTO(
                    q  != null ? q.getId()      : null,
                    eq.getOrderIndex(),
                    q  != null ? truncate(q.getContent(), 120) : "(Câu hỏi không tìm thấy)",
                    diff,
                    q  != null && q.getType() != null ? q.getType().name() : null,
                    src,
                    q  != null ? q.getTopic()   : null,
                    pts,
                    round2(corRate * 100)
            ));
        }
        return stats;
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private static double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }
}
