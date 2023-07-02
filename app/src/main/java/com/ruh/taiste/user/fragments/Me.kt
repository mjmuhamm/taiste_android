package com.ruh.taiste.user.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ruh.taiste.R
import com.ruh.taiste.databinding.FragmentMeBinding
import com.ruh.taiste.user.AccountSettings
import com.ruh.taiste.user.adapters.me.UserChefsAdapter
import com.ruh.taiste.user.adapters.me.UserLikesAdapter
import com.ruh.taiste.user.adapters.me.UserOrdersAdapter
import com.ruh.taiste.user.adapters.me.UserReviewsAdapter
import com.ruh.taiste.user.userImage
import com.ruh.taiste.user.userImageId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.both.Notifications
import com.ruh.taiste.both.guserName
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import com.ruh.taiste.user.adapters.checkout.CreditsAdapter
import com.ruh.taiste.user.models.*
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


private const val TAG = "Me"
class Me : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentMeBinding? = null

    private val binding get() = _binding!!

    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private lateinit var userOrdersAdapter: UserOrdersAdapter
    private lateinit var userChefsAdapter: UserChefsAdapter
    private lateinit var userLikesAdapter: UserLikesAdapter
    private lateinit var userReviewsAdapter: UserReviewsAdapter
    private lateinit var userCreditsAdapter: CreditsAdapter

    private var userOrders: MutableList<UserOrders> = ArrayList()
    private var userChefs: MutableList<UserChefs> = ArrayList()
    private var userLikes: MutableList<UserLikes> = ArrayList()
    private var userReviews: MutableList<UserReviews> = ArrayList()
    private var userCredits: MutableList<Credits> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeBinding.inflate(inflater, container, false)

        userOrdersAdapter = UserOrdersAdapter(requireContext(), userOrders)
        userChefsAdapter = UserChefsAdapter(requireContext(), userChefs)
        userLikesAdapter = UserLikesAdapter(requireContext(), userLikes)
        userReviewsAdapter = UserReviewsAdapter(requireContext(), userReviews)
        userCreditsAdapter = CreditsAdapter(requireContext(), userCredits)

        binding.ordersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chefsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.likesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.reviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.creditsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.ordersRecyclerView.adapter = userOrdersAdapter
        binding.chefsRecyclerView.adapter = userChefsAdapter
        binding.likesRecyclerView.adapter = userLikesAdapter
        binding.reviewsRecyclerView.adapter = userReviewsAdapter
        binding.creditsRecyclerView.adapter = userCreditsAdapter




        binding.orders.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

            if (FirebaseAuth.getInstance().currentUser != null) {
                loadOrders()
            } else {
                Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            binding.orders.isSelected = true
            binding.chefs.isSelected = false
            binding.likes.isSelected = false
            binding.reviews.isSelected = false
//            binding.credits.isSelected = false
            binding.noItemsText.isVisible = false

            binding.ordersRecyclerView.isVisible = true
            binding.chefsRecyclerView.isVisible = false
            binding.likesRecyclerView.isVisible = false
            binding.reviewsRecyclerView.isVisible = false
            binding.creditsRecyclerView.isVisible = false

            binding.ordersRecyclerView.scrollToPosition(0)

            binding.orders.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.secondary
                )
            )
            binding.orders.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.chefs.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.chefs.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.likes.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.likes.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.reviews.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.reviews.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
//            binding.credits.setBackgroundColor(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.white
//                )
//            )
//            binding.credits.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.chefs.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

            if (FirebaseAuth.getInstance().currentUser != null) {
                loadChefs()
            } else {
                Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            binding.orders.isSelected = false
            binding.chefs.isSelected = true
            binding.likes.isSelected = false
            binding.reviews.isSelected = false
//            binding.credits.isSelected = false
            binding.noItemsText.isVisible = false

            binding.ordersRecyclerView.isVisible = false
            binding.chefsRecyclerView.isVisible = true
            binding.likesRecyclerView.isVisible = false
            binding.reviewsRecyclerView.isVisible = false
            binding.creditsRecyclerView.isVisible = false

            binding.chefsRecyclerView.scrollToPosition(0)

            binding.orders.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.orders.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.chefs.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.secondary
                )
            )
            binding.chefs.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.likes.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.likes.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.reviews.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white))
            binding.reviews.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
//            binding.credits.setBackgroundColor(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.white
//                )
//            )
//            binding.credits.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.likes.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

            if (FirebaseAuth.getInstance().currentUser != null) {
                loadLikes()
            } else {
                Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            binding.orders.isSelected = false
            binding.chefs.isSelected = false
            binding.likes.isSelected = true
            binding.reviews.isSelected = false
//            binding.credits.isSelected = false
            binding.noItemsText.isVisible = false

            binding.ordersRecyclerView.isVisible = false
            binding.chefsRecyclerView.isVisible = false
            binding.likesRecyclerView.isVisible = true
            binding.reviewsRecyclerView.isVisible = false
            binding.creditsRecyclerView.isVisible = false

            binding.likesRecyclerView.scrollToPosition(0)

            binding.orders.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.orders.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.chefs.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.chefs.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.likes.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.secondary
                )
            )
            binding.likes.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.reviews.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.reviews.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
//            binding.credits.setBackgroundColor(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.white
//                )
//            )
//            binding.credits.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.reviews.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

            if (FirebaseAuth.getInstance().currentUser != null) {
                loadReviews()
            } else {
                Toast.makeText(requireContext(), "Something went wrong. Plesae check your connection.", Toast.LENGTH_LONG).show()
            }
            binding.orders.isSelected = false
            binding.chefs.isSelected = false
            binding.likes.isSelected = false
            binding.reviews.isSelected = true
//            binding.credits.isSelected = false
            binding.noItemsText.isVisible = false

            binding.ordersRecyclerView.isVisible = false
            binding.chefsRecyclerView.isVisible = false
            binding.likesRecyclerView.isVisible = false
            binding.reviewsRecyclerView.isVisible = true
            binding.creditsRecyclerView.isVisible = false

            binding.reviewsRecyclerView.scrollToPosition(0)

            binding.orders.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.orders.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.chefs.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.chefs.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.likes.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.likes.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.reviews.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.secondary
                )
            )
            binding.reviews.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
//            binding.credits.setBackgroundColor(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.white
//                )
//            )
//            binding.credits.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }


//        binding.credits.setOnClickListener {
//
//            loadCredits()
//            binding.orders.isSelected = false
//            binding.chefs.isSelected = false
//            binding.likes.isSelected = false
//            binding.reviews.isSelected = false
//            binding.credits.isSelected = true
//            binding.noItemsText.isVisible = false
//
//            binding.ordersRecyclerView.isVisible = false
//            binding.chefsRecyclerView.isVisible = false
//            binding.likesRecyclerView.isVisible = false
//            binding.reviewsRecyclerView.isVisible = false
//            binding.creditsRecyclerView.isVisible = true
//
//            binding.reviewsRecyclerView.scrollToPosition(0)
//
//            binding.orders.setBackgroundColor(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.white
//                )
//            )
//            binding.orders.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
//            binding.chefs.setBackgroundColor(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.white
//                )
//            )
//            binding.chefs.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
//            binding.likes.setBackgroundColor(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.white
//                )
//            )
//            binding.likes.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
//            binding.reviews.setBackgroundColor(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.white
//                )
//            )
//            binding.reviews.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
//            binding.credits.setBackgroundColor(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.secondary
//                )
//            )
//            binding.credits.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
//
//        }

        binding.settingsButton.setOnClickListener {
            val intent = Intent(requireContext(), AccountSettings::class.java)
            startActivity(intent)
        }

        binding.notificationsButton.setOnClickListener {
            binding.notificationsImage.visibility = View.GONE
            val intent = Intent(requireContext(), Notifications::class.java)
            intent.putExtra("chef_or_user", "User")
            startActivity(intent)
        }

        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(requireContext())
            } else {
                isOnline1(requireContext())
            }
        ) {

        if (FirebaseAuth.getInstance().currentUser != null) {
            loadUsername()
            loadNotifications()
            loadPersonalInfo()
            loadOrders()
        } else {
            Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
        }
        } else {
            Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }


        return binding.root
    }

    private fun loadUsername() {
        db.collection("Usernames").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val email = data?.get("email") as String
                    val username = data["username"] as String

                    if (email == FirebaseAuth.getInstance().currentUser!!.email!!) {
                        guserName = username
                    }
                }
            }
        }
    }

    private fun loadNotifications() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).addSnapshotListener { document, _ ->
                if (document != null) {
                    val data = document.data
                    val notifications = data?.get("notifications") as String
                    if (notifications == "yes") {
                        binding.notificationsImage.visibility = View.VISIBLE
                    } else {
                        binding.notificationsImage.visibility = View.GONE
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Something went wrong. Please check connection.", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadPersonalInfo() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("PersonalInfo").get()
                .addOnSuccessListener { documents ->
                    if (documents != null) {

                        for (doc in documents.documents) {
                            val data = doc.data

                            if (data?.get("fullName") != null) {
                                val city = data["city"] as String
                                val state = data["state"] as String
                                val username = data["userName"] as String
                                val burger = data["burger"] as Number
                                val creative = data["creative"] as Number
                                val healthy = data["healthy"] as Number
                                val lowCal = data["lowCal"] as Number
                                val lowCarb = data["lowCarb"] as Number
                                val pasta = data["pasta"] as Number
                                val seafood = data["seafood"] as Number
                                val vegan = data["vegan"] as Number
                                val workout = data["workout"] as Number

                                binding.preferences.text = "Preferences:"
                                if (burger.toInt() == 1) {
                                    binding.preferences.text = "${binding.preferences.text}  Burger"
                                }
                                if (creative.toInt() == 1) {
                                    binding.preferences.text =
                                        "${binding.preferences.text}  Creative"
                                }
                                if (healthy.toInt() == 1) {
                                    binding.preferences.text =
                                        "${binding.preferences.text}  Healthy"
                                }
                                if (lowCal.toInt() == 1) {
                                    binding.preferences.text =
                                        "${binding.preferences.text}  Low Calorie"
                                }
                                if (lowCarb.toInt() == 1) {
                                    binding.preferences.text =
                                        "${binding.preferences.text}  Low Carb"
                                }
                                if (pasta.toInt() == 1) {
                                    binding.preferences.text = "${binding.preferences.text}  Pasta"
                                }
                                if (seafood.toInt() == 1) {
                                    binding.preferences.text =
                                        "${binding.preferences.text}  Seafood"
                                }
                                if (vegan.toInt() == 1) {
                                    binding.preferences.text = "${binding.preferences.text}  Vegan"
                                }
                                if (workout.toInt() == 1) {
                                    binding.preferences.text =
                                        "${binding.preferences.text}  Workout"
                                }


                                if (userImage != null) {
                                    Glide.with(requireContext()).load(userImage)
                                        .placeholder(R.drawable.default_profile)
                                        .into(binding.userImage)
                                } else {
                                    userImageId = FirebaseAuth.getInstance().currentUser!!.uid

                                    Log.d(TAG, "loadPersonalInfo: email ${FirebaseAuth.getInstance().currentUser!!.email!!}")
                                    storage.reference.child("users/${FirebaseAuth.getInstance().currentUser!!.email!!}/profileImage/${FirebaseAuth.getInstance().currentUser!!.uid}.png").downloadUrl.addOnSuccessListener { userUri ->
                                        Glide.with(requireContext()).load(userUri)
                                            .placeholder(R.drawable.default_profile)
                                            .into(binding.userImage)
                                        userImage = userUri
                                    }
                                }

                                if (city == "" && state != "") {
                                    binding.userLocation.text = "Location: $state"
                                } else if (city != "") {
                                    binding.userLocation.text = "Location: $city, $state"
                                } else {
                                    binding.userLocation.text = "Location: Nationwide"
                                }

                                binding.userName.text = "@$username"

                            }
                        }
                    }
                }
        } else {
            Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun loadOrders() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            binding.progressBar.isVisible = true
            db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("Orders").get()
                .addOnSuccessListener { documents ->
                    if (documents != null) {
                        if (documents.documents.size == 0) {
                            binding.noItemsText.text = "No Orders Yet."
                            binding.noItemsText.isVisible = true
                            binding.ordersRecyclerView.isVisible = false
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

                                        val newOrder = UserOrders(
                                            chefEmail,
                                            chefUsername,
                                            chefImageId,
                                            Uri.EMPTY,
                                            city,
                                            state,
                                            eventDates,
                                            itemTitle,
                                            itemDescription,
                                            unitPrice,
                                            menuItemId,
                                            Uri.EMPTY,
                                            orderDate,
                                            orderUpdate,
                                            totalCostOfEvent,
                                            travelFee,
                                            typeOfService,
                                            imageCount,
                                            liked,
                                            itemOrders,
                                            itemRating,
                                            itemCalories.toInt(),
                                            documentId,
                                            signatureDishId
                                        )
                                        if (userOrders.isEmpty()) {
                                            userOrders.add(newOrder)
                                            userOrdersAdapter.submitList(userOrders)
                                            userOrdersAdapter.notifyItemInserted(0)
                                        } else {
                                            val index =
                                                userOrders.indexOfFirst { it.documentId == documentId }
                                            if (index == -1) {
                                                userOrders.add(newOrder)
                                                userOrdersAdapter.submitList(userOrders)
                                                userOrdersAdapter.notifyItemInserted(userOrders.size - 1)
                                            }


                            binding.progressBar.isVisible = false


                        }
                    }

        } else {
                Toast.makeText(
                    requireContext(),
                    "Something went wrong. Please check your connection.",
                    Toast.LENGTH_LONG
                ).show()
            }}}
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun loadChefs() {
        binding.progressBar.isVisible = true
        db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("UserLikes").get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    if (documents.documents.size == 0) {
                        binding.noItemsText.text = "No Chefs Yet."
                        binding.noItemsText.isVisible = true
                        binding.chefsRecyclerView.isVisible = false
                        binding.progressBar.isVisible = false
                    }
                    for (doc in documents.documents) {
                        val data = doc.data

                        val chefEmail = data?.get("chefEmail") as String
                        val chefImageId = data["chefImageId"] as String
                        val chefName = data["chefUsername"] as String
                        val chefPassion = data["chefPassion"] as String
                        val liked = ArrayList<String>()
                        val itemOrders = data["itemOrders"] as Number
                        val itemRating = data["itemRating"] as ArrayList<Number>
                        val index = userChefs.indexOfFirst { it.chefImageId == chefImageId }

                        if (index != -1) {
                            userChefs[index].chefOrders =
                                userChefs[index].chefOrders.toInt() + itemOrders.toInt()
                            for (i in liked) {
                                userChefs[index].chefLiked.add(i)
                            }
                            for (i in itemRating) {
                                userChefs[index].chefRating.add(i)
                            }
                            userChefs[index].timesLiked = userChefs[index].timesLiked.toInt() + 1
                            userChefsAdapter.notifyDataSetChanged()
                        } else {
                            val newChef = UserChefs(
                                chefEmail,
                                chefImageId,
                                Uri.EMPTY,
                                chefName,
                                chefPassion,
                                0,
                                liked,
                                itemOrders,
                                itemRating
                            )


                            if (userChefs.isEmpty()) {
                                userChefs.add(newChef)
                                userChefsAdapter.submitList(userChefs)
                                userChefsAdapter.notifyItemInserted(0)
                            } else {
                                val index = userChefs.indexOfFirst { it.chefEmail == chefEmail }
                                if (index == -1) {
                                    userChefs.add(newChef)
                                    userChefsAdapter.submitList(userChefs)
                                    userChefsAdapter.notifyItemInserted(userChefs.size - 1)
                                }
                            }
                        }

                    }}}
                        binding.progressBar.isVisible = false

    }

    @SuppressLint("SetTextI18n")
    private fun loadLikes() {
        binding.progressBar.isVisible = true
        db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("UserLikes").get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    if (documents.documents.size == 0) {
                        binding.noItemsText.text = "No Likes Yet."
                        binding.noItemsText.isVisible = true
                        binding.likesRecyclerView.isVisible = false
                        binding.progressBar.isVisible = false
                    }
                    for (doc in documents.documents) {
                        val data = doc.data

                        val chefEmail = data?.get("chefEmail") as String
                        val chefImageId = data["chefImageId"] as String
                        val chefUsername = data["chefUsername"] as String
                        val imageCount = data["imageCount"] as Number
                        val itemDescription = data["itemDescription"] as String
                        val city = data["city"] as String
                        val state = data["state"] as String
                        val itemPrice = data["itemPrice"] as String
                        val itemTitle = data["itemTitle"] as String
                        val itemType = data["itemType"] as String
                        val documentId = doc.id
//                        val signatureDishId = data["signatureDishId"] as String

                        db.collection(itemType).document(documentId).get()
                            .addOnSuccessListener { document ->
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
                                         FirebaseAuth.getInstance().currentUser!!.uid,
                                        "signatureDishId"
                                    )

                                    if (userLikes.isEmpty()) {
                                        userLikes.add(newLike)
                                        userLikesAdapter.submitList(userLikes)
                                        userLikesAdapter.notifyItemInserted(0)
                                    } else {
                                        val index =
                                            userLikes.indexOfFirst { it.documentId == documentId }
                                        if (index == -1) {
                                            userLikes.add(newLike)
                                            userLikesAdapter.submitList(userLikes)
                                            userLikesAdapter.notifyItemInserted(userLikes.size - 1)
                                        }}

                                }
                            binding.progressBar.isVisible = false
                        }

                    }
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun loadReviews() {
        binding.progressBar.isVisible = true
        db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("UserReviews").get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    if (documents.documents.size == 0) {
                        binding.noItemsText.text = "No Reviews Yet."
                        binding.noItemsText.isVisible = true
                        binding.reviewsRecyclerView.isVisible = false
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
                        val userChefRating = data["chefRating"] as Number
                        val userExpectationsRating = data["expectations"] as Number
                        val qualityRating = data["quality"] as Number
                        val userRecommendation = data["recommend"] as Number
                        val userReviewTextField = data["thoughts"] as String

                            val newReview = UserReviews(
                                chefEmail,
                                chefImageId,
                                Uri.EMPTY,
                                chefUsername,
                                date,
                                documentID,
                                itemTitle,
                                itemType,
                                liked,
                                reviewItemID,
                                FirebaseAuth.getInstance().currentUser!!.uid,
                                userChefRating,
                                userExpectationsRating,
                                userImageId,
                                qualityRating,
                                userRecommendation,
                                userReviewTextField
                            )

                            if (userReviews.isEmpty()) {
                                userReviews.add(newReview)
                                userReviewsAdapter.submitList(userReviews)
                                userReviewsAdapter.notifyItemInserted(0)
                            } else {
                                val index =
                                    userReviews.indexOfFirst { it.reviewItemID == reviewItemID }
                                if (index == -1) {
                                    userReviews.add(newReview)
                                    userReviewsAdapter.submitList(userReviews)
                                    userReviewsAdapter.notifyItemInserted(userReviews.size - 1)
                                }
                            }

                        binding.progressBar.isVisible = false


                    }
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun loadCredits() {
        binding.progressBar.isVisible = true
        db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Credits").get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    if (documents.documents.size == 0) {
                        binding.noItemsText.text = "No Reviews Yet."
                        binding.noItemsText.isVisible = true
                        binding.reviewsRecyclerView.isVisible = false
                        binding.progressBar.isVisible = false
                    }
                    for (doc in documents.documents) {
                        val data = doc.data
                        var refund = data?.get("refund") as Number
                        val orderDate = data["orderDate"] as String
                        val itemTitle = data["itemTitle"] as String
                        val paymentIntent = data["paymentIntent"] as String
                        var credits = data["credits"] as Number
                        val creditsApplied = data["creditsApplied"] as String
                        var creditAmount = 0.0

                        if (data["creditAmount"] != null) {
                            val a = data["creditAmount"] as Number
                            creditAmount = a.toDouble()
                        }

                        if (creditsApplied == "refund") {
                            if (creditAmount != credits) {
                                credits = creditAmount + credits.toDouble()
                                refund = creditAmount + refund.toDouble()
                            }
                        } else if (creditsApplied == "finished") {
                            if (creditAmount != credits) {
                                credits = credits.toDouble() - creditAmount
                                refund = refund.toDouble() - creditAmount
                            }
                        }

                        if (creditsApplied != "Yes") {

                            val newCredit = Credits(
                                refund,
                                orderDate,
                                itemTitle,
                                paymentIntent,
                                credits,
                                creditsApplied,
                                creditAmount,
                                doc.id
                            )
                            if (userCredits.isEmpty()) {
                                userCredits.add(newCredit)
                                userCreditsAdapter.submitList(userCredits)
                                userCreditsAdapter.notifyItemInserted(0)
                            } else {
                                val index = userCredits.indexOfFirst { it.documentId == doc.id }
                                if (index == -1) {
                                    userCredits.add(newCredit)
                                    userCreditsAdapter.submitList(userCredits)
                                    userCreditsAdapter.notifyItemInserted(userCredits.size - 1)
                                }
                            }



                            binding.progressBar.isVisible = false

                        }
                    }
                }
            }
    }
}