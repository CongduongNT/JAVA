import React from 'react';

import { useSelector, useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { logout } from '../features/auth/authSlice';

const DashboardPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { user } = useSelector((state) => state.auth);

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  return (
    <div className="p-10">
      <div className="max-w-4xl mx-auto">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold">Dashboard</h1>
        </div>
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-2xl">🎉 Welcome, {user?.fullName || 'User'}!</h2>
          <p className="text-slate-600 mt-2">Your role is: <strong>{user?.role}</strong></p>
          <p className="text-slate-500 mt-4">This is your main dashboard. You can add widgets and statistics here.</p>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
