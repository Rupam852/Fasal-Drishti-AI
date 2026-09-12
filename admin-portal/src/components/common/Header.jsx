import React, { useState } from 'react';
import { 
  Bell, 
  Search, 
  ShieldCheck, 
  LogOut, 
  RefreshCw, 
  Wifi, 
  User, 
  CheckCircle2, 
  AlertTriangle,
  ExternalLink
} from 'lucide-react';

export default function Header({ 
  adminSession, 
  onLogout, 
  onRefreshData, 
  isRefreshing,
  activeTab,
  onNavigateTab,
  searchQuery,
  onSearchChange
}) {
  const [showNotifications, setShowNotifications] = useState(false);
  const [showProfileMenu, setShowProfileMenu] = useState(false);

  const notifications = [
    { id: 1, title: 'Critical Outbreak Detected', text: 'Potato Late Blight surging in Agra & Aligarh (UP)', time: '12m ago', type: 'alert' },
    { id: 2, title: 'New App Version v1.2.0 Active', text: '5,200+ farmers already upgraded via OTA updater', time: '1h ago', type: 'info' },
    { id: 3, title: 'Mandi Rate Spike', text: 'Red Chilli prices crossed ₹24,000/Q in Guntur', time: '3h ago', type: 'success' },
  ];

  return (
    <header className="sticky top-0 z-30 w-full glass-panel border-b border-white/10 px-4 lg:px-8 py-3.5 flex items-center justify-between transition-all">
      {/* Left Search & Context */}
      <div className="flex items-center gap-4 flex-1 max-w-xl">
        <div className="relative w-full">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Search scans, crop diseases, mandi markets, farmers..."
            className="w-full pl-10 pr-4 py-2 bg-slate-900/80 border border-slate-700/60 rounded-xl text-sm text-slate-100 placeholder-slate-400 focus:outline-none focus:border-emerald-500/80 focus:ring-2 focus:ring-emerald-500/20 transition-all"
          />
          {searchQuery && (
            <button 
              onClick={() => onSearchChange('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-slate-400 hover:text-slate-200"
            >
              Clear
            </button>
          )}
        </div>
      </div>

      {/* Right Actions & Status */}
      <div className="flex items-center gap-3">
        {/* Live System Sync Indicator */}
        <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-xs font-medium text-emerald-400">
          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping inline-block" />
          <Wifi className="w-3.5 h-3.5" />
          <span>Supabase Live Sync</span>
        </div>

        {/* Refresh Button */}
        <button
          onClick={onRefreshData}
          disabled={isRefreshing}
          title="Refresh live data"
          className="p-2 rounded-xl bg-slate-800/80 hover:bg-slate-700 text-slate-300 hover:text-white border border-slate-700/50 transition-all disabled:opacity-50"
        >
          <RefreshCw className={`w-4 h-4 ${isRefreshing ? 'animate-spin text-emerald-400' : ''}`} />
        </button>

        {/* Notifications Bell */}
        <div className="relative">
          <button
            onClick={() => setShowNotifications(!showNotifications)}
            className="relative p-2 rounded-xl bg-slate-800/80 hover:bg-slate-700 text-slate-300 hover:text-white border border-slate-700/50 transition-all"
          >
            <Bell className="w-4 h-4" />
            <span className="absolute top-1 right-1 w-2 h-2 bg-emerald-500 rounded-full" />
          </button>

          {showNotifications && (
            <div className="absolute right-0 mt-2 w-80 sm:w-96 rounded-2xl glass-panel border border-white/10 shadow-2xl p-4 z-50 animate-in fade-in slide-in-from-top-2">
              <div className="flex items-center justify-between pb-3 border-b border-white/10 mb-2">
                <span className="font-semibold text-sm text-slate-200">National Agri Notifications</span>
                <span className="text-[11px] px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 font-medium">3 New</span>
              </div>
              <div className="space-y-2.5">
                {notifications.map((n) => (
                  <div key={n.id} className="p-2.5 rounded-xl bg-slate-900/60 hover:bg-slate-800/80 border border-white/5 transition-all text-left">
                    <div className="flex items-center justify-between gap-2">
                      <span className="text-xs font-semibold text-slate-200 flex items-center gap-1.5">
                        {n.type === 'alert' && <AlertTriangle className="w-3.5 h-3.5 text-amber-400" />}
                        {n.type === 'info' && <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />}
                        {n.title}
                      </span>
                      <span className="text-[10px] text-slate-400">{n.time}</span>
                    </div>
                    <p className="text-xs text-slate-400 mt-1">{n.text}</p>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Admin Profile Dropdown */}
        <div className="relative">
          <button
            onClick={() => setShowProfileMenu(!showProfileMenu)}
            className="flex items-center gap-3 pl-2 pr-3 py-1.5 rounded-xl bg-slate-800/80 hover:bg-slate-700/80 border border-slate-700/60 transition-all text-left"
          >
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-emerald-500 to-teal-700 flex items-center justify-center font-bold text-white shadow-md text-xs">
              <ShieldCheck className="w-4 h-4 text-white" />
            </div>
            <div className="hidden md:block">
              <div className="text-xs font-bold text-slate-100 flex items-center gap-1">
                {adminSession?.name || 'Super Admin'}
              </div>
              <div className="text-[10px] text-emerald-400 font-medium truncate max-w-[120px]">
                {adminSession?.role?.split('&')[0] || 'Chief Agronomist'}
              </div>
            </div>
          </button>

          {showProfileMenu && (
            <div className="absolute right-0 mt-2 w-64 rounded-2xl glass-panel border border-white/10 shadow-2xl p-3 z-50 animate-in fade-in slide-in-from-top-2 text-left">
              <div className="px-3 py-2 border-b border-white/10 mb-2">
                <p className="text-xs font-bold text-white">{adminSession?.name}</p>
                <p className="text-[11px] text-slate-400">{adminSession?.email}</p>
                <span className="inline-block mt-1 text-[10px] px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 font-mono">
                  {adminSession?.username}
                </span>
              </div>
              
              <button
                onClick={() => {
                  onNavigateTab('settings');
                  setShowProfileMenu(false);
                }}
                className="w-full flex items-center gap-2.5 px-3 py-2 rounded-xl text-xs text-slate-300 hover:text-white hover:bg-slate-800/80 transition-all"
              >
                <User className="w-4 h-4 text-slate-400" />
                <span>Admin Settings & Security</span>
              </button>

              <button
                onClick={onLogout}
                className="w-full flex items-center gap-2.5 px-3 py-2 mt-1 rounded-xl text-xs text-red-400 hover:text-red-300 hover:bg-red-500/10 transition-all"
              >
                <LogOut className="w-4 h-4 text-red-400" />
                <span>Secure Log Out</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
