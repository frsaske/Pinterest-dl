package com.example.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

class DownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val pinId = inputData.getString(KEY_PIN_ID) ?: return Result.failure()
        val title = inputData.getString(KEY_TITLE) ?: "Pinterest_Pin"
        val url = inputData.getString(KEY_URL) ?: return Result.failure()
        val thumb = inputData.getString(KEY_THUMBNAIL) ?: url

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PinterestRepository(applicationContext, database.downloadDao())

        val pinItem = PinItem(
            id = pinId,
            title = title,
            url = url,
            thumbnailUrl = thumb
        )

        val downloadResult = repository.downloadPin(pinItem)
        return if (downloadResult.isSuccess) {
            val entity = downloadResult.getOrNull()
            Result.success(
                workDataOf(
                    "fileName" to (entity?.fileName ?: ""),
                    "filePath" to (entity?.filePath ?: "")
                )
            )
        } else {
            Result.retry()
        }
    }

    companion object {
        const val KEY_PIN_ID = "pin_id"
        const val KEY_TITLE = "title"
        const val KEY_URL = "url"
        const val KEY_THUMBNAIL = "thumbnail"
    }
}
