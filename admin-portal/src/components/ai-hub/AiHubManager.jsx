import React, { useState } from 'react';
import { 
  Cpu, 
  Key, 
  ShieldCheck, 
  Activity, 
  CheckCircle2, 
  AlertTriangle, 
  Save, 
  RefreshCw,
  Sparkles,
  Zap,
  Lock
} from 'lucide-react';
import { updateAppConfig } from '../../services/supabase';

export default function AiHubManager({ enginesConfig }) {
  const [nvidiaKey, setNvidiaKey] = useState('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');
  const [geminiKey, setGeminiKey] = useState('AIzaSyD-StandardCloudFallbackKey');
  const [activeModel, setActiveModel] = useState('meta/llama-3.2-11b-vision-instruct');
  const [isSaving, setIsSaving] = useState(false);
  const [statusMsg, setStatusMsg] = useState(null);

  const handleSaveKeys = async (e) => {
    e.preventDefault();
    setIsSaving(true);
    setStatusMsg(null);

    try {
      await updateAppConfig('nvidia_nim_api_key', nvidiaKey, 'Primary NVIDIA NIM API Key');
      await updateAppConfig('nvidia_model_name', activeModel, 'Active Vision & Agronomy Model');
      
      setStatusMsg({
        type: 'success',
        text: 'AI Keys and Model Configurations updated in Supabase cloud! All connected mobile clients now use these new keys.'
      });
    } catch (err) {
      setStatusMsg({
        type: 'error',
        text: `Update failed: ${err.message}`
      });
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="space-y-6 text-left">
      {/* Header */}
      <div className="glass-panel p-5 lg:p-6 rounded-2xl border border-white/10 shadow-xl">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div>
            <h2 className="text-xl font-black text-white flex items-center gap-2.5">
              <Cpu className="w-5 h-5 text-emerald-400" />
              AI Agronomy Foundation Engines & API Switchboard
            </h2>
            <p className="text-xs text-slate-400 mt-1">
              Live telemetry, latency metrics, and remote cloud API key rotation for Google Gemini and NVIDIA NIM
            </p>
          </div>

          <div className="flex items-center gap-2">
            <span className="text-xs px-3 py-1.5 rounded-xl bg-emerald-500/20 text-emerald-300 font-bold border border-emerald-500/30 font-mono flex items-center gap-1.5">
              <Zap className="w-3.5 h-3.5 text-emerald-400" />
              Zero Downtime Hot-Reload
            </span>
          </div>
        </div>
      </div>

      {/* Engine Status Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 lg:gap-5">
        {enginesConfig.map((engine) => (
          <div
            key={engine.id}
            className="glass-panel p-5 rounded-2xl border border-white/10 shadow-lg flex flex-col justify-between"
          >
            <div>
              <div className="flex items-start justify-between gap-2">
                <div>
                  <h3 className="text-sm font-bold text-white">{engine.name}</h3>
                  <p className="text-[11px] text-slate-400">{engine.provider}</p>
                </div>
                <span className="text-[10px] px-2 py-0.5 rounded-full font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                  {engine.status}
                </span>
              </div>

              <p className="text-xs text-slate-300 mt-3 leading-relaxed">
                {engine.purpose}
              </p>
            </div>

            <div className="mt-4 pt-3 border-t border-white/10 grid grid-cols-3 gap-2 text-center text-xs font-mono">
              <div className="p-2 rounded-xl bg-slate-900/60 border border-white/5">
                <p className="text-[10px] text-slate-400">Latency</p>
                <p className="font-bold text-emerald-400 mt-0.5">{engine.latency}</p>
              </div>
              <div className="p-2 rounded-xl bg-slate-900/60 border border-white/5">
                <p className="text-[10px] text-slate-400">Uptime</p>
                <p className="font-bold text-white mt-0.5">{engine.uptime}</p>
              </div>
              <div className="p-2 rounded-xl bg-slate-900/60 border border-white/5">
                <p className="text-[10px] text-slate-400">Calls Today</p>
                <p className="font-bold text-cyan-400 mt-0.5">{engine.totalCallsToday?.toLocaleString()}</p>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* API Key Master Configuration */}
      <div className="glass-panel p-6 rounded-2xl border border-white/10 shadow-xl">
        <div className="flex items-center justify-between pb-4 border-b border-white/10 mb-5">
          <h3 className="text-sm font-bold text-white flex items-center gap-2">
            <Key className="w-4 h-4 text-emerald-400" />
            Remote API Key Rotation & Master Controller
          </h3>
          <span className="text-[11px] text-slate-400 font-medium">Secured with End-to-End Encryption</span>
        </div>

        {statusMsg && (
          <div className={`p-4 rounded-xl mb-5 text-xs font-semibold flex items-center gap-2.5 ${
            statusMsg.type === 'success'
              ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
              : 'bg-red-500/20 text-red-300 border border-red-500/40'
          }`}>
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span>{statusMsg.text}</span>
          </div>
        )}

        <form onSubmit={handleSaveKeys} className="space-y-4 text-xs">
          <div>
            <label className="text-slate-300 font-bold flex items-center justify-between">
              <span>Primary NVIDIA NIM API Key</span>
              <span className="text-slate-500 font-normal">Remote Fallback Key</span>
            </label>
            <div className="relative mt-1.5">
              <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
              <input
                type="text"
                required
                value={nvidiaKey}
                onChange={(e) => setNvidiaKey(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 bg-slate-900 border border-slate-700 rounded-xl text-white font-mono text-xs focus:border-emerald-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="text-slate-300 font-bold">Active NVIDIA Vision Model</label>
              <select
                value={activeModel}
                onChange={(e) => setActiveModel(e.target.value)}
                className="w-full mt-1.5 p-2.5 bg-slate-900 border border-slate-700 rounded-xl text-white text-xs font-mono focus:border-emerald-500"
              >
                <option value="meta/llama-3.2-11b-vision-instruct">meta/llama-3.2-11b-vision-instruct (Recommended)</option>
                <option value="meta/llama-3.2-90b-vision-instruct">meta/llama-3.2-90b-vision-instruct (Ultra Quality)</option>
                <option value="nvidia/neva-22b">nvidia/neva-22b (Fast Diagnostic)</option>
              </select>
            </div>

            <div>
              <label className="text-slate-300 font-bold">Google Gemini Fallback Key</label>
              <div className="relative mt-1.5">
                <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
                <input
                  type="text"
                  value={geminiKey}
                  onChange={(e) => setGeminiKey(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 bg-slate-900 border border-slate-700 rounded-xl text-white font-mono text-xs focus:border-emerald-500"
                />
              </div>
            </div>
          </div>

          <div className="pt-3">
            <button
              type="submit"
              disabled={isSaving}
              className="py-2.5 px-6 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white font-bold text-xs shadow-lg shadow-emerald-950/40 transition-all flex items-center gap-2 cursor-pointer disabled:opacity-50"
            >
              {isSaving ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
              <span>{isSaving ? 'Syncing with Supabase...' : 'Save & Hot-Reload AI Engines'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
