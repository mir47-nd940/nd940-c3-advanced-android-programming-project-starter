package com.udacity

import android.app.NotificationManager
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.detailContent.textFileName.text = intent.getStringExtra(EXTRA_DOWNLOAD_FILE_NAME)

        val status = intent.getStringExtra(EXTRA_DOWNLOAD_STATUS)
        binding.detailContent.textStatus.text = status
        if (status == getString(R.string.download_status_failed)) {
            binding.detailContent.textStatus.setTextColor(Color.RED)
        }

        binding.detailContent.buttonOk.setOnClickListener {
            onBackPressed()
        }

        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.cancelNotifications()
    }
}
