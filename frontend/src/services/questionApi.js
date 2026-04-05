import api from './api'

export const questionApi = {
  getMyBanks: () => api.get('/question-banks'),
  getBank: (id) => api.get(`/question-banks/${id}`),
  createBank: (data) => api.post('/question-banks', data),
  updateBank: (id, data) => api.put(`/question-banks/${id}`, data),
  deleteBank: (id) => api.delete(`/question-banks/${id}`),
  getBankQuestions: (id, params = {}) => api.get(`/question-banks/${id}/questions`, { params }),

  createQuestion: (data) => api.post('/questions', data),
  getQuestion: (id) => api.get(`/questions/${id}`),
  updateQuestion: (id, data) => api.put(`/questions/${id}`, data),
  deleteQuestion: (id) => api.delete(`/questions/${id}`),

  aiGeneratePreview: (data) => api.post('/questions/ai-generate', { ...data, saveToDb: false }),
  aiGenerateAndSave: (data) => api.post('/questions/ai-generate', { ...data, saveToDb: true }),
  saveBatch: (data) => api.post('/questions/save-batch', data),
}

export default questionApi
