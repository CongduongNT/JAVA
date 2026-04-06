import React from 'react';
import { CheckCircle2, Clock, XCircle } from 'lucide-react';

/**
 * ApprovalStatusBadge – Badge hiển thị trạng thái duyệt của câu hỏi.
 *
 * Props:
 *   - isApproved  {boolean|null}  – true = đã duyệt, false/null = chờ duyệt
 *   - approvedBy  {string}        – Tên người duyệt (hiển thị tooltip)
 *   - size        {'sm'|'md'}     – Kích thước badge (mặc định 'sm')
 */
const ApprovalStatusBadge = ({ isApproved, approvedByName, size = 'sm' }) => {
  const baseClass =
    size === 'md'
      ? 'inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold border'
      : 'inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold border';

  if (isApproved) {
    return (
      <span
        className={`${baseClass} bg-emerald-50 text-emerald-700 border-emerald-200`}
        title={approvedByName ? `Duyệt bởi: ${approvedByName}` : 'Đã được duyệt'}
      >
        <CheckCircle2 size={size === 'md' ? 13 : 11} />
        Đã duyệt
        {approvedByName && size === 'md' && (
          <span className="text-emerald-500 font-normal">· {approvedByName}</span>
        )}
      </span>
    );
  }

  return (
    <span
      className={`${baseClass} bg-amber-50 text-amber-700 border-amber-200`}
      title="Câu hỏi đang chờ Manager duyệt"
    >
      <Clock size={size === 'md' ? 13 : 11} />
      Chờ duyệt
    </span>
  );
};

export default ApprovalStatusBadge;
