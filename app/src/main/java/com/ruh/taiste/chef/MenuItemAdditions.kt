package com.ruh.taiste.chef

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.R
import com.ruh.taiste.chef.models.MenuItemImage
import com.ruh.taiste.databinding.ActivityMenuItemAdditionsBinding
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*

class MenuItemAdditions : AppCompatActivity() {
    private lateinit var binding : ActivityMenuItemAdditionsBinding

    private var chefOrUser = ""
    private var toggle = ""

    private var contentId = ""
    private var ingredientsId = ""
    private var preparationId = ""

    private var ingredientsImage : Uri = Uri.EMPTY
    private var preparationImage : Uri = Uri.EMPTY
    private var contentVideo = ""


    private val db = Firebase.firestore
    private var storage = Firebase.storage

    var typeOfItem = ""
    var menuItemId = ""
    var itemTitle = ""
    var chefName = ""
    var chefEmail = ""
    var chefImageId = ""

    var personalChef = ""

    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuItemAdditionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.getStringExtra("chef_or_user") != null) {
            chefOrUser = intent.getStringExtra("chef_or_user").toString()
        }
        typeOfItem = intent.getStringExtra("type_of_item").toString()
        menuItemId = intent.getStringExtra("menu_item_id").toString()
        chefEmail = intent.getStringExtra("chef_email").toString()
        itemTitle = intent.getStringExtra("item_title").toString()
        chefImageId = intent.getStringExtra("chef_image_id").toString()
        personalChef = intent.getStringExtra("personal_chef").toString()


        if (chefOrUser != "") {
            binding.continueButton.isVisible = false
            binding.ingredientsLabel.text = "No ingredients posted"
            binding.ingredientsButton.isEnabled = false
            binding.preparationLabel.text = "No preparation guide posted"
            binding.preperationGuideButton.isEnabled = false
            binding.promotionalContentLabel.text ="No promotional content posted"
            binding.promotionalContentButton.isEnabled = false
        }
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.continueButton.setOnClickListener {
            if (personalChef == "yes") {
                val intent = Intent(this, PersonalChefAdd::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }




        binding.ingredientsButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            toggle = "Ingredients"
            if (chefOrUser == "") {
                ImagePicker.with(this)
                    .crop()	    			//Crop image(Optional), Check Customization for more option
                    .compress(1024)			//Final image size will be less than 1 MB(Optional)
                    .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                    .start()
            } else {
                val intent = Intent(this, YoutubeView::class.java)
                intent.putExtra("type", "ingredients")
                intent.putExtra("image_uri", ingredientsImage)
                startActivity(intent)
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.preperationGuideButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            toggle = "Preparation"
            if (chefOrUser == "") {
                ImagePicker.with(this)
                    .crop()	    			//Crop image(Optional), Check Customization for more option
                    .compress(1024)			//Final image size will be less than 1 MB(Optional)
                    .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                    .start()
            } else {
                val intent = Intent(this, YoutubeView::class.java)
                intent.putExtra("type", "preparation")
                intent.putExtra("image_uri", preparationImage)
                startActivity(intent)
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.promotionalContentButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {
            toggle = "Content"
            if (chefOrUser == "") {
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
            } else {
                val intent = Intent(this, YoutubeView::class.java)
                intent.putExtra("type", "content")
                intent.putExtra("video_uri", contentVideo)
                startActivity(intent)
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
            loadItems()
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

    @SuppressLint("SetTextI18n")
    private fun loadItems() {
        val a : MutableList<String> = arrayListOf("Ingredients", "Preparation", "Content")
        for (i in 0 until a.count()) {
            db.collection("Chef").document(chefImageId)
                .collection(typeOfItem).document(menuItemId).collection(a[i]).get().addOnSuccessListener { documents ->
                    if (documents != null) {
                        for (doc in documents.documents) {
                            val data = doc.data

                            if (a[i] == "Ingredients") {
                                if (chefOrUser != "") {
                                    storage.reference.child("chefs/$chefEmail/$typeOfItem/$menuItemId/Ingredients/${doc.id}.png").downloadUrl.addOnSuccessListener { uri ->
                                        binding.ingredientsButton.isEnabled = true
                                        ingredientsId = doc.id
                                        ingredientsImage = uri
                                        binding.ingredientsLabel.text = "Added"
                                        binding.ingredientsButton.isEnabled = true
                                        binding.ingredientsLabel.setTextColor(ContextCompat.getColor(this, R.color.main))
                                    }
                                } else {
                                    ingredientsId = doc.id
                                    binding.ingredientsLabel.text = "Added"
                                    binding.ingredientsButton.isEnabled = true
                                    binding.ingredientsLabel.setTextColor(ContextCompat.getColor(this, R.color.main))
                                }
                            } else if (a[i] == "Preparation") {
                                if (chefOrUser != "") {
                                    storage.reference.child("chefs/$chefEmail/$typeOfItem/$menuItemId/Preparation/${doc.id}.png").downloadUrl.addOnSuccessListener { uri ->
                                        binding.preperationGuideButton.isEnabled = true
                                        preparationId = doc.id
                                        preparationImage = uri
                                        binding.preparationLabel.text = "Added"
                                        binding.preperationGuideButton.isEnabled = true
                                        binding.preparationLabel.setTextColor(ContextCompat.getColor(this, R.color.main))
                                    }
                                } else {
                                    preparationId = doc.id
                                    binding.preparationLabel.text = "Added"
                                    binding.preperationGuideButton.isEnabled = true
                                    binding.preparationLabel.setTextColor(ContextCompat.getColor(this, R.color.main))
                                }
                            } else if (a[i] == "Content") {
                                if (chefOrUser != "") {
                                    getVideo(doc.id)
                                    binding.promotionalContentButton.isEnabled = true
                                } else {
                                    binding.promotionalContentLabel.text = "Added"
                                    contentId = doc.id
                                    binding.promotionalContentButton.isEnabled = true
                                }
                                binding.promotionalContentLabel.setTextColor(ContextCompat.getColor(this, R.color.main))
                            }

                        }
                    }
                }
        }
    }

    private fun getVideo(entryId: String)  {
        val description = "Promotional Content for $itemTitle"
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

                                val id = videos.getJSONObject(i)["id"] as String

                                if (id == entryId) {
                                    binding.promotionalContentLabel.text = "Added"
                                    contentId = id
                                    contentVideo = videos.getJSONObject(i)["dataUrl"] as String
                                    binding.promotionalContentButton.isEnabled = true
                                }

                            }
                        }
                    }
                }
            })

    }

    private fun saveVideo(documentId: String, uri: Uri) {

        // Create a storage reference from our app
        val storageRef = storage.reference

// Create a reference with an initial file path and name

        val ref = storageRef.child("chefs/${FirebaseAuth.getInstance().currentUser!!.email!!}/$typeOfItem/$menuItemId/Content/$documentId.png")
        val uploadTask = ref.putFile(uri)

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


                // Handle failures
                // ...
            }
        }

    }

    private fun uploadToKaltura(video: Uri) {
        val description = "Promotional Content for $itemTitle"
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

                        val entryId = json.getString("entryId") as String

                        mHandler.post {

                            val data: Map<String, Any> = hashMapOf("comments" to 0, "shared" to 0, "views" to 0, "liked" to arrayListOf<String>())
                            val data1: Map<String, Any> = hashMapOf("documentId" to entryId)

                            db.collection("Videos").document(entryId).set(data)
                            db.collection("Chefs").document(FirebaseAuth.getInstance().currentUser!!.uid).collection(typeOfItem).document(menuItemId).collection("Content").document(entryId).set(data1)
                            db.collection(typeOfItem).document(menuItemId).collection("Content").document(entryId).set(data1)
                            binding.promotionalContentLabel.text = "Added"
                            Toast.makeText(this@MenuItemAdditions, "Content Added.", Toast.LENGTH_LONG).show()
                            db.collection("Videos").document("Total").get().addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val data4 = document.data

                                    val total = data4?.get("total") as Number
                                    val info : Map<String, Any> = hashMapOf("total" to total.toInt() + 1)
                                    db.collection("Videos").document("Total").update(info)
                                }
                            }




                        }
                    }
                }
            })
    }

    private fun deleteVideo(entryId: String, videoUrl : Uri) {
        val description = "Promotional Content for $itemTitle"
        val body = FormBody.Builder()
            .add("entryId", entryId)
            .build()

        val request = Request.Builder()
            .url("https://taiste-video.onrender.com/delete-video")
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

                        mHandler.post {

                           saveVideo(UUID.randomUUID().toString(), videoUrl)




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


    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1111 && resultCode == RESULT_OK) {
            val videoUri = data?.data

            if (contentId == "") {
                saveVideo(UUID.randomUUID().toString(), videoUri!!)
            } else {
                deleteVideo(contentId, videoUri!!)
            }



        } else {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val uri: Uri = data?.data!!
                    if (toggle == "Ingredients") {
                        if (ingredientsId == "") {
                            val documentId = UUID.randomUUID().toString()
                            val data1 : Map<String, Any> = hashMapOf("documentId" to documentId)
                            db.collection(typeOfItem).document(menuItemId).collection("Ingredients").document(documentId).set(data1)
                            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection(typeOfItem).document(menuItemId).collection("Ingredients").document(documentId).set(data1)
                            storage.reference.child("chefs/${FirebaseAuth.getInstance().currentUser!!.email!!}/$typeOfItem/$menuItemId/Ingredients/$documentId.png").putFile(uri)
                            Toast.makeText(this, "Ingredients Added.", Toast.LENGTH_LONG).show()
                        } else {
                            val data1 : Map<String, Any> = hashMapOf("documentId" to preparationId)
                            db.collection(typeOfItem).document(menuItemId).collection("Ingredients").document(ingredientsId).update(data1)
                            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection(typeOfItem).document(menuItemId).collection("Ingredients").document(ingredientsId).update(data1)
                            storage.reference.child("chefs/${FirebaseAuth.getInstance().currentUser!!.email!!}/$typeOfItem/$menuItemId/Ingredients/$ingredientsId.png").putFile(uri)
                            Toast.makeText(this, "Ingredients Added.", Toast.LENGTH_LONG).show()
                        }
                    } else if (toggle == "Preparation") {
                        if (preparationId == "") {
                            val documentId = UUID.randomUUID().toString()
                            val data1 : Map<String, Any> = hashMapOf("documentId" to preparationId)
                            db.collection(typeOfItem).document(menuItemId).collection("Preparation").document(documentId).set(data1)
                            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection(typeOfItem).document(menuItemId).collection("Preparation").document(documentId).set(data1)
                            storage.reference.child("chefs/${FirebaseAuth.getInstance().currentUser!!.email!!}/$typeOfItem/$menuItemId/Preparation/$documentId.png").putFile(uri)
                            Toast.makeText(this, "Preparation Added.", Toast.LENGTH_LONG).show()
                        } else {
                            val data1 : Map<String, Any> = hashMapOf("documentId" to preparationId)
                            db.collection(typeOfItem).document(menuItemId).collection("Preparation").document(preparationId).update(data1)
                            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection(typeOfItem).document(menuItemId).collection("Preparation").document(preparationId).update(data1)
                            storage.reference.child("chefs/${FirebaseAuth.getInstance().currentUser!!.email!!}/$typeOfItem/$menuItemId/Preparation/$preparationId.png")
                            Toast.makeText(this, "Preparation Added.", Toast.LENGTH_LONG).show()
                        }
                    }

                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}