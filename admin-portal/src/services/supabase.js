import { createClient } from '@supabase/supabase-js';

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL || 'https://tajizxhfxewkelzrmgux.supabase.co';
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRhaml6eGhmeGV3a2VsenJtZ3V4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODg3ODMxMjUsImV4cCI6MjEwNDM1OTEyNX0.l39iKsN8kWzuQt2-T0dISIokx9Ys8E1ITXQfZcTi3Zw';

export const supabase = createClient(supabaseUrl, supabaseAnonKey, {
  auth: {
    persistSession: true,
    autoRefreshToken: true,
  },
  realtime: {
    params: {
      eventsPerSecond: 10,
    },
  },
});

/**
 * Fetch all registered farmers directly from 'users' table
 */
export async function fetchLiveFarmers() {
  try {
    const { data, error } = await supabase
      .from('users')
      .select('*')
      .order('created_at', { ascending: false });

    if (error) throw error;
    return data || [];
  } catch (err) {
    console.error('Error fetching live users from Supabase:', err);
    return [];
  }
}

/**
 * Fetch all crop scans directly from 'scans' table
 */
export async function fetchLiveScans() {
  try {
    const { data, error } = await supabase
      .from('scans')
      .select('*')
      .order('created_at', { ascending: false });

    if (error) throw error;
    return data || [];
  } catch (err) {
    console.error('Error fetching live scans from Supabase:', err);
    return [];
  }
}

/**
 * Fetch crop pathology knowledge library from 'disease_info' table
 */
export async function fetchLiveDiseaseInfo() {
  try {
    const { data, error } = await supabase
      .from('disease_info')
      .select('*')
      .order('crop_name', { ascending: true });

    if (error) throw error;
    return data || [];
  } catch (err) {
    console.error('Error fetching disease_info from Supabase:', err);
    return [];
  }
}

/**
 * Fetch dynamic app configurations (API keys, versions, download links) from 'app_config' table
 */
export async function fetchLiveAppConfigs() {
  try {
    const { data, error } = await supabase
      .from('app_config')
      .select('*');

    if (error) throw error;
    
    const configMap = {};
    data?.forEach((item) => {
      configMap[item.key] = {
        value: item.value,
        description: item.description,
        updatedAt: item.updated_at
      };
    });
    return configMap;
  } catch (err) {
    console.error('Error fetching app_config from Supabase:', err);
    return {};
  }
}

/**
 * Update an app configuration in Supabase 'app_config'
 */
export async function updateLiveAppConfig(key, value, description = '') {
  try {
    const { data, error } = await supabase
      .from('app_config')
      .upsert({
        key,
        value,
        description,
        updated_at: new Date().toISOString()
      }, { onConflict: 'key' })
      .select();

    if (error) throw error;
    return { success: true, data };
  } catch (err) {
    console.error('Failed to update app_config in Supabase:', err);
    return { success: false, error: err.message };
  }
}

// Alias for convenience
export const updateAppConfig = updateLiveAppConfig;
export const fetchAppConfigs = fetchLiveAppConfigs;

/**
 * Subscribe to realtime live updates on Supabase tables
 */
export function subscribeToRealtimeTable(tableName, onInsert, onUpdate, onDelete) {
  const channel = supabase
    .channel(`realtime_${tableName}`)
    .on('postgres_changes', { event: 'INSERT', schema: 'public', table: tableName }, (payload) => {
      if (onInsert) onInsert(payload.new);
    })
    .on('postgres_changes', { event: 'UPDATE', schema: 'public', table: tableName }, (payload) => {
      if (onUpdate) onUpdate(payload.new);
    })
    .on('postgres_changes', { event: 'DELETE', schema: 'public', table: tableName }, (payload) => {
      if (onDelete) onDelete(payload.old);
    })
    .subscribe();

  return () => {
    supabase.removeChannel(channel);
  };
}

/**
 * Compute real-time analytics dynamically from live Supabase records
 */
export function computeLiveMetrics(users = [], scans = [], diseaseInfo = []) {
  const totalFarmers = users.length;
  
  // Aggregate scan totals from both scans table and user profiles
  const scansTableCount = scans.length;
  const userScansSum = users.reduce((acc, u) => acc + (u.total_scans || 0), 0);
  const totalScans = Math.max(scansTableCount, userScansSum);

  const healthyScansCount = scans.filter(s => s.severity?.toLowerCase() === 'healthy' || s.predicted_class?.toLowerCase().includes('healthy')).length;
  const userHealthySum = users.reduce((acc, u) => acc + (u.healthy_count || 0), 0);
  const totalHealthy = Math.max(healthyScansCount, userHealthySum);

  const diseasedScansCount = scans.length - healthyScansCount;
  const userDiseasedSum = users.reduce((acc, u) => acc + (u.diseased_count || 0), 0);
  const totalDiseased = Math.max(diseasedScansCount, userDiseasedSum);

  // Crop Distribution calculation from real scans or disease library
  const cropCountMap = {};
  if (scans.length > 0) {
    scans.forEach(s => {
      const c = s.crop_name || 'Other';
      cropCountMap[c] = (cropCountMap[c] || 0) + 1;
    });
  } else if (diseaseInfo.length > 0) {
    diseaseInfo.forEach(d => {
      const c = d.crop_name || 'Other';
      cropCountMap[c] = (cropCountMap[c] || 0) + 1;
    });
  }

  const cropColors = ['#10b981', '#f59e0b', '#06b6d4', '#ec4899', '#8b5cf6', '#ef4444', '#6366f1'];
  const cropDistribution = Object.keys(cropCountMap).map((crop, idx) => ({
    name: crop,
    value: scans.length > 0 ? Math.round((cropCountMap[crop] / scans.length) * 100) : Math.round((cropCountMap[crop] / diseaseInfo.length) * 100),
    count: cropCountMap[crop],
    color: cropColors[idx % cropColors.length]
  }));

  // Build weekly trends from real scan timestamps
  const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  const last7Days = Array.from({ length: 7 }).map((_, i) => {
    const d = new Date();
    d.setDate(d.getDate() - (6 - i));
    return {
      day: days[d.getDay()],
      dateStr: d.toISOString().split('T')[0],
      scans: 0,
      healthy: 0,
      diseased: 0
    };
  });

  scans.forEach(s => {
    if (s.created_at) {
      const scanDate = s.created_at.split('T')[0];
      const match = last7Days.find(d => d.dateStr === scanDate);
      if (match) {
        match.scans += 1;
        if (s.severity?.toLowerCase() === 'healthy' || s.predicted_class?.toLowerCase().includes('healthy')) {
          match.healthy += 1;
        } else {
          match.diseased += 1;
        }
      }
    }
  });

  return {
    totalFarmers,
    totalScans,
    totalHealthy,
    totalDiseased,
    cropDistribution,
    weeklyTrends: last7Days,
    totalPathologyClasses: diseaseInfo.length
  };
}
