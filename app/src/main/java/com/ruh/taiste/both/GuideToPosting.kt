package com.ruh.taiste.both

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.ruh.taiste.chef.MainActivity
import com.ruh.taiste.chef.Personal
import com.ruh.taiste.databinding.ActivityGuideToPostingBinding

class GuideToPosting : AppCompatActivity() {
    private lateinit var binding : ActivityGuideToPostingBinding

    private var new = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideToPostingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            if (new == "yes") {
                    val intent = Intent(this, Personal::class.java)
                    startActivity(intent)
            } else {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}