package com.ruh.taiste.both

import android.os.Build.VERSION.SDK_INT
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.exoplayer2.ExoPlayer
import com.ruh.taiste.both.adapters.VideoAdapter
import com.ruh.taiste.both.models.VideoModel
import com.ruh.taiste.databinding.ActivityChefContentBinding

class ChefContent : AppCompatActivity() {
    private lateinit var binding: ActivityChefContentBinding

    private var content: MutableList<VideoModel> = arrayListOf()
    private var chefOrUser = ""
    private var chefImageId = ""
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var exoPlayer: ExoPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChefContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chefImageId = intent.getStringExtra("chef_image_id").toString()
        chefOrUser = intent.getStringExtra("chef_or_user").toString()
        if (SDK_INT >= 33) {
            @Suppress("DEPRECATION")
            content = intent.getParcelableArrayListExtra("content")!!
        } else {
            @Suppress("DEPRECATION")
            content = intent.getParcelableArrayListExtra("content")!!

        }
        exoPlayer = ExoPlayer.Builder(this).build()
        videoAdapter = VideoAdapter(this, exoPlayer, content, "chef", chefImageId)
        binding.viewPager.adapter = videoAdapter
        videoAdapter.submitList(content, chefImageId)


        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        videoAdapter.notifyItemInserted(0)
        for (i in 1 until content.size) {
            videoAdapter.notifyItemInserted(i)
        }
    }



}