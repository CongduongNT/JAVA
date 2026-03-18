import { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Eye, EyeOff, KeyRound, Save } from 'lucide-react'
import { toast } from 'sonner'

import { changePassword, resetPasswordStatus } from '@/features/settings/settingsSlice'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'

// ── Helpers ───────────────────────────────────────────────────────────────

const EMPTY = { currentPassword: '', newPassword: '', confirmPassword: '' }

function validate(form) {
  const errs = {}
  if (!form.currentPassword)            errs.currentPassword = 'Vui lòng nhập mật khẩu hiện tại'
  if (!form.newPassword)                errs.newPassword     = 'Vui lòng nhập mật khẩu mới'
  else if (form.newPassword.length < 6) errs.newPassword     = 'Mật khẩu tối thiểu 6 ký tự'
  if (!form.confirmPassword)            errs.confirmPassword = 'Vui lòng xác nhận mật khẩu mới'
  else if (form.confirmPassword !== form.newPassword)
                                        errs.confirmPassword = 'Mật khẩu xác nhận không khớp'
  return errs
}

// ── Password input with show/hide toggle ─────────────────────────────────

function PasswordInput({ value, onChange, placeholder, id }) {
  const [show, setShow] = useState(false)
  return (
    <div className="relative">
      <Input
        id={id}
        type={show ? 'text' : 'password'}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        autoComplete="new-password"
        className="pr-9"
      />
      <button
        type="button"
        onClick={() => setShow((s) => !s)}
        className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
        aria-label={show ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
      >
        {show ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
      </button>
    </div>
  )
}

function Field({ label, htmlFor, error, children }) {
  return (
    <div className="space-y-1.5">
      <Label htmlFor={htmlFor} className="text-sm font-medium">{label}</Label>
      {children}
      {error && <p className="text-xs text-destructive">{error}</p>}
    </div>
  )
}

// ── Main component ────────────────────────────────────────────────────────

export default function SecuritySection() {
  const dispatch = useDispatch()
  const { passwordStatus, passwordError } = useSelector((s) => s.settings)

  const [form, setForm]     = useState(EMPTY)
  const [errors, setErrors] = useState({})
  const isSaving = passwordStatus === 'loading'

  // Toast on result
  useEffect(() => {
    if (passwordStatus === 'succeeded') {
      toast.success('Đổi mật khẩu thành công')
      setForm(EMPTY)
      dispatch(resetPasswordStatus())
    } else if (passwordStatus === 'failed' && passwordError) {
      toast.error(passwordError)
      dispatch(resetPasswordStatus())
    }
  }, [passwordStatus, passwordError, dispatch])

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }))

  const handleSubmit = (e) => {
    e.preventDefault()
    const errs = validate(form)
    setErrors(errs)
    if (Object.keys(errs).length) return
    dispatch(changePassword({
      currentPassword: form.currentPassword,
      newPassword:     form.newPassword,
    }))
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2 text-sm font-semibold">
          <KeyRound className="size-4 text-primary" />
          Đổi mật khẩu
        </CardTitle>
        <CardDescription>
          Để bảo mật tài khoản, hãy sử dụng mật khẩu mạnh và không chia sẻ với người khác
        </CardDescription>
      </CardHeader>

      <CardContent>
        <form onSubmit={handleSubmit} noValidate className="max-w-md space-y-4">
          <Field label="Mật khẩu hiện tại *" htmlFor="currentPassword" error={errors.currentPassword}>
            <PasswordInput
              id="currentPassword"
              value={form.currentPassword}
              onChange={set('currentPassword')}
              placeholder="Nhập mật khẩu hiện tại"
            />
          </Field>

          <Separator />

          <Field label="Mật khẩu mới *" htmlFor="newPassword" error={errors.newPassword}>
            <PasswordInput
              id="newPassword"
              value={form.newPassword}
              onChange={set('newPassword')}
              placeholder="Tối thiểu 6 ký tự"
            />
          </Field>

          <Field label="Xác nhận mật khẩu mới *" htmlFor="confirmPassword" error={errors.confirmPassword}>
            <PasswordInput
              id="confirmPassword"
              value={form.confirmPassword}
              onChange={set('confirmPassword')}
              placeholder="Nhập lại mật khẩu mới"
            />
          </Field>

          <div className="flex justify-end pt-1">
            <Button type="submit" disabled={isSaving} className="gap-1.5" size="sm">
              <Save className="size-3.5" />
              {isSaving ? 'Đang lưu…' : 'Đổi mật khẩu'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}
