package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.PinItem
import com.example.ui.UiState
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkGreyCard
import com.example.ui.theme.DarkGreySurface
import com.example.ui.theme.DarkPillHighlight
import com.example.ui.theme.DownloadSuccessGreen
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PinterestRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite

@Composable
fun HomeScreen(
    uiState: UiState,
    onDownloadPin: (PinItem) -> Unit,
    onStartBulkDownload: () -> Unit,
    onCancelBulkDownload: () -> Unit,
    onLoadMore: () -> Unit,
    onPreviewPin: (PinItem?) -> Unit,
    onSelectSuggestion: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()

    // Detect when scrolled near the bottom to trigger Infinite Scroll (next 50 items)
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 8
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !uiState.isLoading && !uiState.isLoadingMore && uiState.pins.isNotEmpty()) {
            onLoadMore()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Bulk Download Bar (only shown when results exist)
            if (uiState.pins.isNotEmpty()) {
                BulkDownloadHeader(
                    pinsCount = uiState.pins.size,
                    isBulkDownloading = uiState.isBulkDownloading,
                    bulkProgress = uiState.bulkProgress,
                    activeQuery = uiState.activeQuery,
                    onStartBulk = onStartBulkDownload,
                    onCancelBulk = onCancelBulkDownload
                )
            }

            // Screen Content switching between First-Time/Discover, Searching Animation, and 4-Col Grid
            when {
                // 1. Searching Animation UI
                uiState.isLoading -> {
                    SearchingAnimationView(query = uiState.searchQuery.ifEmpty { uiState.activeQuery })
                }

                // 2. First Run / Empty Search Hero Animation UI
                !uiState.hasSearched && uiState.pins.isEmpty() -> {
                    DiscoverEmptyAnimationView(onSelectSuggestion = onSelectSuggestion)
                }

                // 3. Search returned 0 results
                uiState.pins.isEmpty() -> {
                    NoResultsView(query = uiState.activeQuery, onSelectSuggestion = onSelectSuggestion)
                }

                // 4. Results 4-Column Grid
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        state = gridState,
                        contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 84.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("pins_grid_4col")
                    ) {
                        items(
                            items = uiState.pins,
                            key = { it.id }
                        ) { pin ->
                            val isDownloaded = uiState.downloadedPinIds.contains(pin.id)
                            PinGridItem(
                                pin = pin,
                                isDownloaded = isDownloaded,
                                onItemClick = { onPreviewPin(pin) },
                                onDownloadClick = { onDownloadPin(pin) }
                            )
                        }

                        // Bottom Infinite Scroll Loading Wave
                        if (uiState.isLoadingMore) {
                            item(span = { GridItemSpan(4) }) {
                                BottomLoadingIndicator()
                            }
                        }
                    }
                }
            }
        }

        // Full Screen Preview Dialog
        uiState.previewPin?.let { pin ->
            PinDetailDialog(
                pin = pin,
                isDownloaded = uiState.downloadedPinIds.contains(pin.id),
                onDismiss = { onPreviewPin(null) },
                onDownload = {
                    onDownloadPin(pin)
                }
            )
        }
    }
}

/**
 * Cool animated first-time search hero with pulsing rings and quick suggestion tags.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiscoverEmptyAnimationView(
    onSelectSuggestion: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Animated Search Center
        Box(
            modifier = Modifier
                .size(110.dp)
                .scale(pulseScale),
            contentAlignment = Alignment.Center
        ) {
            // Outer glowing ring
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(PinterestRed.copy(alpha = ringAlpha))
            )
            // Mid ring
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF28282E))
            )
            // Inner icon
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = PinterestRed,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Try to Search Something",
            color = TextWhite,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Type any keyword or paste a Pinterest URL above to start discovering high-res media",
            color = TextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick suggestions header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = PinterestRed,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Popular Searches",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Suggestion Chips
        val suggestions = listOf(
            "Sasuke", "Naruto 4K", "Anime Wallpapers",
            "Cyberpunk Samurai", "Dark Aesthetic", "Minimalist Art",
            "Retro Cars", "Pixel Art Horizon"
        )

        FlowRow(
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            suggestions.forEach { tag ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = DarkGreyCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable { onSelectSuggestion(tag) }
                ) {
                    Text(
                        text = tag,
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Smooth searching animation with pulsing indicator.
 */
@Composable
private fun SearchingAnimationView(query: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "searchLoader")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scaleAnim)
        ) {
            CircularProgressIndicator(
                color = PinterestRed,
                strokeWidth = 3.dp,
                modifier = Modifier.size(46.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Searching Pinterest for \"$query\"...",
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Fetching 50 high-resolution pins",
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Infinite scroll bottom loading wave indicator.
 */
@Composable
private fun BottomLoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = PinterestRed,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Loading next 50 pins...",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun NoResultsView(query: String, onSelectSuggestion: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No pins found for \"$query\"",
            color = TextWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Try another search term or pick from trending tags below:",
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Sasuke", "Naruto", "Cyberpunk").forEach { tag ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkGreyCard,
                    modifier = Modifier.clickable { onSelectSuggestion(tag) }
                ) {
                    Text(
                        text = tag,
                        color = PinterestRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BulkDownloadHeader(
    pinsCount: Int,
    isBulkDownloading: Boolean,
    bulkProgress: Pair<Int, Int>,
    activeQuery: String,
    onStartBulk: () -> Unit,
    onCancelBulk: () -> Unit
) {
    Surface(
        color = DarkGreySurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (activeQuery.isNotBlank()) "Search: $activeQuery" else "Pinterest Stream",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$pinsCount pins loaded",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                if (isBulkDownloading) {
                    Button(
                        onClick = onCancelBulk,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A1E24)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("cancel_bulk_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = PinterestRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Stop (${bulkProgress.first}/${bulkProgress.second})",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onStartBulk,
                        colors = ButtonDefaults.buttonColors(containerColor = PinterestRed),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        enabled = pinsCount > 0,
                        modifier = Modifier.testTag("bulk_download_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Bulk Download",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bulk Download",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isBulkDownloading) {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    val progressRatio = if (bulkProgress.second > 0) {
                        bulkProgress.first.toFloat() / bulkProgress.second
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progressRatio },
                        color = PinterestRed,
                        trackColor = DarkPillHighlight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Downloading ${bulkProgress.first} of ${bulkProgress.second} to Pictures/PinterestDL...",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PinGridItem(
    pin: PinItem,
    isDownloaded: Boolean,
    onItemClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.72f)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkGreySurface)
            .clickable { onItemClick() }
            .testTag("pin_card_${pin.id}")
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(pin.thumbnailUrl)
                .crossfade(true)
                .build(),
            contentDescription = pin.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom gradient for download button visibility
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(34.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC000000))
                    )
                )
        )

        // Bottom right download icon
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(3.dp)
        ) {
            when {
                pin.isDownloading -> {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xEE000000),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = PinterestRed,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
                isDownloaded -> {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xEE1E4620),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Downloaded",
                                tint = DownloadSuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                else -> {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xDD000000),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDownloadClick() }
                            .testTag("download_icon_${pin.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Pin",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinDetailDialog(
    pin: PinItem,
    isDownloaded: Boolean,
    onDismiss: () -> Unit,
    onDownload: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = PitchBlack,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pin Preview",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkGreySurface),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(pin.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = pin.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = pin.title,
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                val safeTitle = pin.title.trim().replace("\\s+".toRegex(), "_").take(50)
                Text(
                    text = "Save: Sasuke-$safeTitle.jpg",
                    color = PinterestRed,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Location: Pictures/PinterestDL",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onDownload()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PinterestRed),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dialog_download_button")
                ) {
                    Icon(
                        imageVector = if (isDownloaded) Icons.Default.Check else Icons.Default.Download,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isDownloaded) "Download Again" else "Download to Gallery",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
