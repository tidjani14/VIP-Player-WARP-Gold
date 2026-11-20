package com.iptv.wgplayer.data

import com.google.gson.JsonParser
import com.iptv.wgplayer.model.AccountConfig
import com.iptv.wgplayer.model.AccountStatusInfo
import com.iptv.wgplayer.model.CategoryItem
import com.iptv.wgplayer.model.ContentSection
import com.iptv.wgplayer.model.LoginMode
import com.iptv.wgplayer.model.StreamItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class XtreamRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private fun normalizeBaseUrl(raw: String): String {
        val trimmed = raw.trim().removeSuffix("/")
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "http://$trimmed"
        }
    }

    private fun enc(value: String): String = URLEncoder.encode(value.trim(), "UTF-8")

    private fun fetchString(url: String): String {
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "VIPPlayerWARP/1.3")
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                throw IllegalStateException("HTTP ${resp.code}: ${resp.message}")
            }
            return resp.body?.string() ?: ""
        }
    }

    suspend fun verifyXtreamLogin(account: AccountConfig): AccountStatusInfo = withContext(Dispatchers.IO) {
        val base = normalizeBaseUrl(account.serverUrl)
        val url = "$base/player_api.php?username=${enc(account.username)}&password=${enc(account.password)}"
        val body = fetchString(url)
        val root = JsonParser.parseString(body).asJsonObject
        val userInfo = root.getAsJsonObject("user_info")
            ?: throw IllegalStateException("Invalid Xtream response")
        val auth = userInfo.get("auth")?.asInt ?: 0
        if (auth == 0) {
            throw IllegalStateException("Authentication failed: check username or password")
        }
        AccountStatusInfo(
            status = userInfo.get("status")?.asString ?: "Active",
            expDate = userInfo.get("exp_date")?.asString ?: "",
            activeConnections = userInfo.get("active_cons")?.asString ?: "0",
            maxConnections = userInfo.get("max_connections")?.asString ?: "1"
        )
    }

    suspend fun loadCategories(account: AccountConfig, section: ContentSection): List<CategoryItem> =
        withContext(Dispatchers.IO) {
            if (account.mode == LoginMode.M3U) {
                return@withContext listOf(CategoryItem("all", "All Channels"))
            }
            val base = normalizeBaseUrl(account.serverUrl)
            val action = when (section) {
                ContentSection.LIVE -> "get_live_categories"
                ContentSection.VOD -> "get_vod_categories"
                ContentSection.SERIES -> "get_series_categories"
                ContentSection.FAVORITES -> return@withContext listOf(CategoryItem("fav", "Favorites"))
            }
            val url = "$base/player_api.php?username=${enc(account.username)}&password=${enc(account.password)}&action=$action"
            val body = fetchString(url)
            val arr = JsonParser.parseString(body).asJsonArray
            val list = mutableListOf(CategoryItem("all", "All"))
            for (el in arr) {
                val obj = el.asJsonObject
                val id = obj.get("category_id")?.asString ?: continue
                val name = obj.get("category_name")?.asString ?: "Category $id"
                list.add(CategoryItem(id, name))
            }
            list
        }

    suspend fun loadStreams(
        account: AccountConfig,
        section: ContentSection,
        categoryId: String = "all"
    ): List<StreamItem> = withContext(Dispatchers.IO) {
        if (account.mode == LoginMode.M3U) {
            return@withContext parseM3uPlaylist(account.m3uUrl)
        }
        val base = normalizeBaseUrl(account.serverUrl)
        val action = when (section) {
            ContentSection.LIVE -> "get_live_streams"
            ContentSection.VOD -> "get_vod_streams"
            ContentSection.SERIES -> "get_series"
            ContentSection.FAVORITES -> return@withContext emptyList()
        }
        val catParam = if (categoryId != "all" && categoryId.isNotBlank()) "&category_id=$categoryId" else ""
        val url = "$base/player_api.php?username=${enc(account.username)}&password=${enc(account.password)}&action=$action$catParam"
        val body = fetchString(url)
        val arr = JsonParser.parseString(body).asJsonArray
        val results = mutableListOf<StreamItem>()

        for (el in arr) {
            val obj = el.asJsonObject
            val id = when (section) {
                ContentSection.SERIES -> obj.get("series_id")?.asString
                else -> obj.get("stream_id")?.asString
            } ?: continue
            val name = obj.get("name")?.asString ?: "Stream $id"
            val icon = when (section) {
                ContentSection.SERIES -> obj.get("cover")?.asString ?: ""
                else -> obj.get("stream_icon")?.asString ?: ""
            }
            val catId = obj.get("category_id")?.asString ?: ""
            val ext = obj.get("container_extension")?.asString ?: if (section == ContentSection.LIVE) "ts" else "mp4"
            val rating = obj.get("rating")?.asString ?: ""
            val plot = obj.get("plot")?.asString ?: ""

            val directUrl = when (section) {
                ContentSection.LIVE -> "$base/live/${enc(account.username)}/${enc(account.password)}/$id.ts"
                ContentSection.VOD -> "$base/movie/${enc(account.username)}/${enc(account.password)}/$id.$ext"
                ContentSection.SERIES -> "$base/series/${enc(account.username)}/${enc(account.password)}/$id.$ext"
                ContentSection.FAVORITES -> ""
            }

            results.add(
                StreamItem(
                    id = id,
                    name = name,
                    iconUrl = icon,
                    categoryId = catId,
                    section = section,
                    extension = ext,
                    directUrl = directUrl,
                    rating = rating,
                    plot = plot
                )
            )
        }
        results
    }

    private fun parseM3uPlaylist(m3uUrl: String): List<StreamItem> {
        val text = fetchString(normalizeBaseUrl(m3uUrl))
        val lines = text.lines()
        val items = mutableListOf<StreamItem>()
        var currentName = ""
        var currentLogo = ""
        var currentGroup = "all"
        var index = 1

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.startsWith("#EXTINF")) {
                currentName = line.substringAfterLast(",", "Channel $index").trim()
                currentLogo = Regex("""tvg-logo="([^"]*)"""").find(line)?.groupValues?.getOrNull(1) ?: ""
                currentGroup = Regex("""group-title="([^"]*)"""").find(line)?.groupValues?.getOrNull(1) ?: "all"
            } else if (line.startsWith("http://") || line.startsWith("https://")) {
                items.add(
                    StreamItem(
                        id = "m3u_$index",
                        name = currentName.ifBlank { "Channel $index" },
                        iconUrl = currentLogo,
                        categoryId = currentGroup,
                        section = ContentSection.LIVE,
                        extension = "m3u8",
                        directUrl = line
                    )
                )
                index++
                currentName = ""
                currentLogo = ""
            }
        }
        return items
    }
}
