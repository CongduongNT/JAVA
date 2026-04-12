import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { ArrowLeft, Save, Loader2, Info, FileJson } from 'lucide-react';
import frameworkApi from '../../services/frameworkApi';
import { toast } from 'sonner';

/**
 * FrameworkForm – Form tạo / chỉnh sửa Curriculum Framework.
 *
 * Các trường:
 * - title: Tiêu đề (bắt buộc)
 * - subject: Môn học
 * - gradeLevel: Khối lớp
 * - description: Mô tả
 * - structure: Cấu trúc JSON (nội dung chương trình khung)
 * - isPublished: Publish ngay khi tạo (chỉ khi tạo mới)
 */
const FrameworkForm = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = !!id;

  const [formData, setFormData] = useState({
    title: '',
    subject: '',
    gradeLevel: '',
    description: '',
    structure: '',
    isPublished: false,
  });

  const [structureError, setStructureError] = useState('');
  const [loading, setLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(isEdit);

  useEffect(() => {
    if (isEdit) {
      fetchFramework();
    }
  }, [isEdit, id]);

  const fetchFramework = async () => {
    try {
      setFetchLoading(true);
      const res = await frameworkApi.getFramework(id);
      const framework = res.data;
      
      setFormData({
        title: framework.title || '',
        subject: framework.subject || '',
        gradeLevel: framework.gradeLevel || '',
        description: framework.description || '',
        structure: framework.structure || '',
        isPublished: framework.isPublished || false,
      });
    } catch (err) {
      console.error('Failed to fetch framework:', err);
      toast.error('Không thể tải thông tin framework.');
      navigate('/admin/frameworks');
    } finally {
      setFetchLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));

    // Clear structure error when user types
    if (name === 'structure') {
      setStructureError('');
    }
  };

  const validateStructure = (structure) => {
    if (!structure || structure.trim() === '') {
      return true; // Structure có thể để trống
    }
    try {
      JSON.parse(structure);
      return true;
    } catch (e) {
      return false;
    }
  };

  const formatJson = () => {
    if (!formData.structure) return;
    
    try {
      const parsed = JSON.parse(formData.structure);
      const formatted = JSON.stringify(parsed, null, 2);
      setFormData(prev => ({ ...prev, structure: formatted }));
      setStructureError('');
      toast.success('Đã format JSON');
    } catch (e) {
      setStructureError('JSON không hợp lệ, không thể format');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate JSON structure
    if (!validateStructure(formData.structure)) {
      setStructureError('Cấu trúc JSON không hợp lệ. Vui lòng kiểm tra lại.');
      toast.error('Cấu trúc JSON không hợp lệ');
      return;
    }

    setIsSubmitting(true);

    try {
      const payload = {
        title: formData.title,
        subject: formData.subject || null,
        gradeLevel: formData.gradeLevel || null,
        description: formData.description || null,
        structure: formData.structure || null,
      };

      if (isEdit) {
        await frameworkApi.updateFramework(id, payload);
        toast.success('Cập nhật framework thành công');
      } else {
        // Thêm isPublished nếu tạo mới
        if (formData.isPublished) {
          payload.isPublished = true;
        }
        await frameworkApi.createFramework(payload);
        toast.success('Tạo framework thành công');
      }
      
      navigate('/admin/frameworks');
    } catch (err) {
      console.error('Failed to save framework:', err);
      toast.error(err.response?.data?.message || 'Đã có lỗi xảy ra khi lưu framework');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (fetchLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loader2 className="size-8 animate-spin text-indigo-500" />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-5xl space-y-6 p-4 md:p-8">
      {/* ── Header ── */}
      <div className="flex items-center gap-4">
        <Link
          to="/admin/frameworks"
          className="rounded-full p-2 text-slate-500 hover:bg-slate-100 transition-colors"
        >
          <ArrowLeft className="size-5" />
        </Link>
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            {isEdit ? 'Chỉnh sửa Framework' : 'Tạo Framework mới'}
          </h1>
          <p className="text-sm text-slate-500">
            {isEdit 
              ? 'Cập nhật thông tin chương trình khung.' 
              : 'Thiết lập chương trình khung giáo dục mới.'}
          </p>
        </div>
      </div>

      {/* ── Form ── */}
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Basic Info Card */}
        <div className="bg-white rounded-xl border border-slate-200 p-6 space-y-6">
          <h2 className="text-lg font-semibold text-slate-900 flex items-center gap-2">
            <Info size={20} className="text-indigo-500" />
            Thông tin cơ bản
          </h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Title */}
            <div className="space-y-2 md:col-span-2">
              <label className="text-sm font-semibold text-slate-700">
                Tiêu đề <span className="text-rose-500">*</span>
              </label>
              <input
                required
                type="text"
                name="title"
                value={formData.title}
                onChange={handleChange}
                placeholder="VD: Chương trình Toán lớp 10"
                className="w-full rounded-lg border border-slate-300 px-4 py-2.5 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20 outline-none transition-all"
              />
            </div>

            {/* Subject */}
            <div className="space-y-2">
              <label className="text-sm font-semibold text-slate-700">Môn học</label>
              <input
                type="text"
                name="subject"
                value={formData.subject}
                onChange={handleChange}
                placeholder="VD: Toán, Văn, Anh..."
                className="w-full rounded-lg border border-slate-300 px-4 py-2.5 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20 outline-none transition-all"
              />
            </div>

            {/* Grade Level */}
            <div className="space-y-2">
              <label className="text-sm font-semibold text-slate-700">Khối lớp</label>
              <input
                type="text"
                name="gradeLevel"
                value={formData.gradeLevel}
                onChange={handleChange}
                placeholder="VD: 10, 11, 12..."
                className="w-full rounded-lg border border-slate-300 px-4 py-2.5 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20 outline-none transition-all"
              />
            </div>

            {/* Description */}
            <div className="space-y-2 md:col-span-2">
              <label className="text-sm font-semibold text-slate-700">Mô tả</label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                rows={3}
                placeholder="Mô tả ngắn về chương trình khung..."
                className="w-full rounded-lg border border-slate-300 px-4 py-2.5 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20 outline-none transition-all resize-none"
              />
            </div>
          </div>
        </div>

        {/* Structure Card */}
        <div className="bg-white rounded-xl border border-slate-200 p-6 space-y-6">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-slate-900 flex items-center gap-2">
              <FileJson size={20} className="text-indigo-500" />
              Cấu trúc chương trình (JSON)
            </h2>
            <button
              type="button"
              onClick={formatJson}
              className="text-sm text-indigo-600 hover:text-indigo-700 font-medium px-3 py-1.5 rounded-lg hover:bg-indigo-50 transition-colors"
            >
              Format JSON
            </button>
          </div>

          <div className="space-y-2">
            <textarea
              name="structure"
              value={formData.structure}
              onChange={handleChange}
              rows={15}
              placeholder={`{
  "chapters": [
    {
      "name": "Chương 1: Hàm số",
      "lessons": [
        { "name": "Bài 1: Khái niệm hàm số", "duration": 45 },
        { "name": "Bài 2: Tính chất hàm số", "duration": 45 }
      ]
    }
  ]
}`}
              className={`w-full rounded-lg border px-4 py-3 font-mono text-sm focus:ring-2 outline-none transition-all resize-none ${
                structureError
                  ? 'border-rose-300 focus:border-rose-500 focus:ring-rose-500/20 bg-rose-50/30'
                  : 'border-slate-300 focus:border-indigo-500 focus:ring-indigo-500/20'
              }`}
            />
            {structureError && (
              <p className="text-sm text-rose-600 flex items-center gap-1">
                <Info size={14} />
                {structureError}
              </p>
            )}
            <p className="text-xs text-slate-500">
              Nhập cấu trúc chương trình dưới dạng JSON. Có thể để trống nếu chưa có nội dung.
            </p>
          </div>
        </div>

        {/* Publish Option (only for create) */}
        {!isEdit && (
          <div className="bg-white rounded-xl border border-slate-200 p-6">
            <label className="flex items-center gap-3 cursor-pointer">
              <input
                type="checkbox"
                name="isPublished"
                checked={formData.isPublished}
                onChange={handleChange}
                className="w-5 h-5 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500"
              />
              <div>
                <p className="font-medium text-slate-900">Publish ngay sau khi tạo</p>
                <p className="text-sm text-slate-500">
                  Framework sẽ hiển thị cho Teacher ngay lập tức.
                </p>
              </div>
            </label>
          </div>
        )}

        {/* ── Actions ── */}
        <div className="flex justify-end gap-3 pt-4">
          <Link
            to="/admin/frameworks"
            className="px-6 py-2.5 rounded-lg border border-slate-300 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors"
          >
            Hủy bỏ
          </Link>
          <button
            type="submit"
            disabled={isSubmitting}
            className="flex items-center gap-2 rounded-lg bg-indigo-600 px-6 py-2.5 text-sm font-medium text-white shadow hover:bg-indigo-700 transition-all disabled:opacity-70"
          >
            {isSubmitting ? (
              <Loader2 className="size-4 animate-spin" />
            ) : (
              <Save className="size-4" />
            )}
            {isEdit ? 'Cập nhật' : 'Tạo framework'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default FrameworkForm;
