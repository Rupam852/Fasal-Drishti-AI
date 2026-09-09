package com.fasaldrishti.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

class NetworkMonitor(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isConnected = MutableStateFlow(isCurrentlyConnected())
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            checkAndUpdateConnection()
        }

        override fun onLost(network: Network) {
            _isConnected.value = false
            checkAndUpdateConnection()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            if (!hasInternet) {
                _isConnected.value = false
            } else {
                checkAndUpdateConnection()
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
        
        // Initial check
        checkAndUpdateConnection()
    }

    fun isCurrentlyConnected(): Boolean {
        val cm = connectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun checkAndUpdateConnection() {
        CoroutineScope(Dispatchers.IO).launch {
            val connected = isCurrentlyConnected() && canReachInternet()
            _isConnected.value = connected
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
            // Fallback: If port 53 / socket is blocked on certain carrier networks, trust network capabilities
            isCurrentlyConnected()
        }
    }
}
