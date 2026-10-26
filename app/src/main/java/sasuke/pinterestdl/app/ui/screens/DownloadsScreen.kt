package sasuke.pinterestdl.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import sasuke.pinterestdl.app.data.DownloadEntity
import sasuke.pinterestdl.app.ui.theme.DarkBorder
import sasuke.pinterestdl.app.ui.theme.DarkGreyCard
import sasuke.pinterestdl.app.ui.theme.DarkGreySurface
import sasuke.pinterestdl.app.ui.theme.PitchBlack
import sasuke.pinterestdl.app.ui.theme.TextMuted
import sasuke.pinterestdl.app.ui.theme.TextSecondary
import sasuke.pinterestdl.app.ui.theme.TextWhite
import sasuke.pinterestdl.app.util.VibrationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DownloadsScreen(
    downloads: List<DownloadEntity>,
    onDeleteDownload: (DownloadEntity) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var initialGalleryIndex by remember { mutableStateOf<Int?>(null) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .statusBarsPadding()
    ) {
        // Downloads Top Bar matching YTDLnis header style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Downloads",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            if (downloads.isNotEmpty()) {
                IconButton(
                    onClick = {
                        VibrationHelper.vibrateClick(context, 20L)
                        showClearConfirmDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear All",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Subheader showing destination folder info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = TextWhite,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Pictures/PinterestDL • ${downloads.size} items",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (downloads.isEmpty()) {
            // High contrast Empty State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = DarkGreyCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.DownloadDone,
                            contentDescription = null,
                            tint = TextWhite.copy(alpha = 0.8f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No Downloads Yet",
                    color = TextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Downloaded pins are saved directly to Pictures/PinterestDL and will display here as a clean photo grid.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // 4-Column Photo Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 2.dp, bottom = 84.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("downloads_grid_4col")
            ) {
                itemsIndexed(
                    items = downloads,
                    key = { _, it -> it.id }
                ) { index, download ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.72f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkGreySurface)
                            .clickable {
                                VibrationHelper.vibrateClick(context, 20L)
                                initialGalleryIndex = index
                            }
                            .testTag("download_grid_item_${download.id}")
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(download.fileUri.ifEmpty { download.filePath })
                                .crossfade(true)
                                .build(),
                            contentDescription = download.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Swipeable Gallery Modal Viewer
        initialGalleryIndex?.let { startIndex ->
            if (downloads.isNotEmpty()) {
                DownloadedGalleryViewer(
                    downloads = downloads,
                    initialIndex = startIndex,
                    onDismiss = { initialGalleryIndex = null },
                    onDeleteDownload = { download ->
                        onDeleteDownload(download)
                        initialGalleryIndex = null
                    }
                )
            }
        }

        // Clear All Confirmation Dialog
        if (showClearConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmDialog = false },
                containerColor = DarkGreySurface,
                title = {
                    Text("Clear All Downloads?", color = TextWhite, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "This will clear the download records from the app history.",
                        color = TextSecondary,
                        fontSize = 13.5.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showClearConfirmDialog = false
                            onClearAll()
                        }
                    ) {
                        Text("Clear", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }
    }
}

/**
 * Immersive, full-screen swipeable gallery viewer for downloaded media.
 * Supports smooth horizontal swiping across all downloaded items with a clean white open button.
 */
@Composable
private fun DownloadedGalleryViewer(
    downloads: List<DownloadEntity>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    onDeleteDownload: (DownloadEntity) -> Unit
) {
    val context = LocalContext.current
    val safeIndex = initialIndex.coerceIn(0, (downloads.size - 1).coerceAtLeast(0))
    val pagerState = rememberPagerState(
        initialPage = safeIndex,
        pageCount = { downloads.size }
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
            // Horizontal Pager for smooth swipe gestures
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val download = downloads.getOrNull(page)
                if (download != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(download.fileUri.ifEmpty { download.filePath })
                                .crossfade(true)
                                .build(),
                            contentDescription = download.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Top Header: Close button and counter
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
                        text = "${pagerState.currentPage + 1} / ${downloads.size}",
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.size(40.dp))
            }

            // Bottom Actions: File caption and small white open/share buttons
            val currentDownload = downloads.getOrNull(pagerState.currentPage)
            if (currentDownload != null) {
                val formattedSize = Formatter.formatFileSize(context, currentDownload.fileSize)
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(Date(currentDownload.downloadDate))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Meta text
                    Text(
                        text = "${currentDownload.fileName} • $formattedSize • $formattedDate",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x77000000))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Small White Open Button
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color.White,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .height(44.dp)
                                .clickable {
                                    VibrationHelper.vibrateClick(context, 25L)
                                    openMediaFile(context, currentDownload)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Open",
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Open",
                                    color = Color.Black,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Share Button (Dark Grey Pill)
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = DarkGreySurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier
                                .height(44.dp)
                                .clickable {
                                    VibrationHelper.vibrateClick(context, 25L)
                                    shareMediaFile(context, currentDownload)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = TextWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Share",
                                    color = TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Delete Icon Button
                        Surface(
                            shape = CircleShape,
                            color = DarkGreySurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier
                                .size(44.dp)
                                .clickable {
                                    VibrationHelper.vibrateClick(context, 35L)
                                    onDeleteDownload(currentDownload)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = TextWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun openMediaFile(context: Context, download: DownloadEntity) {
    try {
        val uri = if (download.fileUri.isNotBlank()) {
            Uri.parse(download.fileUri)
        } else {
            Uri.parse(download.thumbnailUri)
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open Pin with"))
    } catch (_: Exception) { }
}

private fun shareMediaFile(context: Context, download: DownloadEntity) {
    try {
        val uri = if (download.fileUri.isNotBlank()) {
            Uri.parse(download.fileUri)
        } else {
            Uri.parse(download.thumbnailUri)
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, download.title)
            putExtra(Intent.EXTRA_TEXT, "Downloaded with PinterestDL: ${download.fileName}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Pin"))
    } catch (_: Exception) { }
}
