import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldX } from 'lucide-react';

const UnauthorizedPage = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50">
      <div className="text-center p-8">
        <div className="flex justify-center mb-4">
          <ShieldX className="w-16 h-16 text-red-400" />
        </div>
        <h1 className="text-3xl font-bold text-slate-800 mb-2">403 – Access Denied</h1>
        <p className="text-slate-500 mb-6">
          Bạn không có quyền truy cập trang này.
          <br />
          Vui lòng liên hệ Admin nếu bạn cho rằng đây là nhầm lẫn.
        </p>
        <div className="flex gap-3 justify-center">
          <button
            onClick={() => navigate(-1)}
            className="px-5 py-2 rounded-lg border border-slate-300 text-slate-600 hover:bg-slate-100 transition"
          >
            ← Quay lại
          </button>
          <button
            onClick={() => navigate('/dashboard')}
            className="px-5 py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700 transition"
          >
            Về Dashboard
          </button>
        </div>
      </div>
    </div>
  );
};

export default UnauthorizedPage;
