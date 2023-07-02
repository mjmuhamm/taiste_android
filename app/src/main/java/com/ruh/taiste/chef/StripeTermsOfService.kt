package com.ruh.taiste.chef

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import com.ruh.taiste.databinding.ActivityStripeTermsOfServiceBinding

class StripeTermsOfService : AppCompatActivity() {
    private lateinit var binding: ActivityStripeTermsOfServiceBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStripeTermsOfServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.webView.webViewClient = WebViewClient()
        binding.webView.apply {
            loadUrl("https://stripe.com/connect-account/legal")
            settings.javaScriptEnabled = true
        }

        binding.backButton.setOnClickListener {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else { onBackPressedDispatcher.onBackPressed() }

        }

        binding.refreshButton.setOnClickListener {
            binding.webView.refreshDrawableState()
        }




    }
}