import {
  BookOpen,
  GraduationCap,
  TrendingUp,
  Users,
} from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'

// ---------------------------------------------------------------------------
// Stat cards data
// ---------------------------------------------------------------------------
const STATS = [
  {
    title: 'Tổng Người dùng',
    value: '—',
    description: 'Tất cả tài khoản trong hệ thống',
    icon: Users,
    color: 'text-emerald-600',
    bg: 'bg-emerald-50',
  },
  {
    title: 'Giáo viên',
    value: '—',
    description: 'Tài khoản với role TEACHER',
    icon: GraduationCap,
    color: 'text-blue-600',
    bg: 'bg-blue-50',
  },
  {
    title: 'Khóa học',
    value: 'N/A',
    description: 'Tính năng sắp ra mắt',
    icon: BookOpen,
    color: 'text-amber-600',
    bg: 'bg-amber-50',
  },
  {
    title: 'Tăng trưởng',
    value: 'N/A',
    description: 'So với tháng trước',
    icon: TrendingUp,
    color: 'text-purple-600',
    bg: 'bg-purple-50',
  },
]

// ---------------------------------------------------------------------------
// Quick links
// ---------------------------------------------------------------------------
const QUICK_LINKS = [
  {
    title: 'Quản lý Người dùng',
    description: 'Xem, tạo và phân quyền người dùng trong hệ thống.',
    href: '/users',
    badge: 'Hoạt động',
    badgeVariant: 'default',
  },
  {
    title: 'Cài đặt Hệ thống',
    description: 'Cấu hình các thông số chung của PlanbookAI.',
    href: '/settings',
    badge: 'Hoạt động',
    badgeVariant: 'default',
  },
  {
    title: 'Khóa học',
    description: 'Quản lý nội dung khóa học và tài liệu.',
    href: '/courses',
    badge: 'Sắp ra mắt',
    badgeVariant: 'secondary',
  },
  {
    title: 'Báo cáo',
    description: 'Thống kê và phân tích dữ liệu hoạt động.',
    href: '/reports',
    badge: 'Sắp ra mắt',
    badgeVariant: 'secondary',
  },
]

// ---------------------------------------------------------------------------
// Dashboard Page
// ---------------------------------------------------------------------------
export default function DashboardPage() {
  return (
    <div className="space-y-8">
      {/* Welcome header */}
      <div>
        <h1 className="page-header">Tổng quan</h1>
        <p className="page-subheader">
          Chào mừng trở lại! Đây là bảng điều khiển quản trị PlanbookAI.
        </p>
      </div>

      {/* Stats grid */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {STATS.map((stat) => (
          <div key={stat.title} className="stat-card flex items-start gap-4">
            <div className={`flex size-10 shrink-0 items-center justify-center rounded-lg ${stat.bg}`}>
              <stat.icon className={`size-5 ${stat.color}`} />
            </div>
            <div className="min-w-0">
              <p className="text-xs uppercase tracking-wide text-muted-foreground">{stat.title}</p>
              <p className="mt-0.5 text-2xl font-bold text-foreground">{stat.value}</p>
              <p className="mt-0.5 text-xs text-muted-foreground">{stat.description}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Quick access */}
      <div>
        <h2 className="mb-4 text-lg font-semibold text-foreground">Truy cập nhanh</h2>
        <div className="grid gap-4 sm:grid-cols-2">
          {QUICK_LINKS.map((link) => (
            <Card
              key={link.title}
              className="group cursor-pointer border transition-all duration-200 hover:border-primary/50 hover:shadow-md"
              onClick={() => window.location.href = link.href}
            >
              <CardHeader className="pb-2">
                <div className="flex items-start justify-between gap-2">
                  <CardTitle className="text-base font-semibold group-hover:text-primary transition-colors">
                    {link.title}
                  </CardTitle>
                  <Badge variant={link.badgeVariant} className="shrink-0 text-xs">
                    {link.badge}
                  </Badge>
                </div>
                <CardDescription>{link.description}</CardDescription>
              </CardHeader>
              <CardContent />
            </Card>
          ))}
        </div>
      </div>
    </div>
  )
}
