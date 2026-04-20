import React, { useEffect, useState } from 'react'
import { systemConfigApi } from '../../services/systemConfigApi'
import { Settings2, Save, RefreshCw, CheckCircle2, AlertCircle } from 'lucide-react'
import { toast } from 'sonner'

const CONFIG_LABELS = {
  max_questions_per_exam:     { label: 'Số câu hỏi tối đa / đề',     type: 'number', unit: 'câu' },
  max_file_size_mb:           { label: 'Kích thước file upload tối đa', type: 'number', unit: 'MB' },
  ai_timeout_seconds:         { label: 'Timeout gọi Gemini AI',        type: 'number', unit: 'giây' },
  ocr_max_files_batch:        { label: 'Số file OCR tối đa / batch',   type: 'number', unit: 'file' },
  allow_teacher_registration: { label: 'Cho phép giáo viên tự đăng ký', type: 'boolean', unit: '' },
  default_exam_duration_mins: { label: 'Thời gian làm bài mặc định',   type: 'number', unit: 'phút' },
  question_ai_batch_size:     { label: 'Số câu AI sinh mỗi batch',     type: 'number', unit: 'câu' },
}

export default function SystemConfigPage() {
  const [configs, setConfigs] = useState([])
  const [editValues, setEditValues] = useState({})
  const [saving, setSaving] = useState({})
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadConfigs()
  }, [])

  const loadConfigs = async () => {
    setLoading(true)
    try {
      const res = await systemConfigApi.getAll()
      const data = res.data || []
      setConfigs(data)
      const initValues = {}
      data.forEach(c => { initValues[c.configKey] = c.configValue })
      setEditValues(initValues)
    } catch {
      toast.error('Không thể tải cấu hình hệ thống')
    } finally {
      setLoading(false)
    }
  }

  const handleSave = async (key) => {
    setSaving(prev => ({ ...prev, [key]: true }))
    try {
      await systemConfigApi.update(key, editValues[key])
      toast.success(`Đã cập nhật "${CONFIG_LABELS[key]?.label || key}"`)
      await loadConfigs()
    } catch {
      toast.error('Cập nhật thất bại')
    } finally {
      setSaving(prev => ({ ...prev, [key]: false }))
    }
  }

  const isDirty = (key) => {
    const orig = configs.find(c => c.configKey === key)?.configValue
    return orig !== undefined && orig !== editValues[key]
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <RefreshCw className="w-8 h-8 animate-spin text-blue-500" />
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto">
      {/* Header */}
      <div className="flex items-center gap-3 mb-8">
        <div className="w-12 h-12 rounded-2xl bg-slate-900 flex items-center justify-center">
          <Settings2 className="w-6 h-6 text-white" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Cấu hình hệ thống</h1>
          <p className="text-sm text-slate-500 mt-0.5">Quản lý các tham số toàn cục của PlanbookAI</p>
        </div>
        <button
          onClick={loadConfigs}
          className="ml-auto flex items-center gap-2 px-4 py-2 text-sm font-medium text-slate-600 border border-slate-200 rounded-xl hover:bg-slate-50 transition-colors"
        >
          <RefreshCw className="w-4 h-4" /> Làm mới
        </button>
      </div>

      {/* Config table */}
      <div className="bg-white rounded-3xl border border-slate-100 shadow-lg overflow-hidden">
        <table className="w-full">
          <thead>
            <tr className="border-b border-slate-100 bg-slate-50">
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-6 py-4 w-1/3">Cấu hình</th>
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-6 py-4">Mô tả</th>
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-6 py-4 w-48">Giá trị</th>
              <th className="px-6 py-4 w-24"></th>
            </tr>
          </thead>
          <tbody>
            {configs.map((config, idx) => {
              const meta = CONFIG_LABELS[config.configKey] || { label: config.configKey, type: 'text', unit: '' }
              const dirty = isDirty(config.configKey)
              return (
                <tr
                  key={config.configKey}
                  className={`border-b border-slate-50 transition-colors ${dirty ? 'bg-amber-50/40' : idx % 2 === 0 ? 'bg-white' : 'bg-slate-50/30'}`}
                >
                  {/* Key */}
                  <td className="px-6 py-4">
                    <div>
                      <p className="font-semibold text-slate-800 text-sm">{meta.label}</p>
                      <code className="text-xs text-slate-400 mt-0.5 block">{config.configKey}</code>
                    </div>
                  </td>

                  {/* Description */}
                  <td className="px-6 py-4">
                    <p className="text-sm text-slate-500">{config.description}</p>
                    {config.updatedBy && (
                      <p className="text-xs text-slate-400 mt-1">
                        Cập nhật bởi: <span className="font-medium">{config.updatedBy}</span>
                      </p>
                    )}
                  </td>

                  {/* Value editor */}
                  <td className="px-6 py-4">
                    {meta.type === 'boolean' ? (
                      <select
                        value={editValues[config.configKey] || ''}
                        onChange={e => setEditValues(prev => ({ ...prev, [config.configKey]: e.target.value }))}
                        className="w-full px-3 py-2 text-sm border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                      >
                        <option value="true">✅ Bật (true)</option>
                        <option value="false">❌ Tắt (false)</option>
                      </select>
                    ) : (
                      <div className="flex items-center gap-1.5">
                        <input
                          type={meta.type === 'number' ? 'number' : 'text'}
                          value={editValues[config.configKey] || ''}
                          onChange={e => setEditValues(prev => ({ ...prev, [config.configKey]: e.target.value }))}
                          className="w-full px-3 py-2 text-sm border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
                          min={0}
                        />
                        {meta.unit && <span className="text-xs text-slate-400 whitespace-nowrap">{meta.unit}</span>}
                      </div>
                    )}
                  </td>

                  {/* Action */}
                  <td className="px-6 py-4 text-right">
                    {dirty ? (
                      <button
                        onClick={() => handleSave(config.configKey)}
                        disabled={saving[config.configKey]}
                        className="flex items-center gap-1.5 px-3 py-2 bg-blue-600 text-white text-xs font-semibold rounded-xl hover:bg-blue-700 disabled:opacity-50 transition-colors"
                      >
                        {saving[config.configKey]
                          ? <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                          : <Save className="w-3.5 h-3.5" />}
                        Lưu
                      </button>
                    ) : (
                      <CheckCircle2 className="w-5 h-5 text-emerald-400 ml-auto" />
                    )}
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>

        {configs.length === 0 && (
          <div className="flex flex-col items-center justify-center py-16 text-slate-400">
            <AlertCircle className="w-10 h-10 mb-3" />
            <p className="font-medium">Không có cấu hình nào</p>
          </div>
        )}
      </div>

      <p className="text-xs text-slate-400 mt-4 text-center">
        ⚠️ Thay đổi cấu hình có hiệu lực ngay lập tức. Hãy cẩn thận khi chỉnh sửa.
      </p>
    </div>
  )
}
