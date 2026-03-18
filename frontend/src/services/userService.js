import api from './api'

// Role mapping: seeded in this order by DataSeeder
export const ROLES = [
  { id: 1, name: 'ADMIN' },
  { id: 2, name: 'MANAGER' },
  { id: 3, name: 'STAFF' },
  { id: 4, name: 'TEACHER' },
]

export const ROLE_NAMES = ROLES.map((r) => r.name)

export const userService = {
  getAll: () => api.get('/api/v1/users'),
  getById: (id) => api.get(`/api/v1/users/${id}`),
  create: (data) => api.post('/api/v1/users', data),
  update: (id, data) => api.put(`/api/v1/users/${id}`, data),
  assignRole: (id, roleId) => api.put(`/api/v1/users/${id}/role`, { roleId }),
  delete: (id) => api.delete(`/api/v1/users/${id}`),
}
