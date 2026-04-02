import api from './api';

/**
 * Question API – Kết nối với backend để quản lý ngân hàng câu hỏi và sinh câu hỏi AI.
 */
export const questionApi = {
  // ==========================================
  // QUESTION BANK
  // ==========================================

  /** Lấy danh sách ngân hàng câu hỏi của tôi */
  getMyBanks: () => api.get('/question-banks'),

  /** Tạo ngân hàng câu hỏi mới */
  createBank: (data) => api.post('/question-banks', data),

  /** Cập nhật ngân hàng câu hỏi */
  updateBank: (id, data) => api.put(`/question-banks/${id}`, data),

  /** Xóa ngân hàng câu hỏi */
  deleteBank: (id) => api.delete(`/question-banks/${id}`),

  /** Lấy câu hỏi trong ngân hàng với phân trang / filter */
  getBankQuestions: (id, params = {}) => api.get(`/question-banks/${id}/questions`, { params }),

  // ==========================================
  // QUESTIONS & AI GENERATE
  // ==========================================

  /** Tạo câu hỏi thủ công */
  createQuestion: (data) => api.post('/questions', data),

  /** Lấy chi tiết câu hỏi */
  getQuestion: (id) => api.get(`/questions/${id}`),

  /** Cập nhật câu hỏi */
  updateQuestion: (id, data) => api.put(`/questions/${id}`, data),

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

  /** Xóa câu hỏi */
  deleteQuestion: (id) => api.delete(`/questions/${id}`),
};

export default questionApi;
