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
                val status = getDownloadStatus(downloadID, downloadManager)
                downloadStatusDelegate = status
                when (status) {
                    DownloadStatus.None,
                    DownloadStatus.Pending,
                    -> {
                    }
                    is DownloadStatus.Running,
                    DownloadStatus.Paused,
                    DownloadStatus.Successful,
                    DownloadStatus.Failed,
                    DownloadStatus.Cancelled,
                    -> cancel()
                }
            }
        }
    }

    fun updateDownloadStatus(downloadID: Long, downloadManager: DownloadManager) {
        CoroutineScope(Dispatchers.IO).launch {
            downloadStatusDelegate = (getDownloadStatus(downloadID, downloadManager))
        }
    }

    fun getDownloadStatus(downloadID: Long, downloadManager: DownloadManager): DownloadStatus {
        val query = DownloadManager.Query()
        query.setFilterById(downloadID)
        val cursor: Cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            return when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                DownloadManager.STATUS_PENDING -> DownloadStatus.Pending
                DownloadManager.STATUS_RUNNING -> {
                    val total =
                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    var progress = 0
                    if (total >= 0) {
                        val downloaded =
                            cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        progress = (downloaded * 100L / total).toInt()
                    }
                    DownloadStatus.Running(progress)
                }
                DownloadManager.STATUS_PAUSED -> DownloadStatus.Paused
                DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.Successful
                DownloadManager.STATUS_FAILED -> DownloadStatus.Failed
                else -> DownloadStatus.None
            }
        }
        return DownloadStatus.None
    }
}