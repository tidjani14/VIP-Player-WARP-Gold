package com.iptv.wgplayer.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.iptv.wgplayer.data.LocalStore
import com.iptv.wgplayer.data.XtreamRepository
import com.iptv.wgplayer.model.CategoryItem
import com.iptv.wgplayer.model.ContentSection
import com.iptv.wgplayer.model.LoginMode
import com.iptv.wgplayer.model.StreamItem
import com.iptv.wgplayer.model.WarpTunnelConfig
import com.iptv.wgplayer.ui.screens.CatalogScreen
import com.iptv.wgplayer.ui.screens.HomeScreen
import com.iptv.wgplayer.ui.screens.LoginScreen
import com.iptv.wgplayer.ui.screens.PlayerScreen
import com.iptv.wgplayer.ui.screens.VpnScreen
import com.iptv.wgplayer.ui.theme.VipPlayerTheme
import com.iptv.wgplayer.vpn.WireGuardVpnManager
import kotlinx.coroutines.launch

private enum class AppRoute {
    LOGIN,
    HOME,
    CATALOG,
    PLAYER,
    VPN
}

class MainActivity : ComponentActivity() {

    private lateinit var store: LocalStore
    private lateinit var repo: XtreamRepository
    private lateinit var vpnManager: WireGuardVpnManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        store = LocalStore(this)
        repo = XtreamRepository()
        vpnManager = WireGuardVpnManager(this, store)

        setContent {
            VipPlayerTheme {
                val scope = rememberCoroutineScope()
                val vpnState by vpnManager.uiState.collectAsState()

                var account by remember { mutableStateOf(store.getAccount()) }
                var route by remember { mutableStateOf(if (account != null) AppRoute.HOME else AppRoute.LOGIN) }
                var previousRoute by remember { mutableStateOf(route) }

                var isLoading by remember { mutableStateOf(false) }
                var errorText by remember { mutableStateOf("") }

                var activeSection by remember { mutableStateOf(ContentSection.LIVE) }
                var categories by remember { mutableStateOf<List<CategoryItem>>(emptyList()) }
                var selectedCategoryId by remember { mutableStateOf("all") }
                var streams by remember { mutableStateOf<List<StreamItem>>(emptyList()) }
                var favorites by remember { mutableStateOf(store.getFavorites()) }

                var currentStream by remember { mutableStateOf<StreamItem?>(null) }
                var currentPlaylist by remember { mutableStateOf<List<StreamItem>>(emptyList()) }

                var autoConnectVpn by remember { mutableStateOf(store.isAutoConnectVpn()) }
                var pendingWarpConfig by remember { mutableStateOf<WarpTunnelConfig?>(null) }

                val vpnPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val cfg = pendingWarpConfig
                        pendingWarpConfig = null
                        if (cfg != null) {
                            scope.launch { vpnManager.connect(cfg) }
                        }
                    }
                }

                fun connectWithPermission(cfg: WarpTunnelConfig) {
                    val intent = vpnManager.prepareVpnPermission()
                    if (intent != null) {
                        pendingWarpConfig = cfg
                        vpnPermissionLauncher.launch(intent)
                    } else {
                        scope.launch { vpnManager.connect(cfg) }
                    }
                }

                fun autoConnectCloudflareWarp(forceRenew: Boolean = false) {
                    scope.launch {
                        val result = vpnManager.ensureWarpConfig(forceRenew = forceRenew)
                        result.onSuccess { cfg ->
                            connectWithPermission(cfg)
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    if (autoConnectVpn) {
                        autoConnectCloudflareWarp(forceRenew = false)
                    }
                }

                BackHandler(enabled = route != AppRoute.HOME && route != AppRoute.LOGIN) {
                    route = when (route) {
                        AppRoute.PLAYER -> AppRoute.CATALOG
                        AppRoute.CATALOG -> AppRoute.HOME
                        AppRoute.VPN -> previousRoute
                        else -> route
                    }
                }

                fun loadSectionData(sec: ContentSection, catId: String = "all") {
                    val acc = account ?: return
                    activeSection = sec
                    selectedCategoryId = catId
                    errorText = ""
                    if (sec == ContentSection.FAVORITES) {
                        categories = listOf(CategoryItem("fav", "Favorites"))
                        streams = favorites
                        return
                    }
                    scope.launch {
                        isLoading = true
                        try {
                            if (catId == "all") {
                                categories = repo.loadCategories(acc, sec)
                            }
                            streams = repo.loadStreams(acc, sec, catId)
                        } catch (e: Exception) {
                            errorText = e.message ?: "Failed to load content"
                        } finally {
                            isLoading = false
                        }
                    }
                }

                when (route) {
                    AppRoute.LOGIN -> {
                        LoginScreen(
                            initialAccount = account,
                            vpnState = vpnState,
                            isLoading = isLoading,
                            errorText = errorText,
                            onSubmit = { newAcc ->
                                scope.launch {
                                    isLoading = true
                                    errorText = ""
                                    try {
                                        if (newAcc.mode == LoginMode.XTREAM) {
                                            repo.verifyXtreamLogin(newAcc)
                                        }
                                        store.saveAccount(newAcc)
                                        account = newAcc
                                        route = AppRoute.HOME
                                    } catch (e: Exception) {
                                        errorText = e.message ?: "Login failed"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            onOpenVpn = {
                                previousRoute = AppRoute.LOGIN
                                route = AppRoute.VPN
                            }
                        )
                    }

                    AppRoute.HOME -> {
                        val acc = account
                        if (acc == null) {
                            route = AppRoute.LOGIN
                        } else {
                            HomeScreen(
                                account = acc,
                                vpnState = vpnState,
                                favoritesCount = favorites.size,
                                onOpenSection = { sec ->
                                    route = AppRoute.CATALOG
                                    loadSectionData(sec, "all")
                                },
                                onOpenVpn = {
                                    previousRoute = AppRoute.HOME
                                    route = AppRoute.VPN
                                },
                                onLogout = {
                                    store.clearAccount()
                                    account = null
                                    route = AppRoute.LOGIN
                                }
                            )
                        }
                    }

                    AppRoute.CATALOG -> {
                        CatalogScreen(
                            section = activeSection,
                            vpnState = vpnState,
                            categories = categories,
                            selectedCategoryId = selectedCategoryId,
                            streams = streams,
                            favoriteIds = favorites.map { "${it.section}_${it.id}" }.toSet(),
                            isLoading = isLoading,
                            errorText = errorText,
                            onSelectCategory = { catId ->
                                loadSectionData(activeSection, catId)
                            },
                            onPlayStream = { item, list ->
                                currentStream = item
                                currentPlaylist = list
                                route = AppRoute.PLAYER
                            },
                            onToggleFavorite = { item ->
                                favorites = store.toggleFavorite(item)
                                if (activeSection == ContentSection.FAVORITES) {
                                    streams = favorites
                                }
                            },
                            onOpenVpn = {
                                previousRoute = AppRoute.CATALOG
                                route = AppRoute.VPN
                            },
                            onBack = { route = AppRoute.HOME }
                        )
                    }

                    AppRoute.PLAYER -> {
                        val s = currentStream
                        if (s == null) {
                            route = AppRoute.CATALOG
                        } else {
                            PlayerScreen(
                                stream = s,
                                playlist = currentPlaylist,
                                vpnState = vpnState,
                                onSwitchStream = { next -> currentStream = next },
                                onBack = { route = AppRoute.CATALOG }
                            )
                        }
                    }

                    AppRoute.VPN -> {
                        VpnScreen(
                            vpnState = vpnState,
                            autoConnect = autoConnectVpn,
                            onToggleAutoConnect = { enabled ->
                                autoConnectVpn = enabled
                                store.setAutoConnectVpn(enabled)
                            },
                            onAutoConnectWarp = { forceRenew ->
                                autoConnectCloudflareWarp(forceRenew = forceRenew)
                            },
                            onDisconnect = { scope.launch { vpnManager.disconnect() } },
                            onBack = { route = previousRoute }
                        )
                    }
                }
            }
        }
    }
}
