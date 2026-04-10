import React, { useState } from 'react'
import {
  ArrowLeft,
  BookOpen,
  Sparkles,
  Loader2,
  Save,
  RotateCw,
  ChevronDown,
  ChevronUp,
  AlertCircle,
  CheckCircle2,
  Clock,
  Target,
  Package,
  Layout,
  ClipboardCheck,
  Home,
  StickyNote,
} from 'lucide-react'
import { toast } from 'sonner'
import lessonPlanApi from '../../services/lessonPlanApi'

/**
 * LessonPlanGenerator – Trang sinh giáo án bằng AI.
 *
 * Luồng state:
 * 1. form → nhập input → bấm "Tạo với AI"
 * 2. generating → loading + disable inputs
 * 3. generated → editor editable + hiện "Tạo lại" + "Lưu"
 * 4. saving → loading save + disable buttons
 */
const LessonPlanGenerator = ({ onBack }) => {
  // --- Form State ---
  const [form, setForm] = useState({
    subject: '',
    gradeLevel: '',
    topic: '',
    objectives: '',
    durationMinutes: 45,
    framework: 'E5',
  })

  // --- Generation State ---
  const [isGenerating, setIsGenerating] = useState(false)
  const [generateError, setGenerateError] = useState('')

  // --- Editor State (editable lesson plan) ---
  const [hasGenerated, setHasGenerated] = useState(false)
  const [lessonPlan, setLessonPlan] = useState(null)

  // --- Save State ---
  const [isSaving, setIsSaving] = useState(false)
  const [saveError, setSaveError] = useState('')

  // --- Expanded sections in preview ---
  const [expandedSections, setExpandedSections] = useState({
    objectives: true,
    materials: true,
    lessonFlow: true,
    assessment: true,
    homework: false,
    notes: false,
  })

  // --- Update form field ---
  const handleFormChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  // --- Generate lesson plan ---
  const handleGenerate = async () => {
    if (!form.subject.trim()) return toast.error('Vui lòng nhập môn học')
    if (!form.gradeLevel.trim()) return toast.error('Vui lòng nhập khối lớp')
    if (!form.topic.trim()) return toast.error('Vui lòng nhập chủ đề')

    setIsGenerating(true)
    setGenerateError('')
    setLessonPlan(null)
    setHasGenerated(false)

    try {
      const res = await lessonPlanApi.generatePreview({
        subject: form.subject,
        gradeLevel: form.gradeLevel,
        topic: form.topic,
        objectives: form.objectives || '',
        durationMinutes: form.durationMinutes,
        framework: form.framework,
      })
      setLessonPlan(res.data)
      setHasGenerated(true)
      toast.success('AI đã sinh giáo án thành công!')
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Có lỗi xảy ra'
      setGenerateError(msg)
      toast.error(msg)
    } finally {
      setIsGenerating(false)
    }
  }

  // --- Regenerate ---
  const handleRegenerate = async () => {
    if (!form.subject.trim() || !form.gradeLevel.trim() || !form.topic.trim()) {
      return toast.error('Vui lòng nhập đủ thông tin trước khi tạo lại')
    }
    await handleGenerate()
  }

  // --- Save lesson plan ---
  const handleSave = async () => {
    if (!lessonPlan) return

    setIsSaving(true)
    setSaveError('')

    try {
      await lessonPlanApi.generateAndSave({
        subject: form.subject,
        gradeLevel: form.gradeLevel,
        topic: form.topic,
        objectives: form.objectives || '',
        durationMinutes: form.durationMinutes,
        framework: form.framework,
      })
      toast.success('Giáo án đã được lưu thành công!')
      onBack?.()
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Có lỗi khi lưu giáo án'
      setSaveError(msg)
      toast.error(msg)
    } finally {
      setIsSaving(false)
    }
  }

  // --- Update lesson plan field (editable) ---
  const handleLessonPlanChange = (field, value) => {
    setLessonPlan((prev) => ({ ...prev, [field]: value }))
  }

  // --- Update lesson phase (editable flow step) ---
  const handlePhaseChange = (phaseIndex, field, value) => {
    setLessonPlan((prev) => {
      const newFlow = [...(prev.lessonFlow || [])]
      newFlow[phaseIndex] = { ...newFlow[phaseIndex], [field]: value }
      return { ...prev, lessonFlow: newFlow }
    })
  }

  // --- Toggle section expansion ---
  const toggleSection = (section) => {
    setExpandedSections((prev) => ({ ...prev, [section]: !prev[section] }))
  }

  // --- Render section header ---
  const renderSectionHeader = (icon, label, sectionKey, count) => (
    <button
      onClick={() => toggleSection(sectionKey)}
      className="w-full flex items-center justify-between px-4 py-3 bg-slate-100 hover:bg-slate-200 transition-colors rounded-xl text-left"
    >
      <div className="flex items-center gap-2 font-bold text-slate-700">
        {icon}
        <span>{label}</span>
        {count != null && (
          <span className="text-xs font-medium text-slate-400">({count})</span>
        )}
      </div>
      {expandedSections[sectionKey] ? (
        <ChevronUp size={16} className="text-slate-400" />
      ) : (
        <ChevronDown size={16} className="text-slate-400" />
      )}
    </button>
  )

  // --- Render editor for a single-line field ---
  const renderSimpleField = (label, value, fieldKey, icon) => (
    <div className="space-y-1">
      <label className="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1">
        {icon}
        {label}
      </label>
      <textarea
        rows={2}
        value={value || ''}
        onChange={(e) => handleLessonPlanChange(fieldKey, e.target.value)}
        className="w-full px-4 py-3 bg-white border border-slate-200 rounded-xl text-sm font-medium text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none transition-all"
      />
    </div>
  )

  // --- Render objectives list ---
  const renderObjectives = () => {
    const list = lessonPlan?.objectives || []
    return (
      <div className="space-y-2">
        <label className="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1">
          <Target size={12} />
          Mục tiêu bài học
        </label>
        {list.map((obj, idx) => (
          <div key={idx} className="flex gap-2">
            <span className="text-xs font-bold text-indigo-500 mt-2">•</span>
            <input
              value={obj}
              onChange={(e) => {
                const next = [...list]
                next[idx] = e.target.value
                handleLessonPlanChange('objectives', next)
              }}
              className="flex-1 px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
        ))}
      </div>
    )
  }

  // --- Render lesson flow phases ---
  const renderLessonFlow = () => {
    const phases = lessonPlan?.lessonFlow || []
    return (
      <div className="space-y-3">
        <label className="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1">
          <Layout size={12} />
          Các giai đoạn (Buổi học)
        </label>
        {phases.length === 0 ? (
          <p className="text-sm text-slate-400 italic">Chưa có dữ liệu các giai đoạn.</p>
        ) : (
          phases.map((phase, idx) => (
            <div key={idx} className="bg-white border border-slate-200 rounded-xl p-4 space-y-3">
              <div className="flex items-center justify-between">
                <input
                  value={phase.phase || ''}
                  onChange={(e) => handlePhaseChange(idx, 'phase', e.target.value)}
                  className="font-bold text-slate-800 bg-transparent outline-none focus:ring-2 focus:ring-indigo-500 rounded px-2 py-1"
                  placeholder="Tên giai đoạn"
                />
                <span className="text-xs font-bold text-slate-400 bg-slate-100 px-2 py-1 rounded-lg flex items-center gap-1">
                  <Clock size={10} />
                  <input
                    type="number"
                    value={phase.timeMinutes || 0}
                    onChange={(e) => handlePhaseChange(idx, 'timeMinutes', parseInt(e.target.value) || 0)}
                    className="w-12 bg-transparent outline-none text-center"
                    min="0"
                  />
                  phút
                </span>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                <div className="space-y-1">
                  <label className="text-xs font-semibold text-slate-500">Hoạt động</label>
                  <textarea
                    rows={2}
                    value={phase.activities || ''}
                    onChange={(e) => handlePhaseChange(idx, 'activities', e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
                    placeholder="Mô tả hoạt động..."
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-xs font-semibold text-slate-500">Hành động GV</label>
                  <textarea
                    rows={2}
                    value={phase.teacherActions || ''}
                    onChange={(e) => handlePhaseChange(idx, 'teacherActions', e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
                    placeholder="Hành động của giáo viên..."
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-xs font-semibold text-slate-500">Hành động HS</label>
                  <textarea
                    rows={2}
                    value={phase.studentActions || ''}
                    onChange={(e) => handlePhaseChange(idx, 'studentActions', e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
                    placeholder="Hành động của học sinh..."
                  />
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    )
  }

  // --- Render assessment section ---
  const renderAssessment = () => {
    const assessment = lessonPlan?.assessment || {}
    return (
      <div className="space-y-3">
        <label className="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1">
          <ClipboardCheck size={12} />
          Đánh giá
        </label>
        <div className="bg-white border border-slate-200 rounded-xl p-4 space-y-3">
          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-500">Phương pháp đánh giá</label>
            <textarea
              rows={2}
              value={assessment.methods?.join('\n') || ''}
              onChange={(e) =>
                handleLessonPlanChange('assessment', {
                  ...assessment,
                  methods: e.target.value.split('\n').filter((s) => s.trim()),
                })
              }
              className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
              placeholder="Mỗi phương pháp 1 dòng..."
            />
          </div>
          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-500">Tiêu chí đánh giá</label>
            <textarea
              rows={2}
              value={assessment.criteria || ''}
              onChange={(e) =>
                handleLessonPlanChange('assessment', {
                  ...assessment,
                  criteria: e.target.value,
                })
              }
              className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
              placeholder="Tiêu chí đánh giá..."
            />
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      {/* Navbar */}
      <div className="h-16 flex items-center px-6 border-b bg-white border-slate-200 justify-between">
        <button
          onClick={onBack}
          className="flex items-center gap-2 text-slate-600 hover:text-indigo-600 font-bold transition-colors"
        >
          <ArrowLeft size={20} />
          Quay lại
        </button>
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-indigo-600 flex items-center justify-center text-white">
            <BookOpen size={16} />
          </div>
          <span className="font-black text-slate-900 uppercase tracking-widest text-sm">
            Trợ lý soạn giáo án AI
          </span>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto p-4 md:p-8">
        <div className="max-w-6xl mx-auto grid grid-cols-1 lg:grid-cols-12 gap-8">
          {/* Left: Input Form */}
          <div className="lg:col-span-4 space-y-6">
            <div className="bg-white rounded-3xl border border-slate-200 shadow-xl p-6 space-y-5">
              <div className="flex items-center gap-2">
                <div className="p-2 bg-indigo-50 rounded-xl text-indigo-600">
                  <Sparkles size={20} />
                </div>
                <h2 className="font-black text-slate-800 uppercase tracking-wider text-sm">
                  Cấu hình sinh giáo án
                </h2>
              </div>

              {/* Subject */}
              <div className="space-y-1">
                <label className="text-xs font-bold uppercase tracking-wider text-slate-500">
                  Môn học
                </label>
                <input
                  type="text"
                  value={form.subject}
                  onChange={(e) => handleFormChange('subject', e.target.value)}
                  disabled={isGenerating}
                  placeholder="VD: Toán, Tiếng Việt, Khoa học..."
                  className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm font-bold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500 disabled:opacity-50 transition-all"
                />
              </div>

              {/* Grade Level */}
              <div className="space-y-1">
                <label className="text-xs font-bold uppercase tracking-wider text-slate-500">
                  Khối lớp
                </label>
                <input
                  type="text"
                  value={form.gradeLevel}
                  onChange={(e) => handleFormChange('gradeLevel', e.target.value)}
                  disabled={isGenerating}
                  placeholder="VD: Lớp 4, Lớp 10, lớp 11..."
                  className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm font-bold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500 disabled:opacity-50 transition-all"
                />
              </div>

              {/* Topic */}
              <div className="space-y-1">
                <label className="text-xs font-bold uppercase tracking-wider text-slate-500">
                  Chủ đề / Bài học
                </label>
                <input
                  type="text"
                  value={form.topic}
                  onChange={(e) => handleFormChange('topic', e.target.value)}
                  disabled={isGenerating}
                  placeholder="VD: Phép cộng phân số, Quang hợp..."
                  className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm font-bold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500 disabled:opacity-50 transition-all"
                />
              </div>

              {/* Objectives */}
              <div className="space-y-1">
                <label className="text-xs font-bold uppercase tracking-wider text-slate-500">
                  Mục tiêu bài học (tùy chọn)
                </label>
                <textarea
                  rows={2}
                  value={form.objectives}
                  onChange={(e) => handleFormChange('objectives', e.target.value)}
                  disabled={isGenerating}
                  placeholder="VD: Hiểu khái niệm phân số, Cộng được hai phân số cùng mẫu..."
                  className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm font-medium text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500 disabled:opacity-50 resize-none transition-all"
                />
              </div>

              {/* Duration & Framework row */}
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <label className="text-xs font-bold uppercase tracking-wider text-slate-500">
                    Thời lượng
                  </label>
                  <select
                    value={form.durationMinutes}
                    onChange={(e) => handleFormChange('durationMinutes', parseInt(e.target.value))}
                    disabled={isGenerating}
                    className="w-full px-3 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm font-bold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500 disabled:opacity-50 transition-all"
                  >
                    <option value={30}>30 phút</option>
                    <option value={45}>45 phút</option>
                    <option value={60}>60 phút</option>
                    <option value={90}>90 phút</option>
                    <option value={120}>120 phút</option>
                  </select>
                </div>

                <div className="space-y-1">
                  <label className="text-xs font-bold uppercase tracking-wider text-slate-500">
                    Framework
                  </label>
                  <select
                    value={form.framework}
                    onChange={(e) => handleFormChange('framework', e.target.value)}
                    disabled={isGenerating}
                    className="w-full px-3 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm font-bold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500 disabled:opacity-50 transition-all"
                  >
                    <option value="E5">E5</option>
                    <option value="E3">E3</option>
                    <option value="E4">E4</option>
                    <option value="BACKWARD_DESIGN">Backward Design</option>
                    <option value="TGAP">TGAP</option>
                  </select>
                </div>
              </div>

              {/* Generate button */}
              <button
                onClick={handleGenerate}
                disabled={isGenerating}
                className="w-full py-4 bg-gradient-to-r from-indigo-600 to-blue-600 text-white rounded-2xl font-black text-sm uppercase tracking-widest flex items-center justify-center gap-3 shadow-lg hover:shadow-xl hover:opacity-90 active:scale-[0.98] transition-all disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isGenerating ? (
                  <>
                    <Loader2 size={20} className="animate-spin" />
                    AI đang tư duy...
                  </>
                ) : (
                  <>
                    <Sparkles size={20} />
                    Tạo với AI
                  </>
                )}
              </button>

              {/* Generate error */}
              {generateError && (
                <div className="flex items-start gap-2 p-3 bg-red-50 border border-red-200 rounded-xl">
                  <AlertCircle size={16} className="text-red-500 mt-0.5 flex-shrink-0" />
                  <p className="text-xs text-red-600 font-medium">{generateError}</p>
                </div>
              )}
            </div>
          </div>

          {/* Right: Editor Preview */}
          <div className="lg:col-span-8">
            <div className="bg-white rounded-3xl border border-slate-200 shadow-2xl flex flex-col overflow-hidden min-h-[600px]">
              {/* Header bar */}
              <div className="px-6 py-4 border-b bg-slate-50/50 flex items-center justify-between">
                <span className="text-xs font-black text-slate-500 uppercase tracking-widest">
                  {hasGenerated ? 'Giáo án – Đang chỉnh sửa' : 'Kết quả giáo án AI'}
                </span>
                {hasGenerated && (
                  <div className="flex items-center gap-2 text-xs text-green-600">
                    <CheckCircle2 size={14} />
                    Đã sinh thành công
                  </div>
                )}
              </div>

              {/* Content area */}
              <div className="flex-1 overflow-y-auto p-6 bg-slate-50/10">
                {!hasGenerated && !isGenerating ? (
                  <div className="h-full flex flex-col items-center justify-center text-slate-400 space-y-4 border-4 border-dashed border-slate-100 rounded-3xl">
                    <BookOpen size={48} className="opacity-15" />
                    <p className="font-bold uppercase tracking-widest text-sm">
                      Chưa có giáo án – Nhấn "Tạo với AI" để bắt đầu
                    </p>
                  </div>
                ) : isGenerating ? (
                  <div className="h-full flex flex-col items-center justify-center text-center space-y-4">
                    <Loader2 size={48} className="text-indigo-400 animate-spin" />
                    <p className="font-bold text-slate-700 animate-pulse text-lg">
                      AI đang tư duy và soạn giáo án cho bạn...
                    </p>
                    <p className="text-sm text-slate-400">
                      Vui lòng chờ trong giây lát
                    </p>
                  </div>
                ) : lessonPlan ? (
                  <div className="space-y-4 animate-in fade-in zoom-in-95 duration-500">
                    {/* Title */}
                    <div className="space-y-1">
                      <label className="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1">
                        <BookOpen size={12} />
                        Tiêu đề giáo án
                      </label>
                      <input
                        value={lessonPlan.title || ''}
                        onChange={(e) => handleLessonPlanChange('title', e.target.value)}
                        className="w-full px-4 py-3 bg-white border border-indigo-200 rounded-xl text-lg font-bold text-slate-800 outline-none focus:ring-2 focus:ring-indigo-500"
                        placeholder="Tiêu đề giáo án..."
                      />
                    </div>

                    {/* Meta row */}
                    <div className="grid grid-cols-3 gap-3">
                      <div className="space-y-1">
                        <label className="text-xs font-bold uppercase tracking-wider text-slate-500">Môn</label>
                        <input
                          value={lessonPlan.subject || ''}
                          onChange={(e) => handleLessonPlanChange('subject', e.target.value)}
                          className="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm font-medium text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500"
                        />
                      </div>
                      <div className="space-y-1">
                        <label className="text-xs font-bold uppercase tracking-wider text-slate-500">Khối</label>
                        <input
                          value={lessonPlan.gradeLevel || ''}
                          onChange={(e) => handleLessonPlanChange('gradeLevel', e.target.value)}
                          className="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm font-medium text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500"
                        />
                      </div>
                      <div className="space-y-1">
                        <label className="text-xs font-bold uppercase tracking-wider text-slate-500">Framework</label>
                        <input
                          value={lessonPlan.framework || ''}
                          onChange={(e) => handleLessonPlanChange('framework', e.target.value)}
                          className="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm font-medium text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500"
                        />
                      </div>
                    </div>

                    {/* Materials */}
                    {lessonPlan.materials && lessonPlan.materials.length > 0 && (
                      <div className="space-y-2">
                        {renderSectionHeader(
                          <Package size={14} />,
                          'Giảng dạy & Học liệu',
                          'materials',
                          null
                        )}
                        {expandedSections.materials && (
                          <div className="space-y-2 pl-2">
                            {(lessonPlan.materials || []).map((m, idx) => (
                              <div key={idx} className="flex gap-2">
                                <span className="text-xs font-bold text-indigo-400 mt-2">•</span>
                                <input
                                  value={m}
                                  onChange={(e) => {
                                    const next = [...(lessonPlan.materials || [])]
                                    next[idx] = e.target.value
                                    handleLessonPlanChange('materials', next)
                                  }}
                                  className="flex-1 px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm text-slate-700 outline-none focus:ring-2 focus:ring-indigo-500"
                                />
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    )}

                    {/* Objectives */}
                    <div className="space-y-2">
                      {renderSectionHeader(
                        <Target size={14} />,
                        'Mục tiêu bài học',
                        'objectives',
                        null
                      )}
                      {expandedSections.objectives && renderObjectives()}
                    </div>

                    {/* Lesson Flow */}
                    <div className="space-y-2">
                      {renderSectionHeader(
                        <Layout size={14} />,
                        'Các giai đoạn buổi học',
                        'lessonFlow',
                        (lessonPlan.lessonFlow || []).length
                      )}
                      {expandedSections.lessonFlow && renderLessonFlow()}
                    </div>

                    {/* Assessment */}
                    <div className="space-y-2">
                      {renderSectionHeader(
                        <ClipboardCheck size={14} />,
                        'Đánh giá',
                        'assessment',
                        null
                      )}
                      {expandedSections.assessment && renderAssessment()}
                    </div>

                    {/* Homework */}
                    <div className="space-y-2">
                      {renderSectionHeader(
                        <Home size={14} />,
                        'Bài tập về nhà',
                        'homework',
                        null
                      )}
                      {expandedSections.homework && renderSimpleField(
                        'Bài tập về nhà',
                        lessonPlan.homework,
                        'homework',
                        <Home size={12} />
                      )}
                    </div>

                    {/* Notes */}
                    <div className="space-y-2">
                      {renderSectionHeader(
                        <StickyNote size={14} />,
                        'Ghi chú',
                        'notes',
                        null
                      )}
                      {expandedSections.notes && renderSimpleField(
                        'Ghi chú',
                        lessonPlan.notes,
                        'notes',
                        <StickyNote size={12} />
                      )}
                    </div>

                    {/* Save error */}
                    {saveError && (
                      <div className="flex items-start gap-2 p-3 bg-red-50 border border-red-200 rounded-xl">
                        <AlertCircle size={16} className="text-red-500 mt-0.5 flex-shrink-0" />
                        <p className="text-xs text-red-600 font-medium">{saveError}</p>
                      </div>
                    )}
                  </div>
                ) : null}
              </div>

              {/* Action bar – only show after generation */}
              {hasGenerated && lessonPlan && (
                <div className="px-6 py-4 border-t bg-slate-50 flex items-center justify-between gap-3">
                  <div className="text-xs text-slate-400 italic">
                    Chỉnh sửa nội dung bên trên, sau đó nhấn "Lưu" để lưu vào hệ thống.
                  </div>
                  <div className="flex gap-3">
                    <button
                      onClick={handleRegenerate}
                      disabled={isGenerating || isSaving}
                      className="flex items-center gap-2 px-5 py-2.5 border border-slate-300 text-slate-700 font-bold rounded-xl hover:bg-slate-100 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {isGenerating ? (
                        <Loader2 size={16} className="animate-spin" />
                      ) : (
                        <RotateCw size={16} />
                      )}
                      Tạo lại
                    </button>
                    <button
                      onClick={handleSave}
                      disabled={isGenerating || isSaving}
                      className="flex items-center gap-2 px-6 py-2.5 bg-green-600 text-white font-bold rounded-xl hover:bg-green-700 shadow-lg shadow-green-100 transition-all active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {isSaving ? (
                        <Loader2 size={16} className="animate-spin" />
                      ) : (
                        <Save size={16} />
                      )}
                      {isSaving ? 'Đang lưu...' : 'Lưu giáo án'}
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default LessonPlanGenerator
