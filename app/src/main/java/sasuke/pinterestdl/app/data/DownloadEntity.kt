package sasuke.pinterestdl.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pinId: String,
    val title: String,
    val fileName: String,
    val filePath: String,
    val fileUri: String,
    val fileSize: Long,
    val downloadDate: Long = System.currentTimeMillis(),
    val thumbnailUri: String
)
