package com.udacity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.detailContent.textFileName.text = intent.getStringExtra(EXTRA_DOWNLOAD_FILE_NAME)
        binding.detailContent.textStatus.text = intent.getStringExtra(EXTRA_DOWNLOAD_STATUS)

        binding.detailContent.buttonOk.setOnClickListener {
            onBackPressed()
        }
    }
}
