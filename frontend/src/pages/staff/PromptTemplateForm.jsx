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
    <div className="mx-auto max-w-4xl space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div className="flex items-center gap-6 bg-white p-8 rounded-[2rem] shadow-sm border border-slate-100">
        <Link
          to="/prompt-templates"
          className="group rounded-2xl p-4 bg-slate-50 text-slate-500 hover:bg-indigo-50 hover:text-indigo-600 transition-all shadow-sm"
        >
          <ArrowLeft className="size-6 group-hover:-translate-x-1 transition-transform" />
        </Link>
        <div>
          <h1 className="text-3xl font-black tracking-tight text-slate-900">
            {isEdit ? 'Chỉnh sửa Template' : 'Tạo Prompt Template mới'}
          </h1>
          <p className="text-sm text-slate-500 font-medium mt-1">
            {isEdit ? 'Cập nhật nội dung câu lệnh AI hiện có.' : 'Vai trò: Nhân viên nội dung (Staff) - Nội dung sẽ được gửi duyệt sau khi lưu.'}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-8 rounded-[2.5rem] border-none bg-white p-10 shadow-2xl shadow-slate-200/60">
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
          <div className="space-y-3">
            <label className="text-xs font-black uppercase tracking-widest text-slate-500 ml-1">Tiêu đề template</label>
            <input
              required
              type="text"
              name="title"
              value={formData.title}
              onChange={handleChange}
              placeholder="VD: Soạn đề kiểm tra Hóa học 10"
              className="w-full rounded-2xl border-2 border-slate-100 bg-slate-50/50 px-5 py-4 text-sm font-bold text-slate-700 focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10 outline-none transition-all placeholder:text-slate-300"
            />
          </div>

          <div className="space-y-3">
            <label className="text-xs font-black uppercase tracking-widest text-slate-500 ml-1">Mục đích sử dụng</label>
            <select
              name="purpose"
              value={formData.purpose}
              onChange={handleChange}
              className="w-full rounded-2xl border-2 border-slate-100 bg-slate-50/50 px-5 py-4 text-sm font-bold text-slate-700 focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10 outline-none transition-all appearance-none cursor-pointer"
            >
              <option value="QUESTION_GEN">Tạo câu hỏi (Question Generation)</option>
              <option value="LESSON_PLAN_GEN">Soạn giáo án (Lesson Plan)</option>
              <option value="EXAM_GEN">Tạo đề thi (Exam Generation)</option>
            </select>
          </div>
        </div>

        <div className="space-y-3">
          <label className="text-xs font-black uppercase tracking-widest text-slate-500 ml-1">Các biến số (Variables)</label>
          <input
            type="text"
            name="variables"
            value={formData.variables}
            onChange={handleChange}
            placeholder="VD: subject, grade, topic, difficulty"
            className="w-full rounded-2xl border-2 border-slate-100 bg-slate-50/50 px-5 py-4 font-mono text-xs font-bold text-indigo-600 focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10 outline-none transition-all placeholder:text-slate-300"
          />
          <p className="text-[10px] text-slate-400 font-bold uppercase tracking-tight flex items-center gap-1.5 ml-1">
            <Info className="size-3" /> Phân cách các biến bằng dấu phẩy. Các biến này sẽ được dùng trong nội dung prompt.
          </p>
          {formData.purpose === 'LESSON_PLAN_GEN' && (
            <p className="text-xs text-indigo-500 font-black mt-1 ml-1">
              💡 Gợi ý cho Giáo án: subject, topic, grade_level, objectives, duration
            </p>
          )}
        </div>

        <div className="space-y-3">
          <label className="text-xs font-black uppercase tracking-widest text-slate-500 ml-1">Nội dung Prompt (System Instruction)</label>
          <textarea
            required
            name="promptText"
            value={formData.promptText}
            onChange={handleChange}
            rows={10}
            placeholder="Nhập nội dung hướng dẫn cho AI tại đây..."
            className="w-full rounded-[2rem] border-2 border-slate-100 bg-slate-50/50 px-6 py-6 font-mono text-xs font-bold text-slate-600 focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10 outline-none transition-all resize-none leading-relaxed"
          />
          <p className="text-[10px] text-slate-400 font-bold uppercase tracking-tight flex items-center gap-1.5 ml-1">
            Sử dụng cú pháp <code className="bg-slate-100 px-2 py-0.5 rounded text-indigo-600 font-black">{"{{variable_name}}"}</code> để đánh dấu vị trí các biến.
          </p>
        </div>

        <div className="flex justify-end gap-4 pt-8 border-t border-slate-50">
          <button
            type="button"
            onClick={() => navigate('/prompt-templates')}
            className="px-8 py-4 rounded-2xl border-2 border-slate-100 text-xs font-black uppercase tracking-widest text-slate-400 hover:bg-slate-50 hover:text-slate-600 transition-all active:scale-95"
          >
            Hủy bỏ
          </button>
          <button
            type="submit"
            disabled={issubmitting}
            className="flex items-center gap-2 rounded-2xl bg-indigo-600 px-10 py-4 text-xs font-black uppercase tracking-widest text-white shadow-xl shadow-indigo-100 hover:bg-indigo-700 hover:-translate-y-0.5 transition-all active:scale-95 disabled:opacity-70 disabled:pointer-events-none"
          >
            {issubmitting ? <Loader2 className="size-5 animate-spin" /> : <Save className="size-5" />}
            {isEdit ? 'Cập nhật Template' : 'Lưu Template'}
          </button>
        </div>
      </form>
    </div>
  );
}
