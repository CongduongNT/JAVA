import React from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'

import MainLayout from './components/layout/MainLayout'
import ProtectedRoute from './components/ProtectedRoute'
import RoleGuard from './components/guards/RoleGuard'

import Login from './pages/LoginPage'
import Register from './pages/RegisterPage'
import UnauthorizedPage from './pages/UnauthorizedPage'
import DashboardPage from './pages/DashboardPage'

import ManagerSubscriptionsPage from './pages/manager/ManagerSubscriptionsPage'
import ManagerOrdersPage from './pages/manager/ManagerOrdersPage'
import ManagerQuestionApprovalPage from './pages/manager/ManagerQuestionApprovalPage'
import TeacherPackagesPage from './pages/teacher/TeacherPackagesPage'
import TeacherOrderHistoryPage from './pages/teacher/TeacherOrderHistoryPage'
import UsersPage from './pages/users/UsersPage'
import UserFormPage from './pages/users/UserFormPage'
import TeacherManagementPage from './pages/manager/TeacherManagementPage'
import SettingsPage from './pages/settings/SettingsPage'
import PromptTemplatesPage from './pages/staff/PromptTemplatesPage'
import PromptTemplateForm from './pages/staff/PromptTemplateForm'
import GenerateLessonPlan from './pages/teacher/GenerateLessonPlan'
import ExamGenerator from './pages/teacher/ExamGenerator'
import QuestionBankPage from './features/question-bank/QuestionBankPage'
import BankQuestionsPage from './features/question-bank/BankQuestionsPage'
import { Toaster } from 'sonner'

const NotFound = () => (
  <div className="flex h-screen flex-col items-center justify-center">
    <h1 className="text-4xl font-bold">404 - Not Found</h1>
    <p className="mt-2 text-muted-foreground">Trang ban tim kiem khong ton tai.</p>
  </div>
)

function App() {
  return (
    <>
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
                <TeacherManagementPage />
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
            path="/manager/questions/approval"
            element={
              <RoleGuard roles={['MANAGER', 'ADMIN']}>
                <ManagerQuestionApprovalPage />
              </RoleGuard>
            }
          />
          <Route
            path="/manager/approve"
            element={
              <RoleGuard roles={['MANAGER', 'ADMIN']}>
                <PromptTemplatesPage />
              </RoleGuard>
            }
          />
          <Route
            path="/manager/analytics"
            element={
              <RoleGuard roles={['MANAGER', 'ADMIN']}>
                <div className="p-8">
                  <h2 className="text-2xl font-bold">Manager – Analytics (Coming Soon)</h2>
                </div>
              </RoleGuard>
            }
          />

          <Route
            path="/prompt-templates"
            element={
              <RoleGuard roles={['STAFF', 'MANAGER', 'ADMIN']}>
                <PromptTemplatesPage />
              </RoleGuard>
            }
          />
          <Route
            path="/prompt-templates/new"
            element={
              <RoleGuard roles={['STAFF']}>
                <PromptTemplateForm />
              </RoleGuard>
            }
          />
          <Route
            path="/prompt-templates/:id/edit"
            element={
              <RoleGuard roles={['STAFF']}>
                <PromptTemplateForm />
              </RoleGuard>
            }
          />

          <Route
            path="/lesson-plans"
            element={
              <RoleGuard roles={['TEACHER', 'STAFF', 'MANAGER']}>
                <PromptTemplatesPage />
              </RoleGuard>
            }
          />
          <Route
            path="/generate-lesson-plan/:id"
            element={
              <RoleGuard roles={['TEACHER', 'STAFF', 'MANAGER']}>
                <GenerateLessonPlan />
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
                <ExamGenerator />
              </RoleGuard>
            }
          />
          <Route
            path="/ocr-grading"
            element={
              <RoleGuard roles={['TEACHER']}>
                <div className="p-8">
                  <h2 className="text-2xl font-bold">Teacher - OCR Grading</h2>
                </div>
              </RoleGuard>
            }
          />

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

        <Route path="*" element={<NotFound />} />
      </Routes>
      <Toaster position="top-right" richColors closeButton />
    </>
  )
}

export default App
