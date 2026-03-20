import { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Save } from 'lucide-react'

import { createUser, fetchUserById, resetCurrent, updateUser } from '@/features/users/usersSlice'
import { ROLES } from '@/services/userService'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Separator } from '@/components/ui/separator'
import { Skeleton } from '@/components/ui/skeleton'

function Field({ label, required, error, children }) {
  return (
    <div className="space-y-1.5">
      <Label className="text-sm font-medium">
        {label} {required && <span className="text-destructive">*</span>}
      </Label>
      {children}
      {error && <p className="text-xs text-destructive">{error}</p>}
    </div>
  )
}

function validate(form, isEdit) {
  const errors = {}
  if (!form.fullName.trim()) errors.fullName = 'Họ và tên không được để trống'
  if (!form.email.trim()) errors.email = 'Email không được để trống'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) errors.email = 'Email không hợp lệ'
  if (!isEdit && !form.password) errors.password = 'Mật khẩu không được để trống'
  if (form.password && form.password.length < 6) errors.password = 'Mật khẩu tối thiểu 6 ký tự'
  if (!form.roleId) errors.roleId = 'Vui lòng chọn vai trò'
  return errors
}

export default function UserFormPage() {
  const { id } = useParams()
  const isEdit = !!id
  const dispatch = useDispatch()
  const navigate = useNavigate()

  const { current, currentStatus, submitStatus, submitError } = useSelector((s) => s.users)

  const [form, setForm] = useState({ fullName: '', email: '', password: '', phone: '', roleId: '' })
  const [errors, setErrors] = useState({})

  const isLoadingUser = isEdit && currentStatus === 'loading'
  const isSubmitting = submitStatus === 'loading'

  useEffect(() => {
    if (isEdit) dispatch(fetchUserById(id))
    return () => { dispatch(resetCurrent()) }
  }, [dispatch, id, isEdit])

  useEffect(() => {
    if (current && isEdit) {
      const matchedRole = ROLES.find((r) => r.name === current.roleName)
      setForm({
        fullName: current.fullName ?? '',
        email: current.email ?? '',
        password: '',
        phone: current.phone ?? '',
        roleId: matchedRole ? String(matchedRole.id) : '',
      })
    }
  }, [current, isEdit])

  useEffect(() => {
    if (submitStatus === 'succeeded') {
      navigate('/admin/users')
    }
  }, [submitStatus, navigate])

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e?.target?.value ?? e }))

  const handleSubmit = (e) => {
    e.preventDefault()
    const errs = validate(form, isEdit)
    setErrors(errs)
    if (Object.keys(errs).length) return

    const payload = {
      fullName: form.fullName.trim(),
      email: form.email.trim(),
      phone: form.phone.trim() || null,
      roleId: Number(form.roleId),
      ...(form.password ? { password: form.password } : {}),
    }

    if (isEdit) dispatch(updateUser({ id: Number(id), data: payload }))
    else dispatch(createUser(payload))
  }

  return (
    <div className="mx-auto max-w-xl space-y-6">
      <div className="flex items-center gap-3">
        <Button variant="ghost" size="icon-sm" onClick={() => navigate('/admin/users')} aria-label="Quay lại">
          <ArrowLeft className="size-4" />
        </Button>
        <div>
          <h1 className="page-header">{isEdit ? 'Chỉnh sửa người dùng' : 'Tạo người dùng'}</h1>
          <p className="page-subheader">
            {isEdit ? `Đang sửa: ${current?.fullName ?? '…'}` : 'Điền thông tin để tạo tài khoản mới'}
          </p>
        </div>
      </div>

      <Separator />

      <div className="rounded-xl border bg-card p-6 shadow-xs">
        {isLoadingUser ? (
          <div className="space-y-5">
            {[1, 2, 3, 4, 5].map((i) => <Skeleton key={i} className="h-9 w-full" />)}
          </div>
        ) : (
          <form onSubmit={handleSubmit} noValidate className="space-y-5">
            <Field label="Họ và tên" required error={errors.fullName}>
              <Input placeholder="Nguyễn Văn A" value={form.fullName} onChange={set('fullName')} />
            </Field>

            <Field label="Email" required error={errors.email}>
              <Input type="email" placeholder="user@example.com" value={form.email} onChange={set('email')} disabled={isEdit} />
            </Field>

            <Field label={isEdit ? 'Mật khẩu mới (để trống nếu không đổi)' : 'Mật khẩu'} required={!isEdit} error={errors.password}>
              <Input
                type="password"
                placeholder={isEdit ? '•••••••• (không bắt buộc)' : '••••••••'}
                value={form.password}
                onChange={set('password')}
                autoComplete="new-password"
              />
            </Field>

            <Field label="Số điện thoại" error={errors.phone}>
              <Input placeholder="0901 234 567" value={form.phone} onChange={set('phone')} />
            </Field>

            <Field label="Vai trò" required error={errors.roleId}>
              <Select value={form.roleId} onValueChange={set('roleId')}>
                <SelectTrigger>
                  <SelectValue placeholder="Chọn vai trò..." />
                </SelectTrigger>
                <SelectContent>
                  {ROLES.map((r) => (
                    <SelectItem key={r.id} value={String(r.id)}>
                      {r.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </Field>

            {submitError && (
              <p className="rounded-lg bg-destructive/10 px-3 py-2 text-sm text-destructive">{submitError}</p>
            )}

            <div className="flex justify-end gap-2 pt-2">
              <Button type="button" variant="outline" onClick={() => navigate('/admin/users')} disabled={isSubmitting}>
                Huỷ
              </Button>
              <Button type="submit" disabled={isSubmitting} className="gap-1.5">
                <Save className="size-4" />
                {isSubmitting ? 'Đang lưu…' : isEdit ? 'Cập nhật' : 'Tạo người dùng'}
              </Button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
