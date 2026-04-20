import React, { useEffect, useState } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { CheckCircle2, XCircle, Clock, ArrowRight, RotateCcw, Shield, Zap, Award, Star } from 'lucide-react'

const VNPAY_RESPONSE_LABELS = {
  '00': 'Giao dịch thành công',
  '07': 'Trừ tiền nhưng nghi ngờ gian lận',
  '09': 'Thẻ/Tài khoản chưa đăng ký Internet Banking',
  '10': 'Xác thực thẻ sai quá 3 lần',
  '11': 'Hết hạn chờ thanh toán',
  '12': 'Thẻ/Tài khoản bị khóa',
  '13': 'OTP nhập sai',
  '24': 'Khách hàng hủy giao dịch',
  '51': 'Số dư không đủ',
  '65': 'Vượt hạn mức giao dịch trong ngày',
  '75': 'Ngân hàng tạm khóa thanh toán',
  '79': 'Nhập sai mật khẩu quá số lần cho phép',
  '99': 'Lỗi không xác định',
}

const PKG_ICONS = {
  PRO:     <Zap  className="w-8 h-8" />,
  PREMIUM: <Award className="w-8 h-8" />,
  FREE:    <Star className="w-8 h-8" />,
}

export default function PaymentResultPage() {
  const [params]  = useSearchParams()
  const navigate  = useNavigate()
  const [countdown, setCountdown] = useState(8)

  const success  = params.get('success') === 'true'
  const code     = params.get('code') || '99'
  const orderId  = params.get('orderId')
  const pkgName  = params.get('pkgName') || ''
  const pkgDays  = params.get('pkgDays') || ''
  const amount   = params.get('amount') || ''
  const bank     = params.get('bank') || ''
  const label    = VNPAY_RESPONSE_LABELS[code] || `Mã lỗi: ${code}`

  const formattedAmount = amount
    ? Number(amount).toLocaleString('vi-VN') + '₫'
    : ''

  const pkgKey = pkgName.toUpperCase()
  const pkgIcon = PKG_ICONS[pkgKey] ?? <Shield className="w-8 h-8" />

  // Auto-redirect về dashboard sau 8s nếu thành công
  useEffect(() => {
    if (!success) return
    const timer = setInterval(() => {
      setCountdown(c => {
        if (c <= 1) { clearInterval(timer); navigate('/dashboard') }
        return c - 1
      })
    }, 1000)
    return () => clearInterval(timer)
  }, [success, navigate])

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 p-4">
      <div className="bg-white rounded-3xl shadow-2xl max-w-md w-full overflow-hidden animate-in fade-in zoom-in-95 duration-500">

        {/* Header band */}
        <div className={`p-8 text-center ${success
          ? 'bg-gradient-to-br from-emerald-500 to-teal-600'
          : 'bg-gradient-to-br from-rose-500 to-red-600'}`}>

          <div className="flex justify-center mb-4">
            <div className="size-20 rounded-full bg-white/20 backdrop-blur flex items-center justify-center ring-4 ring-white/30">
              {success
                ? <CheckCircle2 className="size-10 text-white" />
                : <XCircle      className="size-10 text-white" />}
            </div>
          </div>

          <h1 className="text-2xl font-black text-white">
            {success ? 'Thanh toán thành công!' : 'Thanh toán thất bại'}
          </h1>
          <p className="text-white/80 text-sm mt-1">{label}</p>
        </div>

        <div className="p-6 space-y-4">

          {/* Package highlight – chỉ hiển thị khi thành công và có tên gói */}
          {success && pkgName && (
            <div className="rounded-2xl bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-100 p-5 flex items-center gap-4">
              <div className="shrink-0 size-14 rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center text-white shadow-lg shadow-blue-200">
                {pkgIcon}
              </div>
              <div>
                <p className="text-xs font-bold text-blue-400 uppercase tracking-widest mb-0.5">Gói đã đăng ký</p>
                <p className="text-xl font-black text-slate-800">{pkgName}</p>
                {pkgDays && (
                  <p className="text-sm text-slate-500 font-medium">Hiệu lực {pkgDays} ngày kể từ hôm nay</p>
                )}
              </div>
            </div>
          )}

          {/* Transaction detail box */}
          <div className="rounded-2xl border border-slate-100 divide-y divide-slate-50 bg-slate-50/50 overflow-hidden text-sm">
            {formattedAmount && (
              <Row label="Số tiền thanh toán" value={<span className="font-bold text-slate-800">{formattedAmount}</span>} />
            )}
            {bank && (
              <Row label="Ngân hàng" value={bank} />
            )}
            {orderId && (
              <Row label="Mã giao dịch" value={<span className="font-mono text-xs text-slate-600">{orderId}</span>} />
            )}
            <Row
              label="Trạng thái"
              value={
                <span className={`font-semibold px-2 py-0.5 rounded-full text-xs ${success
                  ? 'bg-emerald-100 text-emerald-700'
                  : 'bg-rose-100 text-rose-700'}`}>
                  {success ? '✓ Đã kích hoạt' : '✗ Thất bại'}
                </span>
              }
            />
          </div>

          {/* Success auto-redirect note */}
          {success && (
            <div className="flex items-center gap-3 bg-blue-50 text-blue-700 rounded-xl px-4 py-3 text-sm">
              <Clock className="size-4 shrink-0" />
              <span>Tự động chuyển về Dashboard sau <strong>{countdown}s</strong></span>
            </div>
          )}

          {/* Actions */}
          <div className="flex flex-col gap-2 pt-1">
            {success ? (
              <button
                onClick={() => navigate('/dashboard')}
                className="flex items-center justify-center gap-2 w-full px-4 py-3 bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-bold rounded-xl hover:from-blue-700 hover:to-indigo-700 transition-all shadow-lg shadow-blue-200 active:scale-95"
              >
                Về Dashboard <ArrowRight className="size-4" />
              </button>
            ) : (
              <>
                <button
                  onClick={() => navigate('/packages')}
                  className="flex items-center justify-center gap-2 w-full px-4 py-3 bg-indigo-600 text-white font-bold rounded-xl hover:bg-indigo-700 transition-all active:scale-95"
                >
                  <RotateCcw className="size-4" /> Thử lại
                </button>
                <button
                  onClick={() => navigate('/dashboard')}
                  className="w-full px-4 py-3 bg-slate-100 text-slate-600 font-medium rounded-xl hover:bg-slate-200 transition-all active:scale-95"
                >
                  Về trang chủ
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

function Row({ label, value }) {
  return (
    <div className="flex items-center justify-between px-4 py-3">
      <span className="text-slate-500">{label}</span>
      <span>{value}</span>
    </div>
  )
}
