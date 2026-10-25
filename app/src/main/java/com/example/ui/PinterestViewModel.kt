package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DownloadEntity
import com.example.data.PinItem
import com.example.data.PinterestRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class UiNotification(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val isSuccess: Boolean = true,
    val isProgress: Boolean = false
)

data class UiState(
    val currentTab: Int = 0, // 0: Home, 1: Downloads, 2: More
    val searchQuery: String = "",
    val activeQuery: String = "",
    val hasSearched: Boolean = false,
    val searchSuggestions: List<String> = emptyList(),
    val isSuggestionsVisible: Boolean = false,
    val pins: List<PinItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isBulkDownloading: Boolean = false,
    val bulkProgress: Pair<Int, Int> = Pair(0, 0),
    val downloadedPinIds: Set<String> = emptySet(),
    val downloads: List<DownloadEntity> = emptyList(),
    val previewPin: PinItem? = null,
    val notification: UiNotification? = null
)

class PinterestViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("pinterest_dl_prefs", Context.MODE_PRIVATE)
    private val database = AppDatabase.getDatabase(application)
    private val repository = PinterestRepository(application, database.downloadDao())

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var currentOffset = 0
    private var bulkJob: Job? = null
    private var suggestionJob: Job? = null

    init {
        // Collect downloaded pins from Room database
        viewModelScope.launch {
            repository.downloadedPinIdsFlow.collectLatest { ids ->
                _uiState.value = _uiState.value.copy(downloadedPinIds = ids.toSet())
            }
        }

        viewModelScope.launch {
            repository.downloadsFlow.collectLatest { list ->
                _uiState.value = _uiState.value.copy(downloads = list)
            }
        }

        // Restore last search query if available
        val lastQuery = prefs.getString("last_search_query", null)
        if (!lastQuery.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                searchQuery = lastQuery,
                activeQuery = lastQuery,
                hasSearched = true
            )
            loadPins(query = lastQuery, isInitial = true)
        } else {
            _uiState.value = _uiState.value.copy(
                searchQuery = "",
                activeQuery = "",
                hasSearched = false
            )
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(currentTab = index, isSuggestionsVisible = false)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            isSuggestionsVisible = query.trim().isNotEmpty()
        )

        // Debounced search suggestions fetching (like YouTube)
        suggestionJob?.cancel()
        if (query.trim().length >= 2) {
            suggestionJob = viewModelScope.launch {
                delay(180)
                val suggestions = fetchAutocompleteSuggestions(query.trim())
                _uiState.value = _uiState.value.copy(searchSuggestions = suggestions)
            }
        } else {
            _uiState.value = _uiState.value.copy(searchSuggestions = emptyList())
        }
    }

    fun hideSuggestions() {
        _uiState.value = _uiState.value.copy(isSuggestionsVisible = false)
    }

    private suspend fun fetchAutocompleteSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://suggestqueries.google.com/complete/search?client=chrome&hl=en&q=$encoded"
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string().orEmpty()
                val jsonArray = JSONArray(body)
                val suggestionsArray = jsonArray.optJSONArray(1)
                val list = mutableListOf<String>()
                if (suggestionsArray != null) {
                    for (i in 0 until minOf(suggestionsArray.length(), 6)) {
                        list.add(suggestionsArray.getString(i))
                    }
                }
                return@withContext list
            }
        } catch (_: Exception) { }
        emptyList()
    }

    fun performSearch(query: String? = null) {
        val targetQuery = (query ?: _uiState.value.searchQuery).trim()
        if (targetQuery.isEmpty()) return

        prefs.edit().putString("last_search_query", targetQuery).apply()

        _uiState.value = _uiState.value.copy(
            searchQuery = targetQuery,
            activeQuery = targetQuery,
            hasSearched = true,
            isSuggestionsVisible = false,
            currentTab = 0
        )
        currentOffset = 0
        loadPins(query = targetQuery, isInitial = true)
    }

    fun loadMorePins() {
        if (_uiState.value.isLoading || _uiState.value.isLoadingMore) return
        val nextOffset = currentOffset + 50
        currentOffset = nextOffset
        loadPins(query = _uiState.value.activeQuery, isInitial = false)
    }

    private fun loadPins(query: String, isInitial: Boolean) {
        if (query.isBlank()) return

        viewModelScope.launch {
            if (isInitial) {
                _uiState.value = _uiState.value.copy(isLoading = true)
                currentOffset = 0
            } else {
                _uiState.value = _uiState.value.copy(isLoadingMore = true)
            }

            try {
                val newPins = repository.fetchImages(
                    query = query,
                    offset = currentOffset,
                    limit = 50
                )

                val updatedList = if (isInitial) {
                    newPins
                } else {
                    _uiState.value.pins + newPins
                }

                _uiState.value = _uiState.value.copy(
                    pins = updatedList,
                    isLoading = false,
                    isLoadingMore = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    notification = UiNotification(
                        message = "Search error: ${e.message}",
                        isSuccess = false
                    )
                )
            }
        }
    }

    fun downloadPin(pin: PinItem) {
        val notificationId = pin.id.hashCode()
        viewModelScope.launch {
            updatePinDownloadingState(pin.id, isDownloading = true)
            _uiState.value = _uiState.value.copy(
                notification = UiNotification(
                    message = "Downloading ${pin.title.take(24)}...",
                    isProgress = true
                )
            )

            NotificationHelper.showDownloadProgress(
                getApplication(),
                notificationId,
                pin.title,
                0,
                100
            )

            val result = repository.downloadPin(pin)
            updatePinDownloadingState(pin.id, isDownloading = false)

            result.onSuccess { entity ->
                _uiState.value = _uiState.value.copy(
                    notification = UiNotification(
                        message = "✓ Saved ${entity.fileName}",
                        isSuccess = true
                    )
                )
                NotificationHelper.showDownloadComplete(
                    getApplication(),
                    notificationId,
                    entity.fileName,
                    entity.fileUri
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    notification = UiNotification(
                        message = "Download failed: ${err.message}",
                        isSuccess = false
                    )
                )
            }
        }
    }

    fun startBulkDownload() {
        val pinsToDownload = _uiState.value.pins.filter {
            !_uiState.value.downloadedPinIds.contains(it.id)
        }

        if (pinsToDownload.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                notification = UiNotification(
                    message = "All visible pins already downloaded",
                    isSuccess = true
                )
            )
            return
        }

        bulkJob?.cancel()
        bulkJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBulkDownloading = true,
                bulkProgress = Pair(0, pinsToDownload.size),
                notification = UiNotification(
                    message = "Downloading ${pinsToDownload.size} pins...",
                    isProgress = true
                )
            )

            var successCount = 0
            for ((index, pin) in pinsToDownload.withIndex()) {
                _uiState.value = _uiState.value.copy(
                    bulkProgress = Pair(index + 1, pinsToDownload.size)
                )
                updatePinDownloadingState(pin.id, isDownloading = true)

                NotificationHelper.showDownloadProgress(
                    getApplication(),
                    9999,
                    "Bulk Download (${index + 1}/${pinsToDownload.size})",
                    index + 1,
                    pinsToDownload.size
                )

                val res = repository.downloadPin(pin)
                updatePinDownloadingState(pin.id, isDownloading = false)
                if (res.isSuccess) {
                    successCount++
                }
            }

            _uiState.value = _uiState.value.copy(
                isBulkDownloading = false,
                notification = UiNotification(
                    message = "Bulk complete! $successCount saved.",
                    isSuccess = true
                )
            )
        }
    }

    fun cancelBulkDownload() {
        bulkJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isBulkDownloading = false,
            notification = UiNotification(
                message = "Bulk download paused",
                isSuccess = false
            )
        )
    }

    private fun updatePinDownloadingState(pinId: String, isDownloading: Boolean) {
        val updated = _uiState.value.pins.map {
            if (it.id == pinId) it.copy(isDownloading = isDownloading) else it
        }
        _uiState.value = _uiState.value.copy(pins = updated)
    }

    fun setPreviewPin(pin: PinItem?) {
        _uiState.value = _uiState.value.copy(previewPin = pin)
    }

    fun deleteDownload(entity: DownloadEntity) {
        viewModelScope.launch {
            repository.deleteDownload(entity)
            _uiState.value = _uiState.value.copy(
                notification = UiNotification(
                    message = "Deleted ${entity.fileName}",
                    isSuccess = true
                )
            )
        }
    }

    fun clearAllDownloads() {
        viewModelScope.launch {
            repository.clearAllDownloads()
            _uiState.value = _uiState.value.copy(
                notification = UiNotification(
                    message = "Cleared download records",
                    isSuccess = true
                )
            )
        }
    }

    fun dismissNotification() {
        _uiState.value = _uiState.value.copy(notification = null)
    }
}
