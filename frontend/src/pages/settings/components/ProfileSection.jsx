import { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Save, User } from 'lucide-react'
import { toast } from 'sonner'

import { saveProfile, resetSaveStatus } from '@/features/settings/settingsSlice'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'

// ── Helpers ───────────────────────────────────────────────────────────────

const ROLE_LABEL = {
  ADMIN:   'Quản trị viên',
  MANAGER: 'Quản lý',
  STAFF:   'Nhân viên',
  TEACHER: 'Giáo viên',
}

function initials(name) {
  if (!name) return '?'
  return name.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2)
}

function Field({ label, error, children }) {
  return (
    <div className="space-y-1.5">
      <Label className="text-sm font-medium">{label}</Label>
      {children}
      {error && <p className="text-xs text-destructive">{error}</p>}
    </div>
  )
}

// ── Main component ────────────────────────────────────────────────────────

export default function ProfileSection() {
  const dispatch = useDispatch()
  const user = useSelector((s) => s.auth.user)
  const { saveStatus, saveError } = useSelector((s) => s.settings)

  const [form, setForm]     = useState({ fullName: '', phone: '' })
  const [errors, setErrors] = useState({})
  const isSaving = saveStatus === 'loading'

  // Pre-fill form from Redux auth user
  useEffect(() => {
    if (user) setForm({ fullName: user.fullName ?? '', phone: user.phone ?? '' })
  }, [user])

  // Toast on save result
  useEffect(() => {
    if (saveStatus === 'succeeded') {
      toast.success('Cập nhật hồ sơ thành công')
      dispatch(resetSaveStatus())
    } else if (saveStatus === 'failed' && saveError) {
      toast.error(saveError)
      dispatch(resetSaveStatus())
    }
  }, [saveStatus, saveError, dispatch])

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }))

  const validate = () => {
    const errs = {}
    if (!form.fullName.trim()) errs.fullName = 'Họ và tên không được để trống'
    return errs
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    const errs = validate()
    setErrors(errs)
    if (Object.keys(errs).length) return
    dispatch(saveProfile({
      fullName: form.fullName.trim(),
      phone:    form.phone.trim() || null,
    }))
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2 text-sm font-semibold">
          <User className="size-4 text-primary" />
          Hồ sơ cá nhân
        </CardTitle>
        <CardDescription>Cập nhật tên hiển thị và số điện thoại của bạn</CardDescription>
      </CardHeader>

      <CardContent>
        {/* Avatar + summary */}
        <div className="flex items-center gap-4 pb-5">
          <Avatar className="size-16 rounded-xl">
            <AvatarImage src={user?.avatarUrl} alt={user?.fullName} />
            <AvatarFallback className="rounded-xl bg-primary/10 text-primary text-lg font-bold">
              {initials(user?.fullName)}
            </AvatarFallback>
          </Avatar>
          <div className="space-y-1">
            <p className="font-semibold text-foreground">{user?.fullName ?? '—'}</p>
            <p className="text-sm text-muted-foreground">{user?.email ?? '—'}</p>
            <Badge variant="secondary" className="text-xs">
              {ROLE_LABEL[user?.roleName] ?? user?.roleName ?? '—'}
            </Badge>
          </div>
        </div>

        <Separator className="mb-5" />

        {/* Edit form */}
        <form onSubmit={handleSubmit} noValidate className="max-w-md space-y-4">
          <Field label="Họ và tên *" error={errors.fullName}>
            <Input
              value={form.fullName}
              onChange={set('fullName')}
              placeholder="Nguyễn Văn A"
            />
          </Field>

          <Field label="Email (không thể thay đổi)">
            <Input value={user?.email ?? ''} disabled className="opacity-60 cursor-not-allowed" />
          </Field>

          <Field label="Số điện thoại">
            <Input
              value={form.phone}
              onChange={set('phone')}
              placeholder="0901 234 567"
            />
          </Field>

          {/* Actions */}
          <div className="flex justify-end pt-1">
            <Button type="submit" disabled={isSaving} className="gap-1.5" size="sm">
              <Save className="size-3.5" />
              {isSaving ? 'Đang lưu…' : 'Lưu thay đổi'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}
