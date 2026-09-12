import React, { useState } from 'react';
import { 
  Megaphone, 
  Send, 
  AlertTriangle, 
  CheckCircle2, 
  MapPin, 
  Clock, 
  Users,
  Sparkles,
  Layers,
  Radio
} from 'lucide-react';
import confetti from 'canvas-confetti';

export default function BroadcastManager({ broadcasts, onSendBroadcast }) {
  const [broadcastList, setBroadcastList] = useState(broadcasts);
  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');
  const [targetRegion, setTargetRegion] = useState('All India');
  const [severity, setSeverity] = useState('High Alert');
  const [isSending, setIsSending] = useState(false);
  const [sentSuccess, setSentSuccess] = useState(false);

  const regionOptions = [
    'All India',
    'Punjab & Haryana',
    'Uttar Pradesh & Bihar',
    'Maharashtra & Gujarat',
    'West Bengal & North-East',
    'Andhra Pradesh & Telangana',
    'Madhya Pradesh & Rajasthan',
    'Tamil Nadu & Karnataka'
  ];

  const handleSend = (e) => {
    e.preventDefault();
    if (!title || !message) return;

    setIsSending(true);
    setTimeout(() => {
      const newBroadcast = {
        id: `bc-${Date.now()}`,
        title,
        message,
        targetRegion,
        severity,
        sentAt: 'Just now',
        recipientsCount: targetRegion === 'All India' ? 14850 : 3400,
        status: 'Delivered'
      };

      setBroadcastList([newBroadcast, ...broadcastList]);
      if (onSendBroadcast) onSendBroadcast(newBroadcast);

      confetti({ particleCount: 60, spread: 50, origin: { y: 0.5 } });
      setIsSending(false);
      setSentSuccess(true);
      setTitle('');
      setMessage('');
      setTimeout(() => setSentSuccess(false), 3000);
    }, 800);
  };

  return (
    <div className="space-y-6 text-left">
      {/* Top Header */}
      <div className="glass-panel p-5 lg:p-6 rounded-2xl border border-white/10 shadow-xl">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div>
            <h2 className="text-xl font-black text-white flex items-center gap-2.5">
              <Megaphone className="w-5 h-5 text-emerald-400" />
              Emergency Agricultural Advisory & Push Broadcaster
            </h2>
            <p className="text-xs text-slate-400 mt-1">
              Broadcast critical weather warnings, pest outbreak alerts, and government schemes directly to farmers' devices
            </p>
          </div>

          <div className="flex items-center gap-2">
            <span className="text-xs px-3 py-1.5 rounded-xl bg-emerald-500/20 text-emerald-300 font-bold border border-emerald-500/30 font-mono flex items-center gap-1.5">
              <Radio className="w-3.5 h-3.5 text-emerald-400 animate-pulse" />
              Broadcaster Live
            </span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Dispatch Form */}
        <div className="lg:col-span-6 glass-panel p-6 rounded-2xl border border-white/10 shadow-xl">
          <h3 className="text-sm font-bold text-white mb-4 flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-emerald-400" />
            Compose Emergency Alert
          </h3>

          {sentSuccess && (
            <div className="p-3.5 rounded-xl bg-emerald-500/20 border border-emerald-500/40 text-emerald-300 text-xs font-semibold flex items-center gap-2 mb-4">
              <CheckCircle2 className="w-4 h-4" />
              <span>Broadcast dispatched successfully to active farmer devices!</span>
            </div>
          )}

          <form onSubmit={handleSend} className="space-y-4 text-xs">
            <div>
              <label className="text-slate-300 font-bold">Alert Title</label>
              <input
                type="text"
                required
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g. ⚠️ Severe Hailstorm Alert for Potato & Wheat"
                className="w-full mt-1.5 p-3 bg-slate-900 border border-slate-700 rounded-xl text-white text-xs focus:border-emerald-500"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-slate-300 font-bold">Target Region</label>
                <select
                  value={targetRegion}
                  onChange={(e) => setTargetRegion(e.target.value)}
                  className="w-full mt-1.5 p-2.5 bg-slate-900 border border-slate-700 rounded-xl text-white text-xs focus:border-emerald-500 font-medium"
                >
                  {regionOptions.map((r) => (
                    <option key={r} value={r}>{r}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="text-slate-300 font-bold">Priority Severity</label>
                <select
                  value={severity}
                  onChange={(e) => setSeverity(e.target.value)}
                  className="w-full mt-1.5 p-2.5 bg-slate-900 border border-slate-700 rounded-xl text-white text-xs focus:border-emerald-500 font-medium"
                >
                  <option value="High Alert">High Alert (Emergency Sound)</option>
                  <option value="Warning">Warning Advisory</option>
                  <option value="Info">Information / Scheme</option>
                </select>
              </div>
            </div>

            <div>
              <label className="text-slate-300 font-bold">Advisory Message Content</label>
              <textarea
                rows={4}
                required
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder="Write specific chemical/biological advice, precautions, or helpline numbers..."
                className="w-full mt-1.5 p-3 bg-slate-900 border border-slate-700 rounded-xl text-white text-xs leading-relaxed focus:border-emerald-500"
              />
            </div>

            <button
              type="submit"
              disabled={isSending}
              className="w-full py-3 px-4 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white font-bold text-xs shadow-lg shadow-emerald-950/40 transition-all flex items-center justify-center gap-2 cursor-pointer disabled:opacity-50"
            >
              <Send className="w-4 h-4" />
              <span>{isSending ? 'Transmitting Broadcast...' : 'Dispatch Broadcast Alert Now'}</span>
            </button>
          </form>
        </div>

        {/* Right Broadcast History */}
        <div className="lg:col-span-6 glass-panel p-6 rounded-2xl border border-white/10 shadow-xl space-y-4">
          <h3 className="text-sm font-bold text-white flex items-center justify-between">
            <span className="flex items-center gap-2">
              <Layers className="w-4 h-4 text-emerald-400" />
              Recent Dispatched Broadcasts
            </span>
            <span className="text-[11px] text-slate-400 font-mono">{broadcastList.length} Sent</span>
          </h3>

          <div className="space-y-3 max-h-[420px] overflow-y-auto pr-1">
            {broadcastList.map((bc) => {
              const isHigh = bc.severity === 'High Alert';

              return (
                <div key={bc.id} className="p-4 rounded-xl bg-slate-900/80 border border-white/5 space-y-2 text-xs">
                  <div className="flex items-start justify-between gap-2">
                    <h4 className="font-bold text-white text-xs">{bc.title}</h4>
                    <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold uppercase shrink-0 border ${
                      isHigh ? 'bg-red-500/20 text-red-300 border-red-500/30' : 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30'
                    }`}>
                      {bc.severity}
                    </span>
                  </div>

                  <p className="text-slate-300 leading-relaxed font-medium">{bc.message}</p>

                  <div className="pt-2 border-t border-white/5 flex items-center justify-between text-[11px] text-slate-400">
                    <span className="flex items-center gap-1 font-semibold text-emerald-400">
                      <MapPin className="w-3 h-3" />
                      {bc.targetRegion}
                    </span>
                    <span className="flex items-center gap-1 font-mono">
                      <Users className="w-3 h-3" />
                      {bc.recipientsCount?.toLocaleString()} Farmers
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
