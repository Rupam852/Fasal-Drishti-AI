import React, { useState } from 'react';
import { 
  MapPin, 
  AlertTriangle, 
  ShieldAlert, 
  Send, 
  Eye, 
  Sparkles, 
  ChevronRight,
  Info
} from 'lucide-react';

export default function IndiaOutbreakHeatmap({ zones, onBroadcastZone }) {
  const [selectedZone, setSelectedZone] = useState(zones[0]);

  return (
    <div className="glass-panel p-5 lg:p-6 rounded-2xl border border-white/10 shadow-xl text-left mb-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6">
        <div>
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-red-500/20 text-red-400">
              <ShieldAlert className="w-5 h-5" />
            </div>
            <h3 className="text-lg font-bold text-white flex items-center gap-2">
              National Crop Disease Outbreak Radar
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-red-500/20 text-red-300 font-mono font-bold border border-red-500/30 animate-pulse">
                LIVE SATELLITE & SCAN TELEMETRY
              </span>
            </h3>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Realtime epidemiological disease clusters detected from farmer image submissions across India
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Zones Cards List */}
        <div className="lg:col-span-7 space-y-3">
          {zones.map((zone) => {
            const isSelected = selectedZone?.id === zone.id;
            const isCritical = zone.riskLevel === 'Critical';

            return (
              <div
                key={zone.id}
                onClick={() => setSelectedZone(zone)}
                className={`p-4 rounded-xl border transition-all cursor-pointer ${
                  isSelected
                    ? 'bg-slate-900/90 border-emerald-500/80 shadow-lg shadow-emerald-950/30 ring-1 ring-emerald-500/40'
                    : 'bg-slate-900/40 border-white/5 hover:bg-slate-900/70 hover:border-white/10'
                }`}
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-start gap-3">
                    <div className={`p-2 rounded-xl shrink-0 mt-0.5 ${
                      isCritical ? 'bg-red-500/20 text-red-400' : 'bg-amber-500/20 text-amber-400'
                    }`}>
                      <MapPin className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="text-sm font-bold text-white">{zone.state}</span>
                        <span className="text-xs text-slate-400 font-medium">({zone.region})</span>
                      </div>
                      <p className="text-xs font-semibold text-slate-200 mt-0.5">
                        <span className="text-emerald-400">{zone.crop}:</span> {zone.disease}
                      </p>
                    </div>
                  </div>

                  <div className="text-right shrink-0">
                    <span className={`inline-block text-[10px] px-2.5 py-0.5 rounded-full font-bold uppercase tracking-wider border ${
                      isCritical
                        ? 'bg-red-500/20 text-red-300 border-red-500/40'
                        : 'bg-amber-500/20 text-amber-300 border-amber-500/40'
                    }`}>
                      {zone.riskLevel}
                    </span>
                    <p className="text-[11px] text-slate-400 font-mono mt-1">{zone.affectedFarms}</p>
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        {/* Right Zone Deep Detail & Action Box */}
        <div className="lg:col-span-5 glass-panel p-5 rounded-2xl border border-emerald-500/30 bg-slate-950/70">
          <div className="flex items-center justify-between pb-3 border-b border-white/10">
            <span className="text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
              <Sparkles className="w-3.5 h-3.5 text-emerald-400" />
              Agronomist Action Protocol
            </span>
            <span className="text-[11px] font-mono text-emerald-400 font-semibold">{selectedZone?.state}</span>
          </div>

          <div className="mt-4 space-y-3.5">
            <div>
              <p className="text-[11px] text-slate-400 uppercase font-semibold">Active Threat Vector</p>
              <h4 className="text-base font-extrabold text-white mt-0.5">{selectedZone?.disease}</h4>
              <p className="text-xs text-slate-300 font-medium mt-0.5">Crop: {selectedZone?.crop}</p>
            </div>

            <div className="p-3 rounded-xl bg-slate-900/80 border border-white/10">
              <p className="text-[11px] text-amber-300 font-bold flex items-center gap-1">
                <AlertTriangle className="w-3.5 h-3.5" />
                Prescribed Field Treatment:
              </p>
              <p className="text-xs text-slate-200 mt-1 leading-relaxed font-medium">
                {selectedZone?.recommendation}
              </p>
            </div>

            <div className="grid grid-cols-2 gap-2 text-xs pt-2">
              <div className="p-2.5 rounded-xl bg-slate-900/50 border border-white/5">
                <p className="text-[10px] text-slate-400">Estimated Outbreak Area</p>
                <p className="text-sm font-bold text-white font-mono mt-0.5">{selectedZone?.affectedFarms}</p>
              </div>
              <div className="p-2.5 rounded-xl bg-slate-900/50 border border-white/5">
                <p className="text-[10px] text-slate-400">Risk Assessment</p>
                <p className="text-sm font-bold text-red-400 font-mono mt-0.5">{selectedZone?.riskLevel} Priority</p>
              </div>
            </div>

            <button
              onClick={() => onBroadcastZone && onBroadcastZone(selectedZone)}
              className="w-full flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white font-bold text-xs shadow-lg shadow-emerald-950/40 transition-all cursor-pointer"
            >
              <Send className="w-3.5 h-3.5" />
              <span>Broadcast Instant Push Alert to {selectedZone?.state} Farmers</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
