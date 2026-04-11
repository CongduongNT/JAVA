import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { Sparkles, ArrowLeft, Send, Loader2, Copy, CheckCircle2, FileText } from 'lucide-react';
import { fetchTemplates } from '../../features/promptTemplates/promptTemplateSlice';
import { apiClient } from '../../features/auth/authApi';
import { toast } from 'sonner';

export default function GenerateLessonPlan() {
  const { id } = useParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { templates = [] } = useSelector((state) => state.promptTemplates || {});
  
  const [template, setTemplate] = useState(null);
  const [inputs, setInputs] = useState({});
  const [result, setResult] = useState('');
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [copied, setCopied] = useState(false);

  // Hàm khởi tạo inputs từ variables của template
  const initInputs = useCallback((tpl) => {
    const initialInputs = {};
    if (tpl.variables) {
      tpl.variables.split(',').forEach(v => {
        initialInputs[v.trim()] = '';
      });
    }
    setInputs(initialInputs);
  }, []);

  useEffect(() => {
    const safeTemplates = Array.isArray(templates) ? templates : [];
    const found = safeTemplates.find(t => t?.id?.toString() === id);
    
    if (found) {
      setTemplate(found);
      initInputs(found);
    } else if (templates.length === 0) {
      // Nếu F5 trang và store trống (chưa fetch dữ liệu), hãy fetch lại
      dispatch(fetchTemplates());
    } else {
      // Nếu đã fetch xong mà vẫn không thấy ID này thì mới báo lỗi
      toast.error("Không tìm thấy mẫu giáo án này hoặc bạn không có quyền sử dụng.");
      navigate('/lesson-plans');
    }
  }, [id, templates, dispatch, navigate, initInputs]);

  const handleInputChange = (key, value) => {
    setInputs(prev => ({ ...prev, [key]: value }));
  };

  const handleGenerate = async (e) => {
    e.preventDefault();
    setGenerating(true);
    setResult('');

    try {
      // Kết nối với Endpoint: POST /api/v1/prompt-templates/generate
      const response = await apiClient.post('/prompt-templates/generate', {
        templateId: id,
        inputs: inputs
      });
      
      setResult(response.data.content);
      toast.success('AI đã soạn giáo án thành công!');
    } catch (err) {
      toast.error('Lỗi AI: ' + (err.response?.data?.message || err.message));
    } finally {
      setGenerating(false);
    }
  };

  const copyResult = () => {
    navigator.clipboard.writeText(result);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
    toast.info('Đã sao chép nội dung giáo án');
  };

  if (!template) return <div className="p-8 text-center">Đang tải mẫu...</div>;

  return (
    <div className="max-w-5xl mx-auto space-y-6 p-4">
      <div className="flex items-center gap-4">
        <button onClick={() => navigate('/lesson-plans')} className="p-2 hover:bg-gray-100 rounded-full">
          <ArrowLeft className="size-5 text-gray-500" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{template.title}</h1>
          <p className="text-sm text-gray-500">Điền thông tin vào các trường bên dưới để AI bắt đầu soạn bài.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Cấu hình Input */}
        <form onSubmit={handleGenerate} className="bg-white p-6 rounded-2xl border shadow-sm space-y-5 h-fit">
          <div className="flex items-center gap-2 pb-2 border-b">
            <FileText className="size-5 text-primary" />
            <h2 className="font-bold text-gray-700">Thông tin bài dạy</h2>
          </div>
          
          {Object.keys(inputs).length > 0 ? (
            Object.keys(inputs).map((key) => (
              <div key={key} className="space-y-1.5">
                <label className="text-sm font-semibold text-gray-600 capitalize">
                  {key.replace('_', ' ')}
                </label>
                <input
                  required
                  type="text"
                  value={inputs[key]}
                  onChange={(e) => handleInputChange(key, e.target.value)}
                  placeholder={`Nhập ${key.toLowerCase()}...`}
                  className="w-full rounded-xl border-gray-200 bg-gray-50 px-4 py-2.5 focus:ring-2 focus:ring-primary/20 outline-none transition-all"
                />
              </div>
            ))
          ) : (
            <p className="text-sm text-gray-400 italic">Mẫu này không yêu cầu biến số đầu vào.</p>
          )}

          <button
            type="submit"
            disabled={generating}
            className="w-full flex items-center justify-center gap-2 bg-primary text-white py-3 rounded-xl font-bold hover:bg-primary/90 transition-all shadow-lg disabled:opacity-50"
          >
            {generating ? <Loader2 className="size-5 animate-spin" /> : <Sparkles className="size-5" />}
            Bắt đầu tạo giáo án
          </button>
        </form>

        {/* Kết quả hiển thị */}
        <div className="bg-white min-h-[500px] rounded-2xl border shadow-sm flex flex-col overflow-hidden">
          <div className="px-6 py-4 border-b bg-gray-50/50 flex justify-between items-center">
            <span className="text-xs font-black text-gray-500 uppercase">Bản thảo giáo án AI</span>
            {result && (
              <button onClick={copyResult} className="flex items-center gap-1.5 text-sm font-bold text-primary hover:opacity-80">
                {copied ? <CheckCircle2 className="size-4 text-green-500" /> : <Copy className="size-4" />}
                {copied ? 'Đã chép' : 'Sao chép'}
              </button>
            )}
          </div>
          
          <div className="p-6 flex-1 overflow-auto">
            {generating ? (
              <div className="h-full flex flex-col items-center justify-center text-gray-400 space-y-4">
                <Loader2 className="size-10 animate-spin text-primary/30" />
                <p className="animate-pulse font-medium">AI đang thiết kế giáo án phù hợp với yêu cầu...</p>
              </div>
            ) : result ? (
              <div className="prose prose-blue max-w-none whitespace-pre-wrap font-sans text-gray-800 leading-relaxed">
                {result}
              </div>
            ) : (
              <div className="h-full flex flex-col items-center justify-center text-gray-300 border-2 border-dashed border-gray-50 rounded-xl">
                <Sparkles className="size-12 opacity-10 mb-2" />
                <p className="text-sm">Kết quả sẽ hiển thị tại đây</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}