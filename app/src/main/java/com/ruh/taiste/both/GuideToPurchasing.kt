package com.ruh.taiste.both

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.ruh.taiste.databinding.ActivityGuideToPurchasingBinding
import com.ruh.taiste.user.MainActivity

class GuideToPurchasing : AppCompatActivity() {
    private lateinit var binding: ActivityGuideToPurchasingBinding

    private var new = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideToPurchasingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            if (new == "yes") {
                if (FirebaseAuth.getInstance().currentUser!!.displayName == "Chef") {
                    val intent = Intent(this, com.ruh.taiste.chef.MainActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            } else {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}