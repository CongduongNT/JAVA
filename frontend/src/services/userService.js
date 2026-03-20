import api from './api'

export const ROLES = [
  { id: 1, name: 'ADMIN' },
  { id: 2, name: 'MANAGER' },
  { id: 3, name: 'STAFF' },
  { id: 4, name: 'TEACHER' },
]

export const ROLE_NAMES = ROLES.map((r) => r.name)

export const userService = {
  getAll: () => api.get('/users'),
  getById: (id) => api.get(`/users/${id}`),
  create: (data) => api.post('/users', data),
  update: (id, data) => api.put(`/users/${id}`, data),
  assignRole: (id, roleId) => api.put(`/users/${id}/role`, { roleId }),
  delete: (id) => api.delete(`/users/${id}`),
}
