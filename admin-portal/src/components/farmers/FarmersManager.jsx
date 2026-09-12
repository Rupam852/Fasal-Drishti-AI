import React, { useState } from 'react';
import { 
  Users, 
  Search, 
  MapPin, 
  Calendar, 
  ShieldCheck, 
  Download,
  Mail,
  UserCheck
} from 'lucide-react';

export default function FarmersManager({ farmers = [] }) {
  const [searchQuery, setSearchQuery] = useState('');

  const filteredFarmers = farmers.filter(f => 
    !searchQuery || 
    f.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    f.email?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    f.id?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const exportCSV = () => {
    if (filteredFarmers.length === 0) return;
    const headers = ['User ID', 'Name', 'Email', 'Total Scans', 'Healthy Scans', 'Diseased Scans', 'Created At'];
    const rows = filteredFarmers.map(f => [
      f.id,
      `"${f.name || 'Farmer'}"`,
      `"${f.email || ''}"`,
      f.total_scans || 0,
      f.healthy_count || 0,
      f.diseased_count || 0,
      f.created_at
    ]);

    const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map(e => e.join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `fasal_drishti_farmers_${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="space-y-6 text-left">
      {/* Top Header */}
      <div className="glass-panel p-5 lg:p-6 rounded-2xl border border-white/10 shadow-xl">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div>
            <h2 className="text-xl font-black text-white flex items-center gap-2.5">
              <Users className="w-5 h-5 text-emerald-400" />
              Registered Farmers & Beneficiaries Directory
            </h2>
            <p className="text-xs text-slate-400 mt-1">
              Live authenticated users from Supabase PostgreSQL table <code className="text-emerald-400 font-mono">public.users</code>
            </p>
          </div>

          <div className="flex items-center gap-3">
            <span className="text-xs px-3 py-1.5 rounded-xl bg-emerald-500/20 text-emerald-300 font-bold border border-emerald-500/30 font-mono">
              {farmers.length} Authenticated Farmers
            </span>

            <button
              onClick={exportCSV}
              disabled={filteredFarmers.length === 0}
              className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-xs font-semibold transition-all flex items-center gap-2 disabled:opacity-40"
            >
              <Download className="w-4 h-4 text-emerald-400" />
              <span>Export CSV</span>
            </button>
          </div>
        </div>

        {/* Search Bar */}
        <div className="mt-5 relative max-w-lg">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search farmer by name, email, or Google User ID..."
            className="w-full pl-10 pr-4 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-400 focus:outline-none focus:border-emerald-500"
          />
        </div>
      </div>

      {/* Farmers Table */}
      <div className="glass-panel p-5 rounded-2xl border border-white/10 shadow-xl overflow-hidden">
        {farmers.length === 0 ? (
          <div className="p-12 text-center rounded-2xl bg-slate-900/40 border border-white/5">
            <UserCheck className="w-8 h-8 text-slate-600 mx-auto mb-2" />
            <p className="text-xs text-slate-400">Loading live farmer profiles from Supabase...</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="border-b border-white/10 text-slate-400 font-semibold uppercase text-[10px] tracking-wider bg-slate-900/50">
                  <th className="py-3.5 pl-3">Farmer Profile</th>
                  <th className="py-3.5">Email Address</th>
                  <th className="py-3.5">Supabase Auth ID</th>
                  <th className="py-3.5 text-center">Total Scans</th>
                  <th className="py-3.5 text-center">Healthy / Diseased</th>
                  <th className="py-3.5">Registration Date</th>
                  <th className="py-3.5 text-right pr-3">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5">
                {filteredFarmers.map((farmer, idx) => (
                  <tr key={farmer.id || idx} className="hover:bg-slate-800/40 transition-colors">
                    <td className="py-3.5 pl-3">
                      <div className="flex items-center gap-2.5">
                        {farmer.avatar_url ? (
                          <img 
                            src={farmer.avatar_url} 
                            alt={farmer.name} 
                            className="w-8 h-8 rounded-full object-cover border border-emerald-500/40"
                            onError={(e) => {
                              e.target.style.display = 'none';
                            }}
                          />
                        ) : (
                          <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-emerald-600 to-teal-800 flex items-center justify-center font-bold text-white text-xs shrink-0">
                            {farmer.name ? farmer.name.charAt(0) : 'F'}
                          </div>
                        )}
                        <div>
                          <p className="font-bold text-white text-xs">{farmer.name || 'Farmer'}</p>
                          <p className="text-[10px] text-emerald-400 font-mono">Verified Google Account</p>
                        </div>
                      </div>
                    </td>

                    <td className="py-3.5 font-mono text-slate-300">
                      {farmer.email || 'N/A'}
                    </td>

                    <td className="py-3.5 font-mono text-slate-400 text-[11px] truncate max-w-[140px]">
                      {farmer.id}
                    </td>

                    <td className="py-3.5 text-center font-bold font-mono text-emerald-400">
                      {farmer.total_scans || 0}
                    </td>

                    <td className="py-3.5 text-center">
                      <span className="text-emerald-400 font-bold">{farmer.healthy_count || 0}</span>
                      <span className="text-slate-500 mx-1">/</span>
                      <span className="text-amber-400 font-bold">{farmer.diseased_count || 0}</span>
                    </td>

                    <td className="py-3.5 text-slate-400 font-mono text-[11px]">
                      {farmer.created_at ? new Date(farmer.created_at).toLocaleDateString() : 'Recent'}
                    </td>

                    <td className="py-3.5 text-right pr-3">
                      <span className="text-[10px] px-2.5 py-0.5 rounded-full font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                        Active
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
