import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import questionApi from '@/services/questionApi'

// ── Thunks ──────────────────────────────────────────────────────────────────

export const fetchMyBanks = createAsyncThunk(
  'questionBank/fetchAll',
  async (_, { rejectWithValue }) => {
    try {
      const res = await questionApi.getMyBanks()
      return res.data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể tải danh sách ngân hàng')
    }
  }
)

export const createBank = createAsyncThunk(
  'questionBank/create',
  async (data, { rejectWithValue }) => {
    try {
      const res = await questionApi.createBank(data)
      return res.data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể tạo ngân hàng')
    }
  }
)

export const updateBank = createAsyncThunk(
  'questionBank/update',
  async ({ id, data }, { rejectWithValue }) => {
    try {
      const res = await questionApi.updateBank(id, data)
      return res.data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể cập nhật ngân hàng')
    }
  }
)

export const deleteBank = createAsyncThunk(
  'questionBank/delete',
  async (id, { rejectWithValue }) => {
    try {
      await questionApi.deleteBank(id)
      return id
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể xóa ngân hàng')
    }
  }
)

// ── Slice ────────────────────────────────────────────────────────────────────

const questionBankSlice = createSlice({
  name: 'questionBank',
  initialState: {
    list: [],
    status: 'idle',   // idle | loading | succeeded | failed
    error: null,
    deletingId: null,
    submitStatus: 'idle',
    submitError: null,
    filter: { subject: '', gradeLevel: '' },
  },
  reducers: {
    setFilter: (state, { payload }) => {
      state.filter = { ...state.filter, ...payload }
    },
    clearFilter: (state) => {
      state.filter = { subject: '', gradeLevel: '' }
    },
    clearSubmitStatus: (state) => {
      state.submitStatus = 'idle'
      state.submitError = null
    },
  },
  extraReducers: (builder) => {
    builder
      // fetchMyBanks
      .addCase(fetchMyBanks.pending, (state) => { state.status = 'loading'; state.error = null })
      .addCase(fetchMyBanks.fulfilled, (state, { payload }) => { state.status = 'succeeded'; state.list = payload })
      .addCase(fetchMyBanks.rejected, (state, { payload }) => { state.status = 'failed'; state.error = payload })
      // createBank
      .addCase(createBank.pending, (state) => { state.submitStatus = 'loading'; state.submitError = null })
      .addCase(createBank.fulfilled, (state, { payload }) => {
        state.submitStatus = 'succeeded'
        state.list = [payload, ...state.list]
      })
      .addCase(createBank.rejected, (state, { payload }) => { state.submitStatus = 'failed'; state.submitError = payload })
      // updateBank
      .addCase(updateBank.pending, (state) => { state.submitStatus = 'loading'; state.submitError = null })
      .addCase(updateBank.fulfilled, (state, { payload }) => {
        state.submitStatus = 'succeeded'
        state.list = state.list.map((b) => (b.id === payload.id ? payload : b))
      })
      .addCase(updateBank.rejected, (state, { payload }) => { state.submitStatus = 'failed'; state.submitError = payload })
      // deleteBank
      .addCase(deleteBank.pending, (state, { meta }) => { state.deletingId = meta.arg })
      .addCase(deleteBank.fulfilled, (state, { payload }) => {
        state.list = state.list.filter((b) => b.id !== payload)
        state.deletingId = null
      })
      .addCase(deleteBank.rejected, (state, { payload }) => { state.deletingId = null; state.error = payload })
  },
})

export const { setFilter, clearFilter, clearSubmitStatus } = questionBankSlice.actions
export default questionBankSlice.reducer
