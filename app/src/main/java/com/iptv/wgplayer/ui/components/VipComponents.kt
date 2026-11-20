package com.iptv.wgplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptv.wgplayer.model.VpnConnectionState
import com.iptv.wgplayer.model.VpnUiState
import com.iptv.wgplayer.ui.theme.BgBlack
import com.iptv.wgplayer.ui.theme.BorderDarkGray
import com.iptv.wgplayer.ui.theme.CardDarkGray
import com.iptv.wgplayer.ui.theme.ElevatedDarkGray
import com.iptv.wgplayer.ui.theme.GoldDark
import com.iptv.wgplayer.ui.theme.GoldLight
import com.iptv.wgplayer.ui.theme.GoldMuted
import com.iptv.wgplayer.ui.theme.StatusGreen
import com.iptv.wgplayer.ui.theme.StatusRed
import com.iptv.wgplayer.ui.theme.SurfaceDarkGray
import com.iptv.wgplayer.ui.theme.TextMuted
import com.iptv.wgplayer.ui.theme.TextPrimary

@Composable
fun vipTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = SurfaceDarkGray,
    unfocusedContainerColor = SurfaceDarkGray,
    focusedBorderColor = GoldDark,
    unfocusedBorderColor = BorderDarkGray,
    focusedLabelColor = GoldLight,
    unfocusedLabelColor = TextMuted,
    cursorColor = GoldLight,
    focusedPlaceholderColor = TextMuted,
    unfocusedPlaceholderColor = TextMuted
)

@Composable
fun vipSwitchColors(): SwitchColors = SwitchDefaults.colors(
    checkedThumbColor = BgBlack,
    checkedTrackColor = GoldDark,
    uncheckedThumbColor = TextMuted,
    uncheckedTrackColor = ElevatedDarkGray,
    uncheckedBorderColor = BorderDarkGray
)

@Composable
fun VipModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) GoldDark.copy(alpha = 0.22f) else SurfaceDarkGray)
            .border(
                width = 1.dp,
                color = if (selected) GoldDark else BorderDarkGray,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            text = label,
            color = if (selected) GoldLight else TextMuted,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun VipBackButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(CardDarkGray)
            .border(1.dp, BorderDarkGray, CircleShape)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = GoldLight
        )
    }
}

@Composable
fun WarpStatusPill(
    vpnState: VpnUiState,
    onClick: () -> Unit
) {
    val dotColor = when (vpnState.state) {
        VpnConnectionState.CONNECTED -> StatusGreen
        VpnConnectionState.REGISTERING_WARP, VpnConnectionState.CONNECTING -> GoldLight
        VpnConnectionState.ERROR -> StatusRed
        VpnConnectionState.DISCONNECTED -> TextMuted
    }

    val label = when (vpnState.state) {
        VpnConnectionState.CONNECTED -> "WARP Protected"
        VpnConnectionState.REGISTERING_WARP -> "Fetching Key..."
        VpnConnectionState.CONNECTING -> "Connecting..."
        VpnConnectionState.ERROR -> "WARP Error"
        VpnConnectionState.DISCONNECTED -> "WARP Off"
    }

    val borderCol = when (vpnState.state) {
        VpnConnectionState.CONNECTED -> StatusGreen.copy(alpha = 0.6f)
        VpnConnectionState.ERROR -> StatusRed.copy(alpha = 0.6f)
        else -> GoldMuted
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(CardDarkGray)
            .border(1.dp, borderCol, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
