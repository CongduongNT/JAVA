import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { userService } from '@/services/userService'

// ── Thunks ──────────────────────────────────────────────────────────────────

export const fetchUsers = createAsyncThunk('users/fetchAll', async (_, { rejectWithValue }) => {
  try {
    const res = await userService.getAll()
    return res.data
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Không thể tải danh sách người dùng')
  }
})

export const deleteUser = createAsyncThunk('users/delete', async (id, { rejectWithValue }) => {
  try {
    await userService.delete(id)
    return id
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Không thể xoá người dùng')
  }
})

// ── Slice ────────────────────────────────────────────────────────────────────

const usersSlice = createSlice({
  name: 'users',
  initialState: {
    list: [],
    status: 'idle', // 'idle' | 'loading' | 'succeeded' | 'failed'
    error: null,
    deletingId: null,
  },
  reducers: {
    clearError: (state) => { state.error = null },
  },
  extraReducers: (builder) => {
    builder
      // fetchUsers
      .addCase(fetchUsers.pending, (state) => {
        state.status = 'loading'
        state.error = null
      })
      .addCase(fetchUsers.fulfilled, (state, action) => {
        state.status = 'succeeded'
        state.list = action.payload
      })
      .addCase(fetchUsers.rejected, (state, action) => {
        state.status = 'failed'
        state.error = action.payload
      })
      // deleteUser
      .addCase(deleteUser.pending, (state, action) => {
        state.deletingId = action.meta.arg
      })
      .addCase(deleteUser.fulfilled, (state, action) => {
        state.list = state.list.filter((u) => u.id !== action.payload)
        state.deletingId = null
      })
      .addCase(deleteUser.rejected, (state, action) => {
        state.deletingId = null
        state.error = action.payload
      })
  },
})

export const { clearError } = usersSlice.actions
export default usersSlice.reducer
