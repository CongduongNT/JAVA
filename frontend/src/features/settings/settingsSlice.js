import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { loginSuccess } from '@/features/auth/authSlice'
import { settingsService } from '@/services/settingsService'

// ── Thunks ───────────────────────────────────────────────────────────────

export const saveProfile = createAsyncThunk(
  'settings/saveProfile',
  async (data, { rejectWithValue, getState, dispatch }) => {
    try {
      const res = await settingsService.updateProfile(data)
      // Sync updated user back into auth state
      const token = getState().auth.token
      dispatch(loginSuccess({ user: res.data, token }))
      return res.data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể cập nhật hồ sơ')
    }
  }
)

export const changePassword = createAsyncThunk(
  'settings/changePassword',
  async (data, { rejectWithValue }) => {
    try {
      await settingsService.changePassword(data)
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể đổi mật khẩu')
    }
  }
)

// ── Slice ────────────────────────────────────────────────────────────────

const settingsSlice = createSlice({
  name: 'settings',
  initialState: {
    saveStatus:     'idle',  // 'idle' | 'loading' | 'succeeded' | 'failed'
    saveError:      null,
    passwordStatus: 'idle',
    passwordError:  null,
  },
  reducers: {
    resetSaveStatus:     (state) => { state.saveStatus = 'idle';     state.saveError = null },
    resetPasswordStatus: (state) => { state.passwordStatus = 'idle'; state.passwordError = null },
  },
  extraReducers: (builder) => {
    builder
      // saveProfile
      .addCase(saveProfile.pending,   (state) => { state.saveStatus = 'loading';    state.saveError = null })
      .addCase(saveProfile.fulfilled, (state) => { state.saveStatus = 'succeeded' })
      .addCase(saveProfile.rejected,  (state, { payload }) => { state.saveStatus = 'failed'; state.saveError = payload })
      // changePassword
      .addCase(changePassword.pending,   (state) => { state.passwordStatus = 'loading';    state.passwordError = null })
      .addCase(changePassword.fulfilled, (state) => { state.passwordStatus = 'succeeded' })
      .addCase(changePassword.rejected,  (state, { payload }) => { state.passwordStatus = 'failed'; state.passwordError = payload })
  },
})

export const { resetSaveStatus, resetPasswordStatus } = settingsSlice.actions
export default settingsSlice.reducer
