import api from './api'

/**
 * examApi – Gọi các endpoint /api/v1/exams
 */
export const examApi = {
  /**
   * [KAN-23] Sinh đề thi bằng AI kết hợp ngân hàng câu hỏi.
   * @param {object} payload
   * @param {string} payload.subject
   * @param {string} payload.grade_level
   * @param {string} payload.topic
   * @param {number} payload.total_questions
   * @param {object} payload.difficulty_mix   e.g. { EASY: 5, MEDIUM: 10, HARD: 5 }
   * @param {number|null} payload.bank_id
   * @param {string} [payload.title]
   * @param {string} [payload.question_type]  MULTIPLE_CHOICE | SHORT_ANSWER | FILL_IN_BLANK
   * @param {number} [payload.duration_mins]
   * @param {boolean} [payload.randomized]
   */
  aiGenerate: (payload) =>
    api.post('/exams/ai-generate', payload),

  /** Lấy danh sách đề thi của teacher (phân trang) */
  getMyExams: (params = {}) =>
    api.get('/exams', { params }),

  /** Xem chi tiết đề thi kèm câu hỏi */
  getExamDetail: (id) =>
    api.get(`/exams/${id}`),

  /** Publish đề thi (DRAFT → PUBLISHED) */
  publishExam: (id) =>
    api.put(`/exams/${id}/publish`),

  /** Xóa đề thi */
  deleteExam: (id) =>
    api.delete(`/exams/${id}`),
}

export default examApi
