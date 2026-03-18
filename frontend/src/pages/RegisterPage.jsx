import { Link } from 'react-router-dom'
import { GraduationCap } from 'lucide-react'

// Placeholder — to be implemented in a future task
export default function RegisterPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-[#EEF0F8] px-4">
      <div className="w-full max-w-md rounded-2xl bg-white shadow-lg p-10 text-center">
        <div className="flex size-12 mx-auto items-center justify-center rounded-xl bg-primary/10 mb-4">
          <GraduationCap className="size-6 text-primary" />
        </div>
        <h1 className="text-2xl font-bold text-primary mb-1">PlanbookAI</h1>
        <h2 className="text-lg font-semibold text-foreground mb-2">Đăng ký tài khoản</h2>
        <p className="text-sm text-muted-foreground mb-6">
          Tính năng đăng ký đang được phát triển. Vui lòng liên hệ quản trị viên.
        </p>
        <Link
          to="/login"
          className="text-sm font-medium text-primary hover:underline"
        >
          ← Quay lại đăng nhập
        </Link>
      </div>
    </div>
  )
}
