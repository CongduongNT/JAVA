import { useEffect, useMemo, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import { Plus, RefreshCw, Search } from 'lucide-react'

import { fetchUsers } from '@/features/users/usersSlice'
import { ROLE_NAMES } from '@/services/userService'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import {
  Pagination, PaginationContent, PaginationEllipsis,
  PaginationItem, PaginationLink, PaginationNext, PaginationPrevious,
} from '@/components/ui/pagination'
import UserTable from './components/UserTable'

const PAGE_SIZE = 10

function FilterToolbar({ search, role, status, onSearch, onRole, onStatus, onRefresh }) {
  return (
    <div className="flex flex-wrap items-center gap-2">
      <div className="relative flex-1 min-w-[200px] max-w-sm">
        <Search className="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          placeholder="Tìm tên hoặc email..."
          value={search}
          onChange={(e) => onSearch(e.target.value)}
          className="pl-8 h-9"
        />
      </div>

      <Select value={role} onValueChange={onRole}>
        <SelectTrigger className="h-9 w-[140px]">
          <SelectValue placeholder="Role" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="ALL">Tất cả Role</SelectItem>
          {ROLE_NAMES.map((r) => (
            <SelectItem key={r} value={r}>{r}</SelectItem>
          ))}
        </SelectContent>
      </Select>

      <Select value={status} onValueChange={onStatus}>
        <SelectTrigger className="h-9 w-[140px]">
          <SelectValue placeholder="Trạng thái" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="ALL">Tất cả</SelectItem>
          <SelectItem value="ACTIVE">Hoạt động</SelectItem>
          <SelectItem value="INACTIVE">Khoá</SelectItem>
        </SelectContent>
      </Select>

      <Button variant="outline" size="sm" onClick={onRefresh} className="h-9 gap-1.5">
        <RefreshCw className="size-3.5" />
        Làm mới
      </Button>
    </div>
  )
}

function PaginationBar({ page, totalPages, onChange }) {
  if (totalPages <= 1) return null

  const pages = []
  for (let i = 1; i <= totalPages; i++) {
    if (i === 1 || i === totalPages || (i >= page - 1 && i <= page + 1)) {
      pages.push(i)
    } else if (pages[pages.length - 1] !== '...') {
      pages.push('...')
    }
  }

  return (
    <Pagination>
      <PaginationContent>
        <PaginationItem>
          <PaginationPrevious
            onClick={() => onChange(Math.max(1, page - 1))}
            className={page === 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
          />
        </PaginationItem>
        {pages.map((p, i) =>
          p === '...' ? (
            <PaginationItem key={`ellipsis-${i}`}>
              <PaginationEllipsis />
            </PaginationItem>
          ) : (
            <PaginationItem key={p}>
              <PaginationLink isActive={p === page} onClick={() => onChange(p)} className="cursor-pointer">
                {p}
              </PaginationLink>
            </PaginationItem>
          )
        )}
        <PaginationItem>
          <PaginationNext
            onClick={() => onChange(Math.min(totalPages, page + 1))}
            className={page === totalPages ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
          />
        </PaginationItem>
      </PaginationContent>
    </Pagination>
  )
}

export default function UsersPage() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { list, status, error } = useSelector((s) => s.users)

  const [search, setSearch] = useState('')
  const [role, setRole] = useState('ALL')
  const [activeStatus, setActiveStatus] = useState('ALL')
  const [page, setPage] = useState(1)

  const isLoading = status === 'loading'

  useEffect(() => { dispatch(fetchUsers()) }, [dispatch])
  useEffect(() => { setPage(1) }, [search, role, activeStatus])

  const filtered = useMemo(() => {
    const q = search.toLowerCase()
    return list.filter((u) => {
      const matchSearch = !q || u.fullName?.toLowerCase().includes(q) || u.email?.toLowerCase().includes(q)
      const matchRole = role === 'ALL' || u.roleName === role
      const matchStatus =
        activeStatus === 'ALL' ||
        (activeStatus === 'ACTIVE' && u.isActive) ||
        (activeStatus === 'INACTIVE' && !u.isActive)
      return matchSearch && matchRole && matchStatus
    })
  }, [list, search, role, activeStatus])

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE))
  const paginated = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE)

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="page-header">Quản lý Người dùng</h1>
          <p className="page-subheader">
            {isLoading ? 'Đang tải...' : `${filtered.length} / ${list.length} người dùng`}
          </p>
        </div>
        <Button onClick={() => navigate('/admin/users')} className="gap-1.5 shrink-0" size="sm">
          <Plus className="size-4" />
          Thêm người dùng
        </Button>
      </div>

      <FilterToolbar
        search={search} role={role} status={activeStatus}
        onSearch={setSearch} onRole={setRole} onStatus={setActiveStatus}
        onRefresh={() => dispatch(fetchUsers())}
      />

      {error && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {error}
        </div>
      )}

      <UserTable users={paginated} isLoading={isLoading} />

      <div className="flex items-center justify-between">
        <p className="text-xs text-muted-foreground">
          Trang {page} / {totalPages} · {filtered.length} kết quả
        </p>
        <PaginationBar page={page} totalPages={totalPages} onChange={setPage} />
      </div>
    </div>
  )
}
