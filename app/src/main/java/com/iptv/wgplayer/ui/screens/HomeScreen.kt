package com.iptv.wgplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptv.wgplayer.model.AccountConfig
import com.iptv.wgplayer.model.ContentSection
import com.iptv.wgplayer.model.VpnConnectionState
import com.iptv.wgplayer.model.VpnUiState
import com.iptv.wgplayer.ui.components.WarpStatusPill
import com.iptv.wgplayer.ui.theme.BgBlack
import com.iptv.wgplayer.ui.theme.BorderDarkGray
import com.iptv.wgplayer.ui.theme.CardDarkGray
import com.iptv.wgplayer.ui.theme.ElevatedDarkGray
import com.iptv.wgplayer.ui.theme.GoldDark
import com.iptv.wgplayer.ui.theme.GoldLight
import com.iptv.wgplayer.ui.theme.GoldMuted
import com.iptv.wgplayer.ui.theme.StatusGreen
import com.iptv.wgplayer.ui.theme.SurfaceDarkGray
import com.iptv.wgplayer.ui.theme.TextMuted
import com.iptv.wgplayer.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    account: AccountConfig,
    vpnState: VpnUiState,
    favoritesCount: Int,
    onOpenSection: (ContentSection) -> Unit,
    onOpenVpn: () -> Unit,
    onLogout: () -> Unit
) {
    var clockText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFmt = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        while (true) {
            val now = Date()
            clockText = timeFmt.format(now)
            dateText = dateFmt.format(now)
            delay(15_000L)
        }
    }

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
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "VIP PLAYER",
                    color = GoldLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${account.profileName}  •  ${account.mode.name}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                WarpStatusPill(vpnState = vpnState, onClick = onOpenVpn)

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = clockText,
                        color = GoldLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateText,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ElevatedDarkGray)
                        .border(1.dp, BorderDarkGray, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Switch Account",
                        tint = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashboardTile(
                title = "LIVE TV",
                subtitle = "Channels & Sports",
                icon = Icons.Default.LiveTv,
                accent = GoldLight,
                modifier = Modifier.weight(1f).fillMaxSize(),
                onClick = { onOpenSection(ContentSection.LIVE) }
            )

            DashboardTile(
                title = "MOVIES",
                subtitle = "VOD Library",
                icon = Icons.Default.Movie,
                accent = GoldDark,
                modifier = Modifier.weight(1f).fillMaxSize(),
                onClick = { onOpenSection(ContentSection.VOD) }
            )

            DashboardTile(
                title = "SERIES",
                subtitle = "TV Shows",
                icon = Icons.Default.Tv,
                accent = GoldLight,
                modifier = Modifier.weight(1f).fillMaxSize(),
                onClick = { onOpenSection(ContentSection.SERIES) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashboardTile(
                title = "CLOUDFLARE WARP",
                subtitle = when (vpnState.state) {
                    VpnConnectionState.CONNECTED -> "Encrypted • ${vpnState.endpoint}"
                    VpnConnectionState.REGISTERING_WARP -> "Generating WireGuard key..."
                    VpnConnectionState.CONNECTING -> "Establishing encrypted tunnel..."
                    else -> "Auto Encrypted Tunnel"
                },
                icon = Icons.Default.Shield,
                accent = if (vpnState.state == VpnConnectionState.CONNECTED) StatusGreen else GoldDark,
                modifier = Modifier.weight(1f).fillMaxSize(),
                onClick = onOpenVpn
            )

            DashboardTile(
                title = "FAVORITES",
                subtitle = "$favoritesCount saved channels & movies",
                icon = Icons.Default.Favorite,
                accent = GoldLight,
                modifier = Modifier.weight(1f).fillMaxSize(),
                onClick = { onOpenSection(ContentSection.FAVORITES) }
            )
        }
    }
}

@Composable
private fun DashboardTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused) GoldLight else GoldMuted.copy(alpha = 0.55f)
    val borderWidth = if (isFocused) 2.dp else 1.dp

    Box(
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        if (isFocused) ElevatedDarkGray else CardDarkGray,
                        accent.copy(alpha = if (isFocused) 0.22f else 0.12f)
                    )
                )
            )
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceDarkGray)
                    .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}
