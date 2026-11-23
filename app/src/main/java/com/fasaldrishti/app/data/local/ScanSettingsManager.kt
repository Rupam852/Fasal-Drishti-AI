package com.fasaldrishti.app.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScanSettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fasal_scan_settings_prefs", Context.MODE_PRIVATE)

    private val _isOfflineModelEnabled = MutableStateFlow(prefs.getBoolean("offline_model_enabled", true))
    val isOfflineModelEnabled: StateFlow<Boolean> = _isOfflineModelEnabled.asStateFlow()

    private val _isOnlineVerificationEnabled = MutableStateFlow(prefs.getBoolean("online_verification_enabled", true))
    val isOnlineVerificationEnabled: StateFlow<Boolean> = _isOnlineVerificationEnabled.asStateFlow()

    fun setOfflineModelEnabled(enabled: Boolean): Boolean {
        // Mutual exclusion rule: Both cannot be OFF at the same time
        if (!enabled && !_isOnlineVerificationEnabled.value) {
            return false // Action blocked because online verification is already OFF
        }
        _isOfflineModelEnabled.value = enabled
        prefs.edit().putBoolean("offline_model_enabled", enabled).apply()
        return true
    }

    fun setOnlineVerificationEnabled(enabled: Boolean): Boolean {
        // Mutual exclusion rule: Both cannot be OFF at the same time
        if (!enabled && !_isOfflineModelEnabled.value) {
            return false // Action blocked because offline model is already OFF
        }
        _isOnlineVerificationEnabled.value = enabled
        prefs.edit().putBoolean("online_verification_enabled", enabled).apply()
        return true
    }
}
