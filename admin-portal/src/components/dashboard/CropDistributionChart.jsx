import React from 'react';
import { 
  PieChart, 
  Pie, 
  Cell, 
  ResponsiveContainer, 
  Tooltip 
} from 'recharts';
import { PieChart as PieIcon, Layers } from 'lucide-react';

export default function CropDistributionChart({ data = [] }) {
  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      return (
        <div className="glass-panel p-2.5 rounded-xl border border-white/20 shadow-xl text-xs text-left">
          <p className="font-bold text-white">{payload[0]?.name}</p>
          <p className="text-emerald-400 font-mono font-semibold">{payload[0]?.value}% of scans ({payload[0]?.payload?.count || 0} items)</p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="glass-panel p-5 rounded-2xl border border-white/10 shadow-xl text-left flex flex-col justify-between">
      <div>
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-emerald-500/20 text-emerald-400">
              <PieIcon className="w-4 h-4" />
            </div>
            <h3 className="text-base font-bold text-white">Crop Classification Share</h3>
          </div>
          <span className="text-[11px] px-2 py-0.5 rounded-full bg-slate-800 text-emerald-400 font-mono font-bold">
            Live DB
          </span>
        </div>
        <p className="text-xs text-slate-400">
          Distribution computed dynamically from active pathology records & scans
        </p>
      </div>

      {data.length === 0 ? (
        <div className="h-[200px] flex flex-col items-center justify-center text-center p-4">
          <Layers className="w-8 h-8 text-slate-600 mb-2" />
          <p className="text-xs text-slate-400">No crop scans in database yet.</p>
          <p className="text-[10px] text-slate-500 mt-1">Data will appear automatically as farmers scan crops.</p>
        </div>
      ) : (
        <>
          <div className="h-[200px] w-full my-2">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Tooltip content={<CustomTooltip />} />
                <Pie
                  data={data}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={85}
                  paddingAngle={4}
                  dataKey="value"
                >
                  {data.map((entry, index) => (
                    <Cell 
                      key={`cell-${index}`} 
                      fill={entry.color} 
                      stroke="rgba(0,0,0,0.4)" 
                      strokeWidth={2}
                    />
                  ))}
                </Pie>
              </PieChart>
            </ResponsiveContainer>
          </div>

          {/* Legend list */}
          <div className="grid grid-cols-2 gap-2 text-xs pt-2 border-t border-white/10 max-h-[90px] overflow-y-auto">
            {data.map((item, idx) => (
              <div key={idx} className="flex items-center gap-2">
                <span 
                  className="w-2.5 h-2.5 rounded-full shrink-0" 
                  style={{ backgroundColor: item.color }} 
                />
                <span className="text-slate-300 truncate text-[11px] font-medium">{item.name}</span>
                <span className="text-slate-400 font-mono text-[10px] ml-auto font-bold">{item.value}%</span>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
