import React from 'react';
import { 
  LayoutDashboard, 
  ScanLine, 
  MapPin, 
  TrendingUp, 
  DownloadCloud, 
  Megaphone, 
  Cpu, 
  Users, 
  Settings,
  Sparkles,
  ChevronRight,
  ShieldAlert,
  Sprout
} from 'lucide-react';

export default function Sidebar({ activeTab, onSelectTab, pendingReviewCount = 2 }) {
  const menuItems = [
    { id: 'dashboard', label: 'Command Center', icon: LayoutDashboard, badge: null },
    { id: 'scans', label: 'Crop Scan Monitor', icon: ScanLine, badge: pendingReviewCount > 0 ? `${pendingReviewCount} New` : null, badgeColor: 'bg-amber-500/20 text-amber-300 border-amber-500/30' },
    { id: 'outbreaks', label: 'Outbreak Radar', icon: MapPin, badge: 'Live', badgeColor: 'bg-red-500/20 text-red-300 border-red-500/30' },
    { id: 'mandi', label: 'Mandi Bhav Manager', icon: TrendingUp, badge: null },
    { id: 'updater', label: 'In-App OTA Updates', icon: DownloadCloud, badge: 'v1.2.0', badgeColor: 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30' },
    { id: 'broadcast', label: 'Broadcast & Alerts', icon: Megaphone, badge: null },
    { id: 'ai-hub', label: 'AI Engine Controller', icon: Cpu, badge: '3 Active' },
    { id: 'farmers', label: 'Farmers Directory', icon: Users, badge: null },
    { id: 'settings', label: 'Security & Settings', icon: Settings, badge: null },
  ];

  return (
    <aside className="w-64 lg:w-72 glass-panel border-r border-white/10 flex flex-col justify-between p-4 min-h-screen shrink-0 transition-all">
      <div>
        {/* Brand Logo Header */}
        <div className="flex items-center gap-3 px-2 py-3 mb-6 border-b border-white/10">
          <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-emerald-600 via-teal-500 to-emerald-400 p-0.5 shadow-lg shadow-emerald-900/30 flex items-center justify-center">
            <div className="w-full h-full bg-slate-950 rounded-[14px] flex items-center justify-center">
              <Sprout className="w-5 h-5 text-emerald-400" />
            </div>
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <span className="font-extrabold text-base tracking-tight text-white">Fasal Drishti</span>
              <span className="text-[10px] uppercase font-bold tracking-widest px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">AI</span>
            </div>
            <p className="text-[11px] font-medium text-slate-400 tracking-wide">National Admin Console</p>
          </div>
        </div>

        {/* Navigation Items */}
        <nav className="space-y-1.5">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;

            return (
              <button
                key={item.id}
                onClick={() => onSelectTab(item.id)}
                className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl text-sm font-semibold transition-all group ${
                  isActive
                    ? 'bg-gradient-to-r from-emerald-600/90 to-teal-700/80 text-white shadow-lg shadow-emerald-950/40 border border-emerald-400/30'
                    : 'text-slate-300 hover:text-white hover:bg-slate-800/60 border border-transparent'
                }`}
              >
                <div className="flex items-center gap-3">
                  <Icon className={`w-4 h-4 transition-transform group-hover:scale-110 ${isActive ? 'text-white' : 'text-slate-400 group-hover:text-emerald-400'}`} />
                  <span>{item.label}</span>
                </div>

                <div className="flex items-center gap-2">
                  {item.badge && (
                    <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold border ${item.badgeColor || 'bg-slate-800 text-slate-300 border-slate-700'}`}>
                      {item.badge}
                    </span>
                  )}
                  {isActive && <ChevronRight className="w-3.5 h-3.5 text-emerald-200" />}
                </div>
              </button>
            );
          })}
        </nav>
      </div>

      {/* Bottom Mission Card */}
      <div className="mt-6 p-3.5 rounded-2xl bg-gradient-to-br from-slate-900 via-slate-900 to-emerald-950/60 border border-emerald-500/20 shadow-xl">
        <div className="flex items-center gap-2 mb-1.5">
          <div className="p-1 rounded-lg bg-emerald-500/20 text-emerald-400">
            <Sparkles className="w-3.5 h-3.5" />
          </div>
          <span className="text-xs font-bold text-slate-200">SIH 2024 Flagship</span>
        </div>
        <p className="text-[11px] text-slate-400 leading-relaxed">
          Real-time AI Agronomy Network serving 14,800+ farmers across 18 Indian states.
        </p>
        <div className="mt-2.5 pt-2 border-t border-white/5 flex items-center justify-between text-[10px] text-emerald-400 font-mono">
          <span>Server: Mumbai, IN</span>
          <span className="flex items-center gap-1">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
            99.9% Up
          </span>
        </div>
      </div>
    </aside>
  );
}
