import React, { useState, useEffect, useCallback } from 'react'
import { useParams, useSearchParams, Link } from 'react-router-dom'
import { gradingApi } from '../../services/gradingApi'
import {
  ArrowLeft, CheckCircle2, XCircle, HelpCircle,
  Loader2, Sparkles, Save, AlertTriangle, RefreshCw
} from 'lucide-react'
import { toast } from 'sonner'

// ─── Constants ───────────────────────────────────────────────────────────────────

const STATUS_CONFIG = {
  CORRECT: {
    icon: CheckCircle2,
    label: 'ĐÚNG',
    bg: 'bg-emerald-50',
    border: 'border-emerald-200',
    iconColor: 'text-emerald-600',
    textColor: 'text-emerald-800',
    pointsColor: 'text-emerald-700',
  },
  WRONG: {
    icon: XCircle,
    label: 'SAI',
    bg: 'bg-red-50',
    border: 'border-red-200',
    iconColor: 'text-red-600',
    textColor: 'text-red-800',
    pointsColor: 'text-red-700',
  },
  BLANK: {
    icon: HelpCircle,
    label: 'TRỐNG',
    bg: 'bg-gray-50',
    border: 'border-gray-200',
    iconColor: 'text-gray-400',
    textColor: 'text-gray-500',
    pointsColor: 'text-gray-400',
  },
  PARTIAL: {
    icon: AlertTriangle,
    label: 'TỪNG PHẦN',
    bg: 'bg-amber-50',
    border: 'border-amber-200',
    iconColor: 'text-amber-600',
    textColor: 'text-amber-800',
    pointsColor: 'text-amber-700',
  },
}

// ─── Score Summary Card ─────────────────────────────────────────────────────────

const ScoreSummary = ({ result }) => (
  <div className="bg-white border border-gray-200 rounded-xl p-6 mb-6">
    <div className="flex items-center justify-between flex-wrap gap-4">
      {/* Student info */}
      <div>
        <h1 className="text-xl font-bold text-gray-900">{result.studentName}</h1>
        {result.studentCode && (
          <p className="text-sm text-gray-500 mt-0.5">Mã: {result.studentCode}</p>
        )}
        <p className="text-sm text-gray-500">{result.examTitle}</p>
      </div>

      {/* Score */}
      <div className="flex items-center gap-6">
        <div className="text-right">
          <p className="text-xs text-gray-500 uppercase tracking-wide">Điểm</p>
          <p className="text-3xl font-bold font-mono text-gray-900">
            {parseFloat(result.totalScore).toFixed(1)}
            <span className="text-lg font-normal text-gray-400">
              /{parseFloat(result.totalPossible).toFixed(1)}
            </span>
          </p>
        </div>

        <div className="text-right">
          <p className="text-xs text-gray-500 uppercase tracking-wide">Phần trăm</p>
          <p className={`text-3xl font-bold ${
            result.percentage >= 80 ? 'text-emerald-600' :
            result.percentage >= 50 ? 'text-amber-600' : 'text-red-600'
          }`}>
            {parseFloat(result.percentage).toFixed(1)}%
          </p>
        </div>

        {/* Correct/Wrong/Blank */}
        <div className="flex items-center gap-4 text-sm">
          <span className="text-emerald-600 font-medium">{result.correctCount} ✓ Đúng</span>
          <span className="text-red-500 font-medium">{result.wrongCount} ✗ Sai</span>
          {result.blankCount > 0 && (
            <span className="text-gray-400">{result.blankCount} — Trống</span>
          )}
        </div>
      </div>
    </div>
  </div>
)

// ─── Question Row ───────────────────────────────────────────────────────────────

const QuestionRow = ({ detail }) => {
  const cfg = STATUS_CONFIG[detail.resultStatus] || STATUS_CONFIG.WRONG
  const Icon = cfg.icon

  return (
    <div className={`border rounded-xl p-4 mb-3 ${cfg.bg} ${cfg.border}`}>
      {/* Header */}
      <div className="flex items-start justify-between gap-4 mb-3">
        <div className="flex items-center gap-2">
          <Icon className={`w-5 h-5 ${cfg.iconColor} mt-0.5`} />
          <span className={`text-xs font-bold uppercase tracking-wide ${cfg.textColor}`}>
            Câu {detail.orderIndex}
          </span>
          <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${cfg.bg} ${cfg.textColor} border ${cfg.border}`}>
            {cfg.label}
          </span>
        </div>
        <div className={`font-mono text-sm font-semibold ${cfg.pointsColor}`}>
          {parseFloat(detail.pointsEarned).toFixed(1)}
          <span className="font-normal text-gray-400">/{parseFloat(detail.pointsPossible).toFixed(1)} đ</span>
        </div>
      </div>

      {/* Question */}
      <p className="text-sm text-gray-700 font-medium mb-3">{detail.questionContent}</p>

      {/* Answers */}
      <div className="grid grid-cols-2 gap-3 text-sm">
        {/* Student's answer */}
        <div className={`p-3 rounded-lg ${detail.resultStatus === 'CORRECT' ? 'bg-emerald-100 border border-emerald-200' : 'bg-white border border-gray-200'}`}>
          <p className="text-xs text-gray-500 mb-1 font-medium">Câu trả lời</p>
          <p className={`font-medium ${detail.resultStatus === 'CORRECT' ? 'text-emerald-800' : 'text-red-700'}`}>
            {detail.ocrAnswerText || <span className="italic text-gray-400">(bỏ trống)</span>}
          </p>
          {detail.ocrConfidence != null && detail.ocrConfidence < 0.5 && (
            <p className="text-xs text-amber-600 mt-1 flex items-center gap-1">
              <AlertTriangle className="w-3 h-3" />
              OCR confidence thấp ({parseFloat(detail.ocrConfidence).toFixed(2)})
            </p>
          )}
        </div>

        {/* Correct answer */}
        <div className="p-3 rounded-lg bg-white border border-gray-200">
          <p className="text-xs text-gray-500 mb-1 font-medium">Đáp án đúng</p>
          <p className="font-medium text-emerald-700">{detail.correctAnswerText}</p>
        </div>
      </div>
    </div>
  )
}

// ─── Feedback Area ──────────────────────────────────────────────────────────────

const FeedbackArea = ({ result, onSave, saving, onRequestAi }) => {
  const [feedbackText, setFeedbackText] = useState(result.teacherFeedback || '')
  const [aiSuggestion, setAiSuggestion] = useState(result.aiFeedbackSuggestion || null)
  const [showAiSuggestion, setShowAiSuggestion] = useState(false)

  // Reset when result changes
  useEffect(() => {
    setFeedbackText(result.teacherFeedback || '')
    setAiSuggestion(result.aiFeedbackSuggestion || null)
    setShowAiSuggestion(!!result.aiFeedbackSuggestion)
  }, [result.teacherFeedback, result.aiFeedbackSuggestion])

  const handleUseAiSuggestion = () => {
    if (aiSuggestion) {
      setFeedbackText(aiSuggestion)
      setShowAiSuggestion(false)
      toast.success('Đã dùng gợi ý từ AI')
    }
  }

  const handleRequestAi = async () => {
    const suggestion = await onRequestAi()
    if (suggestion) {
      setAiSuggestion(suggestion)
      setShowAiSuggestion(true)
    }
  }

  const hasChanges = feedbackText !== (result.teacherFeedback || '')

  return (
    <div className="bg-white border border-gray-200 rounded-xl p-6">
      <h2 className="text-base font-semibold text-gray-900 mb-4">
        Nhận xét của giáo viên
      </h2>

      {/* Textarea */}
      <textarea
        value={feedbackText}
        onChange={e => {
          setFeedbackText(e.target.value)
          setShowAiSuggestion(false)
        }}
        placeholder="Nhập nhận xét cho học sinh..."
        rows={4}
        className="w-full px-4 py-3 border border-gray-200 rounded-lg text-sm resize-none
                   focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent
                   placeholder:text-gray-400"
        disabled={saving}
      />

      {/* AI suggestion display */}
      {showAiSuggestion && aiSuggestion && (
        <div className="mt-3 p-4 bg-blue-50 border border-blue-200 rounded-lg">
          <div className="flex items-center justify-between mb-2">
            <div className="flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-blue-600" />
              <span className="text-xs font-semibold text-blue-700 uppercase tracking-wide">
                Gợi ý từ AI
              </span>
            </div>
            <div className="flex gap-2">
              <button
                onClick={handleUseAiSuggestion}
                className="text-xs px-3 py-1 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
              >
                Dùng gợi ý này
              </button>
              <button
                onClick={() => setShowAiSuggestion(false)}
                className="text-xs px-3 py-1 border border-gray-300 text-gray-600 rounded-md hover:bg-gray-50"
              >
                Bỏ qua
              </button>
            </div>
          </div>
          <p className="text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">
            {aiSuggestion}
          </p>
        </div>
      )}

      {/* Action buttons */}
      <div className="flex items-center justify-between mt-4">
        <button
          onClick={handleRequestAi}
          disabled={saving}
          className="flex items-center gap-2 px-4 py-2 text-sm border border-gray-200 rounded-lg
                     hover:bg-gray-50 transition-colors disabled:opacity-50"
        >
          {saving && showAiSuggestion ? (
            <Loader2 className="w-4 h-4 animate-spin" />
          ) : (
            <Sparkles className="w-4 h-4 text-blue-600" />
          )}
          Gợi ý nhận xét bằng AI
        </button>

        <button
          onClick={() => onSave(feedbackText)}
          disabled={saving || !hasChanges}
          className="flex items-center gap-2 px-5 py-2 text-sm bg-blue-600 text-white rounded-lg
                     hover:bg-blue-700 transition-colors disabled:opacity-50"
        >
          {saving ? (
            <Loader2 className="w-4 h-4 animate-spin" />
          ) : (
            <Save className="w-4 h-4" />
          )}
          Lưu nhận xét
        </button>
      </div>

      {/* Source indicator */}
      {result.feedbackSource && (
        <p className="text-xs text-gray-400 mt-3">
          Nguồn feedback: {result.feedbackSource === 'AI_EDITED' ? 'Đã chỉnh sửa từ AI' : 'Nhập tay'}
        </p>
      )}
    </div>
  )
}

// ─── Main Component ────────────────────────────────────────────────────────────

export default function GradingResultDetail() {
  const { id } = useParams()
  const [searchParams] = useSearchParams()
  const examId = searchParams.get('exam_id')

  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [saving, setSaving] = useState(false)
  const [requestingAi, setRequestingAi] = useState(false)

  // Fetch detail
  const fetchDetail = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await gradingApi.getGradingResultDetail(id)
      setResult(res.data)
    } catch (err) {
      const msg = err.response?.data?.message || 'Không thể tải kết quả chấm'
      setError(msg)
      toast.error(msg)
    } finally {
      setLoading(false)
    }
  }, [id])

  useEffect(() => {
    if (id) fetchDetail()
  }, [id, fetchDetail])

  // Save feedback
  const handleSave = async (feedbackText) => {
    setSaving(true)
    try {
      const res = await gradingApi.updateFeedback(id, {
        teacherFeedback: feedbackText,
        requestAiFeedback: false,
      })
      setResult(prev => ({
        ...prev,
        teacherFeedback: res.data.teacherFeedback,
        feedbackSource: res.data.feedbackSource,
        aiFeedbackSuggestion: res.data.aiFeedbackSuggestion,
      }))
      toast.success('Đã lưu nhận xét')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Lưu nhận xét thất bại')
    } finally {
      setSaving(false)
    }
  }

  // Request AI feedback
  const handleRequestAiFeedback = async () => {
    setRequestingAi(true)
    try {
      const res = await gradingApi.updateFeedback(id, {
        teacherFeedback: null, // keep existing
        requestAiFeedback: true,
      })
      // Update with AI suggestion
      setResult(prev => ({
        ...prev,
        aiFeedbackSuggestion: res.data.aiFeedbackSuggestion,
        feedbackSource: res.data.feedbackSource,
      }))
      return res.data.aiFeedbackSuggestion
    } catch (err) {
      toast.error(err.response?.data?.message || 'Không thể xin gợi ý từ AI')
      return null
    } finally {
      setRequestingAi(false)
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
      </div>
    )
  }

  if (error || !result) {
    return (
      <div className="p-8 text-center">
        <AlertTriangle className="mx-auto h-10 w-10 text-red-400 mb-3" />
        <p className="text-gray-600 mb-4">{error || 'Không tìm thấy kết quả'}</p>
        <Link
          to={examId ? `/grading?exam_id=${examId}` : '/exams'}
          className="inline-flex items-center gap-2 text-blue-600 hover:underline"
        >
          <ArrowLeft className="w-4 h-4" /> Quay lại
        </Link>
      </div>
    )
  }

  return (
    <div className="p-6 max-w-4xl mx-auto pb-16">
      {/* Back link */}
      <Link
        to={examId ? `/grading?exam_id=${examId}` : '/exams'}
        className="inline-flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900 mb-4
                   transition-colors"
      >
        <ArrowLeft className="w-4 h-4" />
        Quay lại danh sách kết quả
      </Link>

      {/* Score summary */}
      <ScoreSummary result={result} />

      {/* Questions */}
      <h2 className="text-base font-semibold text-gray-900 mb-4">
        Chi tiết bài làm
      </h2>
      <div className="mb-8">
        {result.details && result.details.length > 0 ? (
          result.details.map(detail => (
            <QuestionRow key={detail.id} detail={detail} />
          ))
        ) : (
          <div className="text-center py-8 text-gray-500 text-sm">
            Chưa có chi tiết câu hỏi
          </div>
        )}
      </div>

      {/* Feedback area */}
      <FeedbackArea
        result={result}
        onSave={handleSave}
        saving={saving || requestingAi}
        onRequestAi={handleRequestAiFeedback}
      />
    </div>
  )
}
