package com.iptv.wgplayer.vpn

import android.content.Context
import android.content.Intent
import com.iptv.wgplayer.data.LocalStore
import com.iptv.wgplayer.model.VpnConnectionState
import com.iptv.wgplayer.model.VpnUiState
import com.iptv.wgplayer.model.WarpTunnelConfig
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.StringReader

class WireGuardVpnManager(
    private val context: Context,
    private val store: LocalStore
) {
    private val backend: GoBackend by lazy { GoBackend(context.applicationContext) }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var statsJob: Job? = null

    private val _uiState = MutableStateFlow(VpnUiState())
    val uiState: StateFlow<VpnUiState> = _uiState.asStateFlow()

    private val tunnel = object : Tunnel {
        override fun getName(): String = "vip_warp_tunnel"

        override fun onStateChange(newState: Tunnel.State) {
            val mapped = when (newState) {
                Tunnel.State.UP -> VpnConnectionState.CONNECTED
                Tunnel.State.DOWN -> VpnConnectionState.DISCONNECTED
                Tunnel.State.TOGGLE -> VpnConnectionState.CONNECTING
            }
            _uiState.value = _uiState.value.copy(state = mapped)
        }
    }

    fun prepareVpnPermission(): Intent? {
        return GoBackend.VpnService.prepare(context)
    }

    suspend fun ensureWarpConfig(forceRenew: Boolean = false): Result<WarpTunnelConfig> =
        withContext(Dispatchers.IO) {
            try {
                if (!forceRenew) {
                    val cached = store.getCachedWarpConfig()
                    if (cached != null) {
                        return@withContext Result.success(cached)
                    }
                }
                _uiState.value = _uiState.value.copy(
                    state = VpnConnectionState.REGISTERING_WARP,
                    errorMessage = ""
                )
                val fresh = CloudflareWarpClient.registerAndBuildConfig()
                store.saveWarpConfig(fresh)
                Result.success(fresh)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    state = VpnConnectionState.ERROR,
                    errorMessage = e.message ?: "Failed to fetch Cloudflare WARP key"
                )
                Result.failure(e)
            }
        }

    suspend fun connect(warpConfig: WarpTunnelConfig): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _uiState.value = _uiState.value.copy(
                state = VpnConnectionState.CONNECTING,
                endpoint = warpConfig.endpoint,
                clientAddress = warpConfig.clientAddress,
                errorMessage = ""
            )
            val parsedConfig = Config.parse(BufferedReader(StringReader(warpConfig.configText.trim())))
            backend.setState(tunnel, Tunnel.State.UP, parsedConfig)
            _uiState.value = _uiState.value.copy(
                state = VpnConnectionState.CONNECTED,
                endpoint = warpConfig.endpoint,
                clientAddress = warpConfig.clientAddress,
                errorMessage = ""
            )
            startStatsPolling()
            Result.success(Unit)
        } catch (e: Exception) {
            stopStatsPolling()
            _uiState.value = _uiState.value.copy(
                state = VpnConnectionState.ERROR,
                errorMessage = e.message ?: "WARP tunnel connection failed"
            )
            Result.failure(e)
        }
    }

    suspend fun disconnect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            stopStatsPolling()
            backend.setState(tunnel, Tunnel.State.DOWN, null)
            _uiState.value = VpnUiState(state = VpnConnectionState.DISCONNECTED)
            Result.success(Unit)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                state = VpnConnectionState.ERROR,
                errorMessage = e.message ?: "Disconnect failed"
            )
            Result.failure(e)
        }
    }

    private fun startStatsPolling() {
        statsJob?.cancel()
        statsJob = scope.launch {
            while (isActive) {
                runCatching {
                    val stats = backend.getStatistics(tunnel)
                    _uiState.value = _uiState.value.copy(
                        rxBytes = stats.totalRx(),
                        txBytes = stats.totalTx()
                    )
                }
                delay(1500L)
            }
        }
    }

    private fun stopStatsPolling() {
        statsJob?.cancel()
        statsJob = null
    }
}
