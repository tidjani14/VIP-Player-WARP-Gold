package com.iptv.wgplayer.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iptv.wgplayer.model.AccountConfig
import com.iptv.wgplayer.model.StreamItem
import com.iptv.wgplayer.model.WarpTunnelConfig

class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("vip_warp_gold_store", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveAccount(account: AccountConfig) {
        prefs.edit().putString("account_config", gson.toJson(account)).apply()
    }

    fun getAccount(): AccountConfig? {
        val raw = prefs.getString("account_config", null) ?: return null
        return runCatching { gson.fromJson(raw, AccountConfig::class.java) }.getOrNull()
    }

    fun clearAccount() {
        prefs.edit().remove("account_config").apply()
    }

    fun getFavorites(): List<StreamItem> {
        val raw = prefs.getString("favorites_list", null) ?: return emptyList()
        val type = object : TypeToken<List<StreamItem>>() {}.type
        return runCatching { gson.fromJson<List<StreamItem>>(raw, type) }.getOrDefault(emptyList())
    }

    fun toggleFavorite(item: StreamItem): List<StreamItem> {
        val current = getFavorites().toMutableList()
        val existingIndex = current.indexOfFirst { it.id == item.id && it.section == item.section }
        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
        } else {
            current.add(0, item)
        }
        prefs.edit().putString("favorites_list", gson.toJson(current)).apply()
        return current
    }

    fun getCachedWarpConfig(): WarpTunnelConfig? {
        val raw = prefs.getString("warp_cached_config", null) ?: return null
        return runCatching { gson.fromJson(raw, WarpTunnelConfig::class.java) }.getOrNull()
    }

    fun saveWarpConfig(config: WarpTunnelConfig) {
        prefs.edit().putString("warp_cached_config", gson.toJson(config)).apply()
    }

    fun isAutoConnectVpn(): Boolean = prefs.getBoolean("warp_auto_connect", true)

    fun setAutoConnectVpn(enabled: Boolean) {
        prefs.edit().putBoolean("warp_auto_connect", enabled).apply()
    }
}
