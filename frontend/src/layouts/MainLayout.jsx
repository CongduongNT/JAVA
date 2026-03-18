import { useDispatch, useSelector } from 'react-redux'
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom'
import {
  BarChart3,
  BookOpen,
  ChevronRight,
  GraduationCap,
  LayoutDashboard,
  LogOut,
  Settings,
  Users,
} from 'lucide-react'

import { logout } from '@/features/auth/authSlice'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
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
import {
  TooltipProvider,
} from '@/components/ui/tooltip'

// ---------------------------------------------------------------------------
// Navigation config
// ---------------------------------------------------------------------------
const NAV_MAIN = [
  {
    title: 'Tổng quan',
    href: '/',
    icon: LayoutDashboard,
  },
  {
    title: 'Quản lý Người dùng',
    href: '/users',
    icon: Users,
  },
  {
    title: 'Khóa học',
    href: '/courses',
    icon: BookOpen,
    disabled: true,
  },
  {
    title: 'Báo cáo',
    href: '/reports',
    icon: BarChart3,
    disabled: true,
  },
]

const NAV_SYSTEM = [
  {
    title: 'Cài đặt Hệ thống',
    href: '/settings',
    icon: Settings,
  },
]

// ---------------------------------------------------------------------------
// App Sidebar
// ---------------------------------------------------------------------------
function AppSidebar() {
  const location = useLocation()
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const user = useSelector((state) => state.auth.user)

  const isActive = (href) => {
    if (href === '/') return location.pathname === '/'
    return location.pathname.startsWith(href)
  }

  const handleLogout = () => {
    dispatch(logout())
    navigate('/login')
  }

  const initials = user?.fullName
    ? user.fullName.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2)
    : 'AD'

  return (
    <Sidebar collapsible="icon" className="border-r-0">
      {/* Logo */}
      <SidebarHeader className="h-16 flex-row items-center gap-3 px-4">
        <div className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground shadow">
          <GraduationCap className="size-5" />
        </div>
        <div className="grid flex-1 text-left leading-tight group-data-[collapsible=icon]:hidden">
          <span className="truncate text-base font-bold text-sidebar-foreground">
            PlanbookAI
          </span>
          <span className="truncate text-xs text-sidebar-accent-foreground">
            Nền tảng LMS
          </span>
        </div>
      </SidebarHeader>

      <Separator className="bg-sidebar-border" />

      <SidebarContent className="py-2">
        {/* Main navigation */}
        <SidebarGroup>
          <SidebarGroupLabel className="text-xs uppercase tracking-widest text-sidebar-accent-foreground px-3 pb-1">
            Điều hướng
          </SidebarGroupLabel>
          <SidebarMenu>
            {NAV_MAIN.map((item) => (
              <SidebarMenuItem key={item.href}>
                <SidebarMenuButton
                  render={item.disabled ? undefined : <Link to={item.href} />}
                  isActive={isActive(item.href)}
                  tooltip={item.title}
                  className={
                    item.disabled
                      ? 'cursor-not-allowed opacity-50'
                      : 'transition-colors duration-150'
                  }
                >
                  <item.icon className="size-4 shrink-0" />
                  <span className="truncate">{item.title}</span>
                  {item.disabled && (
                    <span className="ml-auto text-[10px] opacity-60 group-data-[collapsible=icon]:hidden">
                      Soon
                    </span>
                  )}
                </SidebarMenuButton>
              </SidebarMenuItem>
            ))}
          </SidebarMenu>
        </SidebarGroup>

        {/* System navigation */}
        <SidebarGroup className="mt-auto">
          <SidebarGroupLabel className="text-xs uppercase tracking-widest text-sidebar-accent-foreground px-3 pb-1">
            Hệ thống
          </SidebarGroupLabel>
          <SidebarMenu>
            {NAV_SYSTEM.map((item) => (
              <SidebarMenuItem key={item.href}>
                <SidebarMenuButton
                  render={<Link to={item.href} />}
                  isActive={isActive(item.href)}
                  tooltip={item.title}
                  className="transition-colors duration-150"
                >
                  <item.icon className="size-4 shrink-0" />
                  <span className="truncate">{item.title}</span>
                </SidebarMenuButton>
              </SidebarMenuItem>
            ))}
          </SidebarMenu>
        </SidebarGroup>
      </SidebarContent>

      {/* User footer */}
      <Separator className="bg-sidebar-border" />
      <SidebarFooter className="py-3">
        <SidebarMenu>
          <SidebarMenuItem>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <SidebarMenuButton
                  size="lg"
                  className="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
                  tooltip={user?.fullName || 'Tài khoản'}
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
                    <span className="truncate text-xs text-sidebar-accent-foreground">
                      {user?.email || 'admin@planbookai.com'}
                    </span>
                  </div>
                  <ChevronRight className="ml-auto size-4 text-sidebar-accent-foreground group-data-[collapsible=icon]:hidden" />
                </SidebarMenuButton>
              </DropdownMenuTrigger>
              <DropdownMenuContent
                className="w-56 rounded-lg"
                side="right"
                align="end"
                sideOffset={8}
              >
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
                  Đăng xuất
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

// ---------------------------------------------------------------------------
// Breadcrumb helper
// ---------------------------------------------------------------------------
const ROUTE_LABELS = {
  '/': 'Tổng quan',
  '/users': 'Quản lý Người dùng',
  '/users/new': 'Tạo người dùng',
  '/settings': 'Cài đặt Hệ thống',
  '/courses': 'Khóa học',
  '/reports': 'Báo cáo',
}

function useBreadcrumb(pathname) {
  const segments = pathname === '/' ? ['/'] : pathname.split('/').filter(Boolean)
  const crumbs = []

  if (pathname.includes('/edit')) {
    crumbs.push({ label: 'Quản lý Người dùng', href: '/users' })
    crumbs.push({ label: 'Chỉnh sửa người dùng', href: null })
    return crumbs
  }

  let path = ''
  for (const seg of segments) {
    path = path ? `${path}/${seg}` : `/${seg}`
    const label = ROUTE_LABELS[path]
    if (label) crumbs.push({ label, href: path })
  }

  if (crumbs.length === 0) crumbs.push({ label: 'Tổng quan', href: '/' })
  return crumbs
}

// ---------------------------------------------------------------------------
// Top Header
// ---------------------------------------------------------------------------
function TopHeader() {
  const location = useLocation()
  const crumbs = useBreadcrumb(location.pathname)
  const currentPage = crumbs[crumbs.length - 1]?.label ?? 'Tổng quan'

  return (
    <header className="flex h-16 shrink-0 items-center gap-3 border-b bg-card px-4 shadow-xs">
      <SidebarTrigger className="-ml-1 text-muted-foreground hover:text-foreground hover:bg-muted" />
      <Separator orientation="vertical" className="mx-1 h-5" />

      {/* Breadcrumb */}
      <nav className="flex items-center gap-1 text-sm" aria-label="Breadcrumb">
        {crumbs.map((crumb, idx) => (
          <span key={idx} className="flex items-center gap-1">
            {idx > 0 && <ChevronRight className="size-3.5 text-muted-foreground" />}
            {crumb.href && idx < crumbs.length - 1 ? (
              <Link
                to={crumb.href}
                className="text-muted-foreground hover:text-foreground transition-colors"
              >
                {crumb.label}
              </Link>
            ) : (
              <span className="font-medium text-foreground">{crumb.label}</span>
            )}
          </span>
        ))}
      </nav>

      <div className="ml-auto flex items-center gap-2">
        <span className="hidden text-xs text-muted-foreground md:inline-block">
          PlanbookAI Admin
        </span>
      </div>
    </header>
  )
}

// ---------------------------------------------------------------------------
// Footer
// ---------------------------------------------------------------------------
function AppFooter() {
  return (
    <footer className="border-t bg-card px-6 py-3">
      <div className="flex flex-col items-center justify-between gap-1 sm:flex-row">
        <p className="text-xs text-muted-foreground">
          © {new Date().getFullYear()} PlanbookAI. Nền tảng học tập thông minh cho giáo viên.
        </p>
        <p className="text-xs text-muted-foreground">
          Phiên bản 1.0.0
        </p>
      </div>
    </footer>
  )
}

// ---------------------------------------------------------------------------
// Main Layout
// ---------------------------------------------------------------------------
export default function MainLayout() {
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
