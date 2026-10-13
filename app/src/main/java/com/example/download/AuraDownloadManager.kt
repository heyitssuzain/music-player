package com.example.download

import android.content.Context
import com.example.data.local.AuraDao
import com.example.data.local.DownloadEntity
import com.example.data.local.TrackEntity
import com.example.data.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

class AuraDownloadManager(
    private val context: Context,
    private val auraDao: AuraDao,
    private val scope: CoroutineScope
) {
    private val client = OkHttpClient()
    private val activeJobs = ConcurrentHashMap<String, Job>()

    val downloadsFlow: Flow<List<DownloadEntity>> = auraDao.getAllDownloads()

    private val downloadDir: File by lazy {
        val dir = File(context.filesDir, "audio_downloads")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    fun downloadTrack(track: Track) {
        if (activeJobs.containsKey(track.id)) return

        val job = scope.launch(Dispatchers.IO) {
            val safeFileName = "aura_${track.id.replace("[^a-zA-Z0-9]".toRegex(), "_")}.mp3"
            val targetFile = File(downloadDir, safeFileName)

            val downloadRecord = DownloadEntity(
                trackId = track.id,
                title = track.title,
                artist = track.artist,
                downloadUrl = track.mediaUri,
                localFilePath = targetFile.absolutePath,
                status = "DOWNLOADING",
                progress = 0.05f
            )
            auraDao.insertOrUpdateDownload(downloadRecord)

            if (track.mediaUri.startsWith("file:") || !track.mediaUri.startsWith("http")) {
                val srcFile = try {
                    if (track.mediaUri.startsWith("file:")) File(java.net.URI(track.mediaUri))
                    else File(track.mediaUri)
                } catch (e: Exception) {
                    null
                }
                if (srcFile != null && srcFile.exists()) {
                    srcFile.copyTo(targetFile, overwrite = true)
                    auraDao.insertOrUpdateDownload(
                        downloadRecord.copy(
                            totalBytes = srcFile.length(),
                            downloadedBytes = srcFile.length(),
                            progress = 1.0f,
                            status = "COMPLETED",
                            completedAt = System.currentTimeMillis()
                        )
                    )
                    val offlineTrack = track.copy(
                        mediaUri = targetFile.absolutePath,
                        isLocal = true,
                        sourceProvider = "download"
                    )
                    auraDao.insertTrack(TrackEntity.fromDomainTrack(offlineTrack))
                    activeJobs.remove(track.id)
                    return@launch
                }
            }

            try {
                val request = Request.Builder().url(track.mediaUri).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful || response.body == null) {
                    auraDao.insertOrUpdateDownload(
                        downloadRecord.copy(status = "FAILED", progress = 0f)
                    )
                    return@launch
                }

                val body = response.body!!
                val contentLength = body.contentLength()
                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(targetFile)

                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    val progress = if (contentLength > 0) {
                        (totalBytesRead.toFloat() / contentLength).coerceIn(0f, 1f)
                    } else {
                        0.5f
                    }
                    auraDao.insertOrUpdateDownload(
                        downloadRecord.copy(
                            totalBytes = contentLength,
                            downloadedBytes = totalBytesRead,
                            progress = progress,
                            status = "DOWNLOADING"
                        )
                    )
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                // Mark completed and insert into local tracks
                auraDao.insertOrUpdateDownload(
                    downloadRecord.copy(
                        totalBytes = totalBytesRead,
                        downloadedBytes = totalBytesRead,
                        progress = 1.0f,
                        status = "COMPLETED",
                        completedAt = System.currentTimeMillis()
                    )
                )

                val offlineTrack = track.copy(
                    mediaUri = targetFile.absolutePath,
                    isLocal = true,
                    sourceProvider = "download"
                )
                auraDao.insertTrack(TrackEntity.fromDomainTrack(offlineTrack))

            } catch (e: Exception) {
                if (targetFile.exists()) targetFile.delete()
                auraDao.insertOrUpdateDownload(
                    downloadRecord.copy(status = "FAILED", progress = 0f)
                )
            } finally {
                activeJobs.remove(track.id)
            }
        }
        activeJobs[track.id] = job
    }

    fun cancelDownload(trackId: String) {
        activeJobs[trackId]?.cancel()
        activeJobs.remove(trackId)
        scope.launch(Dispatchers.IO) {
            val record = auraDao.getDownload(trackId)
            if (record != null) {
                record.localFilePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) file.delete()
                }
                auraDao.insertOrUpdateDownload(record.copy(status = "CANCELLED", progress = 0f))
            }
        }
    }

    suspend fun getStorageUsageBytes(): Long = withContext(Dispatchers.IO) {
        var total = 0L
        downloadDir.listFiles()?.forEach { file ->
            if (file.isFile) total += file.length()
        }
        total
    }

    fun formatBytes(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return "%.1f MB".format(mb)
    }
}
