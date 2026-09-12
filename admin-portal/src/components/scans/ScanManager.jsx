import React, { useState } from 'react';
import { 
  ScanLine, 
  Search, 
  Filter, 
  Download, 
  Eye, 
  CheckCircle2, 
  AlertTriangle, 
  MapPin, 
  Clock, 
  ShieldCheck,
  RefreshCw
} from 'lucide-react';
import ScanDetailModal from './ScanDetailModal';

export default function ScanManager({ scans, onSaveScanUpdate }) {
  const [selectedCrop, setSelectedCrop] = useState('All');
  const [selectedSeverity, setSelectedSeverity] = useState('All');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedScan, setSelectedScan] = useState(null);

  const cropsList = ['All', 'Tomato', 'Potato', 'Wheat', 'Rice', 'Chilli'];
  const severitiesList = ['All', 'Healthy', 'Mild', 'Moderate', 'High', 'Critical'];

  const filteredScans = scans.filter((s) => {
    const matchCrop = selectedCrop === 'All' || s.cropName.toLowerCase() === selectedCrop.toLowerCase();
    const matchSeverity = selectedSeverity === 'All' || s.severity.toLowerCase() === selectedSeverity.toLowerCase();
    const matchQuery = !searchQuery || 
      s.farmerName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.diseaseName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.location?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.id?.toLowerCase().includes(searchQuery.toLowerCase());
    return matchCrop && matchSeverity && matchQuery;
  });

  const exportCSV = () => {
    const headers = ['Scan ID', 'Farmer Name', 'Location', 'Crop', 'Disease Detected', 'Confidence', 'Severity', 'Created At'];
    const rows = filteredScans.map(s => [
      s.id,
      `"${s.farmerName || 'Farmer'}"`,
      `"${s.location || 'India'}"`,
      s.cropName,
      `"${s.diseaseName}"`,
      `${Math.round((s.confidence || 0.95) * 100)}%`,
      s.severity,
      s.createdAt
    ]);

    const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map(e => e.join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `fasal_drishti_scans_${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="space-y-6 text-left">
      {/* Header & Controls */}
      <div className="glass-panel p-5 lg:p-6 rounded-2xl border border-white/10 shadow-xl">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div>
            <h2 className="text-xl font-black text-white flex items-center gap-2.5">
              <ScanLine className="w-5 h-5 text-emerald-400" />
              National Crop Disease Scan Inspector
            </h2>
            <p className="text-xs text-slate-400 mt-1">
              Review, verify, and override AI disease predictions submitted by farmers
            </p>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={exportCSV}
              className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-xs font-semibold transition-all flex items-center gap-2"
            >
              <Download className="w-4 h-4 text-emerald-400" />
              <span>Export CSV</span>
            </button>
          </div>
        </div>

        {/* Filters Bar */}
        <div className="mt-5 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-12 gap-3">
          <div className="lg:col-span-5 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search by farmer name, scan ID, disease or district..."
              className="w-full pl-9 pr-4 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-400 focus:outline-none focus:border-emerald-500"
            />
          </div>

          <div className="lg:col-span-3">
            <select
              value={selectedCrop}
              onChange={(e) => setSelectedCrop(e.target.value)}
              className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-emerald-500 font-medium"
            >
              {cropsList.map((c) => (
                <option key={c} value={c}>{c === 'All' ? 'All Crop Categories' : c}</option>
              ))}
            </select>
          </div>

          <div className="lg:col-span-4">
            <select
              value={selectedSeverity}
              onChange={(e) => setSelectedSeverity(e.target.value)}
              className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-emerald-500 font-medium"
            >
              {severitiesList.map((s) => (
                <option key={s} value={s}>{s === 'All' ? 'All Severity Levels' : `${s} Severity`}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Main Scans Table */}
      <div className="glass-panel p-5 rounded-2xl border border-white/10 shadow-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="border-b border-white/10 text-slate-400 font-semibold uppercase text-[10px] tracking-wider bg-slate-900/50">
                <th className="py-3.5 pl-3">Sample Leaf</th>
                <th className="py-3.5">Farmer & Location</th>
                <th className="py-3.5">Crop</th>
                <th className="py-3.5">AI Detected Pathogen</th>
                <th className="py-3.5">Confidence</th>
                <th className="py-3.5">Severity</th>
                <th className="py-3.5">Status</th>
                <th className="py-3.5 text-right pr-3">Inspect</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {filteredScans.length === 0 ? (
                <tr>
                  <td colSpan="8" className="text-center py-8 text-slate-400">
                    No scans matching the selected filter criteria.
                  </td>
                </tr>
              ) : (
                filteredScans.map((scan) => {
                  const isHealthy = scan.severity === 'Healthy' || scan.diseaseName.toLowerCase().includes('healthy');
                  const isCritical = scan.severity === 'Critical';

                  return (
                    <tr 
                      key={scan.id} 
                      className="hover:bg-slate-800/40 transition-colors group cursor-pointer"
                      onClick={() => setSelectedScan(scan)}
                    >
                      <td className="py-3 pl-3">
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
                        <p className="font-bold text-white text-xs">{scan.farmerName || 'Farmer'}</p>
                        <p className="text-slate-400 text-[11px] flex items-center gap-1 mt-0.5">
                          <MapPin className="w-3 h-3 text-slate-500" />
                          {scan.location || 'India'}
                        </p>
                      </td>

                      <td className="py-3">
                        <span className="font-bold text-slate-200">{scan.cropName}</span>
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

                      <td className="py-3">
                        {scan.verified ? (
                          <span className="text-[11px] text-emerald-400 font-semibold flex items-center gap-1">
                            <CheckCircle2 className="w-3.5 h-3.5" />
                            Verified
                          </span>
                        ) : (
                          <span className="text-[11px] text-amber-400 font-semibold flex items-center gap-1">
                            <AlertTriangle className="w-3.5 h-3.5" />
                            Pending
                          </span>
                        )}
                      </td>

                      <td className="py-3 text-right pr-3">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            setSelectedScan(scan);
                          }}
                          className="px-3 py-1.5 rounded-xl bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-300 font-bold text-xs transition-all border border-emerald-500/30 flex items-center gap-1.5 ml-auto"
                        >
                          <Eye className="w-3.5 h-3.5" />
                          <span>Inspect</span>
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Detail & Agronomist Verification Modal */}
      {selectedScan && (
        <ScanDetailModal
          scan={selectedScan}
          onClose={() => setSelectedScan(null)}
          onSaveScanUpdate={(updated) => {
            if (onSaveScanUpdate) onSaveScanUpdate(updated);
            setSelectedScan(null);
          }}
        />
      )}
    </div>
  );
}
