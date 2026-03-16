import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';

// Import components from their correct subdirectories
import Login from './pages/LoginPage';
import Register from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import MainLayout from './components/layout/MainLayout';
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  return (
    <Routes>
      {/* Public routes that are accessible to everyone */}
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Protected routes that require authentication and use the MainLayout */}
      <Route element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
        <Route path="/dashboard" element={<DashboardPage />} />
        {/* Add other protected routes here */}
      </Route>
    </Routes>
  );
}

export default App;
