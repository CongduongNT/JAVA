import React, { useState, useEffect } from 'react';
import { PlusCircle, Search, Trash2, Brain, MoreHorizontal, Layout, BookOpen, AlertCircle, Loader2 } from 'lucide-react';
import questionApi from '../../services/questionApi';
import AIQuestionGenerator from './AIQuestionGenerator';
import { toast } from 'react-hot-toast'; // Giả định toast là common notification handler

/**
 * QuestionBankPage – Trang chính quản lý Ngân hàng câu hỏi.
 * Hỗ trợ các chức năng:
 * - Xem danh sách ngân hàng
 * - Tạo / Xóa ngân hàng câu hỏi
 * - Mở bộ tạo câu hỏi AI (Generator)
 */
const QuestionBankPage = () => {
  const [banks, setBanks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showGenerator, setShowGenerator] = useState(false);
  const [selectedBankId, setSelectedBankId] = useState(null);

  useEffect(() => {
    fetchBanks();
  }, []);

  const fetchBanks = async () => {
    try {
      setLoading(true);
      const res = await questionApi.getMyBanks();
      setBanks(res.data);
    } catch (err) {
      console.error("Failed to fetch banks:", err);
      toast.error("Không thể tải danh sách ngân hàng câu hỏi.");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateBank = async () => {
    const bankName = prompt("Nhập tên ngân hàng câu hỏi:");
    if (!bankName) return;

    try {
      await questionApi.createBank({ name: bankName, isPublished: false });
      toast.success("Đã tạo ngân hàng câu hỏi mới!");
      fetchBanks();
    } catch (err) {
      toast.error("Không thể tạo ngân hàng.");
    }
  };

  const handleDeleteBank = async (id, e) => {
    e.stopPropagation();
    if (!window.confirm("Bạn có chắc muốn xóa ngân hàng này? Tất cả câu hỏi bên trong sẽ bị mất!")) return;

    try {
      await questionApi.deleteBank(id);
      toast.success("Đã xóa ngân hàng!");
      fetchBanks();
    } catch (err) {
      toast.error("Không thể xóa ngân hàng.");
    }
  };

  if (showGenerator) {
    return (
      <AIQuestionGenerator 
        banks={banks} 
        initialBankId={selectedBankId}
        onClose={() => {
          setShowGenerator(false);
          fetchBanks();
        }} 
      />
    );
  }

  return (
    <div className="p-4 md:p-8 space-y-6">
      {/* Header Section */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-bold text-slate-900 leading-tight">Ngân hàng câu hỏi</h1>
          <p className="text-slate-500 mt-1">Quản lý và tổ chức các câu hỏi của bạn.</p>
        </div>
        
        <div className="flex gap-2">
          <button 
            onClick={handleCreateBank}
            className="flex items-center gap-2 px-4 py-2 bg-indigo-50 text-indigo-700 font-semibold rounded-lg hover:bg-indigo-100 transition-all active:scale-95"
          >
            <PlusCircle size={20} />
            Tạo ngân hàng
          </button>
          
          <button 
            onClick={() => {
              setSelectedBankId(null);
              setShowGenerator(true);
            }}
            className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white font-semibold rounded-lg hover:bg-indigo-700 shadow-md shadow-indigo-100 transition-all active:scale-95"
          >
            <Brain size={20} />
            <span className="hidden sm:inline">Sinh câu hỏi bằng AI</span>
            <span className="sm:hidden text-xs font-bold uppercase tracking-wider">Sinh AI</span>
          </button>
        </div>
      </div>

      {/* Main Grid View */}
      {loading ? (
        <div className="flex flex-col items-center justify-center py-20 space-y-4">
          <Loader2 className="animate-spin text-indigo-600" size={40} />
          <p className="text-slate-500 font-medium animate-pulse">Đang nạp ngân hàng câu hỏi...</p>
        </div>
      ) : banks.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {banks.map((bank) => (
            <div 
              key={bank.id}
              className="group relative bg-white border border-slate-200 rounded-xl overflow-hidden hover:border-indigo-300 hover:shadow-xl hover:shadow-indigo-50/50 transition-all duration-300 cursor-pointer p-5 flex flex-col justify-between"
              onClick={() => {
                setSelectedBankId(bank.id);
                setShowGenerator(true);
              }}
            >
              <div className="flex items-start justify-between mb-4">
                <div className="p-3 bg-slate-50 text-slate-400 group-hover:bg-indigo-50 group-hover:text-indigo-600 rounded-lg transition-colors duration-300">
                  <BookOpen size={24} />
                </div>
                <button 
                  onClick={(e) => handleDeleteBank(bank.id, e)}
                  className="opacity-0 group-hover:opacity-100 p-2 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-all"
                >
                  <Trash2 size={18} />
                </button>
              </div>

              <div>
                <h3 className="text-lg font-bold text-slate-800 group-hover:text-indigo-700 transition-colors truncate">
                  {bank.name}
                </h3>
                <div className="flex flex-wrap gap-2 mt-2">
                  <span className="text-[10px] font-bold uppercase tracking-wider bg-slate-100 text-slate-600 px-2.5 py-1 rounded-full border border-slate-200">
                    {bank.subject || "Chưa có môn"}
                  </span>
                  <span className="text-[10px] font-bold uppercase tracking-wider bg-slate-100 text-slate-600 px-2.5 py-1 rounded-full border border-slate-200">
                    Lớp {bank.gradeLevel || "?"}
                  </span>
                </div>
              </div>

              <div className="mt-6 pt-4 border-t border-slate-100 flex items-center justify-between">
                <div className="flex items-center gap-1.5 text-slate-500">
                  <Layout size={14} className="opacity-60" />
                  <span className="text-xs font-medium uppercase tracking-tight">Vào ngân hàng</span>
                </div>
                <button className="text-slate-300 group-hover:text-indigo-500 transition-colors">
                  <PlusCircle size={18} />
                </button>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="flex flex-col items-center justify-center py-20 px-4 bg-slate-50 border-2 border-dashed border-slate-200 rounded-2xl text-center">
          <div className="w-16 h-16 bg-slate-100 text-slate-300 rounded-full flex items-center justify-center mb-4">
            <Search size={32} />
          </div>
          <h3 className="text-xl font-bold text-slate-800">Chưa có ngân hàng câu hỏi</h3>
          <p className="text-slate-500 mt-2 max-w-sm mx-auto font-medium">Bạn chưa tạo ngân hàng câu hỏi nào. Hãy nhấn “Tạo ngân hàng” hoặc dùng AI để bắt đầu ngay!</p>
          <button 
            onClick={handleCreateBank}
            className="mt-6 flex items-center gap-2 px-6 py-2.5 bg-indigo-600 text-white font-bold rounded-xl hover:bg-indigo-700 shadow-lg shadow-indigo-100 transition-all hover:-translate-y-0.5"
          >
            <PlusCircle size={20} />
            Tạo ngân hàng đầu tiên
          </button>
        </div>
      )}
    </div>
  );
};

export default QuestionBankPage;
