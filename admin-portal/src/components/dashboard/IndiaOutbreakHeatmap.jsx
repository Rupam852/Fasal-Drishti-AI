import React, { useState } from 'react';
import { 
  MapPin, 
  AlertTriangle, 
  ShieldAlert, 
  Send, 
  Eye, 
  Sparkles, 
  ChevronRight,
  Info,
  Layers
} from 'lucide-react';

export default function IndiaOutbreakHeatmap({ diseaseInfo = [], onBroadcastZone }) {
  const [selectedDisease, setSelectedDisease] = useState(null);

  // Use real disease info records from Supabase
  const activeRecords = diseaseInfo.length > 0 ? diseaseInfo : [
    {
      class_id: 'Tomato___Late_blight',
      crop_name: 'Tomato',
      crop_hindi: 'टमाटर',
      disease_name: 'Late Blight',
      disease_hindi: 'पछेती झुलसा (Late Blight)',
      severity: 'Severe',
      symptoms: 'Dark brown water-soaked lesions on leaves and stems, white fungal growth underneath leaf in humid weather.',
      treatment: 'Apply fungicides like Mancozeb (2.5g/L) or Metalaxyl + Mancozeb (Ridomil MZ @ 2g/L).',
      prevention: 'Ensure good air circulation, avoid overhead sprinkler irrigation.'
    }
  ];

  const currentSelection = selectedDisease || activeRecords[0];

  return (
    <div className="glass-panel p-5 lg:p-6 rounded-2xl border border-white/10 shadow-xl text-left mb-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6">
        <div>
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-emerald-500/20 text-emerald-400">
              <ShieldAlert className="w-5 h-5" />
            </div>
            <h3 className="text-lg font-bold text-white flex items-center gap-2">
              National Crop Pathology Knowledge Radar
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 font-mono font-bold border border-emerald-500/30">
                SUPABASE LIVE DATABASE
              </span>
            </h3>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Pathological disease vectors and treatment protocols active in mobile AI classifier
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Zones Cards List */}
        <div className="lg:col-span-7 space-y-3 max-h-[440px] overflow-y-auto pr-1">
          {activeRecords.map((item) => {
            const isSelected = (currentSelection?.class_id === item.class_id) || (currentSelection?.id === item.id);
            const isSevere = item.severity?.toLowerCase() === 'severe' || item.severity?.toLowerCase() === 'critical';

            return (
              <div
                key={item.class_id || item.id}
                onClick={() => setSelectedDisease(item)}
                className={`p-4 rounded-xl border transition-all cursor-pointer ${
                  isSelected
                    ? 'bg-slate-900/90 border-emerald-500/80 shadow-lg shadow-emerald-950/30 ring-1 ring-emerald-500/40'
                    : 'bg-slate-900/40 border-white/5 hover:bg-slate-900/70 hover:border-white/10'
                }`}
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-start gap-3">
                    <div className={`p-2 rounded-xl shrink-0 mt-0.5 ${
                      isSevere ? 'bg-red-500/20 text-red-400' : 'bg-emerald-500/20 text-emerald-400'
                    }`}>
                      <MapPin className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="text-sm font-bold text-white">{item.crop_name} {item.crop_hindi ? `(${item.crop_hindi})` : ''}</span>
                      </div>
                      <p className="text-xs font-semibold text-slate-200 mt-0.5">
                        <span className="text-emerald-400 font-bold">{item.disease_name}</span> {item.disease_hindi ? `• ${item.disease_hindi}` : ''}
                      </p>
                    </div>
                  </div>

                  <div className="text-right shrink-0">
                    <span className={`inline-block text-[10px] px-2.5 py-0.5 rounded-full font-bold uppercase tracking-wider border ${
                      isSevere
                        ? 'bg-red-500/20 text-red-300 border-red-500/40'
                        : 'bg-amber-500/20 text-amber-300 border-amber-500/40'
                    }`}>
                      {item.severity || 'Moderate'}
                    </span>
                    <p className="text-[10px] text-slate-500 font-mono mt-1">{item.class_id}</p>
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        {/* Right Deep Detail & Action Box */}
        <div className="lg:col-span-5 glass-panel p-5 rounded-2xl border border-emerald-500/30 bg-slate-950/70">
          <div className="flex items-center justify-between pb-3 border-b border-white/10">
            <span className="text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
              <Sparkles className="w-3.5 h-3.5 text-emerald-400" />
              Agronomist Clinical Protocol
            </span>
            <span className="text-[11px] font-mono text-emerald-400 font-semibold">{currentSelection?.crop_name}</span>
          </div>

          <div className="mt-4 space-y-3.5 text-xs">
            <div>
              <p className="text-[11px] text-slate-400 uppercase font-semibold">Identified Pathogen / Condition</p>
              <h4 className="text-base font-extrabold text-white mt-0.5">{currentSelection?.disease_name}</h4>
              <p className="text-xs text-slate-300 font-medium mt-0.5">{currentSelection?.disease_hindi || ''}</p>
            </div>

            <div className="p-3 rounded-xl bg-slate-900/80 border border-white/10 space-y-1">
              <p className="text-[11px] text-amber-300 font-bold flex items-center gap-1">
                <AlertTriangle className="w-3.5 h-3.5" />
                Symptoms & Pathological Identification:
              </p>
              <p className="text-xs text-slate-200 leading-relaxed font-medium">
                {currentSelection?.symptoms || 'Visual leaf damage and discoloration.'}
              </p>
            </div>

            <div className="p-3 rounded-xl bg-slate-900/80 border border-emerald-500/20 space-y-1">
              <p className="text-[11px] text-emerald-400 font-bold flex items-center gap-1">
                <Sparkles className="w-3.5 h-3.5" />
                Standard Chemical & Bio-Remedy:
              </p>
              <p className="text-xs text-slate-200 leading-relaxed font-medium">
                {currentSelection?.treatment || 'Spray recommended fungicide or organic bio-agents.'}
              </p>
            </div>

            <button
              onClick={() => onBroadcastZone && onBroadcastZone(currentSelection)}
              className="w-full flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white font-bold text-xs shadow-lg shadow-emerald-950/40 transition-all cursor-pointer"
            >
              <Send className="w-3.5 h-3.5" />
              <span>Broadcast Advisory on {currentSelection?.crop_name} to Farmers</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
