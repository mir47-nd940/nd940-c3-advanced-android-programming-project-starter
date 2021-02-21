package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var downloadName: String = ""

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        createNotificationChannel()

        binding.content.customButton.setLoadingAnimationListener {
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            viewModel.updateDownloadStatus(downloadID, downloadManager)
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

        viewModel.downloadStatus.observe(this) {
            when (it) {
                is DownloadStatus.Pending -> {
                }
                is DownloadStatus.Running -> {
                    binding.content.customButton.animateTo(it.progress)
                }
                is DownloadStatus.Paused -> {
                    binding.content.customButton.isEnabled = true
                }
                is DownloadStatus.Successful -> {
                    binding.content.customButton.animateTo(100)
                    binding.content.customButton.isEnabled = true
                }
                is DownloadStatus.Failed -> {
                    binding.content.customButton.isEnabled = true
                }
                is DownloadStatus.Cancelled -> {
                    binding.content.customButton.isEnabled = true
                }
                DownloadStatus.None -> {
                }
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

                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val status = when (viewModel.getDownloadStatus(id, downloadManager)) {
                    DownloadStatus.None -> getString(R.string.download_status_none)
                    DownloadStatus.Pending -> getString(R.string.download_status_pending)
                    is DownloadStatus.Running -> getString(R.string.download_status_running)
                    DownloadStatus.Paused -> getString(R.string.download_status_paused)
                    DownloadStatus.Successful -> getString(R.string.download_status_successful)
                    DownloadStatus.Failed -> getString(R.string.download_status_failed)
                    DownloadStatus.Cancelled -> getString(R.string.download_status_cancelled)
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

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        // enqueue puts the download request in the queue.
        downloadID = downloadManager.enqueue(request)
        viewModel.pollDownloadStatusPending(downloadID, downloadManager)
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
            "https://github.com/bumptech/glide/releases/download/v3.6.0/glide-3.6.0.jar"
        private const val URL_LOADAPP =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_RETROFIT =
            "https://search.maven.org/remote_content?g=com.squareup.retrofit2&a=retrofit&v=LATEST"
    }
}
