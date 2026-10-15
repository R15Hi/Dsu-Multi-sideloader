package vegabobo.dsusideloader.core.gsirepo

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import vegabobo.dsusideloader.model.gsirepo.GsiAsset
import vegabobo.dsusideloader.model.gsirepo.GsiDownloadProgress
import vegabobo.dsusideloader.model.gsirepo.GsiDownloadState

class GsiDownloader(private val context: Context) {

    private val tag = "GsiDownloader"
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val downloadSubfolder = "Multi-DSU-Sideloader"

    fun getTargetDirectory(): File {
        val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val targetDir = File(publicDownloads, downloadSubfolder)
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        return targetDir
    }

    fun getLocalFileForAsset(assetName: String): File {
        return File(getTargetDirectory(), assetName)
    }

    /**
     * Checks if a GSI image has been completely downloaded and is valid for installation.
     */
    fun isAssetFullyDownloaded(assetName: String): Boolean {
        val file = getLocalFileForAsset(assetName)
        // Must exist, be a regular file, and be larger than 10MB to be a complete image/archive
        return file.exists() && file.isFile && file.length() > 10L * 1024L * 1024L
    }

    fun isAssetDownloaded(assetName: String): Boolean {
        return isAssetFullyDownloaded(assetName)
    }

    fun cancelDownload(downloadId: Long, assetName: String) {
        try {
            if (downloadId > 0) {
                downloadManager.remove(downloadId)
            }
            val targetFile = getLocalFileForAsset(assetName)
            if (targetFile.exists()) {
                targetFile.delete()
            }
            Log.d(tag, "Cancelled download ID: $downloadId, purged file for $assetName")
        } catch (e: Exception) {
            Log.e(tag, "Failed to cancel download", e)
        }
    }

    fun enqueueDownload(asset: GsiAsset): Long {
        val targetFile = getLocalFileForAsset(asset.name)
        if (targetFile.exists()) {
            targetFile.delete()
        }

        val request = DownloadManager.Request(Uri.parse(asset.downloadUrl))
            .setTitle("Downloading GSI: ${asset.name}")
            .setDescription("${asset.architecture} • ${asset.variant}")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$downloadSubfolder/${asset.name}")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val id = downloadManager.enqueue(request)
        Log.d(tag, "Enqueued download ID: $id for ${asset.name}")
        return id
    }

    fun monitorDownload(downloadId: Long, assetName: String): Flow<GsiDownloadProgress> = flow {
        var downloading = true
        while (downloading) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor != null && cursor.moveToFirst()) {
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = if (statusIndex != -1) cursor.getInt(statusIndex) else -1

                val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val totalBytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                val bytesDownloaded = if (bytesDownloadedIndex != -1) cursor.getLong(bytesDownloadedIndex) else 0L
                val totalBytes = if (totalBytesIndex != -1) cursor.getLong(totalBytesIndex) else -1L

                val progress = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt().coerceIn(0, 100) else 0

                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        val localFile = getLocalFileForAsset(assetName)
                        if (isAssetFullyDownloaded(assetName)) {
                            emit(
                                GsiDownloadProgress(
                                    assetName = assetName,
                                    downloadId = downloadId,
                                    state = GsiDownloadState.DOWNLOADED,
                                    progress = 100,
                                    localUri = Uri.fromFile(localFile).toString(),
                                    localFilePath = localFile.absolutePath,
                                ),
                            )
                        } else {
                            emit(
                                GsiDownloadProgress(
                                    assetName = assetName,
                                    downloadId = downloadId,
                                    state = GsiDownloadState.FAILED,
                                    error = "Downloaded file is incomplete or corrupted",
                                ),
                            )
                        }
                        downloading = false
                    }
                    DownloadManager.STATUS_FAILED -> {
                        val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                        val reason = if (reasonIndex != -1) cursor.getInt(reasonIndex) else -1
                        emit(
                            GsiDownloadProgress(
                                assetName = assetName,
                                downloadId = downloadId,
                                state = GsiDownloadState.FAILED,
                                progress = progress,
                                error = "Download failed (code $reason)",
                            ),
                        )
                        downloading = false
                    }
                    DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PENDING, DownloadManager.STATUS_PAUSED -> {
                        emit(
                            GsiDownloadProgress(
                                assetName = assetName,
                                downloadId = downloadId,
                                state = GsiDownloadState.DOWNLOADING,
                                progress = progress,
                            ),
                        )
                    }
                }
                cursor.close()
            } else {
                // If cursor is null or empty, download was likely cancelled or removed
                downloading = false
                emit(
                    GsiDownloadProgress(
                        assetName = assetName,
                        downloadId = downloadId,
                        state = GsiDownloadState.IDLE,
                    ),
                )
            }
            if (downloading) {
                delay(800)
            }
        }
    }.flowOn(Dispatchers.IO)
}
