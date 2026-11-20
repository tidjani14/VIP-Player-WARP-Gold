package com.iptv.wgplayer.ui.screens

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.iptv.wgplayer.model.StreamItem
import com.iptv.wgplayer.model.VpnConnectionState
import com.iptv.wgplayer.model.VpnUiState
import com.iptv.wgplayer.player.Media3PlayerFactory
import com.iptv.wgplayer.ui.components.VipBackButton
import com.iptv.wgplayer.ui.theme.BgBlack
import com.iptv.wgplayer.ui.theme.CardDarkGray
import com.iptv.wgplayer.ui.theme.GoldDark
import com.iptv.wgplayer.ui.theme.GoldLight
import com.iptv.wgplayer.ui.theme.GoldMuted
import com.iptv.wgplayer.ui.theme.StatusGreen
import com.iptv.wgplayer.ui.theme.StatusRed
import com.iptv.wgplayer.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    stream: StreamItem,
    playlist: List<StreamItem>,
    vpnState: VpnUiState,
    onSwitchStream: (StreamItem) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val player = remember { Media3PlayerFactory.create(context) }

    var showControls by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var playbackError by remember { mutableStateOf("") }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    playbackError = ""
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                playbackError = error.localizedMessage ?: "Stream playback error"
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.stop()
            player.release()
        }
    }

    LaunchedEffect(stream.directUrl) {
        playbackError = ""
        isBuffering = true
        player.setMediaItem(MediaItem.fromUri(stream.directUrl))
        player.prepare()
        player.playWhenReady = true
    }

    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000L)
            showControls = false
        }
    }

    val currentIndex = playlist.indexOfFirst { it.id == stream.id }
    val aspectLabel = when (resizeMode) {
        AspectRatioFrameLayout.RESIZE_MODE_FILL -> "FILL"
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> "ZOOM"
        else -> "FIT"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { showControls = !showControls }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    keepScreenOn = true
                    this.resizeMode = resizeMode
                }
            },
            update = { view ->
                view.resizeMode = resizeMode
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isBuffering && playbackError.isBlank()) {
            CircularProgressIndicator(
                color = GoldLight,
                strokeWidth = 3.dp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (playbackError.isNotBlank()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(CardDarkGray.copy(alpha = 0.94f), RoundedCornerShape(16.dp))
                    .border(1.dp, StatusRed, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = playbackError, color = StatusRed, fontSize = 14.sp)
                Button(
                    onClick = {
                        playbackError = ""
                        isBuffering = true
                        player.prepare()
                        player.playWhenReady = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldDark)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = BgBlack)
                    Text(" Retry Stream", color = BgBlack, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showControls) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VipBackButton(onClick = onBack)
                    Text(
                        text = stream.name,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (vpnState.state == VpnConnectionState.CONNECTED) {
                    Text(
                        text = "WARP ENCRYPTED",
                        color = StatusGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(CardDarkGray.copy(alpha = 0.8f), RoundedCornerShape(50))
                            .border(1.dp, StatusGreen.copy(alpha = 0.6f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = {
                        if (currentIndex > 0) onSwitchStream(playlist[currentIndex - 1])
                    },
                    enabled = currentIndex > 0
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = TextPrimary)
                }

                IconButton(
                    onClick = {
                        if (player.isPlaying) {
                            player.pause()
                        } else {
                            player.play()
                        }
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(GoldDark)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = BgBlack,
                        modifier = Modifier.size(30.dp)
                    )
                }

                IconButton(
                    onClick = {
                        if (currentIndex in 0 until playlist.lastIndex) {
                            onSwitchStream(playlist[currentIndex + 1])
                        }
                    },
                    enabled = currentIndex in 0 until playlist.lastIndex
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = TextPrimary)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(CardDarkGray.copy(alpha = 0.85f))
                        .border(1.dp, GoldMuted, RoundedCornerShape(50))
                        .clickable {
                            resizeMode = when (resizeMode) {
                                AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                AspectRatioFrameLayout.RESIZE_MODE_FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AspectRatio,
                        contentDescription = "Aspect Ratio",
                        tint = GoldLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(text = aspectLabel, color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
