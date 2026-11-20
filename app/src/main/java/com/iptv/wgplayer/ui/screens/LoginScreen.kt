package com.iptv.wgplayer.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptv.wgplayer.model.AccountConfig
import com.iptv.wgplayer.model.LoginMode
import com.iptv.wgplayer.model.VpnUiState
import com.iptv.wgplayer.ui.components.VipModeChip
import com.iptv.wgplayer.ui.components.WarpStatusPill
import com.iptv.wgplayer.ui.components.vipTextFieldColors
import com.iptv.wgplayer.ui.theme.BgBlack
import com.iptv.wgplayer.ui.theme.CardDarkGray
import com.iptv.wgplayer.ui.theme.GoldDark
import com.iptv.wgplayer.ui.theme.GoldLight
import com.iptv.wgplayer.ui.theme.GoldMuted
import com.iptv.wgplayer.ui.theme.StatusRed
import com.iptv.wgplayer.ui.theme.SurfaceDarkGray
import com.iptv.wgplayer.ui.theme.TextMuted

@Composable
fun LoginScreen(
    initialAccount: AccountConfig?,
    vpnState: VpnUiState,
    isLoading: Boolean,
    errorText: String,
    onSubmit: (AccountConfig) -> Unit,
    onOpenVpn: () -> Unit
) {
    var mode by remember { mutableStateOf(initialAccount?.mode ?: LoginMode.XTREAM) }
    var profileName by remember { mutableStateOf(initialAccount?.profileName ?: "VIP Server") }
    var serverUrl by remember { mutableStateOf(initialAccount?.serverUrl ?: "") }
    var username by remember { mutableStateOf(initialAccount?.username ?: "") }
    var password by remember { mutableStateOf(initialAccount?.password ?: "") }
    var m3uUrl by remember { mutableStateOf(initialAccount?.m3uUrl ?: "") }

    val fieldColors = vipTextFieldColors()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgBlack, SurfaceDarkGray)))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .background(CardDarkGray, RoundedCornerShape(22.dp))
                .border(1.dp, GoldMuted, RoundedCornerShape(22.dp))
                .padding(26.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VIP PLAYER",
                        color = GoldLight,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Encrypted WARP IPTV",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                WarpStatusPill(vpnState = vpnState, onClick = onOpenVpn)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VipModeChip(
                    label = "Xtream Codes API",
                    selected = mode == LoginMode.XTREAM,
                    onClick = { mode = LoginMode.XTREAM }
                )
                VipModeChip(
                    label = "M3U Playlist URL",
                    selected = mode == LoginMode.M3U,
                    onClick = { mode = LoginMode.M3U }
                )
            }

            OutlinedTextField(
                value = profileName,
                onValueChange = { profileName = it },
                label = { Text("Profile Name") },
                singleLine = true,
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (mode == LoginMode.XTREAM) {
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Server URL (http://host:port)") },
                    singleLine = true,
                    colors = fieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    colors = fieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = fieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = m3uUrl,
                    onValueChange = { m3uUrl = it },
                    label = { Text("M3U / M3U8 URL") },
                    singleLine = true,
                    colors = fieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (errorText.isNotBlank()) {
                Text(text = errorText, color = StatusRed, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    onSubmit(
                        AccountConfig(
                            profileName = profileName.ifBlank { "VIP Server" },
                            mode = mode,
                            serverUrl = serverUrl.trim(),
                            username = username.trim(),
                            password = password.trim(),
                            m3uUrl = m3uUrl.trim()
                        )
                    )
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(strokeWidth = 2.dp, color = BgBlack)
                } else {
                    Text(
                        text = "Connect & Load Playlist",
                        color = BgBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
