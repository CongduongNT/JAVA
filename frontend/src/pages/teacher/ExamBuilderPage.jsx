import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { 
  ArrowLeft, 
  Save, 
  Plus, 
  Trash2, 
  GripVertical, 
  Search, 
  Database, 
  ChevronRight,
  Loader2,
  CheckCircle2,
  Target,
  Clock,
  BookOpen,
  Shuffle,
  Settings2,
  Layers,
  Eye,
  X
} from 'lucide-react';
import { apiClient } from '../../features/auth/authApi';
import { toast } from 'sonner';

const ExamBuilderPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = id !== 'new';

  const [exam, setExam] = useState({
    title: '',
    subject: '',
    gradeLevel: '',
    durationMinutes: 45,
    totalPoints: 10,
    status: 'DRAFT',
    settings: null
  });

  const [settings, setSettings] = useState({
    shuffleQuestions: false,
    versionCount: 1,
    targetQuestionCount: 10,
    showSolutions: true
  });

  const [selectedQuestions, setSelectedQuestions] = useState([]);
  const [banks, setBanks] = useState([]);
  const [selectedBankId, setSelectedBankId] = useState(null);
  const [bankQuestions, setBankQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [draggedItemIndex, setDraggedItemIndex] = useState(null);
  const [showPreview, setShowPreview] = useState(false);

  useEffect(() => {
    const init = async () => {
      try {
        setLoading(true);
        // 1. Lấy thông tin đề thi nếu là mode edit
        if (isEdit) {
          const examRes = await apiClient.get(`/exams/${id}`);
          const examData = examRes.data;
          setExam(examData);

          if (examData.settings) {
            try {
              const parsedSettings = typeof examData.settings === 'string' 
                ? JSON.parse(examData.settings) 
                : examData.settings;
              setSettings(prev => ({ ...prev, ...parsedSettings }));
            } catch (e) {
              console.error("Failed to parse settings", e);
            }
          }
          // 2. Lấy danh sách câu hỏi hiện tại trong đề
          const questionsRes = await apiClient.get(`/exams/${id}/questions`);
          setSelectedQuestions(questionsRes.data.map(q => ({
            ...q.question,
            points: q.points,
            orderIndex: q.orderIndex
          })));
        }
        // 3. Lấy danh sách ngân hàng câu hỏi
        const banksRes = await apiClient.get('/question-bank');
        setBanks(banksRes.data);
      } catch (err) {
        toast.error('Lỗi khi tải dữ liệu đề thi');
      } finally {
        setLoading(false);
      }
    };
    init();
  }, [id, isEdit]);

  // Load câu hỏi khi chọn bank
  useEffect(() => {
    if (selectedBankId) {
      apiClient.get(`/question-bank/${selectedBankId}/questions`)
        .then(res => setBankQuestions(res.data))
        .catch(() => toast.error('Không thể tải câu hỏi từ ngân hàng này'));
    }
  }, [selectedBankId]);

  const handleAddQuestion = (q) => {
    if (selectedQuestions.some(sq => sq.id === q.id)) {
      return toast.warning('Câu hỏi này đã có trong đề');
    }
    const newQuestion = {
      ...q,
      points: 1.0,
      orderIndex: selectedQuestions.length + 1
    };
    setSelectedQuestions([...selectedQuestions, newQuestion]);
    toast.success('Đã thêm vào đề');
  };

  const handleRemoveQuestion = (qid) => {
    setSelectedQuestions(selectedQuestions.filter(q => q.id !== qid));
  };

  const handleShuffle = () => {
    if (selectedQuestions.length < 2) return;
    const shuffled = [...selectedQuestions].sort(() => Math.random() - 0.5);
    setSelectedQuestions(shuffled);
    toast.info('Đã xáo trộn thứ tự câu hỏi');
  };

  const handleDragStart = (e, index) => {
    setDraggedItemIndex(index);
    e.dataTransfer.effectAllowed = 'move';
  };

  const handleDragOver = (e, index) => {
    e.preventDefault();
    if (draggedItemIndex === null || draggedItemIndex === index) return;

    const newQuestions = [...selectedQuestions];
    const itemToMove = newQuestions.splice(draggedItemIndex, 1)[0];
    newQuestions.splice(index, 0, itemToMove);
    
    setDraggedItemIndex(index);
    setSelectedQuestions(newQuestions);
  };

  const handleDragEnd = () => {
    setDraggedItemIndex(null);
  };

  const handleSave = async () => {
    if (!exam.title) return toast.error('Vui lòng nhập tiêu đề đề thi');
    setSaving(true);
    try {
      const payload = {
        ...exam,
        settings: JSON.stringify(settings),
        questions: selectedQuestions.map((q, idx) => ({
          questionId: q.id,
          orderIndex: idx + 1,
          points: q.points || 1.0
        }))
      };

      if (isEdit) {
        await apiClient.put(`/exams/${id}`, payload);
      } else {
        await apiClient.post('/exams', payload);
      }
      toast.success('Lưu đề thi thành công!');
      navigate('/exams');
    } catch (err) {
      toast.error('Lỗi khi lưu đề thi');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="flex h-screen items-center justify-center"><Loader2 className="animate-spin text-indigo-600 size-10" /></div>;

  // Component hiển thị Preview đề thi
  const ExamPreview = () => (
    <div className="fixed inset-0 z-[100] bg-slate-900/60 backdrop-blur-md flex justify-center overflow-y-auto p-4 md:p-10 animate-in fade-in duration-300">
      <div className="bg-white w-full max-w-4xl min-h-screen shadow-2xl rounded-[1rem] md:rounded-[2rem] flex flex-col animate-in zoom-in-95 duration-300">
        {/* Preview Toolbar */}
        <div className="sticky top-0 z-10 flex items-center justify-between p-6 border-b bg-white/80 backdrop-blur-md rounded-t-[2rem]">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-indigo-50 rounded-lg text-indigo-600">
              <Eye className="size-5" />
            </div>
            <h2 className="font-black text-slate-800 uppercase tracking-widest text-sm">Xem trước đề thi</h2>
          </div>
          <button 
            onClick={() => setShowPreview(false)}
            className="p-3 hover:bg-slate-100 rounded-full transition-colors text-slate-400 hover:text-slate-600"
          >
            <X className="size-6" />
          </button>
        </div>

        {/* Paper Content */}
        <div className="p-10 md:p-16 flex-1 font-serif text-slate-800">
          {/* Exam Header */}
          <div className="flex justify-between items-start mb-12 border-b-2 border-slate-900 pb-8">
            <div className="text-center space-y-1">
              <p className="font-bold text-sm uppercase">Sở Giáo dục và Đào tạo</p>
              <p className="font-bold text-sm uppercase">Trường THPT Planbook AI</p>
              <div className="w-20 h-0.5 bg-slate-900 mx-auto mt-2" />
            </div>
            <div className="text-center space-y-2">
              <h3 className="font-black text-xl uppercase tracking-tighter">Đề thi: {exam.title || 'Chưa đặt tên'}</h3>
              <p className="text-sm font-bold italic">Môn: {exam.subject} - Lớp: {exam.gradeLevel}</p>
              <p className="text-sm">Thời gian làm bài: <span className="font-bold">{exam.durationMinutes} phút</span></p>
              {settings.versionCount > 1 && (
                <p className="text-xs bg-slate-100 px-2 py-1 rounded inline-block font-bold">Mã đề: 101</p>
              )}
            </div>
          </div>

          {/* Student Info Block */}
          <div className="grid grid-cols-2 gap-10 mb-10 text-sm italic">
            <div className="border-b border-dashed border-slate-400 pb-1">Họ và tên thí sinh: ................................................................</div>
            <div className="border-b border-dashed border-slate-400 pb-1">Số báo danh: ..........................................</div>
          </div>

          {/* Questions List */}
          <div className="space-y-10">
            {selectedQuestions.length === 0 ? (
              <p className="text-center text-slate-400 italic py-20">Đề thi chưa có câu hỏi nào để hiển thị.</p>
            ) : (
              selectedQuestions.map((q, idx) => (
                <div key={idx} className="space-y-4">
                  <div className="flex gap-2">
                    <span className="font-black">Câu {idx + 1} ({q.points}đ):</span>
                    <div className="flex-1 leading-relaxed whitespace-pre-wrap">{q.content}</div>
                  </div>

                  {/* Options for Multiple Choice */}
                  {q.type === 'MULTIPLE_CHOICE' && q.options && (
                    <div className="grid grid-cols-2 gap-x-8 gap-y-3 pl-8">
                      {q.options.map((opt, oIdx) => (
                        <div key={oIdx} className="flex gap-2 items-start">
                          <span className="font-bold">{opt.label}.</span>
                          <span className={`${settings.showSolutions && opt.isCorrect ? 'text-indigo-600 font-bold underline decoration-indigo-200' : ''}`}>
                            {opt.text}
                          </span>
                        </div>
                      ))}
                    </div>
                  )}

                  {/* Space for Short Answer */}
                  {q.type === 'SHORT_ANSWER' && (
                    <div className="pl-8 space-y-2">
                      <div className="h-20 w-full border border-dashed border-slate-200 rounded-lg" />
                      {settings.showSolutions && q.correctAnswer && (
                        <p className="text-xs text-indigo-500 font-bold italic">Đáp án: {q.correctAnswer}</p>
                      )}
                    </div>
                  )}
                </div>
              ))
            )}
          </div>

          {/* Footer */}
          <div className="mt-20 text-center border-t border-slate-100 pt-8">
            <p className="text-sm font-bold uppercase tracking-widest">--- Hết ---</p>
            <p className="text-[10px] text-slate-400 mt-2 uppercase tracking-tight">(Cán bộ coi thi không giải thích gì thêm)</p>
          </div>
        </div>

        {/* Bottom Actions */}
        <div className="p-6 border-t bg-slate-50 flex justify-center rounded-b-[2rem]">
          <button 
            onClick={() => setShowPreview(false)}
            className="bg-slate-900 text-white px-10 py-4 rounded-2xl text-xs font-black uppercase tracking-widest hover:bg-indigo-600 transition-all shadow-xl active:scale-95"
          >
            Quay lại chỉnh sửa
          </button>
        </div>
      </div>
    </div>
  );

  return (
    <div className="flex flex-col h-[calc(100vh-100px)] animate-in fade-in duration-500">
      {showPreview && <ExamPreview />}
      {/* Top Header */}
      <div className="flex items-center justify-between bg-white p-6 rounded-[2rem] shadow-sm border border-slate-100 mb-6">
        <div className="flex items-center gap-4">
          <button onClick={() => navigate('/exams')} className="p-3 hover:bg-slate-50 rounded-2xl transition-colors">
            <ArrowLeft className="size-5 text-slate-500" />
          </button>
          <div>
            <h1 className="text-xl font-black text-slate-900">{isEdit ? 'Thiết kế đề thi' : 'Tạo đề thi mới'}</h1>
            <p className="text-xs text-slate-400 font-bold uppercase tracking-widest mt-0.5">Editor Mode</p>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <button 
            onClick={() => setShowPreview(true)}
            className="flex items-center gap-2 bg-white border-2 border-slate-100 text-slate-600 px-6 py-3 rounded-2xl text-xs font-black uppercase tracking-widest hover:bg-slate-50 transition-all active:scale-95"
          >
            <Eye className="size-4" />
            Xem trước
          </button>
          <button 
            onClick={handleSave}
            disabled={saving}
            className="flex items-center gap-2 bg-indigo-600 text-white px-8 py-3 rounded-2xl text-xs font-black uppercase tracking-widest hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-100 disabled:opacity-50"
          >
            {saving ? <Loader2 className="size-4 animate-spin" /> : <Save className="size-4" />}
            Lưu đề thi
          </button>
        </div>
      </div>

      <div className="flex-1 grid grid-cols-12 gap-6 overflow-hidden">
        {/* Left: Exam Structure */}
        <div className="col-span-12 lg:col-span-7 flex flex-col gap-6 overflow-y-auto pr-2 custom-scrollbar">
          <div className="bg-white p-8 rounded-[2.5rem] shadow-xl shadow-slate-200/50 border-none space-y-6">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="text-[10px] font-black uppercase tracking-widest text-slate-400 ml-1">Tiêu đề đề thi</label>
                <input 
                  type="text" 
                  value={exam.title}
                  onChange={(e) => setExam({...exam, title: e.target.value})}
                  className="w-full bg-slate-50 border-none rounded-2xl px-5 py-4 text-sm font-bold text-slate-700 focus:ring-2 focus:ring-indigo-500/20 transition-all"
                  placeholder="Nhập tiêu đề..."
                />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-2">
                  <label className="text-[10px] font-black uppercase tracking-widest text-slate-400 ml-1">Thời gian (phút)</label>
                  <input 
                    type="number" 
                    value={exam.durationMinutes}
                    onChange={(e) => setExam({...exam, durationMinutes: e.target.value})}
                    className="w-full bg-slate-50 border-none rounded-2xl px-5 py-4 text-sm font-bold text-slate-700 focus:ring-2 focus:ring-indigo-500/20"
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-[10px] font-black uppercase tracking-widest text-slate-400 ml-1">Tổng điểm</label>
                  <input 
                    type="number" 
                    value={exam.totalPoints}
                    onChange={(e) => setExam({...exam, totalPoints: e.target.value})}
                    className="w-full bg-slate-50 border-none rounded-2xl px-5 py-4 text-sm font-bold text-slate-700 focus:ring-2 focus:ring-indigo-500/20"
                  />
                </div>
              </div>
            </div>

            {/* Advanced Configuration */}
            <div className="pt-6 border-t border-slate-50 space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Settings2 className="size-4 text-indigo-500" />
                  <h3 className="text-[10px] font-black uppercase tracking-widest text-slate-800">Cấu hình đề thi</h3>
                </div>
                <button 
                  onClick={handleShuffle}
                  className="flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-indigo-600 hover:bg-indigo-50 px-3 py-1.5 rounded-xl transition-all"
                >
                  <Shuffle className="size-3" /> Trộn câu hỏi ngay
                </button>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="space-y-2">
                  <label className="text-[10px] font-black uppercase tracking-widest text-slate-400 ml-1 flex items-center gap-1.5">
                    <Layers className="size-3" /> Số phiên bản (Mã đề)
                  </label>
                  <input 
                    type="number" 
                    min="1"
                    max="10"
                    value={settings.versionCount}
                    onChange={(e) => setSettings({...settings, versionCount: parseInt(e.target.value) || 1})}
                    className="w-full bg-slate-50 border-none rounded-2xl px-5 py-4 text-sm font-bold text-slate-700 focus:ring-2 focus:ring-indigo-500/20 outline-none transition-all"
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-[10px] font-black uppercase tracking-widest text-slate-400 ml-1 flex items-center gap-1.5">
                    <Target className="size-3" /> Số câu mục tiêu
                  </label>
                  <input 
                    type="number" 
                    value={settings.targetQuestionCount}
                    onChange={(e) => setSettings({...settings, targetQuestionCount: parseInt(e.target.value) || 0})}
                    className="w-full bg-slate-50 border-none rounded-2xl px-5 py-4 text-sm font-bold text-slate-700 focus:ring-2 focus:ring-indigo-500/20 outline-none transition-all"
                  />
                </div>
                <div className="flex flex-col justify-center gap-2 px-1">
                  <label className="flex items-center gap-3 cursor-pointer group">
                    <input 
                      type="checkbox"
                      checked={settings.shuffleQuestions}
                      onChange={(e) => setSettings({...settings, shuffleQuestions: e.target.checked})}
                      className="size-5 rounded-lg border-2 border-slate-200 text-indigo-600 focus:ring-indigo-500/20 transition-all cursor-pointer"
                    />
                    <span className="text-[10px] font-black uppercase tracking-widest text-slate-500 group-hover:text-indigo-600 transition-colors">Tự động trộn</span>
                  </label>
                  <label className="flex items-center gap-3 cursor-pointer group">
                    <input 
                      type="checkbox"
                      checked={settings.showSolutions}
                      onChange={(e) => setSettings({...settings, showSolutions: e.target.checked})}
                      className="size-5 rounded-lg border-2 border-slate-200 text-indigo-600 focus:ring-indigo-500/20 transition-all cursor-pointer"
                    />
                    <span className="text-[10px] font-black uppercase tracking-widest text-slate-500 group-hover:text-indigo-600 transition-colors">Hiện đáp án</span>
                  </label>
                </div>
              </div>
            </div>
          </div>

          <div className="space-y-4">
            <div className="flex items-center justify-between px-4">
              <h3 className="text-sm font-black text-slate-800 uppercase tracking-widest flex items-center gap-2">
                <BookOpen className="size-4 text-indigo-500" />
                Câu hỏi trong đề ({selectedQuestions.length})
              </h3>
            </div>
            
            {selectedQuestions.length === 0 ? (
              <div className="bg-white border-2 border-dashed border-slate-200 rounded-[2rem] p-12 text-center">
                <Plus className="size-10 text-slate-200 mx-auto mb-3" />
                <p className="text-sm text-slate-400 font-bold uppercase">Chưa có câu hỏi nào. Hãy chọn từ bên phải.</p>
              </div>
            ) : (
              selectedQuestions.map((q, index) => (
                <div 
                  key={q.id} 
                  draggable
                  onDragStart={(e) => handleDragStart(e, index)}
                  onDragOver={(e) => handleDragOver(e, index)}
                  onDragEnd={handleDragEnd}
                  className={`bg-white p-6 rounded-[2rem] shadow-sm border border-slate-50 flex gap-4 group hover:border-indigo-100 transition-all cursor-default ${draggedItemIndex === index ? 'opacity-40 border-indigo-500 border-dashed scale-[0.98]' : ''}`}
                >
                  <div className="flex flex-col items-center gap-2 pt-1">
                    <span className="bg-slate-900 text-white size-8 rounded-full flex items-center justify-center text-xs font-black">
                      {index + 1}
                    </span>
                    <GripVertical className="size-4 text-slate-300 cursor-move" />
                  </div>
                  <div className="flex-1 space-y-3">
                    <p className="text-sm font-bold text-slate-800 leading-relaxed">{q.content}</p>
                    <div className="flex items-center gap-4">
                      <div className="flex items-center gap-2 bg-slate-50 px-3 py-1.5 rounded-xl border border-slate-100">
                        <span className="text-[10px] font-black text-slate-400 uppercase">Điểm:</span>
                        <input 
                          type="number" 
                          step="0.5"
                          value={q.points}
                          onChange={(e) => {
                            const newQuestions = [...selectedQuestions];
                            newQuestions[index].points = e.target.value;
                            setSelectedQuestions(newQuestions);
                          }}
                          className="w-12 bg-transparent text-xs font-black text-indigo-600 outline-none text-center"
                        />
                      </div>
                      <span className="px-2.5 py-1 bg-slate-50 text-slate-500 rounded-lg text-[10px] font-bold uppercase">
                        {q.type}
                      </span>
                    </div>
                  </div>
                  <button 
                    onClick={() => handleRemoveQuestion(q.id)}
                    className="p-2 text-slate-300 hover:text-red-500 hover:bg-red-50 rounded-xl transition-all h-fit"
                  >
                    <Trash2 className="size-4" />
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Right: Bank Browser */}
        <div className="col-span-12 lg:col-span-5 bg-white rounded-[2.5rem] shadow-xl shadow-slate-200/50 border border-slate-50 flex flex-col overflow-hidden">
          <div className="p-6 border-b border-slate-50 space-y-4 bg-slate-50/30">
            <h3 className="text-sm font-black text-slate-800 uppercase tracking-widest flex items-center gap-2">
              <Database className="size-4 text-indigo-500" /> Ngân hàng câu hỏi
            </h3>
            <select 
              onChange={(e) => setSelectedBankId(e.target.value)}
              className="w-full bg-white border-none rounded-2xl px-4 py-3 text-sm font-bold text-slate-600 shadow-sm focus:ring-2 focus:ring-indigo-500/20 outline-none"
            >
              <option value="">-- Chọn ngân hàng câu hỏi --</option>
              {banks.map(b => (
                <option key={b.id} value={b.id}>{b.name} ({b.subject})</option>
              ))}
            </select>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-slate-400" />
              <input 
                type="text" 
                placeholder="Tìm nhanh câu hỏi..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full bg-white border-none rounded-2xl pl-10 pr-4 py-3 text-sm font-bold text-slate-600 shadow-sm focus:ring-2 focus:ring-indigo-500/20 outline-none"
              />
            </div>
          </div>

          <div className="flex-1 overflow-y-auto p-6 space-y-4 custom-scrollbar">
            {!selectedBankId ? (
              <div className="h-full flex flex-col items-center justify-center text-slate-400 opacity-50">
                <Database className="size-12 mb-2" />
                <p className="text-xs font-black uppercase">Vui lòng chọn ngân hàng</p>
              </div>
            ) : (
              bankQuestions
                .filter(q => q.content.toLowerCase().includes(searchQuery.toLowerCase()))
                .map(q => (
                  <div key={q.id} className="group p-4 bg-slate-50 rounded-2xl border border-transparent hover:border-indigo-100 hover:bg-white hover:shadow-lg transition-all">
                    <p className="text-sm font-medium text-slate-700 line-clamp-3 mb-3">{q.content}</p>
                    <div className="flex items-center justify-between">
                      <span className="px-2 py-0.5 bg-indigo-50 text-indigo-600 rounded text-[10px] font-black uppercase">
                        {q.difficulty}
                      </span>
                      <button 
                        onClick={() => handleAddQuestion(q)}
                        className="flex items-center gap-1.5 text-[10px] font-black uppercase tracking-widest text-indigo-600 hover:text-white hover:bg-indigo-600 px-3 py-2 rounded-xl transition-all"
                      >
                        <Plus className="size-3" /> Thêm vào đề
                      </button>
                    </div>
                  </div>
                ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ExamBuilderPage;