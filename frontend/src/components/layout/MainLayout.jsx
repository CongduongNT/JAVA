import React from 'react'
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import {
  AlignLeft,
  BarChart3,
  BookCopy,
  BookOpen,
  Bot,
  FileText,
  LayoutDashboard,
  LogOut,
  Package,
  Settings,
  Shield,
  Users,
  Loader2,
} from 'lucide-react'

import { logout } from '../../features/auth/authSlice'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Separator } from '@/components/ui/separator'
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarInset,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarProvider,
  SidebarRail,
  SidebarTrigger,
} from '@/components/ui/sidebar'
import { TooltipProvider } from '@/components/ui/tooltip'

const NAV_ITEMS = [
  { title: 'Dashboard', href: '/dashboard', icon: LayoutDashboard, roles: [] },
  { title: 'My Lesson Plans', href: '/lesson-plans', icon: BookOpen, roles: ['TEACHER'] },
  { title: 'Packages', href: '/packages', icon: Package, roles: ['TEACHER'] },
  { title: 'Order History', href: '/orders/history', icon: Settings, roles: ['TEACHER'] },
  { title: 'Question Bank', href: '/question-bank', icon: BookCopy, roles: ['TEACHER', 'STAFF', 'MANAGER', 'ADMIN'] },
  { title: 'Exam Generator', href: '/exam-generator', icon: FileText, roles: ['TEACHER'] },
  { title: 'OCR Grading', href: '/ocr-grading', icon: Bot, roles: ['TEACHER'] },
  { title: 'Prompt Templates', href: '/prompt-templates', icon: Settings, roles: ['STAFF', 'MANAGER', 'ADMIN'] },
  { title: 'Manage Teachers', href: '/manager/teachers', icon: Users, roles: ['MANAGER', 'ADMIN'] },
  { title: 'Manager Packages', href: '/manager/subscriptions', icon: Package, roles: ['MANAGER', 'ADMIN'] },
  { title: 'Orders', href: '/manager/orders', icon: BookCopy, roles: ['MANAGER', 'ADMIN'] },
  { title: 'Analytics', href: '/manager/analytics', icon: BarChart3, roles: ['MANAGER', 'ADMIN'] },
  { title: 'User Management', href: '/admin/users', icon: Shield, roles: ['ADMIN'] },
  { title: 'System Settings', href: '/settings', icon: Settings, roles: ['ADMIN'] },
]

const ROLE_LABELS = {
  ADMIN: { label: 'Admin', color: 'bg-red-100 text-red-700' },
  MANAGER: { label: 'Manager', color: 'bg-purple-100 text-purple-700' },
  STAFF: { label: 'Staff', color: 'bg-blue-100 text-blue-700' },
  TEACHER: { label: 'Teacher', color: 'bg-green-100 text-green-700' },
}

function useBreadcrumb(pathname) {
  if (pathname.startsWith('/question-bank')) return 'Question Bank'
  if (pathname === '/lesson-plans/ai-generator') return 'Tạo giáo án với AI'
  if (pathname === '/lesson-plans/new') return 'Tạo giáo án'
  if (/^\/lesson-plans\/\d+\/edit$/.test(pathname)) return 'Chỉnh sửa giáo án'
  if (/^\/lesson-plans\/\d+$/.test(pathname)) return 'Chi tiết giáo án'
  if (pathname.startsWith('/lesson-plans')) return 'Lesson Plans'
  const map = {
    '/dashboard': 'Dashboard',
    '/packages': 'Packages',
    '/orders/history': 'Orders',
    '/question-bank': 'Question Bank',
    '/exam-generator': 'Exam Generator',
    '/ocr-grading': 'OCR Grading',
    '/prompt-templates': 'Prompt Templates',
    '/manager/teachers': 'Manage Teachers',
    '/manager/subscriptions': 'Manager Packages',
    '/manager/orders': 'Orders',
    '/manager/analytics': 'Analytics',
    '/manager/approve': 'Approve Templates',
    '/admin/users': 'User Management',
  }
  return map[pathname] || 'Dashboard'
}

function AppSidebar() {
  const location = useLocation()
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const user = useSelector((state) => state.auth?.user)
  const rawRole = user?.roleName || (typeof user?.role === 'object' ? user?.role?.name : user?.role);
  const role = rawRole?.toUpperCase() || '';

  const isActive = (href) => {
    if (href === '/dashboard') return location.pathname === '/dashboard'
    return location.pathname.startsWith(href)
  }

  const initials = user?.fullName
    ? user.fullName.trim().split(/\s+/).map((n) => n[0]).join('').toUpperCase().slice(0, 2)
    : 'AD'

  const handleLogout = () => {
    dispatch(logout())
    navigate('/login')
  }

  const visibleItems = NAV_ITEMS.filter((item) => {
    if (!item.roles || item.roles.length === 0) return true
    return role && item.roles.map((r) => r.toUpperCase().replace('ROLE_', '')).includes(role)
  })

  const currentRoleKey = role; // Đã chuẩn hóa ở trên
  const roleInfo = ROLE_LABELS[currentRoleKey] || { label: role || 'User', color: 'bg-slate-100 text-slate-600' }

  return (
    <Sidebar collapsible="icon" className="border-r-0">
      <SidebarHeader className="h-16 flex-row items-center gap-3 px-4">
        <div className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground shadow">
          <AlignLeft className="size-5" />
        </div>
        <div className="grid flex-1 text-left leading-tight group-data-[collapsible=icon]:hidden">
          <span className="truncate text-base font-bold text-sidebar-foreground">PlanbookAI</span>
          <span className="truncate text-xs text-sidebar-accent-foreground">LMS Platform</span>
        </div>
      </SidebarHeader>

      <Separator className="bg-sidebar-border" />

      <SidebarContent className="py-2">
        <SidebarGroup>
          <SidebarGroupLabel className="px-3 pb-1">Navigation</SidebarGroupLabel>
          <SidebarMenu>
            {visibleItems.map((item) => (
              <SidebarMenuItem key={item.href}>
                <SidebarMenuButton
                  asChild
                  isActive={isActive(item.href)}
                  tooltip={item.title}
                  className="transition-colors duration-150"
                >
                  <Link to={item.href}>
                    <item.icon className="size-4 shrink-0" />
                    <span className="truncate">{item.title}</span>
                  </Link>
                </SidebarMenuButton>
              </SidebarMenuItem>
            ))}
          </SidebarMenu>
        </SidebarGroup>
      </SidebarContent>

      <Separator className="bg-sidebar-border" />
      <SidebarFooter className="py-3">
        <SidebarMenu>
          <SidebarMenuItem>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <SidebarMenuButton
                  size="lg"
                  className="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
                  tooltip={user?.fullName || 'Account'}
                >
                  <Avatar className="size-8 rounded-lg">
                    <AvatarImage src={user?.avatarUrl} alt={user?.fullName} />
                    <AvatarFallback className="rounded-lg bg-sidebar-primary text-sidebar-primary-foreground text-xs font-semibold">
                      {initials}
                    </AvatarFallback>
                  </Avatar>
                  <div className="grid flex-1 text-left text-sm leading-tight group-data-[collapsible=icon]:hidden">
                    <span className="truncate font-semibold text-sidebar-foreground">
                      {user?.fullName || 'Admin'}
                    </span>
                    <span className="truncate text-[11px] text-sidebar-accent-foreground">
                      {user?.email || 'admin@planbookai.com'}
                    </span>
                    {role && (
                      <span className={`mt-1 inline-flex w-fit rounded px-1.5 py-0.5 text-[10px] font-semibold ${roleInfo.color}`}>
                        {roleInfo.label}
                      </span>
                    )}
                  </div>
                </SidebarMenuButton>
              </DropdownMenuTrigger>
              <DropdownMenuContent className="w-56 rounded-lg" side="right" align="end" sideOffset={8}>
                <DropdownMenuLabel className="font-normal">
                  <div className="flex flex-col gap-0.5">
                    <p className="text-sm font-semibold">{user?.fullName || 'Admin'}</p>
                    <p className="text-xs text-muted-foreground">{user?.email || 'admin@planbookai.com'}</p>
                  </div>
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem
                  className="text-destructive focus:text-destructive cursor-pointer"
                  onClick={handleLogout}
                >
                  <LogOut className="mr-2 size-4" />
                  Logout
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarFooter>

      <SidebarRail />
    </Sidebar>
  )
}

function TopHeader() {
  const location = useLocation()
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const user = useSelector((state) => state.auth?.user)
  const breadcrumb = useBreadcrumb(location.pathname)

  const handleLogout = () => {
    dispatch(logout())
    navigate('/login')
  }

  return (
    <header className="flex h-16 shrink-0 items-center gap-3 border-b bg-card px-4 shadow-sm">
      <SidebarTrigger className="-ml-1 text-muted-foreground hover:text-foreground hover:bg-muted" />
      <Separator orientation="vertical" className="mx-1 h-5" />
      <div className="flex items-center gap-2 text-sm">
        <span className="font-semibold text-foreground">{breadcrumb}</span>
      </div>
      <div className="ml-auto flex items-center gap-2 text-sm text-muted-foreground">
        <span className="hidden md:inline-block">{user?.email || 'admin@planbookai.com'}</span>
        <button
          onClick={handleLogout}
          className="inline-flex items-center gap-1 rounded-md border border-border px-3 py-1.5 text-xs font-medium text-foreground hover:bg-muted"
        >
          <LogOut className="size-4" />
          Logout
        </button>
      </div>
    </header>
  )
}

function AppFooter() {
  return (
    <footer className="border-t bg-card px-6 py-3">
      <div className="flex flex-col items-center justify-between gap-1 sm:flex-row">
        <p className="text-xs text-muted-foreground">
          © {new Date().getFullYear()} PlanbookAI. Nền tảng học tập cho giáo viên.
        </p>
        <p className="text-xs text-muted-foreground">Phiên bản 1.0.0</p>
      </div>
    </footer>
  )
}

export default function MainLayout() {
  // Kiểm tra an toàn: nếu state._persist tồn tại thì mới đợi rehydrated, nếu không thì render luôn
  const isRehydrated = useSelector((state) => state._persist?.rehydrated ?? true);

  if (!isRehydrated) {
    return (
      <div className="flex h-screen w-full items-center justify-center">
        <Loader2 className="size-10 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <TooltipProvider delayDuration={300}>
      <SidebarProvider>
        <AppSidebar />
        <SidebarInset className="flex min-h-screen flex-col bg-background">
          <TopHeader />
          <main className="flex-1 overflow-auto p-6">
            <Outlet />
          </main>
          <AppFooter />
        </SidebarInset>
      </SidebarProvider>
    </TooltipProvider>
  )
}
