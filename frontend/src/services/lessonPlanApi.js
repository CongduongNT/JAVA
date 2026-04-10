import api from './api'

export const lessonPlanApi = {
  /**
   * Gọi AI sinh lesson plan (preview, không lưu DB).
   * @param {object} data - { subject, gradeLevel, topic, objectives, durationMinutes, framework }
   */
  generatePreview: (data) =>
    api.post('/ai/lesson-plans/generate', { ...data, saveToDb: false }),

  /**
   * Sinh và lưu lesson plan luôn vào DB.
   */
  generateAndSave: (data) =>
    api.post('/ai/lesson-plans/generate', { ...data, saveToDb: true }),

  /**
   * Lấy tất cả lesson plans của current user.
   */
  getAll: () => api.get('/ai/lesson-plans'),

  /**
   * Lấy lesson plan theo id.
   */
  getById: (id) => api.get(`/ai/lesson-plans/${id}`),
}

export default lessonPlanApi
