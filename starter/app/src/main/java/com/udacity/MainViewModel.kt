package com.udacity

import android.app.DownloadManager
import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule
import kotlin.properties.Delegates

sealed class DownloadStatus {
    object None : DownloadStatus()
    object Pending : DownloadStatus()
    data class Running(val progress: Int) : DownloadStatus()
    object Paused : DownloadStatus()
    object Successful : DownloadStatus()
    object Failed : DownloadStatus()
    object Cancelled : DownloadStatus()
}

class MainViewModel : ViewModel() {

    private var downloadStatusDelegate: DownloadStatus by Delegates.observable(DownloadStatus.None) { _, old, new ->
        if (old != new || new is DownloadStatus.Running) {
            _downloadStatus.postValue(new)
        }
    }

    private val _downloadStatus = MutableLiveData<DownloadStatus>()
    val downloadStatus: LiveData<DownloadStatus>
        get() = _downloadStatus

    fun pollDownloadStatusPending(downloadID: Long, downloadManager: DownloadManager) {
        CoroutineScope(Dispatchers.IO).launch {
            Timer("DownloadStatusPendingTimer", false).schedule(500, 500) {
                val query = DownloadManager.Query()
                query.setFilterById(downloadID)
                val cursor: Cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    when (status) {
                        DownloadManager.STATUS_PENDING -> {
                            downloadStatusDelegate = DownloadStatus.Pending
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            downloadStatusDelegate = DownloadStatus.Running(0)
                            cancel()
                        }
                        DownloadManager.STATUS_PAUSED -> {
                            downloadManager.remove(downloadID)
                            downloadStatusDelegate = DownloadStatus.Cancelled
                            cancel()
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            downloadStatusDelegate = DownloadStatus.Successful
                            cancel()
                        }
                        DownloadManager.STATUS_FAILED -> {
                            downloadStatusDelegate = DownloadStatus.Failed
                            cancel()
                        }
                    }
                }
            }
        }
    }

    fun updateDownloadStatus(downloadID: Long, downloadManager: DownloadManager) {
        CoroutineScope(Dispatchers.IO).launch {
            val query = DownloadManager.Query()
            query.setFilterById(downloadID)
            val cursor: Cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                    DownloadManager.STATUS_PENDING -> {
                        downloadStatusDelegate = DownloadStatus.Pending
                    }
                    DownloadManager.STATUS_RUNNING -> {
                        val total =
                            cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        var progress = 0
                        if (total >= 0) {
                            val downloaded =
                                cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            progress = (downloaded * 100L / total).toInt()
                        }
                        downloadStatusDelegate = DownloadStatus.Running(progress)
                    }
                    DownloadManager.STATUS_PAUSED -> {
                        downloadStatusDelegate = DownloadStatus.Paused
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        downloadStatusDelegate = DownloadStatus.Successful
                    }
                    DownloadManager.STATUS_FAILED -> {
                        downloadStatusDelegate = DownloadStatus.Failed
                    }
                }
            }
        }
    }
}