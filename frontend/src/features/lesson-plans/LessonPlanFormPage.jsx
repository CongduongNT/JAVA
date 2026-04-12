import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate, useParams } from 'react-router-dom'
import {
  AlertTriangle,
  ArrowLeft,
  BookOpen,
  CheckCircle2,
  ClipboardList,
  FileText,
  Loader2,
  Package,
  Save,
  Send,
} from 'lucide-react'
import { toast } from 'sonner'

import {
  clearSubmitStatus,
  createLessonPlan,
  publishLessonPlan,
  updateLessonPlan,
} from './lessonPlanSlice'
import lessonPlanApi from '@/services/lessonPlanApi'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import {
  FRAMEWORK_OPTIONS,
  GRADES,
  getFirstInvalidTab,
  getPublishMissingFields,
  getFrameworkLabel,
} from './lessonPlanMeta'

const TABS = [
  { value: 'basic', label: 'Thông tin chung', icon: BookOpen },
  { value: 'objectives', label: 'Mục tiêu', icon: ClipboardList },
  { value: 'activities', label: 'Hoạt động', icon: FileText },
  { value: 'assessment', label: 'Đánh giá', icon: Send },
  { value: 'materials', label: 'Tài liệu', icon: Package },
]

const INITIAL_FORM = {
  frameworkId: '',
  title: '',
  subject: '',
  gradeLevel: '',
  topic: '',
  durationMinutes: '',
  objectives: '',
  activities: '',
  assessment: '',
  materials: '',
}

const selectCls =
  'h-8 w-full rounded-lg border border-input bg-background px-2.5 text-sm outline-none transition-colors focus:border-ring'

const textareaCls =
  'w-full min-h-[200px] rounded-lg border border-input bg-transparent px-2.5 py-2 text-sm transition-colors outline-none placeholder:text-muted-foreground focus:border-ring focus:ring-3 focus:ring-ring/50 disabled:opacity-50 resize-y'

function buildDraftValidationErrors(form) {
  const validationErrors = {}

  if (!form.title.trim()) {
    validationErrors.title = 'Tiêu đề là bắt buộc để lưu bản nháp'
  }

  if (form.durationMinutes && (isNaN(Number(form.durationMinutes)) || Number(form.durationMinutes) <= 0)) {
    validationErrors.durationMinutes = 'Thời lượng phải là số dương'
  }

  return validationErrors
}

function buildPublishValidationErrors(form) {
  const validationErrors = buildDraftValidationErrors(form)

  getPublishMissingFields(form).forEach(({ field, label }) => {
    if (field === 'durationMinutes') {
      if (!form.durationMinutes) {
        validationErrors.durationMinutes = 'Thời lượng là bắt buộc khi xuất bản'
      }
      return
    }

    if (!validationErrors[field]) {
      validationErrors[field] = `${label} là bắt buộc khi xuất bản`
    }
  })

  return validationErrors
}

function FieldRow({
  label,
  required,
  publishRequired,
  hint,
  error,
  children,
}) {
  return (
    <div className="grid gap-1.5">
      <div className="flex flex-wrap items-center gap-2">
        <Label>{label}</Label>
        {required && <span className="text-destructive text-xs">*</span>}
        {publishRequired && <Badge variant="outline" className="text-[10px]">Bắt buộc khi xuất bản</Badge>}
      </div>
      {children}
      {error && <p className="text-xs text-destructive">{error}</p>}
      {hint && <p className="text-xs text-muted-foreground">{hint}</p>}
    </div>
  )
}

function PublishReadinessCard({ missingFields, frameworkId }) {
  const frameworkLabel = getFrameworkLabel(frameworkId)
  const isReady = missingFields.length === 0

  return (
    <div
      className={
        isReady
          ? 'rounded-xl border border-green-200 bg-green-50/80 p-4'
          : 'rounded-xl border border-amber-200 bg-amber-50/80 p-4'
      }
    >
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            {isReady ? (
              <CheckCircle2 className="size-4 text-green-700" />
            ) : (
              <AlertTriangle className="size-4 text-amber-700" />
            )}
            <h2 className={isReady ? 'text-sm font-semibold text-green-900' : 'text-sm font-semibold text-amber-900'}>
              {isReady ? 'Sẵn sàng để xuất bản' : 'Cần hoàn thiện trước khi xuất bản'}
            </h2>
          </div>
          <p className={isReady ? 'text-sm text-green-800' : 'text-sm text-amber-800'}>
            Lưu bản nháp chỉ yêu cầu tiêu đề. Xuất bản yêu cầu hoàn thiện đầy đủ thông tin bài dạy và các section bắt buộc.
          </p>
        </div>
        {frameworkLabel && (
          <Badge variant="secondary" className="max-w-full whitespace-normal text-xs">
            {frameworkLabel}
          </Badge>
        )}
      </div>

      {missingFields.length > 0 && (
        <div className="mt-3 flex flex-wrap gap-2">
          {missingFields.map(({ field, label }) => (
            <Badge key={field} variant="outline" className="border-amber-300 bg-white/70 text-amber-900">
              {label}
            </Badge>
          ))}
        </div>
      )}
    </div>
  )
}

function BasicInfoSection({ form, onChange, errors }) {
  return (
    <div className="space-y-5">
      <div className="rounded-lg border border-border bg-muted/30 p-4">
        <p className="text-sm text-muted-foreground">
          Chọn framework nếu bạn muốn gắn giáo án với một chương trình giảng dạy cụ thể.
          Bản nháp có thể lưu dần, nhưng để xuất bản bạn cần hoàn thiện đầy đủ các mục bắt buộc.
        </p>
      </div>

      <FieldRow
        label="Framework / Chương trình"
        hint="Hiện đang dùng danh mục tạm trên FE cho đến khi backend mở API framework."
      >
        <select
          className={selectCls}
          value={form.frameworkId}
          onChange={(e) => onChange('frameworkId', e.target.value)}
        >
          {FRAMEWORK_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>
      </FieldRow>

      <FieldRow
        label="Tiêu đề giáo án"
        required
        publishRequired
        hint="Tên bài học hoặc tiêu đề giáo án (tối đa 255 ký tự)"
        error={errors.title}
      >
        <Input
          placeholder="VD: Bài 1 - Phương trình bậc nhất một ẩn"
          value={form.title}
          onChange={(e) => onChange('title', e.target.value)}
          aria-invalid={!!errors.title}
        />
      </FieldRow>

      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
        <FieldRow
          label="Môn học"
          publishRequired
          hint="VD: Toán, Văn, Vật lý..."
          error={errors.subject}
        >
          <Input
            placeholder="VD: Toán"
            value={form.subject}
            onChange={(e) => onChange('subject', e.target.value)}
            aria-invalid={!!errors.subject}
          />
        </FieldRow>

        <FieldRow label="Khối lớp" publishRequired error={errors.gradeLevel}>
          <select
            className={selectCls}
            value={form.gradeLevel}
            onChange={(e) => onChange('gradeLevel', e.target.value)}
            aria-invalid={!!errors.gradeLevel}
          >
            <option value="">— Chọn lớp —</option>
            {GRADES.map((grade) => (
              <option key={grade} value={grade}>Lớp {grade}</option>
            ))}
          </select>
        </FieldRow>
      </div>

      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
        <FieldRow
          label="Chủ đề / Nội dung"
          publishRequired
          hint="Chủ đề hoặc nội dung chính của bài học"
          error={errors.topic}
        >
          <Input
            placeholder="VD: Hàm số và đồ thị"
            value={form.topic}
            onChange={(e) => onChange('topic', e.target.value)}
            aria-invalid={!!errors.topic}
          />
        </FieldRow>

        <FieldRow
          label="Thời lượng (phút)"
          publishRequired
          hint="Số phút dự kiến cho bài học"
          error={errors.durationMinutes}
        >
          <Input
            type="number"
            min="1"
            placeholder="VD: 45"
            value={form.durationMinutes}
            onChange={(e) => onChange('durationMinutes', e.target.value)}
            aria-invalid={!!errors.durationMinutes}
          />
        </FieldRow>
      </div>
    </div>
  )
}

function TextareaSection({
  field,
  form,
  onChange,
  label,
  placeholder,
  hint,
  error,
}) {
  return (
    <div className="space-y-3">
      <div className="rounded-lg border border-border bg-muted/30 p-4">
        <p className="text-sm text-muted-foreground">{hint}</p>
      </div>
      <FieldRow label={label} publishRequired error={error}>
        <textarea
          className={textareaCls}
          placeholder={placeholder}
          value={form[field]}
          onChange={(e) => onChange(field, e.target.value)}
          aria-invalid={!!error}
        />
      </FieldRow>
    </div>
  )
}

export default function LessonPlanFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { submitStatus, submitError } = useSelector((state) => state.lessonPlans)

  const [activeTab, setActiveTab] = useState('basic')
  const [form, setForm] = useState(INITIAL_FORM)
  const [errors, setErrors] = useState({})
  const [loadingDetail, setLoadingDetail] = useState(false)

  const missingPublishFields = getPublishMissingFields(form)

  useEffect(() => {
    if (!isEdit) return undefined

    setLoadingDetail(true)
    lessonPlanApi.getLessonPlan(id)
      .then((res) => {
        const plan = res.data
        setForm({
          frameworkId: plan.frameworkId ? String(plan.frameworkId) : '',
          title: plan.title || '',
          subject: plan.subject || '',
          gradeLevel: plan.gradeLevel || '',
          topic: plan.topic || '',
          durationMinutes: plan.durationMinutes ? String(plan.durationMinutes) : '',
          objectives: plan.objectives || '',
          activities: plan.activities || '',
          assessment: plan.assessment || '',
          materials: plan.materials || '',
        })
      })
      .catch(() => toast.error('Không thể tải thông tin giáo án.'))
      .finally(() => setLoadingDetail(false))

    return undefined
  }, [id, isEdit])

  useEffect(() => (
    () => {
      dispatch(clearSubmitStatus())
    }
  ), [dispatch])

  const handleChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }))

    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }))
    }
  }

  const saveLessonPlan = async () => {
    const payload = {
      frameworkId: form.frameworkId ? Number(form.frameworkId) : null,
      title: form.title.trim(),
      subject: form.subject.trim() || null,
      gradeLevel: form.gradeLevel || null,
      topic: form.topic.trim() || null,
      durationMinutes: form.durationMinutes ? Number(form.durationMinutes) : null,
      objectives: form.objectives.trim() || null,
      activities: form.activities.trim() || null,
      assessment: form.assessment.trim() || null,
      materials: form.materials.trim() || null,
    }

    const result = isEdit
      ? await dispatch(updateLessonPlan({ id: Number(id), data: payload }))
      : await dispatch(createLessonPlan(payload))

    if (result.error) {
      toast.error(result.payload || 'Không thể lưu giáo án.')
      return null
    }

    return result.payload
  }

  const handleSaveDraft = async () => {
    const validationErrors = buildDraftValidationErrors(form)
    setErrors(validationErrors)

    if (Object.keys(validationErrors).length > 0) {
      setActiveTab('basic')
      return
    }

    const savedPlan = await saveLessonPlan()
    if (!savedPlan?.id) return

    toast.success(isEdit ? 'Đã cập nhật bản nháp.' : 'Đã tạo bản nháp.')
    navigate(`/lesson-plans/${savedPlan.id}`)
  }

  const handlePublish = async () => {
    const validationErrors = buildPublishValidationErrors(form)
    setErrors(validationErrors)

    if (Object.keys(validationErrors).length > 0) {
      setActiveTab(getFirstInvalidTab(getPublishMissingFields(form)))
      toast.error('Cần hoàn thiện các mục bắt buộc trước khi xuất bản.')
      return
    }

    const savedPlan = await saveLessonPlan()
    if (!savedPlan?.id) return

    const publishResult = await dispatch(publishLessonPlan(savedPlan.id))
    if (publishResult.error) {
      toast.error(publishResult.payload || 'Không thể xuất bản giáo án.')

      if (!isEdit) {
        navigate(`/lesson-plans/${savedPlan.id}/edit`, { replace: true })
      }
      return
    }

    toast.success('Đã xuất bản giáo án thành công!')
    navigate(`/lesson-plans/${savedPlan.id}`)
  }

  const isSaving = submitStatus === 'loading'

  if (loadingDetail) {
    return (
      <div className="max-w-3xl space-y-5">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-48 w-full" />
      </div>
    )
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-3">
        <Button variant="ghost" size="icon" onClick={() => navigate('/lesson-plans')} aria-label="Quay lại">
          <ArrowLeft className="size-4" />
        </Button>
        <div>
          <h1 className="page-header">{isEdit ? 'Chỉnh sửa giáo án' : 'Tạo giáo án mới'}</h1>
          <p className="page-subheader">
            {isEdit
              ? 'Cập nhật nội dung giáo án và kiểm tra mức sẵn sàng trước khi xuất bản.'
              : 'Tạo giáo án thủ công theo từng section, lưu nháp dần và chỉ xuất bản khi đã hoàn thiện.'}
          </p>
        </div>
      </div>

      {submitError && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {submitError}
        </div>
      )}

      <PublishReadinessCard
        missingFields={missingPublishFields}
        frameworkId={form.frameworkId}
      />

      <div className="rounded-xl border border-border bg-card">
        <Tabs value={activeTab} onValueChange={setActiveTab} className="gap-0">
          <div className="border-b border-border px-4 pt-4">
            <TabsList variant="line" className="flex h-auto w-full gap-0 overflow-x-auto pb-0">
              {TABS.map((tab) => (
                <TabsTrigger
                  key={tab.value}
                  value={tab.value}
                  className="flex items-center gap-1.5 px-3 py-2 text-xs sm:text-sm"
                >
                  <tab.icon className="size-3.5" />
                  <span className="hidden sm:inline">{tab.label}</span>
                  <span className="sm:hidden">{tab.label.split(' ')[0]}</span>
                </TabsTrigger>
              ))}
            </TabsList>
          </div>

          <div className="p-6">
            <TabsContent value="basic">
              <BasicInfoSection form={form} onChange={handleChange} errors={errors} />
            </TabsContent>

            <TabsContent value="objectives">
              <TextareaSection
                field="objectives"
                form={form}
                onChange={handleChange}
                label="Mục tiêu học tập"
                placeholder="Mô tả các mục tiêu cụ thể học sinh đạt được sau bài học..."
                hint="Nêu rõ kiến thức, kỹ năng và thái độ mà học sinh sẽ đạt được sau khi học xong bài."
                error={errors.objectives}
              />
            </TabsContent>

            <TabsContent value="activities">
              <TextareaSection
                field="activities"
                form={form}
                onChange={handleChange}
                label="Hoạt động dạy học"
                placeholder="Mô tả các hoạt động trong tiết học (khởi động, hình thành kiến thức, luyện tập, vận dụng)..."
                hint="Mô tả chi tiết trình tự dạy học, thời gian và cách tổ chức từng hoạt động."
                error={errors.activities}
              />
            </TabsContent>

            <TabsContent value="assessment">
              <TextareaSection
                field="assessment"
                form={form}
                onChange={handleChange}
                label="Kiểm tra & Đánh giá"
                placeholder="Hình thức kiểm tra, câu hỏi đánh giá, tiêu chí đánh giá..."
                hint="Mô tả cách đánh giá học sinh, tiêu chí và cách ghi nhận kết quả."
                error={errors.assessment}
              />
            </TabsContent>

            <TabsContent value="materials">
              <TextareaSection
                field="materials"
                form={form}
                onChange={handleChange}
                label="Tài liệu & Thiết bị"
                placeholder="Sách giáo khoa, tài liệu tham khảo, thiết bị dạy học, đồ dùng..."
                hint="Liệt kê đầy đủ tài liệu, thiết bị và học liệu cần chuẩn bị cho tiết dạy."
                error={errors.materials}
              />
            </TabsContent>
          </div>
        </Tabs>
      </div>

      <div className="flex flex-col gap-3 rounded-xl border border-border bg-card px-5 py-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="space-y-1">
          <p className="text-sm font-medium text-foreground">Quy tắc lưu</p>
          <p className="text-sm text-muted-foreground">
            Lưu nháp chỉ cần tiêu đề. Xuất bản sẽ kiểm tra toàn bộ các mục bắt buộc ngay trên form.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <Button variant="outline" onClick={() => navigate('/lesson-plans')} disabled={isSaving}>
            Hủy
          </Button>
          <Button variant="outline" onClick={handleSaveDraft} disabled={isSaving}>
            {isSaving ? <Loader2 className="size-4 animate-spin" /> : <Save className="size-4" />}
            Lưu nháp
          </Button>
          <Button onClick={handlePublish} disabled={isSaving}>
            {isSaving ? <Loader2 className="size-4 animate-spin" /> : <Send className="size-4" />}
            Lưu và xuất bản
          </Button>
        </div>
      </div>
    </div>
  )
}
