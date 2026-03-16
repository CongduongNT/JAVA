import axios from 'axios';
import { store } from '../../app/store'; // Import a global store
import { loginSuccess, logout } from './authSlice';

// Sử dụng biến môi trường (Vite), fallback về localhost
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

const apiClient = axios.create({
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
    const token = store.getState().auth.accessToken;
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
    if (error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = store.getState().auth.refreshToken;
        if (!refreshToken) {
          store.dispatch(logout());
          return Promise.reject(error);
        }

        // Call the refresh token endpoint
        const { data } = await axios.post(`${API_URL}/auth/refresh`, { refreshToken });

        // Update the store with the new tokens
        store.dispatch(loginSuccess(data));

        // Update the header of the original request
        originalRequest.headers['Authorization'] = `Bearer ${data.accessToken}`;

        // Retry the original request
        return apiClient(originalRequest);
      } catch (refreshError) {
        // If refresh fails, logout the user
        store.dispatch(logout());
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);
