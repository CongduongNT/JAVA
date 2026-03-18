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

export const fetchUserById = createAsyncThunk('users/fetchById', async (id, { rejectWithValue }) => {
  try {
    const res = await userService.getById(id)
    return res.data
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Không tìm thấy người dùng')
  }
})

export const createUser = createAsyncThunk('users/create', async (data, { rejectWithValue }) => {
  try {
    const res = await userService.create(data)
    return res.data
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Không thể tạo người dùng')
  }
})

export const updateUser = createAsyncThunk('users/update', async ({ id, data }, { rejectWithValue }) => {
  try {
    const res = await userService.update(id, data)
    return res.data
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Không thể cập nhật người dùng')
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
    status: 'idle',       // 'idle' | 'loading' | 'succeeded' | 'failed'
    error: null,
    deletingId: null,
    // single-user state for the edit form
    current: null,
    currentStatus: 'idle',
    submitStatus: 'idle', // 'idle' | 'loading' | 'succeeded' | 'failed'
    submitError: null,
  },
  reducers: {
    clearError: (state) => {
      state.error = null
      state.submitError = null
    },
    resetCurrent: (state) => {
      state.current = null
      state.currentStatus = 'idle'
      state.submitStatus = 'idle'
      state.submitError = null
    },
  },
  extraReducers: (builder) => {
    builder
      // fetchUsers
      .addCase(fetchUsers.pending,    (state) => { state.status = 'loading'; state.error = null })
      .addCase(fetchUsers.fulfilled,  (state, { payload }) => { state.status = 'succeeded'; state.list = payload })
      .addCase(fetchUsers.rejected,   (state, { payload }) => { state.status = 'failed'; state.error = payload })

      // fetchUserById
      .addCase(fetchUserById.pending,   (state) => { state.currentStatus = 'loading'; state.current = null })
      .addCase(fetchUserById.fulfilled, (state, { payload }) => { state.currentStatus = 'succeeded'; state.current = payload })
      .addCase(fetchUserById.rejected,  (state, { payload }) => { state.currentStatus = 'failed'; state.error = payload })

      // createUser
      .addCase(createUser.pending,   (state) => { state.submitStatus = 'loading'; state.submitError = null })
      .addCase(createUser.fulfilled, (state, { payload }) => {
        state.submitStatus = 'succeeded'
        state.list = [...state.list, payload]
      })
      .addCase(createUser.rejected,  (state, { payload }) => { state.submitStatus = 'failed'; state.submitError = payload })

      // updateUser
      .addCase(updateUser.pending,   (state) => { state.submitStatus = 'loading'; state.submitError = null })
      .addCase(updateUser.fulfilled, (state, { payload }) => {
        state.submitStatus = 'succeeded'
        state.list = state.list.map((u) => u.id === payload.id ? payload : u)
      })
      .addCase(updateUser.rejected,  (state, { payload }) => { state.submitStatus = 'failed'; state.submitError = payload })

      // deleteUser
      .addCase(deleteUser.pending,   (state, { meta }) => { state.deletingId = meta.arg })
      .addCase(deleteUser.fulfilled, (state, { payload }) => { state.list = state.list.filter((u) => u.id !== payload); state.deletingId = null })
      .addCase(deleteUser.rejected,  (state, { payload }) => { state.deletingId = null; state.error = payload })
  },
})

export const { clearError, resetCurrent } = usersSlice.actions
export default usersSlice.reducer
