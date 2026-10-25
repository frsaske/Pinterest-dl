package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.PinterestViewModel
import com.example.ui.components.PinterestTopBar
import com.example.ui.components.YtdlnisBottomNavBar
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.theme.PinterestDLTheme
import com.example.ui.theme.PitchBlack
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.init(applicationContext)

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

    // Request notification permission on Android 13+ for real system notifications
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { _ -> }

        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Back handler: return to Home tab if on sub-tab
    BackHandler(enabled = uiState.currentTab != 0) {
        viewModel.selectTab(0)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
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
        }
    }
}
