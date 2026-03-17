import React, { useState, useEffect } from 'react';
import { Loader2, Package, Clock, ShieldCheck, XCircle } from 'lucide-react';
import { subscriptionsApi } from '../../features/subscriptions/subscriptionsApi';

const TeacherOrderHistoryPage = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchOrders = async () => {
    try {
      const res = await subscriptionsApi.getMyOrders();
      setOrders(res.data);
    } catch (error) {
      console.error("Lỗi khi tải lịch sử đơn hàng:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const getStatusIcon = (status) => {
    switch(status) {
      case 'ACTIVE': return <ShieldCheck className="w-5 h-5 text-green-500" />;
      case 'PENDING': return <Clock className="w-5 h-5 text-yellow-500" />;
      case 'EXPIRED': return <Package className="w-5 h-5 text-slate-400" />;
      case 'CANCELLED': return <XCircle className="w-5 h-5 text-red-500" />;
      default: return null;
    }
  };

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <h1 className="text-2xl font-bold text-slate-800 mb-6">Lịch Sử Mua Gói</h1>

      {loading ? (
        <div className="flex justify-center py-20"><Loader2 className="w-8 h-8 animate-spin text-blue-500" /></div>
      ) : orders.length === 0 ? (
        <div className="text-center bg-white p-12 rounded-xl shadow-sm border border-slate-200">
          <Package className="mx-auto w-12 h-12 text-slate-300 mb-4" />
          <h3 className="text-lg font-medium text-slate-700">Chưa có giao dịch</h3>
          <p className="text-slate-500">Bạn chưa mua hoặc yêu cầu gói dịch vụ nào.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map(order => (
            <div key={order.id} className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 flex flex-col md:flex-row items-center justify-between transition hover:shadow-md">
              
              <div className="flex items-center space-x-4 mb-4 md:mb-0 w-full md:w-auto">
                <div className="bg-slate-50 p-3 rounded-full border border-slate-100">
                  {getStatusIcon(order.status)}
                </div>
                <div>
                  <h3 className="text-lg font-bold text-slate-800">{order.packageName}</h3>
                  <div className="text-sm text-slate-500 mt-1">Đơn hàng #{order.id} • {new Date(order.createdAt).toLocaleDateString()}</div>
                </div>
              </div>

              <div className="flex items-center justify-between w-full md:w-auto gap-8">
                <div className="text-right">
                  <span className="block text-xl font-bold text-blue-600">${order.amountPaid}</span>
                  <span className="text-xs text-slate-500 capitalize">{order.paymentMethod}</span>
                </div>
                
                <div className="w-32 text-right">
                  <span className={`inline-block px-3 py-1 text-xs font-semibold rounded-full 
                    ${order.status === 'PENDING' ? 'bg-yellow-100 text-yellow-700' : 
                      order.status === 'ACTIVE' ? 'bg-green-100 text-green-700' : 
                      order.status === 'CANCELLED' ? 'bg-red-100 text-red-700' : 
                      'bg-slate-100 text-slate-700'}`}>
                    {order.status}
                  </span>
                  {order.status === 'ACTIVE' && order.expiresAt && (
                    <div className="text-xs text-slate-500 mt-2">
                       Hết hạn: {new Date(order.expiresAt).toLocaleDateString()}
                    </div>
                  )}
                </div>
              </div>

            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default TeacherOrderHistoryPage;
