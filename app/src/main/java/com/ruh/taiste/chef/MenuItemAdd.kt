package com.ruh.taiste.chef

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.ruh.taiste.R
import com.ruh.taiste.databinding.ActivityMenuItemAddBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.chef.models.MenuItemImage
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import com.ruh.taiste.user.models.PersonalChefInfo
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.typeOf

private const val TAG = "MenuItemAdd"
class MenuItemAdd : AppCompatActivity() {
    private lateinit var binding : ActivityMenuItemAddBinding

    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private var city = ""
    private var state = ""
    private var zipCode = ""
    private var latitude = ""
    private var longitude = ""

    private var newOrEdit = ""
    private var itemLabel = ""

    private var documentId = UUID.randomUUID().toString()

    private var imageArr = ArrayList<MenuItemImage>()
    private val imageList = ArrayList<SlideModel>()

    private var burger = 0
    private var creative = 0
    private var healthy = 0
    private var lowCal = 0
    private var lowCarb = 0
    private var vegan = 0
    private var workout = 0
    private var seafood = 0
    private var pasta = 0
    private var personalChefInfo : PersonalChefInfo? = null
    private var typeOfItem = ""

    private var chefPassion = ""
    private var chefUsername = ""
    private var chefEmail = ""
    private var go = ""

    private var type = ""
    private var newPersonalOrEdit = "new"
    private var personalDocumentId = UUID.randomUUID().toString()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuItemAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        newOrEdit = intent.getStringExtra("new_or_edit").toString()
        itemLabel = intent.getStringExtra("item_label").toString()
        latitude = intent.getStringExtra("latitude").toString()
        longitude = intent.getStringExtra("longitude").toString()
        @Suppress("DEPRECATION")
        personalChefInfo = intent.getParcelableExtra("personal_chef_item")
        typeOfItem = intent.getStringExtra("type_of_item").toString()
        type = intent.getStringExtra("type").toString()


        val document = intent.getStringExtra("document_id").toString()
        if (document != "null") {
             documentId = document
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

                Log.d(TAG, "onCreate: type of item $typeOfItem")
            if (FirebaseAuth.getInstance().currentUser != null) {

                    loadItem()


            } else {
                Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }
        binding.addItemLabel.text = itemLabel
        Log.d(TAG, "onCreate: item label $itemLabel")

        Log.d(TAG, "onCreate: type of item $typeOfItem")
        if (typeOfItem == "Signature Dish") {
            loadItem1()
            go = typeOfItem
            binding.addItemLabel.text = "Signature Dish"
            binding.itemPrice.isVisible = false
        } else if (typeOfItem == "Option 1") {
            loadItem1()
            go = typeOfItem
            binding.itemPrice.isVisible = false
            binding.addItemLabel.text = "Option 1"
        } else if (typeOfItem == "Option 2") {
            loadItem1()
            go = typeOfItem
            binding.itemPrice.isVisible = false
            binding.addItemLabel.text = "Option 2"
        } else if (typeOfItem == "Option 3") {
            loadItem1()
            go = typeOfItem
            binding.itemPrice.isVisible = false
            binding.addItemLabel.text = "Option 3"
        } else if (typeOfItem == "Option 4") {
            loadItem1()
            go = typeOfItem
            binding.addItemLabel.text = "Option 4"
            binding.itemPrice.isVisible = false
        }

        city = intent.getStringExtra("city").toString()
        state = intent.getStringExtra("state").toString()
        zipCode = intent.getStringExtra("zipCode").toString()

      



        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.addImage.setOnClickListener {
            ImagePicker.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }


        binding.cancelImage.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            Log.d(TAG, "onCreate: ${imageArr}")
            Log.d(TAG, "onCreate: ${binding.imageSlider.x.toInt() + 1}")
                if (type != "Executive Chef") {
                    if (newOrEdit == "edit") {
                        AlertDialog.Builder(this)
                            .setTitle("Delete Image")
                            .setMessage("Are you sure you want to delete this image?")
                            // if the dialog is cancelable
                            .setCancelable(false)
                            .setPositiveButton("Yes") { dialog, _ ->
                                val storageRef = storage.reference
                                val renewRef = storage.reference
                                var path = imageArr[binding.imageSlider.x.toInt() + 1].imgPath

                                for (i in 0 until imageArr.size) {
                                    storageRef.child(imageArr[i].imgPath).delete()
                                }
                                imageArr.removeAt(binding.imageSlider.x.toInt() + 1)
                                imageList.removeAt(binding.imageSlider.x.toInt() + 1)
                                binding.imageSlider.setImageList(imageList)
                                if (imageList.size == 0) {
                                    binding.cancelImage.isVisible = false
                                }

                                for (i in 0 until imageArr.size) {
                                    renewRef.child("chefs/${FirebaseAuth.getInstance().currentUser!!.email!!}/${itemLabel}/${documentId}$i.png")
                                        .putFile(imageArr[i].img)
                                }
                                val data: Map<String, Any> =
                                    hashMapOf("imageCount" to imageArr.size)
                                db.collection("Chef")
                                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .collection(itemLabel).document(documentId).update(data)
                                db.collection(itemLabel).document(documentId).update(data)
                                Toast.makeText(this, "Image Deleted.", Toast.LENGTH_LONG).show()

                                dialog.dismiss()
                                startActivity(intent)

                            }
                            .setNegativeButton("No") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    } else {

                        imageList.removeAt(0)
                        binding.imageSlider.setImageList(imageList)
                        if (imageList.size == 0) {
                            binding.cancelImage.isVisible = false
                        }

                    }
                } else {
                    if (newPersonalOrEdit == "edit") {
                        AlertDialog.Builder(this)
                            .setTitle("Delete Image")
                            .setMessage("Are you sure you want to delete this image?")
                            // if the dialog is cancelable
                            .setCancelable(false)
                            .setPositiveButton("Yes") { dialog, _ ->
                                val storageRef = storage.reference
                                val renewRef = storage.reference
                                var path = imageArr[binding.imageSlider.x.toInt() + 1].imgPath

                                for (i in 0 until imageArr.size) {
                                    storageRef.child(imageArr[i].imgPath).delete()
                                }
                                imageArr.removeAt(binding.imageSlider.x.toInt() + 1)
                                imageList.removeAt(binding.imageSlider.x.toInt() + 1)
                                binding.imageSlider.setImageList(imageList)
                                if (imageList.size == 0) {
                                    binding.cancelImage.isVisible = false
                                }

                                for (i in 0 until imageArr.size) {
                                    renewRef.child("chefs/${FirebaseAuth.getInstance().currentUser!!.email!!}/Executive Chef/$personalDocumentId$i.png")
                                        .putFile(imageArr[i].img)
                                }
                                val data: Map<String, Any> =
                                    hashMapOf("imageCount" to imageArr.size)
                                db.collection("Chef")
                                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .collection("Executive Items").document(personalDocumentId).update(data)
                                db.collection("Executive Items").document(personalDocumentId).update(data)
                                Toast.makeText(this, "Image Deleted.", Toast.LENGTH_LONG).show()

                                dialog.dismiss()
                                startActivity(intent)

                            }
                            .setNegativeButton("No") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    } else {
                        imageList.removeAt(0)
                        binding.imageSlider.setImageList(imageList)
                        if (imageList.size == 0) {
                            binding.cancelImage.isVisible = false
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }


        binding.workout.setOnClickListener {
            binding.workout.isSelected = !binding.workout.isSelected
            workout = if (binding.workout.isSelected) {
                binding.workout.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.workout.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.workout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.workout.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.creative.setOnClickListener {
            binding.creative.isSelected = !binding.creative.isSelected
            creative = if (binding.creative.isSelected) {
                binding.creative.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.creative.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.creative.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.creative.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.healthy.setOnClickListener {
            binding.healthy.isSelected = !binding.healthy.isSelected
            healthy = if (binding.healthy.isSelected) {
                binding.healthy.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.healthy.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.healthy.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.healthy.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.lowCal.setOnClickListener {
            binding.lowCal.isSelected = !binding.lowCal.isSelected
            lowCal = if (binding.lowCal.isSelected) {
                binding.lowCal.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.lowCal.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.lowCal.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.lowCal.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.lowCarb.setOnClickListener {
            binding.lowCarb.isSelected = !binding.lowCarb.isSelected
            lowCarb = if (binding.lowCarb.isSelected) {
                binding.lowCarb.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.lowCarb.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.lowCarb.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.lowCarb.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.vegan.setOnClickListener {
            binding.vegan.isSelected = !binding.vegan.isSelected
            vegan = if (binding.vegan.isSelected) {
                binding.vegan.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.vegan.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.vegan.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.vegan.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.pasta.setOnClickListener {
            binding.pasta.isSelected = !binding.pasta.isSelected
            pasta = if (binding.pasta.isSelected) {
                binding.pasta.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.pasta.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.pasta.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.pasta.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.seafood.setOnClickListener {
            binding.seafood.isSelected = !binding.seafood.isSelected
            seafood = if (binding.seafood.isSelected) {
                binding.seafood.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.seafood.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.seafood.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.seafood.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.burger.setOnClickListener {
            binding.burger.isSelected = !binding.burger.isSelected
            burger = if (binding.burger.isSelected) {
                binding.burger.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.burger.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.burger.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.burger.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }



        binding.addButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            if (FirebaseAuth.getInstance().currentUser != null) {
                if (binding.itemTitle.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please add an item title to your item.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.itemDescription.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please add an item description to your item.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if ((binding.lowCal.isSelected && binding.itemCalories.text.isEmpty()) || binding.lowCal.isSelected && binding.itemCalories.text.toString()
                        .toInt() > 900
                ) {
                    Toast.makeText(
                        this,
                        "For low calorie selection, your calorie count must be lesser than 900. ",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (imageList.size == 0) {
                    Toast.makeText(
                        this,
                        "Please select atLeast one image to compliment your item.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.itemPrice.text.isEmpty() && type != "Executive Chef") {
                    Toast.makeText(this, "Please add a price to your item.", Toast.LENGTH_LONG)
                        .show()
                } else {
                    saveInfo()
                }
            } else {
                Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.deleteButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {
                val itemLabel = binding.addItemLabel.text.toString()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("where_to", "home")
                val intent1 = Intent(this, PersonalChefAdd::class.java)
            AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete? All date will be removed forever.")
                // if the dialog is cancelable
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, _ ->

                        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid)
                            .collection(itemLabel).document(documentId).delete()
                        db.collection(itemLabel).document(documentId).delete()
                    for (i in 0 until imageList.count()) {
                        storage.reference.child("chefs/${FirebaseAuth.getInstance().currentUser!!.email!!}/$itemLabel/$documentId$i.png")
                            .delete()
                    }

                    Toast.makeText(this, "Item Deleted.", Toast.LENGTH_LONG).show()
                    if (go != "") {
                        dialog.dismiss()
                        startActivity(intent)
                    } else {
                        dialog.dismiss()
                        startActivity(intent1)
                    }
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
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
            loadPersonalInfo()
        } else {
            Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadPersonalInfo() {
        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("PersonalInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    chefPassion = data?.get("chefPassion") as String
                    chefEmail = data["email"] as String
                    chefUsername = data["chefName"] as String
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadItem() {
        db.collection(itemLabel).document(documentId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val data = document.data

                val burger = data?.get("burger") as Number
                val creative = data["creative"] as Number
                val healthy = data["healthy"] as Number
                val imageCount = data["imageCount"] as Number
                val itemCalories = data["itemCalories"] as String
                val itemDescription = data["itemDescription"] as String
                val itemPrice = data["itemPrice"] as String
                val itemTitle = data["itemTitle"] as String
                val lowCal = data["lowCal"] as Number
                val lowCarb = data["lowCarb"] as Number
                val pasta = data["pasta"] as Number
                val seafood = data["seafood"] as Number
                val vegan = data["vegan"] as Number
                val workout = data["workout"] as Number


                binding.itemCalories.setText(itemCalories)
                binding.itemTitle.setText(itemTitle)
                binding.itemDescription.setText(itemDescription)
                binding.itemPrice.setText("$${itemPrice}")
                binding.cancelImage.isVisible = true
                binding.addButton.text = "Save"
                val email = data["chefEmail"] as String

                for (i in 0 until imageCount.toInt()) {
                    var path = "chefs/$email/$itemLabel/${documentId}$i.png"
                    storage.reference.child(path).downloadUrl.addOnSuccessListener { itemUri ->
                        imageList.add(SlideModel(itemUri.toString(), "", ScaleTypes.CENTER_CROP))
                        imageArr.add(MenuItemImage(itemUri!!, path))
                        binding.imageSlider.setImageList(imageList)
                    }
                }

                this.burger = burger.toInt()
                this.creative = creative.toInt()
                this.healthy = healthy.toInt()
                this.lowCarb = lowCal.toInt()
                this.lowCarb = lowCarb.toInt()
                this.pasta = pasta.toInt()
                this.seafood = seafood.toInt()
                this.vegan = vegan.toInt()
                this.workout = workout.toInt()

                if (burger.toInt() == 1) {
                    binding.burger.isSelected = true
                    binding.burger.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                    binding.burger.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else {
                    binding.burger.isSelected = false
                    binding.burger.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    binding.burger.setTextColor(ContextCompat.getColor(this, R.color.main))
                }

                if (creative.toInt() == 1) {
                    binding.creative.isSelected = true
                    binding.creative.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                    binding.creative.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else {
                    binding.creative.isSelected = false
                    binding.creative.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    binding.creative.setTextColor(ContextCompat.getColor(this, R.color.main))
                }

                if (healthy.toInt() == 1) {
                    binding.healthy.isSelected = true
                    binding.healthy.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                    binding.healthy.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else {
                    binding.healthy.isSelected = false
                    binding.healthy.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    binding.healthy.setTextColor(ContextCompat.getColor(this, R.color.main))
                }

                if (lowCarb.toInt() == 1) {
                    binding.lowCarb.isSelected = true
                    binding.lowCarb.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                    binding.lowCarb.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else {
                    binding.lowCarb.isSelected = false
                    binding.lowCarb.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    binding.lowCarb.setTextColor(ContextCompat.getColor(this, R.color.main))
                }

                if (pasta.toInt() == 1) {
                    binding.pasta.isSelected = true
                    binding.pasta.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                    binding.pasta.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else {
                    binding.pasta.isSelected = false
                    binding.pasta.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    binding.pasta.setTextColor(ContextCompat.getColor(this, R.color.main))
                }

                if (seafood.toInt() == 1) {
                    binding.seafood.isSelected = true
                    binding.seafood.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                    binding.seafood.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else {
                    binding.seafood.isSelected = false
                    binding.seafood.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    binding.seafood.setTextColor(ContextCompat.getColor(this, R.color.main))
                }

                if (vegan.toInt() == 1) {
                    binding.vegan.isSelected = true
                    binding.vegan.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                    binding.vegan.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else {
                    binding.vegan.isSelected = false
                    binding.vegan.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    binding.vegan.setTextColor(ContextCompat.getColor(this, R.color.main))
                }

                if (workout.toInt() == 1) {
                    binding.workout.isSelected = true
                    binding.workout.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                    binding.workout.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else {
                    binding.workout.isSelected = false
                    binding.workout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    binding.workout.setTextColor(ContextCompat.getColor(this, R.color.main))
                }

                if (lowCal.toInt() == 1) {
                    binding.lowCal.isSelected = true
                    binding.lowCal.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                    binding.lowCal.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else {
                    binding.lowCal.isSelected = false
                    binding.lowCal.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    binding.lowCal.setTextColor(ContextCompat.getColor(this, R.color.main))
                }


                binding.deleteButton.isVisible = true
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadItem1() {
        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Executive Items").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data
                    
                    
                    val typeOfService = data?.get("typeOfService") as String
                    if (typeOfService == typeOfItem) {
                        Log.d(TAG, "loadItem1: ahppening")
                        val burger = data["burger"] as Number
                        val creative = data["creative"] as Number
                        val healthy = data["healthy"] as Number
                        val imageCount = data["imageCount"] as Number
                        val itemCalories = data["itemCalories"] as String
                        val itemDescription = data["itemDescription"] as String
                        val itemPrice = data["itemPrice"] as String
                        val itemTitle = data["itemTitle"] as String
                        val lowCal = data["lowCal"] as Number
                        val lowCarb = data["lowCarb"] as Number
                        val pasta = data["pasta"] as Number
                        val seafood = data["seafood"] as Number
                        val vegan = data["vegan"] as Number
                        val workout = data["workout"] as Number


                        binding.itemCalories.setText(itemCalories)
                        binding.itemTitle.setText(itemTitle)
                        binding.itemDescription.setText(itemDescription)
                        binding.itemPrice.setText("$${itemPrice}")
                        binding.cancelImage.isVisible = true
                        binding.addButton.text = "Save"
                        binding.itemPrice.isVisible = false
                        binding.addItemLabel.text = typeOfService
                        this.documentId = doc.id
                        this.newPersonalOrEdit = "edit"
                        this.personalDocumentId = doc.id
                        val email = data["chefEmail"] as String

                        for (i in 0 until imageCount.toInt()) {
                            var path = "chefs/$email/Executive Items/${doc.id}$i.png"
                            storage.reference.child(path).downloadUrl.addOnSuccessListener { itemUri ->
                                imageList.add(
                                    SlideModel(
                                        itemUri.toString(),
                                        "",
                                        ScaleTypes.CENTER_CROP
                                    )
                                )
                                imageArr.add(MenuItemImage(itemUri!!, path))
                                binding.imageSlider.setImageList(imageList)


                            }
                        }

                        this.burger = burger.toInt()
                        this.creative = creative.toInt()
                        this.healthy = healthy.toInt()
                        this.lowCarb = lowCal.toInt()
                        this.lowCarb = lowCarb.toInt()
                        this.pasta = pasta.toInt()
                        this.seafood = seafood.toInt()
                        this.vegan = vegan.toInt()
                        this.workout = workout.toInt()

                        if (burger.toInt() == 1) {
                            binding.burger.isSelected = true
                            binding.burger.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.secondary
                                )
                            )
                            binding.burger.setTextColor(ContextCompat.getColor(this, R.color.white))
                        } else {
                            binding.burger.isSelected = false
                            binding.burger.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.white
                                )
                            )
                            binding.burger.setTextColor(ContextCompat.getColor(this, R.color.main))
                        }

                        if (creative.toInt() == 1) {
                            binding.creative.isSelected = true
                            binding.creative.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.secondary
                                )
                            )
                            binding.creative.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.white
                                )
                            )
                        } else {
                            binding.creative.isSelected = false
                            binding.creative.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.white
                                )
                            )
                            binding.creative.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.main
                                )
                            )
                        }

                        if (healthy.toInt() == 1) {
                            binding.healthy.isSelected = true
                            binding.healthy.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.secondary
                                )
                            )
                            binding.healthy.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.white
                                )
                            )
                        } else {
                            binding.healthy.isSelected = false
                            binding.healthy.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.white
                                )
                            )
                            binding.healthy.setTextColor(ContextCompat.getColor(this, R.color.main))
                        }

                        if (lowCarb.toInt() == 1) {
                            binding.lowCarb.isSelected = true
                            binding.lowCarb.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.secondary
                                )
                            )
                            binding.lowCarb.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.white
                                )
                            )
                        } else {
                            binding.lowCarb.isSelected = false
                            binding.lowCarb.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.white
                                )
                            )
                            binding.lowCarb.setTextColor(ContextCompat.getColor(this, R.color.main))
                        }

                        if (pasta.toInt() == 1) {
                            binding.pasta.isSelected = true
                            binding.pasta.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.secondary
                                )
                            )
                            binding.pasta.setTextColor(ContextCompat.getColor(this, R.color.white))
                        } else {
                            binding.pasta.isSelected = false
                            binding.pasta.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.white
                                )
                            )
                            binding.pasta.setTextColor(ContextCompat.getColor(this, R.color.main))
                        }

                        if (seafood.toInt() == 1) {
                            binding.seafood.isSelected = true
                            binding.seafood.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.secondary
                                )
                            )
                            binding.seafood.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.white
                                )
                            )
                        } else {
                            binding.seafood.isSelected = false
                            binding.seafood.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.white
                                )
                            )
                            binding.seafood.setTextColor(ContextCompat.getColor(this, R.color.main))
                        }

                        if (vegan.toInt() == 1) {
                            binding.vegan.isSelected = true
                            binding.vegan.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.secondary
                                )
                            )
                            binding.vegan.setTextColor(ContextCompat.getColor(this, R.color.white))
                        } else {
                            binding.vegan.isSelected = false
                            binding.vegan.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.white
                                )
                            )
                            binding.vegan.setTextColor(ContextCompat.getColor(this, R.color.main))
                        }

                        if (workout.toInt() == 1) {
                            binding.workout.isSelected = true
                            binding.workout.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.secondary
                                )
                            )
                            binding.workout.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.white
                                )
                            )
                        } else {
                            binding.workout.isSelected = false
                            binding.workout.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.white
                                )
                            )
                            binding.workout.setTextColor(ContextCompat.getColor(this, R.color.main))
                        }

                        if (lowCal.toInt() == 1) {
                            binding.lowCal.isSelected = true
                            binding.lowCal.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.secondary
                                )
                            )
                            binding.lowCal.setTextColor(ContextCompat.getColor(this, R.color.white))
                        } else {
                            binding.lowCal.isSelected = false
                            binding.lowCal.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.white
                                )
                            )
                            binding.lowCal.setTextColor(ContextCompat.getColor(this, R.color.main))
                        }


                        binding.deleteButton.isVisible = true
                    }
                }
            }
        }
    }

    private fun saveInfo() {
        val intent = Intent(this, MenuItemAdditions::class.java)


        val arr = ArrayList<Double>()
        val liked = ArrayList<String>()
        arr.add(0.0)


        val data: Map<String, Any> = hashMapOf("available" to "Yes", "burger" to burger, "chefEmail" to FirebaseAuth.getInstance().currentUser!!.email!!, "chefPassion" to chefPassion, "chefUsername" to chefUsername, "city" to city, "creative" to creative, "date" to Date(), "healthy" to healthy, "imageCount" to imageList.size, "itemCalories" to "${binding.itemCalories.text}", "itemDescription" to binding.itemDescription.text.toString(), "itemLikes" to 0, "itemOrders" to 0, "itemPrice" to "${binding.itemPrice.text}", "itemRating" to arr, "itemTitle" to binding.itemTitle.text.toString(), "itemType" to itemLabel, "liked" to liked, "lowCal" to lowCal, "lowCarb" to lowCarb, "pasta" to pasta, "profileImageId" to FirebaseAuth.getInstance().currentUser!!.uid, "quantityLimit" to "No Limit", "randomVariable" to documentId, "seafood" to seafood, "state" to state, "typeOfService" to itemLabel, "user" to "${FirebaseAuth.getInstance().currentUser!!.email!!}", "vegan"  to vegan, "workout" to workout, "zipCode" to "")
        val itemLabel = binding.addItemLabel.text.toString()

        if (type != "Executive Chef") {
            if (newOrEdit == "edit") {

                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .collection(itemLabel).document(documentId).update(data)
                db.collection(itemLabel).document(documentId).update(data)

            } else {
                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .collection(itemLabel).document(documentId).set(data)
                db.collection(itemLabel).document(documentId).set(data)

                for (i in 0 until imageList.size) {
                    storage.reference.child("chefs/${FirebaseAuth.getInstance().currentUser!!.email!!}/$itemLabel/${documentId}$i.png")
                        .putFile(Uri.parse(imageList[i].imageUrl))
                }
            }

        } else {
            intent.putExtra("personal_chef", "yes")
            val data1: Map<String, Any> = hashMapOf("available" to "Yes", "burger" to burger, "chefEmail" to FirebaseAuth.getInstance().currentUser!!.email!!, "chefPassion" to chefPassion, "chefUsername" to chefUsername, "city" to city, "creative" to creative, "date" to Date(), "healthy" to healthy, "imageCount" to imageList.size, "itemCalories" to "${binding.itemCalories.text}", "itemDescription" to binding.itemDescription.text.toString(), "itemLikes" to 0, "itemOrders" to 0, "itemPrice" to "${binding.itemPrice.text}", "itemRating" to arr, "itemTitle" to binding.itemTitle.text.toString(), "itemType" to itemLabel, "liked" to liked, "lowCal" to lowCal, "lowCarb" to lowCarb, "pasta" to pasta, "profileImageId" to FirebaseAuth.getInstance().currentUser!!.uid, "quantityLimit" to "No Limit", "randomVariable" to documentId, "seafood" to seafood, "state" to state, "typeOfService" to typeOfItem, "user" to FirebaseAuth.getInstance().currentUser!!.email!!, "vegan"  to vegan, "workout" to workout, "zipCode" to "")
            if (newPersonalOrEdit == "new") {
                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .collection("Executive Items").document(personalDocumentId).set(data1)

                for (i in 0 until imageList.size) {
                    storage.reference.child("chefs/${FirebaseAuth.getInstance().currentUser!!.email!!}/Executive Items/${personalDocumentId}$i.png")
                        .putFile(Uri.parse(imageList[i].imageUrl))
                }
                if (typeOfItem == "Signature Dish") {
                    db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Executive Items").get().addOnSuccessListener { documents ->
                        if (documents != null) {
                            for (doc in documents.documents) {
                                val data2 = doc.data

                                val typeOfService = data2?.get("typeOfService")
                                if (typeOfService == "info") {
                                    val data3 : Map<String, Any> = hashMapOf("signatureDishId" to personalDocumentId)
                                    db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Executive Items").document(doc.id).update(data3)
                                }
                            }
                        }
                    }
                }
            } else {
                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .collection("Executive Items").document(personalDocumentId).update(data)
            }
            Toast.makeText(this, "Item Saved.", Toast.LENGTH_LONG).show()

        }
        Toast.makeText(this, "Item Saved.", Toast.LENGTH_LONG).show()
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val uri: Uri = data?.data!!
                imageList.add(SlideModel(uri.toString(), "", ScaleTypes.CENTER_CROP))
                var path = "chefs/${FirebaseAuth.getInstance().currentUser!!.email!!}/$itemLabel/$documentId${imageArr.count() - 1}.png"
                imageArr.add(MenuItemImage(uri, path))
                if (newOrEdit == "edit") {
                    storage.reference.child(path)
                        .putFile(Uri.parse(imageList[imageArr.count() - 1].imageUrl))
                    Toast.makeText(this, "Image Added.", Toast.LENGTH_LONG)
                } else if (newPersonalOrEdit == "edit") {
                    storage.reference.child("chefs/${FirebaseAuth.getInstance().currentUser!!.email!!}/Executive Items/$personalDocumentId${imageArr.count() - 1}.png").putFile(Uri.parse(imageList[imageArr.count() - 1].imageUrl))
                    Toast.makeText(this, "Image Added.", Toast.LENGTH_LONG)
                }
                binding.imageSlider.setImageList(imageList)
                binding.cancelImage.isVisible = true
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