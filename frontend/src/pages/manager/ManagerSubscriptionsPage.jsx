import React, { useState, useEffect } from 'react';
import { Plus, Edit3, Power, Loader2, Sparkles, Calendar, Check, X, Shield, Zap, Rocket } from 'lucide-react';
import { subscriptionsApi } from '../../features/subscriptions/subscriptionsApi';

const ManagerSubscriptionsPage = () => {
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: 0,
    durationDays: 30,
    features: '[]',
    isActive: true
  });

  const fetchPackages = async () => {
    try {
      const res = await subscriptionsApi.getPackages();
      setPackages(res.data);
    } catch (error) {
      console.error("Lỗi khi tải danh sách gói:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPackages();
  }, []);

  const handleOpenModal = (pkg = null) => {
    if (pkg) {
      setEditingId(pkg.id);
      setFormData({
        name: pkg.name,
        description: pkg.description,
        price: pkg.price,
        durationDays: pkg.durationDays,
        features: pkg.features,
        isActive: pkg.isActive
      });
    } else {
      setEditingId(null);
      setFormData({
        name: '',
        description: '',
        price: 0,
        durationDays: 30,
        features: '[]',
        isActive: true
      });
    }
    setShowModal(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    try {
      const dataToSave = { ...formData };
      if (editingId) {
        await subscriptionsApi.updatePackage(editingId, dataToSave);
      } else {
        await subscriptionsApi.createPackage(dataToSave);
      }
      setShowModal(false);
      fetchPackages();
    } catch (error) {
      console.error("Lỗi khi lưu gói:", error);
      alert("Đã có lỗi xảy ra. Kiểm tra console.");
    }
  };

  const handleDeactivate = async (id) => {
    if (window.confirm("Bạn có chắc chắn muốn vô hiệu hoá gói này?")) {
      try {
        await subscriptionsApi.deactivatePackage(id);
        fetchPackages();
      } catch (error) {
        console.error("Lỗi khi cập nhật gói:", error);
      }
    }
  };

  const getPlanIcon = (name) => {
    const n = name.toUpperCase();
    if (n.includes('FREE')) return <Zap className="w-6 h-6 text-emerald-500" />;
    if (n.includes('PRO')) return <Rocket className="w-6 h-6 text-blue-500" />;
    return <Sparkles className="w-6 h-6 text-purple-500" />;
  };

  const getPlanGradient = (name) => {
    const n = name.toUpperCase();
    if (n.includes('FREE')) return 'from-emerald-50 to-teal-50 border-emerald-100';
    if (n.includes('PRO')) return 'from-blue-50 to-indigo-50 border-blue-100';
    return 'from-purple-50 to-fuchsia-50 border-purple-100';
  };

  return (
    <div className="max-w-7xl mx-auto p-4 md:p-8 animate-in fade-in duration-500">
      {/* Header Section */}
      <div className="relative overflow-hidden rounded-3xl bg-slate-900 p-8 md:p-12 mb-10 shadow-2xl">
        <div className="absolute top-0 right-0 -mt-10 -mr-10 w-64 h-64 bg-blue-500/10 rounded-full blur-3xl"></div>
        <div className="absolute bottom-0 left-0 -mb-10 -ml-10 w-64 h-64 bg-purple-500/10 rounded-full blur-3xl"></div>
        
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div>
            <h1 className="text-3xl md:text-4xl font-bold text-white mb-2">Quản lý Gói dịch vụ</h1>
            <p className="text-slate-400 text-lg">Thiết kế và cấu hình các gói VIP cho hệ thống của bạn.</p>
          </div>
          <button 
            onClick={() => handleOpenModal()}
            className="group flex items-center justify-center gap-2 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white font-semibold py-3 px-6 rounded-2xl shadow-lg shadow-blue-500/20 transition-all duration-300 hover:-translate-y-1 active:scale-95"
          >
            <Plus className="w-5 h-5 group-hover:rotate-90 transition-transform duration-300" />
            Tạo Gói Mới
          </button>
        </div>
      </div>

      {loading ? (
        <div className="flex flex-col items-center justify-center py-20 gap-4">
          <Loader2 className="w-12 h-12 animate-spin text-blue-600" />
          <p className="font-medium text-slate-500">Đang chuẩn bị dữ liệu...</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {Array.isArray(packages) && packages.map((pkg, idx) => (
            <div 
              key={pkg.id} 
              className={`group relative bg-white rounded-[2rem] border-2 p-8 transition-all duration-500 hover:shadow-2xl hover:shadow-slate-200/50 hover:-translate-y-2 ${!pkg.isActive ? 'opacity-70 grayscale bg-slate-50 border-slate-200' : getPlanGradient(pkg.name)}`}
              style={{ animationDelay: `${idx * 100}ms` }}
            >
              {/* Status Badge */}
              <div className="absolute top-6 right-6">
                <span className={`flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider ${pkg.isActive ? 'bg-white text-emerald-600 shadow-sm' : 'bg-slate-200 text-slate-500'}`}>
                  {pkg.isActive ? (
                    <><div className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" /> Active</>
                  ) : (
                    <><X className="w-3 h-3" /> Disabled</>
                  )}
                </span>
              </div>

              {/* Icon & Title */}
              <div className="mb-6">
                <div className={`w-14 h-14 rounded-2xl flex items-center justify-center mb-6 shadow-sm ${pkg.isActive ? 'bg-white' : 'bg-slate-200'}`}>
                  {getPlanIcon(pkg.name)}
                </div>
                <h3 className="text-2xl font-bold text-slate-800 mb-2">{pkg.name}</h3>
                <p className="text-slate-500 text-sm leading-relaxed line-clamp-2 h-10">
                  {pkg.description || 'Gói dịch vụ cao cấp dành cho giáo viên hiện đại.'}
                </p>
              </div>

              {/* Price Section */}
              <div className="mb-8 pt-6 border-t border-slate-200/50">
                <div className="flex items-baseline gap-1">
                  <span className="text-4xl font-extrabold text-slate-900 tracking-tight">${pkg.price}</span>
                  <span className="text-slate-400 font-medium">/ gối</span>
                </div>
                <div className="flex items-center gap-2 mt-2 text-sm text-slate-500 bg-white/50 w-fit px-3 py-1 rounded-lg">
                  <Calendar className="w-4 h-4" /> 
                  Sử dụng trong {pkg.durationDays} ngày
                </div>
              </div>

              {/* Action Buttons */}
              <div className="flex items-center gap-3">
                <button 
                  onClick={() => handleOpenModal(pkg)}
                  className="flex-1 flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-white border border-slate-200 text-slate-700 font-bold text-sm hover:bg-slate-50 transition-colors shadow-sm"
                >
                  <Edit3 className="w-4 h-4" /> Chi tiết
                </button>
                {pkg.isActive && (
                  <button 
                    onClick={() => handleDeactivate(pkg.id)}
                    className="p-3 rounded-xl bg-white border border-red-100 text-red-500 hover:bg-red-50 transition-colors shadow-sm"
                    title="Ngừng kích hoạt"
                  >
                    <Power className="w-5 h-5" />
                  </button>
                )}
              </div>
            </div>
          ))}

          {(!Array.isArray(packages) || packages.length === 0) && (
             <div className="col-span-full bg-white/50 backdrop-blur-sm border-2 border-dashed border-slate-200 rounded-[2.5rem] p-16 text-center">
               <div className="w-20 h-20 bg-slate-100 rounded-3xl flex items-center justify-center mx-auto mb-6">
                  <Shield className="w-10 h-10 text-slate-400" />
               </div>
               <h3 className="text-xl font-bold text-slate-800 mb-2">Chưa có gói dịch vụ</h3>
               <p className="text-slate-500 max-w-sm mx-auto">Hãy bắt đầu tạo những gói dịch vụ đầu tiên để giáo viên có thể nâng cấp tài khoản.</p>
             </div>
          )}
        </div>
      )}

      {/* Modern Modal Design */}
      {showModal && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 md:p-6">
          <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-md animate-in fade-in duration-300" onClick={() => setShowModal(false)}></div>
          
          <div className="relative w-full max-w-xl bg-white rounded-[2.5rem] shadow-2xl overflow-hidden animate-in zoom-in-95 slide-in-from-bottom-10 duration-500">
            {/* Modal Header */}
            <div className={`p-8 pb-0 flex justify-between items-start`}>
               <div>
                  <h2 className="text-2xl font-bold text-slate-900">{editingId ? 'Cập nhật Gói' : 'Thiết lập Gói mới'}</h2>
                  <p className="text-slate-500 text-sm mt-1">Vui lòng điền đầy đủ các thông tin bên dưới.</p>
               </div>
               <button onClick={() => setShowModal(false)} className="p-2 hover:bg-slate-100 rounded-full transition-colors text-slate-400">
                  <X className="w-6 h-6" />
               </button>
            </div>

            <form onSubmit={handleSave} className="p-8 space-y-5">
              <div className="space-y-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-500 uppercase tracking-wider ml-1">Tên định danh gói</label>
                  <input required type="text" className="w-full bg-slate-50 border-none rounded-2xl px-5 py-3.5 text-slate-800 font-medium focus:ring-2 focus:ring-blue-500 transition-all placeholder:text-slate-400" placeholder="VD: Premium Pro Max" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} />
                </div>
                
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-500 uppercase tracking-wider ml-1">Mô tả ngắn gọn</label>
                  <textarea className="w-full bg-slate-50 border-none rounded-2xl px-5 py-3.5 text-slate-800 font-medium focus:ring-2 focus:ring-blue-500 transition-all resize-none placeholder:text-slate-400" placeholder="Mô tả những lợi ích tuyệt vời nhất..." value={formData.description} onChange={e => setFormData({...formData, description: e.target.value})} rows="3" />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-1.5">
                    <label className="text-xs font-bold text-slate-500 uppercase tracking-wider ml-1">Đơn giá ($)</label>
                    <div className="relative">
                      <span className="absolute left-5 top-1/2 -translate-y-1/2 text-slate-400 font-bold">$</span>
                      <input required type="number" step="0.01" className="w-full bg-slate-50 border-none rounded-2xl pl-10 pr-5 py-3.5 text-slate-800 font-bold focus:ring-2 focus:ring-blue-500 transition-all" value={formData.price} onChange={e => setFormData({...formData, price: e.target.value})} />
                    </div>
                  </div>
                  <div className="space-y-1.5">
                    <label className="text-xs font-bold text-slate-500 uppercase tracking-wider ml-1">Hạn dùng (ngày)</label>
                    <input required type="number" className="w-full bg-slate-50 border-none rounded-2xl px-5 py-3.5 text-slate-800 font-bold focus:ring-2 focus:ring-blue-500 transition-all" value={formData.durationDays} onChange={e => setFormData({...formData, durationDays: e.target.value})} />
                  </div>
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-500 uppercase tracking-wider ml-1">Tính năng đi kèm (JSON)</label>
                  <div className="relative group">
                    <div className="absolute top-3.5 left-5">
                       <Check className="w-5 h-5 text-blue-500" />
                    </div>
                    <input type="text" className="w-full bg-slate-50 border-none rounded-2xl pl-12 pr-5 py-3.5 text-slate-600 font-mono text-sm focus:ring-2 focus:ring-blue-500 transition-all" value={formData.features} onChange={e => setFormData({...formData, features: e.target.value})} placeholder='["Feature 1", "Feature 2"]' />
                  </div>
                </div>

                <label className="flex items-center gap-3 p-4 bg-slate-50 rounded-2xl cursor-pointer group transition-colors hover:bg-slate-100">
                  <div className="relative">
                    <input type="checkbox" className="sr-only peer" checked={formData.isActive} onChange={e => setFormData({...formData, isActive: e.target.checked})} />
                    <div className="w-12 h-6 bg-slate-300 rounded-full peer peer-checked:bg-blue-600 transition-all"></div>
                    <div className="absolute top-1 left-1 w-4 h-4 bg-white rounded-full transition-all peer-checked:translate-x-6"></div>
                  </div>
                  <span className="text-sm font-bold text-slate-700">Kích hoạt gói dịch vụ ngay lập tức</span>
                </label>
              </div>

              <div className="flex gap-4 pt-4 mt-6">
                <button type="button" onClick={() => setShowModal(false)} className="flex-1 py-3.5 px-6 rounded-2xl bg-slate-100 text-slate-600 font-bold hover:bg-slate-200 transition-all active:scale-95">Hủy bỏ</button>
                <button type="submit" className="flex-[2] py-3.5 px-6 rounded-2xl bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-bold hover:shadow-lg hover:shadow-blue-500/25 transition-all active:scale-95">Lưu thay đổi</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ManagerSubscriptionsPage;
