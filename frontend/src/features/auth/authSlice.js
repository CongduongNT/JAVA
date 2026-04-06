import { createSlice } from '@reduxjs/toolkit';

// Lấy user từ localStorage nếu có (để giữ đăng nhập khi F5)
const user = JSON.parse(localStorage.getItem('user'));
const token = localStorage.getItem('token');
const refreshToken = localStorage.getItem('refreshToken');

const initialState = {
  user: user || null,
  token: token || null,
  refreshToken: refreshToken || null,
  isAuthenticated: !!token,
  isLoading: false,
  isError: false,
  message: '',
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    loginSuccess: (state, action) => {
      state.isAuthenticated = true;
      state.user = action.payload.user; // Giả sử payload backend trả về có object user
      state.token = action.payload.accessToken; 
      state.refreshToken = action.payload.refreshToken;
      localStorage.setItem('user', JSON.stringify(action.payload.user));
      localStorage.setItem('token', action.payload.accessToken);
      localStorage.setItem('refreshToken', action.payload.refreshToken);
    },
    logout: (state) => {
      state.isAuthenticated = false;
      state.user = null;
      state.token = null;
      state.refreshToken = null;
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
    },
  },
});

export const { loginSuccess, logout } = authSlice.actions;
export default authSlice.reducer;