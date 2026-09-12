import React from 'react';
import { 
  ScanLine, 
  CheckCircle2, 
  AlertCircle, 
  ArrowRight, 
  Clock, 
  MapPin, 
  Eye,
  ShieldCheck
} from 'lucide-react';

export default function RecentScansFeed({ scans, onViewScan, onNavigateAll }) {
  return (
    <div className="glass-panel p-5 rounded-2xl border border-white/10 shadow-xl text-left">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <ScanLine className="w-4 h-4 text-emerald-400" />
            Live Farmer Scan Telemetry Feed
          </h3>
          <p className="text-xs text-slate-400 mt-0.5">
            Realtime diagnosis streaming from Android client devices
          </p>
        </div>

        <button
          onClick={onNavigateAll}
          className="text-xs font-semibold text-emerald-400 hover:text-emerald-300 flex items-center gap-1 transition-colors"
        >
          <span>View All Scans</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </button>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse text-xs">
          <thead>
            <tr className="border-b border-white/10 text-slate-400 font-semibold uppercase text-[10px] tracking-wider">
              <th className="pb-3 pl-2">Leaf Sample</th>
              <th className="pb-3">Farmer & Location</th>
              <th className="pb-3">Crop Type</th>
              <th className="pb-3">AI Prediction</th>
              <th className="pb-3">Confidence</th>
              <th className="pb-3">Severity</th>
              <th className="pb-3 text-right pr-2">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-white/5">
            {scans.slice(0, 5).map((scan) => {
              const isHealthy = scan.severity === 'Healthy' || scan.diseaseName.toLowerCase().includes('healthy');
              const isCritical = scan.severity === 'Critical';

              return (
                <tr 
                  key={scan.id} 
                  className="hover:bg-slate-800/40 transition-colors group cursor-pointer"
                  onClick={() => onViewScan(scan)}
                >
                  <td className="py-3 pl-2">
                    <div className="w-12 h-12 rounded-xl overflow-hidden bg-slate-800 border border-white/10 shrink-0">
                      <img 
                        src={scan.imageUrl} 
                        alt={scan.cropName} 
                        className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300"
                        onError={(e) => {
                          e.target.src = 'https://images.unsplash.com/photo-1592841200221-a6898f307baa?auto=format&fit=crop&w=300&q=80';
                        }}
                      />
                    </div>
                  </td>

                  <td className="py-3">
                    <p className="font-bold text-white text-xs">{scan.farmerName || 'Registered Farmer'}</p>
                    <p className="text-slate-400 text-[11px] flex items-center gap-1 mt-0.5">
                      <MapPin className="w-3 h-3 text-slate-500" />
                      {scan.location || 'India'}
                    </p>
                  </td>

                  <td className="py-3">
                    <span className="font-semibold text-slate-200">{scan.cropName}</span>
                  </td>

                  <td className="py-3">
                    <p className="font-bold text-slate-100">{scan.diseaseName}</p>
                    <span className="text-[10px] text-slate-400 flex items-center gap-1 mt-0.5">
                      <Clock className="w-2.5 h-2.5" />
                      {scan.createdAt}
                    </span>
                  </td>

                  <td className="py-3 font-mono">
                    <span className="px-2 py-0.5 rounded-md bg-slate-800 font-bold text-slate-200 border border-white/5">
                      {Math.round((scan.confidence || 0.95) * 100)}%
                    </span>
                  </td>

                  <td className="py-3">
                    <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider border ${
                      isHealthy
                        ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30'
                        : isCritical
                        ? 'bg-red-500/20 text-red-300 border-red-500/30'
                        : 'bg-amber-500/20 text-amber-300 border-amber-500/30'
                    }`}>
                      {scan.severity}
                    </span>
                  </td>

                  <td className="py-3 text-right pr-2">
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        onViewScan(scan);
                      }}
                      className="px-2.5 py-1 rounded-lg bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-300 font-semibold text-xs transition-all border border-emerald-500/30 flex items-center gap-1 ml-auto"
                    >
                      <Eye className="w-3.5 h-3.5" />
                      <span>Review</span>
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
