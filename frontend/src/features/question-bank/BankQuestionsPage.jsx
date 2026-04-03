import React, { useState, useEffect, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, PlusCircle, Loader2 } from 'lucide-react'
import { toast } from 'sonner'
import questionApi from '@/services/questionApi'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import QuestionsTable from './QuestionsTable'
import QuestionFormModal from './QuestionFormModal'

// ── Constants ────────────────────────────────────────────────────────────────

const PAGE_SIZE = 10

const TYPE_OPTIONS = [
  { value: '', label: 'Tất cả loại' },
  { value: 'MULTIPLE_CHOICE', label: 'Trắc nghiệm' },
  { value: 'SHORT_ANSWER',    label: 'Trả lời ngắn' },
  { value: 'FILL_IN_BLANK',   label: 'Điền khuyết' },
]

const DIFF_OPTIONS = [
  { value: '',       label: 'Tất cả độ khó' },
  { value: 'EASY',   label: 'Dễ' },
  { value: 'MEDIUM', label: 'Trung bình' },
  { value: 'HARD',   label: 'Khó' },
]

const selectCls =
  'h-8 rounded-lg border border-input bg-background px-2.5 text-sm outline-none focus:border-ring transition-colors'

// ── Main Component ───────────────────────────────────────────────────────────

export default function BankQuestionsPage() {
  const { bankId } = useParams()
  const navigate   = useNavigate()

  // Bank info
  const [bank, setBank]             = useState(null)
  const [bankLoading, setBankLoading] = useState(true)

  // Questions list
  const [questions,      setQuestions]      = useState([])
  const [totalPages,     setTotalPages]     = useState(0)
  const [totalElements,  setTotalElements]  = useState(0)
  const [page,           setPage]           = useState(0)
  const [qLoading,       setQLoading]       = useState(false)

  // Filter
  const [filter, setFilter] = useState({ topic: '', type: '', difficulty: '' })

  // Form modal
  const [formModal, setFormModal] = useState({ open: false, question: null })

  // Fetch bank detail
  useEffect(() => {
    setBankLoading(true)
    questionApi.getBank(bankId)
      .then((res) => setBank(res.data))
      .catch(() => toast.error('Không thể tải thông tin ngân hàng.'))
      .finally(() => setBankLoading(false))
  }, [bankId])

  // Fetch questions (memoised so ESLint is happy)
  const fetchQuestions = useCallback(async (currentPage) => {
    setQLoading(true)
    try {
      const params = { page: currentPage, size: PAGE_SIZE }
      if (filter.topic)      params.topic      = filter.topic
      if (filter.type)       params.type       = filter.type
      if (filter.difficulty) params.difficulty = filter.difficulty

      const res  = await questionApi.getBankQuestions(bankId, params)
      const data = res.data
      setQuestions(data.content ?? [])
      setTotalPages(data.totalPages ?? 0)
      setTotalElements(data.totalElements ?? 0)
    } catch {
      toast.error('Không thể tải danh sách câu hỏi.')
    } finally {
      setQLoading(false)
    }
  }, [bankId, filter]) // eslint-disable-line react-hooks/exhaustive-deps

  // Re-fetch when filter changes → reset to page 0
  useEffect(() => {
    setPage(0)
    fetchQuestions(0)
  }, [bankId, filter.topic, filter.type, filter.difficulty]) // eslint-disable-line

  const handlePageChange = (newPage) => {
    setPage(newPage)
    fetchQuestions(newPage)
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Xóa câu hỏi này?')) return
    try {
      await questionApi.deleteQuestion(id)
      toast.success('Đã xóa câu hỏi!')
      fetchQuestions(page)
    } catch {
      toast.error('Không thể xóa câu hỏi.')
    }
  }

  const hasFilter = !!(filter.topic || filter.type || filter.difficulty)

  const bankMeta = [
    bank?.subject   && `Môn: ${bank.subject}`,
    bank?.gradeLevel && `Lớp ${bank.gradeLevel}`,
    totalElements > 0 && `${totalElements} câu hỏi`,
  ].filter(Boolean).join(' · ')

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => navigate('/question-bank')}
            aria-label="Quay lại"
          >
            <ArrowLeft className="size-4" />
          </Button>
          <div>
            {bankLoading ? (
              <div className="space-y-1.5">
                <div className="h-6 w-52 bg-muted animate-pulse rounded" />
                <div className="h-4 w-36 bg-muted animate-pulse rounded" />
              </div>
            ) : (
              <>
                <h1 className="page-header">{bank?.name ?? 'Ngân hàng câu hỏi'}</h1>
                {bankMeta && <p className="page-subheader">{bankMeta}</p>}
              </>
            )}
          </div>
        </div>

        <Button onClick={() => setFormModal({ open: true, question: null })}>
          <PlusCircle className="size-4" /> Thêm câu hỏi
        </Button>
      </div>

      {/* Filter bar */}
      <div className="flex flex-wrap gap-2 items-center p-3 bg-card border border-border rounded-xl">
        <Input
          className="flex-1 min-w-[180px]"
          placeholder="Tìm theo chủ đề..."
          value={filter.topic}
          onChange={(e) => setFilter((f) => ({ ...f, topic: e.target.value }))}
        />
        <select
          className={selectCls}
          value={filter.type}
          onChange={(e) => setFilter((f) => ({ ...f, type: e.target.value }))}
        >
          {TYPE_OPTIONS.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
        <select
          className={selectCls}
          value={filter.difficulty}
          onChange={(e) => setFilter((f) => ({ ...f, difficulty: e.target.value }))}
        >
          {DIFF_OPTIONS.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
        {hasFilter && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setFilter({ topic: '', type: '', difficulty: '' })}
          >
            Xóa bộ lọc
          </Button>
        )}
      </div>

      {/* Table or loader */}
      {qLoading ? (
        <div className="flex justify-center py-16">
          <Loader2 className="animate-spin text-primary size-8" />
        </div>
      ) : (
        <QuestionsTable
          questions={questions}
          page={page}
          totalPages={totalPages}
          pageSize={PAGE_SIZE}
          onPageChange={handlePageChange}
          onEdit={(q) => setFormModal({ open: true, question: q })}
          onDelete={handleDelete}
        />
      )}

      {/* Question create / edit modal */}
      {formModal.open && (
        <QuestionFormModal
          bankId={Number(bankId)}
          initialData={formModal.question}
          onClose={() => setFormModal({ open: false, question: null })}
          onSuccess={() => {
            setFormModal({ open: false, question: null })
            fetchQuestions(page)
          }}
        />
      )}
    </div>
  )
}
