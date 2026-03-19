import { useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import { Pencil, Trash2 } from 'lucide-react'

import { deleteUser } from '@/features/users/usersSlice'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'

const ROLE_VARIANT = {
  ADMIN: 'default',
  MANAGER: 'secondary',
  STAFF: 'outline',
  TEACHER: 'outline',
}

const ROLE_LABEL = { ADMIN: 'Admin', MANAGER: 'Manager', STAFF: 'Staff', TEACHER: 'Teacher' }

function RoleBadge({ role }) {
  return (
    <Badge variant={ROLE_VARIANT[role] ?? 'outline'} className="capitalize text-xs">
      {ROLE_LABEL[role] ?? role ?? '—'}
    </Badge>
  )
}

function StatusBadge({ isActive }) {
  return isActive ? (
    <Badge className="bg-emerald-100 text-emerald-700 hover:bg-emerald-100 text-xs">Hoạt động</Badge>
  ) : (
    <Badge variant="secondary" className="text-xs">
      Khoá
    </Badge>
  )
}

function initials(name) {
  if (!name) return '?'
  return name
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
}

function formatDate(dateString) {
  if (!dateString) return '—'
  const d = new Date(dateString)
  if (Number.isNaN(d.getTime())) return '—'
  return d.toLocaleDateString('vi-VN')
}

function TableSkeleton() {
  return Array.from({ length: 5 }).map((_, i) => (
    <TableRow key={i}>
      {[40, 200, 180, 100, 90, 80, 120].map((w, j) => (
        <TableCell key={j}>
          <Skeleton className="h-4" style={{ width: w }} />
        </TableCell>
      ))}
      <TableCell>
        <Skeleton className="h-7 w-16" />
      </TableCell>
    </TableRow>
  ))
}

export default function UserTable({ users, isLoading }) {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const deletingId = useSelector((s) => s.users.deletingId)

  const [confirmId, setConfirmId] = useState(null)
  const confirmUser = users?.find((u) => u.id === confirmId)

  const handleDelete = () => {
    if (confirmId) dispatch(deleteUser(confirmId))
    setConfirmId(null)
  }

  return (
    <>
      <div className="rounded-lg border bg-card overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/40">
              <TableHead className="w-10 text-center">#</TableHead>
              <TableHead>Người dùng</TableHead>
              <TableHead>Email</TableHead>
              <TableHead>Số điện thoại</TableHead>
              <TableHead>Role</TableHead>
              <TableHead>Trạng thái</TableHead>
              <TableHead>Ngày tạo</TableHead>
              <TableHead className="text-right">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableSkeleton />
            ) : users?.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} className="py-10 text-center text-muted-foreground">
                  Không tìm thấy người dùng nào
                </TableCell>
              </TableRow>
            ) : (
              users?.map((user, idx) => (
                <TableRow key={user.id} className="hover:bg-muted/30 transition-colors">
                  <TableCell className="text-center text-muted-foreground text-sm">{idx + 1}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2.5">
                      <Avatar className="size-8 shrink-0">
                        <AvatarImage src={user.avatarUrl} alt={user.fullName} />
                        <AvatarFallback className="bg-primary/10 text-primary text-xs font-semibold">
                          {initials(user.fullName)}
                        </AvatarFallback>
                      </Avatar>
                      <span className="font-medium text-sm truncate max-w-[140px]">{user.fullName}</span>
                    </div>
                  </TableCell>
                  <TableCell className="text-sm text-muted-foreground">{user.email}</TableCell>
                  <TableCell className="text-sm text-muted-foreground">{user.phone || '—'}</TableCell>
                  <TableCell>
                    <RoleBadge role={user.roleName} />
                  </TableCell>
                  <TableCell>
                    <StatusBadge isActive={user.isActive} />
                  </TableCell>
                  <TableCell className="text-xs text-muted-foreground">{formatDate(user.createdAt)}</TableCell>
                  <TableCell className="text-right">
                    <div className="flex items-center justify-end gap-1">
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        onClick={() => navigate(`/users/${user.id}/edit`)}
                        aria-label="Chỉnh sửa"
                      >
                        <Pencil className="size-3.5" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        className="text-destructive hover:text-destructive hover:bg-destructive/10"
                        onClick={() => setConfirmId(user.id)}
                        disabled={deletingId === user.id}
                        aria-label="Xoá"
                      >
                        <Trash2 className="size-3.5" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <AlertDialog open={!!confirmId} onOpenChange={(o) => !o && setConfirmId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xác nhận xoá người dùng</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn có chắc muốn xoá <strong>{confirmUser?.fullName}</strong>? Hành động này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Huỷ</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              onClick={handleDelete}
            >
              Xoá
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  )
}
