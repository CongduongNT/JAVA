import React, { useEffect, useState } from 'react';
import { Search, Mail, Phone, Calendar, UserCheck, UserX, Loader2 } from 'lucide-react';
import { apiClient } from '../../features/auth/authApi';
import { toast } from 'sonner';

export default function TeacherManagementPage() {
  const [teachers, setTeachers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    const fetchTeachers = async () => {
      try {
        const response = await apiClient.get('/users');
        const allUsers = Array.isArray(response.data) ? response.data : [];
        
        // Lọc lấy những người có role là TEACHER
        const filtered = allUsers.filter(u => {
          const roleName = u.roleName || (typeof u.role === 'object' ? u.role?.name : u.role);
          return roleName?.toUpperCase().replace('ROLE_', '') === 'TEACHER';
        });
        
        setTeachers(filtered);
      } catch (err) {
        toast.error("Không thể tải danh sách giáo viên: " + (err.response?.data?.message || err.message));
      } finally {
        setLoading(false);
      }
    };

    fetchTeachers();
  }, []);

  const filteredTeachers = teachers.filter(t => 
    t.fullName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    t.email?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) return (
    <div className="flex h-64 flex-col items-center justify-center gap-2">
      <Loader2 className="size-8 animate-spin text-primary" />
      <p className="text-sm text-gray-500 italic">Đang tải danh sách giáo viên...</p>
    </div>
  );

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-gray-900">Quản lý Giáo viên</h1>
        <p className="text-sm text-gray-500">Xem danh sách và thông tin liên hệ của các giáo viên trong hệ thống.</p>
      </div>

      <div className="flex items-center gap-4 bg-white p-4 rounded-xl border shadow-sm">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Tìm kiếm giáo viên theo tên hoặc email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full rounded-lg border border-gray-200 pl-10 pr-4 py-2 text-sm focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
          />
        </div>
      </div>

      <div className="rounded-xl border bg-white shadow-sm overflow-hidden">
        <table className="w-full text-left text-sm">
          <thead className="bg-gray-50 text-xs uppercase text-gray-700 font-semibold border-b">
            <tr>
              <th className="px-6 py-4">Giáo viên</th>
              <th className="px-6 py-4">Liên hệ</th>
              <th className="px-6 py-4">Ngày tham gia</th>
              <th className="px-6 py-4">Trạng thái</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {filteredTeachers.length === 0 ? (
              <tr>
                <td colSpan="4" className="px-6 py-12 text-center text-gray-500 italic">
                  Không tìm thấy giáo viên nào phù hợp.
                </td>
              </tr>
            ) : (
              filteredTeachers.map((teacher) => (
                <tr key={teacher.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className="size-9 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-sm">
                        {teacher.fullName?.charAt(0).toUpperCase() || 'U'}
                      </div>
                      <div className="flex flex-col">
                        <span className="font-semibold text-gray-900">{teacher.fullName}</span>
                        <span className="text-xs text-gray-500">ID: #{teacher.id}</span>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 space-y-1">
                    <div className="flex items-center gap-1.5 text-gray-600">
                      <Mail className="size-3.5 text-gray-400" />
                      {teacher.email}
                    </div>
                    <div className="flex items-center gap-1.5 text-gray-600">
                      <Phone className="size-3.5 text-gray-400" />
                      {teacher.phoneNumber || 'N/A'}
                    </div>
                  </td>
                  <td className="px-6 py-4 text-gray-500">
                    {teacher.createdAt ? new Date(teacher.createdAt).toLocaleDateString('vi-VN') : 'N/A'}
                  </td>
                  <td className="px-6 py-4">
                    {teacher.enabled !== false ? (
                      <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200 text-[11px] font-bold">
                        <UserCheck className="size-3" />
                        Đang hoạt động
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-red-50 text-red-700 border border-red-200 text-[11px] font-bold">
                        <UserX className="size-3" />
                        Tạm khóa
                      </span>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}