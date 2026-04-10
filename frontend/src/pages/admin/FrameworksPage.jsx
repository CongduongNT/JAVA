import React, { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Plus,
  Edit,
  Trash2,
  CheckCircle2,
  XCircle,
  Clock,
  RefreshCw,
  Filter,
  ChevronLeft,
  ChevronRight,
  BookOpen,
  AlertCircle,
} from 'lucide-react';
import frameworkApi from '../../services/frameworkApi';
import { toast } from 'sonner';

/**
 * AdminFrameworksPage – Màn hình Admin quản lý Curriculum Frameworks.
 *
 * Chức năng:
 * - Xem danh sách frameworks với phân trang
 * - Filter theo subject, gradeLevel
 * - Tạo, sửa, xóa framework
 * - Publish/Unpublish framework
 */
const AdminFrameworksPage = () => {
  const navigate = useNavigate();
  const [frameworks, setFrameworks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState('ALL'); // ALL | PUBLISHED | DRAFT
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
  });
  const [filters, setFilters] = useState({
    subject: '',
    gradeLevel: '',
  });
  const [actionLoading, setActionLoading] = useState(null);

  const fetchFrameworks = useCallback(async () => {
    setLoading(true);
    try {
      const params = {
        page: pagination.page,
        size: pagination.size,
        ...(filters.subject && { subject: filters.subject }),
        ...(filters.gradeLevel && { gradeLevel: filters.gradeLevel }),
      };
      
      const res = await frameworkApi.getFrameworks(params);
      let data = res.data;
      
      // Filter theo status trên client (vì API Admin trả về tất cả)
      if (filterStatus === 'PUBLISHED') {
        data.content = data.content.filter(f => f.isPublished);
      } else if (filterStatus === 'DRAFT') {
        data.content = data.content.filter(f => !f.isPublished);
      }
      
      setFrameworks(data.content || []);
      setPagination({
        page: data.page || 0,
        size: data.size || 10,
        totalElements: data.totalElements || 0,
        totalPages: data.totalPages || 0,
      });
    } catch (err) {
      console.error('Failed to fetch frameworks:', err);
      toast.error('Không thể tải danh sách frameworks.');
    } finally {
      setLoading(false);
    }
  }, [pagination.page, pagination.size, filters, filterStatus]);

  useEffect(() => {
    fetchFrameworks();
  }, [fetchFrameworks]);

  const handleDelete = async (id) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa framework này?')) {
      try {
        await frameworkApi.deleteFramework(id);
        toast.success('Xóa framework thành công');
        fetchFrameworks();
      } catch (err) {
        toast.error('Không thể xóa framework. Vui lòng thử lại.');
      }
    }
  };

  const handlePublish = async (id) => {
    setActionLoading(id);
    try {
      await frameworkApi.publishFramework(id);
      toast.success('Đã publish framework thành công!');
      fetchFrameworks();
    } catch (err) {
      toast.error('Không thể publish framework.');
    } finally {
      setActionLoading(null);
    }
  };

  const handleUnpublish = async (id) => {
    setActionLoading(id);
    try {
      await frameworkApi.unpublishFramework(id);
      toast.success('Đã unpublish framework thành công!');
      fetchFrameworks();
    } catch (err) {
      toast.error('Không thể unpublish framework.');
    } finally {
      setActionLoading(null);
    }
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < pagination.totalPages) {
      setPagination(prev => ({ ...prev, page: newPage }));
    }
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
    setPagination(prev => ({ ...prev, page: 0 }));
  };

  const formatDate = (dateString) => {
    if (!dateString) return '—';
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="p-4 md:p-8 space-y-6 max-w-7xl mx-auto">
      {/* ── Header ── */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-bold text-slate-900 leading-tight">
            Quản lý Curriculum Frameworks
          </h1>
          <p className="text-slate-500 mt-1 text-sm">
            Tạo và quản lý các chương trình khung giáo dục.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={fetchFrameworks}
            className="flex items-center gap-2 px-4 py-2 bg-slate-100 text-slate-700 font-medium rounded-lg hover:bg-slate-200 transition-all text-sm"
          >
            <RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
            Làm mới
          </button>
          <Link
            to="/admin/frameworks/new"
            className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 transition-all text-sm"
          >
            <Plus size={16} />
            Thêm mới
          </Link>
        </div>
      </div>

      {/* ── Filters ── */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 space-y-4">
        <div className="flex flex-col md:flex-row gap-4">
          {/* Status Filter */}
          <div className="flex gap-1 bg-slate-100 p-1 rounded-lg w-fit">
            {[
              { value: 'ALL', label: 'Tất cả', icon: Filter },
              { value: 'PUBLISHED', label: 'Đã publish', icon: CheckCircle2 },
              { value: 'DRAFT', label: 'Bản nháp', icon: Clock },
            ].map(({ value, label, icon: Icon }) => (
              <button
                key={value}
                onClick={() => {
                  setFilterStatus(value);
                  setPagination(prev => ({ ...prev, page: 0 }));
                }}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium transition-all ${
                  filterStatus === value
                    ? 'bg-white text-indigo-700 shadow-sm'
                    : 'text-slate-500 hover:text-slate-700'
                }`}
              >
                <Icon size={14} />
                {label}
              </button>
            ))}
          </div>

          {/* Subject Filter */}
          <input
            type="text"
            name="subject"
            placeholder="Lọc theo môn học..."
            value={filters.subject}
            onChange={handleFilterChange}
            className="px-4 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
          />

          {/* Grade Filter */}
          <input
            type="text"
            name="gradeLevel"
            placeholder="Lọc theo khối..."
            value={filters.gradeLevel}
            onChange={handleFilterChange}
            className="px-4 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
          />
        </div>
      </div>

      {/* ── Content ── */}
      {loading ? (
        <div className="flex flex-col items-center justify-center py-24 gap-3">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-500" />
          <p className="text-slate-500 text-sm font-medium animate-pulse">Đang tải frameworks...</p>
        </div>
      ) : frameworks.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 bg-slate-50 border-2 border-dashed border-slate-200 rounded-2xl">
          <BookOpen className="text-slate-300 mb-3" size={48} />
          <p className="text-slate-500 font-medium">Chưa có framework nào.</p>
          <Link
            to="/admin/frameworks/new"
            className="mt-4 text-indigo-600 hover:text-indigo-700 text-sm font-medium"
          >
            Tạo framework đầu tiên →
          </Link>
        </div>
      ) : (
        <>
          <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase text-slate-700 font-semibold border-b">
                <tr>
                  <th className="px-6 py-4">Tiêu đề</th>
                  <th className="px-6 py-4">Môn học</th>
                  <th className="px-6 py-4">Khối</th>
                  <th className="px-6 py-4">Trạng thái</th>
                  <th className="px-6 py-4">Ngày tạo</th>
                  <th className="px-6 py-4 text-right">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {frameworks.map((framework) => (
                  <tr key={framework.id} className="hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4">
                      <div>
                        <p className="font-medium text-slate-900">{framework.title}</p>
                        {framework.description && (
                          <p className="text-xs text-slate-500 mt-0.5 line-clamp-1">{framework.description}</p>
                        )}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-50 text-blue-700">
                        {framework.subject || '—'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-slate-600">{framework.gradeLevel || '—'}</td>
                    <td className="px-6 py-4">
                      {framework.isPublished ? (
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                          <CheckCircle2 size={11} /> Đã publish
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200">
                          <Clock size={11} /> Bản nháp
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-xs text-slate-500">
                      {formatDate(framework.createdAt)}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        {/* Publish/Unpublish */}
                        {framework.isPublished ? (
                          <button
                            onClick={() => handleUnpublish(framework.id)}
                            disabled={actionLoading === framework.id}
                            className="p-2 text-slate-400 hover:text-amber-600 hover:bg-amber-50 rounded-lg transition-all"
                            title="Unpublish"
                          >
                            {actionLoading === framework.id ? (
                              <div className="animate-spin h-4 w-4 border-b-2 border-amber-600 rounded-full" />
                            ) : (
                              <XCircle size={18} />
                            )}
                          </button>
                        ) : (
                          <button
                            onClick={() => handlePublish(framework.id)}
                            disabled={actionLoading === framework.id}
                            className="p-2 text-slate-400 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition-all"
                            title="Publish"
                          >
                            {actionLoading === framework.id ? (
                              <div className="animate-spin h-4 w-4 border-b-2 border-emerald-600 rounded-full" />
                            ) : (
                              <CheckCircle2 size={18} />
                            )}
                          </button>
                        )}

                        {/* Edit */}
                        <Link
                          to={`/admin/frameworks/${framework.id}/edit`}
                          className="p-2 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-all"
                          title="Chỉnh sửa"
                        >
                          <Edit size={18} />
                        </Link>

                        {/* Delete */}
                        <button
                          onClick={() => handleDelete(framework.id)}
                          className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-all"
                          title="Xóa"
                        >
                          <Trash2 size={18} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* ── Pagination ── */}
          {pagination.totalPages > 1 && (
            <div className="flex items-center justify-between">
              <p className="text-sm text-slate-500">
                Hiển thị <span className="font-medium text-slate-900">{frameworks.length}</span> /{' '}
                <span className="font-medium text-slate-900">{pagination.totalElements}</span> frameworks
              </p>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => handlePageChange(pagination.page - 1)}
                  disabled={pagination.page === 0}
                  className="p-2 rounded-lg border border-slate-300 text-slate-600 hover:bg-slate-50 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                >
                  <ChevronLeft size={16} />
                </button>
                <span className="px-4 py-2 text-sm font-medium text-slate-700">
                  Trang {pagination.page + 1} / {pagination.totalPages}
                </span>
                <button
                  onClick={() => handlePageChange(pagination.page + 1)}
                  disabled={pagination.page >= pagination.totalPages - 1}
                  className="p-2 rounded-lg border border-slate-300 text-slate-600 hover:bg-slate-50 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                >
                  <ChevronRight size={16} />
                </button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default AdminFrameworksPage;
