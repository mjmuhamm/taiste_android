package com.ruh.taiste.chef

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ruh.taiste.both.CreateUser
import com.ruh.taiste.databinding.ActivityDisclaimerBinding

class Disclaimer : AppCompatActivity() {
    private lateinit var binding : ActivityDisclaimerBinding

    private var newUser = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisclaimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        newUser = intent.getStringExtra("new_user").toString()

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.okButton.setOnClickListener {
            val newOrEdit = intent.getStringExtra("new_or_edit")
            if (newOrEdit == "edit") {
                onBackPressedDispatcher.onBackPressed()
            } else {
                val intent = Intent(this, Personal::class.java)
                intent.putExtra("chef_or_user", "Chef")
                intent.putExtra("new_user", "yes")
                startActivity(intent)
            }
        }
    }
}