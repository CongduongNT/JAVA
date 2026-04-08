import React, { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Loader2, PlusCircle } from 'lucide-react'
import { toast } from 'sonner'
import questionApi from '@/services/questionApi'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import QuestionsTable from './QuestionsTable'
import QuestionFormModal from './QuestionFormModal'

const PAGE_SIZE = 10

const TYPE_OPTIONS = [
  { value: '', label: 'Tat ca loai' },
  { value: 'MULTIPLE_CHOICE', label: 'Trac nghiem' },
  { value: 'SHORT_ANSWER', label: 'Tra loi ngan' },
  { value: 'FILL_IN_BLANK', label: 'Dien khuyet' },
]

const DIFF_OPTIONS = [
  { value: '', label: 'Tat ca do kho' },
  { value: 'EASY', label: 'De' },
  { value: 'MEDIUM', label: 'Trung binh' },
  { value: 'HARD', label: 'Kho' },
]

const selectCls =
  'h-8 rounded-lg border border-input bg-background px-2.5 text-sm outline-none transition-colors focus:border-ring'

export default function BankQuestionsPage() {
  const { bankId } = useParams()
  const navigate = useNavigate()

  const [bank, setBank] = useState(null)
  const [bankLoading, setBankLoading] = useState(true)
  const [questions, setQuestions] = useState([])
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [page, setPage] = useState(0)
  const [questionsLoading, setQuestionsLoading] = useState(false)
  const [filter, setFilter] = useState({ topic: '', type: '', difficulty: '' })
  const [formModal, setFormModal] = useState({ open: false, question: null })

  useEffect(() => {
    setBankLoading(true)
    questionApi.getBank(bankId)
      .then((response) => setBank(response.data))
      .catch(() => toast.error('Khong the tai thong tin ngan hang.'))
      .finally(() => setBankLoading(false))
  }, [bankId])

  const fetchQuestions = useCallback(async (currentPage) => {
    setQuestionsLoading(true)
    try {
      const params = { page: currentPage, size: PAGE_SIZE }
      if (filter.topic) params.topic = filter.topic
      if (filter.type) params.type = filter.type
      if (filter.difficulty) params.difficulty = filter.difficulty

      const response = await questionApi.getBankQuestions(bankId, params)
      const data = response.data
      setQuestions(data.content ?? [])
      setTotalPages(data.totalPages ?? 0)
      setTotalElements(data.totalElements ?? 0)
    } catch {
      toast.error('Khong the tai danh sach cau hoi.')
    } finally {
      setQuestionsLoading(false)
    }
  }, [bankId, filter])

  useEffect(() => {
    setPage(0)
    fetchQuestions(0)
  }, [bankId, fetchQuestions, filter.difficulty, filter.topic, filter.type])

  const handlePageChange = (newPage) => {
    setPage(newPage)
    fetchQuestions(newPage)
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Xoa cau hoi nay?')) return
    try {
      await questionApi.deleteQuestion(id)
      toast.success('Da xoa cau hoi!')
      fetchQuestions(page)
    } catch {
      toast.error('Khong the xoa cau hoi.')
    }
  }

  const hasFilter = Boolean(filter.topic || filter.type || filter.difficulty)
  const bankMeta = [
    bank?.subject && `Mon: ${bank.subject}`,
    bank?.gradeLevel && `Lop ${bank.gradeLevel}`,
    totalElements > 0 && `${totalElements} cau hoi`,
  ].filter(Boolean).join(' · ')

  return (
    <div className="space-y-5">
      <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
        <div className="flex items-center gap-3">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => navigate('/question-bank')}
            aria-label="Quay lai"
          >
            <ArrowLeft className="size-4" />
          </Button>
          <div>
            {bankLoading ? (
              <div className="space-y-1.5">
                <div className="h-6 w-52 animate-pulse rounded bg-muted" />
                <div className="h-4 w-36 animate-pulse rounded bg-muted" />
              </div>
            ) : (
              <>
                <h1 className="page-header">{bank?.name ?? 'Ngan hang cau hoi'}</h1>
                {bankMeta && <p className="page-subheader">{bankMeta}</p>}
              </>
            )}
          </div>
        </div>

        <Button onClick={() => setFormModal({ open: true, question: null })}>
          <PlusCircle className="size-4" /> Them cau hoi
        </Button>
      </div>

      <div className="flex flex-wrap items-center gap-2 rounded-xl border border-border bg-card p-3">
        <Input
          className="min-w-[180px] flex-1"
          placeholder="Tim theo chu de..."
          value={filter.topic}
          onChange={(event) => setFilter((prev) => ({ ...prev, topic: event.target.value }))}
        />
        <select
          className={selectCls}
          value={filter.type}
          onChange={(event) => setFilter((prev) => ({ ...prev, type: event.target.value }))}
        >
          {TYPE_OPTIONS.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
        </select>
        <select
          className={selectCls}
          value={filter.difficulty}
          onChange={(event) => setFilter((prev) => ({ ...prev, difficulty: event.target.value }))}
        >
          {DIFF_OPTIONS.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
        </select>
        {hasFilter && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setFilter({ topic: '', type: '', difficulty: '' })}
          >
            Xoa bo loc
          </Button>
        )}
      </div>

      {questionsLoading ? (
        <div className="flex justify-center py-16">
          <Loader2 className="size-8 animate-spin text-primary" />
        </div>
      ) : (
        <QuestionsTable
          questions={questions}
          page={page}
          totalPages={totalPages}
          pageSize={PAGE_SIZE}
          onPageChange={handlePageChange}
          onEdit={(question) => setFormModal({ open: true, question })}
          onDelete={handleDelete}
        />
      )}

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
