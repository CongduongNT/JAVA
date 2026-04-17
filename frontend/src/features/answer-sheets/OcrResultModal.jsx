import React, { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { X, Loader2, ImageOff, FileText, AlertTriangle } from 'lucide-react'
import { clearDetailSheet, fetchSheetDetail } from './answerSheetSlice'

export default function OcrResultModal({ sheetId, onClose }) {
  const dispatch = useDispatch()
  const { detailSheet, detailStatus, detailError } = useSelector(
    (state) => state.answerSheets
  )

  useEffect(() => {
    if (sheetId) dispatch(fetchSheetDetail(sheetId))
    return () => {
      dispatch(clearDetailSheet())
    }
  }, [sheetId, dispatch])

  // Close on Escape key
  useEffect(() => {
    const handler = (e) => { if (e.key === 'Escape') onClose() }
    document.addEventListener('keydown', handler)
    return () => document.removeEventListener('keydown', handler)
  }, [onClose])

  const isPdf = detailSheet?.fileUrl?.toLowerCase().endsWith('.pdf')

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
        aria-hidden="true"
      />

      {/* Modal panel */}
      <div
        role="dialog"
        aria-modal="true"
        aria-label="Kết quả OCR"
        className="relative z-10 flex h-[90vh] w-full max-w-6xl flex-col rounded-2xl border border-border bg-card shadow-2xl"
      >
        {/* Header */}
        <div className="flex shrink-0 items-center justify-between border-b border-border px-6 py-4">
          <div>
            <h2 className="text-base font-semibold text-foreground">Kết quả OCR</h2>
            {detailSheet && (
              <p className="mt-0.5 text-xs text-muted-foreground">
                {detailSheet.studentName
                  ? `Học sinh: ${detailSheet.studentName}`
                  : `Bài #${detailSheet.id}`}
                {detailSheet.studentCode && ` – Mã: ${detailSheet.studentCode}`}
                {detailSheet.examId && ` · Đề #${detailSheet.examId}`}
              </p>
            )}
          </div>
          <button
            onClick={onClose}
            aria-label="Đóng"
            className="rounded-lg p-1.5 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
          >
            <X className="size-4" />
          </button>
        </div>

        {/* Body */}
        <div className="flex min-h-0 flex-1 overflow-hidden">
          {/* Loading */}
          {detailStatus === 'loading' && (
            <div className="flex flex-1 flex-col items-center justify-center gap-3">
              <Loader2 className="size-9 animate-spin text-primary" />
              <p className="text-sm text-muted-foreground">Đang tải dữ liệu...</p>
            </div>
          )}

          {/* Error */}
          {detailStatus === 'failed' && (
            <div className="flex flex-1 flex-col items-center justify-center gap-3 text-destructive">
              <AlertTriangle className="size-10" />
              <p className="text-sm font-medium">
                {detailError || 'Không thể tải chi tiết bài làm.'}
              </p>
            </div>
          )}

          {/* Content */}
          {detailStatus === 'succeeded' && detailSheet && (
            <>
              {/* ── Left: Image viewer ── */}
              <div className="flex w-1/2 min-w-0 flex-col border-r border-border">
                <div className="shrink-0 border-b border-border bg-muted/20 px-5 py-2.5">
                  <span className="text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                    Ảnh bài làm
                  </span>
                </div>
                <div className="flex flex-1 items-center justify-center overflow-auto bg-muted/10 p-6">
                  {detailSheet.fileUrl ? (
                    isPdf ? (
                      <div className="flex flex-col items-center gap-4 text-muted-foreground">
                        <FileText className="size-16 text-primary/40" />
                        <p className="text-sm font-medium">File PDF</p>
                        <a
                          href={detailSheet.fileUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="rounded-lg border border-border bg-background px-4 py-2 text-xs font-medium text-primary transition-colors hover:bg-muted"
                        >
                          Mở PDF trong tab mới →
                        </a>
                      </div>
                    ) : (
                      <img
                        src={detailSheet.fileUrl}
                        alt="Answer sheet scan"
                        className="max-h-full max-w-full rounded-xl object-contain shadow-lg"
                      />
                    )
                  ) : (
                    <div className="flex flex-col items-center gap-3 text-muted-foreground">
                      <ImageOff className="size-12 opacity-40" />
                      <p className="text-sm">Không có ảnh</p>
                    </div>
                  )}
                </div>
              </div>

              {/* ── Right: OCR raw text ── */}
              <div className="flex w-1/2 min-w-0 flex-col">
                <div className="shrink-0 border-b border-border bg-muted/20 px-5 py-2.5">
                  <span className="text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                    Văn bản OCR nhận dạng
                  </span>
                </div>
                <div className="flex flex-1 flex-col overflow-hidden p-4">
                  {detailSheet.ocrRawData ? (
                    <div className="flex flex-1 flex-col gap-2">
                      {/* OCR status indicator */}
                      <div className="flex items-center gap-1.5">
                        <span className="inline-flex items-center gap-1 rounded-full bg-green-50 border border-green-200 px-2 py-0.5 text-[10px] font-medium text-green-700">
                          ✓ OCR hoàn tất
                        </span>
                        <span className="text-[10px] text-muted-foreground">
                          {detailSheet.ocrRawData.length} ký tự nhận dạng
                        </span>
                      </div>
                      <pre className="flex-1 overflow-auto rounded-lg border border-border bg-muted/30 p-4 font-mono text-xs leading-relaxed text-foreground whitespace-pre-wrap break-words">
                        {detailSheet.ocrRawData}
                      </pre>
                    </div>
                  ) : (
                    <div className="flex flex-1 flex-col items-center justify-center gap-3 text-center">
                      <FileText className="size-12 text-muted-foreground/40" />
                      <div>
                        <p className="text-sm font-medium text-muted-foreground">
                          {detailSheet.ocrStatus === 'PENDING'
                            ? 'Chưa xử lý OCR'
                            : detailSheet.ocrStatus === 'PROCESSING'
                            ? 'Đang xử lý OCR...'
                            : 'Không có dữ liệu OCR'}
                        </p>
                        <p className="mt-1 text-xs text-muted-foreground/70">
                          {detailSheet.ocrStatus === 'PENDING'
                            ? 'Nhấn "Bắt đầu chấm" trên bảng danh sách để xử lý.'
                            : detailSheet.ocrStatus === 'PROCESSING'
                            ? 'Vui lòng đợi hệ thống xử lý xong và thử lại.'
                            : ''}
                        </p>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  )
}
