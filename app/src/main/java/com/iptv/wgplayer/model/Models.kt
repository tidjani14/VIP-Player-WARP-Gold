package com.iptv.wgplayer.model

enum class LoginMode {
    XTREAM,
    M3U
}

enum class ContentSection(val title: String) {
    LIVE("LIVE TV"),
    VOD("MOVIES"),
    SERIES("SERIES"),
    FAVORITES("FAVORITES")
}

data class AccountConfig(
    val profileName: String = "VIP Account",
    val mode: LoginMode = LoginMode.XTREAM,
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val m3uUrl: String = ""
)

data class AccountStatusInfo(
    val status: String = "",
    val expDate: String = "",
    val activeConnections: String = "0",
    val maxConnections: String = "1"
)

data class CategoryItem(
    val id: String,
    val name: String
)

data class StreamItem(
    val id: String,
    val name: String,
    val iconUrl: String = "",
    val categoryId: String = "",
    val section: ContentSection = ContentSection.LIVE,
    val extension: String = "ts",
    val directUrl: String = "",
    val rating: String = "",
    val plot: String = ""
)

data class WarpTunnelConfig(
    val configText: String,
    val endpoint: String,
    val clientAddress: String
)

enum class VpnConnectionState {
    DISCONNECTED,
    REGISTERING_WARP,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class VpnUiState(
    val state: VpnConnectionState = VpnConnectionState.DISCONNECTED,
    val endpoint: String = "",
    val clientAddress: String = "",
    val rxBytes: Long = 0L,
    val txBytes: Long = 0L,
    val errorMessage: String = ""
)
