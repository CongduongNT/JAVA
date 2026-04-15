import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import answerSheetApi from '@/services/answerSheetApi'

// ─── Async Thunks ──────────────────────────────────────────────────────────────

export const fetchAnswerSheets = createAsyncThunk(
  'answerSheets/fetchAll',
  async ({ examId, page = 0, size = 20 } = {}, { rejectWithValue }) => {
    try {
      const params = { page, size }
      if (examId) params.exam_id = examId
      const res = await answerSheetApi.getAnswerSheets(params)
      return res.data // PageResponse<AnswerSheetDTO>
    } catch (err) {
      return rejectWithValue(
        err.response?.data?.message || 'Không thể tải danh sách bài làm'
      )
    }
  }
)

export const uploadAnswerSheets = createAsyncThunk(
  'answerSheets/upload',
  async ({ examId, files }, { rejectWithValue }) => {
    try {
      const res = await answerSheetApi.uploadAnswerSheets(examId, files)
      return res.data // List<AnswerSheetDTO>
    } catch (err) {
      return rejectWithValue(
        err.response?.data?.message || 'Không thể tải lên bài làm'
      )
    }
  }
)

export const triggerOcr = createAsyncThunk(
  'answerSheets/process',
  async (id, { rejectWithValue }) => {
    try {
      const res = await answerSheetApi.processAnswerSheet(id)
      return res.data // AnswerSheetDTO (updated)
    } catch (err) {
      return rejectWithValue(
        err.response?.data?.message || 'Không thể xử lý OCR'
      )
    }
  }
)

export const fetchSheetDetail = createAsyncThunk(
  'answerSheets/fetchDetail',
  async (id, { rejectWithValue }) => {
    try {
      const res = await answerSheetApi.getAnswerSheet(id)
      return res.data // AnswerSheetDTO (with ocrRawData)
    } catch (err) {
      return rejectWithValue(
        err.response?.data?.message || 'Không thể tải chi tiết bài làm'
      )
    }
  }
)

// ─── Slice ─────────────────────────────────────────────────────────────────────

const answerSheetSlice = createSlice({
  name: 'answerSheets',
  initialState: {
    // list state
    list: [],
    totalElements: 0,
    totalPages: 0,
    status: 'idle',   // idle | loading | succeeded | failed
    error: null,

    // upload state
    uploadStatus: 'idle',
    uploadError: null,

    // OCR processing – track which IDs are being processed
    processingIds: [],

    // detail / OCR result modal
    detailSheet: null,
    detailStatus: 'idle',
    detailError: null,
  },
  reducers: {
    clearUploadStatus: (state) => {
      state.uploadStatus = 'idle'
      state.uploadError = null
    },
    clearDetailSheet: (state) => {
      state.detailSheet = null
      state.detailStatus = 'idle'
      state.detailError = null
    },
  },
  extraReducers: (builder) => {
    builder
      // ── fetchAnswerSheets ─────────────────────────────────────────────────
      .addCase(fetchAnswerSheets.pending, (state) => {
        state.status = 'loading'
        state.error = null
      })
      .addCase(fetchAnswerSheets.fulfilled, (state, { payload }) => {
        state.status = 'succeeded'
        state.list = payload.content ?? []
        state.totalElements = payload.totalElements ?? 0
        state.totalPages = payload.totalPages ?? 0
      })
      .addCase(fetchAnswerSheets.rejected, (state, { payload }) => {
        state.status = 'failed'
        state.error = payload
      })

      // ── uploadAnswerSheets ────────────────────────────────────────────────
      .addCase(uploadAnswerSheets.pending, (state) => {
        state.uploadStatus = 'loading'
        state.uploadError = null
      })
      .addCase(uploadAnswerSheets.fulfilled, (state, { payload }) => {
        state.uploadStatus = 'succeeded'
        // Prepend newly uploaded sheets so they appear at the top
        const uploaded = Array.isArray(payload) ? payload : []
        state.list = [...uploaded, ...state.list]
        state.totalElements += uploaded.length
      })
      .addCase(uploadAnswerSheets.rejected, (state, { payload }) => {
        state.uploadStatus = 'failed'
        state.uploadError = payload
      })

      // ── triggerOcr ────────────────────────────────────────────────────────
      .addCase(triggerOcr.pending, (state, { meta }) => {
        if (!state.processingIds.includes(meta.arg)) {
          state.processingIds.push(meta.arg)
        }
      })
      .addCase(triggerOcr.fulfilled, (state, { payload }) => {
        state.processingIds = state.processingIds.filter((id) => id !== payload.id)
        state.list = state.list.map((s) => (s.id === payload.id ? payload : s))
      })
      .addCase(triggerOcr.rejected, (state, { meta }) => {
        state.processingIds = state.processingIds.filter((id) => id !== meta.arg)
      })

      // ── fetchSheetDetail ──────────────────────────────────────────────────
      .addCase(fetchSheetDetail.pending, (state) => {
        state.detailStatus = 'loading'
        state.detailError = null
        state.detailSheet = null
      })
      .addCase(fetchSheetDetail.fulfilled, (state, { payload }) => {
        state.detailStatus = 'succeeded'
        state.detailSheet = payload
      })
      .addCase(fetchSheetDetail.rejected, (state, { payload }) => {
        state.detailStatus = 'failed'
        state.detailError = payload
      })
  },
})

export const { clearUploadStatus, clearDetailSheet } = answerSheetSlice.actions
export default answerSheetSlice.reducer
