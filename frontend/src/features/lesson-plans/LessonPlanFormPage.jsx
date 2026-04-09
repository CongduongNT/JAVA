import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, BookOpen, ClipboardList, FileText, Loader2, Package, Save, Send } from 'lucide-react'
import { toast } from 'sonner'

import { clearSubmitStatus, createLessonPlan, updateLessonPlan } from './lessonPlanSlice'
import lessonPlanApi from '@/services/lessonPlanApi'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Skeleton } from '@/components/ui/skeleton'

// ─── Constants ────────────────────────────────────────────────────────────────

const GRADES = ['1','2','3','4','5','6','7','8','9','10','11','12']

// Common Vietnamese curriculum frameworks
const FRAMEWORK_OPTIONS = [
  { value: '', label: '— Không chọn —' },
  { value: '1', label: 'Chương trình GDPT 2018 (Bộ GD&ĐT)' },
  { value: '2', label: 'Chương trình cũ 2006 (Bộ GD&ĐT)' },
  { value: '3', label: 'Chương trình tích hợp' },
]

const TABS = [
  { value: 'basic',       label: 'Thông tin chung',  icon: BookOpen },
  { value: 'objectives',  label: 'Mục tiêu',          icon: ClipboardList },
  { value: 'activities',  label: 'Hoạt động',         icon: FileText },
  { value: 'assessment',  label: 'Đánh giá',          icon: Send },
  { value: 'materials',   label: 'Tài liệu',          icon: Package },
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

// ─── Field Row ─────────────────────────────────────────────────────────────────

function FieldRow({ label, required, hint, children }) {
  return (
    <div className="grid gap-1.5">
      <div className="flex items-baseline gap-1">
        <Label>{label}</Label>
        {required && <span className="text-destructive text-xs">*</span>}
      </div>
      {children}
      {hint && <p className="text-xs text-muted-foreground">{hint}</p>}
    </div>
  )
}

// ─── Section: Basic Info ───────────────────────────────────────────────────────

function BasicInfoSection({ form, onChange, errors }) {
  return (
    <div className="space-y-5">
      <div className="rounded-lg border border-border bg-muted/30 p-4">
        <p className="text-sm text-muted-foreground">
          Điền thông tin cơ bản cho giáo án. Tiêu đề là bắt buộc. Các trường còn lại có thể điền sau.
        </p>
      </div>

      <FieldRow label="Framework / Chương trình" hint="Chọn chương trình giảng dạy phù hợp (không bắt buộc)">
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

      <FieldRow label="Tiêu đề giáo án" required hint="Tên bài học hoặc tiêu đề giáo án (tối đa 255 ký tự)">
        <Input
          placeholder="VD: Bài 1 – Phương trình bậc nhất một ẩn"
          value={form.title}
          onChange={(e) => onChange('title', e.target.value)}
          aria-invalid={!!errors.title}
        />
        {errors.title && <p className="text-xs text-destructive">{errors.title}</p>}
      </FieldRow>

      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
        <FieldRow label="Môn học" hint="VD: Toán, Văn, Vật lý...">
          <Input
            placeholder="VD: Toán"
            value={form.subject}
            onChange={(e) => onChange('subject', e.target.value)}
          />
        </FieldRow>
        <FieldRow label="Khối lớp">
          <select
            className={selectCls}
            value={form.gradeLevel}
            onChange={(e) => onChange('gradeLevel', e.target.value)}
          >
            <option value="">— Chọn lớp —</option>
            {GRADES.map((g) => (
              <option key={g} value={g}>Lớp {g}</option>
            ))}
          </select>
        </FieldRow>
      </div>

      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
        <FieldRow label="Chủ đề / Nội dung" hint="Chủ đề hoặc nội dung chính của bài học">
          <Input
            placeholder="VD: Hàm số và đồ thị"
            value={form.topic}
            onChange={(e) => onChange('topic', e.target.value)}
          />
        </FieldRow>
        <FieldRow label="Thời lượng (phút)" hint="Số phút dự kiến cho bài học">
          <Input
            type="number"
            min="1"
            placeholder="VD: 45"
            value={form.durationMinutes}
            onChange={(e) => onChange('durationMinutes', e.target.value)}
            aria-invalid={!!errors.durationMinutes}
          />
          {errors.durationMinutes && (
            <p className="text-xs text-destructive">{errors.durationMinutes}</p>
          )}
        </FieldRow>
      </div>
    </div>
  )
}

// ─── Section: Textarea ─────────────────────────────────────────────────────────

function TextareaSection({ field, form, onChange, label, placeholder, hint }) {
  return (
    <div className="space-y-3">
      <div className="rounded-lg border border-border bg-muted/30 p-4">
        <p className="text-sm text-muted-foreground">{hint}</p>
      </div>
      <FieldRow label={label}>
        <textarea
          className={textareaCls}
          placeholder={placeholder}
          value={form[field]}
          onChange={(e) => onChange(field, e.target.value)}
        />
      </FieldRow>
    </div>
  )
}

// ─── Form Page ────────────────────────────────────────────────────────────────

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

  // Load existing plan when editing
  useEffect(() => {
    if (isEdit) {
      setLoadingDetail(true)
      lessonPlanApi.getLessonPlan(id)
        .then((res) => {
          const p = res.data
          setForm({
            frameworkId: p.frameworkId ? String(p.frameworkId) : '',
            title: p.title || '',
            subject: p.subject || '',
            gradeLevel: p.gradeLevel || '',
            topic: p.topic || '',
            durationMinutes: p.durationMinutes ? String(p.durationMinutes) : '',
            objectives: p.objectives || '',
            activities: p.activities || '',
            assessment: p.assessment || '',
            materials: p.materials || '',
          })
        })
        .catch(() => toast.error('Không thể tải thông tin giáo án.'))
        .finally(() => setLoadingDetail(false))
    }
  }, [id, isEdit])

  // Clear submit status on unmount
  useEffect(() => {
    return () => { dispatch(clearSubmitStatus()) }
  }, [dispatch])

  const handleChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }))
    if (errors[field]) setErrors((prev) => ({ ...prev, [field]: undefined }))
  }

  const validate = () => {
    const newErrors = {}
    if (!form.title.trim()) newErrors.title = 'Tiêu đề là bắt buộc'
    if (form.durationMinutes && (isNaN(Number(form.durationMinutes)) || Number(form.durationMinutes) <= 0)) {
      newErrors.durationMinutes = 'Thời lượng phải là số dương'
    }
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const buildPayload = () => ({
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
  })

  const handleSaveDraft = async () => {
    if (!validate()) {
      setActiveTab('basic')
      return
    }
    const payload = buildPayload()
    const action = isEdit
      ? dispatch(updateLessonPlan({ id: Number(id), data: payload }))
      : dispatch(createLessonPlan(payload))

    action.then((result) => {
      if (!result.error) {
        toast.success(isEdit ? 'Đã lưu giáo án!' : 'Đã tạo giáo án!')
        navigate('/lesson-plans')
      } else {
        toast.error(result.payload || 'Không thể lưu giáo án.')
      }
    })
  }

  const handlePublish = async () => {
    if (!validate()) {
      setActiveTab('basic')
      return
    }

    // Save first, then publish via the detail page
    const payload = buildPayload()
    const action = isEdit
      ? dispatch(updateLessonPlan({ id: Number(id), data: payload }))
      : dispatch(createLessonPlan(payload))

    action.then((result) => {
      if (!result.error) {
        const savedId = result.payload?.id
        if (savedId) {
          navigate(`/lesson-plans/${savedId}?action=publish`)
        } else {
          toast.success(isEdit ? 'Đã lưu giáo án!' : 'Đã tạo giáo án!')
          navigate('/lesson-plans')
        }
      } else {
        toast.error(result.payload || 'Không thể lưu giáo án.')
      }
    })
  }

  const isSaving = submitStatus === 'loading'

  if (loadingDetail) {
    return (
      <div className="space-y-5 max-w-3xl">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-48 w-full" />
      </div>
    )
  }

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex items-center gap-3">
        <Button variant="ghost" size="icon" onClick={() => navigate('/lesson-plans')} aria-label="Quay lại">
          <ArrowLeft className="size-4" />
        </Button>
        <div>
          <h1 className="page-header">{isEdit ? 'Chỉnh sửa giáo án' : 'Tạo giáo án mới'}</h1>
          <p className="page-subheader">
            {isEdit ? 'Cập nhật nội dung giáo án của bạn.' : 'Điền thông tin để tạo giáo án thủ công.'}
          </p>
        </div>
      </div>

      {/* Error banner */}
      {submitError && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {submitError}
        </div>
      )}

      {/* Tabbed Form */}
      <div className="rounded-xl border border-border bg-card">
        <Tabs value={activeTab} onValueChange={setActiveTab} className="gap-0">
          {/* Tab list */}
          <div className="border-b border-border px-4 pt-4">
            <TabsList variant="line" className="w-full overflow-x-auto h-auto pb-0 flex gap-0">
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

          {/* Tab panels */}
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
                hint="Nêu rõ kiến thức, kỹ năng và thái độ mà học sinh sẽ đạt được sau khi học xong bài. Có thể phân theo mức độ nhận thức (biết, hiểu, vận dụng...)."
              />
            </TabsContent>

            <TabsContent value="activities">
              <TextareaSection
                field="activities"
                form={form}
                onChange={handleChange}
                label="Hoạt động dạy học"
                placeholder="Mô tả các hoạt động trong tiết học (khởi động, hình thành kiến thức, luyện tập, vận dụng)..."
                hint="Mô tả chi tiết các hoạt động dạy và học theo trình tự tiết học. Bao gồm phương pháp, thời gian và cách thức tổ chức từng hoạt động."
              />
            </TabsContent>

            <TabsContent value="assessment">
              <TextareaSection
                field="assessment"
                form={form}
                onChange={handleChange}
                label="Kiểm tra & Đánh giá"
                placeholder="Hình thức kiểm tra, câu hỏi đánh giá, tiêu chí đánh giá..."
                hint="Mô tả cách thức kiểm tra đánh giá học sinh: hình thức (vấn đáp, bài tập, trắc nghiệm...), tiêu chí và thang điểm."
              />
            </TabsContent>

            <TabsContent value="materials">
              <TextareaSection
                field="materials"
                form={form}
                onChange={handleChange}
                label="Tài liệu & Thiết bị"
                placeholder="Sách giáo khoa, tài liệu tham khảo, thiết bị dạy học, đồ dùng..."
                hint="Liệt kê tất cả tài liệu, thiết bị và đồ dùng dạy học cần chuẩn bị cho tiết học."
              />
            </TabsContent>
          </div>
        </Tabs>
      </div>

      {/* Action bar */}
      <div className="flex items-center justify-between gap-3 rounded-xl border border-border bg-card px-5 py-3">
        <Button variant="outline" onClick={() => navigate('/lesson-plans')} disabled={isSaving}>
          Hủy
        </Button>
        <div className="flex items-center gap-2">
          <Button variant="outline" onClick={handleSaveDraft} disabled={isSaving}>
            {isSaving ? <Loader2 className="size-4 animate-spin" /> : <Save className="size-4" />}
            Lưu nháp
          </Button>
          <Button onClick={handlePublish} disabled={isSaving}>
            {isSaving ? <Loader2 className="size-4 animate-spin" /> : <Send className="size-4" />}
            Lưu & Xuất bản
          </Button>
        </div>
      </div>
    </div>
  )
}
