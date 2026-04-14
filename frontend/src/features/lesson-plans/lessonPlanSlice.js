import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import lessonPlanApi from '@/services/lessonPlanApi'

// ─── Async Thunks ─────────────────────────────────────────────────────────────

export const fetchLessonPlans = createAsyncThunk(
  'lessonPlans/fetchAll',
  async (params, { rejectWithValue }) => {
    try {
      const res = await lessonPlanApi.getMyLessonPlans(params)
      return res.data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể tải danh sách giáo án')
    }
  }
)

export const createLessonPlan = createAsyncThunk(
  'lessonPlans/create',
  async (data, { rejectWithValue }) => {
    try {
      const res = await lessonPlanApi.createLessonPlan(data)
      return res.data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể tạo giáo án')
    }
  }
)

export const updateLessonPlan = createAsyncThunk(
  'lessonPlans/update',
  async ({ id, data }, { rejectWithValue }) => {
    try {
      const res = await lessonPlanApi.updateLessonPlan(id, data)
      return res.data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể cập nhật giáo án')
    }
  }
)

export const deleteLessonPlan = createAsyncThunk(
  'lessonPlans/delete',
  async (id, { rejectWithValue }) => {
    try {
      await lessonPlanApi.deleteLessonPlan(id)
      return id
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể xóa giáo án')
    }
  }
)

export const publishLessonPlan = createAsyncThunk(
  'lessonPlans/publish',
  async (id, { rejectWithValue }) => {
    try {
      const res = await lessonPlanApi.publishLessonPlan(id)
      return res.data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể xuất bản giáo án')
    }
  }
)

// ─── Slice ────────────────────────────────────────────────────────────────────

const lessonPlanSlice = createSlice({
  name: 'lessonPlans',
  initialState: {
    list: [],
    totalElements: 0,
    totalPages: 0,
    currentPage: 0,
    pageSize: 10,
    status: 'idle',   // idle | loading | succeeded | failed
    error: null,

    // submit state (create/update/publish/delete)
    submitStatus: 'idle',
    submitError: null,

    // filters
    filter: {
      keyword: '',
      subject: '',
      gradeLevel: '',
      status: '',
    },
  },
  reducers: {
    setFilter: (state, { payload }) => {
      state.filter = { ...state.filter, ...payload }
      state.currentPage = 0
    },
    clearFilter: (state) => {
      state.filter = { keyword: '', subject: '', gradeLevel: '', status: '' }
      state.currentPage = 0
    },
    setPage: (state, { payload }) => {
      state.currentPage = payload
    },
    clearSubmitStatus: (state) => {
      state.submitStatus = 'idle'
      state.submitError = null
    },
  },
  extraReducers: (builder) => {
    builder
      // ── fetchLessonPlans ──────────────────────────────────────────────────
      .addCase(fetchLessonPlans.pending, (state) => {
        state.status = 'loading'
        state.error = null
      })
      .addCase(fetchLessonPlans.fulfilled, (state, { payload }) => {
        state.status = 'succeeded'
        state.list = payload.content
        state.totalElements = payload.totalElements
        state.totalPages = payload.totalPages
      })
      .addCase(fetchLessonPlans.rejected, (state, { payload }) => {
        state.status = 'failed'
        state.error = payload
      })

      // ── createLessonPlan ──────────────────────────────────────────────────
      .addCase(createLessonPlan.pending, (state) => {
        state.submitStatus = 'loading'
        state.submitError = null
      })
      .addCase(createLessonPlan.fulfilled, (state) => {
        state.submitStatus = 'succeeded'
      })
      .addCase(createLessonPlan.rejected, (state, { payload }) => {
        state.submitStatus = 'failed'
        state.submitError = payload
      })

      // ── updateLessonPlan ──────────────────────────────────────────────────
      .addCase(updateLessonPlan.pending, (state) => {
        state.submitStatus = 'loading'
        state.submitError = null
      })
      .addCase(updateLessonPlan.fulfilled, (state, { payload }) => {
        state.submitStatus = 'succeeded'
        state.list = state.list.map((item) => (item.id === payload.id ? payload : item))
      })
      .addCase(updateLessonPlan.rejected, (state, { payload }) => {
        state.submitStatus = 'failed'
        state.submitError = payload
      })

      // ── deleteLessonPlan ──────────────────────────────────────────────────
      .addCase(deleteLessonPlan.pending, (state) => {
        state.submitStatus = 'loading'
        state.submitError = null
      })
      .addCase(deleteLessonPlan.fulfilled, (state, { payload }) => {
        state.submitStatus = 'succeeded'
        state.list = state.list.filter((item) => item.id !== payload)
        state.totalElements = Math.max(0, state.totalElements - 1)
      })
      .addCase(deleteLessonPlan.rejected, (state, { payload }) => {
        state.submitStatus = 'failed'
        state.submitError = payload
      })

      // ── publishLessonPlan ─────────────────────────────────────────────────
      .addCase(publishLessonPlan.pending, (state) => {
        state.submitStatus = 'loading'
        state.submitError = null
      })
      .addCase(publishLessonPlan.fulfilled, (state, { payload }) => {
        state.submitStatus = 'succeeded'
        state.list = state.list.map((item) => (item.id === payload.id ? { ...item, status: payload.status } : item))
      })
      .addCase(publishLessonPlan.rejected, (state, { payload }) => {
        state.submitStatus = 'failed'
        state.submitError = payload
      })
  },
})

export const { setFilter, clearFilter, setPage, clearSubmitStatus } = lessonPlanSlice.actions
export default lessonPlanSlice.reducer
