package com.example.data

data class PinItem(
    val id: String,
    val title: String,
    val url: String,
    val thumbnailUrl: String = url,
    val isDownloading: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadProgress: Float = 0f,
    val localUri: String? = null
)
