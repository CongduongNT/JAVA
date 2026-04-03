import React, { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { X } from 'lucide-react'
import { toast } from 'sonner'
import questionApi from '@/services/questionApi'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

// ── Constants ────────────────────────────────────────────────────────────────

const QUESTION_TYPES = [
  { value: 'MULTIPLE_CHOICE', label: 'Trắc nghiệm (4 đáp án)' },
  { value: 'SHORT_ANSWER',    label: 'Trả lời ngắn' },
  { value: 'FILL_IN_BLANK',   label: 'Điền khuyết (___)' },
]

const DIFFICULTIES = [
  { value: 'EASY',   label: 'Dễ' },
  { value: 'MEDIUM', label: 'Trung bình' },
  { value: 'HARD',   label: 'Khó' },
]

const DEFAULT_OPTIONS = [
  { label: 'A', text: '', isCorrect: true  },
  { label: 'B', text: '', isCorrect: false },
  { label: 'C', text: '', isCorrect: false },
  { label: 'D', text: '', isCorrect: false },
]

// ── Shared style helpers ─────────────────────────────────────────────────────

const selectCls =
  'h-8 w-full rounded-lg border border-input bg-background px-2.5 text-sm outline-none focus:border-ring transition-colors'

const textareaCls =
  'w-full rounded-lg border border-input bg-background px-2.5 py-2 text-sm outline-none focus:border-ring resize-none transition-colors'

// ── Multiple Choice Options panel ────────────────────────────────────────────

function MultipleChoiceOptions({ options, onChange, onCorrectChange }) {
  return (
    <div className="space-y-2">
      <div className="flex items-baseline justify-between">
        <Label>Các đáp án <span className="text-destructive">*</span></Label>
        <span className="text-xs text-muted-foreground">Chọn radio → đáp án đúng</span>
      </div>
      <div className="space-y-2">
        {options.map((opt, idx) => (
          <div
            key={opt.label}
            className={`flex items-center gap-2.5 rounded-lg border p-2.5 transition-colors ${
              opt.isCorrect ? 'border-primary/50 bg-primary/5' : 'border-border bg-card'
            }`}
          >
            {/* Correct radio */}
            <input
              type="radio"
              name="correctOption"
              checked={opt.isCorrect}
              onChange={() => onCorrectChange(idx)}
              className="size-4 accent-primary shrink-0 cursor-pointer"
              title="Đánh dấu đáp án đúng"
            />

            {/* Label badge */}
            <span className="shrink-0 w-6 h-6 flex items-center justify-center rounded-full bg-muted text-[11px] font-bold text-muted-foreground">
              {opt.label}
            </span>

            {/* Text input */}
            <input
              type="text"
              placeholder={`Nội dung đáp án ${opt.label}...`}
              value={opt.text}
              onChange={(e) => onChange(idx, e.target.value)}
              className="flex-1 h-7 bg-transparent text-sm outline-none placeholder:text-muted-foreground"
            />

            {opt.isCorrect && (
              <span className="shrink-0 text-[10px] font-semibold text-primary">✓ Đúng</span>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}

// ── Main Modal ───────────────────────────────────────────────────────────────

export default function QuestionFormModal({ bankId, initialData, onClose, onSuccess }) {
  const isEdit = !!initialData
  const [submitting, setSubmitting] = useState(false)

  // Options state (MULTIPLE_CHOICE only)
  const [options, setOptions] = useState(() => {
    if (initialData?.options?.length) {
      return initialData.options.map((o) => ({ ...o }))
    }
    return DEFAULT_OPTIONS.map((o) => ({ ...o }))
  })

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm({
    defaultValues: {
      type:          initialData?.type          ?? 'MULTIPLE_CHOICE',
      content:       initialData?.content       ?? '',
      difficulty:    initialData?.difficulty    ?? 'MEDIUM',
      topic:         initialData?.topic         ?? '',
      correctAnswer: initialData?.correctAnswer ?? '',
      explanation:   initialData?.explanation   ?? '',
    },
  })

  const type = watch('type')

  // Reset options to blank when switching TO multiple choice on create
  useEffect(() => {
    if (!isEdit && type === 'MULTIPLE_CHOICE') {
      setOptions(DEFAULT_OPTIONS.map((o) => ({ ...o })))
    }
  }, [type, isEdit])

  // Options handlers
  const handleOptionText    = (idx, text) => setOptions((prev) => prev.map((o, i) => i === idx ? { ...o, text } : o))
  const handleCorrectChange = (idx) => setOptions((prev) => prev.map((o, i) => ({ ...o, isCorrect: i === idx })))

  // Submit
  const onSubmit = async (data) => {
    if (data.type === 'MULTIPLE_CHOICE') {
      const filled   = options.filter((o) => o.text.trim())
      const hasRight = options.some((o) => o.isCorrect && o.text.trim())
      if (filled.length < 2) { toast.error('Cần ít nhất 2 đáp án có nội dung.'); return }
      if (!hasRight)         { toast.error('Vui lòng chọn đáp án đúng.');         return }
    }

    const payload = {
      bankId,
      content:    data.content,
      type:       data.type,
      difficulty: data.difficulty,
      topic:      data.topic      || undefined,
      explanation:data.explanation|| undefined,
      ...(data.type === 'MULTIPLE_CHOICE'
        ? {
            options:       options.filter((o) => o.text.trim()),
            correctAnswer: options.find((o) => o.isCorrect)?.text ?? '',
          }
        : { correctAnswer: data.correctAnswer }),
    }

    setSubmitting(true)
    try {
      if (isEdit) await questionApi.updateQuestion(initialData.id, payload)
      else        await questionApi.createQuestion(payload)
      toast.success(isEdit ? 'Đã cập nhật câu hỏi!' : 'Đã tạo câu hỏi mới!')
      onSuccess()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={(e) => { if (e.target === e.currentTarget) onClose() }}
    >
      <div className="w-full max-w-2xl max-h-[90vh] overflow-y-auto bg-card rounded-xl border border-border shadow-xl">

        {/* Header – sticky so always visible while scrolling */}
        <div className="sticky top-0 z-10 flex items-center justify-between px-5 py-4 border-b border-border bg-card">
          <h2 className="text-base font-semibold">
            {isEdit ? 'Chỉnh sửa câu hỏi' : 'Thêm câu hỏi mới'}
          </h2>
          <button
            onClick={onClose}
            className="text-muted-foreground hover:text-foreground transition-colors"
            aria-label="Đóng"
          >
            <X className="size-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="p-5 space-y-5">

          {/* ── Row 1: Type + Difficulty ──────────────────────────────── */}
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label>Loại câu hỏi <span className="text-destructive">*</span></Label>
              <select className={selectCls} {...register('type')}>
                {QUESTION_TYPES.map((t) => (
                  <option key={t.value} value={t.value}>{t.label}</option>
                ))}
              </select>
            </div>
            <div className="space-y-1.5">
              <Label>Độ khó</Label>
              <select className={selectCls} {...register('difficulty')}>
                {DIFFICULTIES.map((d) => (
                  <option key={d.value} value={d.value}>{d.label}</option>
                ))}
              </select>
            </div>
          </div>

          {/* ── Question content ──────────────────────────────────────── */}
          <div className="space-y-1.5">
            <Label>
              Nội dung câu hỏi <span className="text-destructive">*</span>
              {type === 'FILL_IN_BLANK' && (
                <span className="ml-2 text-xs font-normal text-muted-foreground">
                  — dùng <code className="bg-muted px-1 rounded">___</code> để đánh dấu chỗ trống
                </span>
              )}
            </Label>
            <textarea
              rows={3}
              aria-invalid={!!errors.content}
              placeholder={
                type === 'FILL_IN_BLANK'
                  ? 'VD: Thủ đô của Việt Nam là ___, thuộc khu vực ___.'
                  : 'Nhập nội dung câu hỏi...'
              }
              className={textareaCls}
              {...register('content', { required: 'Nội dung câu hỏi là bắt buộc' })}
            />
            {errors.content && (
              <p className="text-xs text-destructive">{errors.content.message}</p>
            )}
          </div>

          {/* ── MULTIPLE_CHOICE options ───────────────────────────────── */}
          {type === 'MULTIPLE_CHOICE' && (
            <MultipleChoiceOptions
              options={options}
              onChange={handleOptionText}
              onCorrectChange={handleCorrectChange}
            />
          )}

          {/* ── SHORT_ANSWER / FILL_IN_BLANK correct answer ───────────── */}
          {(type === 'SHORT_ANSWER' || type === 'FILL_IN_BLANK') && (
            <div className="space-y-1.5">
              <Label>
                {type === 'FILL_IN_BLANK'
                  ? 'Đáp án (các chỗ trống, cách nhau bằng dấu phẩy)'
                  : 'Đáp án đúng'}
                <span className="text-destructive"> *</span>
              </Label>
              <Input
                aria-invalid={!!errors.correctAnswer}
                placeholder={
                  type === 'FILL_IN_BLANK'
                    ? 'VD: Hà Nội, Đông Nam Á'
                    : 'Nhập đáp án đúng...'
                }
                {...register('correctAnswer', { required: 'Đáp án là bắt buộc' })}
              />
              {errors.correctAnswer && (
                <p className="text-xs text-destructive">{errors.correctAnswer.message}</p>
              )}
              {type === 'FILL_IN_BLANK' && (
                <p className="text-xs text-muted-foreground">
                  Nhập theo đúng thứ tự các chỗ trống, cách nhau bằng dấu phẩy.
                </p>
              )}
            </div>
          )}

          {/* ── Topic ────────────────────────────────────────────────── */}
          <div className="space-y-1.5">
            <Label>Chủ đề / Chương</Label>
            <Input placeholder="VD: Chương 1 – Đại số, Địa lý..." {...register('topic')} />
          </div>

          {/* ── Explanation ───────────────────────────────────────────── */}
          <div className="space-y-1.5">
            <Label>Giải thích <span className="text-xs font-normal text-muted-foreground">(tùy chọn)</span></Label>
            <textarea
              rows={2}
              placeholder="Giải thích câu trả lời hoặc kiến thức liên quan..."
              className={textareaCls}
              {...register('explanation')}
            />
          </div>

          {/* ── Actions ──────────────────────────────────────────────── */}
          <div className="flex justify-end gap-2 pt-1">
            <Button type="button" variant="outline" onClick={onClose}>Hủy</Button>
            <Button type="submit" disabled={submitting}>
              {submitting ? 'Đang lưu...' : isEdit ? 'Cập nhật' : 'Tạo câu hỏi'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
