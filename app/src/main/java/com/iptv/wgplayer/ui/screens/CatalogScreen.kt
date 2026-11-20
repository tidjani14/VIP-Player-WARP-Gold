package com.iptv.wgplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iptv.wgplayer.model.CategoryItem
import com.iptv.wgplayer.model.ContentSection
import com.iptv.wgplayer.model.StreamItem
import com.iptv.wgplayer.model.VpnUiState
import com.iptv.wgplayer.ui.components.VipBackButton
import com.iptv.wgplayer.ui.components.WarpStatusPill
import com.iptv.wgplayer.ui.components.vipTextFieldColors
import com.iptv.wgplayer.ui.theme.BgBlack
import com.iptv.wgplayer.ui.theme.BorderDarkGray
import com.iptv.wgplayer.ui.theme.CardDarkGray
import com.iptv.wgplayer.ui.theme.ElevatedDarkGray
import com.iptv.wgplayer.ui.theme.GoldDark
import com.iptv.wgplayer.ui.theme.GoldLight
import com.iptv.wgplayer.ui.theme.StatusRed
import com.iptv.wgplayer.ui.theme.SurfaceDarkGray
import com.iptv.wgplayer.ui.theme.TextMuted
import com.iptv.wgplayer.ui.theme.TextPrimary

@Composable
fun CatalogScreen(
    section: ContentSection,
    vpnState: VpnUiState,
    categories: List<CategoryItem>,
    selectedCategoryId: String,
    streams: List<StreamItem>,
    favoriteIds: Set<String>,
    isLoading: Boolean,
    errorText: String,
    onSelectCategory: (String) -> Unit,
    onPlayStream: (StreamItem, List<StreamItem>) -> Unit,
    onToggleFavorite: (StreamItem) -> Unit,
    onOpenVpn: () -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredStreams = remember(streams, searchQuery) {
        if (searchQuery.isBlank()) streams
        else streams.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VipBackButton(onClick = onBack)

            Column {
                Text(
                    text = section.title,
                    color = GoldLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${filteredStreams.size} items",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search channels or titles...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = GoldDark)
                },
                singleLine = true,
                colors = vipTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )

            WarpStatusPill(vpnState = vpnState, onClick = onOpenVpn)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .width(210.dp)
                    .fillMaxHeight()
                    .background(SurfaceDarkGray, RoundedCornerShape(16.dp))
                    .border(1.dp, BorderDarkGray, RoundedCornerShape(16.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(categories, key = { it.id }) { cat ->
                    val selected = cat.id == selectedCategoryId
                    var focused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focused = it.isFocused }
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when {
                                    selected -> GoldDark.copy(alpha = 0.24f)
                                    focused -> ElevatedDarkGray
                                    else -> CardDarkGray
                                }
                            )
                            .border(
                                width = if (focused || selected) 1.5.dp else 1.dp,
                                color = when {
                                    focused -> GoldLight
                                    selected -> GoldDark
                                    else -> BorderDarkGray
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectCategory(cat.id) }
                            .padding(horizontal = 12.dp, vertical = 11.dp)
                    ) {
                        Text(
                            text = cat.name,
                            color = if (selected || focused) GoldLight else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(SurfaceDarkGray, RoundedCornerShape(16.dp))
                    .border(1.dp, BorderDarkGray, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            color = GoldLight,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    errorText.isNotBlank() -> {
                        Text(
                            text = errorText,
                            color = StatusRed,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    filteredStreams.isEmpty() -> {
                        Text(
                            text = "No channels or videos found.",
                            color = TextMuted,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredStreams, key = { "${it.section}_${it.id}" }) { item ->
                                val isFav = favoriteIds.contains("${item.section}_${item.id}")
                                var rowFocused by remember { mutableStateOf(false) }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged { rowFocused = it.isFocused }
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (rowFocused) ElevatedDarkGray else CardDarkGray)
                                        .border(
                                            width = if (rowFocused) 1.5.dp else 1.dp,
                                            color = if (rowFocused) GoldLight else BorderDarkGray,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onPlayStream(item, filteredStreams) }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(BgBlack)
                                            .border(1.dp, BorderDarkGray, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (item.iconUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = item.iconUrl,
                                                contentDescription = item.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.LiveTv,
                                                contentDescription = null,
                                                tint = GoldDark,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            color = if (rowFocused) GoldLight else TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (item.rating.isNotBlank()) {
                                            Text(
                                                text = "Rating: ${item.rating}",
                                                color = GoldDark,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    IconButton(onClick = { onToggleFavorite(item) }) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (isFav) GoldLight else TextMuted
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = GoldDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
