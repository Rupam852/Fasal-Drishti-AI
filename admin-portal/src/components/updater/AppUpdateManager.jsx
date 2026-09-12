import React, { useState, useEffect } from 'react';
import { 
  DownloadCloud, 
  Upload, 
  CheckCircle2, 
  AlertTriangle, 
  FileText, 
  Send, 
  ShieldCheck,
  RefreshCw,
  Sparkles,
  Layers,
  ArrowUpRight
} from 'lucide-react';
import { updateAppConfig } from '../../services/supabase';

export default function AppUpdateManager({ currentAppVersion = '1.2.0', downloadUrl = '', onUpdateSuccess }) {
  const [versionName, setVersionName] = useState(currentAppVersion);
  const [versionCode, setVersionCode] = useState(12);
  const [apkUrl, setApkUrl] = useState(downloadUrl || 'https://github.com/Rupam852/Fasal-Drishti-AI/releases/latest/download/app-release.apk');
  const [isForceUpdate, setIsForceUpdate] = useState(false);
  const [minSupportedVersion, setMinSupportedVersion] = useState('1.0.0');
  const [releaseNotes, setReleaseNotes] = useState(`• 📸 Permanent On-Device Chat Photo Storage (No blank/green image issues)
• 🧠 Smart Multi-Turn Agronomic Conversational Memory
• 🌾 5-Section In-Depth Diagnostic Dossiers (Dosages per L / 15L tank)
• 🧹 Complete AI Chat History & Storage Cleanup in Profile
• 🎨 Default Light Mode Migration & User Theme Preference Retention`);

  const [isPublishing, setIsPublishing] = useState(false);
  const [publishStatus, setPublishStatus] = useState(null);

  const handlePublishUpdate = async (e) => {
    e.preventDefault();
    setIsPublishing(true);
    setPublishStatus(null);

    try {
      // 1. Update version in Supabase app_config
      await updateAppConfig('latest_app_version', versionName, 'Latest released app version');
      await updateAppConfig('latest_version_code', versionCode.toString(), 'Latest Android version code');
      await updateAppConfig('app_download_url', apkUrl, 'Direct APK download link');
      await updateAppConfig('app_release_notes', releaseNotes, 'Latest version changelog');
      await updateAppConfig('is_force_update', isForceUpdate.toString(), 'Mandatory force update flag');

      setPublishStatus({
        type: 'success',
        message: `Version ${versionName} (Build ${versionCode}) published successfully! Broadcast signal sent to all farmer apps.`
      });

      if (onUpdateSuccess) {
        onUpdateSuccess({
          version: versionName,
          code: versionCode,
          url: apkUrl,
          force: isForceUpdate
        });
      }
    } catch (err) {
      setPublishStatus({
        type: 'error',
        message: `Publish failed: ${err.message}`
      });
    } finally {
      setIsPublishing(false);
    }
  };

  return (
    <div className="space-y-6 text-left">
      {/* Top Banner */}
      <div className="glass-panel p-5 lg:p-6 rounded-2xl border border-white/10 shadow-xl">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div>
            <h2 className="text-xl font-black text-white flex items-center gap-2.5">
              <DownloadCloud className="w-5 h-5 text-emerald-400" />
              In-App OTA App Update & Release Manager
            </h2>
            <p className="text-xs text-slate-400 mt-1">
              Publish new Android APK releases, control force updates, and push instant notifications to farmer phones
            </p>
          </div>

          <div className="flex items-center gap-2">
            <span className="text-xs px-3 py-1.5 rounded-xl bg-emerald-500/20 text-emerald-300 font-bold border border-emerald-500/30 font-mono">
              Live Production: v{currentAppVersion}
            </span>
          </div>
        </div>
      </div>

      {/* Publish Form Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Form */}
        <div className="lg:col-span-8 glass-panel p-6 rounded-2xl border border-white/10 shadow-xl">
          <div className="flex items-center justify-between pb-4 border-b border-white/10 mb-5">
            <h3 className="text-sm font-bold text-white flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-emerald-400" />
              Release New Production Version
            </h3>
            <span className="text-[11px] text-slate-400 font-medium">Synced with Supabase app_config</span>
          </div>

          {publishStatus && (
            <div className={`p-4 rounded-xl mb-5 text-xs font-semibold flex items-center gap-2.5 ${
              publishStatus.type === 'success'
                ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                : 'bg-red-500/20 text-red-300 border border-red-500/40'
            }`}>
              {publishStatus.type === 'success' ? <CheckCircle2 className="w-4 h-4 shrink-0" /> : <AlertTriangle className="w-4 h-4 shrink-0" />}
              <span>{publishStatus.message}</span>
            </div>
          )}

          <form onSubmit={handlePublishUpdate} className="space-y-4 text-xs">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="text-slate-300 font-bold">New Version Name (e.g. 1.3.0)</label>
                <input
                  type="text"
                  required
                  value={versionName}
                  onChange={(e) => setVersionName(e.target.value)}
                  placeholder="1.3.0"
                  className="w-full mt-1.5 p-3 bg-slate-900 border border-slate-700 rounded-xl text-white font-mono text-sm focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="text-slate-300 font-bold">Android Version Code (e.g. 13)</label>
                <input
                  type="number"
                  required
                  value={versionCode}
                  onChange={(e) => setVersionCode(Number(e.target.value))}
                  placeholder="13"
                  className="w-full mt-1.5 p-3 bg-slate-900 border border-slate-700 rounded-xl text-white font-mono text-sm focus:border-emerald-500"
                />
              </div>
            </div>

            <div>
              <label className="text-slate-300 font-bold">Direct APK Download URL / CDN Path</label>
              <input
                type="url"
                required
                value={apkUrl}
                onChange={(e) => setApkUrl(e.target.value)}
                placeholder="https://github.com/.../app-release.apk"
                className="w-full mt-1.5 p-3 bg-slate-900 border border-slate-700 rounded-xl text-white font-mono text-xs focus:border-emerald-500"
              />
            </div>

            <div>
              <label className="text-slate-300 font-bold">Multilingual Release Notes & Changelog</label>
              <textarea
                rows={5}
                required
                value={releaseNotes}
                onChange={(e) => setReleaseNotes(e.target.value)}
                className="w-full mt-1.5 p-3 bg-slate-900 border border-slate-700 rounded-xl text-slate-200 text-xs leading-relaxed font-mono focus:border-emerald-500"
              />
            </div>

            {/* Toggles */}
            <div className="p-4 rounded-xl bg-slate-900/60 border border-white/5 space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-bold text-white text-xs">Force Update (Mandatory)</p>
                  <p className="text-[11px] text-slate-400">Blocks outdated apps until farmer installs this new build</p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={isForceUpdate}
                    onChange={(e) => setIsForceUpdate(e.target.checked)}
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-slate-800 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-500" />
                </label>
              </div>
            </div>

            <div className="pt-3">
              <button
                type="submit"
                disabled={isPublishing}
                className="w-full py-3.5 px-6 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white font-extrabold text-sm shadow-xl shadow-emerald-950/50 transition-all flex items-center justify-center gap-2 cursor-pointer disabled:opacity-50"
              >
                {isPublishing ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Upload className="w-4 h-4" />}
                <span>{isPublishing ? 'Publishing to Cloud...' : 'Publish Update to All Farmers Now'}</span>
              </button>
            </div>
          </form>
        </div>

        {/* Right Preview Card */}
        <div className="lg:col-span-4 space-y-4">
          <div className="glass-panel p-5 rounded-2xl border border-emerald-500/30 bg-slate-950/70">
            <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-3 flex items-center gap-1.5">
              <Layers className="w-3.5 h-3.5 text-emerald-400" />
              Farmer Mobile Notification Preview
            </h4>

            {/* Mobile Push Notification Mockup */}
            <div className="p-3.5 rounded-2xl bg-slate-900 border border-white/10 shadow-lg text-left space-y-2">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-5 h-5 rounded bg-emerald-500 flex items-center justify-center text-[10px] font-bold text-white">
                    🌾
                  </div>
                  <span className="font-bold text-slate-200 text-xs">Fasal Drishti AI</span>
                </div>
                <span className="text-[10px] text-slate-500">Just now</span>
              </div>

              <p className="text-xs font-bold text-white">
                Fasal Drishti Update Available! 🌾
              </p>
              <p className="text-[11px] text-slate-300 leading-snug">
                Version v{versionName} is ready. Tap to download new features, disease models and speed upgrades.
              </p>

              <div className="pt-1.5 border-t border-white/5 flex justify-end">
                <span className="text-[10px] text-emerald-400 font-bold uppercase tracking-wider flex items-center gap-0.5">
                  Update Now <ArrowUpRight className="w-3 h-3" />
                </span>
              </div>
            </div>

            <div className="mt-4 p-3 rounded-xl bg-slate-900/50 border border-white/5 space-y-1 text-xs">
              <p className="text-slate-400 text-[11px]">OTA Rollout Speed: <span className="text-emerald-400 font-bold">Instant (0s)</span></p>
              <p className="text-slate-400 text-[11px]">Target Audience: <span className="text-white font-bold">All 14,800+ Devices</span></p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
