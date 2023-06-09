package com.example.pdfviewerfirebase

import android.annotation.SuppressLint
import android.app.DownloadManager
import kotlinx.coroutines.delay

const val DOWNLOAD_COMPLETE = 100L
const val DOWNLOAD_FAILED  = -100L

internal class DownloadProgressUpdater(
    private val manager: DownloadManager,
    private val downloadId: Long,
    private var downloadProgressListener: DownloadProgressListener?
)  {
    private val query: DownloadManager.Query = DownloadManager.Query()
    private var totalBytes: Int = 0

    interface DownloadProgressListener {
        fun updateProgress(progress: Long)
    }

    init {
        query.setFilterById(this.downloadId)
    }

    @SuppressLint("Range")
    suspend fun run() {

        while (downloadId > 0) {

          delay(250)

            manager.query(query).use {
                if (it.moveToFirst()) {

                    //get total bytes of the file
                    if (totalBytes <= 0) {
                        totalBytes =
                            it.getInt(it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    }

                    val downloadStatus = it.getInt(it.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    val bytesDownloadedSoFar =
                        it.getInt(it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

                    when (downloadStatus) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            downloadProgressListener?.updateProgress(DOWNLOAD_COMPLETE)

                            return

                        }
                        DownloadManager.STATUS_FAILED -> {
                            downloadProgressListener?.updateProgress(DOWNLOAD_FAILED)
                            return
                        }
                        else -> {
                            //update progress
                            val percentProgress = ((bytesDownloadedSoFar * 100L) / totalBytes)
                            downloadProgressListener?.updateProgress(percentProgress)
                        }
                    }

                }
            }
        }

        if (downloadProgressListener != null) {
            downloadProgressListener = null
        }
    }

}