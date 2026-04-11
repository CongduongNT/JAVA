import React from 'react';
import { useSelector } from 'react-redux';
import { Navigate, useLocation } from 'react-router-dom';
import { Loader2 } from 'lucide-react';

/**
 * RoleGuard – bảo vệ route theo role.
 *
 * Cách dùng:
 *   <RoleGuard roles={['ADMIN']}>
 *     <AdminPage />
 *   </RoleGuard>
 *
 *   <RoleGuard roles={['ADMIN', 'MANAGER']}>
 *     <ManagerPage />
 *   </RoleGuard>
 *
 * @param {string[]} roles - Mảng các role được phép truy cập
 * @param {React.ReactNode} children - Component con cần bảo vệ
 * @param {string} redirectTo - Đường dẫn redirect khi không có quyền (mặc định: /unauthorized)
 */
const RoleGuard = ({ roles = [], children, redirectTo = '/unauthorized' }) => {
  // Thêm fallback {} để tránh crash nếu auth slice chưa load
  const { user, isAuthenticated, loading } = useSelector((state) => state.auth || {});
  const location = useLocation();

  // Nếu đang xác thực hoặc đã auth nhưng chưa có data user (đang fetch /me)
  // thì hiển thị loading thay vì redirect nhầm
  if (loading || (isAuthenticated && !user)) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Loader2 className="size-8 animate-spin text-primary" />
      </div>
    );
  }

  // Chưa đăng nhập → về trang login
  if (!isAuthenticated || !user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Không truyền roles → chỉ cần authenticated là vào được
  if (roles.length === 0) {
    return children;
  }

  // Kiểm tra role của user có nằm trong danh sách roles cho phép không
  // Backend trả về roleName, nên cần kiểm tra đúng thuộc tính này
  // Đảm bảo lấy được chuỗi role cho dù role là string hay object
  const rawRole = user?.roleName || (user?.role && typeof user.role === 'object' ? user.role.name : user?.role) || '';
  
  // Normalize role: UpperCase and strip ROLE_ prefix (e.g., ROLE_ADMIN -> ADMIN)
  const userRole = rawRole.toUpperCase().replace('ROLE_', '');
  
  // Mặc định ADMIN luôn có quyền truy cập vào các trang quản trị/nhân sự
  // Nếu danh sách roles có MANAGER, Manager sẽ được vào
  // Loại bỏ ROLE_ ở cả danh sách roles yêu cầu để đảm bảo so sánh khớp
  const normalizedRoles = (roles || []).map((r) => r.toUpperCase().replace('ROLE_', ''));
  const hasAccess = userRole === 'ADMIN' || normalizedRoles.includes(userRole);

  if (!hasAccess) {
    return <Navigate to={redirectTo} state={{ from: location }} replace />;
  }

  return children;
};

export default RoleGuard;
