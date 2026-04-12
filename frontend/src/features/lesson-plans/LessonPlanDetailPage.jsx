import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate, useParams } from 'react-router-dom'
import {
  AlertTriangle,
  ArrowLeft,
  BookOpen,
  CheckCircle2,
  ClipboardList,
  Clock,
  FileText,
  Loader2,
  Package,
  Pencil,
  Send,
  Trash2,
} from 'lucide-react'
import { toast } from 'sonner'

import { deleteLessonPlan, publishLessonPlan } from './lessonPlanSlice'
import lessonPlanApi from '@/services/lessonPlanApi'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { getFrameworkLabel, getPublishMissingFields } from './lessonPlanMeta'

function StatusBadge({ status }) {
  if (status === 'PUBLISHED') {
    return (
      <span className="inline-flex items-center gap-1 rounded-full border border-green-200 bg-green-50 px-2.5 py-1 text-xs font-medium text-green-700">
        <CheckCircle2 className="size-3" />
        Đã xuất bản
      </span>
    )
  }

  return (
    <span className="inline-flex items-center gap-1 rounded-full border border-amber-200 bg-amber-50 px-2.5 py-1 text-xs font-medium text-amber-700">
      <FileText className="size-3" />
      Bản nháp
    </span>
  )
}

function InfoRow({ label, value }) {
  return (
    <div className="flex items-start gap-3">
      <span className="min-w-[120px] text-xs font-medium text-muted-foreground">{label}</span>
      <span className="flex-1 text-sm text-foreground">
        {value || <span className="italic text-muted-foreground">Chưa điền</span>}
      </span>
    </div>
  )
}

function ContentSection({ icon: Icon, title, content }) {
  return (
    <div className="overflow-hidden rounded-xl border border-border bg-card">
      <div className="flex items-center gap-2 border-b border-border bg-muted/30 px-4 py-3">
        <Icon className="size-4 text-primary" />
        <h2 className="text-sm font-semibold text-foreground">{title}</h2>
      </div>
      <div className="px-4 py-4">
        {content ? (
          <p className="whitespace-pre-wrap text-sm leading-relaxed text-foreground">{content}</p>
        ) : (
          <p className="text-sm italic text-muted-foreground">Chưa có nội dung.</p>
        )}
      </div>
    </div>
  )
}

function PublishReadinessPanel({ plan, onEdit, onPublish, isActionLoading }) {
  if (!plan) return null

  if (plan.status === 'PUBLISHED') {
    return (
      <div className="rounded-xl border border-green-200 bg-green-50/80 px-5 py-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-start gap-2">
            <CheckCircle2 className="mt-0.5 size-4 text-green-700" />
            <div>
              <h2 className="text-sm font-semibold text-green-900">Giáo án đã được xuất bản</h2>
              <p className="text-sm text-green-800">
                Bạn vẫn có thể vào màn chỉnh sửa để cập nhật nội dung nếu cần.
              </p>
            </div>
          </div>
          <Button variant="outline" size="sm" onClick={onEdit} disabled={isActionLoading}>
            <Pencil className="size-3.5" />
            Chỉnh sửa
          </Button>
        </div>
      </div>
    )
  }

  const missingFields = getPublishMissingFields(plan)
  const canPublish = missingFields.length === 0

  return (
    <div
      className={
        canPublish
          ? 'rounded-xl border border-green-200 bg-green-50/80 px-5 py-4'
          : 'rounded-xl border border-amber-200 bg-amber-50/80 px-5 py-4'
      }
    >
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            {canPublish ? (
              <CheckCircle2 className="size-4 text-green-700" />
            ) : (
              <AlertTriangle className="size-4 text-amber-700" />
            )}
            <h2 className={canPublish ? 'text-sm font-semibold text-green-900' : 'text-sm font-semibold text-amber-900'}>
              {canPublish ? 'Bản nháp đã sẵn sàng để xuất bản' : 'Bản nháp chưa thể xuất bản'}
            </h2>
          </div>
          <p className={canPublish ? 'text-sm text-green-800' : 'text-sm text-amber-800'}>
            {canPublish
              ? 'Tất cả thông tin bắt buộc đã có. Bạn có thể xuất bản ngay.'
              : 'Cần hoàn thiện các mục dưới đây trước khi gọi hành động xuất bản.'}
          </p>
          {!canPublish && (
            <div className="flex flex-wrap gap-2">
              {missingFields.map(({ field, label }) => (
                <Badge key={field} variant="outline" className="border-amber-300 bg-white/80 text-amber-900">
                  {label}
                </Badge>
              ))}
            </div>
          )}
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <Button variant="outline" size="sm" onClick={onEdit} disabled={isActionLoading}>
            <Pencil className="size-3.5" />
            Chỉnh sửa bản nháp
          </Button>
          <Button size="sm" onClick={onPublish} disabled={isActionLoading || !canPublish}>
            {isActionLoading ? <Loader2 className="size-3.5 animate-spin" /> : <Send className="size-3.5" />}
            Xuất bản
          </Button>
        </div>
      </div>
    </div>
  )
}

function DetailSkeleton() {
  return (
    <div className="space-y-5">
      <Skeleton className="h-8 w-72" />
      <Skeleton className="h-32 w-full" />
      <Skeleton className="h-48 w-full" />
      <Skeleton className="h-48 w-full" />
    </div>
  )
}

export default function LessonPlanDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const dispatch = useDispatch()
  const { submitStatus } = useSelector((state) => state.lessonPlans)

  const [plan, setPlan] = useState(null)
  const [loading, setLoading] = useState(true)
  const [publishing, setPublishing] = useState(false)

  const fetchDetail = () => {
    setLoading(true)
    lessonPlanApi.getLessonPlan(id)
      .then((res) => setPlan(res.data))
      .catch(() => {
        toast.error('Không thể tải giáo án.')
        navigate('/lesson-plans')
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    fetchDetail()
  }, [id])

  const handlePublish = async () => {
    if (!plan || plan.status === 'PUBLISHED') return

    const missingFields = getPublishMissingFields(plan)
    if (missingFields.length > 0) {
      toast.error('Giáo án chưa đủ thông tin để xuất bản.')
      return
    }

    setPublishing(true)
    try {
      const result = await dispatch(publishLessonPlan(Number(id)))
      if (result.error) {
        toast.error(result.payload || 'Không thể xuất bản giáo án.')
        return
      }

      toast.success('Đã xuất bản giáo án thành công!')
      setPlan(result.payload)
    } finally {
      setPublishing(false)
    }
  }

  const handleEdit = () => {
    navigate(`/lesson-plans/${id}/edit`)
  }

  const handleDelete = async () => {
    if (!window.confirm(`Bạn có chắc muốn xóa giáo án "${plan?.title}"?`)) return

    const result = await dispatch(deleteLessonPlan(Number(id)))
    if (result.error) {
      toast.error(result.payload || 'Không thể xóa giáo án.')
      return
    }

    toast.success('Đã xóa giáo án!')
    navigate('/lesson-plans')
  }

  const isActionLoading = publishing || submitStatus === 'loading'

  if (loading) return <DetailSkeleton />
  if (!plan) return null

  const frameworkLabel = getFrameworkLabel(plan.frameworkId)
  const missingPublishFields = getPublishMissingFields(plan)

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex items-start gap-3">
          <Button variant="ghost" size="icon" onClick={() => navigate('/lesson-plans')} aria-label="Quay lại">
            <ArrowLeft className="size-4" />
          </Button>
          <div className="space-y-1">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-xl font-bold leading-tight text-foreground">{plan.title}</h1>
              <StatusBadge status={plan.status} />
            </div>
            <div className="flex flex-wrap items-center gap-3 text-xs text-muted-foreground">
              {plan.subject && <Badge variant="secondary" className="text-[10px]">{plan.subject}</Badge>}
              {plan.gradeLevel && <span>Lớp {plan.gradeLevel}</span>}
              {plan.durationMinutes && (
                <span className="flex items-center gap-0.5">
                  <Clock className="size-3" />
                  {plan.durationMinutes} phút
                </span>
              )}
              {plan.aiGenerated && (
                <span className="inline-flex items-center rounded-full border border-blue-200 bg-blue-50 px-2 py-0.5 text-[10px] font-medium text-blue-700">
                  AI Generated
                </span>
              )}
            </div>
          </div>
        </div>

        <div className="flex shrink-0 items-center gap-2 pl-10 sm:pl-0">
          <Button variant="outline" size="sm" onClick={handleEdit} disabled={isActionLoading}>
            <Pencil className="size-3.5" />
            Chỉnh sửa
          </Button>
          <Button
            variant="outline"
            size="sm"
            className="border-destructive/30 text-destructive hover:bg-destructive/10 hover:text-destructive"
            onClick={handleDelete}
            disabled={isActionLoading}
          >
            <Trash2 className="size-3.5" />
            Xóa
          </Button>
        </div>
      </div>

      <PublishReadinessPanel
        plan={plan}
        onEdit={handleEdit}
        onPublish={handlePublish}
        isActionLoading={isActionLoading}
      />

      <div className="space-y-3 rounded-xl border border-border bg-card px-5 py-4">
        <h2 className="flex items-center gap-1.5 text-sm font-semibold text-foreground">
          <BookOpen className="size-4 text-primary" />
          Thông tin chung
        </h2>
        <div className="grid grid-cols-1 gap-2.5 sm:grid-cols-2">
          <InfoRow label="Chủ đề" value={plan.topic} />
          <InfoRow label="Môn học" value={plan.subject} />
          <InfoRow label="Khối lớp" value={plan.gradeLevel ? `Lớp ${plan.gradeLevel}` : null} />
          <InfoRow label="Thời lượng" value={plan.durationMinutes ? `${plan.durationMinutes} phút` : null} />
          <InfoRow label="Framework" value={frameworkLabel} />
          <InfoRow label="Ngày tạo" value={plan.createdAt ? new Date(plan.createdAt).toLocaleString('vi-VN') : null} />
          <InfoRow label="Cập nhật" value={plan.updatedAt ? new Date(plan.updatedAt).toLocaleString('vi-VN') : null} />
        </div>
      </div>

      <ContentSection icon={ClipboardList} title="Mục tiêu học tập" content={plan.objectives} />
      <ContentSection icon={FileText} title="Hoạt động dạy học" content={plan.activities} />
      <ContentSection icon={Send} title="Kiểm tra & Đánh giá" content={plan.assessment} />
      <ContentSection icon={Package} title="Tài liệu & Thiết bị" content={plan.materials} />

      <div className="flex flex-wrap items-center justify-end gap-2 rounded-xl border border-border bg-card px-5 py-3">
        <Button variant="outline" onClick={() => navigate('/lesson-plans')}>
          Quay lại danh sách
        </Button>
        <Button variant="outline" onClick={handleEdit} disabled={isActionLoading}>
          <Pencil className="size-3.5" />
          Chỉnh sửa
        </Button>
        {plan.status === 'DRAFT' && (
          <Button
            onClick={handlePublish}
            disabled={isActionLoading || missingPublishFields.length > 0}
          >
            {isActionLoading ? <Loader2 className="size-3.5 animate-spin" /> : <Send className="size-3.5" />}
            Xuất bản
          </Button>
        )}
      </div>
    </div>
  )
}
