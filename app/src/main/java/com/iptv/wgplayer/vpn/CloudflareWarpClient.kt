package com.iptv.wgplayer.vpn

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.iptv.wgplayer.model.WarpTunnelConfig
import com.wireguard.crypto.KeyPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object CloudflareWarpClient {

    private const val FALLBACK_PEER_PUBLIC_KEY = "bmXOC+F1FxEMF9dyiK2H5/1SUtzH0JuVo51h2wPfgyo="
    private const val FALLBACK_ENDPOINT = "engage.cloudflareclient.com:2408"

    private val regEndpoints = listOf(
        "https://api.cloudflareclient.com/v0a2158/reg",
        "https://api.cloudflareclient.com/v0a1922/reg"
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private fun currentIsoUtc(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    suspend fun registerAndBuildConfig(): WarpTunnelConfig = withContext(Dispatchers.IO) {
        val keyPair = KeyPair()
        val privateKeyB64 = keyPair.privateKey.toBase64()
        val publicKeyB64 = keyPair.publicKey.toBase64()

        val payload = JsonObject().apply {
            addProperty("key", publicKeyB64)
            addProperty("install_id", "")
            addProperty("fcm_token", "")
            addProperty("tos", currentIsoUtc())
            addProperty("model", "Android")
            addProperty("type", "Android")
            addProperty("locale", "en_US")
            addProperty("warp_enabled", true)
        }

        val mediaType = "application/json; charset=UTF-8".toMediaType()
        var lastError: Exception? = null

        for (url in regEndpoints) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "okhttp/3.12.1")
                    .header("CF-Client-Version", "a-6.11-2223")
                    .header("Accept", "application/json")
                    .post(payload.toString().toRequestBody(mediaType))
                    .build()

                client.newCall(request).execute().use { response ->
                    val rawBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw IllegalStateException("WARP registration HTTP ${response.code}")
                    }
                    val root = JsonParser.parseString(rawBody).asJsonObject
                    val configObj = root.getAsJsonObject("config")
                        ?: throw IllegalStateException("Missing WARP config in response")

                    val ifaceObj = configObj.getAsJsonObject("interface")
                    val addressesObj = ifaceObj?.getAsJsonObject("addresses")
                    val clientV4 = addressesObj?.get("v4")?.asString?.ifBlank { null } ?: "172.16.0.2"
                    val clientV6 = addressesObj?.get("v6")?.asString?.ifBlank { null }

                    val peersArr = configObj.getAsJsonArray("peers")
                    val firstPeer = if (peersArr != null && peersArr.size() > 0) {
                        peersArr[0].asJsonObject
                    } else {
                        null
                    }
                    val peerPublicKey = firstPeer?.get("public_key")?.asString?.ifBlank { null }
                        ?: FALLBACK_PEER_PUBLIC_KEY
                    val endpointHost = firstPeer
                        ?.getAsJsonObject("endpoint")
                        ?.get("host")
                        ?.asString
                        ?.ifBlank { null }
                        ?: FALLBACK_ENDPOINT

                    val addressLine = if (!clientV6.isNullOrBlank()) {
                        "$clientV4/32, $clientV6/128"
                    } else {
                        "$clientV4/32"
                    }

                    val wgConf = buildString {
                        appendLine("[Interface]")
                        appendLine("PrivateKey = $privateKeyB64")
                        appendLine("Address = $addressLine")
                        appendLine("DNS = 1.1.1.1, 1.0.0.1")
                        appendLine("MTU = 1280")
                        appendLine()
                        appendLine("[Peer]")
                        appendLine("PublicKey = $peerPublicKey")
                        appendLine("AllowedIPs = 0.0.0.0/0, ::/0")
                        appendLine("Endpoint = $endpointHost")
                        appendLine("PersistentKeepalive = 25")
                    }

                    return@withContext WarpTunnelConfig(
                        configText = wgConf,
                        endpoint = endpointHost,
                        clientAddress = clientV4
                    )
                }
            } catch (e: Exception) {
                lastError = e
            }
        }

        throw lastError ?: IllegalStateException("Unable to fetch Cloudflare WARP key")
    }
}
