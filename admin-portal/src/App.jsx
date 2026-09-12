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
  fetchLiveAppConfigs, 
  fetchLiveScans, 
  fetchLiveFarmers,
  fetchLiveDiseaseInfo,
  computeLiveMetrics,
  subscribeToRealtimeTable
} from './services/supabase';

export default function App() {
  const [adminSession, setAdminSession] = useState(getCurrentAdminSession());
  const [activeTab, setActiveTab] = useState('dashboard');
  const [searchQuery, setSearchQuery] = useState('');
  const [isRefreshing, setIsRefreshing] = useState(false);

  // 100% Live Supabase Data State
  const [farmers, setFarmers] = useState([]);
  const [scans, setScans] = useState([]);
  const [diseaseInfo, setDiseaseInfo] = useState([]);
  const [appConfigMap, setAppConfigMap] = useState({});
  const [metrics, setMetrics] = useState({
    totalFarmers: 0,
    totalScans: 0,
    totalHealthy: 0,
    totalDiseased: 0,
    cropDistribution: [],
    weeklyTrends: [],
    totalPathologyClasses: 0
  });

  const [mandiRates, setMandiRates] = useState([]);
  const [broadcasts, setBroadcasts] = useState([]);
  const [selectedScanForModal, setSelectedScanForModal] = useState(null);

  // Fetch live Supabase data on mount & subscribe to realtime changes
  useEffect(() => {
    if (adminSession) {
      loadLiveData();

      // Setup live Realtime listeners on Supabase PostgreSQL tables
      const unsubscribeUsers = subscribeToRealtimeTable('users', () => loadLiveData(), () => loadLiveData());
      const unsubscribeScans = subscribeToRealtimeTable('scans', () => loadLiveData(), () => loadLiveData());
      const unsubscribeConfigs = subscribeToRealtimeTable('app_config', () => loadLiveData(), () => loadLiveData());

      return () => {
        if (unsubscribeUsers) unsubscribeUsers();
        if (unsubscribeScans) unsubscribeScans();
        if (unsubscribeConfigs) unsubscribeConfigs();
      };
    }
  }, [adminSession]);

  const loadLiveData = async () => {
    setIsRefreshing(true);
    try {
      const [liveConfigs, liveScans, liveFarmers, liveDiseases] = await Promise.all([
        fetchLiveAppConfigs(),
        fetchLiveScans(),
        fetchLiveFarmers(),
        fetchLiveDiseaseInfo()
      ]);

      if (liveConfigs) {
        setAppConfigMap(liveConfigs);
      }

      if (liveDiseases) {
        setDiseaseInfo(liveDiseases);
      }

      if (liveFarmers) {
        setFarmers(liveFarmers);
      }

      if (liveScans) {
        const formattedScans = liveScans.map(s => ({
          id: s.id,
          farmerName: s.farmer_name || s.user_name || 'Farmer',
          farmerPhone: s.farmer_phone || '+91 98XXX XXXXX',
          location: s.location || 'India',
          cropName: s.crop_name || 'Crop',
          diseaseName: s.disease_name || s.predicted_class || 'Leaf Analysis',
          confidence: Number(s.confidence) || 0.95,
          severity: s.severity || 'Moderate',
          status: s.is_synced ? 'Verified' : 'Pending',
          imageUrl: s.image_url || 'https://images.unsplash.com/photo-1592841200221-a6898f307baa?auto=format&fit=crop&w=600&q=80',
          symptoms: s.symptoms,
          treatment: s.treatment,
          createdAt: s.created_at ? new Date(s.created_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Recent',
          verified: s.is_synced || false
        }));
        setScans(formattedScans);
      }

      // Compute dynamic analytics purely from real Supabase records
      const computed = computeLiveMetrics(liveFarmers || [], liveScans || [], liveDiseases || []);
      setMetrics(computed);

    } catch (e) {
      console.error('Error fetching live data from Supabase:', e);
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

  // If user is not logged in, show dark glassmorphic Login Screen
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
              {/* Dynamic Metric Cards computed from live DB */}
              <OverviewMetrics metrics={metrics} appConfigs={appConfigMap} />

              {/* Realtime Velocity & Crop Share Charts */}
              <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                <div className="lg:col-span-8">
                  <DiseaseAnalyticsChart data={metrics.weeklyTrends} />
                </div>
                <div className="lg:col-span-4">
                  <CropDistributionChart data={metrics.cropDistribution} />
                </div>
              </div>

              {/* India Outbreak Knowledge Radar (from Supabase disease_info) */}
              <IndiaOutbreakHeatmap 
                diseaseInfo={diseaseInfo} 
                onBroadcastZone={handleBroadcastZone} 
              />

              {/* Realtime Farmer Scan Telemetry Feed */}
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
                diseaseInfo={diseaseInfo} 
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
                currentAppVersion={appConfigMap['latest_app_version']?.value || '1.2.0'}
                downloadUrl={appConfigMap['app_download_url']?.value || 'https://github.com/Rupam852/Fasal-Drishti-AI/releases/latest'}
                onUpdateSuccess={(res) => {
                  setAppConfigMap({
                    ...appConfigMap,
                    latest_app_version: { value: res.version },
                    app_download_url: { value: res.url }
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
              <AiHubManager 
                appConfigs={appConfigMap} 
                onConfigUpdated={loadLiveData}
              />
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
