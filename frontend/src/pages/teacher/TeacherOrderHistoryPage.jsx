import React, { useState, useEffect, useMemo } from "react";
import {
  Loader2, Package, Clock, ShieldCheck, XCircle,
  ChevronLeft, ChevronRight, Search, Filter, CreditCard,
  Calendar, Tag, Receipt, ArrowUpRight
} from "lucide-react";
import { subscriptionsApi } from "../../features/subscriptions/subscriptionsApi";

const PAGE_SIZE = 6;

const STATUS_CONFIG = {
  ALL:       { label: "Tat ca",   bg: "bg-slate-100",  text: "text-slate-600",  dot: "bg-slate-400"  },
  ACTIVE:    { label: "Hoat dong", bg: "bg-emerald-100", text: "text-emerald-700", dot: "bg-emerald-500" },
  PENDING:   { label: "Cho xu ly", bg: "bg-yellow-100", text: "text-yellow-700",  dot: "bg-yellow-400"  },
  EXPIRED:   { label: "Het han",  bg: "bg-slate-100",  text: "text-slate-500",  dot: "bg-slate-400"  },
  CANCELLED: { label: "Da huy",   bg: "bg-red-100",    text: "text-red-700",    dot: "bg-red-400"    },
};

const StatusBadge = ({ status }) => {
  const cfg = STATUS_CONFIG[status] ?? STATUS_CONFIG.ALL;
  return (
    <span className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold ${cfg.bg} ${cfg.text}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${cfg.dot}`} />
      {cfg.label}
    </span>
  );
};

const TeacherOrderHistoryPage = () => {
  const [orders, setOrders]       = useState([]);
  const [loading, setLoading]     = useState(true);
  const [filterStatus, setFilterStatus] = useState("ALL");
  const [search, setSearch]       = useState("");
  const [page, setPage]           = useState(1);

  useEffect(() => {
    subscriptionsApi.getMyOrders()
      .then(res => setOrders(Array.isArray(res.data) ? res.data : []))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  const filtered = useMemo(() => {
    return orders.filter(o => {
      const matchStatus = filterStatus === "ALL" || o.status === filterStatus;
      const matchSearch = !search.trim() ||
        (o.packageName || "").toLowerCase().includes(search.toLowerCase()) ||
        String(o.id).includes(search);
      return matchStatus && matchSearch;
    });
  }, [orders, filterStatus, search]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const paginated  = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  const handleFilter = (status) => { setFilterStatus(status); setPage(1); };
  const handleSearch = (v)      => { setSearch(v);            setPage(1); };

  const formatDate   = (d) => d ? new Date(d).toLocaleDateString("vi-VN") : "--";
  const formatPrice  = (p) => p === 0 ? "Mien phi" : Number(p).toLocaleString("vi-VN") + "\u20ab";

  return (
    <div className="max-w-5xl mx-auto p-4 md:p-8 animate-in fade-in duration-500">

      {/* Header */}
      <div className="relative overflow-hidden rounded-[2.5rem] bg-gradient-to-br from-slate-900 via-slate-800 to-blue-900 p-8 mb-10 shadow-2xl">
        <div className="absolute top-0 right-0 -mt-8 -mr-8 w-56 h-56 bg-blue-500/10 rounded-full blur-3xl" />
        <div className="relative z-10">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-10 h-10 rounded-2xl bg-white/10 flex items-center justify-center">
              <Receipt className="w-5 h-5 text-white" />
            </div>
            <span className="text-white/60 text-sm font-semibold uppercase tracking-widest">Tai khoan</span>
          </div>
          <h1 className="text-3xl font-black text-white mb-1">Lich su giao dich</h1>
          <p className="text-slate-400">Toan bo cac don hang va trang thai thanh toan cua ban.</p>
        </div>
        <div className="absolute bottom-6 right-8 flex gap-3 opacity-30">
          <CreditCard className="w-10 h-10 text-white" />
        </div>
      </div>

      {/* Filter + Search bar */}
      <div className="flex flex-col md:flex-row gap-4 mb-6">
        {/* Search */}
        <div className="relative flex-1">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            type="text"
            value={search}
            onChange={e => handleSearch(e.target.value)}
            placeholder="Tim kiem theo ten goi hoac ma don..."
            className="w-full bg-white border border-slate-200 rounded-2xl pl-11 pr-4 py-3 text-sm text-slate-700 placeholder:text-slate-400 focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none shadow-sm transition"
          />
        </div>

        {/* Status filter pills */}
        <div className="flex items-center gap-2 flex-wrap">
          <Filter className="w-4 h-4 text-slate-400 shrink-0" />
          {Object.entries(STATUS_CONFIG).map(([key, cfg]) => (
            <button
              key={key}
              onClick={() => handleFilter(key)}
              className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
                filterStatus === key
                  ? "bg-slate-900 text-white shadow-md scale-105"
                  : "bg-white border border-slate-200 text-slate-600 hover:bg-slate-50"
              }`}
            >
              {cfg.label}
            </button>
          ))}
        </div>
      </div>

      {/* Summary stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        {[
          { label: "Tong don", value: orders.length, color: "text-slate-800" },
          { label: "Hoat dong", value: orders.filter(o => o.status === "ACTIVE").length, color: "text-emerald-600" },
          { label: "Cho xu ly", value: orders.filter(o => o.status === "PENDING").length, color: "text-yellow-600" },
          { label: "Da huy", value: orders.filter(o => o.status === "CANCELLED").length, color: "text-red-500" },
        ].map((s, i) => (
          <div key={i} className="bg-white rounded-2xl border border-slate-100 p-4 shadow-sm text-center">
            <p className={`text-2xl font-black ${s.color}`}>{s.value}</p>
            <p className="text-xs text-slate-500 font-semibold mt-1 uppercase tracking-wider">{s.label}</p>
          </div>
        ))}
      </div>

      {/* Content */}
      {loading ? (
        <div className="flex flex-col items-center justify-center py-24 gap-4">
          <Loader2 className="w-12 h-12 animate-spin text-blue-500" />
          <p className="text-slate-500 font-medium">Dang tai du lieu...</p>
        </div>
      ) : paginated.length === 0 ? (
        <div className="text-center bg-white p-16 rounded-[2rem] shadow-sm border border-slate-100">
          <div className="w-16 h-16 bg-slate-100 rounded-3xl flex items-center justify-center mx-auto mb-5">
            <Package className="w-8 h-8 text-slate-400" />
          </div>
          <h3 className="text-lg font-bold text-slate-700 mb-1">Khong co giao dich</h3>
          <p className="text-slate-400 text-sm">
            {filterStatus !== "ALL" || search ? "Khong tim thay ket qua phu hop." : "Ban chua mua goi dich vu nao."}
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {paginated.map((order, idx) => (
            <div
              key={order.id}
              className="group bg-white rounded-[1.5rem] border border-slate-100 p-6 flex flex-col md:flex-row items-start md:items-center justify-between gap-4 shadow-sm hover:shadow-lg hover:-translate-y-0.5 transition-all duration-300"
              style={{ animationDelay: `${idx * 50}ms` }}
            >
              {/* Left: icon + info */}
              <div className="flex items-center gap-4 flex-1 min-w-0">
                <div className={`shrink-0 w-12 h-12 rounded-2xl flex items-center justify-center ${
                  order.status === "ACTIVE"    ? "bg-emerald-50" :
                  order.status === "PENDING"   ? "bg-yellow-50"  :
                  order.status === "CANCELLED" ? "bg-red-50"     : "bg-slate-50"
                }`}>
                  {order.status === "ACTIVE"    ? <ShieldCheck className="w-6 h-6 text-emerald-500" /> :
                   order.status === "PENDING"   ? <Clock       className="w-6 h-6 text-yellow-500" /> :
                   order.status === "CANCELLED" ? <XCircle     className="w-6 h-6 text-red-400"    /> :
                                                  <Package     className="w-6 h-6 text-slate-400"  />}
                </div>
                <div className="min-w-0">
                  <div className="flex items-center gap-2 flex-wrap">
                    <h3 className="text-base font-bold text-slate-800">{order.packageName || "Goi khong ro"}</h3>
                    <StatusBadge status={order.status} />
                  </div>
                  <div className="flex items-center gap-4 mt-1 text-xs text-slate-500 flex-wrap">
                    <span className="flex items-center gap-1">
                      <Tag className="w-3 h-3" /> Don #{order.id}
                    </span>
                    <span className="flex items-center gap-1">
                      <Calendar className="w-3 h-3" /> {formatDate(order.createdAt)}
                    </span>
                    {order.status === "ACTIVE" && order.expiresAt && (
                      <span className="flex items-center gap-1 text-emerald-600 font-semibold">
                        <ShieldCheck className="w-3 h-3" /> Het han: {formatDate(order.expiresAt)}
                      </span>
                    )}
                  </div>
                </div>
              </div>

              {/* Right: price + method */}
              <div className="flex items-center gap-6 shrink-0">
                <div className="text-right">
                  <p className="text-lg font-black text-blue-600">{formatPrice(order.amountPaid)}</p>
                  <p className="text-xs text-slate-400 font-medium mt-0.5 uppercase">{order.paymentMethod || "—"}</p>
                </div>
                {order.status === "ACTIVE" && (
                  <div className="w-8 h-8 rounded-xl bg-emerald-50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                    <ArrowUpRight className="w-4 h-4 text-emerald-500" />
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Pagination */}
      {!loading && filtered.length > PAGE_SIZE && (
        <div className="flex items-center justify-between mt-8 pt-6 border-t border-slate-100">
          <p className="text-sm text-slate-500">
            Hien thi <span className="font-bold text-slate-700">{(page - 1) * PAGE_SIZE + 1}–{Math.min(page * PAGE_SIZE, filtered.length)}</span> / {filtered.length} giao dich
          </p>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setPage(p => Math.max(1, p - 1))}
              disabled={page === 1}
              className="w-9 h-9 rounded-xl border border-slate-200 bg-white flex items-center justify-center text-slate-500 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            {Array.from({ length: totalPages }, (_, i) => i + 1).map(p => (
              <button
                key={p}
                onClick={() => setPage(p)}
                className={`w-9 h-9 rounded-xl text-sm font-bold transition-all ${
                  page === p
                    ? "bg-blue-600 text-white shadow-md shadow-blue-200"
                    : "bg-white border border-slate-200 text-slate-600 hover:bg-slate-50"
                }`}
              >
                {p}
              </button>
            ))}
            <button
              onClick={() => setPage(p => Math.min(totalPages, p + 1))}
              disabled={page === totalPages}
              className="w-9 h-9 rounded-xl border border-slate-200 bg-white flex items-center justify-center text-slate-500 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default TeacherOrderHistoryPage;