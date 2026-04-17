import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  FileText, 
  Plus, 
  Search, 
  Filter, 
  Clock, 
  Target, 
  GraduationCap, 
  Calendar,
  MoreVertical,
  Edit,
  Trash2,
  CheckCircle2,
  Inbox,
  ChevronRight
} from 'lucide-react';
import { apiClient } from '../../features/auth/authApi';
import { toast } from 'sonner';

const ExamsPage = () => {
  const [exams, setExams] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({
    subject: '',
    status: ''
  });

  useEffect(() => {
    fetchExams();
  }, [filters]);

  const fetchExams = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get('/exams', { params: filters });
      setExams(response.data);
    } catch (err) {
      toast.error('Không thể tải danh sách đề thi');
    } finally {
      setLoading(false);
    }
  };

  const getStatusStyle = (status) => {
    switch (status) {
      case 'PUBLISHED':
        return 'bg-emerald-50 text-emerald-600 border-emerald-100';
      case 'ARCHIVED':
        return 'bg-slate-100 text-slate-500 border-slate-200';
      default:
        return 'bg-indigo-50 text-indigo-600 border-indigo-100';
    }
  };

  return (
    <div className="space-y-8 animate-in fade-in duration-500 pb-10">
      {/* Header Section */}
      <div className="flex flex-col md:flex-row md:items-center justify-between bg-white p-8 rounded-[2rem] shadow-sm border border-slate-100 gap-6">
        <div>
          <h1 className="text-3xl font-black tracking-tight text-slate-900">
            Quản lý <span className="text-indigo-600">Đề thi</span>
          </h1>
          <p className="text-sm text-slate-500 font-medium mt-1">
            Tạo, quản lý và xuất bản các bộ đề thi chuẩn hóa cho học sinh.
          </p>
        </div>
        <Link
          to="/exams/new"
          className="inline-flex items-center justify-center gap-2 rounded-2xl bg-slate-900 px-8 py-4 text-xs font-black uppercase tracking-widest text-white shadow-lg hover:bg-indigo-600 hover:shadow-indigo-200 transition-all active:scale-95"
        >
          <Plus className="size-5" />
          Tạo đề thi mới
        </Link>
      </div>

      {/* Filter Bar */}
      <div className="bg-white p-6 rounded-[2rem] shadow-sm border border-slate-100 flex flex-wrap gap-4 items-center">
        <div className="flex-1 min-w-[200px] relative">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 size-4 text-slate-400" />
          <input 
            type="text" 
            placeholder="Tìm kiếm tên đề thi..." 
            className="w-full pl-11 pr-4 py-3 rounded-xl bg-slate-50 border-none text-sm font-bold text-slate-700 focus:ring-2 focus:ring-indigo-500/20 outline-none transition-all"
          />
        </div>
        
        <div className="flex items-center gap-3">
          <Filter className="size-4 text-slate-400" />
          <select 
            value={filters.subject}
            onChange={(e) => setFilters({...filters, subject: e.target.value})}
            className="bg-slate-50 border-none rounded-xl px-4 py-3 text-sm font-bold text-slate-600 outline-none focus:ring-2 focus:ring-indigo-500/20"
          >
            <option value="">Tất cả môn học</option>
            <option value="Math">Toán học</option>
            <option value="Physics">Vật lý</option>
            <option value="Chemistry">Hóa học</option>
            <option value="Biology">Sinh học</option>
          </select>

          <select 
            value={filters.status}
            onChange={(e) => setFilters({...filters, status: e.target.value})}
            className="bg-slate-50 border-none rounded-xl px-4 py-3 text-sm font-bold text-slate-600 outline-none focus:ring-2 focus:ring-indigo-500/20"
          >
            <option value="">Mọi trạng thái</option>
            <option value="DRAFT">Bản nháp</option>
            <option value="PUBLISHED">Đã xuất bản</option>
            <option value="ARCHIVED">Lưu trữ</option>
          </select>
        </div>
      </div>

      {/* Exams Grid */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {[1, 2, 3].map(i => (
            <div key={i} className="h-64 bg-slate-100 animate-pulse rounded-[2rem]" />
          ))}
        </div>
      ) : exams.length === 0 ? (
        <div className="py-32 text-center bg-white rounded-[3rem] border-2 border-dashed border-slate-100 shadow-sm">
          <div className="bg-slate-50 size-24 rounded-full flex items-center justify-center mx-auto mb-6">
            <Inbox className="size-12 text-slate-200" />
          </div>
          <p className="text-slate-600 font-black text-xl">Chưa có đề thi nào</p>
          <p className="text-sm text-slate-400 mt-2">Hãy bắt đầu bằng cách tạo đề thi đầu tiên của bạn.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {exams.map((exam) => (
            <div 
              key={exam.id} 
              className="group bg-white rounded-[2rem] p-8 shadow-xl shadow-slate-200/50 border border-transparent hover:border-indigo-100 hover:shadow-indigo-100 transition-all duration-300 relative overflow-hidden"
            >
              {/* Status Badge */}
              <div className="flex justify-between items-start mb-6">
                <span className={`px-3 py-1 rounded-lg text-[10px] font-black uppercase tracking-widest border ${getStatusStyle(exam.status)}`}>
                  {exam.status}
                </span>
                <button className="p-2 text-slate-300 hover:text-slate-600 transition-colors">
                  <MoreVertical className="size-5" />
                </button>
              </div>

              {/* Title & Subject */}
              <div className="mb-6">
                <h3 className="text-xl font-black text-slate-800 line-clamp-2 min-h-[3.5rem] mb-2 group-hover:text-indigo-600 transition-colors">
                  {exam.title}
                </h3>
                <div className="flex items-center gap-2 text-indigo-500 font-bold text-xs uppercase tracking-tighter">
                  <span className="bg-indigo-50 px-2 py-0.5 rounded">#{exam.subject}</span>
                  <span className="bg-slate-50 text-slate-500 px-2 py-0.5 rounded">Lớp {exam.gradeLevel}</span>
                </div>
              </div>

              {/* Meta Info */}
              <div className="grid grid-cols-2 gap-4 mb-8">
                <div className="flex items-center gap-2 text-slate-500">
                  <div className="p-2 bg-slate-50 rounded-xl">
                    <Clock className="size-4" />
                  </div>
                  <span className="text-xs font-bold">{exam.durationMinutes} phút</span>
                </div>
                <div className="flex items-center gap-2 text-slate-500">
                  <div className="p-2 bg-slate-50 rounded-xl">
                    <Target className="size-4" />
                  </div>
                  <span className="text-xs font-bold">{exam.totalPoints} điểm</span>
                </div>
              </div>

              {/* Footer Actions */}
              <div className="flex items-center gap-2 pt-6 border-t border-slate-50">
                <Link 
                  to={`/exams/${exam.id}/edit`}
                  className="flex-1 inline-flex items-center justify-center gap-2 bg-slate-50 text-slate-600 py-3 rounded-xl text-xs font-black uppercase tracking-widest hover:bg-slate-100 transition-all"
                >
                  <Edit className="size-4" />
                  Sửa đề
                </Link>
                <Link 
                  to={`/exams/${exam.id}`}
                  className="size-12 inline-flex items-center justify-center bg-indigo-50 text-indigo-600 rounded-xl hover:bg-indigo-600 hover:text-white transition-all shadow-sm"
                >
                  <ChevronRight className="size-5" />
                </Link>
              </div>

              {/* Decorative Element */}
              <div className="absolute -right-4 -bottom-4 opacity-[0.03] group-hover:opacity-[0.08] transition-opacity pointer-events-none">
                <FileText className="size-32 rotate-12" />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ExamsPage;