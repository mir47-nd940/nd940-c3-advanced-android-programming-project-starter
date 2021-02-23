package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var downloadName: String = ""
    private var downloadProgress: Int = 0
    private lateinit var downloadManager: DownloadManager

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        createNotificationChannel()

        binding.content.customButton.setLoadingAnimationListener {
            updateDownloadProgress()
        }

        binding.content.customButton.setOnClickListener {
            when (binding.content.radioGroup.checkedRadioButtonId) {
                R.id.radio_glide -> Pair(URL_GLIDE, getString(R.string.button_glide))
                R.id.radio_loadapp -> Pair(URL_LOADAPP, getString(R.string.button_loadapp))
                R.id.radio_retrofit -> Pair(URL_RETROFIT, getString(R.string.button_retrofit))
                else -> null
            }?.let {
                binding.content.customButton.isEnabled = false
                download(it.first)
                downloadName = it.second
            } ?: run {
                Toast.makeText(this, getString(R.string.select_file), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadID) {
                val notificationManager = ContextCompat.getSystemService(
                    applicationContext,
                    NotificationManager::class.java
                ) as NotificationManager

                val status = when (getDownloadStatus()?.first) {
                    DownloadManager.STATUS_SUCCESSFUL -> getString(R.string.download_status_successful)
                    DownloadManager.STATUS_FAILED -> getString(R.string.download_status_failed)
                    else -> getString(R.string.download_status_incomplete)
                }

                notificationManager.sendNotification(
                    getString(R.string.notification_description),
                    downloadName,
                    status,
                    applicationContext
                )
            }
        }
    }

    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)

        // enqueue puts the download request in the queue.
        downloadID = downloadManager.enqueue(request)
        updateDownloadProgress()
    }

    private fun updateDownloadProgress() {
        val status = getDownloadStatus()
        when (status?.first) {
            DownloadManager.STATUS_RUNNING -> {
                if (downloadProgress != status.second) {
                    downloadProgress = status.second
                    binding.content.customButton.animateTo(downloadProgress)
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        updateDownloadProgress()
                    }, 200)
                }
            }
            DownloadManager.STATUS_SUCCESSFUL -> {
                downloadProgress = 100
                binding.content.customButton.animateTo(downloadProgress)
                binding.content.customButton.isEnabled = true
            }
            DownloadManager.STATUS_FAILED -> {
                binding.content.customButton.reset()
                binding.content.customButton.isEnabled = true
            }
        }
    }

    private fun getDownloadStatus(): Pair<Int, Int>? {
        val query = DownloadManager.Query()
        query.setFilterById(downloadID)
        val cursor: Cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            var progress = 0
            when (status) {
                DownloadManager.STATUS_PENDING -> {
                    Handler(Looper.getMainLooper()).postDelayed({
                        updateDownloadProgress()
                    }, 500)
                }
                DownloadManager.STATUS_RUNNING -> {
                    val total =
                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    if (total >= 0) {
                        val downloaded =
                            cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        progress = (downloaded * 100L / total).toInt()
                    }
                }
            }
            return Pair(status, progress)
        } else {
            return null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                getString(R.string.download_notification_channel_id),
                getString(R.string.download_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { setShowBadge(false) }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.enableVibration(true)
            notificationChannel.description =
                getString(R.string.download_notification_channel_description)

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private const val URL_GLIDE =
            "https://github.com/bumptech/glide/archive/master.zip"
        private const val URL_LOADAPP =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_RETROFIT =
            "https://github.com/square/retrofit/archive/master.zip"
    }
}
