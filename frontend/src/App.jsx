import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'

// Layout & Guards
import MainLayout from './components/layout/MainLayout'
import ProtectedRoute from './components/ProtectedRoute'
import RoleGuard from './components/guards/RoleGuard'

// Public pages
import Login from './pages/LoginPage'
import Register from './pages/RegisterPage'
import UnauthorizedPage from './pages/UnauthorizedPage'

// Dashboard (all authenticated users)
import DashboardPage from './pages/DashboardPage'

// Features
import ManagerSubscriptionsPage from './pages/manager/ManagerSubscriptionsPage'
import ManagerOrdersPage from './pages/manager/ManagerOrdersPage'
import TeacherPackagesPage from './pages/teacher/TeacherPackagesPage'
import TeacherOrderHistoryPage from './pages/teacher/TeacherOrderHistoryPage'
import UsersPage from './pages/users/UsersPage'
import UserFormPage from './pages/users/UserFormPage'
import SettingsPage from './pages/settings/SettingsPage'
import QuestionBankPage from './features/question-bank/QuestionBankPage'
import BankQuestionsPage from './features/question-bank/BankQuestionsPage'
import { Toaster } from 'sonner'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/unauthorized" element={<UnauthorizedPage />} />

      <Route
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/settings" element={<SettingsPage />} />

        {/* ADMIN routes */}
        <Route
          path="/admin/users"
          element={
            <RoleGuard roles={['ADMIN']}>
              <UsersPage />
            </RoleGuard>
          }
        />
        <Route
          path="/admin/users/new"
          element={
            <RoleGuard roles={['ADMIN']}>
              <UserFormPage />
            </RoleGuard>
          }
        />
        <Route
          path="/admin/users/:id/edit"
          element={
            <RoleGuard roles={['ADMIN']}>
              <UserFormPage />
            </RoleGuard>
          }
        />

        {/* MANAGER routes */}
        <Route
          path="/manager/teachers"
          element={
            <RoleGuard roles={['MANAGER', 'ADMIN']}>
              <div className="p-8">
                <h2 className="text-2xl font-bold">Manager – Danh sách giáo viên</h2>
              </div>
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
              <div className="p-8">
                <h2 className="text-2xl font-bold">Manager – Analytics</h2>
              </div>
            </RoleGuard>
          }
        />

        {/* STAFF routes */}
        <Route
          path="/prompt-templates"
          element={
            <RoleGuard roles={['STAFF', 'MANAGER', 'ADMIN']}>
              <div className="p-8">
                <h2 className="text-2xl font-bold">Staff – AI Prompt Templates</h2>
              </div>
            </RoleGuard>
          }
        />

        {/* TEACHER routes */}
        <Route
          path="/lesson-plans"
          element={
            <RoleGuard roles={['TEACHER']}>
              <div className="p-8">
                <h2 className="text-2xl font-bold">Teacher – Lesson Plans</h2>
              </div>
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
              <div className="p-8">
                <h2 className="text-2xl font-bold">Teacher – Exam Generator</h2>
              </div>
            </RoleGuard>
          }
        />
        <Route
          path="/ocr-grading"
          element={
            <RoleGuard roles={['TEACHER']}>
              <div className="p-8">
                <h2 className="text-2xl font-bold">Teacher – OCR Grading</h2>
              </div>
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
        <Route
          path="/question-bank/:bankId"
          element={
            <RoleGuard roles={['TEACHER', 'STAFF', 'MANAGER', 'ADMIN']}>
              <BankQuestionsPage />
            </RoleGuard>
          }
        />
      </Route>

      {/* Global toast */}
      <Toaster position="top-right" richColors closeButton />
    </Routes>
  )
}

export default App
