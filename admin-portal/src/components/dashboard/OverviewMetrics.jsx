import React from 'react';
import { 
  Users, 
  Scan, 
  Sparkles, 
  AlertTriangle, 
  ArrowUpRight, 
  ArrowDownRight,
  Activity,
  CheckCircle2,
  TrendingUp
} from 'lucide-react';

export default function OverviewMetrics({ stats }) {
  const cards = [
    {
      id: 'farmers',
      title: 'Total Registered Farmers',
      value: (stats.totalFarmers || 14850).toLocaleString(),
      change: stats.farmersGrowth || '+18.4%',
      isPositive: true,
      subtitle: 'Across 18 States',
      icon: Users,
      gradient: 'from-emerald-500/20 to-teal-500/5',
      borderColor: 'border-emerald-500/30',
      iconColor: 'text-emerald-400 bg-emerald-500/10'
    },
    {
      id: 'scans',
      title: 'Total Disease Scans',
      value: (stats.totalScans || 62410).toLocaleString(),
      change: stats.scansGrowth || '+24.1%',
      isPositive: true,
      subtitle: `${stats.scansToday || 842} Scans Today`,
      icon: Scan,
      gradient: 'from-cyan-500/20 to-blue-500/5',
      borderColor: 'border-cyan-500/30',
      iconColor: 'text-cyan-400 bg-cyan-500/10'
    },
    {
      id: 'accuracy',
      title: 'AI Diagnostic Precision',
      value: stats.aiAccuracy || '98.4%',
      change: '+1.2%',
      isPositive: true,
      subtitle: 'MobileNet + Gemini NIM',
      icon: Sparkles,
      gradient: 'from-purple-500/20 to-pink-500/5',
      borderColor: 'border-purple-500/30',
      iconColor: 'text-purple-400 bg-purple-500/10'
    },
    {
      id: 'outbreaks',
      title: 'Active Outbreak Alerts',
      value: stats.activeOutbreaks || '7 Hotspots',
      change: 'Urgent',
      isPositive: false,
      subtitle: 'Wheat Rust & Potato Blight',
      icon: AlertTriangle,
      gradient: 'from-amber-500/20 to-red-500/5',
      borderColor: 'border-amber-500/30',
      iconColor: 'text-amber-400 bg-amber-500/10'
    },
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 lg:gap-5 mb-6">
      {cards.map((c) => {
        const Icon = c.icon;
        return (
          <div
            key={c.id}
            className={`glass-panel p-5 rounded-2xl border ${c.borderColor} bg-gradient-to-br ${c.gradient} hover:translate-y-[-2px] transition-all duration-300 shadow-lg`}
          >
            <div className="flex items-start justify-between">
              <div>
                <p className="text-xs font-semibold text-slate-400 tracking-wide uppercase">{c.title}</p>
                <h3 className="text-2xl lg:text-3xl font-black text-white mt-1.5 tracking-tight font-mono">
                  {c.value}
                </h3>
              </div>
              <div className={`p-3 rounded-2xl ${c.iconColor} border border-white/5`}>
                <Icon className="w-5 h-5" />
              </div>
            </div>

            <div className="mt-4 pt-3 border-t border-white/10 flex items-center justify-between text-xs">
              <span className="text-slate-400 font-medium flex items-center gap-1.5">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
                {c.subtitle}
              </span>

              <span className={`inline-flex items-center gap-0.5 px-2 py-0.5 rounded-full text-[11px] font-bold ${
                c.isPositive 
                  ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                  : 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              }`}>
                {c.isPositive ? <ArrowUpRight className="w-3 h-3" /> : <Activity className="w-3 h-3" />}
                {c.change}
              </span>
            </div>
          </div>
        );
      })}
    </div>
  );
}
