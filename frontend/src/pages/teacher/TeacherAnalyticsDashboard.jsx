import React, { useEffect, useState } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
  RadarChart, Radar, PolarGrid, PolarAngleAxis, PolarRadiusAxis,
} from 'recharts';
import {
  BookOpen, BarChart2, TrendingUp, AlertTriangle, Award,
  ChevronDown, RefreshCw, Loader2
} from 'lucide-react';
import analyticsApi from '../../services/analyticsApi';
import { toast } from 'sonner';

// ── Colour palette ───────────────────────────────────────────────────────────
const CHART_COLORS = ['#6366f1', '#22d3ee', '#f59e0b', '#10b981', '#f43f5e'];
const DIFF_COLORS = { EASY: '#10b981', MEDIUM: '#f59e0b', HARD: '#f43f5e', UNKNOWN: '#94a3b8' };
const SCORE_BAND_COLORS = ['#f43f5e', '#fb923c', '#facc15', '#4ade80', '#22d3ee'];

// ── Helpers ───────────────────────────────────────────────────────────────────
const Spinner = () => (
  <div className="flex items-center justify-center h-64">
    <Loader2 className="w-8 h-8 text-indigo-500 animate-spin" />
  </div>
);

const SectionCard = ({ title, icon: Icon, iconClass, children, className = '' }) => (
  <div className={`bg-white rounded-3xl border border-slate-100 shadow-xl shadow-slate-200/30 p-6 ${className}`}>
    <h3 className="flex items-center gap-2 text-lg font-bold text-slate-800 mb-5">
      <Icon className={`w-5 h-5 ${iconClass}`} />
      {title}
    </h3>
    {children}
  </div>
);

const KpiCard = ({ label, value, sub, color }) => (
  <div className="bg-white rounded-2xl border border-slate-100 shadow-lg p-5 flex flex-col gap-1">
    <p className="text-xs font-bold text-slate-400 uppercase tracking-wider">{label}</p>
    <p className={`text-3xl font-black ${color}`}>{value}</p>
    {sub && <p className="text-xs text-slate-400">{sub}</p>}
  </div>
);

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-white border border-slate-200 rounded-xl shadow-xl p-3 text-sm">
      <p className="font-bold text-slate-700 mb-1">{label}</p>
      {payload.map((p, i) => (
        <p key={i} style={{ color: p.color }}>{p.name}: <b>{p.value}</b></p>
      ))}
    </div>
  );
};

// ── Select Exam dropdown ──────────────────────────────────────────────────────
const ExamSelector = ({ exams, selected, onChange }) => (
  <div className="relative w-72">
    <select
      className="w-full appearance-none bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 pr-10 text-sm font-medium text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-400 cursor-pointer"
      value={selected ?? ''}
      onChange={(e) => onChange(Number(e.target.value))}
    >
      <option value="" disabled>-- Chọn đề thi --</option>
      {exams.map((e) => (
        <option key={e.examId} value={e.examId}>
          {e.examTitle || `Đề #${e.examId}`}
        </option>
      ))}
    </select>
    <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
  </div>
);

// ─────────────────────────────────────────────────────────────────────────────
// Main page
// ─────────────────────────────────────────────────────────────────────────────
const TeacherAnalyticsDashboard = () => {
  const [studentData, setStudentData] = useState(null);
  const [examList, setExamList]       = useState([]);
  const [selectedExamId, setSelectedExamId] = useState(null);
  const [examData, setExamData]       = useState(null);
  const [loading, setLoading]         = useState(true);
  const [examLoading, setExamLoading] = useState(false);

  // ── Load student analytics (all exams overview) ───────────────────────────
  useEffect(() => {
    setLoading(true);
    analyticsApi.getStudentAnalytics()
      .then((data) => {
        setStudentData(data);
        // Build exam list from studentGroups → recentExams
        const allExams = data.studentGroups?.flatMap((g) =>
          (g.recentExams || []).map((e) => ({ ...e, groupLabel: `${g.gradeLevel} – ${g.subject}` }))
        ) ?? [];
        // Deduplicate by examId
        const unique = [...new Map(allExams.map((e) => [e.examId, e])).values()];
        setExamList(unique);
        if (unique.length > 0) setSelectedExamId(unique[0].examId);
      })
      .catch(() => toast.error('Không thể tải dữ liệu analytics học sinh'))
      .finally(() => setLoading(false));
  }, []);

  // ── Load single exam analytics when selectedExamId changes ───────────────
  useEffect(() => {
    if (!selectedExamId) return;
    setExamLoading(true);
    analyticsApi.getExamResults(selectedExamId)
      .then(setExamData)
      .catch(() => toast.error('Không thể tải dữ liệu đề thi'))
      .finally(() => setExamLoading(false));
  }, [selectedExamId]);

  if (loading) return <Spinner />;

  const summary = studentData?.summary;

  // ── Score distribution chart data ─────────────────────────────────────────
  const scoreDist = examData
    ? Object.entries(examData.scoreDistribution || {}).map(([band, count], i) => ({
        band, count,
        fill: SCORE_BAND_COLORS[i] ?? '#6366f1',
      }))
    : [];

  // ── Difficulty breakdown (radar) ──────────────────────────────────────────
  const diffRadar = examData
    ? Object.entries(examData.difficultyStats || {}).map(([diff, cnt]) => ({
        diff, count: cnt, fill: DIFF_COLORS[diff] ?? '#94a3b8',
      }))
    : [];

  // ── Top wrong questions (lowest estimatedCorrectRate) ─────────────────────
  const topWrong = (examData?.questionStats ?? [])
    .slice()
    .sort((a, b) => a.estimatedCorrectRate - b.estimatedCorrectRate)
    .slice(0, 10)
    .map((q) => ({
      label: `Câu ${q.orderIndex + 1}`,
      rate: q.estimatedCorrectRate,
      diff: q.difficulty,
      content: q.content?.substring(0, 60) + (q.content?.length > 60 ? '…' : ''),
    }));

  // ── Student group progress table ──────────────────────────────────────────
  const groups = studentData?.studentGroups ?? [];

  return (
    <div className="max-w-7xl mx-auto p-4 md:p-8 animate-in fade-in duration-500">

      {/* ── Header ── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">
            Phân tích học sinh
          </h1>
          <p className="text-slate-500 mt-1">Thống kê theo đề thi, độ khó và tỉ lệ đúng ước lượng</p>
        </div>
        <button
          onClick={() => window.location.reload()}
          className="flex items-center gap-2 px-4 py-2 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-semibold rounded-xl text-sm transition-colors"
        >
          <RefreshCw className="w-4 h-4" /> Làm mới
        </button>
      </div>

      {/* ── KPI Row ── */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
        <KpiCard label="Tổng đề thi" value={summary?.totalExams ?? 0} color="text-indigo-600" />
        <KpiCard
          label="Đã xuất bản"
          value={summary?.publishedExams ?? 0}
          sub={`${summary?.draftExams ?? 0} bản nháp`}
          color="text-emerald-600"
        />
        <KpiCard label="Nhóm học sinh" value={summary?.studentGroupCount ?? 0} color="text-purple-600" />
        <KpiCard
          label="Tỉ lệ câu AI"
          value={
            summary?.totalQuestions > 0
              ? `${Math.round((summary.totalAiQuestions / summary.totalQuestions) * 100)}%`
              : '–'
          }
          sub={`${summary?.totalAiQuestions ?? 0} / ${summary?.totalQuestions ?? 0} câu`}
          color="text-amber-600"
        />
      </div>

      {/* ── Exam Selector ── */}
      <div className="flex flex-col sm:flex-row sm:items-center gap-3 mb-6 p-4 bg-slate-50 rounded-2xl border border-slate-200">
        <span className="text-sm font-bold text-slate-600">📋 Chọn đề thi để phân tích:</span>
        <ExamSelector exams={examList} selected={selectedExamId} onChange={setSelectedExamId} />
        {examData && (
          <span className="text-xs text-slate-400 ml-auto">
            {examData.totalQuestions} câu · {examData.durationMins} phút · {examData.status}
          </span>
        )}
      </div>

      {examLoading && <Spinner />}

      {!examLoading && examData && (
        <>
          {/* ── Score KPIs ── */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
            <KpiCard label="Điểm TB ước tính" value={examData.avgScore?.toFixed(1)} color="text-indigo-600" />
            <KpiCard label="Tỉ lệ đạt" value={`${examData.passRate?.toFixed(1)}%`} color="text-emerald-600" />
            <KpiCard label="Câu AI" value={examData.aiVsBankStats?.aiCount ?? 0} sub={`${examData.aiVsBankStats?.aiRatio ?? 0}%`} color="text-purple-600" />
            <KpiCard label="Câu Bank" value={examData.aiVsBankStats?.bankCount ?? 0} sub={`${examData.aiVsBankStats?.bankRatio ?? 0}%`} color="text-sky-600" />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
            {/* ── Score Distribution Bar Chart ── */}
            <SectionCard title="Phân bố điểm thi (ước lượng)" icon={BarChart2} iconClass="text-indigo-500">
              <ResponsiveContainer width="100%" height={240}>
                <BarChart data={scoreDist} barSize={36}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                  <XAxis dataKey="band" tick={{ fontSize: 13, fontWeight: 600 }} />
                  <YAxis tick={{ fontSize: 12 }} />
                  <Tooltip content={<CustomTooltip />} />
                  <Bar dataKey="count" name="Học sinh" radius={[8, 8, 0, 0]}>
                    {scoreDist.map((entry, i) => (
                      <Cell key={i} fill={entry.fill} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </SectionCard>

            {/* ── Difficulty radar ── */}
            <SectionCard title="Phân bổ độ khó" icon={TrendingUp} iconClass="text-amber-500">
              <ResponsiveContainer width="100%" height={240}>
                <RadarChart data={diffRadar} cx="50%" cy="50%" outerRadius="70%">
                  <PolarGrid stroke="#e2e8f0" />
                  <PolarAngleAxis dataKey="diff" tick={{ fontSize: 13, fontWeight: 600 }} />
                  <PolarRadiusAxis tick={{ fontSize: 11 }} />
                  <Radar name="Số câu" dataKey="count" stroke="#6366f1" fill="#6366f1" fillOpacity={0.3} />
                  <Tooltip content={<CustomTooltip />} />
                </RadarChart>
              </ResponsiveContainer>
            </SectionCard>
          </div>

          {/* ── Top Wrong Questions ── */}
          <SectionCard title="Câu hỏi bị sai nhiều nhất (tỉ lệ đúng thấp nhất)" icon={AlertTriangle} iconClass="text-rose-500" className="mb-6">
            {topWrong.length === 0 ? (
              <p className="text-slate-400 text-sm text-center py-8">Chọn đề thi để xem thống kê</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-xs font-bold text-slate-400 uppercase border-b border-slate-100">
                      <th className="text-left pb-3 pr-4">Câu</th>
                      <th className="text-left pb-3 pr-4">Nội dung</th>
                      <th className="text-left pb-3 pr-4">Độ khó</th>
                      <th className="text-right pb-3">Tỉ lệ đúng</th>
                    </tr>
                  </thead>
                  <tbody>
                    {topWrong.map((q, i) => (
                      <tr key={i} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                        <td className="py-3 pr-4 font-bold text-slate-700">{q.label}</td>
                        <td className="py-3 pr-4 text-slate-500 max-w-xs truncate">{q.content}</td>
                        <td className="py-3 pr-4">
                          <span
                            className="px-2 py-1 rounded-lg text-xs font-bold"
                            style={{
                              background: DIFF_COLORS[q.diff] + '22',
                              color: DIFF_COLORS[q.diff],
                            }}
                          >
                            {q.diff}
                          </span>
                        </td>
                        <td className="py-3 text-right font-black"
                          style={{ color: q.rate < 40 ? '#f43f5e' : q.rate < 65 ? '#f59e0b' : '#10b981' }}>
                          {q.rate}%
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </SectionCard>
        </>
      )}

      {/* ── Student Group Progress Table ── */}
      <SectionCard title="Tiến độ theo nhóm học sinh (khối × môn)" icon={Award} iconClass="text-purple-500">
        {groups.length === 0 ? (
          <p className="text-slate-400 text-sm text-center py-8">Chưa có đề thi nào</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-xs font-bold text-slate-400 uppercase border-b border-slate-100">
                  <th className="text-left pb-3 pr-3">Khối</th>
                  <th className="text-left pb-3 pr-3">Môn</th>
                  <th className="text-right pb-3 pr-3">Đề</th>
                  <th className="text-right pb-3 pr-3">Câu</th>
                  <th className="text-right pb-3 pr-3">Điểm TB</th>
                  <th className="text-right pb-3 pr-3">Tỉ lệ đạt</th>
                  <th className="text-right pb-3">AI %</th>
                </tr>
              </thead>
              <tbody>
                {groups.map((g, i) => (
                  <tr key={i} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                    <td className="py-3 pr-3 font-bold text-slate-700">Lớp {g.gradeLevel}</td>
                    <td className="py-3 pr-3 text-slate-600">{g.subject}</td>
                    <td className="py-3 pr-3 text-right text-slate-700 font-semibold">
                      {g.publishedExamCount}/{g.examCount}
                    </td>
                    <td className="py-3 pr-3 text-right text-slate-500">{g.totalQuestions}</td>
                    <td className="py-3 pr-3 text-right font-black text-indigo-600">
                      {g.estimatedAvgScore?.toFixed(1)}
                    </td>
                    <td className="py-3 pr-3 text-right">
                      <span
                        className="px-2 py-1 rounded-lg text-xs font-bold"
                        style={{
                          background: g.estimatedPassRate >= 70 ? '#d1fae5' : '#fee2e2',
                          color: g.estimatedPassRate >= 70 ? '#059669' : '#dc2626',
                        }}
                      >
                        {g.estimatedPassRate?.toFixed(1)}%
                      </span>
                    </td>
                    <td className="py-3 text-right text-purple-600 font-semibold">
                      {g.aiQuestionRatio?.toFixed(0)}%
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </SectionCard>
    </div>
  );
};

export default TeacherAnalyticsDashboard;
