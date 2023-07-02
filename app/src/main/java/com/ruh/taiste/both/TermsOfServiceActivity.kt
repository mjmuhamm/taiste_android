package com.ruh.taiste.both

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ruh.taiste.databinding.ActivityPrivacyPolicyBinding
import com.ruh.taiste.databinding.ActivityTermsOfServiceBinding

class TermsOfServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTermsOfServiceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTermsOfServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}