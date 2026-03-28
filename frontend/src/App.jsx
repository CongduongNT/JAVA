import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';

// Layout & Guards
import MainLayout from './components/layout/MainLayout';
import ProtectedRoute from './components/ProtectedRoute';
import RoleGuard from './components/guards/RoleGuard';

// Public pages
import Login from './pages/LoginPage';
import Register from './pages/RegisterPage';
import UnauthorizedPage from './pages/UnauthorizedPage';

// Dashboard (all authenticated users)
import DashboardPage from './pages/DashboardPage';

// ─── Placeholder pages (tạo sau khi implement từng feature) ───
// import AdminUserPage from './pages/admin/AdminUserPage';
// import ManagerTeachersPage from './pages/manager/ManagerTeachersPage';
import ManagerSubscriptionsPage from './pages/manager/ManagerSubscriptionsPage';
import ManagerOrdersPage from './pages/manager/ManagerOrdersPage';
import TeacherPackagesPage from './pages/teacher/TeacherPackagesPage';
import TeacherOrderHistoryPage from './pages/teacher/TeacherOrderHistoryPage';
// import ManagerAnalyticsPage from './pages/manager/ManagerAnalyticsPage';
// import StaffPromptPage from './pages/staff/StaffPromptPage';
// import TeacherLessonPlansPage from './pages/teacher/TeacherLessonPlansPage';
// import QuestionBankPage from './pages/QuestionBankPage';
// import ExamGeneratorPage from './pages/ExamGeneratorPage';
// import OCRGradingPage from './pages/OCRGradingPage';
import QuestionBankPage from './features/question-bank/QuestionBankPage';

function App() {
  return (
    <>
      <Toaster position="top-right" />
      <Routes>
      {/* ======================================================
          PUBLIC ROUTES
      ====================================================== */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/unauthorized" element={<UnauthorizedPage />} />

      {/* ======================================================
          PROTECTED ROUTES (require authentication)
          Mọi route bên trong đều cần đăng nhập trước (ProtectedRoute).
          Sau đó từng route có thể thêm RoleGuard để giới hạn role.
      ====================================================== */}
      <Route
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        {/* Dashboard – tất cả roles */}
        <Route path="/dashboard" element={<DashboardPage />} />

        {/* ── ADMIN routes ── */}
        <Route
          path="/admin/users"
          element={
            <RoleGuard roles={['ADMIN']}>
              {/* <AdminUserPage /> */}
              <div className="p-8"><h2 className="text-2xl font-bold">Admin – User Management</h2><p className="text-slate-500 mt-2">Implement AdminUserPage tại đây.</p></div>
            </RoleGuard>
          }
        />

        {/* ── MANAGER routes ── */}
        <Route
          path="/manager/teachers"
          element={
            <RoleGuard roles={['MANAGER', 'ADMIN']}>
              {/* <ManagerTeachersPage /> */}
              <div className="p-8"><h2 className="text-2xl font-bold">Manager – Danh sách giáo viên</h2></div>
            </RoleGuard>
          }
        />
        <Route
          path="/manager/subscriptions"
          element={
            <RoleGuard roles={['MANAGER', 'ADMIN']}>
              <ManagerSubscriptionsPage />
            </RoleGuard>
          }
        />
        <Route
          path="/manager/orders"
          element={
            <RoleGuard roles={['MANAGER', 'ADMIN']}>
              <ManagerOrdersPage />
            </RoleGuard>
          }
        />
        <Route
          path="/manager/analytics"
          element={
            <RoleGuard roles={['MANAGER', 'ADMIN']}>
              {/* <ManagerAnalyticsPage /> */}
              <div className="p-8"><h2 className="text-2xl font-bold">Manager – Analytics</h2></div>
            </RoleGuard>
          }
        />

        {/* ── STAFF routes ── */}
        <Route
          path="/prompt-templates"
          element={
            <RoleGuard roles={['STAFF', 'MANAGER', 'ADMIN']}>
              {/* <StaffPromptPage /> */}
              <div className="p-8"><h2 className="text-2xl font-bold">Staff – AI Prompt Templates</h2></div>
            </RoleGuard>
          }
        />

        {/* ── TEACHER routes ── */}
        <Route
          path="/lesson-plans"
          element={
            <RoleGuard roles={['TEACHER']}>
              {/* <TeacherLessonPlansPage /> */}
              <div className="p-8"><h2 className="text-2xl font-bold">Teacher – Lesson Plans</h2></div>
            </RoleGuard>
          }
        />
        <Route
          path="/packages"
          element={
            <RoleGuard roles={['TEACHER']}>
              <TeacherPackagesPage />
            </RoleGuard>
          }
        />
        <Route
          path="/orders/history"
          element={
            <RoleGuard roles={['TEACHER']}>
              <TeacherOrderHistoryPage />
            </RoleGuard>
          }
        />
        <Route
          path="/exam-generator"
          element={
            <RoleGuard roles={['TEACHER']}>
              {/* <ExamGeneratorPage /> */}
              <div className="p-8"><h2 className="text-2xl font-bold">Teacher – Exam Generator</h2></div>
            </RoleGuard>
          }
        />
        <Route
          path="/ocr-grading"
          element={
            <RoleGuard roles={['TEACHER']}>
              {/* <OCRGradingPage /> */}
              <div className="p-8"><h2 className="text-2xl font-bold">Teacher – OCR Grading</h2></div>
            </RoleGuard>
          }
        />

        {/* Question Bank – TEACHER + STAFF */}
        <Route
          path="/question-bank"
          element={
            <RoleGuard roles={['TEACHER', 'STAFF', 'MANAGER', 'ADMIN']}>
              <QuestionBankPage />
            </RoleGuard>
          }
        />
      </Route>
    </Routes>
  </>
);
}

export default App;
