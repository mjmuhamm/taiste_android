package com.ruh.taiste.chef

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.ruh.taiste.databinding.ActivityContentAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import okhttp3.*
import java.io.IOException
import java.util.*

class ContentAdd : AppCompatActivity() {
    private lateinit var binding : ActivityContentAddBinding

    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private val documentId = UUID.randomUUID().toString()
    private var chefName = ""

    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    private var videoUri : Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentAddBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.record.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 111)
            } else {
                val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                @Suppress("DEPRECATION")
                startActivityForResult(intent, 1111)
            }
        }

        binding.playPauseButton.setOnClickListener {
            binding.playPauseButton.isSelected = !binding.playPauseButton.isSelected
            if (binding.playPauseButton.isSelected) {
                binding.videoView.pause()
                binding.playImage.isVisible = true
            } else {
                binding.videoView.start()
                binding.playImage.isVisible = false
            }
        }

        binding.upload.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 222)
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 333)
                } else {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "video/*"
                    @Suppress("DEPRECATION")
                    startActivityForResult(intent, 1111)
                }
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.remove.setOnClickListener {
            videoUri = null
            binding.videoView.setVideoURI(null)
            binding.itemDescription.setText("")
            binding.videoLayout.isVisible = false
            binding.recordLayout.isVisible = true
        }

        binding.save.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            if (FirebaseAuth.getInstance().currentUser != null) {
                if (videoUri == null) {
                    Toast.makeText(this, "Please select a video first.", Toast.LENGTH_LONG).show()
                } else {
                    binding.progressBar.isVisible = true
                    binding.save.isEnabled = false
                    binding.remove.isEnabled = false
                    binding.backButton.isEnabled = false

                    saveVideo()
                }

            } else {
                Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(this)
            } else {
                isOnline1(this)
            }
        ) {
            loadUsername()
        } else {
            Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }

    }

    private fun loadUsername() {
        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("PersonalInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    this.chefName = data?.get("chefName") as String

                }
            }
        }
    }
    private fun saveVideo() {

        // Create a storage reference from our app
        val storageRef = storage.reference

// Create a reference with an initial file path and name

        val ref = storageRef.child("chefs/${FirebaseAuth.getInstance().currentUser!!.email!!}/Content/$documentId.png")
        val uploadTask = ref.putFile(videoUri!!)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                uploadToKaltura(downloadUri)

            } else {
                Toast.makeText(this, "Upload has failed. Please try again another time.", Toast.LENGTH_LONG).show()
                binding.progressBar.isVisible = false
                binding.save.isEnabled = true
                binding.remove.isEnabled = true
                binding.backButton.isEnabled = true

                // Handle failures
                // ...
            }
        }
//        storage.reference.child("chefs/${FirebaseAuth.getInstance().currentUser!!.email!!}/Content/$documentId.png").putFile(videoUri!!)
    }

    private fun uploadToKaltura(video: Uri) {
        val description = if (binding.itemDescription.text.isNotEmpty()) { binding.itemDescription.text.toString() } else { "no description" }
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("where_to", "home")
            val body = FormBody.Builder()
                .add("videoUrl", video.toString())
                .add("description", description)
                .add("name", this.chefName)
                .build()

            val request = Request.Builder()
                .url("https://taiste-video.onrender.com/upload-video")
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(body)
                .build()

            httpClient.newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        displayAlert( "Error: $e")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (!response.isSuccessful) {
                            displayAlert(
                                "Error: $response"
                            )
                        } else {
                            mHandler.post {
                              val docRef = db.collection("Videos").document("Total")
                                  docRef.get().addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        val data = document.data

                                        val total = data?.get("Total") as Number
                                        val info : Map<String, Any> = hashMapOf("total" to total.toInt() + 1)
                                        docRef.update(info)
                                    }
                                }
                                Toast.makeText(this@ContentAdd, "Item Uploaded Successfully. Please give it a few minutes before appearing on your home screen.", Toast.LENGTH_LONG).show()
                                startActivity(intent)
                                finish()


                            }
                        }
                    }
                })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            binding.record.isEnabled = true
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            @Suppress("DEPRECATION")
            startActivityForResult(intent, 1111)

        }
        if (requestCode == 222 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 333)
            } else {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "video/*"
                @Suppress("DEPRECATION")
                startActivityForResult(intent, 1111)
            }
        }
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

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1111 && resultCode == RESULT_OK) {
            val videoUri = data?.data
            this.videoUri = videoUri
            binding.videoLayout.isVisible = true
            binding.recordLayout.isVisible = false

            binding.videoView.setVideoURI(videoUri)
            binding.videoView.start()


        }
    }
}