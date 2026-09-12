import React, { useState } from 'react';
import { 
  Settings, 
  ShieldCheck, 
  Lock, 
  Database, 
  Server, 
  CheckCircle2, 
  Key, 
  RefreshCw,
  Sparkles,
  Layers
} from 'lucide-react';

export default function SettingsManager({ adminSession }) {
  const [dbStatus, setDbStatus] = useState('Connected & Healthy');
  const [syncFreq, setSyncFreq] = useState('Realtime (WebSockets)');

  return (
    <div className="space-y-6 text-left">
      {/* Top Header */}
      <div className="glass-panel p-5 lg:p-6 rounded-2xl border border-white/10 shadow-xl">
        <h2 className="text-xl font-black text-white flex items-center gap-2.5">
          <Settings className="w-5 h-5 text-emerald-400" />
          Admin Security & System Diagnostics
        </h2>
        <p className="text-xs text-slate-400 mt-1">
          Manage system configurations, Supabase cloud connections, and master administrative access
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Profile & Credentials Info */}
        <div className="lg:col-span-6 glass-panel p-6 rounded-2xl border border-white/10 shadow-xl space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-white/10">
            <h3 className="text-sm font-bold text-white flex items-center gap-2">
              <ShieldCheck className="w-4 h-4 text-emerald-400" />
              Active Admin Session
            </h3>
            <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 font-bold border border-emerald-500/30">
              Authenticated
            </span>
          </div>

          <div className="space-y-3 text-xs">
            <div className="p-3.5 rounded-xl bg-slate-900/80 border border-white/5 flex items-center justify-between">
              <div>
                <p className="text-slate-400 text-[11px]">Admin Username</p>
                <p className="font-mono font-bold text-white mt-0.5">{adminSession?.username || 'adminsupport'}</p>
              </div>
              <span className="text-[11px] text-slate-500 font-mono">Protected</span>
            </div>

            <div className="p-3.5 rounded-xl bg-slate-900/80 border border-white/5 flex items-center justify-between">
              <div>
                <p className="text-slate-400 text-[11px]">Assigned Role</p>
                <p className="font-bold text-emerald-400 mt-0.5">{adminSession?.role || 'Super Administrator'}</p>
              </div>
            </div>

            <div className="p-3.5 rounded-xl bg-slate-900/80 border border-white/5 flex items-center justify-between">
              <div>
                <p className="text-slate-400 text-[11px]">Session Login Time</p>
                <p className="font-mono text-slate-300 mt-0.5">{new Date(adminSession?.loggedInAt || Date.now()).toLocaleString()}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Right Cloud & Database Diagnostics */}
        <div className="lg:col-span-6 glass-panel p-6 rounded-2xl border border-white/10 shadow-xl space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-white/10">
            <h3 className="text-sm font-bold text-white flex items-center gap-2">
              <Database className="w-4 h-4 text-emerald-400" />
              Supabase Cloud Telemetry
            </h3>
            <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 font-mono font-bold">
              PostgreSQL 15.6
            </span>
          </div>

          <div className="space-y-3 text-xs">
            <div className="p-3.5 rounded-xl bg-slate-900/80 border border-white/5 flex items-center justify-between">
              <div>
                <p className="text-slate-400 text-[11px]">Database Connection</p>
                <p className="font-bold text-emerald-400 mt-0.5 flex items-center gap-1.5">
                  <CheckCircle2 className="w-3.5 h-3.5" />
                  {dbStatus}
                </p>
              </div>
              <span className="text-[11px] text-slate-400 font-mono">Region: ap-south-1</span>
            </div>

            <div className="p-3.5 rounded-xl bg-slate-900/80 border border-white/5 flex items-center justify-between">
              <div>
                <p className="text-slate-400 text-[11px]">Sync Mode</p>
                <p className="font-bold text-white mt-0.5">{syncFreq}</p>
              </div>
            </div>

            <div className="p-3.5 rounded-xl bg-slate-900/80 border border-white/5 flex items-center justify-between">
              <div>
                <p className="text-slate-400 text-[11px]">Row Level Security (RLS)</p>
                <p className="font-bold text-cyan-400 mt-0.5">Enforced across Scans & Configs</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
