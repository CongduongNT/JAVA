import React from 'react';
import { useSelector } from 'react-redux';
import { Navigate, useLocation } from 'react-router-dom';

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
  const { user, isAuthenticated } = useSelector((state) => state.auth);
  const location = useLocation();

  // Chưa đăng nhập → về trang login
  if (!isAuthenticated || !user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Không truyền roles → chỉ cần authenticated là vào được
  if (roles.length === 0) {
    return children;
  }

  // Kiểm tra role của user có nằm trong danh sách roles cho phép không
  const userRole = user.role?.toUpperCase();
  const hasAccess = roles.map((r) => r.toUpperCase()).includes(userRole);

  if (!hasAccess) {
    return <Navigate to={redirectTo} state={{ from: location }} replace />;
  }

  return children;
};

export default RoleGuard;
