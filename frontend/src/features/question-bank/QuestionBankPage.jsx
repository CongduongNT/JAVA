import React, { useState, useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import { PlusCircle, Search, Trash2, Brain, BookOpen, Loader2, Pencil } from 'lucide-react'
import { toast } from 'sonner'
import { fetchMyBanks, deleteBank, setFilter, clearFilter } from './questionBankSlice'
import QuestionBankFormModal from './QuestionBankFormModal'
import AIQuestionGenerator from './AIQuestionGenerator'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'

const GRADES = ['1','2','3','4','5','6','7','8','9','10','11','12']

// ── Sub-components ───────────────────────────────────────────────────────────

function BankCard({ bank, deleting, onDelete, onEdit, onAI, onClick }) {
  return (
    <div
      className="group relative bg-card border border-border rounded-xl p-5 flex flex-col gap-4 cursor-pointer hover:border-primary/40 hover:shadow-lg hover:shadow-primary/5 transition-all duration-200"
      onClick={onClick}
    >
      {/* Top row: icon + action buttons */}
      <div className="flex items-start justify-between">
        <div className="p-2.5 bg-accent rounded-lg text-accent-foreground group-hover:bg-primary/10 group-hover:text-primary transition-colors">
          <BookOpen className="size-5" />
        </div>
        <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
          <button
            onClick={onAI}
            title="Sinh câu hỏi AI"
            className="p-1.5 text-muted-foreground hover:text-primary hover:bg-accent rounded-md transition-colors"
          >
            <Brain className="size-3.5" />
          </button>
          <button
            onClick={onEdit}
            title="Chỉnh sửa"
            className="p-1.5 text-muted-foreground hover:text-foreground hover:bg-accent rounded-md transition-colors"
          >
            <Pencil className="size-3.5" />
          </button>
          <button
            onClick={onDelete}
            disabled={deleting}
            title="Xóa"
            className="p-1.5 text-muted-foreground hover:text-destructive hover:bg-destructive/10 rounded-md transition-colors disabled:opacity-50"
          >
            <Trash2 className="size-3.5" />
          </button>
        </div>
      </div>

      {/* Bank info */}
      <div className="flex-1">
        <h3 className="font-semibold text-foreground group-hover:text-primary transition-colors line-clamp-2 leading-snug text-sm">
          {bank.name}
        </h3>
        <div className="flex flex-wrap gap-1.5 mt-2">
          {bank.subject && <Badge variant="secondary" className="text-[10px]">{bank.subject}</Badge>}
          {bank.gradeLevel && <Badge variant="outline" className="text-[10px]">Lớp {bank.gradeLevel}</Badge>}
          {bank.isPublished && (
            <span className="inline-flex items-center rounded-full border border-green-200 bg-green-50 px-2 py-0.5 text-[10px] font-medium text-green-700">
              Công khai
            </span>
          )}
        </div>
      </div>

      {/* Footer hint */}
      <div className="pt-3 border-t border-border">
        <p className="text-xs text-muted-foreground line-clamp-1">
          {bank.description || 'Nhấn để xem danh sách câu hỏi →'}
        </p>
      </div>
    </div>
  )
}

function EmptyState({ hasFilter, onClear, onCreate }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 px-4 bg-card border-2 border-dashed border-border rounded-2xl text-center">
      <div className="w-14 h-14 bg-muted text-muted-foreground rounded-full flex items-center justify-center mb-4">
        <Search className="size-7" />
      </div>
      {hasFilter ? (
        <>
          <h3 className="text-base font-semibold">Không tìm thấy kết quả</h3>
          <p className="text-sm text-muted-foreground mt-1">Thử thay đổi bộ lọc hoặc xóa bộ lọc hiện tại.</p>
          <Button variant="outline" size="sm" className="mt-4" onClick={onClear}>Xóa bộ lọc</Button>
        </>
      ) : (
        <>
          <h3 className="text-base font-semibold">Chưa có ngân hàng câu hỏi</h3>
          <p className="text-sm text-muted-foreground mt-1 max-w-xs">Tạo ngân hàng đầu tiên hoặc dùng AI để bắt đầu.</p>
          <Button className="mt-4" onClick={onCreate}>
            <PlusCircle className="size-4" /> Tạo ngân hàng
          </Button>
        </>
      )}
    </div>
  )
}

// ── Main Page ────────────────────────────────────────────────────────────────

export default function QuestionBankPage() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { list, status, deletingId, filter } = useSelector((s) => s.questionBank)

  const [formModal, setFormModal] = useState({ open: false, bank: null })
  const [generator, setGenerator] = useState({ open: false, bankId: null })

  useEffect(() => { dispatch(fetchMyBanks()) }, [dispatch])

  const filteredBanks = list.filter((b) => {
    const okSubject = !filter.subject || b.subject?.toLowerCase().includes(filter.subject.toLowerCase())
    const okGrade   = !filter.gradeLevel || String(b.gradeLevel) === String(filter.gradeLevel)
    return okSubject && okGrade
  })

  const handleDelete = (id, e) => {
    e.stopPropagation()
    if (!window.confirm('Bạn có chắc muốn xóa ngân hàng này? Tất cả câu hỏi bên trong sẽ bị mất!')) return
    dispatch(deleteBank(id)).then((res) => {
      if (!res.error) toast.success('Đã xóa ngân hàng!')
      else toast.error(res.payload || 'Không thể xóa ngân hàng.')
    })
  }

  // AI Generator view
  if (generator.open) {
    return (
      <AIQuestionGenerator
        banks={list}
        initialBankId={generator.bankId}
        onClose={() => { setGenerator({ open: false, bankId: null }); dispatch(fetchMyBanks()) }}
      />
    )
  }

  const hasFilter = !!(filter.subject || filter.gradeLevel)

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="page-header">Ngân hàng câu hỏi</h1>
          <p className="page-subheader">Quản lý và tổ chức các câu hỏi của bạn.</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => setFormModal({ open: true, bank: null })}>
            <PlusCircle className="size-4" /> Tạo ngân hàng
          </Button>
          <Button onClick={() => setGenerator({ open: true, bankId: null })}>
            <Brain className="size-4" /> Sinh câu hỏi AI
          </Button>
        </div>
      </div>

      {/* Filter bar */}
      <div className="flex flex-wrap gap-2 items-center p-3 bg-card border border-border rounded-xl">
        <Input
          className="flex-1 min-w-[160px]"
          placeholder="Lọc theo môn học..."
          value={filter.subject}
          onChange={(e) => dispatch(setFilter({ subject: e.target.value }))}
        />
        <select
          className="h-8 rounded-lg border border-input bg-background px-2.5 text-sm outline-none focus:border-ring transition-colors"
          value={filter.gradeLevel}
          onChange={(e) => dispatch(setFilter({ gradeLevel: e.target.value }))}
        >
          <option value="">Tất cả lớp</option>
          {GRADES.map((g) => <option key={g} value={g}>Lớp {g}</option>)}
        </select>
        {hasFilter && (
          <Button variant="ghost" size="sm" onClick={() => dispatch(clearFilter())}>Xóa bộ lọc</Button>
        )}
        <span className="text-xs text-muted-foreground ml-auto">
          {filteredBanks.length} ngân hàng
        </span>
      </div>

      {/* Content */}
      {status === 'loading' ? (
        <div className="flex flex-col items-center justify-center py-20 gap-3">
          <Loader2 className="animate-spin text-primary size-9" />
          <p className="text-sm text-muted-foreground">Đang tải ngân hàng câu hỏi...</p>
        </div>
      ) : filteredBanks.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {filteredBanks.map((bank) => (
            <BankCard
              key={bank.id}
              bank={bank}
              deleting={deletingId === bank.id}
              onDelete={(e) => handleDelete(bank.id, e)}
              onEdit={(e) => { e.stopPropagation(); setFormModal({ open: true, bank }) }}
              onAI={(e) => { e.stopPropagation(); setGenerator({ open: true, bankId: bank.id }) }}
              onClick={() => navigate(`/question-bank/${bank.id}`)}
            />
          ))}
        </div>
      ) : (
        <EmptyState
          hasFilter={hasFilter}
          onClear={() => dispatch(clearFilter())}
          onCreate={() => setFormModal({ open: true, bank: null })}
        />
      )}

      {/* Create / Edit modal */}
      {formModal.open && (
        <QuestionBankFormModal
          initialData={formModal.bank}
          onClose={() => setFormModal({ open: false, bank: null })}
          onSuccess={() => setFormModal({ open: false, bank: null })}
        />
      )}
    </div>
  )
}
