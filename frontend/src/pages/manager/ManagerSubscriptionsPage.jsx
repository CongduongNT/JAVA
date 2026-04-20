import React, { useState, useEffect, useRef } from "react";
import { Plus, Edit3, Power, Loader2, Sparkles, Calendar, Check, X, Shield, Zap, Rocket, Tag } from "lucide-react";
import { subscriptionsApi } from "../../features/subscriptions/subscriptionsApi";

const ManagerSubscriptionsPage = () => {
  const [packages, setPackages]       = useState([]);
  const [loading, setLoading]         = useState(true);
  const [showModal, setShowModal]     = useState(false);
  const [editingId, setEditingId]     = useState(null);
  const [formData, setFormData] = useState({
    name: "", description: "", price: 0, durationDays: 30, isActive: true
  });
  const [featuresList, setFeaturesList] = useState([]);
  const [featureInput, setFeatureInput] = useState("");

  const fetchPackages = async () => {
    try {
      const res = await subscriptionsApi.getPackages();
      setPackages(res.data);
    } catch (error) {
      console.error("Loi tai danh sach goi:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchPackages(); }, []);

  const handleOpenModal = (pkg = null) => {
    if (pkg) {
      setEditingId(pkg.id);
      let parsedFeatures = [];
      try { parsedFeatures = typeof pkg.features === "string" ? JSON.parse(pkg.features) : (pkg.features || []); } catch {}
      setFeaturesList(Array.isArray(parsedFeatures) ? parsedFeatures : []);
      setFormData({ name: pkg.name, description: pkg.description, price: pkg.price, durationDays: pkg.durationDays, isActive: pkg.isActive });
    } else {
      setEditingId(null);
      setFeaturesList([]);
      setFormData({ name: "", description: "", price: 0, durationDays: 30, isActive: true });
    }
    setFeatureInput("");
    setShowModal(true);
  };

  const handleAddFeature = () => {
    const trimmed = featureInput.trim();
    if (trimmed && !featuresList.includes(trimmed)) setFeaturesList(prev => [...prev, trimmed]);
    setFeatureInput("");
  };
  const handleRemoveFeature = (idx) => setFeaturesList(prev => prev.filter((_, i) => i !== idx));
  const handleFeatureKeyDown = (e) => {
    if (e.key === "Enter") { e.preventDefault(); handleAddFeature(); }
    if (e.key === "Backspace" && featureInput === "" && featuresList.length > 0) setFeaturesList(prev => prev.slice(0, -1));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    try {
      const dataToSave = { ...formData, features: JSON.stringify(featuresList) };
      if (editingId) await subscriptionsApi.updatePackage(editingId, dataToSave);
      else await subscriptionsApi.createPackage(dataToSave);
      setShowModal(false);
      fetchPackages();
    } catch (error) {
      console.error("Loi luu goi:", error);
      alert("Da co loi xay ra. Kiem tra console.");
    }
  };

  const handleDeactivate = async (id) => {
    if (window.confirm("Ban co chac chan muon vo hieu hoa goi nay?")) {
      try { await subscriptionsApi.deactivatePackage(id); fetchPackages(); }
      catch (error) { console.error("Loi cap nhat goi:", error); }
    }
  };

  const getPlanIcon = (name) => {
    const n = name.toUpperCase();
    if (n.includes("FREE")) return <Zap className="w-6 h-6 text-emerald-500" />;
    if (n.includes("PRO")) return <Rocket className="w-6 h-6 text-blue-500" />;
    return <Sparkles className="w-6 h-6 text-purple-500" />;
  };

  const getPlanGradient = (name) => {
    const n = name.toUpperCase();
    if (n.includes("FREE")) return "from-emerald-50 to-teal-50 border-emerald-100";
    if (n.includes("PRO")) return "from-blue-50 to-indigo-50 border-blue-100";
    return "from-purple-50 to-fuchsia-50 border-purple-100";
  };

  const formatPrice = (price) =>
    price === 0 ? "Mien phi" : Number(price).toLocaleString("vi-VN") + "\u20ab";

  return (
    <div className="max-w-7xl mx-auto p-4 md:p-8 animate-in fade-in duration-500">
      <div className="relative overflow-hidden rounded-3xl bg-slate-900 p-8 md:p-12 mb-10 shadow-2xl">
        <div className="absolute top-0 right-0 -mt-10 -mr-10 w-64 h-64 bg-blue-500/10 rounded-full blur-3xl" />
        <div className="absolute bottom-0 left-0 -mb-10 -ml-10 w-64 h-64 bg-purple-500/10 rounded-full blur-3xl" />
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div>
            <h1 className="text-3xl md:text-4xl font-bold text-white mb-2">Quan ly Goi dich vu</h1>
            <p className="text-slate-400 text-lg">Thiet ke va cau hinh cac goi VIP cho he thong cua ban.</p>
          </div>
          <button onClick={() => handleOpenModal()} className="group flex items-center justify-center gap-2 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white font-semibold py-3 px-6 rounded-2xl shadow-lg shadow-blue-500/20 transition-all duration-300 hover:-translate-y-1 active:scale-95">
            <Plus className="w-5 h-5 group-hover:rotate-90 transition-transform duration-300" /> Tao Goi Moi
          </button>
        </div>
      </div>

      {loading ? (
        <div className="flex flex-col items-center justify-center py-20 gap-4">
          <Loader2 className="w-12 h-12 animate-spin text-blue-600" />
          <p className="font-medium text-slate-500">Dang chuan bi du lieu...</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {Array.isArray(packages) && packages.map((pkg, idx) => (
            <div key={pkg.id}
              className={`group relative bg-white rounded-[2rem] border-2 p-8 transition-all duration-500 hover:shadow-2xl hover:shadow-slate-200/50 hover:-translate-y-2 ${!pkg.isActive ? "opacity-70 grayscale bg-slate-50 border-slate-200" : getPlanGradient(pkg.name)}`}
              style={{ animationDelay: `${idx * 100}ms` }}>
              <div className="absolute top-6 right-6">
                <span className={`flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider ${pkg.isActive ? "bg-white text-emerald-600 shadow-sm" : "bg-slate-200 text-slate-500"}`}>
                  {pkg.isActive ? <><div className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" /> Active</> : <><X className="w-3 h-3" /> Disabled</>}
                </span>
              </div>
              <div className="mb-6">
                <div className={`w-14 h-14 rounded-2xl flex items-center justify-center mb-6 shadow-sm ${pkg.isActive ? "bg-white" : "bg-slate-200"}`}>{getPlanIcon(pkg.name)}</div>
                <h3 className="text-2xl font-bold text-slate-800 mb-2">{pkg.name}</h3>
                <p className="text-slate-500 text-sm leading-relaxed line-clamp-2 h-10">{pkg.description || "Goi dich vu cao cap danh cho giao vien hien dai."}</p>
              </div>
              <div className="mb-8 pt-6 border-t border-slate-200/50">
                <div className="flex items-baseline gap-1">
                  <span className="text-4xl font-extrabold text-slate-900 tracking-tight">{formatPrice(pkg.price)}</span>
                  <span className="text-slate-400 font-medium">/ goi</span>
                </div>
                <div className="flex items-center gap-2 mt-2 text-sm text-slate-500 bg-white/50 w-fit px-3 py-1 rounded-lg">
                  <Calendar className="w-4 h-4" /> Su dung trong {pkg.durationDays} ngay
                </div>
              </div>
              <div className="flex items-center gap-3">
                <button onClick={() => handleOpenModal(pkg)} className="flex-1 flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-white border border-slate-200 text-slate-700 font-bold text-sm hover:bg-slate-50 transition-colors shadow-sm">
                  <Edit3 className="w-4 h-4" /> Chi tiet
                </button>
                {pkg.isActive && (
                  <button onClick={() => handleDeactivate(pkg.id)} className="p-3 rounded-xl bg-white border border-red-100 text-red-500 hover:bg-red-50 transition-colors shadow-sm" title="Ngung kich hoat">
                    <Power className="w-5 h-5" />
                  </button>
                )}
              </div>
            </div>
          ))}

          {(!Array.isArray(packages) || packages.length === 0) && (
            <div className="col-span-full bg-white/50 backdrop-blur-sm border-2 border-dashed border-slate-200 rounded-[2.5rem] p-16 text-center">
              <div className="w-20 h-20 bg-slate-100 rounded-3xl flex items-center justify-center mx-auto mb-6"><Shield className="w-10 h-10 text-slate-400" /></div>
              <h3 className="text-xl font-bold text-slate-800 mb-2">Chua co goi dich vu</h3>
              <p className="text-slate-500 max-w-sm mx-auto">Hay bat dau tao nhung goi dich vu dau tien de giao vien co the nang cap tai khoan.</p>
            </div>
          )}
        </div>
      )}

      {showModal && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 md:p-6">
          <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-md animate-in fade-in duration-300" onClick={() => setShowModal(false)} />
          <div className="relative w-full max-w-xl bg-white rounded-[2.5rem] shadow-2xl overflow-hidden animate-in zoom-in-95 slide-in-from-bottom-10 duration-500">
            <div className="p-8 pb-0 flex justify-between items-start">
              <div>
                <h2 className="text-2xl font-bold text-slate-900">{editingId ? "Cap nhat Goi" : "Thiet lap Goi moi"}</h2>
                <p className="text-slate-500 text-sm mt-1">Vui long dien day du cac thong tin ben duoi.</p>
              </div>
              <button onClick={() => setShowModal(false)} className="p-2 hover:bg-slate-100 rounded-full transition-colors text-slate-400"><X className="w-6 h-6" /></button>
            </div>
            <form onSubmit={handleSave} className="p-8 space-y-5">
              <div className="space-y-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-500 uppercase tracking-wider ml-1">Ten dinh danh goi</label>
                  <input required type="text" className="w-full bg-slate-50 border-none rounded-2xl px-5 py-3.5 text-slate-800 font-medium focus:ring-2 focus:ring-blue-500 transition-all placeholder:text-slate-400" placeholder="VD: ENTERPRISE" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-500 uppercase tracking-wider ml-1">Mo ta ngan gon</label>
                  <textarea className="w-full bg-slate-50 border-none rounded-2xl px-5 py-3.5 text-slate-800 font-medium focus:ring-2 focus:ring-blue-500 transition-all resize-none placeholder:text-slate-400" placeholder="Mo ta nhung loi ich tuyet voi nhat..." value={formData.description} onChange={e => setFormData({...formData, description: e.target.value})} rows="3" />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-1.5">
                    <label className="text-xs font-bold text-slate-500 uppercase tracking-wider ml-1">Don gia (VND)</label>
                    <div className="relative">
                      <span className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 font-bold text-sm">VND</span>
                      <input required type="number" step="1000" min="0" className="w-full bg-slate-50 border-none rounded-2xl pl-16 pr-5 py-3.5 text-slate-800 font-bold focus:ring-2 focus:ring-blue-500 transition-all" value={formData.price} onChange={e => setFormData({...formData, price: e.target.value})} />
                    </div>
                  </div>
                  <div className="space-y-1.5">
                    <label className="text-xs font-bold text-slate-500 uppercase tracking-wider ml-1">Han dung (ngay)</label>
                    <input required type="number" min="1" className="w-full bg-slate-50 border-none rounded-2xl px-5 py-3.5 text-slate-800 font-bold focus:ring-2 focus:ring-blue-500 transition-all" value={formData.durationDays} onChange={e => setFormData({...formData, durationDays: e.target.value})} />
                  </div>
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-500 uppercase tracking-wider ml-1 flex items-center gap-1.5">
                    <Tag className="w-3.5 h-3.5" /> Tinh nang di kem
                  </label>
                  <div className="min-h-[52px] bg-slate-50 rounded-2xl px-4 py-3 flex flex-wrap gap-2 items-center cursor-text focus-within:ring-2 focus-within:ring-blue-500 transition-all"
                    onClick={() => document.getElementById("featureTagInput").focus()}>
                    {featuresList.map((f, idx) => (
                      <span key={idx} className="flex items-center gap-1.5 bg-blue-100 text-blue-700 text-xs font-bold px-3 py-1.5 rounded-full">
                        <Check className="w-3 h-3" />{f}
                        <button type="button" onClick={() => handleRemoveFeature(idx)} className="ml-0.5 text-blue-400 hover:text-red-500 transition-colors"><X className="w-3 h-3" /></button>
                      </span>
                    ))}
                    <input id="featureTagInput" type="text" value={featureInput} onChange={e => setFeatureInput(e.target.value)} onKeyDown={handleFeatureKeyDown}
                      className="flex-1 min-w-[140px] bg-transparent border-none outline-none text-sm text-slate-700 placeholder:text-slate-400 font-medium"
                      placeholder={featuresList.length === 0 ? "Nhap tinh nang roi nhan Enter..." : "Them tinh nang..."} />
                    {featureInput.trim() && (
                      <button type="button" onClick={handleAddFeature} className="shrink-0 w-7 h-7 bg-blue-600 rounded-full flex items-center justify-center text-white hover:bg-blue-700 transition-colors">
                        <Plus className="w-3.5 h-3.5" />
                      </button>
                    )}
                  </div>
                  <p className="text-[11px] text-slate-400 ml-1">Go tinh nang roi nhan <kbd className="bg-slate-100 px-1.5 py-0.5 rounded text-[10px] font-mono">Enter</kbd> de them.</p>
                </div>
                <label className="flex items-center gap-3 p-4 bg-slate-50 rounded-2xl cursor-pointer hover:bg-slate-100 transition-colors">
                  <div className="relative">
                    <input type="checkbox" className="sr-only peer" checked={formData.isActive} onChange={e => setFormData({...formData, isActive: e.target.checked})} />
                    <div className="w-12 h-6 bg-slate-300 rounded-full peer peer-checked:bg-blue-600 transition-all" />
                    <div className="absolute top-1 left-1 w-4 h-4 bg-white rounded-full transition-all peer-checked:translate-x-6" />
                  </div>
                  <span className="text-sm font-bold text-slate-700">Kich hoat goi dich vu ngay lap tuc</span>
                </label>
              </div>
              <div className="flex gap-4 pt-4 mt-6">
                <button type="button" onClick={() => setShowModal(false)} className="flex-1 py-3.5 px-6 rounded-2xl bg-slate-100 text-slate-600 font-bold hover:bg-slate-200 transition-all active:scale-95">Huy bo</button>
                <button type="submit" className="flex-[2] py-3.5 px-6 rounded-2xl bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-bold hover:shadow-lg hover:shadow-blue-500/25 transition-all active:scale-95">Luu thay doi</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ManagerSubscriptionsPage;