import React, { useState } from 'react';
import { 
  Users, 
  Search, 
  MapPin, 
  Phone, 
  Calendar, 
  Scan, 
  ShieldCheck, 
  Download,
  Mail
} from 'lucide-react';

export default function FarmersManager({ farmers = [] }) {
  const [searchQuery, setSearchQuery] = useState('');

  const fallbackFarmers = [
    { id: 'f-1', name: 'Rameshwar Patel', phone: '+91 98765 43210', email: 'rameshwar.p@gmail.com', state: 'Madhya Pradesh', district: 'Indore', totalScans: 28, healthyScans: 19, diseasedScans: 9, joinedDate: '2026-08-14' },
    { id: 'f-2', name: 'Gurpreet Singh Dhillon', phone: '+91 98140 11223', email: 'gurpreet.farm@yahoo.com', state: 'Punjab', district: 'Ludhiana', totalScans: 45, healthyScans: 30, diseasedScans: 15, joinedDate: '2026-07-22' },
    { id: 'f-3', name: 'Subhasish Mondal', phone: '+91 94331 88765', email: 'mondal.krishi@gmail.com', state: 'West Bengal', district: 'Purba Bardhaman', totalScans: 16, healthyScans: 11, diseasedScans: 5, joinedDate: '2026-08-30' },
    { id: 'f-4', name: 'Dnyaneshwar Shinde', phone: '+91 99220 54321', email: 'shinde.farm@rediffmail.com', state: 'Maharashtra', district: 'Nashik', totalScans: 34, healthyScans: 22, diseasedScans: 12, joinedDate: '2026-08-05' },
    { id: 'f-5', name: 'Anil Kumar Yadav', phone: '+91 97980 65432', email: 'anilyadav.agri@gmail.com', state: 'Uttar Pradesh', district: 'Varanasi', totalScans: 12, healthyScans: 8, diseasedScans: 4, joinedDate: '2026-09-02' },
    { id: 'f-6', name: 'Venkat Subba Reddy', phone: '+91 98480 33445', email: 'venkatreddy.guntur@gmail.com', state: 'Andhra Pradesh', district: 'Guntur', totalScans: 52, healthyScans: 31, diseasedScans: 21, joinedDate: '2026-06-18' }
  ];

  const listToUse = farmers.length > 0 ? farmers : fallbackFarmers;

  const filteredFarmers = listToUse.filter(f => 
    !searchQuery || 
    f.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    f.phone?.includes(searchQuery) ||
    f.state?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    f.district?.toLowerCase().includes(searchQuery.toLowerCase())
  );

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
              National database of farmers utilizing Fasal Drishti AI for crop diagnosis and advisory
            </p>
          </div>

          <div className="flex items-center gap-2">
            <span className="text-xs px-3 py-1.5 rounded-xl bg-emerald-500/20 text-emerald-300 font-bold border border-emerald-500/30 font-mono">
              14,850+ Total Enrolled
            </span>
          </div>
        </div>

        {/* Search Bar */}
        <div className="mt-5 relative max-w-lg">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search farmer by name, phone number, state, or district..."
            className="w-full pl-10 pr-4 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-400 focus:outline-none focus:border-emerald-500"
          />
        </div>
      </div>

      {/* Farmers Table */}
      <div className="glass-panel p-5 rounded-2xl border border-white/10 shadow-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="border-b border-white/10 text-slate-400 font-semibold uppercase text-[10px] tracking-wider bg-slate-900/50">
                <th className="py-3.5 pl-3">Farmer Name</th>
                <th className="py-3.5">Contact Number</th>
                <th className="py-3.5">State & District</th>
                <th className="py-3.5 text-center">Total Scans</th>
                <th className="py-3.5 text-center">Healthy / Diseased</th>
                <th className="py-3.5">Enrolled Date</th>
                <th className="py-3.5 text-right pr-3">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {filteredFarmers.map((farmer, idx) => (
                <tr key={farmer.id || idx} className="hover:bg-slate-800/40 transition-colors">
                  <td className="py-3.5 pl-3">
                    <div className="flex items-center gap-2.5">
                      <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-emerald-600 to-teal-800 flex items-center justify-center font-bold text-white text-xs">
                        {farmer.name ? farmer.name.charAt(0) : 'F'}
                      </div>
                      <div>
                        <p className="font-bold text-white">{farmer.name}</p>
                        <p className="text-[10px] text-slate-400">{farmer.email || 'farmer@fasaldrishti.in'}</p>
                      </div>
                    </div>
                  </td>

                  <td className="py-3.5 font-mono text-slate-300">
                    {farmer.phone}
                  </td>

                  <td className="py-3.5">
                    <p className="font-semibold text-slate-200">{farmer.state}</p>
                    <p className="text-[11px] text-slate-400">{farmer.district}</p>
                  </td>

                  <td className="py-3.5 text-center font-bold font-mono text-emerald-400">
                    {farmer.totalScans || 0}
                  </td>

                  <td className="py-3.5 text-center">
                    <span className="text-emerald-400 font-bold">{farmer.healthyScans || 0}</span>
                    <span className="text-slate-500 mx-1">/</span>
                    <span className="text-amber-400 font-bold">{farmer.diseasedScans || 0}</span>
                  </td>

                  <td className="py-3.5 text-slate-400 font-mono text-[11px]">
                    {farmer.joinedDate || '2026-08-01'}
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
      </div>
    </div>
  );
}
