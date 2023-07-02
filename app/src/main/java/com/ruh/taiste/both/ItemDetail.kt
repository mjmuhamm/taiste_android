package com.ruh.taiste.both

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.ruh.taiste.R
import com.ruh.taiste.both.adapters.ReviewsAdapter
import com.ruh.taiste.both.models.Reviews
import com.ruh.taiste.databinding.ActivityItemDetailBinding
import com.ruh.taiste.user.models.FeedMenuItems
import com.ruh.taiste.user.models.UserLikes
import com.ruh.taiste.user.models.UserOrders
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.chef.MainActivity
import com.ruh.taiste.chef.MenuItemAdditions
import com.ruh.taiste.chef.chefImage
import com.ruh.taiste.chef.models.MenuItemImage
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import com.ruh.taiste.user.models.PersonalChefInfo
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "ItemDetail"
class ItemDetail : AppCompatActivity() {

    private lateinit var binding: ActivityItemDetailBinding

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val httpClient = OkHttpClient()
    private val imageList = ArrayList<SlideModel>()

    private var item: FeedMenuItems? = null
    private var orderItem: UserOrders? = null
    private var likeItem: UserLikes? = null


    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var reviewsLabel : TextView
    private lateinit var exitButton : TextView
    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var messageText : EditText
    private lateinit var sendMessage: ImageView


    private var userChefRatingArray : MutableList<Int> = arrayListOf()
    private var userExpectationsArray : MutableList<Int> = arrayListOf()
    private var userQualityArray : MutableList<Int> = arrayListOf()


    private lateinit var reviewsAdapter: ReviewsAdapter
    private var reviews : MutableList<Reviews> = ArrayList()

    private var typeOfItem = ""
    private var chefImageId = ""
    private var caterOrPersonal = ""

    private var menuItemId = ""

    //MealKit
    private var imgArr1 : MutableList<MenuItemImage> = arrayListOf()
    var isMealKit = ""
    var mealKitOrderId = ""
    var receiverImageId = ""
    var receiverUserName = ""
    var mealKitItemTitle = ""
    var mealKitNewOrEdit = ""
    private var acceptOrDeny = ""

    private var chefEmail = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        typeOfItem = intent.getStringExtra("type_of_item").toString()
        chefImageId = intent.getStringExtra("chef_image_id").toString()
        caterOrPersonal = intent.getStringExtra("cater_or_personal").toString()
        @Suppress("DEPRECATION")
        val item = intent.getParcelableExtra<FeedMenuItems>("item")
        @Suppress("DEPRECATION")
        val orderItem = intent.getParcelableExtra<UserOrders>("order_item")
        @Suppress("DEPRECATION")
        val likeItem = intent.getParcelableExtra<UserLikes>("like_item")

        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(this)
            } else {
                isOnline1(this)
            }
        ) {


        if (typeOfItem == "Executive Chef") {
            binding.initialLayout.isVisible = false
            binding.itemTitle.isVisible = false
            binding.imageSlider.isVisible = false
            binding.itemDescription.isVisible = false
            binding.additionalInfoButton.isVisible = false
            binding.personalChefView.isVisible = true
            binding.payLayoutPersonalChef.isVisible = true

            loadPersonalChefInfo()
        } else if (isMealKit != "") {
            binding.itemTitle.isVisible = false
            binding.ratingLayout.isVisible = false
            binding.reviewLayout.isVisible = false
            binding.addImageLayout.isVisible = true
            if (FirebaseAuth.getInstance().currentUser!!.displayName!! == "Chef") {
                binding.upload.isVisible = true
            } else {
                binding.acceptOrDenyLayout.isVisible = true
            }
            binding.accept.isEnabled = false
            binding.deny.isEnabled = false
            loadMealKitDeliveryImages()

        } else {
                if (item != null) {
                    this.item = item

                    binding.itemTitle.text = item.itemTitle
                    binding.itemDescription.text = item.itemDescription
                    binding.itemCalories.text = "Calories:  ${item.itemCalories}"
                    getImages()
                    loadReviews("")
                } else if (orderItem != null) {
                    this.orderItem = orderItem

                    binding.itemTitle.text = orderItem.itemTitle
                    binding.itemDescription.text = orderItem.itemDescription
                    binding.itemCalories.text = "Calories:  ${orderItem.itemCalories}"
                    getImages()
                    loadReviews("")
                } else if (likeItem != null) {
                    this.likeItem = likeItem

                    binding.itemTitle.text = likeItem.itemTitle
                    binding.itemDescription.text = likeItem.itemDescription
                    binding.itemCalories.text = "Calories:  ${likeItem.itemCalories}"
                    getImages()
                    loadReviews("")
                } else {
                    typeOfItem = "Executive Chef"
                    loadDish()
                }
        }
        } else {
            Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }
        bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(this).inflate(R.layout.review_bottom_sheet, R.id.review_bottom_sheet as? RelativeLayout)

        reviewsLabel = bottomSheetView.findViewById(R.id.reviews_label)
        exitButton = bottomSheetView.findViewById(R.id.exit_button)
        reviewsRecyclerView = bottomSheetView.findViewById(R.id.reviews_recycler_view)

        bottomSheetDialog.setContentView(bottomSheetView)

        reviewsAdapter = ReviewsAdapter(this, reviews)
        reviewsRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewsRecyclerView.adapter = reviewsAdapter

        binding.backButtonPersonalChef.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.signatureImage.setOnClickListener {
            val intent = Intent(this, ItemDetail::class.java)
            intent.putExtra("chef_image_id", chefImageId)
            intent.putExtra("cater_or_personal", "Signature Dish")
            startActivity(intent)
        }
        binding.option1Button.setOnClickListener {
            if (binding.option1Text.text != "No Item") {
                val intent = Intent(this, ItemDetail::class.java)
                intent.putExtra("chef_image_id", chefImageId)
                intent.putExtra("cater_or_personal", "Option 1")
                startActivity(intent)
            }
        }

        Log.d(TAG, "onCreate: option1text ${binding.option1Text.text}")
        binding.option2Button.setOnClickListener {
            if (binding.option2Text.text != "No Item") {
                val intent = Intent(this, ItemDetail::class.java)
                intent.putExtra("chef_image_id", chefImageId)
                intent.putExtra("cater_or_personal", "Option 2")
                startActivity(intent)
            }
        }

        binding.option3Button.setOnClickListener {
            if (binding.option3Text.text != "No Item") {
                val intent = Intent(this, ItemDetail::class.java)
                intent.putExtra("chef_image_id", chefImageId)
                intent.putExtra("cater_or_personal", "Option 3")
                startActivity(intent)
            }
        }

        binding.option4Button.setOnClickListener {
            if (binding.option4Text.text != "No Item") {
                val intent = Intent(this, ItemDetail::class.java)
                intent.putExtra("chef_image_id", chefImageId)
                intent.putExtra("cater_or_personal", "Option 4")
                startActivity(intent)
            }
        }

        binding.reviewLayout.setOnClickListener {
            bottomSheetDialog.show()


        }

        binding.additionalInfoButton.setOnClickListener {
            val intent = Intent(this, MenuItemAdditions::class.java)
            intent.putExtra("chef_or_user", "user")
            intent.putExtra("type_of_item", typeOfItem)

            if (item != null) {
                intent.putExtra("menu_item_id", item.menuItemId)
                intent.putExtra("chef_email", item.chefEmail)
                intent.putExtra("chef_image_id", item.chefImageId)
            } else if (orderItem != null) {
                intent.putExtra("menu_item_id", orderItem.menuItemId)
                intent.putExtra("chef_email", orderItem.chefEmail)
                intent.putExtra("chef_image_id", orderItem.chefImageId)
            } else if (likeItem != null) {
                intent.putExtra("menu_item_id", likeItem.documentId)
                intent.putExtra("chef_email", likeItem.chefEmail)
                intent.putExtra("chef_image_id", likeItem.chefImageId)
            } else if (menuItemId != "") {
                intent.putExtra("menu_item_id", menuItemId)
                intent.putExtra("chef_email", chefEmail)
                intent.putExtra("chef_image_id", chefImageId)
            }
            intent.putExtra("item_title", binding.itemTitle.text.toString())


            startActivity(intent)

        }

        exitButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        //MealKit
        binding.cancelImage.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

                if (mealKitNewOrEdit == "edit") {
                    AlertDialog.Builder(this)
                        .setTitle("Delete Image")
                        .setMessage("Are you sure you want to delete this image?")
                        // if the dialog is cancelable
                        .setCancelable(false)
                        .setPositiveButton("Yes") { dialog, _ ->
                            val storageRef = storage.reference
                            val renewRef = storage.reference
                            var path = imgArr1[binding.imageSlider.x.toInt() + 1].imgPath

                            for (i in 0 until imgArr1.size) {
                                storageRef.child(imgArr1[i].imgPath).delete()
                            }
                            imgArr1.removeAt(binding.imageSlider.x.toInt() + 1)
                            imageList.removeAt(binding.imageSlider.x.toInt() + 1)
                            binding.imageSlider.setImageList(imageList)
                            if (imageList.size == 0) {
                                binding.cancelImage.isVisible = false
                            }

                            for (i in 0 until imgArr1.size) {
                                renewRef.child("mealKitDelivery/$mealKitOrderId$i.png").putFile(imgArr1[i].img)
                            }
                            val data: Map<String, Any> = hashMapOf("imageCount" to imgArr1.size)
                            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").document(mealKitOrderId).collection("MealKit Delivery").document(mealKitOrderId).update(data)
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
                    imageList.removeAt(binding.imageSlider.x.toInt() + 1)
                    binding.imageSlider.setImageList(imageList)
                    if (imageList.size == 0) {
                        binding.cancelImage.isVisible = false
                    }
                }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.addImage.setOnClickListener {
            ImagePicker.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }


        binding.accept.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to accept this shipping process?")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, _ ->
                        acceptOrDeny = "accepted"
                        val data : Map<String, Any> = hashMapOf("acceptOrDeny" to "accept")
                        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").document(mealKitOrderId).collection("MealKit Delivery").document(mealKitOrderId).update(data)
                        db.collection("User").document(receiverImageId).collection("Orders").document(mealKitOrderId).collection("MealKit Delivery").document(mealKitOrderId).update(data)
                        notify1()
                        Toast.makeText(this, "Images accepted. Your items should arrive soon.", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, com.ruh.taiste.user.MainActivity::class.java)
                        startActivity(intent)
                        dialog.dismiss()
                        startActivity(intent)

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

        binding.deny.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to deny this shipping process?")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, _ ->
                        acceptOrDeny = "denied"
                        val data : Map<String, Any> = hashMapOf("acceptOrDeny" to "deny")
                        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").document(mealKitOrderId).collection("MealKit Delivery").document(mealKitOrderId).update(data)
                        db.collection("User").document(receiverImageId).collection("Orders").document(mealKitOrderId).collection("MealKit Delivery").document(mealKitOrderId).update(data)
                        notify1()
                        Toast.makeText(this, "Images Denied. Please give your chef more time to deliver better expectations.", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, com.ruh.taiste.user.MainActivity::class.java)
                        startActivity(intent)

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


        binding.upload.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {
                if (imgArr1.size > 0) {
                    if (mealKitNewOrEdit != "edit") {
                        val data : Map<String, Any> = hashMapOf("imageCount" to imgArr1.size, "acceptOrDeny" to "")
                        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").document(mealKitOrderId).collection("MealKit Delivery").document(mealKitOrderId).set(data)
                        db.collection("User").document(receiverImageId).collection("Orders").document(mealKitOrderId).collection("MealKit Delivery").document(mealKitOrderId).set(data)
                        for (i in 0 until imgArr1.size) {
                            storage.reference.child("mealKitDelivery/$mealKitOrderId$i.png").putFile(imgArr1[i].img)
                        }
                    } else {
                        val data : Map<String, Any> = hashMapOf("imageCount" to imgArr1.size)
                        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").document(mealKitOrderId).collection("MealKit Delivery").document(mealKitOrderId).set(data)
                        db.collection("User").document(receiverImageId).collection("Orders").document(mealKitOrderId).collection("MealKit Delivery").document(mealKitOrderId).set(data)
                    }
                    notify1()
                    Toast.makeText(this, "Images Uploaded.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                } else {
                    Toast.makeText(this, "Please upload your images of containers, shipping box, and shipping label.", Toast.LENGTH_LONG).show()
                }

            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }

        }



        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }



    }

    @SuppressLint("SetTextI18n")
    private fun loadPersonalChefInfo() {
        db.collection("Chef").document(chefImageId).collection("Executive Items").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val typeOfService = data?.get("typeOfService") as String

                    if (typeOfService == "info") {
                        val briefIntroduction = data["briefIntroduction"] as String
                        val lengthOfPersonalChef = data["lengthOfPersonalChef"] as String
                        val specialty = data["specialty"] as String
                        val servicePrice = data["servicePrice"] as String
                        val expectations = data["expectations"] as Number
                        val chefRating = data["chefRating"] as Number
                        val quality = data["quality"] as Number
                        val whatHelpsYouExcel = data["whatHelpsYouExcel"] as String
                        val mostPrizedAccomplishment = data["mostPrizedAccomplishment"] as String
                        val weeks = data["weeks"] as Number
                        val months = data["months"] as Number
                        val trialRun = data["trialRun"] as Number
                        val openToMenuRequests = data["openToMenuRequests"] as String
                        binding.briefIntroduction.text = briefIntroduction


                        binding.lengthOfPersonalChef.setText(lengthOfPersonalChef)
                        binding.itemPrice.setText(servicePrice)
                        binding.mostPrizedAccomplishment.setText(mostPrizedAccomplishment)
                        binding.specialty.setText(specialty)
                        binding.whatHelpsYouExcel.setText(whatHelpsYouExcel)

                        var availability = ""
                        if (trialRun != 0) {
                            availability = "Trial Runs"
                        }
                        if (weeks != 0) {
                            availability = "  Weeks"
                        }
                        if (months != 0) {
                            availability = "  Months"
                        }
                        if (openToMenuRequests == "Yes") {
                            binding.openToMenuRequests.text = "Yes"
                        } else {
                            binding.openToMenuRequests.text = "No"
                        }

                        binding.progressBar.isVisible = false
                        loadReviews(doc.id)
                    } else {
                        val itemTitle = data["itemTitle"] as String
                        val chefEmail = data["chefEmail"] as String
                        when (typeOfService) {
                            "Signature Dish" -> {
                                binding.signatureTitle.text = itemTitle
                                storage.reference.child("chefs/$chefEmail/Executive Items/${doc.id}0.png").downloadUrl.addOnSuccessListener { uri ->
                                    Glide.with(this).load(uri).placeholder(R.drawable.default_profile).into(binding.signatureImage)
                                }
                            }
                            "Option 1" -> {
                                binding.option1Text.text = itemTitle.take(18)
                                binding.option2Button.isVisible = true
                            }
                            "Option 2" -> {
                                binding.option2Text.text = itemTitle.take(18)
                                binding.option3Button.isVisible = true
                            }
                            "Option 3" -> {
                                binding.option3Text.text = itemTitle.take(18)
                                binding.option4Button.isVisible = true
                            }
                            "Option 4" -> {
                                binding.option4Text.text = itemTitle.take(18)
                                binding.option4Button.isVisible = true
                                binding.option4Text.isVisible = true
                            }
                        }

                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadDish() {
        db.collection("Chef").document(chefImageId).collection("Executive Items").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data
                    val typeOfService = data?.get("typeOfService") as String

                    if (typeOfService == caterOrPersonal) {
                        val itemTitle = data["itemTitle"] as String
                        val imageCount = data["imageCount"] as Number
                        val chefEmail = data["chefEmail"] as String
                        val itemDescription = data["itemDescription"] as String
                        val itemCalories = data["itemCalories"] as String

                        binding.itemCalories.text = "Item Calories: $itemCalories"
                        binding.itemTitle.text = itemTitle
                        binding.itemDescription.text = itemDescription
                        this.menuItemId = doc.id
                        this.chefEmail = chefEmail
                        loadReviews(chefImageId)
                        for (i in 0 until imageCount.toInt()) {
                            storage.reference.child("chefs/$chefEmail/Executive Items/${doc.id}$i.png").downloadUrl.addOnSuccessListener { uri ->
                                imageList.add(
                                    SlideModel(
                                        uri.toString(),
                                        "",
                                        ScaleTypes.CENTER_CROP
                                    )
                                )
                                binding.imageSlider.setImageList(imageList)
                            }
                        }
                        binding.progressBar.isVisible = false

                    }
                }
            }
        }


    }
    private fun subscribeToTopic(userNotification: String, chefNotification: String, orderId: String, itemTitle: String) {

        val body = FormBody.Builder()
            .add("notificationToken1", userNotification)
            .add("notificationToken2", chefNotification)
            .add("topic", orderId)
            .build()

        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/subscribe-to-topic")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    displayAlert("Error: $e")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        displayAlert(
                            "Error: $response"
                        )
                    } else {
                        val responseData = response.body!!.string()
                        val responseJson =
                            JSONObject(responseData)

                       if (FirebaseAuth.getInstance().currentUser!!.displayName!! == "Chef") {
                           if (acceptOrDeny == "accepted") {
                               sendMessage("Meal Kit Delivery Notification", "$receiverUserName has just accepted your image upload! Payout is on its way.", orderId)
                           } else {
                               sendMessage("Meal Kit Delivery Notification", "$receiverUserName has denied your image upload. Our internal team will review this. In the meantime, please try again with new methods.", orderId)
                           }
                       } else {
                           sendMessage("Meal Kit Delivery Notification", "New images uploaded for your $mealKitItemTitle order; awaiting your approval.", orderId)
                       }

                    }
                }
            })
    }
    private fun sendMessage(title: String, notification: String, topic: String) {

        val body = FormBody.Builder()
            .add("title", title)
            .add("notification", notification)
            .add("topic", topic)
            .build()

        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/send-message")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    displayAlert("Error: $e")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        displayAlert(
                            "Error: $response"
                        )
                    } else {
                        val responseData = response.body!!.string()
                        val responseJson =
                            JSONObject(responseData)



                    }
                }
            })
    }

    private fun notify1() {
        val sdfT = SimpleDateFormat("MM-dd-yyyy hh:mm:ss a")
        val date1 = sdfT.format(Date())
        db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!).document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener { document ->
            if (document != null) {
                val data = document.data

                val token = data?.get("notificationToken") as String
                if (FirebaseAuth.getInstance().currentUser!!.displayName!! == "Chef") {
                    db.collection("User").document(receiverImageId).get().addOnSuccessListener { document ->
                        if (document != null) {
                            val data1 = document.data

                            val token1 = data1?.get("notificationToken") as String
                            subscribeToTopic(token, token1, mealKitOrderId, mealKitItemTitle)
                        }
                    }
                    val data3: Map<String, Any> = hashMapOf("notification" to "New images uploaded for your $mealKitItemTitle order.", "date" to sdfT.format(Date()))
                    val data4: Map<String, Any> = hashMapOf("notifications" to "yes")
                    db.collection("User").document(receiverImageId).update(data4)
                    db.collection("User").document(receiverImageId).collection("Notifications").document().set(data3)
                } else {
                    db.collection("Chef").document(receiverImageId).get().addOnSuccessListener { document1 ->
                        if (document1 != null) {
                            val data1 = document1.data

                            val token1 = data1?.get("notificationToken") as String
                            subscribeToTopic(token, token1, mealKitOrderId, mealKitItemTitle)
                        }
                    }

                    val data4: Map<String, Any> = hashMapOf("notifications" to "yes")
                    db.collection("Chef").document(receiverImageId).update(data4)
                    if (acceptOrDeny == "accepted") {
                        val data3: Map<String, Any> = hashMapOf("notification" to "$receiverUserName has accepted your image uploads! Your payout is on its way.", "date" to sdfT.format(Date()))
                        db.collection("Chef").document(receiverImageId).collection("Notifications").document().set(data3)
                    } else {
                        val data3: Map<String, Any> = hashMapOf("notification" to "$receiverUserName has denied your image upload. Our internal team will review this. In the meantime, please try again with new methods.", "date" to sdfT.format(Date()))
                        db.collection("Chef").document(receiverImageId).collection("Notifications").document().set(data3)
                    }


                }
            }
        }
    }

    private fun getImages() {

        var imageCount = 0
        var chefEmail = ""
        var itemType = ""
        var menuItemId = ""

        when {
            item != null -> {
                imageCount = item!!.imageCount.toInt()
                chefEmail = item!!.chefEmail
                itemType = item!!.itemType
                menuItemId = item!!.menuItemId
            }
            orderItem != null -> {
                imageCount = orderItem!!.imageCount.toInt()
                chefEmail = orderItem!!.chefEmail
                itemType = orderItem!!.typeOfService
                menuItemId = orderItem!!.menuItemId
            }
            likeItem != null -> {
                imageCount = likeItem!!.imageCount.toInt()
                chefEmail = likeItem!!.chefEmail
                itemType = likeItem!!.itemType
                menuItemId = likeItem!!.documentId
            }
        }

        for (i in 0 until imageCount) {
            storage.reference.child("chefs/${chefEmail}/${itemType}/${menuItemId}$i.png").downloadUrl.addOnSuccessListener { imageUri ->
                imageList.add(SlideModel(imageUri.toString(), "", ScaleTypes.CENTER_CROP))
                binding.imageSlider.setImageList(imageList)

            }
            binding.progressBar.isVisible = false
        }

    }

    private fun loadMealKitDeliveryImages() {
        db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!).document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").document(mealKitOrderId).collection("MealKit Delivery").document(mealKitOrderId).get().addOnSuccessListener { document ->
            if (document != null) {
                val data = document.data

                val imageCount = data?.get("imageCount") as Number
                for (i in 0 until imageCount.toInt()) {
                    val path = "mealKitDeliver/$mealKitOrderId$i.png"
                    storage.reference.child(path).downloadUrl.addOnSuccessListener { uri ->
                        val img = MenuItemImage(uri, path)
                        imgArr1.add(img)
                        imageList.add(SlideModel(uri.toString(), "", ScaleTypes.CENTER_CROP))
                        binding.imageSlider.setImageList(imageList)
                    }
                }
            }
        }
    }

    private fun loadReviews(id: String) {
        var chefEmail = ""
        var itemType = ""
        var menuItemId = ""

        when {
            item != null -> {
                chefEmail = item!!.chefEmail
                itemType = item!!.itemType
                menuItemId = item!!.menuItemId
            }
            orderItem != null -> {
                chefEmail = orderItem!!.chefEmail
                itemType = orderItem!!.typeOfService
                menuItemId = orderItem!!.menuItemId
            }
            likeItem != null -> {
                chefEmail = likeItem!!.chefEmail
                itemType = likeItem!!.itemType
                menuItemId = likeItem!!.documentId
            }
            typeOfItem == "Executive Chef" -> {
                itemType = "Executive Items"
                menuItemId = id
            }
        }

        db.collection(itemType).document(menuItemId).collection("UserReviews").get().addOnSuccessListener { documents ->
            if (documents != null) {
                var count = 0
                for (doc in documents.documents) {
                    val data = doc.data
                    count += 1
                    val chefImageId = data?.get("chefImageId") as String
                    val date = data["date"] as String
                    val itemTitle = data["itemTitle"] as String
                    val liked = data["liked"] as ArrayList<String>
                    val reviewItemID = doc.id
                    val user = data["userEmail"] as String
                    val userChefRatingI = data["chefRating"] as Number
                    val userExpectationsRatingI = data["expectations"] as Number
                    val userImageId = data["userImageId"] as String
                    val userQualityRatingI = data["quality"] as Number
                    val userRecommendation = data["recommend"] as Number
                    val userReviewTextField = data["thoughts"] as String
                    var numberOfReplies = 0

                    userChefRatingArray.add(userChefRatingI.toInt())
                    userExpectationsArray.add(userExpectationsRatingI.toInt())
                    userQualityArray.add(userQualityRatingI.toInt())
                    if (count == documents.documents.size - 1) {
                        var chefRating = 0.0
                        var userExpectation = 0.0
                        var userQuality = 0.0

                        for (i in 0 until userChefRatingArray.size) {
                            chefRating += userChefRatingArray[i]
                            if (i == userChefRatingArray.size - 1) {
                                chefRating /= userChefRatingArray.size
                                if (chefRating <= 1) {
                                    binding.chefRating1.setImageResource(R.drawable.star_filled)
                                } else if (chefRating > 1 && chefRating < 2) {
                                    binding.chefRating1.setImageResource(R.drawable.star_filled)
                                    binding.chefRating2.setImageResource(R.drawable.star_filled)
                                } else if (chefRating > 2 && chefRating < 3) {
                                    binding.chefRating1.setImageResource(R.drawable.star_filled)
                                    binding.chefRating2.setImageResource(R.drawable.star_filled)
                                    binding.chefRating3.setImageResource(R.drawable.star_filled)
                                } else if (chefRating > 3 && chefRating < 4) {
                                    binding.chefRating1.setImageResource(R.drawable.star_filled)
                                    binding.chefRating2.setImageResource(R.drawable.star_filled)
                                    binding.chefRating3.setImageResource(R.drawable.star_filled)
                                    binding.chefRating4.setImageResource(R.drawable.star_filled)
                                } else {
                                    binding.chefRating1.setImageResource(R.drawable.star_filled)
                                    binding.chefRating2.setImageResource(R.drawable.star_filled)
                                    binding.chefRating3.setImageResource(R.drawable.star_filled)
                                    binding.chefRating4.setImageResource(R.drawable.star_filled)
                                    binding.chefRating5.setImageResource(R.drawable.star_filled)
                                }
                            }
                            userExpectation += userExpectationsArray[i]
                            if (i == userExpectationsArray.size - 1) {
                                userExpectation /= userExpectationsArray.size
                                if (userExpectation <= 1) {
                                    binding.expectations1.setImageResource(R.drawable.star_filled)
                                } else if (userExpectation > 1 && userExpectation < 2) {
                                    binding.expectations1.setImageResource(R.drawable.star_filled)
                                    binding.expectations2.setImageResource(R.drawable.star_filled)
                                } else if (userExpectation > 2 && userExpectation < 3) {
                                    binding.expectations1.setImageResource(R.drawable.star_filled)
                                    binding.expectations2.setImageResource(R.drawable.star_filled)
                                    binding.expectations3.setImageResource(R.drawable.star_filled)
                                } else if (userExpectation > 3 && userExpectation < 4) {
                                    binding.expectations1.setImageResource(R.drawable.star_filled)
                                    binding.expectations2.setImageResource(R.drawable.star_filled)
                                    binding.expectations3.setImageResource(R.drawable.star_filled)
                                    binding.expectations4.setImageResource(R.drawable.star_filled)
                                } else {
                                    binding.expectations1.setImageResource(R.drawable.star_filled)
                                    binding.expectations2.setImageResource(R.drawable.star_filled)
                                    binding.expectations3.setImageResource(R.drawable.star_filled)
                                    binding.expectations4.setImageResource(R.drawable.star_filled)
                                    binding.expectations5.setImageResource(R.drawable.star_filled)
                                }
                            }
                            userQuality += userQualityArray[i]
                            if (i == userQualityArray.size - 1) {
                                userQuality /= userQualityArray.size
                                if (userQuality <= 1) {
                                    binding.quality1.setImageResource(R.drawable.star_filled)
                                } else if (userQuality > 1 && userQuality < 2) {
                                    binding.quality1.setImageResource(R.drawable.star_filled)
                                    binding.quality2.setImageResource(R.drawable.star_filled)
                                } else if (userQuality > 2 && userQuality < 3) {
                                    binding.quality1.setImageResource(R.drawable.star_filled)
                                    binding.quality2.setImageResource(R.drawable.star_filled)
                                    binding.quality3.setImageResource(R.drawable.star_filled)
                                } else if (userQuality > 3 && userQuality < 4) {
                                    binding.quality1.setImageResource(R.drawable.star_filled)
                                    binding.quality2.setImageResource(R.drawable.star_filled)
                                    binding.quality3.setImageResource(R.drawable.star_filled)
                                    binding.quality4.setImageResource(R.drawable.star_filled)
                                } else {
                                    binding.quality1.setImageResource(R.drawable.star_filled)
                                    binding.quality2.setImageResource(R.drawable.star_filled)
                                    binding.quality3.setImageResource(R.drawable.star_filled)
                                    binding.quality4.setImageResource(R.drawable.star_filled)
                                    binding.quality5.setImageResource(R.drawable.star_filled)
                                }
                            }
                        }
                    }





                        if (data["numberOfReplies"] != null) {
                            val numberOfRepliesI = data["numberOfReplies"] as Number
                            numberOfReplies = numberOfRepliesI.toInt()
                        }


                        storage.reference.child("users/$user/profileImage/$userImageId.png").downloadUrl.addOnSuccessListener { userUri ->

                            val newReview = Reviews(
                                chefEmail,
                                chefImageId,
                                date,
                                itemType,
                                itemTitle,
                                liked,
                                numberOfReplies,
                                menuItemId,
                                reviewItemID,
                                user,
                                userChefRatingI,
                                userExpectationsRatingI,
                                userImageId,
                                userUri,
                                userQualityRatingI,
                                userRecommendation,
                                userReviewTextField
                            )

                            if (reviews.isEmpty()) {
                                reviews.add(newReview)
                                reviewsAdapter.submitList(reviews)
                                reviewsAdapter.notifyItemInserted(0)
                            } else {
                                val index = reviews.indexOfFirst { it.reviewItemID == reviewItemID }
                                if (index == -1) {
                                    reviews.add(newReview)
                                    reviewsAdapter.submitList(reviews)
                                    reviewsAdapter.notifyItemInserted(reviews.size - 1)
                                }
                            }

                        }

                        storage.reference.child("users/$user/profileImage/$userImageId.png").downloadUrl.addOnFailureListener {

                            val newReview = Reviews(
                                chefEmail,
                                chefImageId,
                                date,
                                itemType,
                                itemTitle,
                                liked,
                                numberOfReplies,
                                menuItemId,
                                reviewItemID,
                                user,
                                userChefRatingI,
                                userExpectationsRatingI,
                                userImageId,
                                Uri.EMPTY,
                                userQualityRatingI,
                                userRecommendation,
                                userReviewTextField
                            )

                            if (reviews.isEmpty()) {
                                reviews.add(newReview)
                                reviewsAdapter.submitList(reviews)
                                reviewsAdapter.notifyItemInserted(0)
                            } else {
                                val index = reviews.indexOfFirst { it.reviewItemID == reviewItemID }
                                if (index == -1) {
                                    reviews.add(newReview)
                                    reviewsAdapter.submitList(reviews)
                                    reviewsAdapter.notifyItemInserted(reviews.size - 1)
                                }
                            }
                        }


                    }
                }
            }
        }

    private fun displayAlert(
        message: String
    ) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
                .setTitle("Failed to load page.")
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
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val uri: Uri = data?.data!!
                imageList.add(SlideModel(uri.toString(), "", ScaleTypes.CENTER_CROP))
                var path = "mealKitDelivery/$mealKitOrderId${imgArr1.count() - 1}.png"
                imgArr1.add(MenuItemImage(uri, path))
                if (mealKitNewOrEdit == "edit") {
                    storage.reference.child(path)
                        .putFile(Uri.parse(imageList[imgArr1.count() - 1].imageUrl))
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



