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
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.ruh.taiste.R
import com.ruh.taiste.databinding.FragmentHomeBinding
import com.ruh.taiste.user.Checkout
import com.ruh.taiste.user.adapters.home.CateringAdapter
import com.ruh.taiste.user.adapters.home.MealKitAdapter
import com.ruh.taiste.user.adapters.home.PersonalChefAdapter
import com.ruh.taiste.user.models.FeedMenuItems
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.both.guserName
import com.ruh.taiste.chef.chefImage
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import com.ruh.taiste.user.models.Filter
import com.ruh.taiste.user.models.PersonalChefInfo

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val TAG = "Home"
class Home : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val user = FirebaseAuth.getInstance().currentUser!!.email!!

    private var cateringItems: MutableList<FeedMenuItems> = ArrayList()
    private var personalChefItems: MutableList<PersonalChefInfo> = ArrayList()
    private var mealKitItems: MutableList<FeedMenuItems> = ArrayList()

    private lateinit var cateringAdapter : CateringAdapter
    private lateinit var personalChefAdapter : PersonalChefAdapter
    private lateinit var mealKitAdapter : MealKitAdapter

    private lateinit var filter: Filter

    private var toggle = "Cater Items"
    private var cart : MutableList<String> = ArrayList()
    private var totalPrice = 0.0


    //Filter

    private lateinit var bottomSheetDialog: BottomSheetDialog

    private lateinit var localButton: MaterialButton
    private lateinit var regionButton: MaterialButton
    private lateinit var nationButton: MaterialButton
    private lateinit var surpriseMeButton: MaterialButton
    private lateinit var workoutButton: MaterialButton
    private lateinit var cityLayout: LinearLayout
    private lateinit var city: EditText
    private lateinit var state: EditText
    private lateinit var creativeButton: MaterialButton
    private lateinit var healthyButton: MaterialButton
    private lateinit var lowCalButton: MaterialButton
    private lateinit var lowCarbButton: MaterialButton
    private lateinit var veganButton: MaterialButton
    private lateinit var pastaButton: MaterialButton
    private lateinit var seafoodButton: MaterialButton
    private lateinit var burgerButton: MaterialButton
    private lateinit var saveButton: MaterialButton
    private lateinit var exitButton: TextView

    private var local = 0
    private var region = 0
    private var nation = 0

    private var surpriseMe = 0
    private var burger = 0
    private var creative = 0
    private var healthy = 0
    private var lowCal = 0
    private var lowCarb = 0
    private var vegan = 0
    private var workout = 0
    private var seafood = 0
    private var pasta = 0
    private var preferenceDocumentId = ""
    private var personalizeDocumentId = ""

    private var itemLocation = ""
    private var itemLocation2 = ""
    private var itemLocation3 = ""
    var stop = ""

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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        //warning free

        binding.cateringRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        cateringAdapter = CateringAdapter(requireContext(), cateringItems)
        binding.cateringRecyclerView.adapter = cateringAdapter

        binding.personalChefRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        personalChefAdapter = PersonalChefAdapter(requireContext(), personalChefItems)
        binding.personalChefRecyclerView.adapter = personalChefAdapter

        binding.mealKitRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        mealKitAdapter = MealKitAdapter(requireContext(), mealKitItems)
        binding.mealKitRecyclerView.adapter = mealKitAdapter
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(requireContext())
            } else {
                isOnline1(requireContext())
            }
        ) {

        if (FirebaseAuth.getInstance().currentUser != null) {
            loadFilter()
        } else {
            Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
        }
        } else {
            Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }
        binding.catering.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

            stop = ""
            toggle = "Cater Items"
//            loadItems(toggle, itemLocation, "")
            loadItems(filter, "no")
            binding.catering.isSelected = true
            binding.personalChef.isSelected = false
            binding.mealKit.isSelected = false

            binding.cateringRecyclerView.isVisible = true
            binding.personalChefRecyclerView.isVisible = false
            binding.mealKitRecyclerView.isVisible = false

            binding.cateringRecyclerView.scrollToPosition(0)

            binding.catering.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.catering.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.personalChef.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.personalChef.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.mealKit.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.mealKit.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
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

            stop = ""
            toggle = "Executive Items"
            if (FirebaseAuth.getInstance().currentUser != null) {
                loadPersonalChefItems(filter, "")
            } else {
                Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            binding.catering.isSelected = false
            binding.personalChef.isSelected = true
            binding.mealKit.isSelected = false
            binding.progressBar.visibility = View.GONE
            binding.cateringRecyclerView.isVisible = false
            binding.personalChefRecyclerView.isVisible = true
            binding.mealKitRecyclerView.isVisible = false

            binding.personalChefRecyclerView.scrollToPosition(0)

            binding.catering.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.catering.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.personalChef.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.personalChef.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.mealKit.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.mealKit.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.mealKit.setOnClickListener {
            Toast.makeText(requireContext(), "Coming Soon.", Toast.LENGTH_LONG).show()
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

//            stop = ""
//            toggle = "MealKit Items"
//            if (FirebaseAuth.getInstance().currentUser != null) {
//                loadItems(filter, "")
//            } else {
//                Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
//            }
//                binding.catering.isSelected = false
//                binding.personalChef.isSelected = false
//                binding.mealKit.isSelected = true
//
//                binding.cateringRecyclerView.isVisible = false
//                binding.personalChefRecyclerView.isVisible = false
//                binding.mealKitRecyclerView.isVisible = true
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
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }

        }

        //Filter
        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.filter_bottom_sheet, R.id.filter_bottom_sheet as? RelativeLayout)

        localButton = bottomSheetView.findViewById(R.id.local)
        regionButton = bottomSheetView.findViewById(R.id.region)
        nationButton = bottomSheetView.findViewById(R.id.nation)
        cityLayout = bottomSheetView.findViewById(R.id.city_layout)
        city = bottomSheetView.findViewById(R.id.city)
        state = bottomSheetView.findViewById(R.id.state)
        surpriseMeButton = bottomSheetView.findViewById(R.id.surprise_me)
        workoutButton = bottomSheetView.findViewById(R.id.workout)
        creativeButton = bottomSheetView.findViewById(R.id.creative)
        healthyButton = bottomSheetView.findViewById(R.id.healthy)
        lowCalButton = bottomSheetView.findViewById(R.id.low_cal)
        lowCarbButton = bottomSheetView.findViewById(R.id.low_carb)
        veganButton = bottomSheetView.findViewById(R.id.vegan)
        pastaButton = bottomSheetView.findViewById(R.id.pasta)
        seafoodButton = bottomSheetView.findViewById(R.id.seafood)
        burgerButton = bottomSheetView.findViewById(R.id.burger)
        saveButton = bottomSheetView.findViewById(R.id.save_button)
        exitButton = bottomSheetView.findViewById(R.id.exit_button)

        bottomSheetDialog.setContentView(bottomSheetView)

        localButton.setOnClickListener {
            local = 1
            region = 0
            nation = 0
            localButton.isSelected = true
            regionButton.isSelected = false
            nationButton.isSelected = false

            cityLayout.visibility = View.VISIBLE
            city.visibility = View.VISIBLE

            localButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            localButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            regionButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            regionButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            nationButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            nationButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))

        }
        regionButton.setOnClickListener {
            local = 0
            region = 1
            nation = 0
            localButton.isSelected = false
            regionButton.isSelected = true
            nationButton.isSelected = false

            cityLayout.visibility = View.VISIBLE
            city.visibility = View.GONE

            localButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            localButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            regionButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            regionButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            nationButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            nationButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
        nationButton.setOnClickListener {
            local = 0
            region = 0
            nation = 1
            localButton.isSelected = false
            regionButton.isSelected = false
            nationButton.isSelected = true

            cityLayout.visibility = View.GONE

            localButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            localButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            regionButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            regionButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            nationButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            nationButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))

        }

        surpriseMeButton.setOnClickListener {
            surpriseMeButton.isSelected = !surpriseMeButton.isSelected
            surpriseMe = if (surpriseMeButton.isSelected) {
                surpriseMeButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
                surpriseMeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                1
            } else {
                surpriseMeButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                surpriseMeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
                0
            }
        }
        workoutButton.setOnClickListener {
            workoutButton.isSelected = !workoutButton.isSelected
            workout = if (workoutButton.isSelected) {
                workoutButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
                workoutButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                1
            } else {
                workoutButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                workoutButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
                0
            }
        }
        creativeButton.setOnClickListener {
            creativeButton.isSelected = !creativeButton.isSelected
            creative = if (creativeButton.isSelected) {
                creativeButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
                creativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                1
            } else {
                creativeButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                creativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
                0
            }
        }
        healthyButton.setOnClickListener {
            healthyButton.isSelected = !healthyButton.isSelected
            healthy = if (healthyButton.isSelected) {
                healthyButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
                healthyButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                1
            } else {
                healthyButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                healthyButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
                0
            }
        }
        lowCalButton.setOnClickListener {
            lowCalButton.isSelected = !lowCalButton.isSelected
            lowCal = if (lowCalButton.isSelected) {
                lowCalButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
                lowCalButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                1
            } else {
                lowCalButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                lowCalButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
                0
            }
        }
        lowCarbButton.setOnClickListener {
            lowCarbButton.isSelected = !lowCarbButton.isSelected
            lowCarb = if (lowCarbButton.isSelected) {
               lowCarbButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
               lowCarbButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                1
            } else {
                lowCarbButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                lowCarbButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
                0
            }
        }
        veganButton.setOnClickListener {
            veganButton.isSelected = !veganButton.isSelected
            vegan = if (veganButton.isSelected) {
                veganButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
                veganButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                1
            } else {
                veganButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                veganButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
                0
            }
        }
        pastaButton.setOnClickListener {
            pastaButton.isSelected = !pastaButton.isSelected
            pasta = if (pastaButton.isSelected) {
                pastaButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
                pastaButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                1
            } else {
                pastaButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                pastaButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
                0
            }
        }
        seafoodButton.setOnClickListener {
            seafoodButton.isSelected = !seafoodButton.isSelected
            seafood = if (seafoodButton.isSelected) {
                seafoodButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
                seafoodButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                1
            } else {
                seafoodButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                seafoodButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
                0
            }
        }
        burgerButton.setOnClickListener {
            burgerButton.isSelected = !burgerButton.isSelected
            burger = if (burgerButton.isSelected) {
                burgerButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
                burgerButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                1
            } else {
                burgerButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                burgerButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
                0
            }
        }

        binding.filterButton.setOnClickListener {
//            loadPreferences()
            bottomSheetDialog.show()
        }

        saveButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

            if (local == 1 && (city.text.isEmpty() || state.text.isEmpty())) {
                Toast.makeText(requireContext(), "Please enter a city or state for local suggestions.", Toast.LENGTH_LONG).show()
            } else if (region == 1 && (state.text.isEmpty())) {
                Toast.makeText(requireContext(), "Please enter a state for regional suggestions.", Toast.LENGTH_LONG).show()
            } else if (region == 1 && stateFilter(state.text.toString()) != "good") {
                Toast.makeText(requireContext(), "Please enter the abbreviation of your state selection.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Filter Saved.", Toast.LENGTH_LONG).show()
                saveData()
                bottomSheetDialog.dismiss()
            }
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }
        exitButton.setOnClickListener {

            bottomSheetDialog.dismiss()
        }




        binding.checkoutButton.setOnClickListener {
            val intent = Intent(requireContext(), Checkout::class.java)
            startActivity(intent)
        }

        if (FirebaseAuth.getInstance().currentUser != null) {
            loadCart()
//        loadPreferences()
            loadUsername()
        } else {
            Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
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

    @SuppressLint("SetTextI18n")
    private fun loadCart() {
     db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Cart").get().addOnSuccessListener { documents ->
         if (documents != null) {
             for (doc in documents.documents) {
                 val data = doc.data

                 val totalCostOfEvent = data?.get("totalCostOfEvent") as Number

                 if (cart.size == 0) {
                     cart.add(doc.id)
                     totalPrice += totalCostOfEvent.toDouble()
                     val num = "%.2f".format(totalPrice)
                     binding.totalPrice.text = "$$num"
                 } else {
                     val index = cart.indexOfFirst { it == doc.id }
                     if (index == -1) {
                         cart.add(doc.id)
                         totalPrice += totalCostOfEvent.toDouble()
                         val num = "%.2f".format(totalPrice)
                         binding.totalPrice.text = "$$num"
                     }
                 }
             }
     }
     }
    }

    private fun loadFilter() {
            db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("PersonalInfo").get()
                .addOnSuccessListener { documents ->
                    if (documents != null) {
                        for (doc in documents.documents) {
                            val data = doc.data

                            personalizeDocumentId = doc.id
                            val burger = data?.get("burger") as Number
                            val creative = data["creative"] as Number
                            val healthy = data["healthy"] as Number
                            val local = data["local"] as Number
                            val lowCal = data["lowCal"] as Number
                            val lowCarb = data["lowCarb"] as Number
                            val nation = data["nation"] as Number
                            val pasta = data["pasta"] as Number
                            val region = data["region"] as Number
                            val seafood = data["seafood"] as Number
                            val vegan = data["vegan"] as Number
                            val workout = data["workout"] as Number
                            val city = data["city"] as String
                            val state = data["state"] as String
                            val surpriseMe = data["surpriseMe"] as Number

                            this.surpriseMe = surpriseMe.toInt()
                            this.burger = burger.toInt()
                            this.creative = creative.toInt()
                            this.healthy = healthy.toInt()
                            this.local = local.toInt()
                            this.lowCal = lowCal.toInt()
                            this.lowCarb = lowCarb.toInt()
                            this.nation = nation.toInt()
                            this.pasta = pasta.toInt()
                            this.region = region.toInt()
                            this.seafood = seafood.toInt()
                            this.vegan = vegan.toInt()
                            this.workout = workout.toInt()
                            this.city.setText(city)
                            this.state.setText(state)

                            Log.d(TAG, "loadFilter: happening1234")
                            filter = Filter(local.toInt(), region.toInt(), nation.toInt(), city, state, burger.toInt(), creative.toInt(), lowCal.toInt(), lowCarb.toInt(), pasta.toInt(), healthy.toInt(), vegan.toInt(), seafood.toInt(), workout.toInt(), surpriseMe.toInt())
                            loadItems(filter, "")


                            if (local.toInt() == 1) {
                                localButton.isSelected = true
                                cityLayout.visibility = View.VISIBLE
                                this.city.visibility = View.VISIBLE

                                localButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                                localButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.secondary
                                    )
                                )
                            } else {
                                localButton.isSelected = false
                                localButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.main
                                    )
                                )
                                localButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                            }

                            if (region.toInt() == 1) {
                                regionButton.isSelected = true
                                nationButton.isSelected = false

                                cityLayout.visibility = View.VISIBLE
                                this.city.visibility = View.GONE

                                regionButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                                regionButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.secondary
                                    )
                                )

                            } else {
                                regionButton.isSelected = false
                                regionButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.main
                                    )
                                )
                                regionButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                            }

                            if (nation.toInt() == 1) {
                                cityLayout.visibility = View.GONE
                                nationButton.isSelected = true
                                nationButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                                nationButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.secondary
                                    )
                                )
                            } else {
                                nationButton.isSelected = false
                                nationButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.main
                                    )
                                )
                                nationButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                            }

                            if (surpriseMe.toInt() == 1) {
                                surpriseMeButton.isSelected = true
                                surpriseMeButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.secondary
                                    )
                                )
                                surpriseMeButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                            } else {
                                surpriseMeButton.isSelected = false
                                surpriseMeButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                                surpriseMeButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.main
                                    )
                                )
                            }

                            if (burger.toInt() == 1) {
                                burgerButton.isSelected = true
                                burgerButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.secondary
                                    )
                                )
                                burgerButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                            } else {
                                burgerButton.isSelected = false
                                burgerButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                                burgerButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.main
                                    )
                                )
                            }

                            if (creative.toInt() == 1) {
                                creativeButton.isSelected = true
                                creativeButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.secondary
                                    )
                                )
                                creativeButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                            } else {
                                creativeButton.isSelected = false
                                creativeButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                                creativeButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.main
                                    )
                                )
                            }

                            if (healthy.toInt() == 1) {
                                healthyButton.isSelected = true
                                healthyButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.secondary
                                    )
                                )
                                healthyButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                            } else {
                                healthyButton.isSelected = false
                                healthyButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                                healthyButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.main
                                    )
                                )
                            }

                            if (lowCarb.toInt() == 1) {
                                lowCarbButton.isSelected = true
                                lowCarbButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.secondary
                                    )
                                )
                                lowCarbButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                            } else {
                                lowCarbButton.isSelected = false
                                lowCarbButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                                lowCarbButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.main
                                    )
                                )
                            }

                            if (pasta.toInt() == 1) {
                                pastaButton.isSelected = true
                                pastaButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.secondary
                                    )
                                )
                                pastaButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                            } else {
                                pastaButton.isSelected = false
                                pastaButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                                pastaButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.main
                                    )
                                )
                            }

                            if (seafood.toInt() == 1) {
                                seafoodButton.isSelected = true
                                seafoodButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.secondary
                                    )
                                )
                                seafoodButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                            } else {
                                seafoodButton.isSelected = false
                                seafoodButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                                seafoodButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.main
                                    )
                                )
                            }

                            if (vegan.toInt() == 1) {
                                veganButton.isSelected = true
                                veganButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.secondary
                                    )
                                )
                                veganButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                            } else {
                                veganButton.isSelected = false
                                veganButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                                veganButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.main
                                    )
                                )
                            }

                            if (workout.toInt() == 1) {
                                workoutButton.isSelected = true
                                workoutButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.secondary
                                    )
                                )
                                workoutButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                            } else {
                                workoutButton.isSelected = false
                                workoutButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                                workoutButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.main
                                    )
                                )
                            }

                            if (lowCal.toInt() == 1) {
                                lowCalButton.isSelected = true
                                lowCalButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.secondary
                                    )
                                )
                                lowCalButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                            } else {
                                lowCalButton.isSelected = false
                                lowCalButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                                lowCalButton.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.main
                                    )
                                )
                            }
                        }

                    }
                }
    }


    private fun saveData() {
        val city = if (city.text.isEmpty()) { "" } else { city.text.toString() }
        val state = if (state.text.isEmpty()) { "" } else { state.text.toString() }

        val data: Map<String, Any> = hashMapOf("local" to local, "region" to region, "nation" to nation, "burger" to burger, "creative" to creative, "healthy" to healthy, "lowCal" to lowCal, "lowCarb" to lowCarb, "vegan" to vegan, "workout" to workout, "seafood" to seafood, "pasta" to pasta, "city" to city, "state" to state, "surpriseMe" to surpriseMe)
        db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("PersonalInfo").document(personalizeDocumentId).update(data)

        filter = Filter(local.toInt(), region.toInt(), nation.toInt(), city, state, burger.toInt(), creative.toInt(), lowCal.toInt(), lowCarb.toInt(), pasta.toInt(), healthy.toInt(), vegan.toInt(), seafood.toInt(), workout.toInt(), surpriseMe.toInt())

        Toast.makeText(requireContext(), "Filter saved.", Toast.LENGTH_LONG).show()

        if (toggle == "Cater Items" || toggle == "MealKit Items") {
            if (toggle == "Cater Items") {
                cateringItems.clear()
                cateringAdapter.submitList(cateringItems)
                cateringAdapter.notifyDataSetChanged()
                loadItems(filter, "")
            }  else if (toggle == "MealKit Items") {
                mealKitItems.clear()
                mealKitAdapter.submitList(mealKitItems)
                mealKitAdapter.notifyDataSetChanged()
            }
        } else {
            personalChefItems.clear()
            personalChefAdapter.submitList(personalChefItems)
            personalChefAdapter.notifyDataSetChanged()
            loadPersonalChefItems(filter, "")
        }


    }

    private fun stateFilter(state: String) : String {
        val stateAbbreviations : MutableList<String> = arrayListOf("Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorodo", "Connecticut", "Deleware", "District of Columbia", "Florida", "Georgia", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oegon", "Pennsylvania", "Puerto Rico", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Virgin Islands", "Washington", "West Virginia", "Wisconson", "Wyoming")
        val stateAbbr : MutableList<String> = arrayListOf("AL", "AK", "AZ", "AR", "AS", "CA", "CO", "CT", "DE", "DC", "FL", "GA", "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "PR", "RI", "SC", "SD", "TN", "TX", "TT", "UT", "VT", "VA", "VI", "WA", "WY", "WV", "WI", "WY")

        for (i in 0 until stateAbbreviations.size) {
            val a = stateAbbreviations[i].lowercase()
            if (a == state.lowercase()) {
                return "good"
            }
        }
        for (i in 0 until stateAbbr.size) {
            val a = stateAbbr[i].lowercase()
            if (a == state.lowercase()) {
                return "good"
            }
        }

        return "not good"
    }


    @SuppressLint("SetTextI18n")
    @Suppress("UNCHECKED_CAST")


    private fun loadItems(filter: Filter, go: String) {

        db.collection(toggle).get().addOnSuccessListener { documents ->
            if (documents != null) {
                binding.progressBar.isVisible = false
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

                    var location = ""
                    var preference = ""

                    if (filter.local == 1) {
                        if ((filter.city == city) || (filter.state == state)) {
                            location = "go"
                        }
                    } else if (filter.region == 1) {
                        if (filter.state == state) {
                            location = "go"
                        }
                    } else if (filter.nation == 1) {
                        location = "go"
                    }

                    Log.d(TAG, "loadItems: filter $filter")

                    if (filter.burger == 1 && burger == 1) {
                        preference = "go"
                    } else if (filter.creative == 1 && creative == 1) {
                        preference = "go"
                    } else if (filter.pasta == 1 && pasta == 1) {
                        preference = "go"
                    } else if (filter.healthy == 1 && healthy == 1) {
                        preference = "go"
                    } else if (filter.vegan == 1 && vegan == 1) {
                        preference = "go"
                    } else if (filter.lowCal == 1 && lowCal == 1) {
                        preference = "go"
                    } else if (filter.lowCarb == 1 && lowCarb == 1) {
                        preference = "go"
                    } else if (filter.seafood == 1 && seafood == 1) {
                        preference = "go"
                    } else if (filter.workout == 1 && workout == 1) {
                        preference = "go"
                    } else if (filter.surpriseMe == 1) {
                        preference = "go"
                    } else if (filter.burger == 0 && filter.creative == 0 && filter.lowCal == 0 && filter.lowCarb == 0 && filter.pasta == 0 && filter.healthy == 0 && filter.vegan == 0 && filter.workout == 0 && filter.seafood == 0) {
                        preference = "go"
                    }
                    Log.d(TAG, "loadItems: happening 123")

                    if (location == "go" && preference == "go" || go == "Yes") {


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



                        }
                    } else {
                        Toast.makeText(requireContext(), "We widened your search criteria just a bit.", Toast.LENGTH_LONG).show()
                        loadItems(filter, "Yes")
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadPersonalChefItems(filter: Filter, go: String) {
        db.collection("Executive Items").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val typeOfInfo = data?.get("typeOfService") as String

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

                    db.collection("Chef").document(chefImageId).collection("Executive Items").document(signatureDishId).get().addOnSuccessListener { document ->
                        if (document != null) {
                            val data = document.data

                            val burger = data?.get("burger") as Number
                            val creative = data["creative"] as Number
                            val pasta = data["pasta"] as Number
                            val healthy = data["healthy"] as Number
                            val lowCal = data["lowCal"] as Number
                            val lowCarb = data["lowCarb"] as Number
                            val seafood = data["seafood"] as Number
                            val workout = data["workout"] as Number
                            val vegan = data["vegan"] as Number

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

                    binding.progressBar.isVisible = false


                    var location = ""
                    var preference = ""

                    if (filter.local == 1) {
                        if ((filter.city == city) || (filter.state == state)) {
                            location = "go"
                        }
                    } else if (filter.region == 1) {
                        if (filter.state == state) {
                            location = "go"
                        }
                    } else if (filter.nation == 1) {
                        location = "go"
                    }

                    Log.d(TAG, "loadItems: filter $filter")

                    if (filter.burger == 1 && burger == 1) {
                        preference = "go"
                    } else if (filter.creative == 1 && creative == 1) {
                        preference = "go"
                    } else if (filter.pasta == 1 && pasta == 1) {
                        preference = "go"
                    } else if (filter.healthy == 1 && healthy == 1) {
                        preference = "go"
                    } else if (filter.vegan == 1 && vegan == 1) {
                        preference = "go"
                    } else if (filter.lowCal == 1 && lowCal == 1) {
                        preference = "go"
                    } else if (filter.lowCarb == 1 && lowCarb == 1) {
                        preference = "go"
                    } else if (filter.seafood == 1 && seafood == 1) {
                        preference = "go"
                    } else if (filter.workout == 1 && workout == 1) {
                        preference = "go"
                    } else if (filter.surpriseMe == 1) {
                        preference = "go"
                    } else if (filter.burger == 0 && filter.creative == 0 && filter.lowCal == 0 && filter.lowCarb == 0 && filter.pasta == 0 && filter.healthy == 0 && filter.vegan == 0 && filter.workout == 0 && filter.seafood == 0) {
                        preference = "go"
                    }

                    if ((location == "go" && preference == "go") || go == "Yes") {

                            val personalChefItem = PersonalChefInfo(chefName, chefEmail, chefImageId, Uri.EMPTY, city, state, zipCode, Uri.EMPTY, signatureDishId, "", "", "", "", briefIntroduction, lengthOfPersonalChef, specialty, whatHelpsYouExcel, mostPrizedAccomplishment, availability, hourlyOrPerSession, servicePrice, trialRun.toInt(), weeks.toInt(), months.toInt(), liked, itemOrders.toInt(), itemRating, expectations.toInt(), chefRating.toInt(), quality.toInt(), doc.id, openToMenuRequests)
                            if (personalChefItems.size == 0) {
                                personalChefItems.add(personalChefItem)
                                personalChefAdapter.submitList(personalChefItems)
                                personalChefAdapter.notifyItemInserted(0)
                            } else {
                                val index =
                                    personalChefItems.indexOfFirst { it.chefImageId == chefImageId }
                                if (index == -1) {
                                    personalChefItems.add(personalChefItem)
                                    personalChefAdapter.submitList(personalChefItems)
                                    personalChefAdapter.notifyItemInserted(personalChefItems.size - 1)

                        }}} else {
                        Toast.makeText(requireContext(), "We widened your search criteria just a bit.", Toast.LENGTH_LONG).show()
                            loadPersonalChefItems(filter, "Yes")
                        }}}}}
        }
    }


}