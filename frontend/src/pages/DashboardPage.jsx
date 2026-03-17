import React from 'react';
import { useSelector } from 'react-redux';
import { Sparkles, Calendar, BookOpen, Clock, Users, ArrowUpRight, Zap, Target, TrendingUp } from 'lucide-react';

const DashboardPage = () => {
  const { user } = useSelector((state) => state.auth);
  
  const stats = [
    { label: 'Giáo án đã tạo', value: '12', icon: BookOpen, color: 'text-blue-600', bg: 'bg-blue-50' },
    { label: 'Học sinh quản lý', value: '128', icon: Users, color: 'text-purple-600', bg: 'bg-purple-50' },
    { label: 'Thời gian AI hỗ trợ', value: '45h', icon: Clock, color: 'text-emerald-600', bg: 'bg-emerald-50' },
    { label: 'Điểm hiệu suất', value: '98%', icon: TrendingUp, color: 'text-amber-600', bg: 'bg-amber-50' },
  ];

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Chào buổi sáng';
    if (hour < 18) return 'Chào buổi chiều';
    return 'Chào buổi tối';
  };

  return (
    <div className="max-w-7xl mx-auto p-4 md:p-8 animate-in fade-in duration-700">
      {/* Welcome Banner */}
      <div className="relative overflow-hidden rounded-[2.5rem] bg-gradient-to-br from-slate-900 via-slate-800 to-blue-900 p-8 md:p-12 mb-10 shadow-2xl">
        <div className="absolute top-0 right-0 -mt-10 -mr-10 w-80 h-80 bg-blue-500/20 rounded-full blur-[100px] animate-pulse"></div>
        <div className="absolute bottom-0 left-0 -mb-10 -ml-10 w-80 h-80 bg-purple-500/10 rounded-full blur-[100px]"></div>
        
        <div className="relative z-10">
          <div className="flex items-center gap-3 mb-4">
             <span className="px-4 py-1.5 bg-blue-500/20 backdrop-blur-md border border-blue-400/30 rounded-full text-blue-300 text-xs font-bold uppercase tracking-widest flex items-center gap-2">
                <Sparkles className="w-3.5 h-3.5" /> AI Powered Platform
             </span>
          </div>
          <h1 className="text-3xl md:text-5xl font-extrabold text-white mb-4 tracking-tight">
            {getGreeting()}, <span className="bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-emerald-400">{user?.fullName || 'Giáo viên'}</span>!
          </h1>
          <p className="text-slate-300 text-lg md:text-xl max-w-2xl font-medium leading-relaxed">
            Hôm nay là một ngày tuyệt vời để sáng tạo những bài giảng đầy cảm hứng. Bạn có <span className="text-emerald-400 font-bold">2 cuộc họp</span> và <span className="text-blue-400 font-bold">3 lớp học</span> sắp tới.
          </p>
          
          <div className="flex flex-wrap gap-4 mt-8">
             <button className="px-6 py-3 bg-white text-slate-900 font-bold rounded-2xl hover:bg-slate-100 transition-all flex items-center gap-2 shadow-lg active:scale-95">
                <Zap className="w-5 h-5 text-blue-600 shadow-sm" /> Soạn bài nhanh với AI
             </button>
             <button className="px-6 py-3 bg-white/10 backdrop-blur-md border border-white/20 text-white font-bold rounded-2xl hover:bg-white/20 transition-all flex items-center gap-2 active:scale-95">
                <Calendar className="w-5 h-5" /> Xem lịch giảng dạy
             </button>
          </div>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
        {stats.map((item, idx) => (
          <div key={idx} className="bg-white p-6 rounded-[2rem] border border-slate-100 shadow-xl shadow-slate-200/40 hover:shadow-2xl hover:-translate-y-1 transition-all duration-300 group">
             <div className="flex justify-between items-start mb-4">
                <div className={`w-12 h-12 rounded-2xl flex items-center justify-center ${item.bg} ${item.color} transition-transform duration-500 group-hover:scale-110`}>
                   <item.icon className="w-6 h-6" />
                </div>
                <span className="text-[10px] font-bold text-emerald-500 bg-emerald-50 px-2 py-1 rounded-lg flex items-center gap-1">
                   +12% <ArrowUpRight className="w-3 h-3" />
                </span>
             </div>
             <p className="text-slate-500 text-sm font-semibold uppercase tracking-wider">{item.label}</p>
             <h3 className="text-3xl font-black text-slate-900 mt-1">{item.value}</h3>
          </div>
        ))}
      </div>

      {/* Primary Content Areas */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Main Feed */}
        <div className="lg:col-span-2 space-y-8">
           <div className="bg-white rounded-[2.5rem] border border-slate-100 p-8 shadow-xl shadow-slate-200/30">
              <div className="flex items-center justify-between mb-8">
                 <h3 className="text-xl font-bold text-slate-900 flex items-center gap-2">
                    <Target className="w-6 h-6 text-blue-600" /> Hoạt động gần đây
                 </h3>
                 <button className="text-sm font-bold text-blue-600 hover:underline">Xem tất cả</button>
              </div>
              
              <div className="space-y-6">
                {[1, 2, 3].map(i => (
                  <div key={i} className="flex gap-4 group cursor-pointer p-4 hover:bg-slate-50 rounded-2xl transition-all">
                     <div className="w-12 h-12 bg-slate-100 rounded-xl flex-shrink-0 flex items-center justify-center font-bold text-slate-400 group-hover:bg-blue-100 group-hover:text-blue-600 transition-colors">
                        0{i}
                     </div>
                     <div className="flex-1">
                        <h4 className="font-bold text-slate-800 group-hover:text-blue-700 transition-colors">Bạn đã hoàn thành giáo án "Vật lý lớp 10 - Chương 1"</h4>
                        <p className="text-sm text-slate-500 mt-1 font-medium">Hỗ trợ bởi AI Editor • 2 giờ trước</p>
                     </div>
                  </div>
                ))}
              </div>
           </div>
        </div>

        {/* Sidebar Mini Profile/Tools */}
        <div className="space-y-8">
           <div className="bg-gradient-to-br from-indigo-500 to-purple-600 rounded-[2.5rem] p-8 text-white shadow-xl shadow-indigo-500/20">
              <h3 className="text-xl font-bold mb-4 flex items-center gap-2">
                 <Zap className="w-6 h-6 fill-amber-400 text-amber-400" /> Nâng cấp PRO
              </h3>
              <p className="text-indigo-100 text-sm mb-6 leading-relaxed opacity-90">
                Trải nghiệm toàn bộ sức mạnh của AI với bộ công cụ soạn giáo án và chấm điểm tự động không giới hạn.
              </p>
              <button className="w-full py-3.5 bg-white text-indigo-600 font-bold rounded-2xl shadow-lg hover:shadow-white/20 transition-all active:scale-95">
                Xem bảng giá ngay
              </button>
           </div>
           
           <div className="bg-white rounded-[2.5rem] border border-slate-100 p-8 shadow-xl shadow-slate-200/30">
              <h3 className="text-lg font-bold text-slate-900 mb-6">Thông tin tài khoản</h3>
              <div className="space-y-4">
                 <div className="flex justify-between py-2 border-b border-slate-50">
                    <span className="text-sm text-slate-500 font-medium">Vai trò</span>
                    <span className="text-sm font-bold text-blue-600 bg-blue-50 px-3 py-1 rounded-lg">{user?.role}</span>
                 </div>
                 <div className="flex justify-between py-2 border-b border-slate-50">
                    <span className="text-sm text-slate-500 font-medium">Ngày tham gia</span>
                    <span className="text-sm font-bold text-slate-700">17/03/2026</span>
                 </div>
                 <div className="flex justify-between py-2">
                    <span className="text-sm text-slate-500 font-medium">Trạng thái</span>
                    <span className="text-sm font-bold text-emerald-500">Đang hoạt động</span>
                 </div>
              </div>
           </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
