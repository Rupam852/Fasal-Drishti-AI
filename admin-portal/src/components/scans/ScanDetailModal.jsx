import React, { useState } from 'react';
import { 
  X, 
  CheckCircle2, 
  AlertTriangle, 
  Send, 
  FileText, 
  User, 
  MapPin, 
  Calendar, 
  ShieldCheck, 
  Sparkles,
  Edit3,
  Save,
  Check
} from 'lucide-react';
import confetti from 'canvas-confetti';

export default function ScanDetailModal({ scan, onClose, onSaveScanUpdate }) {
  if (!scan) return null;

  const [isEditing, setIsEditing] = useState(false);
  const [diseaseName, setDiseaseName] = useState(scan.diseaseName);
  const [severity, setSeverity] = useState(scan.severity || 'Moderate');
  const [treatment, setTreatment] = useState(scan.treatment || '');
  const [symptoms, setSymptoms] = useState(scan.symptoms || '');
  const [isVerified, setIsVerified] = useState(scan.verified || false);
  const [savedSuccess, setSavedSuccess] = useState(false);

  const handleVerifyAndSend = () => {
    setIsVerified(true);
    setSavedSuccess(true);
    
    // Trigger festive verification confetti
    confetti({
      particleCount: 80,
      spread: 60,
      origin: { y: 0.6 }
    });

    if (onSaveScanUpdate) {
      onSaveScanUpdate({
        ...scan,
        diseaseName,
        severity,
        treatment,
        symptoms,
        verified: true,
        status: 'Verified by Chief Agronomist'
      });
    }

    setTimeout(() => setSavedSuccess(false), 3000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-in fade-in">
      <div className="relative w-full max-w-4xl max-h-[90vh] glass-panel border border-emerald-500/30 rounded-3xl overflow-hidden flex flex-col shadow-2xl bg-slate-950/95 text-left">
        {/* Modal Top Header */}
        <div className="p-5 border-b border-white/10 flex items-center justify-between bg-slate-900/80">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-xl bg-emerald-500/20 text-emerald-400">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-base font-bold text-white">Diagnostic Scan Inspection & Verification</h3>
                <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-white/5">
                  ID: {scan.id}
                </span>
              </div>
              <p className="text-xs text-slate-400">National Crop Pathology Repository</p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white transition-all"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-6 overflow-y-auto space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-12 gap-6">
            {/* Left Image & Farmer Info */}
            <div className="md:col-span-5 space-y-4">
              <div className="relative w-full aspect-square rounded-2xl overflow-hidden border border-white/10 bg-slate-900 shadow-xl group">
                <img
                  src={scan.imageUrl}
                  alt={scan.cropName}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                  onError={(e) => {
                    e.target.src = 'https://images.unsplash.com/photo-1592841200221-a6898f307baa?auto=format&fit=crop&w=600&q=80';
                  }}
                />
                <div className="absolute bottom-3 left-3 right-3 p-2.5 rounded-xl glass-panel border border-white/20 text-xs flex items-center justify-between">
                  <span className="font-bold text-white font-mono">
                    AI Match: {Math.round((scan.confidence || 0.95) * 100)}%
                  </span>
                  <span className="text-emerald-300 font-semibold">{scan.cropName} Leaf</span>
                </div>
              </div>

              {/* Farmer Metadata Card */}
              <div className="p-4 rounded-2xl bg-slate-900/80 border border-white/5 space-y-2 text-xs">
                <div className="flex items-center gap-2 text-slate-300">
                  <User className="w-4 h-4 text-emerald-400" />
                  <span className="font-bold text-white">{scan.farmerName || 'Farmer'}</span>
                  <span className="text-[11px] text-slate-400">({scan.farmerPhone || 'Verified'})</span>
                </div>
                <div className="flex items-center gap-2 text-slate-400">
                  <MapPin className="w-4 h-4 text-slate-500" />
                  <span>{scan.location || 'India'}</span>
                </div>
                <div className="flex items-center gap-2 text-slate-400">
                  <Calendar className="w-4 h-4 text-slate-500" />
                  <span>Submitted: {scan.createdAt || 'Recent'}</span>
                </div>
              </div>
            </div>

            {/* Right Pathology Diagnosis & Agronomist Prescription Editor */}
            <div className="md:col-span-7 space-y-4">
              {/* Diagnosis Header Card */}
              <div className="p-4 rounded-2xl bg-slate-900/80 border border-emerald-500/20 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-[11px] font-bold text-emerald-400 uppercase tracking-wider flex items-center gap-1.5">
                    <Sparkles className="w-3.5 h-3.5" />
                    AI Pathological Classification
                  </span>
                  <button
                    onClick={() => setIsEditing(!isEditing)}
                    className="text-xs text-slate-400 hover:text-white flex items-center gap-1 font-semibold transition-colors"
                  >
                    <Edit3 className="w-3.5 h-3.5" />
                    <span>{isEditing ? 'Done Editing' : 'Override Diagnosis'}</span>
                  </button>
                </div>

                {isEditing ? (
                  <div className="space-y-3 pt-1">
                    <div>
                      <label className="text-[11px] text-slate-400 font-semibold">Disease Name</label>
                      <input
                        type="text"
                        value={diseaseName}
                        onChange={(e) => setDiseaseName(e.target.value)}
                        className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                      />
                    </div>
                    <div>
                      <label className="text-[11px] text-slate-400 font-semibold">Severity Assessment</label>
                      <select
                        value={severity}
                        onChange={(e) => setSeverity(e.target.value)}
                        className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                      >
                        <option value="Healthy">Healthy (No Disease)</option>
                        <option value="Mild">Mild Infection</option>
                        <option value="Moderate">Moderate Infection</option>
                        <option value="High">High Severity</option>
                        <option value="Critical">Critical Emergency</option>
                      </select>
                    </div>
                  </div>
                ) : (
                  <div>
                    <h3 className="text-xl font-black text-white">{diseaseName}</h3>
                    <div className="flex items-center gap-2 mt-2">
                      <span className="text-xs px-2.5 py-0.5 rounded-full font-bold uppercase bg-amber-500/20 text-amber-300 border border-amber-500/30">
                        {severity} Severity
                      </span>
                      <span className="text-xs px-2.5 py-0.5 rounded-full font-bold bg-slate-800 text-slate-300">
                        Confidence: {Math.round((scan.confidence || 0.95) * 100)}%
                      </span>
                    </div>
                  </div>
                )}
              </div>

              {/* Symptoms Field */}
              <div className="p-4 rounded-2xl bg-slate-900/60 border border-white/5 space-y-1.5 text-xs">
                <span className="font-bold text-slate-300 uppercase tracking-wider text-[10px]">
                  Observed Foliage Symptoms
                </span>
                {isEditing ? (
                  <textarea
                    rows={2}
                    value={symptoms}
                    onChange={(e) => setSymptoms(e.target.value)}
                    className="w-full p-2.5 bg-slate-950 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:border-emerald-500"
                  />
                ) : (
                  <p className="text-slate-300 leading-relaxed font-medium">
                    {symptoms || 'Concentric dark rings and chlorotic yellowing observed across foliage.'}
                  </p>
                )}
              </div>

              {/* Agronomic Treatment Prescription */}
              <div className="p-4 rounded-2xl bg-slate-900/60 border border-white/5 space-y-1.5 text-xs">
                <span className="font-bold text-emerald-400 uppercase tracking-wider text-[10px]">
                  Prescribed Chemical & Bio-Remedy Protocol
                </span>
                {isEditing ? (
                  <textarea
                    rows={3}
                    value={treatment}
                    onChange={(e) => setTreatment(e.target.value)}
                    className="w-full p-2.5 bg-slate-950 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:border-emerald-500"
                  />
                ) : (
                  <p className="text-slate-200 leading-relaxed font-medium">
                    {treatment || 'Spray Mancozeb 75% WP @ 2.5g per liter water. Repeat after 7 days if infection persists.'}
                  </p>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Modal Bottom Footer */}
        <div className="p-4 border-t border-white/10 bg-slate-900/80 flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            {isVerified ? (
              <span className="text-xs text-emerald-400 font-bold flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-emerald-500/10 border border-emerald-500/30">
                <CheckCircle2 className="w-4 h-4" />
                Verified & Synced with Farmer App
              </span>
            ) : (
              <span className="text-xs text-amber-400 font-bold flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-amber-500/10 border border-amber-500/30">
                <AlertTriangle className="w-4 h-4" />
                Awaiting Agronomist Sign-Off
              </span>
            )}
          </div>

          <div className="flex items-center gap-2.5 ml-auto">
            <button
              onClick={onClose}
              className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold text-xs transition-all"
            >
              Close
            </button>

            <button
              onClick={handleVerifyAndSend}
              className="px-5 py-2 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white font-bold text-xs shadow-lg shadow-emerald-950/40 transition-all flex items-center gap-2 cursor-pointer"
            >
              {savedSuccess ? <Check className="w-4 h-4" /> : <Send className="w-4 h-4" />}
              <span>{savedSuccess ? 'Prescription Sent!' : 'Approve & Push to Farmer'}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
