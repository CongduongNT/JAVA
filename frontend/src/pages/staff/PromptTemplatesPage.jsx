import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useLocation } from 'react-router-dom';
import { Plus, Edit, Trash2, CheckCircle } from 'lucide-react';
import { fetchTemplates, deleteTemplate, approveTemplate } from '../../features/promptTemplates/promptTemplateSlice';
import { toast } from 'sonner';

export default function PromptTemplatesPage() {
  const dispatch = useDispatch();
  const location = useLocation();
  
  // Thêm fallback {} để tránh crash nếu chưa đăng ký reducer trong store
  const { templates = [], loading = false } = useSelector((state) => state.promptTemplates || {});
  
  const user = useSelector((state) => state.auth.user);
  const isManagerOrAdmin = ['MANAGER', 'ADMIN'].includes(user?.role?.toUpperCase());
  
  // Kiểm tra xem đang ở trang duyệt (Manager) hay trang quản lý (Staff)
  const isApprovalView = location.pathname.includes('/manager/');

  useEffect(() => {
    dispatch(fetchTemplates());
  }, [dispatch]);

  const handleDelete = async (id) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa template này?')) {
      try {
        await dispatch(deleteTemplate(id)).unwrap();
        toast.success('Xóa template thành công');
      } catch (err) {
        toast.error(err);
      }
    }
  };

  const handleApprove = async (id) => {
    if (window.confirm('Xác nhận duyệt template này?')) {
      try {
        await dispatch(approveTemplate(id)).unwrap();
        toast.success('Đã duyệt template thành công');
      } catch (err) {
        toast.error(err);
      }
    }
  };

  if (loading && templates.length === 0) return <div className="p-8 text-center">Đang tải...</div>;

  // Lọc template: Manager chỉ xem PENDING, Staff xem tất cả
  const displayTemplates = isApprovalView 
    ? templates.filter(t => t.status === 'PENDING')
    : templates;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-gray-900">
            {isApprovalView ? 'Phê duyệt Prompt Templates' : 'Quản lý Prompt Templates'}
          </h1>
          <p className="text-sm text-gray-500">
            {isApprovalView ? 'Xét duyệt các câu lệnh AI mới từ nhân viên.' : 'Tạo và quản lý các câu lệnh AI để hỗ trợ giáo viên.'}
          </p>
        </div>
        {!isApprovalView && (
          <Link
            to="/prompt-templates/new"
            className="inline-flex items-center gap-2 rounded-md bg-primary px-4 py-2 text-sm font-medium text-white shadow hover:bg-primary/90"
          >
            <Plus className="size-4" />
            Thêm mới
          </Link>
        )}
      </div>

      <div className="rounded-lg border bg-white shadow-sm overflow-hidden">
        <table className="w-full text-left text-sm">
          <thead className="bg-gray-50 text-xs uppercase text-gray-700 font-semibold border-b">
            <tr>
              <th className="px-6 py-4">Tiêu đề</th>
              <th className="px-6 py-4">Mục đích</th>
              <th className="px-6 py-4">Biến số</th>
              <th className="px-6 py-4">Trạng thái</th>
              <th className="px-6 py-4 text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {displayTemplates.map((item) => (
              <tr key={item.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-6 py-4 font-medium text-gray-900">{item.title}</td>
                <td className="px-6 py-4">
                  <span className="px-2 py-1 bg-blue-50 text-blue-600 rounded text-xs font-medium">
                    {item.purpose}
                  </span>
                </td>
                <td className="px-6 py-4 text-gray-500 font-mono text-xs italic">{item.variables}</td>
                <td className="px-6 py-4">
                  <div className="flex flex-col gap-1">
                    <span className={`w-fit px-2 py-1 rounded text-xs font-medium ${
                      item.status === 'APPROVED' ? 'bg-green-100 text-green-700' : 
                      item.status === 'REJECTED' ? 'bg-red-100 text-red-700' : 'bg-yellow-100 text-yellow-700'
                    }`}>
                      {item.status}
                    </span>
                    {item.status === 'APPROVED' && item.approvedByName && (
                      <span className="text-[10px] text-gray-400 italic">Bởi: {item.approvedByName}</span>
                    )}
                  </div>
                </td>
                <td className="px-6 py-4 text-right flex justify-end gap-2">
                  {isManagerOrAdmin && item.status === 'PENDING' && (
                    <button
                      onClick={() => handleApprove(item.id)}
                      className="p-2 text-gray-400 hover:text-green-600 transition-colors"
                      title="Duyệt template"
                    >
                      <CheckCircle className="size-4" />
                    </button>
                  )}
                  {!isApprovalView && (
                    <>
                      <Link
                        to={`/prompt-templates/${item.id}/edit`}
                        className="p-2 text-gray-400 hover:text-primary transition-colors"
                        title="Chỉnh sửa"
                      >
                        <Edit className="size-4" />
                      </Link>
                      <button
                        onClick={() => handleDelete(item.id)}
                        className="p-2 text-gray-400 hover:text-red-600 transition-colors"
                        title="Xóa"
                      >
                        <Trash2 className="size-4" />
                      </button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
