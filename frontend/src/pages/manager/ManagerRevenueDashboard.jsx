import React, { useEffect, useState } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  LineChart, Line,
  PieChart, Pie, Cell, Legend,
} from 'recharts';
import {
  TrendingUp, DollarSign, Package, Clock, BarChart2,
  ArrowUpRight, ArrowDownRight, Loader2, RefreshCw
} from 'lucide-react';
import analyticsApi from '../../services/analyticsApi';
import { toast } from 'sonner';

// ── Helpers ───────────────────────────────────────────────────────────────────
const COLORS = ['#6366f1', '#22d3ee', '#f59e0b', '#10b981', '#f43f5e'];

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

const formatCurrency = (v) => {
  if (!v) return '0 ₫';
  const n = typeof v === 'string' ? parseFloat(v) : v;
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1)}M ₫`;
  if (n >= 1_000) return `${(n / 1_000).toFixed(0)}K ₫`;
  return `${n.toLocaleString('vi-VN')} ₫`;
};

const MONTH_LABELS = {
  '01': 'T1', '02': 'T2', '03': 'T3', '04': 'T4',
  '05': 'T5', '06': 'T6', '07': 'T7', '08': 'T8',
  '09': 'T9', '10': 'T10', '11': 'T11', '12': 'T12',
};

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-white border border-slate-200 rounded-xl shadow-xl p-3 text-sm min-w-[140px]">
      <p className="font-bold text-slate-700 mb-1">{label}</p>
      {payload.map((p, i) => (
        <p key={i} style={{ color: p.color }}>
          {p.name}: <b>{typeof p.value === 'number' && p.value > 1000 ? formatCurrency(p.value) : p.value}</b>
        </p>
      ))}
    </div>
  );
};

// ─────────────────────────────────────────────────────────────────────────────
// Main page
// ─────────────────────────────────────────────────────────────────────────────
const ManagerRevenueDashboard = () => {
  const [data, setData]       = useState(null);
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    analyticsApi.getRevenueAnalytics()
      .then(setData)
      .catch(() => toast.error('Không thể tải dữ liệu doanh thu'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  if (loading) return <Spinner />;
  if (!data)   return null;

  const { summary, revenueByMonth, revenueByPackage, ordersByStatus, topPackages, paymentMethodStats, recentOrders } = data;

  // ── Monthly chart data ────────────────────────────────────────────────────
  const monthlyData = Object.entries(revenueByMonth || {}).map(([key, val]) => {
    const parts = key.split('-');
    return {
      month: MONTH_LABELS[parts[1]] || key,
      revenue: typeof val === 'string' ? parseFloat(val) : val,
    };
  });

  // ── Package pie data ──────────────────────────────────────────────────────
  const pkgPie = (topPackages || []).map((p) => ({
    name: p.packageName,
    value: typeof p.totalRevenue === 'string' ? parseFloat(p.totalRevenue) : p.totalRevenue,
    share: p.revenueShare,
  }));

  // ── Status pie ────────────────────────────────────────────────────────────
  const STATUS_COLORS = { ACTIVE: '#10b981', PENDING: '#f59e0b', EXPIRED: '#94a3b8', CANCELLED: '#f43f5e' };
  const statusPie = Object.entries(ordersByStatus || {}).map(([status, count]) => ({
    name: status, value: count, fill: STATUS_COLORS[status] ?? '#6366f1',
  }));

  return (
    <div className="max-w-7xl mx-auto p-4 md:p-8 animate-in fade-in duration-500">

      {/* ── Header ── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">Báo cáo doanh thu</h1>
          <p className="text-slate-500 mt-1 text-sm">
            Cập nhật lúc {new Date(data.generatedAt).toLocaleTimeString('vi-VN')} – chỉ tính đơn ACTIVE
          </p>
        </div>
        <button onClick={load} className="flex items-center gap-2 px-4 py-2 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-semibold rounded-xl text-sm transition-colors">
          <RefreshCw className="w-4 h-4" /> Làm mới
        </button>
      </div>

      {/* ── KPI Row ── */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <KpiCard
          label="Tổng doanh thu"
          value={formatCurrency(summary.totalRevenue)}
          icon={DollarSign} iconBg="bg-indigo-50" iconColor="text-indigo-600"
        />
        <KpiCard
          label="Tháng này"
          value={formatCurrency(summary.currentMonthRevenue)}
          sub={`Tháng trước: ${formatCurrency(summary.lastMonthRevenue)}`}
          icon={TrendingUp} iconBg="bg-emerald-50" iconColor="text-emerald-600"
          trend={summary.monthOverMonthGrowth}
          trendUp={(summary.monthOverMonthGrowth ?? 0) >= 0}
        />
        <KpiCard
          label="Đơn hàng ACTIVE"
          value={summary.activeOrders}
          sub={`${summary.pendingOrders} đang chờ`}
          icon={Package} iconBg="bg-amber-50" iconColor="text-amber-600"
        />
        <KpiCard
          label="Doanh thu / đơn"
          value={formatCurrency(summary.avgOrderValue)}
          sub={`${summary.uniqueBuyers} người mua`}
          icon={BarChart2} iconBg="bg-purple-50" iconColor="text-purple-600"
        />
      </div>

      {/* ── Monthly Revenue Bar Chart ── */}
      <SectionCard title="Doanh thu theo tháng" icon={BarChart2} iconClass="text-indigo-500" className="mb-6">
        <ResponsiveContainer width="100%" height={280}>
          <BarChart data={monthlyData} barSize={28} margin={{ top: 4, right: 16, left: 8, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
            <XAxis dataKey="month" tick={{ fontSize: 13, fontWeight: 600 }} />
            <YAxis tickFormatter={formatCurrency} tick={{ fontSize: 11 }} width={80} />
            <Tooltip content={<CustomTooltip />} />
            <Bar dataKey="revenue" name="Doanh thu" fill="#6366f1" radius={[8, 8, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </SectionCard>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
        {/* ── Package Pie ── */}
        <SectionCard title="Doanh thu theo gói" icon={Package} iconClass="text-emerald-500" className="lg:col-span-2">
          <div className="flex flex-col md:flex-row items-center gap-4">
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie data={pkgPie} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label={({ name, share }) => `${name}: ${share}%`}>
                  {pkgPie.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Pie>
                <Tooltip formatter={(v) => formatCurrency(v)} />
              </PieChart>
            </ResponsiveContainer>
            <ul className="space-y-2 w-full md:w-auto min-w-[180px]">
              {topPackages?.map((p, i) => (
                <li key={i} className="flex items-center gap-2 text-sm">
                  <span className="w-3 h-3 rounded-full flex-shrink-0" style={{ background: COLORS[i % COLORS.length] }} />
                  <span className="text-slate-600 flex-1 truncate">{p.packageName}</span>
                  <span className="font-bold text-slate-800">{p.orderCount} đơn</span>
                </li>
              ))}
            </ul>
          </div>
        </SectionCard>

        {/* ── Order status pie ── */}
        <SectionCard title="Trạng thái đơn hàng" icon={Clock} iconClass="text-amber-500">
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={statusPie} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={75} label={({ name, value }) => `${name}: ${value}`}>
                {statusPie.map((entry, i) => <Cell key={i} fill={entry.fill} />)}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </SectionCard>
      </div>

      {/* ── Recent Orders ── */}
      <SectionCard title="Đơn hàng gần đây" icon={DollarSign} iconClass="text-slate-500">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-xs font-bold text-slate-400 uppercase border-b border-slate-100">
                <th className="text-left pb-3 pr-3">#</th>
                <th className="text-left pb-3 pr-3">Người mua</th>
                <th className="text-left pb-3 pr-3">Gói</th>
                <th className="text-right pb-3 pr-3">Số tiền</th>
                <th className="text-left pb-3 pr-3">Phương thức</th>
                <th className="text-left pb-3 pr-3">Trạng thái</th>
                <th className="text-right pb-3">Ngày</th>
              </tr>
            </thead>
            <tbody>
              {(recentOrders || []).map((o, i) => {
                const STATUS_LABEL = { ACTIVE: { text: 'Hoạt động', bg: 'bg-emerald-50', color: 'text-emerald-600' }, PENDING: { text: 'Chờ', bg: 'bg-amber-50', color: 'text-amber-600' }, EXPIRED: { text: 'Hết hạn', bg: 'bg-slate-50', color: 'text-slate-500' }, CANCELLED: { text: 'Hủy', bg: 'bg-rose-50', color: 'text-rose-600' } };
                const sl = STATUS_LABEL[o.status] ?? { text: o.status, bg: 'bg-slate-50', color: 'text-slate-500' };
                return (
                  <tr key={i} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                    <td className="py-3 pr-3 font-mono text-slate-400 text-xs">{o.orderId}</td>
                    <td className="py-3 pr-3">
                      <p className="font-semibold text-slate-700">{o.buyerName}</p>
                      <p className="text-xs text-slate-400">{o.buyerEmail}</p>
                    </td>
                    <td className="py-3 pr-3 text-slate-600">{o.packageName}</td>
                    <td className="py-3 pr-3 text-right font-black text-indigo-600">{formatCurrency(o.amountPaid)}</td>
                    <td className="py-3 pr-3 text-slate-500">{o.paymentMethod ?? '–'}</td>
                    <td className="py-3 pr-3">
                      <span className={`px-2 py-1 rounded-lg text-xs font-bold ${sl.bg} ${sl.color}`}>{sl.text}</span>
                    </td>
                    <td className="py-3 text-right text-slate-400 text-xs">
                      {new Date(o.createdAt).toLocaleDateString('vi-VN')}
                    </td>
                  </tr>
                );
              })}
              {!recentOrders?.length && (
                <tr><td colSpan={7} className="py-8 text-center text-slate-400">Chưa có đơn hàng</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </SectionCard>
    </div>
  );
};

export default ManagerRevenueDashboard;
