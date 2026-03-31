import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import { apiClient } from '../../features/auth/authApi';
import { 
  FileText, 
  Brain, 
  Sparkles, 
  Send, 
  Loader2, 
  Copy, 
  CheckCircle2, 
  Layout,
  PlusCircle,
  HelpCircle,
  Inbox
} from 'lucide-react';
import { toast } from 'sonner';

const ExamGenerator = () => {
  const [templates, setTemplates] = useState([]);
  const [selectedTemplate, setSelectedTemplate] = useState('');
  const [topic, setTopic] = useState('');
  const [level, setLevel] = useState('Dễ');
  
  const [loadingTemplates, setLoadingTemplates] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [aiResult, setAiResult] = useState('');
  const [copied, setCopied] = useState(false);

  // Lấy token để đảm bảo đã đăng nhập trước khi gọi API
  const { token } = useSelector((state) => state.auth);

  // 1. Tải danh sách các Template AI đã duyệt dành cho việc tạo câu hỏi/đề thi
  useEffect(() => {
    const fetchTemplates = async () => {
      try {
        // Gọi API với tham số lọc purpose trực tiếp từ Backend
        const response = await apiClient.get('/prompt-templates/approved', { params: { purpose: 'QUESTION_GEN' } });
        setTemplates(response.data);
      } catch (err) {
        console.error('Lỗi khi tải AI templates:', err);
        toast.error('Không thể tải danh sách mẫu AI');
      } finally {
        setLoadingTemplates(false);
      }
    };
    if (token) fetchTemplates();
  }, [token]);

  // 2. Xử lý gọi AI tạo nội dung
  const handleGenerateAI = async () => {
    if (!selectedTemplate) return toast.error('Vui lòng chọn một mẫu câu hỏi');
    if (!topic.trim()) return toast.error('Vui lòng nhập chủ đề');

    setGenerating(true);
    setAiResult('');

    try {
      const response = await apiClient.post('/prompt-templates/generate', {
        templateId: selectedTemplate,
        inputs: {
          topic: topic,
          level: level
        }
      });
      
      // Backend trả về content đã được xử lý bởi AI
      setAiResult(response.data.content);
      toast.success('AI đã tạo câu hỏi thành công!');
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Lỗi kết nối AI';
      toast.error('Có lỗi: ' + errorMsg);
    } finally {
      setGenerating(false);
    }
  };

  const handleCopy = () => {
    navigator.clipboard.writeText(aiResult);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
    toast.info('Đã sao chép nội dung');
  };

  return (
    <div className="p-6 max-w-6xl mx-auto space-y-8 animate-in fade-in duration-500">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b pb-6">
        <div>
          <h1 className="text-2xl font-black text-slate-800 flex items-center gap-2">
            <Layout className="size-6 text-blue-600" /> Trình Tạo Đề Thi Thông Minh
          </h1>
          <p className="text-slate-500 font-medium font-sans">Sử dụng AI để soạn thảo câu hỏi nhanh chóng.</p>
        </div>
        <button className="flex items-center justify-center gap-2 bg-slate-900 text-white px-4 py-2 rounded-xl text-sm font-bold hover:bg-slate-800 transition-all shadow-lg">
          <PlusCircle className="size-4" /> Tạo đề thủ công
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Cấu hình AI bên trái */}
        <div className="lg:col-span-4 space-y-6">
          <div className="bg-white rounded-[2rem] border-2 border-slate-100 shadow-xl shadow-slate-200/40 p-8 space-y-6 overflow-hidden">
            <div className="flex items-center gap-2 mb-2">
              <div className="p-2 bg-blue-50 rounded-lg text-blue-600">
                <Sparkles className="size-5" />
              </div>
              <h2 className="font-black text-slate-800 uppercase tracking-wider text-sm">Trợ lý AI</h2>
            </div>

            <div className="space-y-2">
              <label className="text-xs font-black text-slate-500 uppercase ml-1">Chọn mẫu Template</label>
              <select
                value={selectedTemplate}
                onChange={(e) => setSelectedTemplate(e.target.value)}
                disabled={loadingTemplates}
                className="w-full bg-slate-50 border-none rounded-2xl px-4 py-4 text-sm font-bold text-slate-700 outline-none transition-all cursor-pointer"
              >
                <option value="">-- Chọn loại nội dung --</option>
                {templates.map((t) => (
                  <option key={t.id} value={t.id}>{t.title}</option>
                ))}
              </select>
              {templates.length === 0 && !loadingTemplates && (
                <p className="text-[10px] text-amber-600 mt-1 italic">* Không tìm thấy mẫu tạo câu hỏi nào được duyệt.</p>
              )}
            </div>

            <div className="space-y-2">
              <label className="text-xs font-black text-slate-500 uppercase ml-1">Chủ đề (Topic)</label>
              <input
                type="text"
                placeholder="VD: Sinh học, Lịch sử..."
                value={topic}
                onChange={(e) => setTopic(e.target.value)}
                className="w-full bg-slate-50 border-none rounded-2xl px-4 py-4 text-sm font-bold text-slate-700 outline-none transition-all"
              />
            </div>

            <div className="space-y-2">
              <label className="text-xs font-black text-slate-500 uppercase ml-1">Độ khó (Level)</label>
              <div className="flex p-1 bg-slate-50 rounded-2xl">
                {['Dễ', 'Trung bình', 'Khó'].map((l) => (
                  <button
                    key={l}
                    onClick={() => setLevel(l)}
                    className={`flex-1 py-3 rounded-xl text-xs font-black transition-all ${level === l ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-400'}`}
                  >
                    {l}
                  </button>
                ))}
              </div>
            </div>

            <button
              onClick={handleGenerateAI}
              disabled={generating || loadingTemplates}
              className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 text-white py-5 rounded-[1.25rem] font-black text-sm uppercase tracking-widest flex items-center justify-center gap-3 shadow-lg active:scale-95 transition-all disabled:opacity-50"
            >
              {generating ? <Loader2 className="size-5 animate-spin" /> : <Send className="size-5" />}
              Tạo với AI
            </button>
          </div>
        </div>

        {/* Kết quả bên phải */}
        <div className="lg:col-span-8">
          <div className="bg-white h-full min-h-[500px] rounded-[2.5rem] border-2 border-slate-50 shadow-2xl flex flex-col overflow-hidden">
            <div className="px-8 py-5 border-b bg-slate-50/30 flex items-center justify-between">
              <span className="text-xs font-black text-slate-500 uppercase tracking-widest">Kết quả soạn thảo</span>
              {aiResult && (
                <button onClick={handleCopy} className="text-xs font-bold text-blue-600 flex items-center gap-2 hover:bg-blue-50 px-3 py-1 rounded-lg">
                  {copied ? <CheckCircle2 className="size-4" /> : <Copy className="size-4" />} Sao chép
                </button>
              )}
            </div>
            <div className="flex-1 p-8 overflow-y-auto bg-slate-50/10">
              {generating ? (
                <div className="h-full flex flex-col items-center justify-center text-center space-y-4">
                  <Loader2 className="size-12 text-blue-600 animate-spin" />
                  <p className="font-bold text-slate-800 animate-pulse">AI đang soạn câu hỏi cho bạn...</p>
                </div>
              ) : aiResult ? (
                <div className="prose prose-slate max-w-none">
                  <div className="whitespace-pre-wrap font-sans text-slate-700 leading-relaxed text-lg italic">
                    {aiResult}
                  </div>
                </div>
              ) : (
                <div className="h-full flex flex-col items-center justify-center text-slate-400 space-y-4 border-4 border-dashed border-slate-50 rounded-[2rem]">
                  <Inbox className="size-12 opacity-20" />
                  <p className="font-bold uppercase tracking-widest text-sm">Chưa có nội dung</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ExamGenerator;