import api from './api'

/**
 * gradingApi – Gọi các endpoint /api/v1/grading-results
 */
export const gradingApi = {
  /**
   * GET /api/v1/grading-results?exam_id={id}
   * Danh sách kết quả chấm (phân trang)
   *
   * @param {object} params
   * @param {number} params.examId - ID bài thi (bắt buộc)
   * @param {number} [params.page=0]
   * @param {number} [params.size=20]
   */
  getGradingResults: (params = {}) =>
    api.get('/grading-results', { params }),

  /**
   * GET /api/v1/grading-results/{id}
   * Chi tiết 1 kết quả chấm (gồm từng câu)
   *
   * @param {number} resultId
   */
  getGradingResultDetail: (resultId) =>
    api.get(`/grading-results/${resultId}`),

  /**
   * PUT /api/v1/grading-results/{id}/feedback
   * Cập nhật feedback của giáo viên
   *
   * @param {number} resultId
   * @param {object} payload
   * @param {string|null} [payload.teacherFeedback]
   * @param {boolean} [payload.requestAiFeedback=false]
   */
  updateFeedback: (resultId, payload) =>
    api.put(`/grading-results/${resultId}/feedback`, payload),
}

export default gradingApi
