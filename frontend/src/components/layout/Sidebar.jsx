import React from 'react';
import { NavLink } from 'react-router-dom';
import { useSelector } from 'react-redux';
import {
  LayoutDashboard,
  BookCopy,
  FileText,
  Bot,
  Users,
  Settings,
  BarChart3,
  Package,
  BookOpen,
  Shield,
} from 'lucide-react';

/**
 * Định nghĩa danh sách menu với property `roles`.
 * roles: [] hoặc không có → hiển thị cho tất cả authenticated user.
 * roles: ['ADMIN'] → chỉ hiện với ADMIN.
 */
const ALL_NAV_ITEMS = [
  // ----- Shared (all roles) -----
  {
    name: 'Dashboard',
    icon: LayoutDashboard,
    path: '/dashboard',
    roles: [], // all roles
  },

  // ----- TEACHER -----
  {
    name: 'My Lesson Plans',
    icon: BookOpen,
    path: '/lesson-plans',
    roles: ['TEACHER'],
  },
  {
    name: 'Packages',
    icon: Package,
    path: '/packages',
    roles: ['TEACHER'],
  },
  {
    name: 'Order History',
    icon: Settings,
    path: '/orders/history',
    roles: ['TEACHER'],
  },
  {
    name: 'Question Bank',
    icon: BookCopy,
    path: '/question-bank',
    roles: ['TEACHER', 'STAFF'],
  },
  {
    name: 'Exam Generator',
    icon: FileText,
    path: '/exam-generator',
    roles: ['TEACHER'],
  },
  {
    name: 'OCR Grading',
    icon: Bot,
    path: '/ocr-grading',
    roles: ['TEACHER'],
  },

  // ----- STAFF -----
  {
    name: 'Prompt Templates',
    icon: Settings,
    path: '/prompt-templates',
    roles: ['STAFF', 'MANAGER'],
  },

  // ----- MANAGER -----
  {
    name: 'Manage Teachers',
    icon: Users,
    path: '/manager/teachers',
    roles: ['MANAGER', 'ADMIN'],
  },
  {
    name: 'Packages',
    icon: Package,
    path: '/manager/subscriptions',
    roles: ['MANAGER', 'ADMIN'],
  },
  {
    name: 'Orders',
    icon: BookCopy,
    path: '/manager/orders',
    roles: ['MANAGER', 'ADMIN'],
  },
  {
    name: 'Analytics',
    icon: BarChart3,
    path: '/manager/analytics',
    roles: ['MANAGER', 'ADMIN'],
  },

  // ----- ADMIN -----
  {
    name: 'User Management',
    icon: Shield,
    path: '/admin/users',
    roles: ['ADMIN'],
  },
];

const ROLE_LABELS = {
  ADMIN: { label: 'Admin', color: 'bg-red-100 text-red-700' },
  MANAGER: { label: 'Manager', color: 'bg-purple-100 text-purple-700' },
  STAFF: { label: 'Staff', color: 'bg-blue-100 text-blue-700' },
  TEACHER: { label: 'Teacher', color: 'bg-green-100 text-green-700' },
};

const Sidebar = () => {
  const { user } = useSelector((state) => state.auth);
  const userRole = user?.role?.toUpperCase() || '';

  // Lọc danh sách menu theo role của user hiện tại
  const visibleItems = ALL_NAV_ITEMS.filter((item) => {
    if (!item.roles || item.roles.length === 0) return true; // hiển thị cho tất cả
    return item.roles.map((r) => r.toUpperCase()).includes(userRole);
  });

  const roleInfo = ROLE_LABELS[userRole] || { label: userRole, color: 'bg-slate-100 text-slate-600' };

  return (
    <aside className="w-64 flex-shrink-0 bg-white border-r border-slate-200 flex flex-col">
      {/* Logo */}
      <div className="h-16 flex items-center justify-center border-b border-slate-200">
        <h1 className="text-2xl font-semibold font-display text-blue-600">PlanbookAI</h1>
      </div>

      {/* User role badge */}
      {userRole && (
        <div className="px-4 py-3 border-b border-slate-100 flex items-center gap-2">
          <div className="w-8 h-8 rounded-full bg-blue-500 flex items-center justify-center text-white text-sm font-bold flex-shrink-0">
            {user?.fullName?.charAt(0)?.toUpperCase() || '?'}
          </div>
          <div className="min-w-0">
            <p className="text-xs font-semibold text-slate-700 truncate">{user?.fullName}</p>
            <span className={`inline-block text-[10px] font-bold px-1.5 py-0.5 rounded ${roleInfo.color}`}>
              {roleInfo.label}
            </span>
          </div>
        </div>
      )}

      <nav className="flex-1 px-4 py-4 overflow-y-auto">
        <ul className="space-y-0.5">
          {Array.isArray(visibleItems) && visibleItems.map((item) => (
            <li key={item.path}>
              <NavLink
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center px-4 py-2.5 text-sm font-medium rounded-lg transition-colors ${
                    isActive
                      ? 'bg-blue-50 text-blue-600'
                      : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                  }`
                }
              >
                <item.icon className="w-5 h-5 mr-3 flex-shrink-0" />
                {item.name}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>
    </aside>
  );
};

export default Sidebar;
