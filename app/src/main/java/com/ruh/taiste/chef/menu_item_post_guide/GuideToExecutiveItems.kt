package com.ruh.taiste.chef.menu_item_post_guide

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.chef.MenuItemAdd
import com.ruh.taiste.chef.PersonalChefAdd
import com.ruh.taiste.databinding.ActivityGuideToExecutiveItemsBinding
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class GuideToExecutiveItems : AppCompatActivity() {
    private val db = Firebase.firestore
    private lateinit var binding : ActivityGuideToExecutiveItemsBinding
    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    private var promotionalContentVideo = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideToExecutiveItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(this)
            } else {
                isOnline1(this)
            }
        ) {
            getVideo()
        } else {
            Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }

        binding.menuItemPostButton.setOnClickListener {
            intent.putExtra("type", "menu_item")
            startActivity(intent)
        }
        binding.ingredientsButton.setOnClickListener {
            intent.putExtra("type", "ingredients_example")
            startActivity(intent)
        }

        binding.preperationGuideButton.setOnClickListener {
            intent.putExtra("type", "preparation_example")
            startActivity(intent)
        }

        binding.preperationContentButton.setOnClickListener {
            intent.putExtra("type", "preparation_content_example")
            intent.putExtra("video_uri", promotionalContentVideo)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.continueButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {
                val intent = Intent(this, PersonalChefAdd::class.java)
                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("PersonalInfo").get().addOnSuccessListener { documents ->
                    if (documents != null) {
                        for (doc in documents.documents) {
                            val data = doc.data

                            val city = data?.get("city") as String
                            val state = data["state"] as String
                            val zipCode = data["zipCode"] as String

                            intent.putExtra("new_or_edit", "new")
                            intent.putExtra("item_label", "Executive Items")
                            intent.putExtra("city", city)
                            intent.putExtra("state", state)
                            intent.putExtra("zipCode", zipCode)
                            startActivity(intent)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getVideo()  {
        val body = FormBody.Builder()
            .build()

        val request = Request.Builder()
            .url("https://taiste-video.onrender.com/get-videos")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    displayAlert( "Error: $e")
                }

                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        displayAlert(
                            "Error: $response"
                        )
                    } else {
                        val responseData = response.body!!.string()

                        val json =
                            JSONObject(responseData)

                        val videos = json.getJSONArray("videos")

                        mHandler.post {

                            for (i in 0 until videos.length()) {

                                val name = videos.getJSONObject(i)["name"] as String

                                if (name == "sample1") {
                                    promotionalContentVideo = videos.getJSONObject(i)["dataUrl"] as String
                                }

                            }
                        }
                    }
                }
            })

    }

    private fun displayAlert(
        message: String
    ) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
                .setTitle("Failed to load page")
                .setMessage(message)

            builder.setPositiveButton("Ok", null)
            builder
                .create()
                .show()
        }
    }
}