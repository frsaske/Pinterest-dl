package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.PinterestViewModel
import com.example.ui.UiNotification
import com.example.ui.components.PinterestTopBar
import com.example.ui.components.YtdlnisBottomNavBar
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.theme.DownloadSuccessGreen
import com.example.ui.theme.PinterestDLTheme
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PinterestRed
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PinterestDLTheme {
                PinterestAppRoot()
            }
        }
    }
}

@Composable
fun PinterestAppRoot(
    viewModel: PinterestViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Back handler: return to Home tab if on sub-tab
    BackHandler(enabled = uiState.currentTab != 0) {
        viewModel.selectTab(0)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // Handled explicitly with statusBarsPadding to prevent overlapping
        bottomBar = {
            YtdlnisBottomNavBar(
                selectedTab = uiState.currentTab,
                onTabSelected = { tabIndex -> viewModel.selectTab(tabIndex) },
                downloadsBadgeCount = uiState.downloads.size,
                modifier = Modifier
                    .background(PitchBlack)
                    .navigationBarsPadding()
            )
        },
        containerColor = PitchBlack
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PitchBlack)
        ) {
            when (uiState.currentTab) {
                0 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        PinterestTopBar(
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                            onSearchSubmitted = { query -> viewModel.performSearch(query) },
                            onQuickFilterSelected = { filter -> viewModel.performSearch(filter) },
                            suggestions = uiState.searchSuggestions,
                            isSuggestionsVisible = uiState.isSuggestionsVisible,
                            onSuggestionClicked = { suggestion -> viewModel.performSearch(suggestion) }
                        )

                        HomeScreen(
                            uiState = uiState,
                            onDownloadPin = { pin -> viewModel.downloadPin(pin) },
                            onStartBulkDownload = { viewModel.startBulkDownload() },
                            onCancelBulkDownload = { viewModel.cancelBulkDownload() },
                            onLoadMore = { viewModel.loadMorePins() },
                            onPreviewPin = { pin -> viewModel.setPreviewPin(pin) },
                            onSelectSuggestion = { tag -> viewModel.performSearch(tag) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                1 -> {
                    DownloadsScreen(
                        downloads = uiState.downloads,
                        onDeleteDownload = { entity -> viewModel.deleteDownload(entity) },
                        onClearAll = { viewModel.clearAllDownloads() }
                    )
                }
                2 -> {
                    MoreScreen(
                        onClearHistory = { viewModel.clearAllDownloads() }
                    )
                }
            }

            // Sleek Floating Toast Notification (Auto-dismisses & tap-to-dismiss)
            uiState.notification?.let { notif ->
                FloatingNotificationPill(
                    notification = notif,
                    onDismiss = { viewModel.dismissNotification() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 76.dp)
                )
            }
        }
    }
}

@Composable
private fun FloatingNotificationPill(
    notification: UiNotification,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-dismiss completed notifications after 2.5 seconds
    LaunchedEffect(notification.id) {
        if (!notification.isProgress) {
            delay(2500)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E1E22),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E34)),
            shadowElevation = 8.dp,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clickable { onDismiss() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                when {
                    notification.isProgress -> {
                        CircularProgressIndicator(
                            color = PinterestRed,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    notification.isSuccess -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = DownloadSuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFFFB74D),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = notification.message,
                    color = TextWhite,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0xFF888890),
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onDismiss() }
                )
            }
        }
    }
}
