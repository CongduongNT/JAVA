import api from './api'

export const frameworkApi = {
  // ==========================================
  // FRAMEWORKS CRUD
  // ==========================================
  
  /**
   * Lấy danh sách frameworks (Admin: tất cả, Others: chỉ published)
   * @param {Object} params - { page, size, subject, gradeLevel }
   */
  getFrameworks: (params = {}) => api.get('/frameworks', { params }),

  /**
   * Lấy danh sách tất cả frameworks đã publish (không phân trang)
   * Dùng cho dropdown
   */
  getPublishedFrameworks: () => api.get('/frameworks/published'),

  /**
   * Lấy chi tiết framework theo ID
   */
  getFramework: (id) => api.get(`/frameworks/${id}`),

  /**
   * Tạo mới framework (Admin only)
   */
  createFramework: (data) => api.post('/frameworks', data),

  /**
   * Cập nhật framework (Admin only)
   */
  updateFramework: (id, data) => api.put(`/frameworks/${id}`, data),

  /**
   * Xóa framework (Admin only)
   */
  deleteFramework: (id) => api.delete(`/frameworks/${id}`),

  /**
   * Publish framework (Admin only)
   */
  publishFramework: (id) => api.put(`/frameworks/${id}/publish`),

  /**
   * Unpublish framework (Admin only)
   */
  unpublishFramework: (id) => api.put(`/frameworks/${id}/unpublish`),
}

export default frameworkApi
