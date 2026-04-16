import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate, Link } from 'react-router-dom'
import { gradingApi } from '../../services/gradingApi'
import {
  ArrowLeft, Search, Filter, CheckCircle2,
  AlertCircle, MessageSquare, Loader2, RefreshCw,
  BarChart3, User, Hash
} from 'lucide-react'
import { toast } from 'sonner'

// ─── Percentage Badge ───────────────────────────────────────────────────────────

const PercentageBadge = ({ value }) => {
  const num = parseFloat(value)
  const colorClass =
    num >= 80 ? 'bg-emerald-100 text-emerald-700 border-emerald-300' :
    num >= 50 ? 'bg-amber-100 text-amber-700 border-amber-300' :
                'bg-red-100 text-red-700 border-red-300'

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${colorClass}`}>
      {num.toFixed(1)}%
    </span>
  )
}

// ─── Score Display ─────────────────────────────────────────────────────────────

const ScoreDisplay = ({ score, possible }) => (
  <span className="font-mono text-sm">
    <span className="font-semibold">{parseFloat(score).toFixed(1)}</span>
    <span className="text-gray-400">/{parseFloat(possible).toFixed(1)}</span>
  </span>
)

// ─── Loading Skeleton ──────────────────────────────────────────────────────────

const TableSkeleton = () => (
  <div className="space-y-3">
    {[...Array(5)].map((_, i) => (
      <div key={i} className="flex items-center gap-4 p-4 bg-white rounded-lg border animate-pulse">
        <div className="w-8 h-4 bg-gray-200 rounded" />
        <div className="flex-1 h-4 bg-gray-200 rounded" />
        <div className="w-20 h-4 bg-gray-200 rounded" />
        <div className="w-16 h-4 bg-gray-200 rounded" />
        <div className="w-16 h-4 bg-gray-200 rounded" />
      </div>
    ))}
  </div>
)

// ─── Empty State ───────────────────────────────────────────────────────────────

const EmptyState = ({ examId }) => (
  <div className="text-center py-16">
    <BarChart3 className="mx-auto h-12 w-12 text-gray-300 mb-4" />
    <h3 className="text-lg font-medium text-gray-900 mb-1">Chưa có kết quả chấm</h3>
    <p className="text-sm text-gray-500">
      Kết quả chấm cho bài thi này sẽ xuất hiện sau khi OCR hoàn tất.
    </p>
    <Link
      to="/exams"
      className="mt-4 inline-flex items-center gap-2 text-sm text-blue-600 hover:underline"
    >
      <ArrowLeft className="w-4 h-4" />
      Quay lại danh sách bài thi
    </Link>
  </div>
)

// ─── Main Component ────────────────────────────────────────────────────────────

export default function GradingResultsList() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const examId = searchParams.get('exam_id')

  const [results, setResults] = useState([])
  const [pagination, setPagination] = useState({ page: 0, size: 20, totalElements: 0, totalPages: 0 })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')

  // Fetch grading results
  const fetchResults = async (page = 0) => {
    if (!examId) return
    setLoading(true)
    setError(null)
    try {
      const res = await gradingApi.getGradingResults({ examId, page, size: pagination.size })
      setResults(res.data.content)
      setPagination({
        page: res.data.page,
        size: res.data.size,
        totalElements: res.data.totalElements,
        totalPages: res.data.totalPages,
      })
    } catch (err) {
      const msg = err.response?.data?.message || 'Không thể tải kết quả chấm'
      setError(msg)
      toast.error(msg)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (examId) fetchResults()
  }, [examId])

  // Filter by search (client-side for small result sets)
  const filtered = results.filter(r => {
    if (!search) return true
    const q = search.toLowerCase()
    return r.studentName?.toLowerCase().includes(q) || r.studentCode?.toLowerCase().includes(q)
  })

  const handleRowClick = (result) => {
    navigate(`/grading/${result.id}?exam_id=${examId}`)
  }

  if (!examId) {
    return (
      <div className="p-8 text-center">
        <AlertCircle className="mx-auto h-12 w-12 text-red-400 mb-4" />
        <p className="text-gray-600">Thiếu exam_id. Vui lòng truy cập từ danh sách bài thi.</p>
        <Link to="/exams" className="mt-4 inline-flex items-center gap-2 text-blue-600 hover:underline">
          <ArrowLeft className="w-4 h-4" /> Danh sách bài thi
        </Link>
      </div>
    )
  }

  return (
    <div className="p-6 max-w-6xl mx-auto">
      {/* Header */}
      <div className="flex items-center gap-3 mb-6">
        <Link
          to="/exams"
          className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
        >
          <ArrowLeft className="w-5 h-5 text-gray-600" />
        </Link>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Kết quả chấm bài thi</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            {pagination.totalElements > 0
              ? `${pagination.totalElements} học sinh đã làm bài`
              : 'Chưa có dữ liệu'}
          </p>
        </div>
      </div>

      {/* Toolbar */}
      <div className="flex items-center gap-3 mb-4">
        <div className="relative flex-1 max-w-xs">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            placeholder="Tìm theo tên hoặc mã học sinh..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full pl-9 pr-4 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <button
          onClick={() => fetchResults()}
          className="flex items-center gap-1.5 px-3 py-2 text-sm border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          Làm mới
        </button>
      </div>

      {/* Content */}
      {loading ? (
        <TableSkeleton />
      ) : error ? (
        <div className="text-center py-12">
          <AlertCircle className="mx-auto h-10 w-10 text-red-400 mb-3" />
          <p className="text-gray-600 mb-3">{error}</p>
          <button
            onClick={() => fetchResults()}
            className="text-blue-600 hover:underline text-sm"
          >
            Thử lại
          </button>
        </div>
      ) : filtered.length === 0 ? (
        <EmptyState examId={examId} />
      ) : (
        <>
          {/* Table */}
          <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-200">
                  <th className="px-4 py-3 text-left font-medium text-gray-500 w-10">#</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-500">
                    <div className="flex items-center gap-1.5">
                      <User className="w-3.5 h-3.5" /> Họ tên
                    </div>
                  </th>
                  <th className="px-4 py-3 text-left font-medium text-gray-500">
                    <div className="flex items-center gap-1.5">
                      <Hash className="w-3.5 h-3.5" /> Mã HS
                    </div>
                  </th>
                  <th className="px-4 py-3 text-left font-medium text-gray-500">Điểm</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-500">%</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-500">Đúng/Sai/Trống</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-500">Feedback</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filtered.map((r, idx) => (
                  <tr
                    key={r.id}
                    onClick={() => handleRowClick(r)}
                    className="hover:bg-blue-50 cursor-pointer transition-colors"
                  >
                    <td className="px-4 py-3 text-gray-400">{idx + 1}</td>
                    <td className="px-4 py-3 font-medium text-gray-900">{r.studentName}</td>
                    <td className="px-4 py-3 text-gray-500">{r.studentCode || '—'}</td>
                    <td className="px-4 py-3">
                      <ScoreDisplay score={r.totalScore} possible={r.totalPossible} />
                    </td>
                    <td className="px-4 py-3">
                      <PercentageBadge value={r.percentage} />
                    </td>
                    <td className="px-4 py-3 text-gray-600">
                      <span className="text-emerald-600">{r.correctCount} ✓</span>
                      {' '}
                      <span className="text-red-500">{r.wrongCount} ✗</span>
                      {r.blankCount > 0 && (
                        <>
                          {' '}
                          <span className="text-gray-400">{r.blankCount} —</span>
                        </>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      {r.hasFeedback ? (
                        <span className="inline-flex items-center gap-1 text-emerald-600">
                          <CheckCircle2 className="w-4 h-4" />
                        </span>
                      ) : (
                        <span className="text-gray-300">—</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {pagination.totalPages > 1 && (
            <div className="flex items-center justify-between mt-4">
              <p className="text-sm text-gray-500">
                Trang {pagination.page + 1} / {pagination.totalPages}
                {' '}&mdash;{' '}
                {pagination.totalElements} kết quả
              </p>
              <div className="flex gap-2">
                <button
                  onClick={() => fetchResults(pagination.page - 1)}
                  disabled={pagination.page === 0}
                  className="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50"
                >
                  ← Trước
                </button>
                <button
                  onClick={() => fetchResults(pagination.page + 1)}
                  disabled={pagination.page >= pagination.totalPages - 1}
                  className="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50"
                >
                  Sau →
                </button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  )
}
