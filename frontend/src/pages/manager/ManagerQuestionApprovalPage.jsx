import React, { useState, useEffect, useCallback } from 'react';
import {
  Loader2, CheckCircle2, XCircle, Clock, BookOpen,
  ChevronDown, ChevronUp, MessageSquare, Filter, RefreshCw,
  AlertCircle, User, Layers, Tag, BarChart2
} from 'lucide-react';
import questionApi from '../../services/questionApi';
import { toast } from 'sonner';

/**
 * ManagerQuestionApprovalPage – Màn hình Manager duyệt câu hỏi.
 *
 * Chức năng:
 * - Xem danh sách câu hỏi chờ duyệt / đã duyệt
 * - Duyệt hoặc từ chối câu hỏi (kèm ghi chú lý do)
 * - Filter theo trạng thái
 */
const ManagerQuestionApprovalPage = () => {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('PENDING'); // PENDING | APPROVED | ALL
  const [expandedId, setExpandedId] = useState(null);
  const [actionLoading, setActionLoading] = useState(null);

  // State cho modal từ chối + ghi chú
  const [rejectModal, setRejectModal] = useState({ open: false, questionId: null });
  const [rejectNote, setRejectNote] = useState('');

  const fetchQuestions = useCallback(async () => {
    setLoading(true);
    try {
      const approved = filter === 'PENDING' ? false : filter === 'APPROVED' ? true : undefined;
      const res = await questionApi.getQuestions(approved);
      setQuestions(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      console.error('Failed to fetch questions:', err);
      toast.error('Không thể tải danh sách câu hỏi.');
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    fetchQuestions();
  }, [fetchQuestions]);

  // Duyệt câu hỏi
  const handleApprove = async (id) => {
    setActionLoading(id);
    try {
      await questionApi.approveQuestion(id, true);
      toast.success('Đã duyệt câu hỏi thành công!');
      fetchQuestions();
    } catch (err) {
      toast.error('Không thể duyệt câu hỏi. Vui lòng thử lại.');
    } finally {
      setActionLoading(null);
    }
  };

  // Mở modal từ chối
  const openRejectModal = (id) => {
    setRejectModal({ open: true, questionId: id });
    setRejectNote('');
  };

  // Xác nhận từ chối (huỷ duyệt)
  const handleRejectConfirm = async () => {
    if (!rejectModal.questionId) return;
    setActionLoading(rejectModal.questionId);
    try {
      await questionApi.approveQuestion(rejectModal.questionId, false);
      toast.success(`Đã từ chối câu hỏi.${rejectNote ? ' Lý do: ' + rejectNote : ''}`);
      setRejectModal({ open: false, questionId: null });
      setRejectNote('');
      fetchQuestions();
    } catch (err) {
      toast.error('Không thể từ chối câu hỏi.');
    } finally {
      setActionLoading(null);
    }
  };

  const difficultyStyles = {
    EASY: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    MEDIUM: 'bg-amber-50 text-amber-700 border-amber-200',
    HARD: 'bg-rose-50 text-rose-700 border-rose-200',
  };
  const difficultyLabel = { EASY: 'Dễ', MEDIUM: 'Trung bình', HARD: 'Khó' };
  const typeLabel = {
    MULTIPLE_CHOICE: 'Trắc nghiệm',
    SHORT_ANSWER: 'Trả lời ngắn',
    FILL_IN_BLANK: 'Điền vào chỗ trống',
  };

  const statsTotal = questions.length;

  return (
    <div className="p-4 md:p-8 space-y-6 max-w-6xl mx-auto">
      {/* ── Header ── */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-bold text-slate-900 leading-tight">
            Duyệt Câu Hỏi
          </h1>
          <p className="text-slate-500 mt-1 text-sm">
            Xem xét và phê duyệt câu hỏi do Staff / Teacher tạo.
          </p>
        </div>
        <button
          onClick={fetchQuestions}
          className="flex items-center gap-2 px-4 py-2 bg-slate-100 text-slate-700 font-medium rounded-lg hover:bg-slate-200 transition-all text-sm self-start"
        >
          <RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
          Làm mới
        </button>
      </div>

      {/* ── Filter tabs ── */}
      <div className="flex gap-1 bg-slate-100 p-1 rounded-xl w-fit">
        {[
          { value: 'PENDING', label: 'Chờ duyệt', icon: Clock },
          { value: 'APPROVED', label: 'Đã duyệt', icon: CheckCircle2 },
          { value: 'ALL', label: 'Tất cả', icon: Filter },
        ].map(({ value, label, icon: Icon }) => (
          <button
            key={value}
            onClick={() => setFilter(value)}
            className={`flex items-center gap-1.5 px-4 py-2 rounded-lg text-sm font-medium transition-all ${
              filter === value
                ? 'bg-white text-indigo-700 shadow-sm'
                : 'text-slate-500 hover:text-slate-700'
            }`}
          >
            <Icon size={15} />
            {label}
          </button>
        ))}
      </div>

      {/* ── Stats bar ── */}
      {!loading && (
        <div className="flex items-center gap-2 text-sm text-slate-500">
          <BarChart2 size={15} />
          <span>
            Hiển thị <strong className="text-slate-800">{statsTotal}</strong> câu hỏi
          </span>
        </div>
      )}

      {/* ── Content ── */}
      {loading ? (
        <div className="flex flex-col items-center justify-center py-24 gap-3">
          <Loader2 className="animate-spin text-indigo-500" size={36} />
          <p className="text-slate-500 text-sm font-medium animate-pulse">Đang tải câu hỏi...</p>
        </div>
      ) : questions.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 bg-slate-50 border-2 border-dashed border-slate-200 rounded-2xl">
          <AlertCircle className="text-slate-300 mb-3" size={40} />
          <p className="text-slate-500 font-medium">
            {filter === 'PENDING' ? 'Không có câu hỏi nào đang chờ duyệt.' : 'Không có câu hỏi nào.'}
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {questions.map((q) => {
            const isExpanded = expandedId === q.id;
            const isProcessing = actionLoading === q.id;
            return (
              <div
                key={q.id}
                className="bg-white border border-slate-200 rounded-xl overflow-hidden hover:border-indigo-200 hover:shadow-sm transition-all"
              >
                {/* Card header */}
                <div className="p-5">
                  <div className="flex items-start justify-between gap-4">
                    {/* Left: content preview */}
                    <div className="flex-1 min-w-0">
                      {/* Meta badges */}
                      <div className="flex flex-wrap items-center gap-2 mb-2">
                        {/* Approval status badge */}
                        {q.isApproved ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                            <CheckCircle2 size={11} /> Đã duyệt
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200">
                            <Clock size={11} /> Chờ duyệt
                          </span>
                        )}
                        {/* Difficulty */}
                        <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold border ${difficultyStyles[q.difficulty] || 'bg-slate-100 text-slate-600'}`}>
                          {difficultyLabel[q.difficulty] || q.difficulty}
                        </span>
                        {/* Type */}
                        <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-indigo-50 text-indigo-600 border border-indigo-100">
                          {typeLabel[q.type] || q.type}
                        </span>
                        {/* AI badge */}
                        {q.aiGenerated && (
                          <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-purple-50 text-purple-600 border border-purple-100">
                            ✦ AI
                          </span>
                        )}
                      </div>

                      {/* Question content */}
                      <p className={`text-slate-800 font-medium leading-relaxed ${isExpanded ? '' : 'line-clamp-2'}`}>
                        {q.content}
                      </p>

                      {/* Meta info */}
                      <div className="flex flex-wrap gap-4 mt-3 text-xs text-slate-400">
                        <span className="flex items-center gap-1">
                          <User size={11} /> {q.createdByName || '—'}
                        </span>
                        <span className="flex items-center gap-1">
                          <Layers size={11} /> {q.bankName || '—'}
                        </span>
                        {q.topic && (
                          <span className="flex items-center gap-1">
                            <Tag size={11} /> {q.topic}
                          </span>
                        )}
                        {q.isApproved && q.approvedByName && (
                          <span className="flex items-center gap-1 text-emerald-500">
                            <CheckCircle2 size={11} /> Duyệt bởi: {q.approvedByName}
                          </span>
                        )}
                      </div>
                    </div>

                    {/* Right: action buttons */}
                    <div className="flex items-center gap-2 shrink-0">
                      {/* Expand toggle */}
                      <button
                        onClick={() => setExpandedId(isExpanded ? null : q.id)}
                        className="p-2 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-all"
                        title={isExpanded ? 'Thu gọn' : 'Xem chi tiết'}
                      >
                        {isExpanded ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
                      </button>

                      {/* Approve */}
                      {!q.isApproved && (
                        <button
                          onClick={() => handleApprove(q.id)}
                          disabled={isProcessing}
                          className="flex items-center gap-1.5 px-3 py-1.5 bg-emerald-500 text-white text-sm font-semibold rounded-lg hover:bg-emerald-600 disabled:opacity-50 transition-all"
                        >
                          {isProcessing ? (
                            <Loader2 size={14} className="animate-spin" />
                          ) : (
                            <CheckCircle2 size={14} />
                          )}
                          Duyệt
                        </button>
                      )}

                      {/* Reject / Unapprove */}
                      <button
                        onClick={() => openRejectModal(q.id)}
                        disabled={isProcessing}
                        className={`flex items-center gap-1.5 px-3 py-1.5 text-sm font-semibold rounded-lg disabled:opacity-50 transition-all ${
                          q.isApproved
                            ? 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                            : 'bg-rose-50 text-rose-600 hover:bg-rose-100'
                        }`}
                      >
                        <XCircle size={14} />
                        {q.isApproved ? 'Huỷ duyệt' : 'Từ chối'}
                      </button>
                    </div>
                  </div>

                  {/* Expanded: options + correct answer + explanation */}
                  {isExpanded && (
                    <div className="mt-4 pt-4 border-t border-slate-100 space-y-3">
                      {/* Options (MULTIPLE_CHOICE) */}
                      {q.type === 'MULTIPLE_CHOICE' && Array.isArray(q.options) && q.options.length > 0 && (
                        <div className="space-y-1.5">
                          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Đáp án</p>
                          {q.options.map((opt, idx) => (
                            <div
                              key={idx}
                              className={`flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm ${
                                opt.isCorrect || opt.is_correct
                                  ? 'bg-emerald-50 text-emerald-800 font-medium border border-emerald-200'
                                  : 'bg-slate-50 text-slate-700'
                              }`}
                            >
                              <span className="font-bold text-xs w-5 shrink-0">{opt.label || String.fromCharCode(65 + idx)}.</span>
                              <span>{opt.text}</span>
                              {(opt.isCorrect || opt.is_correct) && (
                                <CheckCircle2 size={13} className="text-emerald-600 ml-auto shrink-0" />
                              )}
                            </div>
                          ))}
                        </div>
                      )}

                      {/* Correct answer (SHORT_ANSWER / FILL_IN_BLANK) */}
                      {q.correctAnswer && (
                        <div className="px-3 py-2 bg-emerald-50 border border-emerald-200 rounded-lg text-sm">
                          <span className="font-semibold text-emerald-700">Đáp án: </span>
                          <span className="text-emerald-800">{q.correctAnswer}</span>
                        </div>
                      )}

                      {/* Explanation */}
                      {q.explanation && (
                        <div className="px-3 py-2 bg-indigo-50 border border-indigo-100 rounded-lg text-sm">
                          <span className="font-semibold text-indigo-700">Giải thích: </span>
                          <span className="text-indigo-800">{q.explanation}</span>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* ── Reject Modal ── */}
      {rejectModal.open && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          {/* Overlay */}
          <div
            className="absolute inset-0 bg-black/40 backdrop-blur-sm"
            onClick={() => setRejectModal({ open: false, questionId: null })}
          />

          {/* Modal card */}
          <div className="relative z-10 bg-white rounded-2xl shadow-2xl w-full max-w-md p-6 animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 rounded-full bg-rose-100 flex items-center justify-center shrink-0">
                <XCircle className="text-rose-600" size={20} />
              </div>
              <div>
                <h3 className="text-lg font-bold text-slate-900">Từ chối câu hỏi</h3>
                <p className="text-sm text-slate-500">Câu hỏi sẽ được đánh dấu chưa được duyệt.</p>
              </div>
            </div>

            <div className="space-y-3">
              <label className="block">
                <span className="text-sm font-medium text-slate-700 flex items-center gap-1.5 mb-1.5">
                  <MessageSquare size={14} />
                  Lý do từ chối <span className="text-slate-400">(tuỳ chọn)</span>
                </span>
                <textarea
                  value={rejectNote}
                  onChange={(e) => setRejectNote(e.target.value)}
                  placeholder="Ví dụ: Nội dung câu hỏi chưa chính xác, thiếu đáp án..."
                  rows={3}
                  className="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-sm text-slate-700 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-rose-300 focus:border-rose-400 resize-none transition"
                />
              </label>
            </div>

            <div className="flex gap-3 mt-5">
              <button
                onClick={() => setRejectModal({ open: false, questionId: null })}
                className="flex-1 px-4 py-2.5 border border-slate-200 text-slate-600 font-medium rounded-xl hover:bg-slate-50 transition-all text-sm"
              >
                Huỷ bỏ
              </button>
              <button
                onClick={handleRejectConfirm}
                disabled={actionLoading !== null}
                className="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 bg-rose-500 text-white font-semibold rounded-xl hover:bg-rose-600 disabled:opacity-60 transition-all text-sm"
              >
                {actionLoading !== null ? (
                  <Loader2 size={15} className="animate-spin" />
                ) : (
                  <XCircle size={15} />
                )}
                Xác nhận từ chối
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ManagerQuestionApprovalPage;
