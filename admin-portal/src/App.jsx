import React, { useState, useEffect } from 'react';
import Header from './components/common/Header';
import Sidebar from './components/common/Sidebar';
import OverviewMetrics from './components/dashboard/OverviewMetrics';
import DiseaseAnalyticsChart from './components/dashboard/DiseaseAnalyticsChart';
import CropDistributionChart from './components/dashboard/CropDistributionChart';
import IndiaOutbreakHeatmap from './components/dashboard/IndiaOutbreakHeatmap';
import RecentScansFeed from './components/dashboard/RecentScansFeed';
import ScanManager from './components/scans/ScanManager';
import MandiManager from './components/mandi/MandiManager';
import AppUpdateManager from './components/updater/AppUpdateManager';
import BroadcastManager from './components/broadcast/BroadcastManager';
import AiHubManager from './components/ai-hub/AiHubManager';
import FarmersManager from './components/farmers/FarmersManager';
import SettingsManager from './components/settings/SettingsManager';
import LoginPage from './components/auth/LoginPage';
import ScanDetailModal from './components/scans/ScanDetailModal';

import { getCurrentAdminSession, logoutAdmin } from './services/auth';
import { 
  fetchAppConfigs, 
  fetchLiveScans, 
  fetchLiveFarmers 
} from './services/supabase';

import {
  INITIAL_STATS,
  CROP_DISTRIBUTION,
  WEEKLY_SCAN_TRENDS,
  INDIA_OUTBREAK_ZONES,
  MOCK_SCANS,
  MOCK_MANDI_RATES,
  MOCK_BROADCASTS,
  AI_ENGINES_CONFIG
} from './data/mockData';

export default function App() {
  const [adminSession, setAdminSession] = useState(getCurrentAdminSession());
  const [activeTab, setActiveTab] = useState('dashboard');
  const [searchQuery, setSearchQuery] = useState('');
  const [isRefreshing, setIsRefreshing] = useState(false);

  // Core Data State
  const [stats, setStats] = useState(INITIAL_STATS);
  const [scans, setScans] = useState(MOCK_SCANS);
  const [mandiRates, setMandiRates] = useState(MOCK_MANDI_RATES);
  const [broadcasts, setBroadcasts] = useState(MOCK_BROADCASTS);
  const [farmers, setFarmers] = useState([]);
  const [selectedScanForModal, setSelectedScanForModal] = useState(null);
  const [appConfigMap, setAppConfigMap] = useState({});

  // Fetch live Supabase data on mount
  useEffect(() => {
    if (adminSession) {
      loadLiveData();
    }
  }, [adminSession]);

  const loadLiveData = async () => {
    setIsRefreshing(true);
    try {
      const [configs, liveScans, liveFarmers] = await Promise.all([
        fetchAppConfigs(),
        fetchLiveScans(50),
        fetchLiveFarmers(50)
      ]);

      if (configs) {
        setAppConfigMap(configs);
      }

      if (liveScans && liveScans.length > 0) {
        // Map Supabase scan records to dashboard format
        const formattedScans = liveScans.map(s => ({
          id: s.id,
          farmerName: s.farmer_name || 'Farmer',
          farmerPhone: s.farmer_phone || '+91 98XXX XXXXX',
          location: s.location || 'India',
          cropName: s.crop_name,
          diseaseName: s.disease_name,
          confidence: Number(s.confidence) || 0.95,
          severity: s.severity || 'Moderate',
          status: s.is_synced ? 'Verified' : 'Pending',
          imageUrl: s.image_url || 'https://images.unsplash.com/photo-1592841200221-a6898f307baa?auto=format&fit=crop&w=600&q=80',
          symptoms: s.symptoms,
          treatment: s.treatment,
          createdAt: new Date(s.created_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          verified: s.is_synced
        }));
        setScans([...formattedScans, ...MOCK_SCANS]);
      }

      if (liveFarmers && liveFarmers.length > 0) {
        setFarmers(liveFarmers);
      }
    } catch (e) {
      console.warn('Using mock dataset as fallback:', e);
    } finally {
      setIsRefreshing(false);
    }
  };

  const handleLogout = () => {
    logoutAdmin();
    setAdminSession(null);
  };

  const handleScanUpdate = (updatedScan) => {
    setScans(scans.map(s => s.id === updatedScan.id ? updatedScan : s));
  };

  const handleBroadcastZone = (zone) => {
    setActiveTab('broadcast');
  };

  // If user is not logged in, show high-tech Login Page
  if (!adminSession) {
    return <LoginPage onLoginSuccess={(session) => setAdminSession(session)} />;
  }

  return (
    <div className="flex min-h-screen bg-[#090d14] text-slate-100 antialiased selection:bg-emerald-500 selection:text-white">
      {/* Left Sidebar Navigation */}
      <Sidebar
        activeTab={activeTab}
        onSelectTab={setActiveTab}
        pendingReviewCount={scans.filter(s => !s.verified).length}
      />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Top Header */}
        <Header
          adminSession={adminSession}
          onLogout={handleLogout}
          onRefreshData={loadLiveData}
          isRefreshing={isRefreshing}
          activeTab={activeTab}
          onNavigateTab={setActiveTab}
          searchQuery={searchQuery}
          onSearchChange={setSearchQuery}
        />

        {/* Dynamic Tab Body */}
        <main className="flex-1 p-4 lg:p-8 overflow-y-auto">
          {activeTab === 'dashboard' && (
            <div className="space-y-6 animate-in fade-in">
              {/* Stat Metric Cards */}
              <OverviewMetrics stats={stats} />

              {/* Charts Grid */}
              <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                <div className="lg:col-span-8">
                  <DiseaseAnalyticsChart data={WEEKLY_SCAN_TRENDS} />
                </div>
                <div className="lg:col-span-4">
                  <CropDistributionChart data={CROP_DISTRIBUTION} />
                </div>
              </div>

              {/* India Outbreak Radar Map */}
              <IndiaOutbreakHeatmap 
                zones={INDIA_OUTBREAK_ZONES} 
                onBroadcastZone={handleBroadcastZone} 
              />

              {/* Recent Scans Feed */}
              <RecentScansFeed
                scans={scans}
                onViewScan={(scan) => setSelectedScanForModal(scan)}
                onNavigateAll={() => setActiveTab('scans')}
              />
            </div>
          )}

          {activeTab === 'scans' && (
            <div className="animate-in fade-in">
              <ScanManager 
                scans={scans} 
                onSaveScanUpdate={handleScanUpdate} 
              />
            </div>
          )}

          {activeTab === 'outbreaks' && (
            <div className="animate-in fade-in space-y-6">
              <IndiaOutbreakHeatmap 
                zones={INDIA_OUTBREAK_ZONES} 
                onBroadcastZone={handleBroadcastZone} 
              />
            </div>
          )}

          {activeTab === 'mandi' && (
            <div className="animate-in fade-in">
              <MandiManager
                mandiRates={mandiRates}
                onAddRate={(newRate) => setMandiRates([newRate, ...mandiRates])}
                onUpdateRate={(updated) => setMandiRates(mandiRates.map(r => r.id === updated.id ? updated : r))}
                onDeleteRate={(id) => setMandiRates(mandiRates.filter(r => r.id !== id))}
              />
            </div>
          )}

          {activeTab === 'updater' && (
            <div className="animate-in fade-in">
              <AppUpdateManager
                currentAppVersion={appConfigMap['latest_app_version'] || '1.2.0'}
                downloadUrl={appConfigMap['app_download_url'] || 'https://github.com/Rupam852/Fasal-Drishti-AI/releases/latest'}
                onUpdateSuccess={(res) => {
                  setAppConfigMap({
                    ...appConfigMap,
                    latest_app_version: res.version,
                    app_download_url: res.url
                  });
                }}
              />
            </div>
          )}

          {activeTab === 'broadcast' && (
            <div className="animate-in fade-in">
              <BroadcastManager
                broadcasts={broadcasts}
                onSendBroadcast={(newBc) => setBroadcasts([newBc, ...broadcasts])}
              />
            </div>
          )}

          {activeTab === 'ai-hub' && (
            <div className="animate-in fade-in">
              <AiHubManager enginesConfig={AI_ENGINES_CONFIG} />
            </div>
          )}

          {activeTab === 'farmers' && (
            <div className="animate-in fade-in">
              <FarmersManager farmers={farmers} />
            </div>
          )}

          {activeTab === 'settings' && (
            <div className="animate-in fade-in">
              <SettingsManager adminSession={adminSession} />
            </div>
          )}
        </main>
      </div>

      {/* Global Scan Inspector Modal */}
      {selectedScanForModal && (
        <ScanDetailModal
          scan={selectedScanForModal}
          onClose={() => setSelectedScanForModal(null)}
          onSaveScanUpdate={(updated) => {
            handleScanUpdate(updated);
            setSelectedScanForModal(null);
          }}
        />
      )}
    </div>
  );
}
