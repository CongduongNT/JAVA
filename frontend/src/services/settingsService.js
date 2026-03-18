import api from './api'

// Profile endpoints (SOLID: separate from generic user management)
export const settingsService = {
  getProfile:    ()     => api.get('/users/me'),
  updateProfile: (data) => api.put('/users/me', data),
  changePassword:(data) => api.put('/users/me/password', data),
}
