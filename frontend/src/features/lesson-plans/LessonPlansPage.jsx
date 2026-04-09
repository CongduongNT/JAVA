import React, { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import {
  BookOpen, Eye, Loader2, Pencil, PlusCircle, Search, Trash2,
} from 'lucide-react'
import { toast } from 'sonner'

import {
  clearFilter,
  deleteLessonPlan,
  fetchLessonPlans,
  setFilter,
  setPage,
} from './lessonPlanSlice'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import {
  Pagination, PaginationContent, PaginationEllipsis, PaginationItem,
  PaginationLink, PaginationNext, PaginationPrevious,
} from '@/components/ui/pagination'
import { Skeleton } from '@/components/ui/skeleton'

// ─── Constants ────────────────────────────────────────────────────────────────

const GRADES = ['1','2','3','4','5','6','7','8','9','10','11','12']
const PAGE_SIZE = 10

const STATUS_OPTIONS = [
  { value: '', label: 'Tất cả trạng thái' },
  { value: 'DRAFT', label: 'Nháp' },
  { value: 'PUBLISHED', label: 'Đã xuất bản' },
]

const selectCls =
  'h-8 rounded-lg border border-input bg-background px-2.5 text-sm outline-none transition-colors focus:border-ring'

// ─── Status Badge ─────────────────────────────────────────────────────────────

function StatusBadge({ status }) {
  if (status === 'PUBLISHED') {
    return (
      <span className="inline-flex items-center rounded-full border border-green-200 bg-green-50 px-2 py-0.5 text-[10px] font-medium text-green-700">
        Đã xuất bản
      </span>
    )
  }
  return (
    <span className="inline-flex items-center rounded-full border border-amber-200 bg-amber-50 px-2 py-0.5 text-[10px] font-medium text-amber-700">
      Nháp
    </span>
  )
}

// ─── Empty State ──────────────────────────────────────────────────────────────

function EmptyState({ hasFilter, onClear, onCreate }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-border bg-card px-4 py-20 text-center">
      <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-muted text-muted-foreground">
        <BookOpen className="size-7" />
      </div>
      {hasFilter ? (
        <>
          <h3 className="text-base font-semibold">Không tìm thấy kết quả</h3>
          <p className="mt-1 text-sm text-muted-foreground">Thử thay đổi bộ lọc hoặc xóa bộ lọc hiện tại.</p>
          <Button variant="outline" size="sm" className="mt-4" onClick={onClear}>Xóa bộ lọc</Button>
        </>
      ) : (
        <>
          <h3 className="text-base font-semibold">Chưa có giáo án nào</h3>
          <p className="mt-1 max-w-xs text-sm text-muted-foreground">Tạo giáo án đầu tiên của bạn để bắt đầu.</p>
          <Button className="mt-4" onClick={onCreate}>
            <PlusCircle className="size-4" /> Tạo giáo án
          </Button>
        </>
      )}
    </div>
  )
}

// ─── Table Skeleton ───────────────────────────────────────────────────────────

function TableSkeleton() {
  return (
    <div className="rounded-xl border border-border bg-card overflow-hidden">
      <div className="p-4 space-y-3">
        {Array.from({ length: 5 }).map((_, i) => (
          <Skeleton key={i} className="h-10 w-full" />
        ))}
      </div>
    </div>
  )
}

// ─── Pagination Controls ──────────────────────────────────────────────────────

function PaginationControls({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null

  const pages = []
  for (let i = 0; i < totalPages; i++) {
    if (i === 0 || i === totalPages - 1 || Math.abs(i - page) <= 1) {
      pages.push(i)
    }
  }

  const rendered = []
  let prev = -1
  for (const p of pages) {
    if (prev !== -1 && p - prev > 1) {
      rendered.push('ellipsis-' + p)
    }
    rendered.push(p)
    prev = p
  }

  return (
    <Pagination>
      <PaginationContent>
        <PaginationItem>
          <PaginationPrevious onClick={() => onPageChange(page - 1)} disabled={page === 0} />
        </PaginationItem>
        {rendered.map((item) =>
          typeof item === 'string' ? (
            <PaginationItem key={item}>
              <PaginationEllipsis />
            </PaginationItem>
          ) : (
            <PaginationItem key={item}>
              <PaginationLink isActive={item === page} onClick={() => onPageChange(item)}>
                {item + 1}
              </PaginationLink>
            </PaginationItem>
          )
        )}
        <PaginationItem>
          <PaginationNext onClick={() => onPageChange(page + 1)} disabled={page >= totalPages - 1} />
        </PaginationItem>
      </PaginationContent>
    </Pagination>
  )
}

// ─── Main Page ────────────────────────────────────────────────────────────────

export default function LessonPlansPage() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { list, status, totalElements, totalPages, currentPage, filter, submitStatus } =
    useSelector((state) => state.lessonPlans)

  // Fetch whenever filter or page changes
  useEffect(() => {
    const params = {
      page: currentPage,
      size: PAGE_SIZE,
    }
    if (filter.keyword) params.keyword = filter.keyword
    if (filter.subject) params.subject = filter.subject
    if (filter.gradeLevel) params.gradeLevel = filter.gradeLevel
    if (filter.status) params.status = filter.status

    dispatch(fetchLessonPlans(params))
  }, [dispatch, currentPage, filter])

  const hasFilter = Boolean(filter.keyword || filter.subject || filter.gradeLevel || filter.status)

  const handleDelete = (id, title) => {
    if (!window.confirm(`Bạn có chắc muốn xóa giáo án "${title}"?`)) return
    dispatch(deleteLessonPlan(id)).then((result) => {
      if (!result.error) {
        toast.success('Đã xóa giáo án!')
        // Re-fetch after delete
        const params = {
          page: currentPage,
          size: PAGE_SIZE,
          ...(filter.keyword && { keyword: filter.keyword }),
          ...(filter.subject && { subject: filter.subject }),
          ...(filter.gradeLevel && { gradeLevel: filter.gradeLevel }),
          ...(filter.status && { status: filter.status }),
        }
        dispatch(fetchLessonPlans(params))
      } else {
        toast.error(result.payload || 'Không thể xóa giáo án.')
      }
    })
  }

  const handlePageChange = (newPage) => {
    if (newPage < 0 || newPage >= totalPages) return
    dispatch(setPage(newPage))
  }

  const isLoading = status === 'loading'
  const isDeleting = submitStatus === 'loading'

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
        <div>
          <h1 className="page-header">Giáo án của tôi</h1>
          <p className="page-subheader">Quản lý và tổ chức các giáo án của bạn.</p>
        </div>
        <Button onClick={() => navigate('/lesson-plans/new')}>
          <PlusCircle className="size-4" /> Tạo giáo án
        </Button>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap items-center gap-2 rounded-xl border border-border bg-card p-3">
        <div className="relative min-w-[200px] flex-1">
          <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 size-3.5 text-muted-foreground" />
          <Input
            className="pl-8"
            placeholder="Tìm theo tiêu đề, chủ đề..."
            value={filter.keyword}
            onChange={(e) => dispatch(setFilter({ keyword: e.target.value }))}
          />
        </div>
        <Input
          className="min-w-[140px] max-w-[180px]"
          placeholder="Môn học..."
          value={filter.subject}
          onChange={(e) => dispatch(setFilter({ subject: e.target.value }))}
        />
        <select
          className={selectCls}
          value={filter.gradeLevel}
          onChange={(e) => dispatch(setFilter({ gradeLevel: e.target.value }))}
        >
          <option value="">Tất cả lớp</option>
          {GRADES.map((g) => (
            <option key={g} value={g}>Lớp {g}</option>
          ))}
        </select>
        <select
          className={selectCls}
          value={filter.status}
          onChange={(e) => dispatch(setFilter({ status: e.target.value }))}
        >
          {STATUS_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>
        {hasFilter && (
          <Button variant="ghost" size="sm" onClick={() => dispatch(clearFilter())}>
            Xóa bộ lọc
          </Button>
        )}
        {!isLoading && (
          <span className="ml-auto text-xs text-muted-foreground">{totalElements} giáo án</span>
        )}
      </div>

      {/* Content */}
      {isLoading ? (
        <TableSkeleton />
      ) : list.length > 0 ? (
        <>
          <div className="rounded-xl border border-border bg-card overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow className="bg-muted/40">
                  <TableHead className="w-[280px]">Tiêu đề</TableHead>
                  <TableHead>Môn học</TableHead>
                  <TableHead>Lớp</TableHead>
                  <TableHead>Chủ đề</TableHead>
                  <TableHead>Thời lượng</TableHead>
                  <TableHead>Trạng thái</TableHead>
                  <TableHead>Cập nhật</TableHead>
                  <TableHead className="text-right">Thao tác</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {list.map((plan) => (
                  <TableRow key={plan.id} className="group">
                    <TableCell>
                      <button
                        className="text-left font-medium text-foreground hover:text-primary transition-colors line-clamp-2 max-w-[260px] whitespace-normal"
                        onClick={() => navigate(`/lesson-plans/${plan.id}`)}
                      >
                        {plan.title}
                      </button>
                    </TableCell>
                    <TableCell>
                      {plan.subject ? (
                        <Badge variant="secondary" className="text-[10px]">{plan.subject}</Badge>
                      ) : (
                        <span className="text-muted-foreground text-xs">—</span>
                      )}
                    </TableCell>
                    <TableCell>
                      {plan.gradeLevel ? (
                        <span className="text-xs">Lớp {plan.gradeLevel}</span>
                      ) : (
                        <span className="text-muted-foreground text-xs">—</span>
                      )}
                    </TableCell>
                    <TableCell>
                      <span className="line-clamp-1 text-xs text-muted-foreground max-w-[160px] whitespace-normal">
                        {plan.topic || '—'}
                      </span>
                    </TableCell>
                    <TableCell>
                      <span className="text-xs">
                        {plan.durationMinutes ? `${plan.durationMinutes} phút` : '—'}
                      </span>
                    </TableCell>
                    <TableCell>
                      <StatusBadge status={plan.status} />
                    </TableCell>
                    <TableCell>
                      <span className="text-xs text-muted-foreground">
                        {plan.updatedAt
                          ? new Date(plan.updatedAt).toLocaleDateString('vi-VN')
                          : new Date(plan.createdAt).toLocaleDateString('vi-VN')}
                      </span>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                        <button
                          title="Xem chi tiết"
                          onClick={() => navigate(`/lesson-plans/${plan.id}`)}
                          className="rounded-md p-1.5 text-muted-foreground transition-colors hover:bg-accent hover:text-foreground"
                        >
                          <Eye className="size-3.5" />
                        </button>
                        <button
                          title="Chỉnh sửa"
                          onClick={() => navigate(`/lesson-plans/${plan.id}/edit`)}
                          className="rounded-md p-1.5 text-muted-foreground transition-colors hover:bg-accent hover:text-foreground"
                        >
                          <Pencil className="size-3.5" />
                        </button>
                        <button
                          title="Xóa"
                          disabled={isDeleting}
                          onClick={() => handleDelete(plan.id, plan.title)}
                          className="rounded-md p-1.5 text-muted-foreground transition-colors hover:bg-destructive/10 hover:text-destructive disabled:opacity-50"
                        >
                          <Trash2 className="size-3.5" />
                        </button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>

          <PaginationControls
            page={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
          />
        </>
      ) : (
        <EmptyState
          hasFilter={hasFilter}
          onClear={() => dispatch(clearFilter())}
          onCreate={() => navigate('/lesson-plans/new')}
        />
      )}
    </div>
  )
}
