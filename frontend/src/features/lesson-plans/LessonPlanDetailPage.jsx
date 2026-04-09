import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import {
  ArrowLeft, BookOpen, CheckCircle2, ClipboardList, Clock, FileText,
  Loader2, Package, Pencil, Send, Trash2,
} from 'lucide-react'
import { toast } from 'sonner'

import { deleteLessonPlan, publishLessonPlan } from './lessonPlanSlice'
import lessonPlanApi from '@/services/lessonPlanApi'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'

// ─── Status Badge ─────────────────────────────────────────────────────────────

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

// ─── Info Row ─────────────────────────────────────────────────────────────────

function InfoRow({ label, value }) {
  return (
    <div className="flex items-start gap-3">
      <span className="min-w-[120px] text-xs font-medium text-muted-foreground">{label}</span>
      <span className="flex-1 text-sm text-foreground">{value || <span className="text-muted-foreground italic">Chưa điền</span>}</span>
    </div>
  )
}

// ─── Content Section ──────────────────────────────────────────────────────────

function ContentSection({ icon: Icon, title, content }) {
  return (
    <div className="rounded-xl border border-border bg-card overflow-hidden">
      <div className="flex items-center gap-2 border-b border-border bg-muted/30 px-4 py-3">
        <Icon className="size-4 text-primary" />
        <h2 className="text-sm font-semibold text-foreground">{title}</h2>
      </div>
      <div className="px-4 py-4">
        {content ? (
          <p className="text-sm text-foreground whitespace-pre-wrap leading-relaxed">{content}</p>
        ) : (
          <p className="text-sm text-muted-foreground italic">Chưa có nội dung.</p>
        )}
      </div>
    </div>
  )
}

// ─── Skeleton Loader ──────────────────────────────────────────────────────────

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

// ─── Confirm Delete Dialog ────────────────────────────────────────────────────

function useConfirm() {
  const confirm = (message) => window.confirm(message)
  return confirm
}

// ─── Main Page ────────────────────────────────────────────────────────────────

export default function LessonPlanDetailPage() {
  const { id } = useParams()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const dispatch = useDispatch()
  const { submitStatus } = useSelector((state) => state.lessonPlans)
  const confirm = useConfirm()

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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  // Auto-publish if redirected from form with ?action=publish
  useEffect(() => {
    if (searchParams.get('action') === 'publish' && plan && plan.status === 'DRAFT') {
      handlePublish(plan)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [plan, searchParams])

  const handlePublish = async (currentPlan = plan) => {
    if (!currentPlan || currentPlan.status === 'PUBLISHED') return
    setPublishing(true)
    try {
      const result = await dispatch(publishLessonPlan(Number(id)))
      if (!result.error) {
        toast.success('Đã xuất bản giáo án thành công!')
        setPlan((prev) => ({ ...prev, status: 'PUBLISHED' }))
      } else {
        toast.error(result.payload || 'Không thể xuất bản giáo án.')
      }
    } finally {
      setPublishing(false)
    }
  }

  const handleSaveDraft = () => {
    navigate(`/lesson-plans/${id}/edit`)
  }

  const handleDelete = async () => {
    if (!confirm(`Bạn có chắc muốn xóa giáo án "${plan?.title}"?`)) return
    const result = await dispatch(deleteLessonPlan(Number(id)))
    if (!result.error) {
      toast.success('Đã xóa giáo án!')
      navigate('/lesson-plans')
    } else {
      toast.error(result.payload || 'Không thể xóa giáo án.')
    }
  }

  const isActionLoading = publishing || submitStatus === 'loading'

  if (loading) return <DetailSkeleton />

  if (!plan) return null

  const frameworkLabel = plan.frameworkId ? `Framework #${plan.frameworkId}` : null

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex items-start gap-3">
          <Button variant="ghost" size="icon" onClick={() => navigate('/lesson-plans')} aria-label="Quay lại">
            <ArrowLeft className="size-4" />
          </Button>
          <div className="space-y-1">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-xl font-bold text-foreground leading-tight">{plan.title}</h1>
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

        {/* Action buttons */}
        <div className="flex shrink-0 items-center gap-2 pl-10 sm:pl-0">
          <Button
            variant="outline"
            size="sm"
            onClick={() => navigate(`/lesson-plans/${id}/edit`)}
            disabled={isActionLoading}
          >
            <Pencil className="size-3.5" />
            Chỉnh sửa
          </Button>
          <Button
            variant="outline"
            size="sm"
            className="text-destructive border-destructive/30 hover:bg-destructive/10 hover:text-destructive"
            onClick={handleDelete}
            disabled={isActionLoading}
          >
            <Trash2 className="size-3.5" />
            Xóa
          </Button>
        </div>
      </div>

      {/* Publish / Save Draft action bar */}
      <div className="flex items-center justify-between gap-3 rounded-xl border border-border bg-card px-5 py-3">
        <div className="flex items-center gap-2">
          <span className="text-sm text-muted-foreground">Trạng thái:</span>
          <StatusBadge status={plan.status} />
        </div>
        <div className="flex items-center gap-2">
          {plan.status === 'DRAFT' ? (
            <>
              <Button
                variant="outline"
                size="sm"
                onClick={handleSaveDraft}
                disabled={isActionLoading}
              >
                <Pencil className="size-3.5" />
                Sửa &amp; Lưu nháp
              </Button>
              <Button
                size="sm"
                onClick={() => handlePublish()}
                disabled={isActionLoading}
              >
                {isActionLoading
                  ? <Loader2 className="size-3.5 animate-spin" />
                  : <Send className="size-3.5" />
                }
                Xuất bản
              </Button>
            </>
          ) : (
            <div className="flex items-center gap-1.5 text-sm text-green-700 font-medium">
              <CheckCircle2 className="size-4" />
              Giáo án đã được xuất bản
            </div>
          )}
        </div>
      </div>

      {/* Metadata card */}
      <div className="rounded-xl border border-border bg-card px-5 py-4 space-y-3">
        <h2 className="text-sm font-semibold text-foreground flex items-center gap-1.5">
          <BookOpen className="size-4 text-primary" />
          Thông tin chung
        </h2>
        <div className="grid grid-cols-1 gap-2.5 sm:grid-cols-2">
          <InfoRow label="Chủ đề" value={plan.topic} />
          <InfoRow label="Môn học" value={plan.subject} />
          <InfoRow label="Khối lớp" value={plan.gradeLevel ? `Lớp ${plan.gradeLevel}` : null} />
          <InfoRow label="Thời lượng" value={plan.durationMinutes ? `${plan.durationMinutes} phút` : null} />
          <InfoRow label="Framework" value={frameworkLabel} />
          <InfoRow
            label="Ngày tạo"
            value={plan.createdAt ? new Date(plan.createdAt).toLocaleString('vi-VN') : null}
          />
          <InfoRow
            label="Cập nhật"
            value={plan.updatedAt ? new Date(plan.updatedAt).toLocaleString('vi-VN') : null}
          />
        </div>
      </div>

      {/* Content sections */}
      <ContentSection icon={ClipboardList} title="Mục tiêu học tập" content={plan.objectives} />
      <ContentSection icon={FileText} title="Hoạt động dạy học" content={plan.activities} />
      <ContentSection icon={Send} title="Kiểm tra & Đánh giá" content={plan.assessment} />
      <ContentSection icon={Package} title="Tài liệu & Thiết bị" content={plan.materials} />

      {/* Bottom action bar */}
      <div className="flex items-center justify-end gap-2 rounded-xl border border-border bg-card px-5 py-3">
        <Button variant="outline" onClick={() => navigate('/lesson-plans')}>
          Quay lại danh sách
        </Button>
        {plan.status === 'DRAFT' && (
          <>
            <Button variant="outline" onClick={handleSaveDraft} disabled={isActionLoading}>
              <Pencil className="size-3.5" />
              Chỉnh sửa
            </Button>
            <Button onClick={() => handlePublish()} disabled={isActionLoading}>
              {isActionLoading
                ? <Loader2 className="size-3.5 animate-spin" />
                : <Send className="size-3.5" />
              }
              Xuất bản
            </Button>
          </>
        )}
      </div>
    </div>
  )
}
