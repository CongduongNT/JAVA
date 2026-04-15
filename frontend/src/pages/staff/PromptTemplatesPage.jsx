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
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
      {displayTemplates.length === 0 ? (
        <div className="col-span-full py-32 text-center bg-white rounded-[2rem] border-2 border-dashed border-slate-200 shadow-sm">
          <div className="bg-slate-50 size-20 rounded-full flex items-center justify-center mx-auto mb-6">
            <BookOpen className="size-10 text-slate-300" />
          </div>
          <p className="text-slate-600 font-bold text-lg">Thư viện hiện đang trống</p>
          <p className="text-sm text-gray-400">Vui lòng quay lại sau khi Manager đã phê duyệt nội dung mới.</p>
        </div>
      ) : (
        displayTemplates.map((item) => (
          <Card key={item.id} className="group hover:shadow-2xl hover:-translate-y-1 transition-all duration-300 border-none bg-white rounded-[2rem] overflow-hidden shadow-xl shadow-slate-200/60">
            <div className="h-2 bg-gradient-to-r from-indigo-500 to-blue-500" />
            <CardHeader className="pb-4 pt-6">
              <div className="flex justify-between items-start">
                <span className="px-2.5 py-1 bg-indigo-50 text-indigo-600 text-[10px] font-black uppercase tracking-wider rounded-lg">
                  {item.purpose}
                </span>
                <Sparkles className="size-5 text-amber-400 fill-amber-400 group-hover:scale-110 transition-transform" />
              </div>
              <CardTitle className="text-xl font-black mt-4 text-slate-800 line-clamp-1">{item.title}</CardTitle>
              <CardDescription className="line-clamp-2 min-h-[40px] text-slate-500 font-medium mt-2">
                {item.description || 'Sử dụng mẫu này để AI hỗ trợ tạo nội dung bài giảng chuyên nghiệp theo khung chuẩn.'}
              </CardDescription>
            </CardHeader>
            <CardContent className="py-4">
              <div className="flex flex-wrap gap-1.5">
                {(item.variables?.split(',') || [])
                  .filter(v => v.trim() !== "")
                  .map((v, idx) => (
                    <span key={idx} className="text-[10px] bg-slate-100 text-slate-500 px-2 py-1 rounded-md font-bold uppercase tracking-tighter">
                      #{v.trim()}
                    </span>
                  ))}
              </div>
            </CardContent>
            <CardFooter className="pt-4">
              <Link
                to={`/generate-lesson-plan/${item.id}`}
                className="w-full inline-flex items-center justify-center gap-2 rounded-2xl bg-slate-900 py-4 text-xs font-black uppercase tracking-widest text-white shadow-lg hover:bg-indigo-600 hover:shadow-indigo-200 transition-all active:scale-95"
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
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="flex items-center justify-between bg-white p-8 rounded-[2rem] shadow-sm border border-slate-100">
        <div>
          <h1 className="text-3xl font-black tracking-tight text-slate-900">
            {isApprovalView ? 'Phê duyệt Prompt Templates' : 
             isTeacher ? 'Thư viện Giáo án ' : 'Quản lý Prompt Templates'}
             {isTeacher && <span className="text-indigo-600">AI</span>}
          </h1>
          <p className="text-sm text-slate-500 font-medium mt-1">
            {isApprovalView ? 'Xét duyệt các câu lệnh AI mới từ nhân viên.' : 
             isTeacher ? 'Chọn mẫu giáo án và để AI hỗ trợ bạn soạn bài trong giây lát.' : 'Tạo và quản lý các câu lệnh AI để hỗ trợ giáo viên.'}
          </p>
        </div>
        {!isApprovalView && !isTeacher && (
          <Link
            to="/prompt-templates/new"
            className="inline-flex items-center gap-2 rounded-xl bg-indigo-600 px-6 py-3 text-sm font-bold text-white shadow-lg shadow-indigo-100 hover:bg-indigo-700 transition-all"
          >
            <Plus className="size-4" />
            Thêm mới
          </Link>
        )}
      </div>

      {isTeacher ? (
        renderTeacherLibrary()
      ) : (
        <div className="rounded-[2rem] border-none bg-white shadow-xl shadow-slate-200/50 overflow-hidden">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50/50 text-xs uppercase text-slate-500 font-black tracking-widest border-b border-slate-100">
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
                  <td colSpan="6" className="px-6 py-12 text-center text-slate-400 font-medium">
                    Danh sách hiện đang trống.
                  </td>
                </tr>
              )}
              {displayTemplates.map((item) => (
                <tr key={item.id} className="hover:bg-slate-50/30 transition-colors">
                  <td className="px-6 py-5 font-bold text-slate-800">{item.title}</td>
                  {isApprovalView && (
                    <td className="px-6 py-5 text-slate-600 text-xs font-bold">{item.createdByName || 'N/A'}</td>
                  )}
                  <td className="px-6 py-5">
                    <span className="px-2.5 py-1 bg-blue-50 text-blue-600 rounded-lg text-[10px] font-black uppercase tracking-wider">
                      {item.purpose}
                    </span>
                  </td>
                  <td className="px-6 py-5">
                    <div className="flex flex-wrap gap-1">
                      {item.variables?.split(',').map((v, i) => (
                        <span key={i} className="px-1.5 py-0.5 bg-slate-100 text-slate-500 rounded text-[9px] font-mono">
                          {v.trim()}
                        </span>
                      ))}
                    </div>
                  </td>
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
