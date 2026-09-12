import React, { useState } from 'react';
import { 
  TrendingUp, 
  TrendingDown, 
  Minus, 
  Plus, 
  Search, 
  MapPin, 
  Edit, 
  Trash2, 
  Download, 
  CheckCircle2, 
  X,
  Sparkles
} from 'lucide-react';

export default function MandiManager({ mandiRates, onAddRate, onUpdateRate, onDeleteRate }) {
  const [rates, setRates] = useState(mandiRates);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedState, setSelectedState] = useState('All');
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingRate, setEditingRate] = useState(null);

  const [formData, setFormData] = useState({
    commodity: '',
    variety: '',
    market: '',
    district: '',
    state: '',
    minPrice: '',
    maxPrice: '',
    modalPrice: '',
    trend: 'up',
    change: '+₹50',
    arrivalQty: '500 Tonnes'
  });

  const statesList = ['All', 'Maharashtra', 'Delhi (NCT)', 'Punjab', 'Andhra Pradesh', 'West Bengal', 'Madhya Pradesh', 'Uttar Pradesh', 'Gujarat'];

  const filteredRates = rates.filter((r) => {
    const matchState = selectedState === 'All' || r.state === selectedState;
    const matchQuery = !searchQuery || 
      r.commodity.toLowerCase().includes(searchQuery.toLowerCase()) ||
      r.market.toLowerCase().includes(searchQuery.toLowerCase()) ||
      r.district.toLowerCase().includes(searchQuery.toLowerCase());
    return matchState && matchQuery;
  });

  const handleSaveForm = (e) => {
    e.preventDefault();
    if (!formData.commodity || !formData.market || !formData.modalPrice) return;

    if (editingRate) {
      const updated = rates.map(r => r.id === editingRate.id ? { ...formData, id: r.id, lastUpdated: 'Just now' } : r);
      setRates(updated);
      if (onUpdateRate) onUpdateRate(formData);
    } else {
      const newEntry = {
        ...formData,
        id: `mandi-${Date.now()}`,
        unit: '₹/Quintal',
        lastUpdated: 'Just now'
      };
      setRates([newEntry, ...rates]);
      if (onAddRate) onAddRate(newEntry);
    }

    setShowAddModal(false);
    setEditingRate(null);
    setFormData({
      commodity: '',
      variety: '',
      market: '',
      district: '',
      state: '',
      minPrice: '',
      maxPrice: '',
      modalPrice: '',
      trend: 'up',
      change: '+₹50',
      arrivalQty: '500 Tonnes'
    });
  };

  const handleEditClick = (rate) => {
    setEditingRate(rate);
    setFormData(rate);
    setShowAddModal(true);
  };

  const handleDelete = (id) => {
    if (confirm('Are you sure you want to delete this Mandi price entry?')) {
      const updated = rates.filter(r => r.id !== id);
      setRates(updated);
      if (onDeleteRate) onDeleteRate(id);
    }
  };

  return (
    <div className="space-y-6 text-left">
      {/* Top Header */}
      <div className="glass-panel p-5 lg:p-6 rounded-2xl border border-white/10 shadow-xl">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div>
            <h2 className="text-xl font-black text-white flex items-center gap-2.5">
              <TrendingUp className="w-5 h-5 text-emerald-400" />
              National APMC Mandi Bhav & Commodity Rates
            </h2>
            <p className="text-xs text-slate-400 mt-1">
              Publish and regulate realtime market prices reflected in the Fasal Drishti farmer mobile app
            </p>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={() => {
                setEditingRate(null);
                setFormData({
                  commodity: '',
                  variety: '',
                  market: '',
                  district: '',
                  state: '',
                  minPrice: '',
                  maxPrice: '',
                  modalPrice: '',
                  trend: 'up',
                  change: '+₹50',
                  arrivalQty: '500 Tonnes'
                });
                setShowAddModal(true);
              }}
              className="px-4 py-2 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white font-bold text-xs shadow-lg shadow-emerald-950/40 transition-all flex items-center gap-2"
            >
              <Plus className="w-4 h-4" />
              <span>Add Live Mandi Rate</span>
            </button>
          </div>
        </div>

        {/* Filters */}
        <div className="mt-5 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-12 gap-3">
          <div className="lg:col-span-8 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search commodity (e.g. Tomato, Wheat, Red Chilli), market yard, or district..."
              className="w-full pl-9 pr-4 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-400 focus:outline-none focus:border-emerald-500"
            />
          </div>

          <div className="lg:col-span-4">
            <select
              value={selectedState}
              onChange={(e) => setSelectedState(e.target.value)}
              className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-emerald-500 font-medium"
            >
              {statesList.map((s) => (
                <option key={s} value={s}>{s === 'All' ? 'All States APMC' : s}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Mandi Rates Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4 lg:gap-5">
        {filteredRates.map((rate) => {
          const isUp = rate.trend === 'up';
          const isDown = rate.trend === 'down';

          return (
            <div
              key={rate.id}
              className="glass-panel p-5 rounded-2xl border border-white/10 hover:border-emerald-500/40 transition-all shadow-lg text-left flex flex-col justify-between group"
            >
              <div>
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <h3 className="text-base font-extrabold text-white">{rate.commodity}</h3>
                    <p className="text-xs text-slate-400 font-medium">{rate.variety}</p>
                  </div>

                  <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold border ${
                    isUp 
                      ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30'
                      : isDown
                      ? 'bg-red-500/20 text-red-300 border-red-500/30'
                      : 'bg-slate-800 text-slate-300 border-slate-700'
                  }`}>
                    {isUp && <TrendingUp className="w-3 h-3" />}
                    {isDown && <TrendingDown className="w-3 h-3" />}
                    {!isUp && !isDown && <Minus className="w-3 h-3" />}
                    {rate.change}
                  </span>
                </div>

                {/* Price Display */}
                <div className="my-4 p-3.5 rounded-xl bg-slate-900/80 border border-white/5 flex items-center justify-between">
                  <div>
                    <p className="text-[10px] text-slate-400 font-semibold uppercase tracking-wider">Modal Benchmark</p>
                    <p className="text-2xl font-black text-white font-mono mt-0.5">
                      ₹{rate.modalPrice.toLocaleString()}
                      <span className="text-xs text-slate-400 font-sans font-normal ml-1">/ Quintal</span>
                    </p>
                  </div>

                  <div className="text-right text-xs">
                    <p className="text-slate-400 text-[11px]">Min: ₹{rate.minPrice}</p>
                    <p className="text-emerald-400 text-[11px] font-semibold">Max: ₹{rate.maxPrice}</p>
                  </div>
                </div>

                {/* Market Location info */}
                <div className="space-y-1 text-xs text-slate-300">
                  <div className="flex items-center gap-1.5 font-semibold text-slate-200">
                    <MapPin className="w-3.5 h-3.5 text-emerald-400" />
                    <span>{rate.market}</span>
                  </div>
                  <p className="text-slate-400 text-[11px] pl-5">{rate.district}, {rate.state}</p>
                </div>
              </div>

              {/* Bottom footer & Actions */}
              <div className="mt-4 pt-3 border-t border-white/10 flex items-center justify-between text-xs">
                <span className="text-[11px] text-slate-400 font-mono">{rate.lastUpdated}</span>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => handleEditClick(rate)}
                    className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white transition-all"
                  >
                    <Edit className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={() => handleDelete(rate.id)}
                    className="p-1.5 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-400 hover:text-red-300 transition-all"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Add / Edit Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-in fade-in">
          <div className="w-full max-w-lg glass-panel border border-emerald-500/30 rounded-3xl p-6 bg-slate-950/95 text-left shadow-2xl">
            <div className="flex items-center justify-between pb-4 border-b border-white/10">
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <Sparkles className="w-4 h-4 text-emerald-400" />
                {editingRate ? 'Edit APMC Mandi Rate' : 'Publish New Mandi Rate'}
              </h3>
              <button onClick={() => setShowAddModal(false)} className="p-1 text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSaveForm} className="mt-4 space-y-3.5 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-slate-400 font-semibold">Commodity (e.g. Tomato)</label>
                  <input
                    type="text"
                    required
                    value={formData.commodity}
                    onChange={(e) => setFormData({ ...formData, commodity: e.target.value })}
                    className="w-full mt-1 p-2.5 bg-slate-900 border border-slate-700 rounded-xl text-white text-xs focus:border-emerald-500"
                  />
                </div>
                <div>
                  <label className="text-slate-400 font-semibold">Variety (e.g. Hybrid Desi)</label>
                  <input
                    type="text"
                    value={formData.variety}
                    onChange={(e) => setFormData({ ...formData, variety: e.target.value })}
                    className="w-full mt-1 p-2.5 bg-slate-900 border border-slate-700 rounded-xl text-white text-xs focus:border-emerald-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="text-slate-400 font-semibold">Market Name</label>
                  <input
                    type="text"
                    required
                    value={formData.market}
                    onChange={(e) => setFormData({ ...formData, market: e.target.value })}
                    className="w-full mt-1 p-2.5 bg-slate-900 border border-slate-700 rounded-xl text-white text-xs focus:border-emerald-500"
                  />
                </div>
                <div>
                  <label className="text-slate-400 font-semibold">District</label>
                  <input
                    type="text"
                    value={formData.district}
                    onChange={(e) => setFormData({ ...formData, district: e.target.value })}
                    className="w-full mt-1 p-2.5 bg-slate-900 border border-slate-700 rounded-xl text-white text-xs focus:border-emerald-500"
                  />
                </div>
                <div>
                  <label className="text-slate-400 font-semibold">State</label>
                  <input
                    type="text"
                    required
                    value={formData.state}
                    onChange={(e) => setFormData({ ...formData, state: e.target.value })}
                    className="w-full mt-1 p-2.5 bg-slate-900 border border-slate-700 rounded-xl text-white text-xs focus:border-emerald-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="text-slate-400 font-semibold">Min (₹/Q)</label>
                  <input
                    type="number"
                    required
                    value={formData.minPrice}
                    onChange={(e) => setFormData({ ...formData, minPrice: Number(e.target.value) })}
                    className="w-full mt-1 p-2.5 bg-slate-900 border border-slate-700 rounded-xl text-white text-xs focus:border-emerald-500 font-mono"
                  />
                </div>
                <div>
                  <label className="text-slate-400 font-semibold">Max (₹/Q)</label>
                  <input
                    type="number"
                    required
                    value={formData.maxPrice}
                    onChange={(e) => setFormData({ ...formData, maxPrice: Number(e.target.value) })}
                    className="w-full mt-1 p-2.5 bg-slate-900 border border-slate-700 rounded-xl text-white text-xs focus:border-emerald-500 font-mono"
                  />
                </div>
                <div>
                  <label className="text-emerald-400 font-bold">Modal Rate (₹/Q)</label>
                  <input
                    type="number"
                    required
                    value={formData.modalPrice}
                    onChange={(e) => setFormData({ ...formData, modalPrice: Number(e.target.value) })}
                    className="w-full mt-1 p-2.5 bg-slate-900 border border-emerald-500 rounded-xl text-white text-xs focus:border-emerald-400 font-mono font-bold"
                  />
                </div>
              </div>

              <div className="pt-4 flex justify-end gap-2.5 border-t border-white/10">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 rounded-xl bg-slate-800 text-slate-300 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-white font-bold"
                >
                  {editingRate ? 'Update Mandi Rate' : 'Publish Mandi Rate'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
