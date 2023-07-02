package com.ruh.taiste.both

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.ruh.taiste.R
import com.ruh.taiste.databinding.ActivityProfileAsUserBinding
import com.ruh.taiste.both.adapters.menuItems.CateringAdapter
import com.ruh.taiste.both.adapters.menuItems.MealKitAdapter
import com.ruh.taiste.both.adapters.menuItems.PersonalChefAdapter
import com.ruh.taiste.both.models.VideoModel
import com.ruh.taiste.chef.adapters.ChefContentAdapter
import com.ruh.taiste.user.adapters.me.UserChefsAdapter
import com.ruh.taiste.user.adapters.me.UserLikesAdapter
import com.ruh.taiste.user.adapters.me.UserOrdersAdapter
import com.ruh.taiste.user.adapters.me.UserReviewsAdapter
import com.ruh.taiste.user.models.*
import com.ruh.taiste.user.userImage
import com.ruh.taiste.user.userImageId
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import com.ruh.taiste.user.MainActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

private const val TAG = "ProfileAsUser"
class ProfileAsUser : AppCompatActivity() {

    private lateinit var binding : ActivityProfileAsUserBinding
    
    private val db = Firebase.firestore
    private val storage = Firebase.storage
//    private val user = FirebaseAuth.getInstance().currentUser!!.email!!

    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())


    private lateinit var cateringAdapter : CateringAdapter
    private lateinit var personalChefAdapter : PersonalChefAdapter
    private lateinit var mealKitAdapter : MealKitAdapter

    private var cateringItems : MutableList<FeedMenuItems> = ArrayList()
    private var personalChefItems : MutableList<PersonalChefInfo> = ArrayList()
    private var mealKitItems : MutableList<FeedMenuItems> = ArrayList()

    private var content: MutableList<VideoModel> = arrayListOf()
    private lateinit var chefContentAdapter: ChefContentAdapter


    private lateinit var ordersAdapter: UserOrdersAdapter
    private lateinit var chefsAdapter: UserChefsAdapter
    private lateinit var likesAdapter: UserLikesAdapter
    private lateinit var reviewsAdapter: UserReviewsAdapter

    private var orders : MutableList<UserOrders> = ArrayList()
    private var chefs : MutableList<UserChefs> = ArrayList()
    private var likes : MutableList<UserLikes> = ArrayList()
    private var reviews : MutableList<UserReviews> = ArrayList()


    private var toggle = ""

    private var chefOrUser = ""
    private var profileUser = ""
    private var userName = ""
    private var chefEmail = ""

    private var city = ""
    private var state = ""
    private var zipCode = ""

    private var chefContentVariable = ""

    private var userEmail = ""
    private var receiverChefOrUser = ""


    private var personalChefInfo: PersonalChefInfo? = null
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileAsUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chefContentVariable = intent.getStringExtra("chef_content_variable").toString()
        toggle = "Cater Items"
        val uri = intent.data
        if (uri != null) {
            Log.d(TAG, "onStart: $uri")
            val params = uri.pathSegments
            val id = params[params.size - 1]
            Toast.makeText(this, id, Toast.LENGTH_LONG).show()

            chefOrUser = "chef"
            profileUser = id

        } else {
            chefOrUser = intent.getStringExtra("chef_or_user").toString()
            profileUser = intent.getStringExtra("user").toString()
            userName = intent.getStringExtra("user_name").toString()

        }



        if (chefOrUser == "chef" || chefOrUser == "Chef") {

            binding.chefProfileLayout.visibility = View.VISIBLE
            binding.userProfileLayout.visibility = View.GONE

            binding.chefToggleLayout.visibility = View.VISIBLE
            binding.userToggleLayout.visibility = View.GONE
            binding.userName.text = "@$userName"

            cateringAdapter = CateringAdapter(this@ProfileAsUser, cateringItems, "user", city, state, zipCode)
            personalChefAdapter = PersonalChefAdapter(this@ProfileAsUser, personalChefItems, "user", city, state, zipCode)
            mealKitAdapter = MealKitAdapter(this@ProfileAsUser, mealKitItems, "user", city, state, zipCode)
            chefContentAdapter = ChefContentAdapter(this@ProfileAsUser, content, "")

            binding.recycler1View.layoutManager = LinearLayoutManager(this@ProfileAsUser)
            binding.recycler2View.layoutManager = LinearLayoutManager(this@ProfileAsUser)
            binding.recycler3View.layoutManager = LinearLayoutManager(this@ProfileAsUser)

            binding.recycler1View.adapter = cateringAdapter
            binding.recycler2View.adapter = personalChefAdapter
            binding.recycler3View.adapter = mealKitAdapter
            binding.contentView.adapter = chefContentAdapter

            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {
                loadChefInfo()
                loadChefItems(toggle)
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }

        } else {
            binding.chefProfileLayout.visibility = View.GONE
            binding.userProfileLayout.visibility = View.VISIBLE

            binding.chefToggleLayout.visibility = View.GONE
            binding.userToggleLayout.visibility = View.VISIBLE
            binding.userName.text = "@$userName"

            ordersAdapter = UserOrdersAdapter(this, orders)
            chefsAdapter = UserChefsAdapter(this, chefs)
            likesAdapter = UserLikesAdapter(this, likes)
            reviewsAdapter = UserReviewsAdapter(this, reviews)

            binding.recycler1View.layoutManager = LinearLayoutManager(this)
            binding.recycler2View.layoutManager = LinearLayoutManager(this)
            binding.recycler3View.layoutManager = LinearLayoutManager(this)
            binding.recycler4View.layoutManager = LinearLayoutManager(this)

            binding.recycler1View.adapter = ordersAdapter
            binding.recycler2View.adapter = chefsAdapter
            binding.recycler3View.adapter = likesAdapter
            binding.recycler4View.adapter = reviewsAdapter
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {
                loadPersonalInfo()
                toggle = "Orders"
                loadOrders()
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }

        }


        binding.catering.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            toggle = "Cater Items"
            loadChefItems(toggle)
            binding.catering.isSelected = true
            binding.personalChef.isSelected = false
            binding.mealKit.isSelected = false
            binding.content.isSelected = false
            binding.noItemsText.isVisible = false

            binding.recycler1View.isVisible = true
            binding.recycler2View.isVisible = false
            binding.recycler3View.isVisible = false
            binding.contentView.isVisible = false

            binding.recycler1View.scrollToPosition(0)

            binding.catering.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.catering.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.personalChef.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.personalChef.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.mealKit.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.mealKit.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.content.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.content.setTextColor(ContextCompat.getColor(this, R.color.main))
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.personalChef.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            toggle = "Executive Items"
            loadPersonalChefInfo()
            binding.catering.isSelected = false
            binding.personalChef.isSelected = true
            binding.mealKit.isSelected = false
            binding.content.isSelected = false
            binding.noItemsText.isVisible = false

            binding.recycler1View.isVisible = false
            binding.recycler2View.isVisible = true
            binding.recycler3View.isVisible = false
            binding.contentView.isVisible = false

            binding.recycler2View.scrollToPosition(0)

            binding.catering.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.catering.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.personalChef.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.personalChef.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.mealKit.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.mealKit.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.content.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.content.setTextColor(ContextCompat.getColor(this, R.color.main))
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.mealKit.setOnClickListener {
            Toast.makeText(this, "Coming Soon.", Toast.LENGTH_LONG).show()
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

//            var abc = toggle
//            toggle = "MealKit Items"
//                loadChefItems(toggle)
//                binding.catering.isSelected = false
//                binding.personalChef.isSelected = false
//                binding.mealKit.isSelected = true
//                binding.content.isSelected = false
//                binding.noItemsText.isVisible = false
//
//                binding.recycler1View.isVisible = false
//                binding.recycler2View.isVisible = false
//                binding.recycler3View.isVisible = true
//                binding.contentView.isVisible = false
//
//
//                binding.recycler3View.scrollToPosition(0)
//
//                binding.catering.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
//                binding.catering.setTextColor(ContextCompat.getColor(this, R.color.main))
//                binding.personalChef.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
//                binding.personalChef.setTextColor(ContextCompat.getColor(this, R.color.main))
//                binding.mealKit.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
//                binding.mealKit.setTextColor(ContextCompat.getColor(this, R.color.white))
//                binding.content.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
//                binding.content.setTextColor(ContextCompat.getColor(this, R.color.main))
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }


        }

        binding.backButton.setOnClickListener {
            if (chefContentVariable == "yes") {
                var intent = if (FirebaseAuth.getInstance().currentUser!!.displayName!! == "Chef") {
                    Intent(this, com.ruh.taiste.chef.MainActivity::class.java)
                } else {
                    Intent(this, MainActivity::class.java)
                }
                startActivity(intent)
            } else {
                onBackPressedDispatcher.onBackPressed()
            }

        }

        binding.content.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            toggle = "Content"
            loadVideos()
            binding.catering.isSelected = false
            binding.personalChef.isSelected = false
            binding.mealKit.isSelected = false
            binding.content.isSelected = true
            binding.noItemsText.isVisible = false

            binding.recycler1View.isVisible = false
            binding.recycler2View.isVisible = false
            binding.recycler3View.isVisible = false
            binding.contentView.isVisible  = true

            binding.contentView.scrollTo(0,0)

            binding.catering.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.catering.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.personalChef.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.personalChef.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.mealKit.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.mealKit.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.content.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.content.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.orders.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            loadOrders()
            binding.orders.isSelected = true
            binding.chefs.isSelected = false
            binding.likes.isSelected = false
            binding.reviews.isSelected = false
            binding.noItemsText.isVisible = false

            binding.recycler1View.scrollToPosition(0)

            binding.orders.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.orders.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.chefs.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.chefs.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.likes.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.likes.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.reviews.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.reviews.setTextColor(ContextCompat.getColor(this, R.color.main))

            binding.recycler1View.isVisible = true
            binding.recycler2View.isVisible = false
            binding.recycler3View.isVisible = false
            binding.recycler4View.isVisible = false
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.chefs.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            loadChefs()
            binding.orders.isSelected = true
            binding.chefs.isSelected = false
            binding.likes.isSelected = false
            binding.reviews.isSelected = false
            binding.noItemsText.isVisible = false

            binding.recycler1View.isVisible = false
            binding.recycler2View.isVisible = true
            binding.recycler3View.isVisible = false
            binding.recycler4View.isVisible = false

            binding.recycler2View.scrollToPosition(0)

            binding.orders.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.orders.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.chefs.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.chefs.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.likes.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.likes.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.reviews.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.reviews.setTextColor(ContextCompat.getColor(this, R.color.main))
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.likes.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            loadLikes()
            binding.orders.isSelected = true
            binding.chefs.isSelected = false
            binding.likes.isSelected = false
            binding.reviews.isSelected = false
            binding.noItemsText.isVisible = false

            binding.recycler3View.scrollToPosition(0)

            binding.orders.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.orders.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.chefs.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.chefs.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.likes.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.likes.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.reviews.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.reviews.setTextColor(ContextCompat.getColor(this, R.color.main))

            binding.recycler1View.isVisible = false
            binding.recycler2View.isVisible = false
            binding.recycler3View.isVisible = true
            binding.recycler4View.isVisible = false
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.reviews.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            loadReviews()
            binding.recycler1View.isVisible = false
            binding.recycler2View.isVisible = false
            binding.recycler3View.isVisible = false
            binding.recycler4View.isVisible = true
            binding.noItemsText.isVisible = false

            binding.orders.isSelected = true
            binding.chefs.isSelected = false
            binding.likes.isSelected = false
            binding.reviews.isSelected = false


            binding.recycler4View.scrollToPosition(0)

            binding.orders.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.orders.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.chefs.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.chefs.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.likes.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.likes.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.reviews.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.reviews.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.messageButton.setOnClickListener {
            if (binding.userName.text != "@chefTest") {
                val intent = Intent(this, Messages::class.java)
                intent.putExtra("chefOrUser", "Chef")
                intent.putExtra("userName", guserName)
                intent.putExtra("user", guserName)
                intent.putExtra("event_type", "na")
                intent.putExtra("userImageId", profileUser)
                intent.putExtra("travel_fee_or_message", "MessageRequests")
                intent.putExtra("user_name", userName)

                //
                val senderImageId = FirebaseAuth.getInstance().currentUser!!.uid
                val receiverImageId = profileUser
                val chefOrUser = FirebaseAuth.getInstance().currentUser!!.displayName
                val documentId = profileUser
                val senderUserName = guserName
                val receiverUserName = binding.userName.text
                val senderEmail = FirebaseAuth.getInstance().currentUser!!.email!!
                val receiverEmail = userEmail

                intent.putExtra("message_request_sender_image_id", senderImageId)
                intent.putExtra("message_request_receiver_image_id", receiverImageId)
                intent.putExtra("message_request_chef_or_user", chefOrUser)
                intent.putExtra("message_request_document_id", documentId)
                intent.putExtra("message_request_receiver_name", receiverUserName)
                intent.putExtra("message_request_sender_name", senderUserName)
                intent.putExtra("message_request_receiver_email", receiverEmail)
                intent.putExtra("message_request_sender_email", senderEmail)
                intent.putExtra("message_request_receiver_chef_or_user", receiverChefOrUser)
                startActivity(intent)

            } else {
                Toast.makeText(this, "This is a test profile.", Toast.LENGTH_LONG).show()
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun loadChefInfo() {
        db.collection("Chef").document(profileUser).collection("PersonalInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {

                for (doc in documents.documents) {
                    val data = doc.data

                    val chefPassion = data?.get("chefPassion") as String
                    val city = data["city"] as String
                    val state = data["state"] as String
                    val education = data["education"] as String
                    val username = data["chefName"] as String
                    val email = data["email"] as String

                        val pictureId = profileUser
                    userName = username
                    chefEmail = email

                    userEmail = email
                    receiverChefOrUser = "Chef"

                        storage.reference.child("chefs/$email/profileImage/$pictureId.png").downloadUrl.addOnSuccessListener { chefUri ->
                            Glide.with(this).load(chefUri).placeholder(R.drawable.default_profile).into(binding.userImage)
                        }

                        binding.chefLocation.text = "Location:  $city, $state"
                        binding.chefPassion.text = chefPassion
                        binding.chefEducation.text = "Education: $education"
                        binding.userName.text = "@$username"

                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Suppress("UNCHECKED_CAST")
    private fun loadChefItems(toggle: String) {
            db.collection("Chef").document(profileUser).collection(toggle).get().addOnSuccessListener { documents ->
                if (documents != null) {

                    for (doc in documents.documents) {
                        val data = doc.data

                        val chefEmail = data?.get("chefEmail") as String
                        val chefPassion = data["chefPassion"] as String
                        val chefUsername = data["chefUsername"] as String
                        val chefImageId = data["profileImageId"] as String
                        val menuItemId = data["randomVariable"] as String
                        val itemTitle = data["itemTitle"] as String
                        val itemDescription = data["itemDescription"] as String
                        val itemPrice = data["itemPrice"] as String
                        val date = data["date"]
                        val imageCount = data["imageCount"] as Number
//                    val itemCalories = data String,
                        val itemType = data["itemType"] as String
                        val city = data["city"] as String
                        val state = data["state"] as String
                        val user = data["user"] as String
                        val healthy = data["healthy"] as Number
                        val creative = data["creative"] as Number
                        val vegan = data["vegan"] as Number
                        val burger = data["burger"] as Number
                        val seafood = data["seafood"] as Number
                        val pasta = data["pasta"] as Number
                        val workout = data["workout"] as Number
                        val lowCal = data["lowCal"] as Number
                        val lowCarb = data["lowCarb"] as Number


                        db.collection(toggle).document(doc.id).get().addOnSuccessListener { document ->
                            if (document != null) {
                                val data1 = document.data

                                val liked = data1?.get("liked") as ArrayList<String>
                                val itemOrders = data1["itemOrders"] as Number
                                val itemRating = data1["itemRating"] as ArrayList<Number>


                                val newItem = FeedMenuItems(chefEmail, chefPassion, chefUsername, chefImageId, Uri.EMPTY, menuItemId, Uri.EMPTY, itemTitle, itemDescription, itemPrice, liked, itemOrders, itemRating, "$date", imageCount, "0", itemType, city, state, user, healthy, creative, vegan, burger, seafood, pasta, workout, lowCal, lowCarb)

                                if (toggle == "Cater Items") {
                                    if (cateringItems.size == 0) {
                                        cateringItems.add(newItem)
                                        cateringAdapter.submitList(cateringItems)
                                        cateringAdapter.notifyItemInserted(0)
                                    } else {
                                        val index =
                                            cateringItems.indexOfFirst { it.menuItemId == menuItemId }
                                        if (index == -1) {
                                            cateringItems.add(newItem)
                                            cateringAdapter.submitList(cateringItems)
                                            cateringAdapter.notifyItemInserted(cateringItems.size - 1)
                                        }
                                    }
                                } else {
                                    if (mealKitItems.size == 0) {
                                        mealKitItems.add(newItem)
                                        mealKitAdapter.submitList(mealKitItems)
                                        mealKitAdapter.notifyItemInserted(0)
                                    } else {
                                        val index =
                                            mealKitItems.indexOfFirst { it.menuItemId == menuItemId }
                                        if (index == -1) {
                                            mealKitItems.add(newItem)
                                            mealKitAdapter.submitList(mealKitItems)
                                            mealKitAdapter.notifyItemInserted(mealKitItems.size - 1)
                                        }
                                    }
                                }


                            }


                            binding.progressBar.isVisible = false

                        }
                    }

                        }

            }

    }

    private fun loadPersonalChefInfo() {
        db.collection("Chef").document(profileUser).collection("Executive Items").get().addOnSuccessListener { documents ->
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
                        val chefName = data["chefName"] as String
                        val chefEmail = data["chefEmail"] as String
                        val chefImageId = data["chefImageId"] as String
                        val city = data["city"] as String
                        val state = data["state"] as String
                        val zipCode = data["zipCode"] as String
                        val trialRun = data["trialRun"] as Number
                        val liked = data["liked"] as ArrayList<String>
                        val itemOrders = data["itemOrders"] as Number
                        val itemRating = data["itemRating"] as ArrayList<Double>
                        val signatureDishId = data["signatureDishId"] as String

                        var availability = ""

                        if (personalChefItems.size == 0) {
                            val info = PersonalChefInfo(
                                chefName,
                                chefEmail,
                                chefImageId,
                                Uri.EMPTY,
                                city,
                                state,
                                zipCode,
                                Uri.EMPTY,
                                signatureDishId,
                                "",
                                "",
                                "",
                                "",
                                briefIntroduction,
                                lengthOfPersonalChef,
                                specialty,
                                whatHelpsYouExcel,
                                mostPrizedAccomplishment,
                                "",
                                "",
                                servicePrice,
                                trialRun.toInt(),
                                weeks.toInt(),
                                months.toInt(),
                                liked,
                                itemOrders.toInt(),
                                itemRating,
                                expectations.toInt(),
                                chefRating.toInt(),
                                quality.toInt(),
                                doc.id,
                                ""
                            )
                            personalChefItems.add(info)
                            personalChefAdapter.submitList(personalChefItems)
                            personalChefAdapter.notifyItemInserted(0)
                        }

            }
        }
    }}}

    private var createdAt = 0
    private fun loadVideos() {

        binding.progressBar.isVisible = true
        Log.d(TAG, "loadVideos: $createdAt")
        val body = FormBody.Builder()
            .add("name", userName)
            .build()

        val request = Request.Builder()
            .url("https://taiste-video.onrender.com/get-user-videos")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    displayAlert("Error: $e")
                }

                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        displayAlert(
                            "Error: $response"
                        )
                    } else {
                        val responseData = response.body!!.string()
                        val responseJson =
                            JSONObject(responseData)

                        val videos = responseJson.getJSONArray("videos")

                        Log.d(TAG, "onResponse: $videos")

                        mHandler.post {
                            binding.progressBar.isVisible = false
                            if (videos.length() == 0) {
                                binding.noItemsText.text = "No Content Items Yet."
                                binding.contentView.isVisible = false
                                binding.noItemsText.isVisible = true
                            }

                                for (i in 0 until videos.length()) {
                                    val id = videos.getJSONObject(i)["id"].toString()
                                    val createdAtI = videos.getJSONObject(i)["createdAt"].toString()
                                    if (i == videos.length() - 1) {
                                        createdAt = createdAtI.toInt()
                                    }
                                    var views = 5
                                    var liked = arrayListOf<String>()
                                    var comments = 0
                                    var shared = 0
                                    binding.progressBar.isVisible = false

                                    val name = videos.getJSONObject(i)["name"].toString()

                                    if (name != "sample" && name != "sample1") {

                                        db.collection("Videos").document(id).get()
                                            .addOnSuccessListener { document ->
                                                if (document.exists()) {
                                                    val data = document.data

                                                    if (data?.get("views") != null) {
                                                        val viewsI = data["views"] as Number
                                                        views = viewsI.toInt()
                                                    }
                                                    if (data?.get("comments") != null) {
                                                        val commentsI = data["comments"] as Number
                                                        comments = commentsI.toInt()
                                                    }

                                                    if (data?.get("liked") != null) {
                                                        val likedI =
                                                            data["liked"] as java.util.ArrayList<String>
                                                        liked = likedI
                                                    }

                                                    if (data?.get("shared") != null) {
                                                        val sharedI =
                                                            data["shared"] as Number
                                                        shared = sharedI.toInt()
                                                    }
                                                    val newVideo = VideoModel(
                                                        videos.getJSONObject(i)["dataUrl"].toString(),
                                                        id,
                                                        createdAtI,
                                                        videos.getJSONObject(i)["name"].toString(),
                                                        videos.getJSONObject(i)["description"].toString(),
                                                        views,
                                                        liked,
                                                        comments,
                                                        shared,
                                                        videos.getJSONObject(i)["thumbnailUrl"].toString()
                                                    )

                                                    if (content.isEmpty()) {
                                                        content.add(newVideo)
                                                        chefContentAdapter.submitList(
                                                            content,
                                                            profileUser
                                                        )
                                                        chefContentAdapter.notifyDataSetChanged()
                                                    } else {
                                                        val index =
                                                            content.indexOfFirst { it.id == id }
                                                        if (index == -1) {
                                                            content.add(newVideo)
                                                            chefContentAdapter.submitList(
                                                                content,
                                                                profileUser
                                                            )
                                                            chefContentAdapter.notifyDataSetChanged()
                                                        }
                                                    }
                                                } else {
                                                    val newVideo = VideoModel(
                                                        videos.getJSONObject(i)["dataUrl"].toString(),
                                                        id,
                                                        createdAtI,
                                                        videos.getJSONObject(i)["name"].toString(),
                                                        videos.getJSONObject(i)["description"].toString(),
                                                        views,
                                                        liked,
                                                        comments,
                                                        shared,
                                                        videos.getJSONObject(i)["thumbnailUrl"].toString()
                                                    )

                                                    if (content.isEmpty()) {
                                                        content.add(newVideo)
                                                        chefContentAdapter.submitList(
                                                            content,
                                                            profileUser
                                                        )
                                                        chefContentAdapter.notifyDataSetChanged()
                                                    } else {
                                                        val index =
                                                            content.indexOfFirst { it.id == id }
                                                        if (index == -1) {
                                                            content.add(newVideo)
                                                            chefContentAdapter.submitList(
                                                                content,
                                                                profileUser
                                                            )
                                                            chefContentAdapter.notifyDataSetChanged()
                                                        }
                                                    }
                                                }
                                            }
                                    }
                                    Log.d(TAG, "onResponse: createdAt $createdAtI")

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
                .setTitle("Failed to load page.")
                .setMessage(message)

            builder.setPositiveButton("Ok", null)
            builder
                .create()
                .show()
        }
    }



    @SuppressLint("SetTextI18n")
    private fun loadPersonalInfo() {
        db.collection("User").document(profileUser).collection("PersonalInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    if (data?.get("fullName") != null) {
                        val city = data["city"] as String
                        val state = data["state"] as String
                        val username = data["userName"] as String
                        val email = data["email"] as String
                        val local = data["local"] as Number
                        val region = data["region"] as Number
                        val nation = data["nation"] as Number

                        val burger = data["burger"] as Number
                        val creative = data["creative"] as Number
                        val healthy = data["healthy"] as Number
                        val lowCal = data["lowCal"] as Number
                        val lowCarb = data["lowCarb"] as Number
                        val pasta = data["pasta"] as Number
                        val surpriseMe = data["surpriseMe"] as Number
                        val vegan = data["vegan"] as Number
                        val workout = data["workout"] as Number


                        userName = username
                        userEmail = email
                        receiverChefOrUser = "User"
                        storage.reference.child("users/$email/profileImage/$profileUser.png").downloadUrl.addOnSuccessListener { userUri ->
                            Glide.with(this).load(userUri)
                                .placeholder(R.drawable.default_profile).into(binding.userImage)
                        }



                        if (local.toInt() == 1) {
                            binding.userLocation.text = "Location:  $city, $state"
                        } else if (region.toInt() == 1) {
                            binding.userLocation.text = "Location:  $state"
                        } else if (nation.toInt() == 1) {
                            binding.userLocation.text = "Location:  USA"
                        }
                        binding.userPreference.text = "Preference:"
                        if (burger.toInt() == 1) {
                            binding.userPreference.text = "${binding.userPreference.text} Burger"
                        }
                        if (creative.toInt() == 1) {
                            binding.userPreference.text = "${binding.userPreference.text} Creative"
                        }
                        if (healthy.toInt() == 1) {
                            binding.userPreference.text = "${binding.userPreference.text} Healthy"
                        }
                        if (lowCal.toInt() == 1) {
                            binding.userPreference.text = "${binding.userPreference.text} Low Calorie"
                        }
                        if (lowCarb.toInt() == 1) {
                            binding.userPreference.text = "${binding.userPreference.text} Low Carbs"
                        }
                        if (pasta.toInt() == 1) {
                            binding.userPreference.text = "${binding.userPreference.text} Pasta"
                        }
                        if (vegan.toInt() == 1) {
                            binding.userPreference.text = "${binding.userPreference.text} Vegan"
                        }
                        if (workout.toInt() == 1) {
                            binding.userPreference.text = "${binding.userPreference.text} Workout"
                        }
                        if (surpriseMe.toInt() == 1) {
                            binding.userPreference.text = "${binding.userPreference.text} Surprise Me"
                        }






                        binding.userName.text = "@$username"

                    }
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun loadOrders() {
        binding.progressBar.isVisible = true
        db.collection("User").document(profileUser).collection("Orders").get().addOnSuccessListener { documents ->
            if (documents != null) {
                if (documents.documents.size == 0) {
                    binding.noItemsText.text = "No Order Items Yet."
                    binding.recycler1View.isVisible = false
                    binding.noItemsText.isVisible = true
                    binding.progressBar.isVisible = false
                    binding.progressBar.isVisible = false
                }
                for (doc in documents.documents) {
                    val data = doc.data

                    val chefEmail = data?.get("chefEmail") as String
                    val chefImageId = data["chefImageId"] as String
                    val city = data["city"] as String
                    val state = data["state"] as String
                    val eventDates = data["eventDates"] as ArrayList<String>
                    val itemTitle = data["itemTitle"] as String
                    val itemDescription = data["itemDescription"] as String
                    val menuItemId = data["menuItemId"] as String
                    val orderDate = data["orderDate"] as String
                    val orderUpdate = data["orderUpdate"] as String
                    val totalCostOfEvent = data["totalCostOfEvent"] as Number
                    val travelFee = data["travelFee"] as String
                    val typeOfService = data["typeOfService"] as String
                    val unitPrice = data["unitPrice"] as String
                    val imageCount = data["imageCount"] as Number
                    val liked = data["liked"] as ArrayList<String>
                    val itemOrders = data["itemOrders"] as Number
                    val itemRating = data["itemRating"] as ArrayList<Number>
                    val itemCalories = data["itemCalories"] as String
                    val signatureDishId = data["signatureDishId"] as String
                    val chefUsername = data["chefUsername"] as String

                    val documentId = doc.id


                                val newOrder = UserOrders(chefEmail, chefUsername, chefImageId, Uri.EMPTY, city, state, eventDates, itemTitle, itemDescription, unitPrice, menuItemId, Uri.EMPTY, orderDate, orderUpdate, totalCostOfEvent, travelFee, typeOfService, imageCount, liked, itemOrders, itemRating, itemCalories.toInt(), documentId, signatureDishId)
                                if (orders.isEmpty()) {
                                    orders.add(newOrder)
                                    ordersAdapter.submitList(orders)
                                    ordersAdapter.notifyItemInserted(0)
                                } else {
                                    val index = orders.indexOfFirst { it.documentId == documentId }
                                    if (index == -1) {
                                        orders.add(newOrder)
                                        ordersAdapter.submitList(orders)
                                        ordersAdapter.notifyItemInserted(orders.size - 1)
                                    }
                                }}

            }
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun loadChefs() {
        binding.progressBar.isVisible = true
        db.collection("User").document(profileUser).collection("UserLikes").get().addOnSuccessListener { documents ->
            if (documents != null) {
                if (documents.documents.size == 0) {
                    binding.noItemsText.text = "No Chefs Yet."
                    binding.recycler2View.isVisible = false
                    binding.noItemsText.isVisible = true
                    binding.progressBar.isVisible = false
                    binding.progressBar.isVisible = false
                }
                for (doc in documents.documents) {
                    val data = doc.data

                    val chefEmail = data?.get("chefEmail") as String
                    val chefImageId = data["chefImageId"] as String
                    val chefName = data["chefUsername"] as String
                    val chefPassion = data["chefPassion"] as String
                    val itemOrders = data["itemOrders"] as Number
                    val itemRating = data["itemRating"] as ArrayList<Number>
                    val liked = ArrayList<String>()
                    val index = chefs.indexOfFirst { it.chefEmail == chefEmail }
                    if (index != -1) {
                        chefs[index].chefOrders = chefs[index].chefOrders.toInt() + itemOrders.toInt()
                        for (i in liked) {
                            chefs[index].chefLiked.add(i)
                        }
                        for (i in itemRating) {
                            chefs[index].chefRating.add(i)
                        }
                        chefs[index].timesLiked = chefs[index].timesLiked.toInt() + 1
                        chefsAdapter.notifyDataSetChanged()
                    } else {

                            val newChef = UserChefs(
                                chefEmail,
                                chefImageId,
                                Uri.EMPTY,
                                chefName,
                                chefPassion,
                                0,
                                liked,
                                itemOrders.toInt(),
                                itemRating
                            )

                            if (chefs.isEmpty()) {
                                chefs.add(newChef)
                                chefsAdapter.submitList(chefs)
                                chefsAdapter.notifyItemInserted(0)
                            } else {
                                val index = chefs.indexOfFirst { it.chefEmail == chefEmail }
                                if (index == -1) {
                                    chefs.add(newChef)
                                    chefsAdapter.submitList(chefs)
                                    chefsAdapter.notifyItemInserted(chefs.size - 1)
                                } else {
                                    chefs[index].timesLiked = chefs[index].timesLiked.toInt() + 1
                                    chefsAdapter.notifyDataSetChanged()
                                }
                            }
                        }

                    }
                    binding.progressBar.isVisible = false


                }
            }

    }

    @SuppressLint("SetTextI18n")
    private fun loadLikes() {
        binding.progressBar.isVisible = true
        db.collection("User").document(profileUser).collection("UserLikes").get().addOnSuccessListener { documents ->
            if (documents != null) {
                if (documents.documents.size == 0) {
                    binding.noItemsText.text = "No Likes Yet."
                    binding.recycler3View.isVisible = false
                    binding.noItemsText.isVisible = true
                    binding.progressBar.isVisible = false
                    binding.progressBar.isVisible = false
                }
                for (doc in documents.documents) {
                    val data = doc.data

                    val chefEmail = data?.get("chefEmail") as String
                    val chefImageId = data["chefImageId"] as String
                    val imageCount = data["imageCount"] as Number
                    val itemDescription = data["itemDescription"] as String
                    val city = data["city"] as String
                    val state = data["state"] as String
                    val itemPrice = data["itemPrice"] as String
                    val itemTitle = data["itemTitle"] as String
                    val itemType = data["itemType"] as String
                    val documentId = doc.id
                    val chefUsername = data["chefUsername"] as String

                    db.collection(itemType).document(documentId).get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val data1 = document.data

                            val liked = data1?.get("liked") as ArrayList<String>
                            val orders = data1["itemOrders"] as Number
                            val rating = data1["itemRating"] as ArrayList<Number>

                                val newLike = UserLikes(
                                    chefEmail,
                                    chefUsername,
                                    chefImageId,
                                    Uri.EMPTY,
                                    city,
                                    state,
                                    itemType,
                                    itemTitle,
                                    itemDescription,
                                    itemPrice,
                                    Uri.EMPTY,
                                    imageCount,
                                    liked,
                                    orders,
                                    rating,
                                    0,
                                    documentId,
                                    profileUser
                                ,"signatureDishId"
                                )

                                if (likes.isEmpty()) {
                                    likes.add(newLike)
                                    likesAdapter.submitList(likes)
                                    likesAdapter.notifyItemInserted(0)
                                } else {
                                    val index = likes.indexOfFirst { it.documentId == documentId }
                                    if (index == -1) {
                                        likes.add(newLike)
                                        likesAdapter.submitList(likes)
                                        likesAdapter.notifyItemInserted(likes.size - 1)
                                    }
                                }

                            }

                    }}
                        binding.progressBar.isVisible = false
                    }

                }

    }

    @SuppressLint("SetTextI18n")
    private fun loadReviews() {
        binding.progressBar.isVisible = true
        db.collection("User").document(profileUser).collection("UserReviews").get().addOnSuccessListener { documents ->
            if (documents != null) {
                if (documents.documents.size == 0) {
                    binding.noItemsText.text = "No Reviews Yet."
                    binding.recycler4View.isVisible = false
                    binding.noItemsText.isVisible = true
                    binding.progressBar.isVisible = false
                    binding.progressBar.isVisible = false
                }
                for (doc in documents.documents) {
                    val data = doc.data

                    val chefEmail = data?.get("chefEmail") as String
                    val chefImageId = data["chefImageId"] as String
                    val chefUsername = data["chefUsername"] as String
                    val date = data["date"] as String
                    val documentID = doc.id
                    val itemTitle = data["itemTitle"] as String
                    val itemType = data["itemType"] as String
                    val liked = data["liked"] as ArrayList<String>
                    val reviewItemID = doc.id
                    val user = data["chefEmail"] as String
                    val userChefRating = data["chefRating"] as Number
                    val userExpectationsRating = data["expectations"] as Number
                    val qualityRating = data["quality"] as Number
                    val userRecommendation = data["recommend"] as Number
                    val userReviewTextField = data["thoughts"] as String

                       val newReview = UserReviews(chefEmail, chefImageId, Uri.EMPTY, chefUsername, date, documentID, itemTitle, itemType, liked, reviewItemID, user, userChefRating, userExpectationsRating, userImageId, qualityRating, userRecommendation, userReviewTextField)

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


                    binding.progressBar.isVisible = false



                }
            }
        }
    }
}