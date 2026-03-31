import axios from 'axios';
import { loginSuccess, logout } from './authSlice';

// Sử dụng biến môi trường (Vite), fallback về localhost
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

export const apiClient = axios.create({
  baseURL: API_URL,
});

export const login = (credentials) => {
  return apiClient.post('/auth/login', credentials);
};

export const register = (userData) => {
    return apiClient.post('/auth/register', userData);
};

// --- Interceptors ---

// Request interceptor to add the auth token header to requests
apiClient.interceptors.request.use(
  (config) => {
    // Lấy token trực tiếp từ localStorage để tránh circular dependency với store
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle token refresh
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // If the error is 401 and we haven't retried yet
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // Lấy refreshToken từ localStorage
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) {
          // Chuyển hướng hoặc xử lý logout thủ công nếu không có store dispatch
          return Promise.reject(error);
        }

        // Call the refresh token endpoint
        const { data } = await axios.post(`${API_URL}/auth/refresh`, { refreshToken });

        // Cập nhật localStorage (Redux state sẽ được đồng bộ ở lần request tiếp theo hoặc reload)
        localStorage.setItem('token', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);

        // Update the header of the original request
        originalRequest.headers['Authorization'] = `Bearer ${data.accessToken}`;

        // Retry the original request
        return apiClient(originalRequest);
      } catch (refreshError) {
        // Nếu refresh thất bại, xóa sạch và đá về login
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);
