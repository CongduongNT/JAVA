import React from 'react'
import { Pencil, Trash2 } from 'lucide-react'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'

const TYPE_META = {
  MULTIPLE_CHOICE: { label: 'Trac nghiem', variant: 'default' },
  SHORT_ANSWER: { label: 'Tra loi ngan', variant: 'secondary' },
  FILL_IN_BLANK: { label: 'Dien khuyet', variant: 'outline' },
}

const DIFF_META = {
  EASY: { label: 'De', cls: 'border-green-200 bg-green-50 text-green-700' },
  MEDIUM: { label: 'Trung binh', cls: 'border-yellow-200 bg-yellow-50 text-yellow-700' },
  HARD: { label: 'Kho', cls: 'border-red-200 bg-red-50 text-red-700' },
}

const PAGE_WINDOW = 5

function truncate(value, max = 90) {
  if (!value) return '-'
  return value.length > max ? `${value.slice(0, max)}...` : value
}

function buildPageRange(current, total) {
  if (total <= PAGE_WINDOW) return Array.from({ length: total }, (_, index) => index)
  const half = Math.floor(PAGE_WINDOW / 2)
  let start = Math.max(0, current - half)
  const end = Math.min(total - 1, start + PAGE_WINDOW - 1)
  start = Math.max(0, end - PAGE_WINDOW + 1)
  return Array.from({ length: end - start + 1 }, (_, index) => start + index)
}

function PageBar({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null

  const range = buildPageRange(page, totalPages)
  const showLeadingEllipsis = range[0] > 0
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

        {range.map((itemPage) => (
          <PaginationItem key={itemPage}>
            <PaginationLink isActive={itemPage === page} onClick={() => onPageChange(itemPage)}>
              {itemPage + 1}
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
      <div className="flex flex-col items-center justify-center gap-2 rounded-xl border border-border bg-card py-16">
        <p className="text-sm font-medium text-muted-foreground">Chua co cau hoi nao.</p>
        <p className="text-xs text-muted-foreground">Nhan "Them cau hoi" hoac dung AI de tao.</p>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div className="overflow-hidden rounded-xl border border-border bg-card">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/40 hover:bg-muted/40">
              <TableHead className="w-10 text-center">#</TableHead>
              <TableHead>Noi dung cau hoi</TableHead>
              <TableHead className="w-32">Loai</TableHead>
              <TableHead className="w-28">Do kho</TableHead>
              <TableHead className="w-36">Chu de</TableHead>
              <TableHead className="w-20 pr-4 text-right">Thao tac</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {questions.map((question, index) => {
              const typeInfo = TYPE_META[question.type] ?? { label: question.type, variant: 'outline' }
              const diffInfo = DIFF_META[question.difficulty]

              return (
                <TableRow key={question.id}>
                  <TableCell className="text-center text-xs text-muted-foreground">
                    {page * pageSize + index + 1}
                  </TableCell>
                  <TableCell className="max-w-xs">
                    <p className="whitespace-normal text-sm leading-snug">{truncate(question.content)}</p>
                  </TableCell>
                  <TableCell>
                    <Badge variant={typeInfo.variant} className="whitespace-nowrap text-[10px]">
                      {typeInfo.label}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    {diffInfo ? (
                      <span className={`inline-flex items-center rounded-full border px-2 py-0.5 text-[10px] font-medium ${diffInfo.cls}`}>
                        {diffInfo.label}
                      </span>
                    ) : (
                      <span className="text-xs text-muted-foreground">-</span>
                    )}
                  </TableCell>
                  <TableCell>
                    <span className="text-xs text-muted-foreground">{question.topic || '-'}</span>
                  </TableCell>
                  <TableCell className="pr-4 text-right">
                    <div className="flex justify-end gap-1">
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        onClick={() => onEdit(question)}
                        aria-label="Sua cau hoi"
                      >
                        <Pencil className="size-3.5" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        onClick={() => onDelete(question.id)}
                        aria-label="Xoa cau hoi"
                        className="text-muted-foreground hover:bg-destructive/10 hover:text-destructive"
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
