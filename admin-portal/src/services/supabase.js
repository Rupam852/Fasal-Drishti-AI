import { createClient } from '@supabase/supabase-js';

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL || 'https://tajizxhfxewkelzrmgux.supabase.co';
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRhaml6eGhmeGV3a2VsenJtZ3V4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODg3ODMxMjUsImV4cCI6MjEwNDM1OTEyNX0.l39iKsN8kWzuQt2-T0dISIokx9Ys8E1ITXQfZcTi3Zw';

export const supabase = createClient(supabaseUrl, supabaseAnonKey, {
  auth: {
    persistSession: true,
    autoRefreshToken: true,
  }
});

/**
 * Fetch live app configurations (API keys, current version, download URL)
 */
export async function fetchAppConfigs() {
  try {
    const { data, error } = await supabase
      .from('app_config')
      .select('*');
    if (error) throw error;
    
    const configMap = {};
    data?.forEach(item => {
      configMap[item.key] = item.value;
    });
    return configMap;
  } catch (err) {
    console.warn('Using local config fallback:', err.message);
    return null;
  }
}

/**
 * Update app config key-value pair in Supabase
 */
export async function updateAppConfig(key, value, description = '') {
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
    console.error('Failed to update app_config:', err);
    return { success: false, error: err.message };
  }
}

/**
 * Fetch latest scan records with farmer details
 */
export async function fetchLiveScans(limit = 100) {
  try {
    const { data, error } = await supabase
      .from('scans')
      .select('*')
      .order('created_at', { ascending: false })
      .limit(limit);
      
    if (error) throw error;
    return data || [];
  } catch (err) {
    console.warn('Scans fetch error, using cache/mock data:', err.message);
    return null;
  }
}

/**
 * Fetch registered farmers
 */
export async function fetchLiveFarmers(limit = 100) {
  try {
    const { data, error } = await supabase
      .from('users')
      .select('*')
      .order('created_at', { ascending: false })
      .limit(limit);
      
    if (error) throw error;
    return data || [];
  } catch (err) {
    console.warn('Users fetch error, using cache/mock data:', err.message);
    return null;
  }
}
