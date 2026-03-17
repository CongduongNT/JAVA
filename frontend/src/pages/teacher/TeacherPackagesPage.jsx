import React, { useState, useEffect } from 'react';
import { Loader2, Check, ShoppingCart } from 'lucide-react';
import { subscriptionsApi } from '../../features/subscriptions/subscriptionsApi';

const TeacherPackagesPage = () => {
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchPackages = async () => {
    try {
      const res = await subscriptionsApi.getPackages();
      const activePackages = res.data.filter(pkg => pkg.isActive);
      setPackages(activePackages);
    } catch (error) {
      console.error("Lỗi khi tải danh sách gói:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPackages();
  }, []);

  const handleSubscribe = async (pkg) => {
    const confirmBuy = window.confirm(`Bạn có muốn mua gói ${pkg.name} với giá $${pkg.price}?`);
    if (confirmBuy) {
      try {
        await subscriptionsApi.createOrder({
          packageId: pkg.id,
          paymentMethod: 'credit_card'
        });
        alert('Giao dịch thành công! Đơn hàng của bạn đang chờ hệ thống xử lý.');
      } catch (error) {
        console.error("Lỗi khi đăng ký gói:", error);
        alert('Lỗi khi thực hiện giao dịch.');
      }
    }
  };

  if (loading) {
    return <div className="flex justify-center py-20"><Loader2 className="w-10 h-10 animate-spin text-blue-500" /></div>;
  }

  return (
    <div className="p-6">
      <div className="text-center max-w-2xl mx-auto mb-12">
        <h1 className="text-3xl font-display font-bold text-slate-800 mb-4">Các Giải Pháp Tối Ưu Cho Giáo Viên</h1>
        <p className="text-slate-500">Nâng cấp PlanbookAI để mở khóa thêm giới hạn tài nguyên và tính năng AI mạnh mẽ, tiết kiệm hàng chục giờ soạn giáo án.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 max-w-6xl mx-auto">
        {packages.map((pkg, i) => {
          let featuresList = [];
          try {
             if(pkg.features) featuresList = JSON.parse(pkg.features);
          } catch(e) {}

          const isPremium = i === 1; // Highlight the middle package usually
          
          return (
            <div key={pkg.id} className={`relative bg-white rounded-2xl p-8 flex flex-col ${isPremium ? 'border-2 border-blue-500 shadow-xl scale-105 z-10' : 'border border-slate-200 shadow-md'}`}>
              {isPremium && (
                <div className="absolute top-0 left-1/2 transform -translate-x-1/2 -translate-y-1/2">
                  <span className="bg-gradient-to-r from-blue-600 to-indigo-600 text-white text-xs font-bold px-4 py-1.5 rounded-full uppercase tracking-wider">Phổ Biến Nhất</span>
                </div>
              )}
              <h3 className="text-2xl font-bold text-slate-800 mb-2">{pkg.name}</h3>
              <p className="text-slate-500 text-sm mb-6 h-10">{pkg.description}</p>
              
              <div className="mb-6 flex items-baseline text-slate-800">
                <span className="text-5xl font-extrabold tracking-tight">${pkg.price}</span>
                <span className="ml-1 text-xl font-medium text-slate-500">/{pkg.durationDays} ngày</span>
              </div>
              
              <button 
                onClick={() => handleSubscribe(pkg)}
                className={`w-full py-3 px-4 rounded-xl font-semibold flex cursor-pointer justify-center items-center transition-all duration-200 
                  ${isPremium 
                    ? 'bg-blue-600 text-white hover:bg-blue-700 shadow-md hover:shadow-lg' 
                    : 'bg-slate-100 text-slate-800 hover:bg-slate-200'}`}
              >
                <ShoppingCart className="w-5 h-5 mr-2" />
                Đăng ký ngay
              </button>

              <div className="mt-8">
                <h4 className="text-sm font-semibold text-slate-800 tracking-wide uppercase mb-4">Bao gồm:</h4>
                <ul className="space-y-4">
                  {(Array.isArray(featuresList) ? featuresList : Object.entries(featuresList).map(([k, v]) => `${k}: ${v}`)).map((feature, index) => (
                    <li key={index} className="flex items-start">
                      <div className="flex-shrink-0">
                        <Check className="w-5 h-5 text-green-500" />
                      </div>
                      <p className="ml-3 text-sm text-slate-600">{feature}</p>
                    </li>
                  ))}
                  {((Array.isArray(featuresList) && featuresList.length === 0) || (!Array.isArray(featuresList) && Object.keys(featuresList).length === 0)) && (
                    <li className="flex items-start">
                      <div className="flex-shrink-0"><Check className="w-5 h-5 text-green-500" /></div>
                      <p className="ml-3 text-sm text-slate-600">Quyền lợi cơ bản</p>
                    </li>
                  )}
                </ul>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default TeacherPackagesPage;
