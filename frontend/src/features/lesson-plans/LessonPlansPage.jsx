import React, { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import {
  BookOpen,
  Eye,
  Pencil,
  PlusCircle,
  Search,
  Trash2,
} from 'lucide-react'
import { toast } from 'sonner'

import {
  clearFilter,
  deleteLessonPlan,
  fetchLessonPlans,
  setFilter,
  setPage,
} from './lessonPlanSlice'
import { GRADES } from './lessonPlanMeta'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'

const PAGE_SIZE = 10

const STATUS_OPTIONS = [
  { value: '', label: 'Tất cả trạng thái' },
  { value: 'DRAFT', label: 'Nháp' },
  { value: 'PUBLISHED', label: 'Đã xuất bản' },
]

const selectCls =
  'h-8 rounded-lg border border-input bg-background px-2.5 text-sm outline-none transition-colors focus:border-ring'

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

function EmptyState({ hasFilter, onClear, onCreate }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-border bg-card px-4 py-20 text-center">
      <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-muted text-muted-foreground">
        <BookOpen className="size-7" />
      </div>
      {hasFilter ? (
        <>
          <h3 className="text-base font-semibold">Không tìm thấy kết quả</h3>
          <p className="mt-1 text-sm text-muted-foreground">
            Thử thay đổi bộ lọc hoặc xóa bộ lọc hiện tại.
          </p>
          <Button variant="outline" size="sm" className="mt-4" onClick={onClear}>
            Xóa bộ lọc
          </Button>
        </>
      ) : (
        <>
          <h3 className="text-base font-semibold">Chưa có giáo án nào</h3>
          <p className="mt-1 max-w-xs text-sm text-muted-foreground">
            Tạo giáo án đầu tiên của bạn để bắt đầu.
          </p>
          <Button className="mt-4" onClick={onCreate}>
            <PlusCircle className="size-4" />
            Tạo giáo án
          </Button>
        </>
      )}
    </div>
  )
}

function ErrorState({ message, onRetry }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-destructive/20 bg-destructive/5 px-4 py-16 text-center">
      <h3 className="text-base font-semibold text-foreground">Không thể tải danh sách giáo án</h3>
      <p className="mt-2 max-w-md text-sm text-muted-foreground">
        {message || 'Đã có lỗi khi tải dữ liệu. Hãy thử lại sau hoặc tải lại trang.'}
      </p>
      <Button variant="outline" size="sm" className="mt-4" onClick={onRetry}>
        Thử lại
      </Button>
    </div>
  )
}

function TableSkeleton() {
  return (
    <div className="overflow-hidden rounded-xl border border-border bg-card">
      <div className="space-y-3 p-4">
        {Array.from({ length: 5 }).map((_, index) => (
          <Skeleton key={index} className="h-10 w-full" />
        ))}
      </div>
    </div>
  )
}

function PaginationControls({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null

  const pages = []
  for (let index = 0; index < totalPages; index += 1) {
    if (index === 0 || index === totalPages - 1 || Math.abs(index - page) <= 1) {
      pages.push(index)
    }
  }

  const rendered = []
  let previousPage = -1
  for (const currentPage of pages) {
    if (previousPage !== -1 && currentPage - previousPage > 1) {
      rendered.push(`ellipsis-${currentPage}`)
    }

    rendered.push(currentPage)
    previousPage = currentPage
  }

  return (
    <Pagination>
      <PaginationContent>
        <PaginationItem>
          <PaginationPrevious onClick={() => onPageChange(page - 1)} disabled={page === 0} />
        </PaginationItem>
        {rendered.map((item) => (
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
        ))}
        <PaginationItem>
          <PaginationNext onClick={() => onPageChange(page + 1)} disabled={page >= totalPages - 1} />
        </PaginationItem>
      </PaginationContent>
    </Pagination>
  )
}

export default function LessonPlansPage() {
  const dispatch = useDispatch()
  const navigate = useNavigate()

  const {
    list,
    status,
    error,
    totalElements,
    totalPages,
    currentPage,
    filter,
    submitStatus,
  } = useSelector((state) => state.lessonPlans)

  const buildParams = () => ({
    page: currentPage,
    size: PAGE_SIZE,
    ...(filter.keyword && { keyword: filter.keyword }),
    ...(filter.subject && { subject: filter.subject }),
    ...(filter.gradeLevel && { gradeLevel: filter.gradeLevel }),
    ...(filter.status && { status: filter.status }),
  })

  useEffect(() => {
    dispatch(fetchLessonPlans(buildParams()))
  }, [dispatch, currentPage, filter])

  const hasFilter = Boolean(filter.keyword || filter.subject || filter.gradeLevel || filter.status)
  const isLoading = status === 'loading'
  const isDeleting = submitStatus === 'loading'

  const handleDelete = (id, title) => {
    if (!window.confirm(`Bạn có chắc muốn xóa giáo án "${title}"?`)) return

    dispatch(deleteLessonPlan(id)).then((result) => {
      if (result.error) {
        toast.error(result.payload || 'Không thể xóa giáo án.')
        return
      }

      toast.success('Đã xóa giáo án!')
      dispatch(fetchLessonPlans(buildParams()))
    })
  }

  const handlePageChange = (nextPage) => {
    if (nextPage < 0 || nextPage >= totalPages) return
    dispatch(setPage(nextPage))
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
        <div>
          <h1 className="page-header">Giáo án của tôi</h1>
          <p className="page-subheader">Quản lý bản nháp, bản đã xuất bản và tiếp tục hoàn thiện giáo án của bạn.</p>
        </div>
        <Button onClick={() => navigate('/lesson-plans/new')}>
          <PlusCircle className="size-4" />
          Tạo giáo án
        </Button>
      </div>

      <div className="flex flex-wrap items-center gap-2 rounded-xl border border-border bg-card p-3">
        <div className="relative min-w-[200px] flex-1">
          <Search className="absolute left-2.5 top-1/2 size-3.5 -translate-y-1/2 text-muted-foreground" />
          <Input
            className="pl-8"
            placeholder="Tìm theo tiêu đề, chủ đề..."
            value={filter.keyword}
            onChange={(event) => dispatch(setFilter({ keyword: event.target.value }))}
          />
        </div>
        <Input
          className="min-w-[140px] max-w-[180px]"
          placeholder="Môn học..."
          value={filter.subject}
          onChange={(event) => dispatch(setFilter({ subject: event.target.value }))}
        />
        <select
          className={selectCls}
          value={filter.gradeLevel}
          onChange={(event) => dispatch(setFilter({ gradeLevel: event.target.value }))}
        >
          <option value="">Tất cả lớp</option>
          {GRADES.map((grade) => (
            <option key={grade} value={grade}>Lớp {grade}</option>
          ))}
        </select>
        <select
          className={selectCls}
          value={filter.status}
          onChange={(event) => dispatch(setFilter({ status: event.target.value }))}
        >
          {STATUS_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>{option.label}</option>
          ))}
        </select>
        {hasFilter && (
          <Button variant="ghost" size="sm" onClick={() => dispatch(clearFilter())}>
            Xóa bộ lọc
          </Button>
        )}
        {!isLoading && status !== 'failed' && (
          <span className="ml-auto text-xs text-muted-foreground">{totalElements} giáo án</span>
        )}
      </div>

      {isLoading ? (
        <TableSkeleton />
      ) : status === 'failed' ? (
        <ErrorState
          message={error}
          onRetry={() => dispatch(fetchLessonPlans(buildParams()))}
        />
      ) : list.length > 0 ? (
        <>
          <div className="overflow-hidden rounded-xl border border-border bg-card">
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
                        type="button"
                        className="max-w-[260px] whitespace-normal text-left font-medium text-foreground transition-colors hover:text-primary line-clamp-2"
                        onClick={() => navigate(`/lesson-plans/${plan.id}`)}
                      >
                        {plan.title}
                      </button>
                    </TableCell>
                    <TableCell>
                      {plan.subject ? (
                        <Badge variant="secondary" className="text-[10px]">{plan.subject}</Badge>
                      ) : (
                        <span className="text-xs text-muted-foreground">—</span>
                      )}
                    </TableCell>
                    <TableCell>
                      {plan.gradeLevel ? (
                        <span className="text-xs">Lớp {plan.gradeLevel}</span>
                      ) : (
                        <span className="text-xs text-muted-foreground">—</span>
                      )}
                    </TableCell>
                    <TableCell>
                      <span className="max-w-[160px] whitespace-normal text-xs text-muted-foreground line-clamp-1">
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
                      <div className="flex items-center justify-end gap-1 opacity-100 transition-opacity sm:opacity-0 sm:group-hover:opacity-100">
                        <button
                          type="button"
                          title="Xem chi tiết"
                          onClick={() => navigate(`/lesson-plans/${plan.id}`)}
                          className="rounded-md p-1.5 text-muted-foreground transition-colors hover:bg-accent hover:text-foreground"
                        >
                          <Eye className="size-3.5" />
                        </button>
                        <button
                          type="button"
                          title="Chỉnh sửa"
                          onClick={() => navigate(`/lesson-plans/${plan.id}/edit`)}
                          className="rounded-md p-1.5 text-muted-foreground transition-colors hover:bg-accent hover:text-foreground"
                        >
                          <Pencil className="size-3.5" />
                        </button>
                        <button
                          type="button"
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
