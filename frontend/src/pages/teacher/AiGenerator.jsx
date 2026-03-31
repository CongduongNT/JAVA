import React, { useState, useEffect } from 'react';
import { apiClient } from '../../features/auth/authApi';
import { Sparkles, Brain, Send, Loader2, Copy, CheckCircle2, AlertCircle, Inbox, Lightbulb } from 'lucide-react';
import { toast } from 'sonner';

export default function AiGenerator() {
  const [templates, setTemplates] = useState([]);
  const [selectedTemplate, setSelectedTemplate] = useState('');
  const [topic, setTopic] = useState('');
  const [level, setLevel] = useState('Dễ');
  const [result, setResult] = useState('');
  const [loadingTemplates, setLoadingTemplates] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [copied, setCopied] = useState(false);

  // 1. Lấy danh sách templates từ Backend
  useEffect(() => {
    const fetchTemplates = async () => {
      try {
        const response = await apiClient.get('/prompt-templates/approved');
        setTemplates(response.data);
        setLoadingTemplates(false);
      } catch (err) {
        console.error('Lỗi khi tải templates:', err);
        setLoadingTemplates(false);
      }
    };
    fetchTemplates();
  }, []);

  // 2. Xử lý khi nhấn nút Generate
  const handleGenerate = async () => {
    if (!selectedTemplate || !topic) {
      toast.error('Vui lòng chọn template và nhập chủ đề');
      return;
    }

    setGenerating(true);
    setResult('');

    try {
      // Gọi API xử lý AI (Gemini/GPT) trên Backend
      const response = await apiClient.post('/prompt-templates/generate', {
        templateId: selectedTemplate,
        inputs: {
          topic: topic,
          level: level
        }
      });
      
      setResult(response.data.content);
      toast.success('AI đã tạo nội dung thành công!');
    } catch (err) {
      toast.error('Có lỗi khi gọi AI: ' + (err.response?.data?.message || err.message));
    } finally {
      setGenerating(false);
    }
  };

  const copyToClipboard = () => {
    navigator.clipboard.writeText(result);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
    toast.info('Đã sao chép vào bộ nhớ tạm');
  };

  return (
    <div className="mx-auto max-w-5xl space-y-8 p-4">
      {/* Header */}
      <div className="text-center space-y-2">
        <div className="inline-flex items-center justify-center p-2 bg-primary/10 rounded-full text-primary mb-2">
          <Sparkles className="size-6" />
        </div>
        <h1 className="text-3xl font-bold text-gray-900">Trợ lý Soạn bài AI</h1>
        <p className="text-gray-500">Sử dụng sức mạnh trí tuệ nhân tạo để tạo câu hỏi và giáo án trong giây lát.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Cấu hình bên trái */}
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-white p-6 rounded-2xl border shadow-sm space-y-4">
            <div className="space-y-2">
              <label className="text-sm font-semibold text-gray-700 flex items-center gap-2">
                <Brain className="size-4 text-primary" /> Chọn mẫu (Template)
              </label>
              <select
                value={selectedTemplate}
                onChange={(e) => setSelectedTemplate(e.target.value)}
                disabled={loadingTemplates}
                className="w-full rounded-xl border-gray-200 bg-gray-50 px-4 py-3 focus:ring-2 focus:ring-primary/20 outline-none transition-all"
              >
                <option value="">-- Chọn mẫu (VD: Tạo câu hỏi...) --</option>
                {templates.map((t) => (
                  <option key={t.id} value={t.id}>{t.title}</option>
                ))}
              </select>
              {templates.length === 0 && !loadingTemplates && (
                <div className="flex items-center gap-1 mt-2 p-2 bg-amber-50 rounded-lg border border-amber-100">
                  <Lightbulb className="size-3 text-amber-600" />
                  <p className="text-[10px] text-amber-700">Chưa có mẫu nào. Hãy nhờ Admin/Staff duyệt template trước.</p>
                </div>
              )}
            </div>

            <div className="space-y-2">
              <label className="text-sm font-semibold text-gray-700">Chủ đề (Topic)</label>
              <input
                type="text"
                value={topic}
                onChange={(e) => setTopic(e.target.value)}
                placeholder="VD: Quang hợp, Giải tích 12..."
                className="w-full rounded-xl border-gray-200 bg-gray-50 px-4 py-3 focus:ring-2 focus:ring-primary/20 outline-none"
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-semibold text-gray-700">Độ khó (Level)</label>
              <div className="flex gap-2">
                {['Dễ', 'Trung bình', 'Khó'].map((l) => (
                  <button
                    key={l}
                    type="button"
                    onClick={() => setLevel(l)}
                    className={`flex-1 py-2 rounded-lg text-sm font-medium transition-all ${
                      level === l 
                      ? 'bg-blue-600 text-white shadow-md' 
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                    }`}
                  >
                    {l}
                  </button>
                ))}
              </div>
            </div>

            <button
              onClick={handleGenerate}
              disabled={generating || loadingTemplates}
              className="w-full mt-4 flex items-center justify-center gap-2 bg-gradient-to-r from-blue-600 to-indigo-600 text-white py-3 rounded-xl font-bold hover:opacity-90 transition-all shadow-lg shadow-blue-200 disabled:opacity-50"
            >
              {generating ? <Loader2 className="size-5 animate-spin" /> : <Send className="size-5" />}
              Tạo với AI
            </button>
          </div>
        </div>

        {/* Kết quả bên phải */}
        <div className="lg:col-span-2">
          <div className="bg-white min-h-[500px] rounded-2xl border shadow-sm flex flex-col overflow-hidden">
            <div className="px-6 py-4 border-b bg-gray-50/50 flex justify-between items-center">
              <span className="text-sm font-bold text-gray-700 uppercase tracking-wider">Kết quả từ AI</span>
              {result && (
                <button 
                  onClick={copyToClipboard}
                  className="text-gray-500 hover:text-primary transition-colors flex items-center gap-1 text-sm font-medium"
                >
                  {copied ? <CheckCircle2 className="size-4 text-green-500" /> : <Copy className="size-4" />}
                  {copied ? 'Đã chép' : 'Sao chép'}
                </button>
              )}
            </div>
            
            <div className="p-6 flex-1 overflow-auto bg-slate-50/30">
              {generating ? (
                <div className="h-full flex flex-col items-center justify-center text-gray-400 space-y-4">
                  <Loader2 className="size-12 animate-spin text-primary/40" />
                  <p className="animate-pulse">AI đang suy nghĩ và soạn câu hỏi cho bạn...</p>
                </div>
              ) : result ? (
                <div className="prose prose-slate max-w-none whitespace-pre-wrap font-sans text-slate-800 leading-relaxed animate-in fade-in slide-in-from-bottom-2 duration-500">
                  {result}
                </div>
              ) : (
                <div className="h-full flex flex-col items-center justify-center text-gray-400 space-y-2 border-2 border-dashed border-gray-100 rounded-xl">
                  <Inbox className="size-10 opacity-20" />
                  <p>{templates.length > 0 ? 'Chọn cấu hình và nhấn "Tạo với AI"' : 'Không có dữ liệu mẫu để hiển thị'}</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
