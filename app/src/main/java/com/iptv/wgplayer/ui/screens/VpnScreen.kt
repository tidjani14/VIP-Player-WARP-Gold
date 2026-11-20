package com.iptv.wgplayer.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptv.wgplayer.model.VpnConnectionState
import com.iptv.wgplayer.model.VpnUiState
import com.iptv.wgplayer.ui.components.VipBackButton
import com.iptv.wgplayer.ui.components.vipSwitchColors
import com.iptv.wgplayer.ui.theme.BgBlack
import com.iptv.wgplayer.ui.theme.BorderDarkGray
import com.iptv.wgplayer.ui.theme.CardDarkGray
import com.iptv.wgplayer.ui.theme.GoldDark
import com.iptv.wgplayer.ui.theme.GoldLight
import com.iptv.wgplayer.ui.theme.GoldMuted
import com.iptv.wgplayer.ui.theme.StatusGreen
import com.iptv.wgplayer.ui.theme.StatusRed
import com.iptv.wgplayer.ui.theme.SurfaceDarkGray
import com.iptv.wgplayer.ui.theme.TextMuted
import com.iptv.wgplayer.ui.theme.TextPrimary

@Composable
fun VpnScreen(
    vpnState: VpnUiState,
    autoConnect: Boolean,
    onToggleAutoConnect: (Boolean) -> Unit,
    onAutoConnectWarp: (Boolean) -> Unit,
    onDisconnect: () -> Unit,
    onBack: () -> Unit
) {
    val isConnected = vpnState.state == VpnConnectionState.CONNECTED
    val isBusy = vpnState.state == VpnConnectionState.REGISTERING_WARP ||
        vpnState.state == VpnConnectionState.CONNECTING

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgBlack, SurfaceDarkGray)))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardDarkGray, RoundedCornerShape(16.dp))
                .border(1.dp, BorderDarkGray, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VipBackButton(onClick = onBack)
                Text(
                    text = "Cloudflare WARP Tunnel",
                    color = GoldLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Auto-connect", color = TextMuted, fontSize = 13.sp)
                Switch(
                    checked = autoConnect,
                    onCheckedChange = onToggleAutoConnect,
                    colors = vipSwitchColors()
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .background(CardDarkGray, RoundedCornerShape(22.dp))
                    .border(
                        width = 1.dp,
                        color = if (isConnected) StatusGreen.copy(alpha = 0.7f) else GoldMuted,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(SurfaceDarkGray)
                        .border(
                            width = 2.dp,
                            color = if (isConnected) StatusGreen else GoldDark,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(color = GoldLight, strokeWidth = 3.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "WARP Shield",
                            tint = if (isConnected) StatusGreen else GoldLight,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }

                Text(
                    text = when (vpnState.state) {
                        VpnConnectionState.CONNECTED -> "Encrypted & Connected"
                        VpnConnectionState.REGISTERING_WARP -> "Generating WARP Key..."
                        VpnConnectionState.CONNECTING -> "Connecting to Cloudflare..."
                        VpnConnectionState.ERROR -> "Connection Failed"
                        VpnConnectionState.DISCONNECTED -> "Tunnel Disconnected"
                    },
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                if (isConnected) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDarkGray, RoundedCornerShape(14.dp))
                            .border(1.dp, BorderDarkGray, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Endpoint: ${vpnState.endpoint}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Tunnel IP: ${vpnState.clientAddress}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "RX: ${formatBytes(vpnState.rxBytes)}   |   TX: ${formatBytes(vpnState.txBytes)}",
                            color = GoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(
                        text = "Automatic WireGuard key exchange with Cloudflare WARP (MTU 1280).",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }

                if (vpnState.errorMessage.isNotBlank()) {
                    Text(text = vpnState.errorMessage, color = StatusRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (isConnected) {
                            onDisconnect()
                        } else {
                            onAutoConnectWarp(false)
                        }
                    },
                    enabled = !isBusy,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected) StatusRed else GoldDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = if (isConnected) "Disconnect WARP" else "Connect WARP Automatically",
                        color = if (isConnected) TextPrimary else BgBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                OutlinedButton(
                    onClick = { onAutoConnectWarp(true) },
                    enabled = !isBusy,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GoldMuted),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Renew Key", tint = GoldLight)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Renew WARP Key & Reconnect", color = TextPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024L) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024.0) return String.format("%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024.0) return String.format("%.2f MB", mb)
    return String.format("%.2f GB", mb / 1024.0)
}
