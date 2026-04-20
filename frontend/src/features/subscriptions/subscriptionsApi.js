import api from '../../services/api';

export const subscriptionsApi = {
  // Packages
  getPackages: () => api.get('/packages'),
  createPackage: (data) => api.post('/packages', data),
  updatePackage: (id, data) => api.put(`/packages/${id}`, data),
  deletePackage: (id) => api.delete(`/packages/${id}`),
  deactivatePackage: (id) => api.put(`/packages/${id}/deactivate`),

  // Orders 
  getAllOrders: () => api.get('/orders'),
  getMyOrders: () => api.get('/orders/my'),
  createOrder: (data) => api.post('/orders', data),
  updateOrderStatus: (id, status) => api.put(`/orders/${id}/status`, { status }),

  // VNPay Payment
  createVNPayPayment: (packageId) =>
    api.post('/payment/vnpay/create-payment-url', {
      packageId,
      paymentMethod: 'VNPAY',
    }),
};
