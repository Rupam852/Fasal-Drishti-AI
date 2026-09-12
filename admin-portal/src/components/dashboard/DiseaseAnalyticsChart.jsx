import React from 'react';
import { 
  AreaChart, 
  Area, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  Legend
} from 'recharts';
import { Activity, TrendingUp, Filter } from 'lucide-react';

export default function DiseaseAnalyticsChart({ data }) {
  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div className="glass-panel p-3 rounded-xl border border-white/20 shadow-xl text-xs text-left">
          <p className="font-bold text-white mb-1.5">{label} — Scan Velocity</p>
          <div className="space-y-1">
            <p className="text-emerald-400 font-medium">Healthy Crops: {payload[0]?.value} scans</p>
            <p className="text-amber-400 font-medium">Diseased Leaves: {payload[1]?.value} scans</p>
            <p className="text-cyan-400 font-mono text-[11px]">AI Confidence: {payload[2]?.value || '98.5'}%</p>
          </div>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="glass-panel p-5 rounded-2xl border border-white/10 shadow-xl text-left">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-5">
        <div>
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-emerald-500/20 text-emerald-400">
              <TrendingUp className="w-4 h-4" />
            </div>
            <h3 className="text-base font-bold text-white">Daily Scan Velocity & Outbreak Trends</h3>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Realtime ratio of healthy vs infected crop leaf submissions across Indian farmlands
          </p>
        </div>

        <div className="flex items-center gap-2 text-xs">
          <span className="px-2.5 py-1 rounded-lg bg-slate-800 text-slate-300 border border-slate-700 font-medium">
            Last 7 Days
          </span>
        </div>
      </div>

      <div className="h-[280px] w-full">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
            <defs>
              <linearGradient id="colorDiseased" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#f59e0b" stopOpacity={0.4}/>
                <stop offset="95%" stopColor="#f59e0b" stopOpacity={0}/>
              </linearGradient>
              <linearGradient id="colorHealthy" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#10b981" stopOpacity={0.4}/>
                <stop offset="95%" stopColor="#10b981" stopOpacity={0}/>
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
            <XAxis dataKey="day" stroke="#64748b" tick={{ fontSize: 12 }} />
            <YAxis stroke="#64748b" tick={{ fontSize: 12 }} />
            <Tooltip content={<CustomTooltip />} />
            <Legend 
              wrapperStyle={{ fontSize: '12px', paddingTop: '10px' }}
              formatter={(value) => <span className="text-slate-300 capitalize">{value}</span>}
            />
            <Area 
              type="monotone" 
              dataKey="healthy" 
              name="Healthy Foliage" 
              stroke="#10b981" 
              strokeWidth={2.5}
              fillOpacity={1} 
              fill="url(#colorHealthy)" 
            />
            <Area 
              type="monotone" 
              dataKey="diseased" 
              name="Infected / Disease Detected" 
              stroke="#f59e0b" 
              strokeWidth={2.5}
              fillOpacity={1} 
              fill="url(#colorDiseased)" 
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
