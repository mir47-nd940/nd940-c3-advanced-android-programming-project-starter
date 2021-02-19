package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.udacity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        binding.content.customButton.setLoadingAnimationListener {
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            viewModel.updateDownloadStatus(downloadID, downloadManager)
        }

        binding.content.customButton.setOnClickListener {
            when (binding.content.radioGroup.checkedRadioButtonId) {
                R.id.radio_glide -> URL_GLIDE
                R.id.radio_loadapp -> URL_LOADAPP
                R.id.radio_retrofit -> URL_RETROFIT
                else -> null
            }?.let {
                binding.content.customButton.isEnabled = false
                download(it)
            } ?: run {
                Toast.makeText(this, getString(R.string.select_file), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.status.observe(this) {
            when (it) {
                is DownloadStatus.Pending -> {
                }
                is DownloadStatus.Running -> {
                     binding.content.customButton.updateProgress(it.progress)
                }
                is DownloadStatus.Paused -> {
                }
                is DownloadStatus.Successful -> {
                    binding.content.customButton.updateProgress(100)
                    binding.content.customButton.isEnabled = true
                }
                is DownloadStatus.Failed -> {
                    binding.content.customButton.isEnabled = true
                }
                is DownloadStatus.Cancelled -> {
                    binding.content.customButton.isEnabled = true
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

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        // enqueue puts the download request in the queue.
        downloadID = downloadManager.enqueue(request)
        viewModel.pollDownloadStatus(downloadID, downloadManager)
    }

    companion object {
        private const val URL_GLIDE =
            "https://github.com/bumptech/glide/archive/master.zip"
        private const val URL_LOADAPP =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_RETROFIT =
            "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }
}
