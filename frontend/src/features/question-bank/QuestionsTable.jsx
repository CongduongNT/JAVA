import React from 'react'
import { Pencil, Trash2 } from 'lucide-react'
import {
  Table, TableHeader, TableBody, TableHead, TableRow, TableCell,
} from '@/components/ui/table'
import {
  Pagination, PaginationContent, PaginationItem,
  PaginationLink, PaginationPrevious, PaginationNext, PaginationEllipsis,
} from '@/components/ui/pagination'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'

// ── Constants ────────────────────────────────────────────────────────────────

const TYPE_META = {
  MULTIPLE_CHOICE: { label: 'Trắc nghiệm', variant: 'default' },
  SHORT_ANSWER:    { label: 'Trả lời ngắn', variant: 'secondary' },
  FILL_IN_BLANK:   { label: 'Điền khuyết',  variant: 'outline' },
}

const DIFF_META = {
  EASY:   { label: 'Dễ',        cls: 'bg-green-50 text-green-700 border-green-200' },
  MEDIUM: { label: 'Trung bình', cls: 'bg-yellow-50 text-yellow-700 border-yellow-200' },
  HARD:   { label: 'Khó',       cls: 'bg-red-50 text-red-700 border-red-200' },
}

const PAGE_WINDOW = 5

// ── Helpers ──────────────────────────────────────────────────────────────────

function truncate(str, n = 90) {
  if (!str) return '—'
  return str.length > n ? `${str.slice(0, n)}…` : str
}

function buildPageRange(current, total) {
  if (total <= PAGE_WINDOW) return Array.from({ length: total }, (_, i) => i)
  const half = Math.floor(PAGE_WINDOW / 2)
  let start = Math.max(0, current - half)
  const end = Math.min(total - 1, start + PAGE_WINDOW - 1)
  start = Math.max(0, end - PAGE_WINDOW + 1)
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
}

// ── Pagination ───────────────────────────────────────────────────────────────

function PageBar({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null

  const range = buildPageRange(page, totalPages)
  const showLeadingEllipsis  = range[0] > 0
  const showTrailingEllipsis = range[range.length - 1] < totalPages - 1

  return (
    <Pagination>
      <PaginationContent>
        <PaginationItem>
          <PaginationPrevious onClick={() => onPageChange(page - 1)} disabled={page === 0} />
        </PaginationItem>

        {showLeadingEllipsis && (
          <>
            <PaginationItem>
              <PaginationLink onClick={() => onPageChange(0)}>1</PaginationLink>
            </PaginationItem>
            <PaginationItem><PaginationEllipsis /></PaginationItem>
          </>
        )}

        {range.map((p) => (
          <PaginationItem key={p}>
            <PaginationLink isActive={p === page} onClick={() => onPageChange(p)}>
              {p + 1}
            </PaginationLink>
          </PaginationItem>
        ))}

        {showTrailingEllipsis && (
          <>
            <PaginationItem><PaginationEllipsis /></PaginationItem>
            <PaginationItem>
              <PaginationLink onClick={() => onPageChange(totalPages - 1)}>{totalPages}</PaginationLink>
            </PaginationItem>
          </>
        )}

        <PaginationItem>
          <PaginationNext onClick={() => onPageChange(page + 1)} disabled={page >= totalPages - 1} />
        </PaginationItem>
      </PaginationContent>
    </Pagination>
  )
}

// ── Main Component ───────────────────────────────────────────────────────────

export default function QuestionsTable({
  questions,
  page,
  totalPages,
  pageSize = 10,
  onPageChange,
  onEdit,
  onDelete,
}) {
  if (questions.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 bg-card border border-border rounded-xl gap-2">
        <p className="text-sm text-muted-foreground font-medium">Chưa có câu hỏi nào.</p>
        <p className="text-xs text-muted-foreground">Nhấn "Thêm câu hỏi" hoặc dùng AI để tạo.</p>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div className="rounded-xl border border-border bg-card overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/40 hover:bg-muted/40">
              <TableHead className="w-10 text-center">#</TableHead>
              <TableHead>Nội dung câu hỏi</TableHead>
              <TableHead className="w-32">Loại</TableHead>
              <TableHead className="w-28">Độ khó</TableHead>
              <TableHead className="w-36">Chủ đề</TableHead>
              <TableHead className="w-20 text-right pr-4">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {questions.map((q, idx) => {
              const typeInfo = TYPE_META[q.type] ?? { label: q.type, variant: 'outline' }
              const diffInfo = DIFF_META[q.difficulty]

              return (
                <TableRow key={q.id}>
                  <TableCell className="text-center text-xs text-muted-foreground">
                    {page * pageSize + idx + 1}
                  </TableCell>

                  <TableCell className="max-w-xs">
                    <p className="text-sm leading-snug whitespace-normal">
                      {truncate(q.content)}
                    </p>
                  </TableCell>

                  <TableCell>
                    <Badge variant={typeInfo.variant} className="text-[10px] whitespace-nowrap">
                      {typeInfo.label}
                    </Badge>
                  </TableCell>

                  <TableCell>
                    {diffInfo ? (
                      <span className={`inline-flex items-center rounded-full border px-2 py-0.5 text-[10px] font-medium ${diffInfo.cls}`}>
                        {diffInfo.label}
                      </span>
                    ) : (
                      <span className="text-xs text-muted-foreground">—</span>
                    )}
                  </TableCell>

                  <TableCell>
                    <span className="text-xs text-muted-foreground">{q.topic || '—'}</span>
                  </TableCell>

                  <TableCell className="text-right pr-4">
                    <div className="flex justify-end gap-1">
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        onClick={() => onEdit(q)}
                        aria-label="Sửa câu hỏi"
                      >
                        <Pencil className="size-3.5" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        onClick={() => onDelete(q.id)}
                        aria-label="Xóa câu hỏi"
                        className="text-muted-foreground hover:text-destructive hover:bg-destructive/10"
                      >
                        <Trash2 className="size-3.5" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              )
            })}
          </TableBody>
        </Table>
      </div>

      <PageBar page={page} totalPages={totalPages} onPageChange={onPageChange} />
    </div>
  )
}
