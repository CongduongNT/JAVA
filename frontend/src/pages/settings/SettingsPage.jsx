import { Activity, CheckCircle2, Server, Shield, User, XCircle } from 'lucide-react'
import { useSelector } from 'react-redux'

import ProfileSection from './components/ProfileSection'
import SecuritySection from './components/SecuritySection'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'

// ── Static data ───────────────────────────────────────────────────────────

const PLATFORM_INFO = [
  { label: 'Tên hệ thống',  value: 'PlanbookAI' },
  { label: 'Phiên bản',     value: '1.0.0' },
  { label: 'Backend',       value: 'Spring Boot 3.x' },
  { label: 'Frontend',      value: 'React 19 + Vite 6' },
  { label: 'Cơ sở dữ liệu', value: 'MySQL 8.x' },
  { label: 'AI Engine',     value: 'Google Gemini AI' },
  { label: 'Lưu trữ',       value: 'Supabase Storage' },
  { label: 'Triển khai',    value: 'Docker · AWS ECS' },
]

const SERVICES = [
  { name: 'API Server (Spring Boot)', key: 'api' },
  { name: 'Cơ sở dữ liệu (MySQL)',   key: 'db' },
  { name: 'Gemini AI',               key: 'ai' },
  { name: 'Supabase Storage',        key: 'storage' },
]

// ── Helpers ───────────────────────────────────────────────────────────────

function StatusBadge({ online }) {
  return online ? (
    <Badge className="bg-emerald-100 text-emerald-700 hover:bg-emerald-100 gap-1 text-xs">
      <CheckCircle2 className="size-3" />Hoạt động
    </Badge>
  ) : (
    <Badge className="bg-red-100 text-red-600 hover:bg-red-100 gap-1 text-xs">
      <XCircle className="size-3" />Offline
    </Badge>
  )
}

// ── System Overview Tab ───────────────────────────────────────────────────

function SystemOverviewTab() {
  // Derive basic "online" state: if the users list loaded successfully,
  // the API + DB are reachable. Falls back to true (optimistic).
  const usersStatus = useSelector((s) => s.users.status)
  const apiOnline = usersStatus !== 'failed'

  const serviceStatus = {
    api:     apiOnline,
    db:      apiOnline,
    ai:      true,       // No live ping endpoint yet — show optimistic
    storage: true,
  }

  return (
    <div className="space-y-4">
      {/* Platform info card */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-sm font-semibold">
            <Server className="size-4 text-primary" />
            Thông tin nền tảng
          </CardTitle>
          <CardDescription>Thông tin cơ bản về cấu hình hệ thống PlanbookAI</CardDescription>
        </CardHeader>
        <CardContent>
          <dl className="grid grid-cols-1 gap-y-3 gap-x-8 sm:grid-cols-2">
            {PLATFORM_INFO.map(({ label, value }) => (
              <div key={label} className="flex flex-col gap-0.5">
                <dt className="text-[11px] font-medium uppercase tracking-wider text-muted-foreground">
                  {label}
                </dt>
                <dd className="text-sm font-medium text-foreground">{value}</dd>
              </div>
            ))}
          </dl>
        </CardContent>
      </Card>

      {/* Service status card */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-sm font-semibold">
            <Activity className="size-4 text-primary" />
            Trạng thái dịch vụ
          </CardTitle>
          <CardDescription>Giám sát trạng thái các thành phần hệ thống</CardDescription>
        </CardHeader>
        <CardContent>
          <ul className="divide-y divide-border">
            {SERVICES.map(({ name, key }) => (
              <li key={key} className="flex items-center justify-between py-2.5">
                <span className="text-sm">{name}</span>
                <StatusBadge online={serviceStatus[key]} />
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>
    </div>
  )
}

// ── Coming Soon placeholder ───────────────────────────────────────────────

function ComingSoonTab({ label, icon: Icon }) {
  return (
    <Card>
      <CardContent className="flex flex-col items-center gap-2 py-14 text-center">
        <Icon className="size-8 text-muted-foreground/40" />
        <p className="text-sm text-muted-foreground">
          Phần <strong className="text-foreground">{label}</strong> sẽ được triển khai ở bước tiếp theo.
        </p>
      </CardContent>
    </Card>
  )
}

// ── Main page ─────────────────────────────────────────────────────────────

export default function SettingsPage() {
  return (
    <div className="space-y-5">
      {/* Page header */}
      <div>
        <h1 className="page-header">Cài đặt Hệ thống</h1>
        <p className="page-subheader">Quản lý thông tin hệ thống, hồ sơ cá nhân và bảo mật tài khoản</p>
      </div>

      <Separator />

      <Tabs defaultValue="system" className="space-y-4">
        <TabsList className="h-9">
          <TabsTrigger value="system" className="gap-1.5 text-xs">
            <Server className="size-3.5" />
            Tổng quan
          </TabsTrigger>
          <TabsTrigger value="profile" className="gap-1.5 text-xs">
            <User className="size-3.5" />
            Hồ sơ cá nhân
          </TabsTrigger>
          <TabsTrigger value="security" className="gap-1.5 text-xs">
            <Shield className="size-3.5" />
            Bảo mật
          </TabsTrigger>
        </TabsList>

        <TabsContent value="system">
          <SystemOverviewTab />
        </TabsContent>

        <TabsContent value="profile">
          <ProfileSection />
        </TabsContent>

        <TabsContent value="security">
          <SecuritySection />
        </TabsContent>
      </Tabs>
    </div>
  )
}
