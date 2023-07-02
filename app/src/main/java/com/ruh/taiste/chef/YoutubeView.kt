package com.ruh.taiste.chef

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import com.google.android.exoplayer2.ExoPlayer
import com.ruh.taiste.R
import com.ruh.taiste.both.adapters.VideoAdapter
import com.ruh.taiste.databinding.ActivityYoutubeViewBinding

class YoutubeView : AppCompatActivity() {
    private lateinit var binding : ActivityYoutubeViewBinding
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var exoPlayer: ExoPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutubeViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val type = intent.getStringExtra("type").toString()
        val imageUri = intent.getStringExtra("image_uri").toString()
        val videoUri = intent.getStringExtra("video_uri").toString()

        if (type == "ingredients") {
            binding.image.setImageURI(Uri.parse(imageUri))
            binding.image.isVisible = true
        } else if (type == "preparation") {
            binding.image.setImageResource(R.drawable.preparation)
            binding.image.setImageURI(Uri.parse(imageUri))
            binding.image.isVisible = true
        } else if (type == "content") {
            binding.videoView.setVideoURI(Uri.parse(videoUri))
            binding.videoView.isVisible = true
        } else if (type == "menu_item") {
            binding.image.isVisible = true
            binding.image.setImageResource(R.drawable.menu_item_post)
        } else if (type == "ingredients_example") {
            binding.image.isVisible = true
            binding.image.setImageResource(R.drawable.ingredients)
        } else if (type == "preparation_example") {
            binding.image.isVisible = true
            binding.image.setImageResource(R.drawable.preparation)
        } else if (type == "preparation_content_example") {
            binding.videoView.setVideoURI(Uri.parse(videoUri))
            binding.videoView.isVisible = true
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

}