package com.ruh.taiste.chef.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ruh.taiste.R
import com.ruh.taiste.both.adapters.menuItems.CateringAdapter
import com.ruh.taiste.both.adapters.menuItems.MealKitAdapter
import com.ruh.taiste.both.adapters.menuItems.PersonalChefAdapter
import com.ruh.taiste.both.models.VideoModel
import com.ruh.taiste.chef.*
import com.ruh.taiste.chef.adapters.ChefContentAdapter
import com.ruh.taiste.chef.models.BankAccount
import com.ruh.taiste.chef.models.Person
import com.ruh.taiste.databinding.FragmentMeChefBinding
import com.ruh.taiste.user.models.FeedMenuItems
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.both.Notifications
import com.ruh.taiste.chef.menu_item_post_guide.GuideToCaterItems
import com.ruh.taiste.chef.menu_item_post_guide.GuideToExecutiveItems
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import com.ruh.taiste.user.models.PersonalChefInfo
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Me.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "Me"

var chefLatitude = ""
var chefLongitude = ""
class Me : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding : FragmentMeChefBinding? = null
    private val binding get() = _binding!!


    private val httpClient = OkHttpClient()

    private val mHandler: Handler = Handler(Looper.getMainLooper())

    private val db = Firebase.firestore
    private val storage = Firebase.storage


    private lateinit var cateringAdapter : CateringAdapter
    private lateinit var personalChefAdapter : PersonalChefAdapter
    private lateinit var mealKitAdapter : MealKitAdapter

    private var cateringItems : MutableList<FeedMenuItems> = ArrayList()
    private var personalChefItems : MutableList<PersonalChefInfo> = ArrayList()
    private var mealKitItems : MutableList<FeedMenuItems> = ArrayList()


    private var content: MutableList<VideoModel> = arrayListOf()
    private lateinit var chefContentAdapter: ChefContentAdapter

    private var bankAccount: BankAccount? = null
    private var toggle = "Cater Items"


    //Bank
    private lateinit var bottomSheetDialog: BottomSheetDialog

    private lateinit var bankName : EditText
    private lateinit var accountHolder: EditText
    private lateinit var accountNumber: EditText
    private lateinit var routingNumber: EditText
    private lateinit var exitButton : TextView
    private lateinit var addButton: MaterialButton

    private lateinit var representative: Person
    private var owners: MutableList<Person> = arrayListOf()

    private var profilePic = ""

    private var cityI = ""
    private var stateI = ""
    private var zipCodeI = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeChefBinding.inflate(inflater, container, false)

        binding.cateringRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        cateringAdapter = CateringAdapter(requireContext(), cateringItems, "chef", "", "", "")
        binding.cateringRecyclerView.adapter = cateringAdapter

        binding.personalChefRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        personalChefAdapter = PersonalChefAdapter(requireContext(), personalChefItems, "chef", "", "", "")
        binding.personalChefRecyclerView.adapter = personalChefAdapter

        binding.mealKitRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        mealKitAdapter = MealKitAdapter(requireContext(), mealKitItems, "chef", "", "", "")
        binding.mealKitRecyclerView.adapter = mealKitAdapter
        chefContentAdapter = ChefContentAdapter(requireContext(), content, "")
        binding.contentView.adapter = chefContentAdapter

        //Bank
        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.banking_bottom_sheet, R.id.banking_bottom_sheet as? RelativeLayout)

        bankName = bottomSheetView.findViewById(R.id.bank_name)
        accountHolder = bottomSheetView.findViewById(R.id.account_holder)
        accountNumber = bottomSheetView.findViewById(R.id.account_number)
        routingNumber = bottomSheetView.findViewById(R.id.routing_number)
        exitButton = bottomSheetView.findViewById(R.id.exit_button)
        addButton = bottomSheetView.findViewById(R.id.add_button)

        bottomSheetDialog.setContentView(bottomSheetView)

        addButton.visibility = View.GONE
        binding.catering.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {
                payout()
            toggle = "Cater Items"
            if (FirebaseAuth.getInstance().currentUser != null) {
                loadItems(toggle)
            } else {
                Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            binding.catering.isSelected = true
            binding.personalChef.isSelected = false
            binding.mealKit.isSelected = false
            binding.content.isSelected = false
            binding.bankingButton.isSelected = false
            binding.noItemsText.isVisible = false

            binding.cateringRecyclerView.isVisible = true
            binding.personalChefRecyclerView.isVisible = false
            binding.mealKitRecyclerView.isVisible = false
            binding.contentView.isVisible = false
            binding.bankingView.isVisible = false


            binding.cateringRecyclerView.scrollToPosition(0)

            binding.catering.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.catering.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.personalChef.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.personalChef.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.mealKit.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.mealKit.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.content.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.content.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.bankingButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.bankingButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.personalChef.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

            toggle = "Executive Items"
            if (FirebaseAuth.getInstance().currentUser != null) {
                loadPersonalChefItems()
            } else {
                Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            binding.catering.isSelected = false
            binding.personalChef.isSelected = true
            binding.mealKit.isSelected = false
            binding.content.isSelected = false
            binding.bankingButton.isSelected = false
            binding.noItemsText.isVisible = false

            binding.cateringRecyclerView.isVisible = false
            binding.personalChefRecyclerView.isVisible = true
            binding.mealKitRecyclerView.isVisible = false
            binding.contentView.isVisible = false
            binding.bankingView.isVisible = false


            binding.personalChefRecyclerView.scrollToPosition(0)

            binding.catering.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.catering.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.personalChef.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.personalChef.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.mealKit.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.mealKit.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.content.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.content.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.bankingButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.bankingButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.mealKit.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {
                Toast.makeText(requireContext(), "Coming Soon.", Toast.LENGTH_LONG).show()

//            val abc = toggle
//            toggle = "MealKit Items"
//            if (FirebaseAuth.getInstance().currentUser != null) {
//                loadItems(toggle)
//                binding.catering.isSelected = false
//                binding.personalChef.isSelected = false
//                binding.mealKit.isSelected = true
//                binding.content.isSelected = false
//                binding.bankingButton.isSelected = false
//                binding.noItemsText.isVisible = false
//
//                binding.cateringRecyclerView.isVisible = false
//                binding.personalChefRecyclerView.isVisible = false
//                binding.mealKitRecyclerView.isVisible = true
//                binding.contentView.isVisible = false
//                binding.bankingView.isVisible = false
//
//                binding.mealKitRecyclerView.scrollToPosition(0)
//
//                binding.catering.setBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.white
//                    )
//                )
//                binding.catering.setTextColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.main
//                    )
//                )
//                binding.personalChef.setBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.white
//                    )
//                )
//                binding.personalChef.setTextColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.main
//                    )
//                )
//                binding.mealKit.setBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.secondary
//                    )
//                )
//                binding.mealKit.setTextColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.white
//                    )
//                )
//                binding.content.setBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.white
//                    )
//                )
//                binding.content.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
//                binding.bankingButton.setBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.white
//                    )
//                )
//                binding.bankingButton.setTextColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.main
//                    )
//                )
//
//        }  else {
//            Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
//            }
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.content.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

            toggle = "Content"
            if (FirebaseAuth.getInstance().currentUser != null) {
                loadVideos()
            } else {
                Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            binding.catering.isSelected = false
            binding.personalChef.isSelected = false
            binding.mealKit.isSelected = false
            binding.content.isSelected = true
            binding.bankingButton.isSelected = false
            binding.noItemsText.isVisible = false

            binding.cateringRecyclerView.isVisible = false
            binding.personalChefRecyclerView.isVisible = false
            binding.mealKitRecyclerView.isVisible = false
            binding.contentView.isVisible = true
            binding.bankingView.isVisible = false

            binding.contentView.scrollTo(0,0)

            binding.catering.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.catering.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.personalChef.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.personalChef.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.mealKit.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.mealKit.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.content.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.content.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.bankingButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.bankingButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.addItemButton.setOnClickListener {
            if (profilePic != "yes") {
                Toast.makeText(requireContext(), "Please upload a profile pic before continuing.", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(requireContext(), MenuItemAdd::class.java)
                intent.putExtra("new_or_edit", "new")
                intent.putExtra("item_label", toggle)
                intent.putExtra("city", cityI)
                intent.putExtra("state", stateI)
                intent.putExtra("zipCode", zipCodeI)

                val intent1 = Intent(requireContext(), ContentAdd::class.java)

                val intent2 = Intent(requireContext(), GuideToCaterItems::class.java)
                val intent3 = Intent(requireContext(), GuideToExecutiveItems::class.java)
                if (toggle != "Content") {
                    if (toggle == "Cater Items" && cateringItems.size == 0) {
                        startActivity(intent2)
                    } else if (toggle == "Executive Items" && personalChefItems.size == 0) {
                        startActivity(intent3)
                    } else {
                        startActivity(intent)
                    }
                } else {
                    startActivity(intent1)
                }
            }
        }

        binding.bankingButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

            if (FirebaseAuth.getInstance().currentUser != null) {
                loadBankingInfo()
            } else {
                Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            binding.catering.isSelected = false
            binding.personalChef.isSelected = false
            binding.mealKit.isSelected = false
            binding.content.isSelected = false
            binding.bankingButton.isSelected = true

            binding.cateringRecyclerView.isVisible = false
            binding.personalChefRecyclerView.isVisible = false
            binding.mealKitRecyclerView.isVisible = false
            binding.mealKitRecyclerView.isVisible = false
            binding.contentView.isVisible = false
            binding.bankingView.isVisible = true

            binding.catering.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.catering.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.personalChef.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.personalChef.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.mealKit.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.mealKit.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.bankingButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.bankingButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.content.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.content.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.editAccountButton.setOnClickListener {
            bottomSheetDialog.show()
            bankName.setText(bankAccount!!.bankName)
            accountHolder.setText(bankAccount!!.accountHolder)
            accountNumber.setText("****${bankAccount!!.accountNumber.toInt()}")
            routingNumber.setText(bankAccount!!.routingNumber.toString())

        }

        exitButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }


        binding.settingsButton.setOnClickListener {
            val intent = Intent(requireContext(), AccountSettings::class.java)
            startActivity(intent)
        }

        binding.notificationsButton.setOnClickListener {
            binding.notificationsImage.visibility = View.GONE
            val intent = Intent(requireContext(), Notifications::class.java)
            intent.putExtra("chef_or_user", "Chef")
            startActivity(intent)
        }


        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(requireContext())
            } else {
                isOnline1(requireContext())
            }
        ) {
            loadNotifications()
            loadPersonalInfo()
            loadItems(toggle)
        } else {
            binding.progressBar.isVisible = true
            Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }


        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Me().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun loadNotifications() {
        Log.d(TAG, "loadNotifications: ${FirebaseAuth.getInstance().currentUser!!.displayName}")
        Log.d(TAG, "loadNotifications: ${FirebaseAuth.getInstance().currentUser!!.uid}")
        if (FirebaseAuth.getInstance().currentUser != null) {
            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).addSnapshotListener { document, _ ->
                if (document != null) {
                    val data = document.data
                    val notifications = data?.get("notifications") as String
                    val profilePic = data["profilePic"] as String
                    this.profilePic = profilePic
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
        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("PersonalInfo").addSnapshotListener  { documents, error ->
            if (error == null) {
                if (documents != null) {
                    for (doc in documents.documents) {
                        val data = doc.data

                        val chefPassionI = data?.get("chefPassion") as String
                        val education = data["education"] as String
                        val city = data["city"] as String
                        val state = data["state"] as String
                        val username = data["chefName"] as String
                        val zipCode = data["zipCode"] as String
                        val email = data["email"] as String


                        binding.cateringRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                        cateringAdapter = CateringAdapter(requireContext(), cateringItems, "chef", city, state, zipCode)
                        binding.cateringRecyclerView.adapter = cateringAdapter

                        binding.personalChefRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                        personalChefAdapter = PersonalChefAdapter(requireContext(), personalChefItems, "chef", city, state, zipCode)
                        binding.personalChefRecyclerView.adapter = personalChefAdapter

                        binding.mealKitRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                        mealKitAdapter = MealKitAdapter(requireContext(), mealKitItems, "chef", city, state, zipCode)
                        binding.mealKitRecyclerView.adapter = mealKitAdapter


                        binding.chefEducation.text = "Education: $education"
                        binding.chefPassion.text = chefPassionI
                        binding.chefLocation.text = "Location: $city, $state"
                        binding.chefName.text = "@$username"
                        cityI = city
                        stateI = state
                        zipCodeI = zipCode




                        chefEducation = education
                        chefPassion = chefPassionI
                        chefLocation = "$city, $state"
                        chefImageId = FirebaseAuth.getInstance().currentUser!!.uid
                        chefUsername = username



                        storage.reference.child("chefs/$email/profileImage/${FirebaseAuth.getInstance().currentUser!!.uid}.png").downloadUrl.addOnSuccessListener { chefUri ->
                            chefImage = chefUri
                            if (activity != null) {
                                if (this != null) {
                                    Glide.with(this).load(chefUri)
                                        .placeholder(R.drawable.default_profile)
                                        .into(binding.chefImage)
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Suppress("UNCHECKED_CAST")
    private fun loadItems(toggle: String) {
        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection(toggle).addSnapshotListener { documents, error ->
            if (error == null) {
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
                        val liked = data["liked"] as ArrayList<String>
                        val itemOrders = data["itemOrders"] as Number
                        val itemRating = data["itemRating"] as ArrayList<Number>
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



                        binding.addItemButton.isVisible = true
                               val newItem = FeedMenuItems(
                                    chefEmail,
                                    chefPassion,
                                    chefUsername,
                                    chefImageId,
                                   Uri.EMPTY,
                                    menuItemId,
                                   Uri.EMPTY,
                                    itemTitle,
                                    itemDescription,
                                    itemPrice,
                                    liked,
                                    itemOrders,
                                    itemRating,
                                    "$date",
                                    imageCount,
                                    "0",
                                    itemType,
                                    city,
                                    state,
                                    user,
                                    healthy,
                                    creative,
                                    vegan,
                                    burger,
                                    seafood,
                                    pasta,
                                    workout,
                                    lowCal,
                                    lowCarb
                                )

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


                            binding.progressBar.isVisible = false
//                        if ((toggle == "Cater Items" && cateringItems.size == 0) ) {
//                            binding.noItemsText.text = "No Cater Items Yet."
//                            binding.cateringRecyclerView.isVisible = false
//                            binding.noItemsText.isVisible = true
//                            binding.progressBar.isVisible = false
//                        } else if (toggle == "Executive Items" && personalChefItems.size == 0) {
//                            binding.noItemsText.text = "No Personal Chef Items Yet."
//                            binding.personalChefRecyclerView.isVisible = false
//                            binding.progressBar.isVisible = false
//                            binding.noItemsText.isVisible = true
//                        } else if (toggle == "MealKit Items" && mealKitItems.size == 0) {
//                            binding.noItemsText.text = "No MealKit Items Yet."
//                            binding.mealKitRecyclerView.isVisible = false
//                            binding.noItemsText.isVisible = true
//                            binding.progressBar.isVisible = false
//                        }


                        }
                    }

                }
            }
        }

    }

    private fun loadPersonalChefItems() {
        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Executive Items").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val typeOfInfo = data?.get("typeOfService") as String


                    if (typeOfInfo == "info") {
                        val complete = data["complete"] as String
                        if (complete == "yes") {
                    val briefIntroduction = data["briefIntroduction"] as String
                    val lengthOfPersonalChef = data["lengthOfPersonalChef"] as String
                    val specialty = data["specialty"] as String
                    val servicePrice = data["servicePrice"] as String
                    val expectations = data["expectations"] as Number
                    val chefRating = data["chefRating"] as Number
                    val quality = data["quality"] as Number
                    val chefName = data["chefName"] as String
                    val whatHelpsYouExcel = data["whatHelpsYouExcel"] as String
                    val mostPrizedAccomplishment = data["mostPrizedAccomplishment"] as String
                    val weeks = data["weeks"] as Number
                    val chefEmail = data["chefEmail"] as String
                    val chefImageId = data["chefImageId"] as String
                    val months = data["months"] as Number
                    val city = data["city"] as String
                    val state = data["state"] as String
                    val zipCode = data["zipCode"] as String
                    val trialRun = data["trialRun"] as Number
                    val hourlyOrPerSession = data["hourlyOrPerSession"] as String
                    val liked = data["liked"] as ArrayList<String>
                    val itemOrders = data["itemOrders"] as Number
                    val itemRating = data["itemRating" ] as  ArrayList<Double>
                    val openToMenuRequests = data["openToMenuRequests"] as String
                    val signatureDishId = data["signatureDishId"] as String

                    var availability = ""
                    if (trialRun != 0) {
                        availability = "Trial Run"
                    }
                    if (weeks != 0) {
                        availability = "$availability  Weeks"
                    }
                    if (months != 0) {
                        availability = "$availability  Months"
                    }

                            binding.addItemButton.isVisible = false



                            val personalChefItem = PersonalChefInfo(chefName, chefEmail, chefImageId, Uri.EMPTY, city, state, zipCode, Uri.EMPTY, signatureDishId, "", "", "", "", briefIntroduction, lengthOfPersonalChef, specialty, whatHelpsYouExcel, mostPrizedAccomplishment, availability, hourlyOrPerSession, servicePrice, trialRun.toInt(), weeks.toInt(), months.toInt(), liked, itemOrders.toInt(), itemRating, expectations.toInt(), chefRating.toInt(), quality.toInt(), doc.id, openToMenuRequests)
                            if (personalChefItems.size == 0) {
                                personalChefItems.add(personalChefItem)
                                personalChefAdapter.submitList(personalChefItems)
                                personalChefAdapter.notifyItemInserted(0)
                            }
                        }}}}}}

    private var createdAt = 0
    private fun loadVideos() {
        binding.addItemButton.isVisible = true
//        binding.progressBar.isVisible = true
        Log.d(TAG, "loadVideos: $createdAt")
        val body = FormBody.Builder()
            .add("name", FirebaseAuth.getInstance().currentUser!!.email!!)
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


                                db.collection("Videos").document(id).get().addOnSuccessListener { document ->
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
                                            val likedI = data["liked"] as java.util.ArrayList<String>
                                            liked = likedI
                                        }

                                        if (data?.get("shared") != null) {
                                            val sharedI = data["shared"] as Number
                                            shared = sharedI.toInt()
                                        }
                                        val newVideo = VideoModel(videos.getJSONObject(i)["dataUrl"].toString(), id, createdAtI, videos.getJSONObject(i)["name"].toString(), videos.getJSONObject(i)["description"].toString(), views, liked, comments, shared, videos.getJSONObject(i)["thumbnailUrl"].toString())

                                        if (content.isEmpty()) {
                                            content.add(newVideo)
                                            chefContentAdapter.submitList(content, chefImageId)
                                            chefContentAdapter.notifyDataSetChanged()
                                        } else {
                                            val index = content.indexOfFirst { it.id == id }
                                            if (index == -1) {
                                                content.add(newVideo)
                                                chefContentAdapter.submitList(content, chefImageId)
                                                chefContentAdapter.notifyDataSetChanged()
                                            }
                                        }
                                    } else {
                                        val newVideo = VideoModel(videos.getJSONObject(i)["dataUrl"].toString(), id, createdAtI, videos.getJSONObject(i)["name"].toString(), videos.getJSONObject(i)["description"].toString(), views, liked, comments, shared, videos.getJSONObject(i)["thumbnailUrl"].toString())

                                        if (content.isEmpty()) {
                                            content.add(newVideo)
                                            chefContentAdapter.submitList(content, chefImageId)
                                            chefContentAdapter.notifyDataSetChanged()
                                        } else {
                                            val index = content.indexOfFirst { it.id == id }
                                            if (index == -1) {
                                                content.add(newVideo)
                                                chefContentAdapter.submitList(content, chefImageId)
                                                chefContentAdapter.notifyDataSetChanged()
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

    private fun payout() {
        @SuppressLint("SimpleDateFormat")
        var sdfT = SimpleDateFormat("MM-dd-yyyy hh:mm a")

        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").addSnapshotListener { documents, error ->
            if (error == null) {
                if (documents != null) {
                    for (doc in documents.documents) {

                        val data = doc.data

                        val eventDates = data?.get("eventDates") as java.util.ArrayList<String>
                        val eventTimes = data["eventTimes"] as java.util.ArrayList<String>
                        val chefImageId = data["chefImageId"] as String
                        val userImageId = data["userImageId"] as String
                        val totalCostOfEvent = data["totalCostOfEvent"] as Number
                        val menuItemId = data["menuItemId"] as String
                        var payoutDates = data["payoutDates"] as java.util.ArrayList<String>
                        val typeOfService = data["typeOfService"] as String


                        var date = ""
                        var index = 0


                        for (i in 0 until eventDates.size) {
                            if (i == 0) {
                                date = "${eventDates[i]} ${eventTimes[i]}"
                                index = 0
                            } else {
                                val time = sdfT.parse(date).time
                                val day = "${eventDates[i]} ${eventTimes[i]}"
                                val dayT = sdfT.parse(day).time
                                if (dayT < time) {
                                    date = day
                                    index = i
                                }
                            }
                            if (i == eventDates.size - 1) {
                                val time = (sdfT.parse(date).time / 1000 / 60 / 60)
                                val today = (System.currentTimeMillis() / 1000 / 60 / 60) + 1


                                if (today >= time) {
                                    var dates = eventDates
                                    var times = eventTimes

                                    if (!payoutDates.contains(date)) {


                                        if (payoutDates.size + 1 == eventDates.size) {
                                            val data6: Map<String, Any> = hashMapOf("orderUpdate" to "orderComplete")
                                            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").document(menuItemId).update(data6)
                                            db.collection("Orders").document(menuItemId).update(data6)
                                        } else {
                                            val data6: Map<String, Any> = hashMapOf("payoutDates" to FieldValue.arrayUnion(date))
                                            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").document(menuItemId).update(data6)
                                            db.collection("Orders").document(menuItemId).update(data6)
                                            payoutDates.add("123")
                                        }
                                        val amountToBePayed = (totalCostOfEvent.toDouble() * 0.95) / (eventDates.size + 1)
                                        transfer(amountToBePayed, doc.id, userImageId, chefImageId, typeOfService)
                                    }

                                }
                            }
                        }
                    }

                }
            }
        }

    }
    private fun transfer(transferAmount: Double, orderId: String, userImageId: String, chefImageId: String, type: String) {
        @SuppressLint("SimpleDateFormat")
        var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener { document ->
            if (document != null) {
                val data = document.data

                val chargeForPayout = data?.get("chargeForPayout") as Double


                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .collection("BankingInfo").get().addOnSuccessListener { documents ->
                        if (documents != null) {
                            for (doc in documents.documents) {
                                val data = doc.data

                                val stripeId = data?.get("stripeAccountId") as String

                                if (transferAmount - chargeForPayout > 0) {
                                    val final = "%.0f".format((
                                            transferAmount + chargeForPayout) * 100)

                                    Log.d(TAG, "transfer: final $final")
                                    val body = FormBody.Builder()
                                        .add("stripeAccountId", stripeId)
                                        .add("amount", final)
                                        .build()

                                    val request = Request.Builder()
                                        .url("https://taiste-payments.onrender.com/transfer")
                                        .addHeader("Content-Type", "application/json; charset=utf-8")
                                        .post(body)
                                        .build()

                                    httpClient.newCall(request)
                                        .enqueue(object : Callback {
                                            override fun onFailure(call: Call, e: IOException) {
                                                mHandler.post {

                                                    val data1: Map<String, Any> = hashMapOf(
                                                        "transferId" to "",
                                                        "orderId" to orderId,
                                                        "date" to sdf.format(Date()),
                                                        "transferAmount" to 0.0,
                                                        "userImageId" to userImageId,
                                                        "chefImageId" to chefImageId
                                                    )
                                                    db.collection("TransferErrors").document(orderId)
                                                        .set(data1)
                                                }
                                            }

                                            override fun onResponse(call: Call, response: Response) {
                                                if (!response.isSuccessful) {
                                                    mHandler.post {
                                                        displayAlert(
                                                            "Error: $response"
                                                        )

                                                        val data1: Map<String, Any> = hashMapOf(
                                                            "transferId" to "",
                                                            "orderId" to orderId,
                                                            "date" to sdf.format(Date()),
                                                            "transferAmount" to 0.0,
                                                            "userImageId" to userImageId,
                                                            "chefImageId" to chefImageId
                                                        )
                                                        db.collection("TransferErrors")
                                                            .document(orderId)
                                                            .set(data1)
                                                    }
                                                } else {
                                                    val responseData = response.body!!.string()
                                                    val json =
                                                        JSONObject(responseData)

                                                    val transferId = json.getString("transferId")

                                                    mHandler.post {
                                                        val data1: Map<String, Any> = hashMapOf(
                                                            "transferId" to transferId,
                                                            "transferAmount" to final,
                                                            "orderId" to orderId,
                                                            "userImageId" to userImageId,
                                                            "date" to Date().toString(),
                                                            "chefImageId" to chefImageId
                                                        )

                                                        Toast.makeText(
                                                            context,
                                                            "Payout on its way",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        db.collection("Transfers").document(orderId)
                                                            .set(data1)

                                                        val data7: Map<String, Any> = hashMapOf("notifications" to "yes")
                                                        val data8: Map<String, Any> = hashMapOf("notification" to "A payout of $$transferAmount is on its way.", "date" to sdf.format(Date()))
                                                        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).update(data7)
                                                        db.collection("Chef")
                                                            .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                                            .collection("Notifications").document(orderId)
                                                            .update(data8)

                                                    }
                                                }
                                            }
                                        })

                                } else {
                                    val amountOwed = chargeForPayout - transferAmount
                                    val data5: Map<String, Any> = hashMapOf("notification" to "You still owed $$chargeForPayout. Your new amount owed is $$amountOwed.", "date" to sdf.format(Date()))
                                    val data6: Map<String, Any> = hashMapOf("notifications" to "yes")
                                    db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Notifications").document().set(data5)
                                    db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).update(data6)
                                }

                            }
                        }
                    }


            }
        }

    }


    private fun loadBankingInfo() {
        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("BankingInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data


                        val accountType = data?.get("accountType") as String
                        val stripeId = data["stripeAccountId"] as String
                        val externalAccount = data["externalAccountId"] as String
                        if (accountType == "Individual") {
                            loadIndividualAccount(stripeId)
                            retrieveExternalAccount(stripeId, externalAccount)
                        } else {
                            loadBusinessAccount(stripeId)
                            retrieveExternalAccount(stripeId, externalAccount)

                        }
                    binding.progressBar.isVisible = false
                    }

            }
        }
    }

    private fun loadIndividualAccount(stripeId: String) {
            binding.progressBar.isVisible = true
            val body = FormBody.Builder()
                .add("stripeAccountId", stripeId)
                .build()

            val request = Request.Builder()
                .url("https://taiste-payments.onrender.com/retrieve-individual-account")
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
                            val json =
                                JSONObject(responseData)

                            val cardPayments = json.getString("card_payments")
                            val transfers = json.getString("transfers")
//
                            val currentlyDue = json.getString("currently_due")
                            val eventuallyDue = json.getString("eventually_due")
                            val currentDeadline = json.getString("current_deadline")

                            val available = json.getString("available").toInt()
                            val pending = json.getString("pending").toInt()


                            mHandler.post {

                                if (cardPayments == "active" && transfers == "active") {
                                    binding.bankingStatusUpdate.text = "Your banking status is good!"
                                    binding.cardPaymentImage.setImageResource(R.drawable.check_circle)
                                    binding.transfersImage.setImageResource(R.drawable.check_circle)
                                } else {
                                    binding.bankingStatusUpdate.text = "Your account needs attention. Please check $currentlyDue, $eventuallyDue, and update by $currentDeadline, or your account may be denied."
                                    binding.cardPaymentImage.setImageResource(R.drawable.warning)
                                    binding.transfersImage.setImageResource(R.drawable.warning)
                                    binding.progressBar.isVisible = false
                                }

                                val pen = pending / 100
                                binding.pendingAmount.text = "$${"%.2f".format(pen.toDouble())}"
                                Log.d(TAG, "onResponse: avail $available")
                                Log.d(TAG, "onResponse: pen $pending")

                                val avail = available / 100
                                binding.availableAmount.text = "$${"%.2f".format(avail.toDouble())}"

                                binding.progressBar.isVisible = false
                            }
                        }
                    }
                })

    }

    private fun loadBusinessAccount(stripeId: String) {

            binding.progressBar.isVisible = true
            val body = FormBody.Builder()
                .add("stripeAccountId", stripeId)
                .build()

            val request = Request.Builder()
                .url("https://taiste-payments.onrender.com/retrieve-business-account")
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
                            val json =
                                JSONObject(responseData)

                            val cardPayments = json.getString("card_payments")
                            val transfers = json.getString("transfers")
//
                            val currentlyDue = json.getString("currently_due")
                            val eventuallyDue = json.getString("eventually_due")
                            val currentDeadline = json.getString("current_deadline")

                            val available = json.getString("available").toInt()
                            val pending = json.getString("pending").toInt()



                            mHandler.post {

                                if (cardPayments == "active" && transfers == "active") {
                                    binding.bankingStatusUpdate.text = "Your banking status is good!"
                                    binding.cardPaymentImage.setImageResource(R.drawable.check_circle)
                                    binding.transfersImage.setImageResource(R.drawable.check_circle)
                                } else {
                                    binding.bankingStatusUpdate.text = "Your account needs attention. Please check $currentlyDue, $eventuallyDue, and update by $currentDeadline, or your account may be denied."
                                    binding.cardPaymentImage.setImageResource(R.drawable.warning)
                                    binding.transfersImage.setImageResource(R.drawable.warning)
                                    binding.progressBar.isVisible = false
                                }


                                val pen = pending / 100
                                binding.pendingAmount.text = "$${"%.2f".format(pen.toDouble())}"
                                Log.d(TAG, "onResponse: avail $available")
                                Log.d(TAG, "onResponse: pen $pending")

                                val avail = available / 100
                                binding.availableAmount.text = "$${"%.2f".format(avail.toDouble())}"
                                binding.progressBar.isVisible = false

                            }
                        }
                    }
                })

        }


    private fun retrieveExternalAccount(stripeId: String, externalAccountId: String) {
        binding.progressBar.isVisible = true
        val body = FormBody.Builder()
            .add("stripeAccountId", stripeId)
            .add("externalAccountId", externalAccountId)
            .build()

        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/retrieve-external-account")
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

                        val bankName = json.getString("bank_name")
                        val accountHolder = json.getString("account_holder")
                        val accountNumber = json.getString("account_number")
                        val routingNumber = json.getString("routing_number")
                        val defaultForCurrency = json.getString("default_for_currency")


                        mHandler.post {

                            Log.d(TAG, "onResponse: $bankName")
                            Log.d(TAG, "onResponse: $accountHolder")
                            Log.d(TAG, "onResponse: $accountNumber")
                            Log.d(TAG, "onResponse: $routingNumber")
                            Log.d(TAG, "onResponse: $defaultForCurrency")

                            binding.accountInfo.text = "****$accountNumber"
                            bankAccount = BankAccount(bankName, accountHolder, accountNumber, routingNumber, externalAccountId)
                        }
                    }
                }
            })

    }

    private fun transfer(stripeId: String) {
        binding.progressBar.isVisible = true
        val body = FormBody.Builder()
            .add("stripeAccountId", stripeId)
            .add("amount", "1558329")
            .build()

        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/transfer")
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
                        val json =
                            JSONObject(responseData)

                        val transferId = json.getString("transferId")


                        mHandler.post {
                            Log.d(TAG, "onResponse: $transferId")
                        }
                    }
                }
            })

    }

    private fun displayAlert(
        message: String
    ) {
        requireActivity().runOnUiThread {
            val builder = AlertDialog.Builder(requireContext())
                .setTitle("Failed to load page.")
                .setMessage(message)

            builder.setPositiveButton("Ok", null)
            builder
                .create()
                .show()
        }
    }
}