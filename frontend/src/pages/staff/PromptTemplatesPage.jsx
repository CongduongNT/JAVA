import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useLocation } from 'react-router-dom';
import { Plus, Edit, Trash2, CheckCircle, XCircle, Sparkles, BookOpen, Search, Info } from 'lucide-react';
import { fetchTemplates, deleteTemplate, approveTemplate } from '../../features/promptTemplates/promptTemplateSlice';
import ApprovalStatusBadge from '../../components/ui/ApprovalStatusBadge';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '../../components/ui/card';
import { toast } from 'sonner';

export default function PromptTemplatesPage() {
  const dispatch = useDispatch();
  const location = useLocation();

  // Thêm fallback {} để tránh crash nếu chưa đăng ký reducer trong store
  const { templates = [], loading = false } = useSelector((state) => state.promptTemplates || {});
  
  // Sử dụng optional chaining để an toàn hơn
  const user = useSelector((state) => state.auth?.user);
  
  const rawRole = user?.roleName || (user?.role && typeof user.role === 'object' ? user.role.name : user?.role) || '';
  const userRole = rawRole.toUpperCase().replace('ROLE_', '');
  const isManagerOrAdmin = ['MANAGER', 'ADMIN'].includes(userRole);
  const isTeacher = userRole === 'TEACHER';
  const isStaff = userRole === 'STAFF';
  
  // Kiểm tra xem đang ở trang duyệt (Manager) hay trang quản lý (Staff)
  const isApprovalView = location.pathname === '/manager/approve';
  const isLessonPlanLibrary = location.pathname.includes('/lesson-plans');

  useEffect(() => {
    // Nếu giáo viên vào Thư viện giáo án, chỉ lấy các mẫu LESSON_PLAN_GEN
    if (isLessonPlanLibrary) {
      dispatch(fetchTemplates({ purpose: 'LESSON_PLAN_GEN' }));
    } else {
      // Staff/Manager xem toàn bộ
      dispatch(fetchTemplates());
    }
  }, [dispatch, isLessonPlanLibrary]);

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

  const handleApproval = async (id, status) => {
    const actionText = status === 'APPROVED' ? 'duyệt' : 'từ chối';
    if (window.confirm(`Xác nhận ${actionText} template này?`)) {
      try {
        // Truyền thêm status vào action để BE xử lý chuyển trạng thái tương ứng
        await dispatch(approveTemplate({ id, status })).unwrap();
        toast.success(`Đã ${actionText} template thành công`);
      } catch (err) {
        toast.error(err);
      }
    }
  };

  if (loading && templates.length === 0) return <div className="p-8 text-center">Đang tải...</div>;

  // UX: Nếu là trang phê duyệt, chỉ hiển thị những mục cần xử lý (PENDING)
  // Đảm bảo templates luôn là mảng để không bị crash
  const safeTemplates = Array.isArray(templates) ? templates : [];
  const displayTemplates = isApprovalView 
    ? safeTemplates.filter(t => t?.status === 'PENDING')
    : safeTemplates;

  // Giao diện Thư viện dành riêng cho Teacher
  const renderTeacherLibrary = () => (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {displayTemplates.length === 0 ? (
        <div className="col-span-full py-20 text-center bg-gray-50/50 rounded-2xl border-2 border-dashed border-gray-200">
          <BookOpen className="size-12 text-gray-300 mx-auto mb-4" />
          <p className="text-gray-500 font-medium">Hiện chưa có mẫu giáo án nào khả dụng.</p>
          <p className="text-sm text-gray-400">Vui lòng quay lại sau khi Manager đã phê duyệt nội dung mới.</p>
        </div>
      ) : (
        displayTemplates.map((item) => (
          <Card key={item.id} className="hover:shadow-md transition-shadow border-t-4 border-t-primary">
            <CardHeader className="pb-2">
              <div className="flex justify-between items-start">
                <span className="px-2 py-0.5 bg-primary/10 text-primary text-[10px] font-bold uppercase rounded">
                  {item.purpose}
                </span>
                <Sparkles className="size-4 text-amber-500" />
              </div>
              <CardTitle className="text-lg mt-2 line-clamp-1">{item.title}</CardTitle>
              <CardDescription className="line-clamp-2 min-h-[40px]">
                Sử dụng mẫu này để AI hỗ trợ tạo nội dung bài giảng chuyên nghiệp.
              </CardDescription>
            </CardHeader>
            <CardContent className="py-2">
              <div className="flex flex-wrap gap-1.5">
                {(item.variables?.split(',') || [])
                  .filter(v => v.trim() !== "")
                  .map((v, idx) => (
                    <span key={idx} className="text-[10px] bg-gray-100 text-gray-600 px-1.5 py-0.5 rounded font-mono">
                      {v.trim()}
                    </span>
                  ))}
              </div>
            </CardContent>
            <CardFooter className="pt-4">
              <Link
                to={`/generate-lesson-plan/${item.id}`}
                className="w-full inline-flex items-center justify-center gap-2 rounded-lg bg-primary py-2 text-sm font-bold text-white shadow-sm hover:bg-primary/90 transition-all"
              >
                <Sparkles className="size-4" />
                Sử dụng ngay
              </Link>
            </CardFooter>
          </Card>
        ))
      )}
    </div>
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-gray-900">
            {isApprovalView ? 'Phê duyệt Prompt Templates' : 
             isTeacher ? 'Thư viện Giáo án AI' : 'Quản lý Prompt Templates'}
          </h1>
          <p className="text-sm text-gray-500">
            {isApprovalView ? 'Xét duyệt các câu lệnh AI mới từ nhân viên.' : 
             isTeacher ? 'Chọn mẫu giáo án và để AI hỗ trợ bạn soạn bài trong giây lát.' : 'Tạo và quản lý các câu lệnh AI để hỗ trợ giáo viên.'}
          </p>
        </div>
        {!isApprovalView && !isTeacher && (
          <Link
            to="/prompt-templates/new"
            className="inline-flex items-center gap-2 rounded-md bg-primary px-4 py-2 text-sm font-medium text-white shadow hover:bg-primary/90"
          >
            <Plus className="size-4" />
            Thêm mới
          </Link>
        )}
      </div>

      {isTeacher ? (
        renderTeacherLibrary()
      ) : (
        <div className="rounded-lg border bg-white shadow-sm overflow-hidden">
          <table className="w-full text-left text-sm">
            <thead className="bg-gray-50 text-xs uppercase text-gray-700 font-semibold border-b">
              <tr>
                <th className="px-6 py-4">Tiêu đề</th>
                {isApprovalView && <th className="px-6 py-4">Người tạo</th>}
                <th className="px-6 py-4">Mục đích</th>
                <th className="px-6 py-4">Biến số</th>
                <th className="px-6 py-4">Trạng thái</th>
                <th className="px-6 py-4 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {displayTemplates.length === 0 && (
                <tr>
                  <td colSpan="5" className="px-6 py-12 text-center text-gray-500 italic">
                    Danh sách hiện đang trống.
                  </td>
                </tr>
              )}
              {displayTemplates.map((item) => (
                <tr key={item.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 font-medium text-gray-900">{item.title}</td>
                  {isApprovalView && (
                    <td className="px-6 py-4 text-gray-600 text-xs font-semibold">{item.createdByName || 'N/A'}</td>
                  )}
                  <td className="px-6 py-4">
                    <span className="px-2 py-1 bg-blue-50 text-blue-600 rounded text-xs font-medium">
                      {item.purpose}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-gray-500 font-mono text-xs italic">{item.variables}</td>
                  <td className="px-6 py-4">
                    <ApprovalStatusBadge 
                      status={item.status} 
                      approvedByName={item.approvedByName} 
                      size="sm"
                    />
                  </td>
                  <td className="px-6 py-4 text-right flex justify-end gap-2">
                    {isManagerOrAdmin && item.status === 'PENDING' && (
                      <div className="flex gap-1">
                        <button
                          onClick={() => handleApproval(item.id, 'APPROVED')}
                          className="p-2 text-gray-400 hover:text-emerald-600 transition-colors"
                        >
                          <CheckCircle className="size-4" />
                        </button>
                        <button
                          onClick={() => handleApproval(item.id, 'REJECTED')}
                          className="p-2 text-gray-400 hover:text-red-600 transition-colors"
                        >
                          <XCircle className="size-4" />
                        </button>
                      </div>
                    )}
                    {!isApprovalView && (
                      <>
                        <Link to={`/prompt-templates/${item.id}/edit`} className="p-2 text-gray-400 hover:text-primary">
                          <Edit className="size-4" />
                        </Link>
                        <button onClick={() => handleDelete(item.id)} className="p-2 text-gray-400 hover:text-red-600">
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
      )}
    </div>
  );
}
