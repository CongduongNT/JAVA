import api from './api'

export const questionApi = {
  getMyBanks: () => api.get('/question-banks'),
  getBank: (id) => api.get(`/question-banks/${id}`),
  createBank: (data) => api.post('/question-banks', data),
  updateBank: (id, data) => api.put(`/question-banks/${id}`, data),
  deleteBank: (id) => api.delete(`/question-banks/${id}`),
  getBankQuestions: (id, params = {}) => api.get(`/question-banks/${id}/questions`, { params }),

  // ==========================================
  // QUESTIONS CRUD
  // ==========================================
  createQuestion: (data) => api.post('/questions', data),
  getQuestion: (id) => api.get(`/questions/${id}`),
  updateQuestion: (id, data) => api.put(`/questions/${id}`, data),
  deleteQuestion: (id) => api.delete(`/questions/${id}`),

  // ==========================================
  // QUESTIONS & AI GENERATE
  // ==========================================

  /**
   * Lấy danh sách câu hỏi với filter (Manager/Admin)
   * approved=false → chờ duyệt | approved=true → đã duyệt | undefined → tất cả
   */
  getQuestions: (approved) => {
    const params = approved !== undefined ? { approved } : {};
    return api.get('/questions', { params });
  },

  /** 
   * Sinh câu hỏi bằng AI (Preview)
   * data: { bankId, subject, topic, difficulty, type, count, saveToDb: false }
   */
  aiGeneratePreview: (data) => api.post('/questions/ai-generate', { ...data, saveToDb: false }),

  /**
   * Sinh câu hỏi và lưu luôn (không preview)
   */
  aiGenerateAndSave: (data) => api.post('/questions/ai-generate', { ...data, saveToDb: true }),

  /**
   * Lưu danh sách câu hỏi đã preview/chỉnh sửa
   * data: { bankId, questions: [...] }
   */
  saveBatch: (data) => api.post('/questions/save-batch', data),

  // ==========================================
  // APPROVAL (Manager only)
  // ==========================================

  /**
   * Duyệt hoặc huỷ duyệt câu hỏi (chỉ Manager)
   * @param {number} id - ID câu hỏi
   * @param {boolean} approve - true = duyệt, false = huỷ duyệt
   */
  approveQuestion: (id, approve) => api.put(`/questions/${id}/approve`, { approve }),
}

export default questionApi