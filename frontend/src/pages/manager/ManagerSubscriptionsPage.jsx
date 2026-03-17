import React, { useState, useEffect } from 'react';
import { Plus, Edit, Trash2, PowerOff, Loader2 } from 'lucide-react';
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
      // features validation - assuming it's JSON array
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

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-slate-800">Quản lý Gói dịch vụ</h1>
        <button 
          onClick={() => handleOpenModal()}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg flex items-center hover:bg-blue-700 transition"
        >
          <Plus className="w-5 h-5 mr-2" /> Thêm Gói Mới
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center py-10"><Loader2 className="w-8 h-8 animate-spin text-blue-500" /></div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {Array.isArray(packages) && packages.map(pkg => (
            <div key={pkg.id} className={`bg-white rounded-xl shadow-sm border p-6 transition hover:shadow-md ${pkg.isActive === false ? 'opacity-60 grayscale' : ''}`}>
              <div className="flex justify-between items-start mb-4">
                <h3 className="text-xl font-bold text-slate-800">{pkg.name}</h3>
                <span className={`px-2 py-1 text-xs font-semibold rounded-full ${pkg.isActive ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                  {pkg.isActive ? 'Active' : 'Deactivated'}
                </span>
              </div>
              <p className="text-slate-500 text-sm mb-4 h-10 line-clamp-2">{pkg.description}</p>
              
              <div className="mb-4">
                <div className="text-2xl font-bold text-blue-600">${pkg.price}</div>
                <div className="text-sm text-slate-500">/ {pkg.durationDays} ngày</div>
              </div>
              
              <div className="flex gap-2 mt-4 pt-4 border-t border-slate-100">
                <button title="Sửa" onClick={() => handleOpenModal(pkg)} className="p-2 text-slate-400 hover:text-blue-500 hover:bg-blue-50 rounded-lg transition">
                  <Edit className="w-5 h-5" />
                </button>
                {pkg.isActive && (
                  <button title="Huỷ kích hoạt" onClick={() => handleDeactivate(pkg.id)} className="p-2 text-slate-400 hover:text-orange-500 hover:bg-orange-50 rounded-lg transition ml-auto">
                    <PowerOff className="w-5 h-5" />
                  </button>
                )}
              </div>
            </div>
          ))}
          {(!Array.isArray(packages) || packages.length === 0) && (
             <div className="col-span-full text-center py-10 text-slate-500 bg-white rounded-xl border">
               Chưa có gói dịch vụ nào được cấu hình.
             </div>
          )}
        </div>
      )}

      {/* Modal CRUD */}
      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-lg">
            <h2 className="text-xl font-bold text-slate-800 mb-4">{editingId ? 'Sửa Gói' : 'Thêm Gói Mới'}</h2>
            <form onSubmit={handleSave} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Tên gói</label>
                <input required type="text" className="w-full border border-slate-300 rounded-lg px-3 py-2" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Mô tả</label>
                <textarea className="w-full border border-slate-300 rounded-lg px-3 py-2" value={formData.description} onChange={e => setFormData({...formData, description: e.target.value})} rows="2" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Giá ($)</label>
                  <input required type="number" step="0.01" className="w-full border border-slate-300 rounded-lg px-3 py-2" value={formData.price} onChange={e => setFormData({...formData, price: e.target.value})} />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Thời hạn (ngày)</label>
                  <input required type="number" className="w-full border border-slate-300 rounded-lg px-3 py-2" value={formData.durationDays} onChange={e => setFormData({...formData, durationDays: e.target.value})} />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Tính năng (JSON Array)</label>
                <input type="text" className="w-full border border-slate-300 rounded-lg px-3 py-2" value={formData.features} onChange={e => setFormData({...formData, features: e.target.value})} placeholder='["Feature 1", "Feature 2"]' />
              </div>
              <div className="flex items-center">
                <input type="checkbox" id="isActive" checked={formData.isActive} onChange={e => setFormData({...formData, isActive: e.target.checked})} className="mr-2" />
                <label htmlFor="isActive" className="text-sm font-medium text-slate-700">Kích hoạt</label>
              </div>

              <div className="flex justify-end gap-3 mt-6 pt-4 border-t border-slate-100">
                <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 text-slate-600 hover:bg-slate-100 rounded-lg transition">Hủy</button>
                <button type="submit" className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">Lưu Lại</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ManagerSubscriptionsPage;
