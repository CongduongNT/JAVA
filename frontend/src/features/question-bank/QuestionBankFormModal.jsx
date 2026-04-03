import React, { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useForm } from 'react-hook-form'
import { X } from 'lucide-react'
import { toast } from 'sonner'
import { createBank, updateBank, clearSubmitStatus } from './questionBankSlice'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

const SUBJECTS = [
  'Toán', 'Ngữ văn', 'Tiếng Anh', 'Vật lý', 'Hóa học',
  'Sinh học', 'Lịch sử', 'Địa lý', 'GDCD', 'Tin học', 'Công nghệ',
]
const GRADES = ['1','2','3','4','5','6','7','8','9','10','11','12']

const selectCls =
  'h-8 w-full rounded-lg border border-input bg-background px-2.5 text-sm outline-none focus:border-ring transition-colors'

export default function QuestionBankFormModal({ initialData, onClose, onSuccess }) {
  const dispatch = useDispatch()
  const { submitStatus, submitError } = useSelector((s) => s.questionBank)
  const isEdit = !!initialData

  const { register, handleSubmit, formState: { errors } } = useForm({
    defaultValues: initialData
      ? {
          name: initialData.name ?? '',
          subject: initialData.subject ?? '',
          gradeLevel: initialData.gradeLevel ?? '',
          description: initialData.description ?? '',
          isPublished: initialData.isPublished ?? false,
        }
      : { name: '', subject: '', gradeLevel: '', description: '', isPublished: false },
  })

  useEffect(() => {
    if (submitStatus === 'succeeded') {
      toast.success(isEdit ? 'Đã cập nhật ngân hàng!' : 'Đã tạo ngân hàng mới!')
      dispatch(clearSubmitStatus())
      onSuccess()
    }
    if (submitStatus === 'failed' && submitError) {
      toast.error(submitError)
      dispatch(clearSubmitStatus())
    }
  }, [submitStatus, submitError]) // eslint-disable-line

  const onSubmit = (data) => {
    if (isEdit) dispatch(updateBank({ id: initialData.id, data }))
    else dispatch(createBank(data))
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={(e) => { if (e.target === e.currentTarget) onClose() }}
    >
      <div className="w-full max-w-lg bg-card rounded-xl border border-border shadow-xl">
        {/* Header */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-border">
          <h2 className="text-base font-semibold">
            {isEdit ? 'Chỉnh sửa ngân hàng' : 'Tạo ngân hàng mới'}
          </h2>
          <button
            onClick={onClose}
            className="text-muted-foreground hover:text-foreground transition-colors"
            aria-label="Đóng"
          >
            <X className="size-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="p-5 space-y-4">
          {/* Name */}
          <div className="space-y-1.5">
            <Label htmlFor="name">
              Tên ngân hàng <span className="text-destructive">*</span>
            </Label>
            <Input
              id="name"
              placeholder="VD: Toán 10 – Chương 1: Đại số"
              aria-invalid={!!errors.name}
              {...register('name', { required: 'Tên là bắt buộc' })}
            />
            {errors.name && (
              <p className="text-xs text-destructive">{errors.name.message}</p>
            )}
          </div>

          {/* Subject + Grade */}
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label htmlFor="subject">Môn học</Label>
              <select id="subject" className={selectCls} {...register('subject')}>
                <option value="">-- Chọn môn --</option>
                {SUBJECTS.map((s) => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="gradeLevel">Lớp</Label>
              <select id="gradeLevel" className={selectCls} {...register('gradeLevel')}>
                <option value="">-- Chọn lớp --</option>
                {GRADES.map((g) => <option key={g} value={g}>Lớp {g}</option>)}
              </select>
            </div>
          </div>

          {/* Description */}
          <div className="space-y-1.5">
            <Label htmlFor="description">Mô tả</Label>
            <textarea
              id="description"
              rows={3}
              placeholder="Mô tả nội dung hoặc phạm vi kiến thức..."
              className="w-full rounded-lg border border-input bg-background px-2.5 py-2 text-sm outline-none focus:border-ring resize-none transition-colors"
              {...register('description')}
            />
          </div>

          {/* isPublished */}
          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              className="size-4 rounded border-input accent-primary"
              {...register('isPublished')}
            />
            <span className="text-sm">Công khai ngân hàng này</span>
          </label>

          {/* Actions */}
          <div className="flex justify-end gap-2 pt-1">
            <Button type="button" variant="outline" onClick={onClose}>Hủy</Button>
            <Button type="submit" disabled={submitStatus === 'loading'}>
              {submitStatus === 'loading' ? 'Đang lưu...' : isEdit ? 'Cập nhật' : 'Tạo mới'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
