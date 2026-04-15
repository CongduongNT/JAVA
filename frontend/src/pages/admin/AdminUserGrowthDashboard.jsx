import React, { useEffect, useState } from 'react';
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  BarChart, Bar,
  PieChart, Pie, Cell, Legend,
} from 'recharts';
import {
  Users, TrendingUp, UserCheck, Package, BarChart2, Loader2, RefreshCw,
  ArrowUpRight, ArrowDownRight, ShieldCheck
} from 'lucide-react';
import analyticsApi from '../../services/analyticsApi';
import { toast } from 'sonner';

// ── Helpers ───────────────────────────────────────────────────────────────────
const ROLE_COLORS  = { ADMIN: '#f43f5e', MANAGER: '#f59e0b', STAFF: '#22d3ee', TEACHER: '#6366f1' };
const ROLE_LABELS  = { ADMIN: 'Quản trị', MANAGER: 'Quản lý', STAFF: 'Nhân viên', TEACHER: 'Giáo viên' };

const Spinner = () => (
  <div className="flex items-center justify-center h-64">
    <Loader2 className="w-8 h-8 text-indigo-500 animate-spin" />
  </div>
);

const SectionCard = ({ title, icon: Icon, iconClass, children, className = '' }) => (
  <div className={`bg-white rounded-3xl border border-slate-100 shadow-xl shadow-slate-200/30 p-6 ${className}`}>
    {title && (
      <h3 className="flex items-center gap-2 text-lg font-bold text-slate-800 mb-5">
        {Icon && <Icon className={`w-5 h-5 ${iconClass}`} />}
        {title}
      </h3>
    )}
    {children}
  </div>
);

const KpiCard = ({ label, value, sub, icon: Icon, iconBg, iconColor, trend, trendUp }) => (
  <div className="bg-white rounded-2xl border border-slate-100 shadow-lg p-5 hover:shadow-xl hover:-translate-y-0.5 transition-all duration-300">
    <div className="flex justify-between items-start mb-3">
      <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${iconBg}`}>
        {Icon && <Icon className={`w-5 h-5 ${iconColor}`} />}
      </div>
      {trend != null && (
        <span className={`flex items-center gap-1 text-xs font-bold px-2 py-1 rounded-lg ${trendUp ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'}`}>
          {trendUp ? <ArrowUpRight className="w-3 h-3" /> : <ArrowDownRight className="w-3 h-3" />}
          {Math.abs(trend).toFixed(1)}%
        </span>
      )}
    </div>
    <p className="text-xs font-bold text-slate-400 uppercase tracking-wider">{label}</p>
    <p className="text-2xl font-black text-slate-900 mt-1">{value}</p>
    {sub && <p className="text-xs text-slate-400 mt-1">{sub}</p>}
  </div>
);

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-white border border-slate-200 rounded-xl shadow-xl p-3 text-sm">
      <p className="font-bold text-slate-700 mb-1">{label}</p>
      {payload.map((p, i) => (
        <p key={i} style={{ color: p.stroke || p.fill || p.color }}>
          {p.name}: <b>{p.value}</b>
        </p>
      ))}
    </div>
  );
};

const MONTH_LABELS = {
  '01':'T1','02':'T2','03':'T3','04':'T4','05':'T5','06':'T6',
  '07':'T7','08':'T8','09':'T9','10':'T10','11':'T11','12':'T12',
};

// ─────────────────────────────────────────────────────────────────────────────
// Main page
// ─────────────────────────────────────────────────────────────────────────────
const AdminUserGrowthDashboard = () => {
  const [data, setData]       = useState(null);
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    analyticsApi.getUserAnalytics()
      .then(setData)
      .catch(() => toast.error('Không thể tải dữ liệu người dùng'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  if (loading) return <Spinner />;
  if (!data)   return null;

  const { summary, usersByRole, usersByMonth, activeVsInactive, topTeachers, subscriptionStats, recentUsers } = data;

  // ── Monthly growth area chart ─────────────────────────────────────────────
  const monthlyGrowth = Object.entries(usersByMonth || {}).map(([key, count]) => {
    const parts = key.split('-');
    return { month: MONTH_LABELS[parts[1]] || key, users: count };
  });

  // ── Role pie ──────────────────────────────────────────────────────────────
  const rolePie = Object.entries(usersByRole || {}).map(([role, count]) => ({
    name: ROLE_LABELS[role] || role, value: count, fill: ROLE_COLORS[role] ?? '#94a3b8',
  })).filter((r) => r.value > 0);

  // ── Subscription pie ──────────────────────────────────────────────────────
  const subPie = [
    { name: 'Có gói',   value: subscriptionStats?.subscribedUsers   ?? 0, fill: '#10b981' },
    { name: 'Chưa gói', value: subscriptionStats?.unsubscribedUsers ?? 0, fill: '#e2e8f0' },
  ];

  // ── Active vs inactive bar ────────────────────────────────────────────────
  const activeBar = [
    { label: 'Active',   count: activeVsInactive?.activeCount   ?? 0, fill: '#10b981' },
    { label: 'Inactive', count: activeVsInactive?.inactiveCount ?? 0, fill: '#f43f5e' },
  ];

  return (
    <div className="max-w-7xl mx-auto p-4 md:p-8 animate-in fade-in duration-500">

      {/* ── Header ── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">Báo cáo người dùng</h1>
          <p className="text-slate-500 mt-1 text-sm">
            Tăng trưởng, phân bổ role và subscription – Admin only
          </p>
        </div>
        <button onClick={load} className="flex items-center gap-2 px-4 py-2 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-semibold rounded-xl text-sm transition-colors">
          <RefreshCw className="w-4 h-4" /> Làm mới
        </button>
      </div>

      {/* ── KPI Row ── */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <KpiCard
          label="Tổng người dùng"
          value={summary.totalUsers}
          icon={Users} iconBg="bg-indigo-50" iconColor="text-indigo-600"
        />
        <KpiCard
          label="Mới tháng này"
          value={summary.newUsersThisMonth}
          sub={`Tháng trước: ${summary.newUsersLastMonth}`}
          icon={TrendingUp} iconBg="bg-emerald-50" iconColor="text-emerald-600"
          trend={summary.userGrowthRate}
          trendUp={(summary.userGrowthRate ?? 0) >= 0}
        />
        <KpiCard
          label="Giáo viên"
          value={summary.totalTeachers}
          sub={`${summary.totalManagers} Manager · ${summary.totalStaff} Staff`}
          icon={UserCheck} iconBg="bg-purple-50" iconColor="text-purple-600"
        />
        <KpiCard
          label="Đang dùng gói"
          value={summary.subscribedUsers}
          sub={`${subscriptionStats?.subscriptionRate?.toFixed(1)}% đã đăng ký`}
          icon={Package} iconBg="bg-amber-50" iconColor="text-amber-600"
        />
      </div>

      {/* ── User Growth Area Chart ── */}
      <SectionCard title="Tăng trưởng người dùng mới theo tháng" icon={TrendingUp} iconClass="text-indigo-500" className="mb-6">
        <ResponsiveContainer width="100%" height={280}>
          <AreaChart data={monthlyGrowth} margin={{ top: 4, right: 16, left: 0, bottom: 0 }}>
            <defs>
              <linearGradient id="userGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%"  stopColor="#6366f1" stopOpacity={0.3} />
                <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
            <XAxis dataKey="month" tick={{ fontSize: 13, fontWeight: 600 }} />
            <YAxis tick={{ fontSize: 12 }} allowDecimals={false} />
            <Tooltip content={<CustomTooltip />} />
            <Area type="monotone" dataKey="users" name="Người dùng mới" stroke="#6366f1" strokeWidth={2.5} fill="url(#userGradient)" dot={{ r: 4, fill: '#6366f1', strokeWidth: 0 }} activeDot={{ r: 6 }} />
          </AreaChart>
        </ResponsiveContainer>
      </SectionCard>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
        {/* ── Role pie ── */}
        <SectionCard title="Phân bổ theo vai trò" icon={ShieldCheck} iconClass="text-rose-500">
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie data={rolePie} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={75}
                label={({ name, value }) => `${name}: ${value}`}>
                {rolePie.map((entry, i) => <Cell key={i} fill={entry.fill} />)}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </SectionCard>

        {/* ── Subscription pie ── */}
        <SectionCard title="Tỉ lệ đăng ký gói" icon={Package} iconClass="text-emerald-500">
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie data={subPie} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={75}
                label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}>
                {subPie.map((entry, i) => <Cell key={i} fill={entry.fill} />)}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </SectionCard>

        {/* ── Active vs Inactive ── */}
        <SectionCard title="Active / Inactive" icon={UserCheck} iconClass="text-sky-500">
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={activeBar} barSize={48}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
              <XAxis dataKey="label" tick={{ fontSize: 13, fontWeight: 600 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip content={<CustomTooltip />} />
              <Bar dataKey="count" name="Người dùng" radius={[8, 8, 0, 0]}>
                {activeBar.map((entry, i) => <Cell key={i} fill={entry.fill} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </SectionCard>
      </div>

      {/* ── Top Teachers ── */}
      <SectionCard title="Top 5 giáo viên tích cực nhất" icon={BarChart2} iconClass="text-purple-500" className="mb-6">
        {!topTeachers?.length ? (
          <p className="text-slate-400 text-sm text-center py-8">Chưa có dữ liệu</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-xs font-bold text-slate-400 uppercase border-b border-slate-100">
                  <th className="text-left pb-3 pr-3">Hạng</th>
                  <th className="text-left pb-3 pr-3">Giáo viên</th>
                  <th className="text-right pb-3 pr-3">Tổng đề</th>
                  <th className="text-right pb-3 pr-3">Đã xuất bản</th>
                  <th className="text-right pb-3">Ngân hàng câu</th>
                </tr>
              </thead>
              <tbody>
                {topTeachers.map((t, i) => (
                  <tr key={i} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                    <td className="py-3 pr-3">
                      <span className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-black ${i === 0 ? 'bg-amber-100 text-amber-700' : i === 1 ? 'bg-slate-100 text-slate-600' : 'bg-orange-50 text-orange-600'}`}>
                        #{i + 1}
                      </span>
                    </td>
                    <td className="py-3 pr-3">
                      <p className="font-semibold text-slate-800">{t.teacherName}</p>
                      <p className="text-xs text-slate-400">{t.teacherEmail}</p>
                    </td>
                    <td className="py-3 pr-3 text-right font-black text-indigo-600">{t.examCount}</td>
                    <td className="py-3 pr-3 text-right text-emerald-600 font-semibold">{t.publishedExamCount}</td>
                    <td className="py-3 text-right text-slate-500">{t.questionBankCount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </SectionCard>

      {/* ── Recent Users ── */}
      <SectionCard title="Người dùng mới nhất" icon={Users} iconClass="text-slate-500">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-xs font-bold text-slate-400 uppercase border-b border-slate-100">
                <th className="text-left pb-3 pr-3">Tên</th>
                <th className="text-left pb-3 pr-3">Email</th>
                <th className="text-left pb-3 pr-3">Vai trò</th>
                <th className="text-left pb-3 pr-3">Gói</th>
                <th className="text-right pb-3">Ngày đăng ký</th>
              </tr>
            </thead>
            <tbody>
              {(recentUsers || []).map((u, i) => (
                <tr key={i} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                  <td className="py-3 pr-3">
                    <div className="flex items-center gap-2">
                      <div className="w-8 h-8 rounded-full bg-indigo-100 text-indigo-700 flex items-center justify-center text-xs font-black flex-shrink-0">
                        {u.fullName?.[0]?.toUpperCase() ?? '?'}
                      </div>
                      <span className="font-semibold text-slate-800">{u.fullName}</span>
                    </div>
                  </td>
                  <td className="py-3 pr-3 text-slate-500">{u.email}</td>
                  <td className="py-3 pr-3">
                    <span className="px-2 py-1 rounded-lg text-xs font-bold"
                      style={{ background: (ROLE_COLORS[u.role] ?? '#94a3b8') + '22', color: ROLE_COLORS[u.role] ?? '#64748b' }}>
                      {ROLE_LABELS[u.role] || u.role}
                    </span>
                  </td>
                  <td className="py-3 pr-3 text-slate-500 text-xs">{u.activePackage ?? '–'}</td>
                  <td className="py-3 text-right text-slate-400 text-xs">
                    {u.createdAt ? new Date(u.createdAt).toLocaleDateString('vi-VN') : '–'}
                  </td>
                </tr>
              ))}
              {!recentUsers?.length && (
                <tr><td colSpan={5} className="py-8 text-center text-slate-400">Chưa có dữ liệu</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </SectionCard>
    </div>
  );
};

export default AdminUserGrowthDashboard;
