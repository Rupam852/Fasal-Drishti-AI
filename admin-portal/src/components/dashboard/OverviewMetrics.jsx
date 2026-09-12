import React from 'react';
import { 
  Users, 
  Scan, 
  Sparkles, 
  AlertTriangle, 
  ArrowUpRight, 
  Activity,
  Cpu,
  BookOpen
} from 'lucide-react';

export default function OverviewMetrics({ metrics = {}, appConfigs = {} }) {
  const cards = [
    {
      id: 'farmers',
      title: 'Registered Farmers',
      value: (metrics.totalFarmers || 0).toLocaleString(),
      change: 'Live Database',
      isPositive: true,
      subtitle: 'Authenticated Supabase Profiles',
      icon: Users,
      gradient: 'from-emerald-500/20 to-teal-500/5',
      borderColor: 'border-emerald-500/30',
      iconColor: 'text-emerald-400 bg-emerald-500/10'
    },
    {
      id: 'scans',
      title: 'Total Disease Scans',
      value: (metrics.totalScans || 0).toLocaleString(),
      change: `${metrics.totalHealthy || 0} Healthy`,
      isPositive: true,
      subtitle: `${metrics.totalDiseased || 0} Disease Detections`,
      icon: Scan,
      gradient: 'from-cyan-500/20 to-blue-500/5',
      borderColor: 'border-cyan-500/30',
      iconColor: 'text-cyan-400 bg-cyan-500/10'
    },
    {
      id: 'pathology',
      title: 'Pathology Knowledge Base',
      value: `${metrics.totalPathologyClasses || 12} Classes`,
      change: 'Active in Cloud',
      isPositive: true,
      subtitle: 'TFLite & Gemini Multi-Model',
      icon: BookOpen,
      gradient: 'from-purple-500/20 to-pink-500/5',
      borderColor: 'border-purple-500/30',
      iconColor: 'text-purple-400 bg-purple-500/10'
    },
    {
      id: 'ai-engines',
      title: 'Cloud AI Status',
      value: appConfigs['gemini_model_name']?.value || 'Gemini 3.7 Flash',
      change: 'NVIDIA Active',
      isPositive: true,
      subtitle: 'Live API Remote Switchboard',
      icon: Cpu,
      gradient: 'from-amber-500/20 to-emerald-500/5',
      borderColor: 'border-emerald-500/30',
      iconColor: 'text-emerald-400 bg-emerald-500/10'
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
                <h3 className="text-xl lg:text-2xl font-black text-white mt-1.5 tracking-tight font-mono truncate max-w-[200px]">
                  {c.value}
                </h3>
              </div>
              <div className={`p-3 rounded-2xl ${c.iconColor} border border-white/5 shrink-0`}>
                <Icon className="w-5 h-5" />
              </div>
            </div>

            <div className="mt-4 pt-3 border-t border-white/10 flex items-center justify-between text-xs">
              <span className="text-slate-400 font-medium flex items-center gap-1.5 truncate max-w-[170px]">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 shrink-0" />
                {c.subtitle}
              </span>

              <span className={`inline-flex items-center gap-0.5 px-2 py-0.5 rounded-full text-[11px] font-bold shrink-0 ${
                c.isPositive 
                  ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                  : 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              }`}>
                {c.change}
              </span>
            </div>
          </div>
        );
      })}
    </div>
  );
}
