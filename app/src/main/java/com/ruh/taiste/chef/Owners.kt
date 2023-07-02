package com.ruh.taiste.chef

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ruh.taiste.chef.models.BusinessBankingInfo
import com.ruh.taiste.databinding.ActivityOwnersBinding

class Owners : AppCompatActivity() {
    private lateinit var binding : ActivityOwnersBinding
    var newOrEdit = ""

    lateinit var businessBankingInfo : BusinessBankingInfo
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        newOrEdit = intent.getStringExtra("new_or_edit").toString()
        if (intent.getStringExtra("external") != null) {
            @Suppress("DEPRECATION")
            businessBankingInfo = intent.getParcelableExtra("business_banking_info")!!

            if (businessBankingInfo.owner1 != null) {
                if (businessBankingInfo.owner1!!.firstName != "") {
                    binding.owner1.text =
                        "${businessBankingInfo.owner1!!.firstName} ${businessBankingInfo.owner1!!.lastName}"
                }
            }
            if (businessBankingInfo.owner2 != null) {
                if (businessBankingInfo.owner2!!.firstName != "") {
                    binding.owner2.text =
                        "${businessBankingInfo.owner2!!.firstName} ${businessBankingInfo.owner2!!.lastName}"
                }
            }
            if (businessBankingInfo.owner3 != null) {
                if (businessBankingInfo.owner3!!.firstName != "") {
                    binding.owner3.text =
                        "${businessBankingInfo.owner3!!.firstName} ${businessBankingInfo.owner3!!.lastName}"
                }
            }
            if (businessBankingInfo.owner4 != null) {
                if (businessBankingInfo.owner4!!.firstName != "") {
                    binding.owner4.text =
                        "${businessBankingInfo.owner4!!.firstName} ${businessBankingInfo.owner4!!.lastName}"
                }
            }
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.owner1Button.setOnClickListener {
            val intent = Intent(this, AddPersonBanking::class.java)
            intent.putExtra("external", "owner1")
            intent.putExtra("new_or_edit", newOrEdit)
            intent.putExtra("business_banking_info", businessBankingInfo)
            startActivity(intent)
        }
        binding.owner2Button.setOnClickListener {
            if (businessBankingInfo.owner1!!.firstName != "") {
                val intent = Intent(this, AddPersonBanking::class.java)
                intent.putExtra("external", "owner2")
                intent.putExtra("new_or_edit", newOrEdit)
                intent.putExtra("business_banking_info", businessBankingInfo)
                startActivity(intent)
            }
        }
        binding.owner3Button.setOnClickListener {
            if (businessBankingInfo.owner2!!.firstName != "") {
                val intent = Intent(this, AddPersonBanking::class.java)
                intent.putExtra("external", "owner3")
                intent.putExtra("new_or_edit", newOrEdit)
                intent.putExtra("business_banking_info", businessBankingInfo)
                startActivity(intent)
            }
        }
        binding.owner4Button.setOnClickListener {
            if (businessBankingInfo.owner3!!.firstName != "") {
                val intent = Intent(this, AddPersonBanking::class.java)
                intent.putExtra("external", "owner4")
                intent.putExtra("new_or_edit", newOrEdit)
                intent.putExtra("business_banking_info", businessBankingInfo)
                startActivity(intent)
            }
        }

        binding.save.setOnClickListener {
            val intent = Intent(this, Banking::class.java)
            intent.putExtra("external", "external")
            intent.putExtra("business_banking_info", businessBankingInfo)
            startActivity(intent)
        }




    }
}