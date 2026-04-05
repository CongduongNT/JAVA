import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { ArrowLeft, Save, Loader2, Info } from 'lucide-react';
import { createTemplate, updateTemplate, fetchTemplates } from '../../features/promptTemplates/promptTemplateSlice';
import { toast } from 'sonner';

export default function PromptTemplateForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const isEdit = !!id;

  // Lấy dữ liệu từ store nếu đang ở chế độ chỉnh sửa
  const { templates = [], loading = false } = useSelector((state) => state.promptTemplates || {});
  
  const [formData, setFormData] = useState({
    title: '',
    purpose: 'QUESTION_GEN',
    promptText: '',
    variables: '',
  });

  const [issubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    // Nếu là chế độ edit và chưa có danh sách templates thì tải về
    if (isEdit && templates.length === 0) {
      dispatch(fetchTemplates());
    }
  }, [dispatch, isEdit, templates.length]);

  useEffect(() => {
    // Khi đã có dữ liệu templates, tìm template cần sửa để điền vào form
    if (isEdit && templates.length > 0) {
      const template = templates.find((t) => t.id.toString() === id);
      if (template) {
        setFormData({
          title: template.title,
          purpose: template.purpose,
          promptText: template.promptText,
          variables: template.variables || '',
        });
      } else {
        toast.error('Không tìm thấy template cần chỉnh sửa');
        navigate('/prompt-templates');
      }
    }
  }, [isEdit, id, templates, navigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      if (isEdit) {
        await dispatch(updateTemplate({ id, data: formData })).unwrap();
        toast.success('Cập nhật template thành công');
      } else {
        await dispatch(createTemplate(formData)).unwrap();
        toast.success('Tạo mới template thành công');
      }
      navigate('/prompt-templates');
    } catch (err) {
      toast.error(err || 'Đã có lỗi xảy ra');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isEdit && loading && templates.length === 0) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loader2 className="size-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <div className="flex items-center gap-4">
        <Link
          to="/prompt-templates"
          className="rounded-full p-2 text-gray-500 hover:bg-gray-100 transition-colors"
        >
          <ArrowLeft className="size-5" />
        </Link>
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-gray-900">
            {isEdit ? 'Chỉnh sửa Template' : 'Tạo Prompt Template mới'}
          </h1>
          <p className="text-sm text-gray-500">
            {isEdit ? 'Cập nhật nội dung câu lệnh AI hiện có.' : 'Thiết lập câu lệnh AI mẫu để hỗ trợ các công cụ tạo nội dung.'}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6 rounded-xl border bg-white p-6 shadow-sm">
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
          <div className="space-y-2">
            <label className="text-sm font-semibold text-gray-700">Tiêu đề template</label>
            <input
              required
              type="text"
              name="title"
              value={formData.title}
              onChange={handleChange}
              placeholder="VD: Soạn đề kiểm tra Hóa học 10"
              className="w-full rounded-lg border border-gray-300 px-4 py-2 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
            />
          </div>

          <div className="space-y-2">
            <label className="text-sm font-semibold text-gray-700">Mục đích sử dụng</label>
            <select
              name="purpose"
              value={formData.purpose}
              onChange={handleChange}
              className="w-full rounded-lg border border-gray-300 px-4 py-2 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all appearance-none bg-white"
            >
              <option value="QUESTION_GEN">Tạo câu hỏi (Question Generation)</option>
              <option value="LESSON_PLAN_GEN">Soạn giáo án (Lesson Plan)</option>
              <option value="EXAM_GEN">Tạo đề thi (Exam Generation)</option>
            </select>
          </div>
        </div>

        <div className="space-y-2">
          <label className="text-sm font-semibold text-gray-700">Các biến số (Variables)</label>
          <input
            type="text"
            name="variables"
            value={formData.variables}
            onChange={handleChange}
            placeholder="VD: subject, grade, topic, difficulty"
            className="w-full rounded-lg border border-gray-300 px-4 py-2 font-mono text-sm focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
          />
          <p className="text-xs text-gray-400 italic flex items-center gap-1">
            <Info className="size-3" /> Phân cách các biến bằng dấu phẩy. Các biến này sẽ được dùng trong nội dung prompt.
          </p>
        </div>

        <div className="space-y-2">
          <label className="text-sm font-semibold text-gray-700">Nội dung Prompt (System Instruction)</label>
          <textarea
            required
            name="promptText"
            value={formData.promptText}
            onChange={handleChange}
            rows={10}
            placeholder="Nhập nội dung hướng dẫn cho AI tại đây..."
            className="w-full rounded-lg border border-gray-300 px-4 py-2 font-mono text-sm focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all resize-none"
          />
          <p className="text-xs text-gray-500">
            Sử dụng cú pháp <code className="bg-gray-100 px-1 rounded">{"{{variable_name}}"}</code> để đánh dấu vị trí các biến sẽ được điền vào.
          </p>
        </div>

        <div className="flex justify-end gap-3 pt-4 border-t">
          <button
            type="button"
            onClick={() => navigate('/prompt-templates')}
            className="px-6 py-2 rounded-lg border border-gray-300 text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors"
          >
            Hủy bỏ
          </button>
          <button
            type="submit"
            disabled={issubmitting}
            className="flex items-center gap-2 rounded-lg bg-primary px-6 py-2 text-sm font-medium text-white shadow hover:bg-primary/90 transition-all disabled:opacity-70"
          >
            {issubmitting ? <Loader2 className="size-4 animate-spin" /> : <Save className="size-4" />}
            {isEdit ? 'Cập nhật Template' : 'Lưu Template'}
          </button>
        </div>
      </form>
    </div>
  );
}
