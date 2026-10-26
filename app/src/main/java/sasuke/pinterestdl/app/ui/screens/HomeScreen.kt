package sasuke.pinterestdl.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import sasuke.pinterestdl.app.data.PinItem
import sasuke.pinterestdl.app.ui.UiState
import sasuke.pinterestdl.app.ui.theme.DarkBorder
import sasuke.pinterestdl.app.ui.theme.DarkGreyCard
import sasuke.pinterestdl.app.ui.theme.DarkGreySurface
import sasuke.pinterestdl.app.ui.theme.DownloadSuccessGreen
import sasuke.pinterestdl.app.ui.theme.PitchBlack
import sasuke.pinterestdl.app.ui.theme.TextMuted
import sasuke.pinterestdl.app.ui.theme.TextSecondary
import sasuke.pinterestdl.app.ui.theme.TextWhite
import sasuke.pinterestdl.app.ui.theme.YtdlnisBlue
import sasuke.pinterestdl.app.util.VibrationHelper

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
    val context = LocalContext.current
    val gridState = rememberLazyGridState()
    var galleryInitialIndex by remember { mutableStateOf<Int?>(null) }

    // Detect when user approaches the bottom to auto-fetch next 50 pins
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
        when {
            // 1. Searching indicator
            uiState.isLoading -> {
                SearchingAnimationView(query = uiState.searchQuery.ifEmpty { uiState.activeQuery })
            }

            // 2. Discover/Home before searching
            !uiState.hasSearched && uiState.pins.isEmpty() -> {
                DiscoverHomeView(onSelectSuggestion = onSelectSuggestion)
            }

            // 3. No results returned
            uiState.pins.isEmpty() -> {
                NoResultsView(query = uiState.activeQuery, onSelectSuggestion = onSelectSuggestion)
            }

            // 4. 4-Column Photo Grid
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    state = gridState,
                    contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("pins_grid_4col")
                ) {
                    itemsIndexed(
                        items = uiState.pins,
                        key = { _, item -> item.id }
                    ) { index, pin ->
                        val isDownloaded = uiState.downloadedPinIds.contains(pin.id)
                        PinGridItem(
                            pin = pin,
                            index = index,
                            isDownloaded = isDownloaded,
                            onItemClick = {
                                galleryInitialIndex = index
                                onPreviewPin(pin)
                            },
                            onDownloadClick = {
                                VibrationHelper.vibrateClick(context, 35L)
                                onDownloadPin(pin)
                            }
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

        // Floating "Download all" Blue Pill FAB (High elevation and visibility)
        if (uiState.pins.isNotEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 20.dp)
                    .zIndex(20f)
            ) {
                if (uiState.isBulkDownloading) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = DarkGreyCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, TextSecondary),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .clickable {
                                VibrationHelper.vibrateClick(context, 35L)
                                onCancelBulkDownload()
                            }
                            .testTag("cancel_bulk_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Stop (${uiState.bulkProgress.first}/${uiState.bulkProgress.second})",
                                color = TextWhite,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = YtdlnisBlue,
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .clickable {
                                VibrationHelper.vibrateClick(context, 40L)
                                onStartBulkDownload()
                            }
                            .testTag("download_all_fab")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Download all",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Download all",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }
                }
            }
        }

        // Full Screen Gallery Slide Viewer (HorizontalPager native gallery experience)
        val activeGalleryIndex = galleryInitialIndex ?: uiState.previewPin?.let { pin ->
            uiState.pins.indexOfFirst { it.id == pin.id }.takeIf { it >= 0 }
        }

        if (activeGalleryIndex != null && uiState.pins.isNotEmpty()) {
            PinGalleryViewer(
                pins = uiState.pins,
                initialIndex = activeGalleryIndex,
                downloadedPinIds = uiState.downloadedPinIds,
                onDismiss = {
                    galleryInitialIndex = null
                    onPreviewPin(null)
                },
                onDownloadPin = { pin ->
                    VibrationHelper.vibrateClick(context, 35L)
                    onDownloadPin(pin)
                }
            )
        }
    }
}

/**
 * High contrast Discover screen with smooth tags.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiscoverHomeView(
    onSelectSuggestion: (String) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = DarkGreyCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Search Pinterest",
            color = TextWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Enter any topic or paste a URL to explore and download high-resolution media",
            color = TextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(26.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = TextWhite,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Popular Searches",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

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
                        .clickable {
                            VibrationHelper.vibrateClick(context, 25L)
                            onSelectSuggestion(tag)
                        }
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
 * Minimalist, high-contrast searching view.
 */
@Composable
private fun SearchingAnimationView(query: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Searching for \"$query\"...",
                color = TextWhite,
                fontSize = 14.5.sp,
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
                color = Color.White,
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
    val context = LocalContext.current

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
            text = "Try another search term or pick from trending tags:",
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.clickable {
                        VibrationHelper.vibrateClick(context, 25L)
                        onSelectSuggestion(tag)
                    }
                ) {
                    Text(
                        text = tag,
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * 4-column item with smooth slide/fade entrance animation and quick download button.
 */
@Composable
private fun PinGridItem(
    pin: PinItem,
    index: Int,
    isDownloaded: Boolean,
    onItemClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    val context = LocalContext.current

    // Subtle slide and fade entrance animation
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 220)) +
                slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                )
    ) {
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

            // Bottom dark gradient overlay for download icon legibility
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

            // Download Icon Button at bottom right
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
                                    color = Color.White,
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
}

@Composable
fun PinGalleryViewer(
    pins: List<PinItem>,
    initialIndex: Int,
    downloadedPinIds: Set<String>,
    onDismiss: () -> Unit,
    onDownloadPin: (PinItem) -> Unit
) {
    val context = LocalContext.current
    val safeInitialPage = initialIndex.coerceIn(0, (pins.size - 1).coerceAtLeast(0))
    val pagerState = rememberPagerState(
        initialPage = safeInitialPage,
        pageCount = { pins.size }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PitchBlack)
        ) {
            // Horizontal Pager for smooth, native gallery swipe transitions
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pin = pins.getOrNull(page)
                if (pin != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp),
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
                }
            }

            // Top Header: Close button and page counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x66000000),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${pins.size}",
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.size(40.dp))
            }

            // Bottom Actions: Sleek small white download pill button & title
            val currentPin = pins.getOrNull(pagerState.currentPage)
            if (currentPin != null) {
                val isDownloaded = downloadedPinIds.contains(currentPin.id)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentPin.title,
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x77000000))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Small White Download Button (user requested clean white button, no red)
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .height(44.dp)
                            .clickable {
                                VibrationHelper.vibrateClick(context, 35L)
                                onDownloadPin(currentPin)
                            }
                            .testTag("gallery_download_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Default.Check else Icons.Default.ArrowDownward,
                                contentDescription = if (isDownloaded) "Downloaded" else "Download",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isDownloaded) "Downloaded" else "Download",
                                color = Color.Black,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
