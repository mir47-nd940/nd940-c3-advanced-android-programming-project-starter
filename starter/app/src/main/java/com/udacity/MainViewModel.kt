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

sealed class DownloadStatus {
    object Pending : DownloadStatus()
    data class Running(val progress: Int) : DownloadStatus()
    object Paused : DownloadStatus()
    object Successful : DownloadStatus()
    object Failed : DownloadStatus()
    object Cancelled : DownloadStatus()
}

class MainViewModel : ViewModel() {

    private val _status = MutableLiveData<DownloadStatus>()
    val status: LiveData<DownloadStatus>
        get() = _status

    fun pollDownloadStatus(downloadID: Long, downloadManager: DownloadManager) {
        CoroutineScope(Dispatchers.IO).launch {
            Timer("DownloadStatusTimer", false).schedule(500, 500) {
                val query = DownloadManager.Query()
                query.setFilterById(downloadID)
                val cursor: Cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_PENDING -> {
//                            _status.postValue(DownloadStatus.Pending)
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            _status.postValue(DownloadStatus.Running(0))
                            cancel()
                        }
                        DownloadManager.STATUS_PAUSED -> {
                            downloadManager.remove(downloadID)
                            _status.postValue(DownloadStatus.Cancelled)
                            cancel()
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            _status.postValue(DownloadStatus.Successful)
                            cancel()
                        }
                        DownloadManager.STATUS_FAILED -> {
                            _status.postValue(DownloadStatus.Failed)
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
                        _status.postValue(DownloadStatus.Pending)
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
                        _status.postValue(DownloadStatus.Running(progress))
                    }
                    DownloadManager.STATUS_PAUSED -> {
                        downloadManager.remove(downloadID)
                        _status.postValue(DownloadStatus.Cancelled)
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        _status.postValue(DownloadStatus.Successful)
                    }
                    DownloadManager.STATUS_FAILED -> {
                        _status.postValue(DownloadStatus.Failed)
                    }
                }
            }
        }
    }
}