import React, { useState, useEffect } from 'react';
import { Loader2, Check, ShoppingCart, Sparkles, Zap, Award, Star, ArrowRight, Rocket, Shield } from 'lucide-react';
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
    return (
      <div className="flex flex-col items-center justify-center py-32 gap-4">
        <Loader2 className="w-12 h-12 animate-spin text-blue-600" />
        <p className="text-slate-500 font-medium">Đang chuẩn bị gói ưu đãi tốt nhất...</p>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto p-4 md:p-8 animate-in fade-in duration-700">
      {/* Hero Intro */}
      <div className="text-center max-w-3xl mx-auto mb-20">
        <div className="inline-flex items-center gap-2 px-4 py-1.5 bg-blue-50 text-blue-600 rounded-full text-xs font-bold uppercase tracking-widest mb-6 border border-blue-100 shadow-sm">
           <Zap className="w-3.5 h-3.5 fill-blue-600" /> Nâng cấp trải nghiệm AI
        </div>
        <h1 className="text-4xl md:text-5xl font-black text-slate-900 mb-6 tracking-tight">Kế hoạch hoàn hảo cho <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-indigo-600">Sự nghiệp giảng dạy</span> của bạn</h1>
        <p className="text-slate-500 text-lg md:text-xl leading-relaxed font-medium">Chọn gói dịch vụ phù hợp để tối ưu công việc soạn giáo án và chấm điểm với sức mạnh của AI thế hệ mới.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 md:gap-10 pb-20">
        {packages.map((pkg, i) => {
          let featuresList = [];
          try {
             // Backend might return JSON string or object already if parsed by interceptor
             if(typeof pkg.features === 'string') {
                featuresList = JSON.parse(pkg.features);
             } else if (pkg.features) {
                featuresList = pkg.features;
             }
          } catch(e) {
             console.error("Error parsing features:", e);
          }

          const isPro = pkg.name.toUpperCase().includes('PRO');
          const isPremium = pkg.name.toUpperCase().includes('PREMIUM');
          const highlight = isPro || isPremium;
          
          return (
            <div 
              key={pkg.id} 
              className={`group relative flex flex-col rounded-[2.5rem] p-10 transition-all duration-500 hover:-translate-y-4 hover:shadow-2xl 
                ${highlight 
                   ? 'bg-slate-900 text-white shadow-blue-500/20 shadow-2xl scale-105 z-10 border-none' 
                   : 'bg-white text-slate-800 border-2 border-slate-100 shadow-xl shadow-slate-200/50'}`}
              style={{ animationDelay: `${i * 150}ms` }}
            >
              {/* Badge for highlight plans */}
              {isPro && (
                <div className="absolute top-0 left-1/2 transform -translate-x-1/2 -translate-y-1/2 bg-gradient-to-r from-blue-600 to-indigo-600 text-white text-[10px] font-black px-5 py-2 rounded-full shadow-xl uppercase tracking-widest">
                  Được chọn nhiều nhất
                </div>
              )}
              {isPremium && (
                <div className="absolute top-0 left-1/2 transform -translate-x-1/2 -translate-y-1/2 bg-gradient-to-r from-purple-600 to-fuchsia-600 text-white text-[10px] font-black px-5 py-2 rounded-full shadow-xl uppercase tracking-widest">
                  Giải pháp tối ưu
                </div>
              )}

              <div className="mb-10">
                <div className={`w-14 h-14 rounded-2xl flex items-center justify-center mb-6 shadow-sm ${highlight ? 'bg-white/10 text-white' : 'bg-blue-50 text-blue-600'}`}>
                   {isPro ? <Rocket className="w-7 h-7" /> : isPremium ? <Award className="w-7 h-7" /> : <Star className="w-7 h-7 text-amber-500" />}
                </div>
                <h3 className="text-3xl font-black mb-3 tracking-tight">{pkg.name}</h3>
                <p className={`text-sm leading-relaxed ${highlight ? 'text-slate-400' : 'text-slate-500'}`}>
                  {pkg.description || 'Gói dịch vụ chuyên nghiệp hỗ trợ giảng dạy thông minh.'}
                </p>
              </div>
              
              <div className="mb-10 flex items-baseline gap-1">
                <span className="text-5xl font-black tracking-tighter">${pkg.price}</span>
                <span className={`text-lg font-bold ${highlight ? 'text-slate-500' : 'text-slate-400'}`}>/ {pkg.durationDays} ngày</span>
              </div>
              
              <button 
                onClick={() => handleSubscribe(pkg)}
                className={`group w-full py-4 px-6 rounded-[1.25rem] font-black text-sm uppercase tracking-widest flex justify-center items-center gap-2 transition-all duration-300 shadow-lg active:scale-95
                  ${highlight 
                    ? 'bg-white text-slate-900 hover:bg-slate-100 shadow-white/10' 
                    : 'bg-slate-900 text-white hover:bg-slate-800 shadow-slate-900/20'}`}
              >
                <ShoppingCart className="w-5 h-5" /> Đăng ký ngay <ArrowRight className="w-4 h-4 opacity-0 group-hover:opacity-100 group-hover:translate-x-1 transition-all" />
              </button>

              <div className="mt-12 pt-10 border-t border-dashed border-slate-700/50">
                <h4 className="text-xs font-black uppercase tracking-widest opacity-60 mb-6 flex items-center gap-2">
                   <Check className="w-4 h-4 text-blue-500" /> Bao gồm các đặc quyền:
                </h4>
                <ul className="space-y-4">
                  {(Array.isArray(featuresList) ? featuresList : Object.entries(featuresList).map(([k, v]) => `${k}: ${v}`)).map((feature, index) => (
                    <li key={index} className="flex items-start gap-4">
                      <div className={`flex-shrink-0 w-5 h-5 rounded-full flex items-center justify-center mt-0.5 ${highlight ? 'bg-blue-500/20 text-blue-400' : 'bg-emerald-50 text-emerald-600'}`}>
                        <Check className="w-3.5 h-3.5" />
                      </div>
                      <p className={`text-sm font-bold ${highlight ? 'text-slate-300' : 'text-slate-600'}`}>{feature}</p>
                    </li>
                  ))}
                  {((Array.isArray(featuresList) && featuresList.length === 0) || (!Array.isArray(featuresList) && Object.keys(featuresList).length === 0)) && (
                    <li className="flex items-start gap-4">
                      <div className={`flex-shrink-0 w-5 h-5 rounded-full flex items-center justify-center mt-0.5 ${highlight ? 'bg-blue-500/20 text-blue-400' : 'bg-emerald-50 text-emerald-600'}`}>
                        <Check className="w-3.5 h-3.5" />
                      </div>
                      <p className={`text-sm font-bold ${highlight ? 'text-slate-300' : 'text-slate-600'}`}>Quyền lợi hỗ trợ 24/7</p>
                    </li>
                  )}
                </ul>
              </div>
            </div>
          );
        })}
      </div>
      
      {/* Trust section footer */}
      <div className="mt-10 p-12 bg-white rounded-[3rem] border-2 border-slate-50 shadow-2xl shadow-slate-200/50 flex flex-col md:flex-row items-center justify-between gap-10">
         <div className="flex-1">
            <h3 className="text-2xl font-black text-slate-900 mb-2">Đảm bảo hài lòng 100%</h3>
            <p className="text-slate-500 font-medium leading-relaxed">Nếu bạn không hài lòng với trải nghiệm, hãy liên hệ với chúng tôi trong vòng 7 ngày để được hỗ trợ hoàn tiền hoặc chuyển đổi gói phù hợp.</p>
         </div>
         <div className="flex items-center gap-8 opacity-40 grayscale group hover:grayscale-0 hover:opacity-100 transition-all duration-700">
            <Shield className="w-12 h-12" />
            <Sparkles className="w-12 h-12" />
            <Zap className="w-12 h-12" />
         </div>
      </div>
    </div>
  );
};

export default TeacherPackagesPage;
