package com.fasaldrishti.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetSocketAddress
import java.net.Socket

class NetworkMonitor(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val monitorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pendingDisconnectJob: Job? = null

    private val _isConnected = MutableStateFlow(isCurrentlyConnected())
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // Cancel any pending disconnect job immediately on reconnect/wake-up
            pendingDisconnectJob?.cancel()
            _isConnected.value = true
        }

        override fun onLost(network: Network) {
            // Screen lock or network interface switch might fire onLost temporarily.
            // Apply a grace period (3.5 seconds) so unlocking the phone doesn't trigger false alarms.
            pendingDisconnectJob?.cancel()
            pendingDisconnectJob = monitorScope.launch {
                delay(3500)
                if (!isCurrentlyConnected()) {
                    _isConnected.value = false
                }
            }
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            
            if (hasInternet) {
                pendingDisconnectJob?.cancel()
                _isConnected.value = true
            } else {
                val hasBasicInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                if (hasBasicInternet) {
                    pendingDisconnectJob?.cancel()
                    _isConnected.value = true
                } else {
                    pendingDisconnectJob?.cancel()
                    pendingDisconnectJob = monitorScope.launch {
                        delay(3500)
                        if (!isCurrentlyConnected()) {
                            _isConnected.value = false
                        }
                    }
                }
            }
        }
    }

    init {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (_: Exception) {}
        
        // Initial state
        _isConnected.value = isCurrentlyConnected()
    }

    fun isCurrentlyConnected(): Boolean {
        val cm = connectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun checkAndUpdateConnection() {
        monitorScope.launch {
            val connected = isCurrentlyConnected()
            if (connected) {
                pendingDisconnectJob?.cancel()
                _isConnected.value = true
            } else {
                delay(2000)
                if (!isCurrentlyConnected()) {
                    _isConnected.value = false
                }
            }
        }
    }

    suspend fun verifyConnection(): Boolean = withContext(Dispatchers.IO) {
        val connected = isCurrentlyConnected() && canReachInternet()
        _isConnected.value = connected
        connected
    }

    private fun canReachInternet(): Boolean {
        return try {
            val socket = Socket()
            val socketAddress = InetSocketAddress("8.8.8.8", 53)
            socket.connect(socketAddress, 1500)
            socket.close()
            true
        } catch (_: Exception) {
            // Fallback: trust system capabilities if socket ping is blocked
            isCurrentlyConnected()
        }
    }
}
