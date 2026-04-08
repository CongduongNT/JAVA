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
  Inbox,
  Trash2,
  Plus,
  Save
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

  // State cho phần tạo đề thủ công
  const [mode, setMode] = useState('ai'); // 'ai' hoặc 'manual'
  const [manualQuestions, setManualQuestions] = useState([]);
  const [currentQuestion, setCurrentQuestion] = useState({
    text: '',
    options: ['', '', '', ''],
    correctAnswer: 0
  });

  // Lấy token để đảm bảo đã đăng nhập trước khi gọi API
  const { token } = useSelector((state) => state.auth);

  // 1. Tải danh sách các Template AI đã duyệt dành cho việc tạo câu hỏi/đề thi
  useEffect(() => {
    const fetchTemplates = async () => {
      try {
        // Gọi API với tham số lọc purpose trực tiếp từ Backend
        const response = await apiClient.get('/prompt-templates', { params: { purpose: 'QUESTION_GEN' } });
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

  // Xử lý thêm câu hỏi thủ công
  const handleAddManualQuestion = () => {
    if (!currentQuestion.text.trim()) return toast.error('Vui lòng nhập nội dung câu hỏi');
    if (currentQuestion.options.some(opt => !opt.trim())) return toast.error('Vui lòng nhập đủ 4 phương án');
    
    setManualQuestions([...manualQuestions, { ...currentQuestion, id: Date.now() }]);
    setCurrentQuestion({ text: '', options: ['', '', '', ''], correctAnswer: 0 });
    toast.success('Đã thêm câu hỏi vào danh sách');
  };

  const handleOptionChange = (index, value) => {
    const newOptions = [...currentQuestion.options];
    newOptions[index] = value;
    setCurrentQuestion({ ...currentQuestion, options: newOptions });
  };

  const removeQuestion = (id) => {
    setManualQuestions(manualQuestions.filter(q => q.id !== id));
  };

  const alphabet = ['A', 'B', 'C', 'D'];

  return (
    <div className="p-6 max-w-6xl mx-auto space-y-8 animate-in fade-in duration-500">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b pb-6">
        <div>
          <h1 className="text-2xl font-black text-slate-800 flex items-center gap-2">
            <Layout className="size-6 text-blue-600" /> Trình Tạo Đề Thi Thông Minh
          </h1>
          <p className="text-slate-500 font-medium font-sans">
            {mode === 'ai' ? 'Sử dụng AI để soạn thảo câu hỏi nhanh chóng.' : 'Tự tay thiết kế bộ câu hỏi cho đề thi của bạn.'}
          </p>
        </div>
        <button 
          onClick={() => setMode(mode === 'ai' ? 'manual' : 'ai')}
          className="flex items-center justify-center gap-2 bg-slate-900 text-white px-4 py-2 rounded-xl text-sm font-bold hover:bg-slate-800 transition-all shadow-lg"
        >
          {mode === 'ai' ? (
            <><PlusCircle className="size-4" /> Tạo đề thủ công</>
          ) : (
            <><Brain className="size-4" /> Quay lại Trợ lý AI</>
          )}
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Cấu hình bên trái */}
        <div className="lg:col-span-4 space-y-6">
          <div className="bg-white rounded-[2rem] border-2 border-slate-100 shadow-xl shadow-slate-200/40 p-6 space-y-6 overflow-hidden">
            {mode === 'ai' ? (
              <>
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
              </>
            ) : (
              <>
                <div className="flex items-center gap-2 mb-2">
                  <div className="p-2 bg-emerald-50 rounded-lg text-emerald-600">
                    <PlusCircle className="size-5" />
                  </div>
                  <h2 className="font-black text-slate-800 uppercase tracking-wider text-sm">Soạn câu hỏi</h2>
                </div>

                <div className="space-y-2">
                  <label className="text-xs font-black text-slate-500 uppercase ml-1">Nội dung câu hỏi</label>
                  <textarea
                    rows={3}
                    value={currentQuestion.text}
                    onChange={(e) => setCurrentQuestion({...currentQuestion, text: e.target.value})}
                    className="w-full bg-slate-50 border-none rounded-2xl px-4 py-3 text-sm font-medium text-slate-700 outline-none resize-none"
                    placeholder="Nhập câu hỏi tại đây..."
                  />
                </div>

                <div className="space-y-3">
                  <label className="text-xs font-black text-slate-500 uppercase ml-1">Các phương án trả lời</label>
                  {currentQuestion.options.map((opt, idx) => (
                    <div key={idx} className="flex items-center gap-2">
                      <button 
                        onClick={() => setCurrentQuestion({...currentQuestion, correctAnswer: idx})}
                        className={`size-8 rounded-full flex-shrink-0 font-bold text-xs transition-all ${currentQuestion.correctAnswer === idx ? 'bg-emerald-500 text-white' : 'bg-slate-200 text-slate-500'}`}
                      >
                        {alphabet[idx]}
                      </button>
                      <input
                        type="text"
                        value={opt}
                        onChange={(e) => handleOptionChange(idx, e.target.value)}
                        className="flex-1 bg-slate-50 border-none rounded-xl px-3 py-2 text-sm font-medium outline-none"
                        placeholder={`Phương án ${alphabet[idx]}`}
                      />
                    </div>
                  ))}
                </div>

                <button
                  onClick={handleAddManualQuestion}
                  className="w-full bg-slate-900 text-white py-4 rounded-[1.25rem] font-black text-sm uppercase tracking-widest flex items-center justify-center gap-3 shadow-lg active:scale-95 transition-all"
                >
                  <Plus className="size-5" /> Thêm câu hỏi
                </button>
              </>
            )}
          </div>
        </div>

        {/* Preview bên phải */}
        <div className="lg:col-span-8">
          <div className="bg-white h-full min-h-[500px] rounded-[2.5rem] border-2 border-slate-50 shadow-2xl flex flex-col overflow-hidden">
            <div className="px-8 py-5 border-b bg-slate-50/30 flex items-center justify-between">
              <span className="text-xs font-black text-slate-500 uppercase tracking-widest">
                {mode === 'ai' ? 'Kết quả soạn thảo từ AI' : `Danh sách câu hỏi thủ công (${manualQuestions.length})`}
              </span>
              {mode === 'ai' && aiResult && (
                <button onClick={handleCopy} className="text-xs font-bold text-blue-600 flex items-center gap-2 hover:bg-blue-50 px-3 py-1 rounded-lg">
                  {copied ? <CheckCircle2 className="size-4" /> : <Copy className="size-4" />} Sao chép
                </button>
              )}
              {mode === 'manual' && manualQuestions.length > 0 && (
                <button className="text-xs font-bold bg-blue-600 text-white flex items-center gap-2 hover:bg-blue-700 px-4 py-2 rounded-xl shadow-md transition-all">
                  <Save className="size-4" /> Lưu đề thi
                </button>
              )}
            </div>
            <div className="flex-1 p-8 overflow-y-auto bg-slate-50/10">
              {mode === 'ai' ? (
                generating ? (
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
                    <p className="font-bold uppercase tracking-widest text-sm">Chưa có nội dung AI</p>
                  </div>
                )
              ) : (
                manualQuestions.length > 0 ? (
                  <div className="space-y-6">
                    {manualQuestions.map((q, qIdx) => (
                      <div key={q.id} className="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm relative group">
                        <button 
                          onClick={() => removeQuestion(q.id)}
                          className="absolute top-4 right-4 p-2 text-slate-300 hover:text-red-500 opacity-0 group-hover:opacity-100 transition-all"
                        >
                          <Trash2 className="size-4" />
                        </button>
                        <h4 className="font-bold text-slate-800 mb-4 pr-8">
                          <span className="text-blue-600 mr-2">Câu {qIdx + 1}:</span> {q.text}
                        </h4>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                          {q.options.map((opt, oIdx) => (
                            <div key={oIdx} className={`p-3 rounded-xl text-sm font-medium border ${q.correctAnswer === oIdx ? 'bg-emerald-50 border-emerald-200 text-emerald-700' : 'bg-slate-50 border-slate-100 text-slate-600'}`}>
                              <span className="font-bold mr-2">{alphabet[oIdx]}.</span> {opt}
                            </div>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="h-full flex flex-col items-center justify-center text-slate-400 space-y-4 border-4 border-dashed border-slate-50 rounded-[2rem]">
                    <PlusCircle className="size-12 opacity-20" />
                    <p className="font-bold uppercase tracking-widest text-sm">Bắt đầu thêm câu hỏi thủ công</p>
                  </div>
                )
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ExamGenerator;