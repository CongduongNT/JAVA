import api from './api'

const lessonPlanApi = {
  /**
   * GET /api/v1/lesson-plans
   * Params: page, size, status, subject, gradeLevel, keyword
   */
  getMyLessonPlans: (params = {}) => api.get('/api/v1/lesson-plans', { params }),

  /**
   * POST /api/v1/lesson-plans
   * Body: LessonPlanRequest
   */
  createLessonPlan: (data) => api.post('/api/v1/lesson-plans', data),

  /**
   * GET /api/v1/lesson-plans/{id}
   */
  getLessonPlan: (id) => api.get(`/api/v1/lesson-plans/${id}`),

  /**
   * PUT /api/v1/lesson-plans/{id}
   * Body: LessonPlanRequest
   */
  updateLessonPlan: (id, data) => api.put(`/api/v1/lesson-plans/${id}`, data),

  /**
   * DELETE /api/v1/lesson-plans/{id}
   */
  deleteLessonPlan: (id) => api.delete(`/api/v1/lesson-plans/${id}`),

  /**
   * PUT /api/v1/lesson-plans/{id}/publish
   */
  publishLessonPlan: (id) => api.put(`/api/v1/lesson-plans/${id}/publish`),
}

export default lessonPlanApi
