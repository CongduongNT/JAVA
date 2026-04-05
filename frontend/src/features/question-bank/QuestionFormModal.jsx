import React, { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { X } from 'lucide-react'
import { toast } from 'sonner'
import questionApi from '@/services/questionApi'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

const QUESTION_TYPES = [
  { value: 'MULTIPLE_CHOICE', label: 'Trac nghiem (4 dap an)' },
  { value: 'SHORT_ANSWER', label: 'Tra loi ngan' },
  { value: 'FILL_IN_BLANK', label: 'Dien khuyet (___)' },
]

const DIFFICULTIES = [
  { value: 'EASY', label: 'De' },
  { value: 'MEDIUM', label: 'Trung binh' },
  { value: 'HARD', label: 'Kho' },
]

const DEFAULT_OPTIONS = [
  { label: 'A', text: '', isCorrect: true },
  { label: 'B', text: '', isCorrect: false },
  { label: 'C', text: '', isCorrect: false },
  { label: 'D', text: '', isCorrect: false },
]

const selectCls =
  'h-8 w-full rounded-lg border border-input bg-background px-2.5 text-sm outline-none transition-colors focus:border-ring'

const textareaCls =
  'w-full resize-none rounded-lg border border-input bg-background px-2.5 py-2 text-sm outline-none transition-colors focus:border-ring'

function MultipleChoiceOptions({ options, onChange, onCorrectChange }) {
  return (
    <div className="space-y-2">
      <div className="flex items-baseline justify-between">
        <Label>Cac dap an <span className="text-destructive">*</span></Label>
        <span className="text-xs text-muted-foreground">Chon radio de dat dap an dung</span>
      </div>
      <div className="space-y-2">
        {options.map((option, index) => (
          <div
            key={option.label}
            className={`flex items-center gap-2.5 rounded-lg border p-2.5 transition-colors ${
              option.isCorrect ? 'border-primary/50 bg-primary/5' : 'border-border bg-card'
            }`}
          >
            <input
              type="radio"
              name="correctOption"
              checked={option.isCorrect}
              onChange={() => onCorrectChange(index)}
              className="size-4 shrink-0 cursor-pointer accent-primary"
              title="Danh dau dap an dung"
            />
            <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-muted text-[11px] font-bold text-muted-foreground">
              {option.label}
            </span>
            <input
              type="text"
              placeholder={`Noi dung dap an ${option.label}...`}
              value={option.text}
              onChange={(event) => onChange(index, event.target.value)}
              className="h-7 flex-1 bg-transparent text-sm outline-none placeholder:text-muted-foreground"
            />
            {option.isCorrect && <span className="shrink-0 text-[10px] font-semibold text-primary">Dung</span>}
          </div>
        ))}
      </div>
    </div>
  )
}

export default function QuestionFormModal({ bankId, initialData, onClose, onSuccess }) {
  const isEdit = Boolean(initialData)
  const [submitting, setSubmitting] = useState(false)
  const [options, setOptions] = useState(() => {
    if (initialData?.options?.length) return initialData.options.map((option) => ({ ...option }))
    return DEFAULT_OPTIONS.map((option) => ({ ...option }))
  })

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm({
    defaultValues: {
      type: initialData?.type ?? 'MULTIPLE_CHOICE',
      content: initialData?.content ?? '',
      difficulty: initialData?.difficulty ?? 'MEDIUM',
      topic: initialData?.topic ?? '',
      correctAnswer: initialData?.correctAnswer ?? '',
      explanation: initialData?.explanation ?? '',
    },
  })

  const type = watch('type')

  useEffect(() => {
    if (!isEdit && type === 'MULTIPLE_CHOICE') {
      setOptions(DEFAULT_OPTIONS.map((option) => ({ ...option })))
    }
  }, [isEdit, type])

  const handleOptionText = (index, text) => {
    setOptions((prev) => prev.map((option, idx) => (idx === index ? { ...option, text } : option)))
  }

  const handleCorrectChange = (index) => {
    setOptions((prev) => prev.map((option, idx) => ({ ...option, isCorrect: idx === index })))
  }

  const onSubmit = async (data) => {
    if (data.type === 'MULTIPLE_CHOICE') {
      const filled = options.filter((option) => option.text.trim())
      const hasRight = options.some((option) => option.isCorrect && option.text.trim())
      if (filled.length < 2) {
        toast.error('Can it nhat 2 dap an co noi dung.')
        return
      }
      if (!hasRight) {
        toast.error('Vui long chon dap an dung.')
        return
      }
    }

    const payload = {
      content: data.content,
      type: data.type,
      difficulty: data.difficulty,
      topic: data.topic || undefined,
      explanation: data.explanation || undefined,
      ...(data.type === 'MULTIPLE_CHOICE'
        ? {
            options: options.filter((option) => option.text.trim()),
            correctAnswer: options.find((option) => option.isCorrect)?.text ?? '',
          }
        : { correctAnswer: data.correctAnswer }),
    }

    if (!isEdit) {
      payload.bankId = bankId
    }

    setSubmitting(true)
    try {
      if (isEdit) await questionApi.updateQuestion(initialData.id, payload)
      else await questionApi.createQuestion(payload)
      toast.success(isEdit ? 'Da cap nhat cau hoi!' : 'Da tao cau hoi moi!')
      onSuccess()
    } catch (error) {
      toast.error(error.response?.data?.message || 'Co loi xay ra, vui long thu lai.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={(event) => {
        if (event.target === event.currentTarget) onClose()
      }}
    >
      <div className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-xl border border-border bg-card shadow-xl">
        <div className="sticky top-0 z-10 flex items-center justify-between border-b border-border bg-card px-5 py-4">
          <h2 className="text-base font-semibold">{isEdit ? 'Chinh sua cau hoi' : 'Them cau hoi moi'}</h2>
          <button
            onClick={onClose}
            className="text-muted-foreground transition-colors hover:text-foreground"
            aria-label="Dong"
          >
            <X className="size-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5 p-5">
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label>Loai cau hoi <span className="text-destructive">*</span></Label>
              <select className={selectCls} {...register('type')}>
                {QUESTION_TYPES.map((questionType) => (
                  <option key={questionType.value} value={questionType.value}>{questionType.label}</option>
                ))}
              </select>
            </div>
            <div className="space-y-1.5">
              <Label>Do kho</Label>
              <select className={selectCls} {...register('difficulty')}>
                {DIFFICULTIES.map((difficulty) => (
                  <option key={difficulty.value} value={difficulty.value}>{difficulty.label}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="space-y-1.5">
            <Label>Noi dung cau hoi <span className="text-destructive">*</span></Label>
            <textarea
              rows={3}
              aria-invalid={Boolean(errors.content)}
              placeholder={type === 'FILL_IN_BLANK'
                ? 'VD: Thu do cua Viet Nam la ___, thuoc khu vuc ___.'
                : 'Nhap noi dung cau hoi...'}
              className={textareaCls}
              {...register('content', { required: 'Noi dung cau hoi la bat buoc' })}
            />
            {errors.content && <p className="text-xs text-destructive">{errors.content.message}</p>}
          </div>

          {type === 'MULTIPLE_CHOICE' && (
            <MultipleChoiceOptions
              options={options}
              onChange={handleOptionText}
              onCorrectChange={handleCorrectChange}
            />
          )}

          {(type === 'SHORT_ANSWER' || type === 'FILL_IN_BLANK') && (
            <div className="space-y-1.5">
              <Label>
                {type === 'FILL_IN_BLANK'
                  ? 'Dap an (cac cho trong, cach nhau bang dau phay)'
                  : 'Dap an dung'}
                <span className="text-destructive"> *</span>
              </Label>
              <Input
                aria-invalid={Boolean(errors.correctAnswer)}
                placeholder={type === 'FILL_IN_BLANK' ? 'VD: Ha Noi, Dong Nam A' : 'Nhap dap an dung...'}
                {...register('correctAnswer', { required: 'Dap an la bat buoc' })}
              />
              {errors.correctAnswer && <p className="text-xs text-destructive">{errors.correctAnswer.message}</p>}
              {type === 'FILL_IN_BLANK' && (
                <p className="text-xs text-muted-foreground">
                  Nhap theo dung thu tu cac cho trong, cach nhau bang dau phay.
                </p>
              )}
            </div>
          )}

          <div className="space-y-1.5">
            <Label>Chu de / Chuong</Label>
            <Input placeholder="VD: Chuong 1 - Dai so, Dia ly..." {...register('topic')} />
          </div>

          <div className="space-y-1.5">
            <Label>Giai thich <span className="text-xs font-normal text-muted-foreground">(tuy chon)</span></Label>
            <textarea
              rows={2}
              placeholder="Giai thich cau tra loi hoac kien thuc lien quan..."
              className={textareaCls}
              {...register('explanation')}
            />
          </div>

          <div className="flex justify-end gap-2 pt-1">
            <Button type="button" variant="outline" onClick={onClose}>Huy</Button>
            <Button type="submit" disabled={submitting}>
              {submitting ? 'Dang luu...' : isEdit ? 'Cap nhat' : 'Tao cau hoi'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
