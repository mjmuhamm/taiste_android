package com.ruh.taiste.chef.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.R
import com.ruh.taiste.both.Notifications
import com.ruh.taiste.chef.models.MenuItems
import com.ruh.taiste.databinding.FragmentDashboardBinding
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


private const val TAG = "Dashboard"

class Dashboard : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val db = Firebase.firestore

    private var menuItems: MutableList<MenuItems> = ArrayList()
    private var menuArray = arrayListOf<String>()

    private var weeklyDates: MutableList<String> = arrayListOf()

    private lateinit var menuArrayAdapter: ArrayAdapter<String>

    @SuppressLint("SimpleDateFormat")
    private val sdfMonth = SimpleDateFormat("MM")

    @SuppressLint("SimpleDateFormat")
    private val sdfDay = SimpleDateFormat("dd")

    @SuppressLint("SimpleDateFormat")
    private val sdfYear = SimpleDateFormat("yyyy")

    @SuppressLint("SimpleDateFormat")
    private var sdfYearMonth = SimpleDateFormat("yyyy, MM")

    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private var toggle = "Weekly"

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
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)


//        setWeeklyDates()
        val typeOfService = resources.getStringArray(R.array.type_of_service)
        val typeOfServiceAll = resources.getStringArray(R.array.type_of_service_all)

        var typeOfServiceAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, typeOfService)
        binding.typeOfServiceText.setAdapter(typeOfServiceAdapter)

        Log.d(TAG, "onCreateView: ${binding.typeOfServiceText.text.toString()}")
        Log.d(TAG, "onCreateView: ${binding.menuItemText.text.toString()}")
        binding.weekly.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

                payout()
            Log.d(TAG, "onCreateView: ${binding.typeOfServiceText.text.toString()}")
            Log.d(TAG, "onCreateView: ${binding.menuItemText.text.toString()}")
            if (binding.menuItemText.text.isNotEmpty()) {
                var service = if (binding.typeOfServiceText.text.toString() == "Personal Chef Items") {
                    "Executive Items"
                } else {
                    binding.typeOfServiceText.text.toString()
                }
                val index = menuItems.indexOfFirst { it.itemTitle == binding.menuItemText.text.toString() }
                loadWeeklyData(service, menuItems[index].documentId)
            }
            toggle = "Weekly"
            binding.weekly.isSelected = true
            binding.monthly.isSelected = false
            binding.total.isSelected = false

            binding.weeklyBarChart.isVisible = true
            binding.monthlyBarChart.isVisible = false
            binding.totalPieChart.isVisible = false
            binding.menuItemLayout.visibility = View.VISIBLE

            binding.weekly.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.secondary
                )
            )
            binding.weekly.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.monthly.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.monthly.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.total.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.total.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))

            typeOfServiceAdapter =
                ArrayAdapter(requireContext(), R.layout.dropdown_item, typeOfService)
            binding.typeOfServiceText.setAdapter(typeOfServiceAdapter)
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.monthly.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

            Log.d(TAG, "onCreateView: ${binding.typeOfServiceText.text.toString()}")
            Log.d(TAG, "onCreateView: ${binding.menuItemText.text.toString()}")
            if (binding.menuItemText.text.isNotEmpty()) {
                var service = if (binding.typeOfServiceText.text.toString() == "Personal Chef Items") {
                    "Executive Items"
                } else {
                    binding.typeOfServiceText.text.toString()
                }
                val index = menuItems.indexOfFirst { it.itemTitle == binding.menuItemText.text.toString() }
                loadMonthlyData(service, menuItems[index].documentId)
            }
            toggle = "Monthly"
            binding.weekly.isSelected = false
            binding.monthly.isSelected = true
            binding.total.isSelected = false
            binding.menuItemLayout.visibility = View.VISIBLE

            binding.weeklyBarChart.isVisible = false
            binding.monthlyBarChart.isVisible = true
            binding.totalPieChart.isVisible = false

            binding.weekly.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.weekly.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.monthly.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.secondary
                )
            )
            binding.monthly.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.total.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.total.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))

            typeOfServiceAdapter =
                ArrayAdapter(requireContext(), R.layout.dropdown_item, typeOfService)
            binding.typeOfServiceText.setAdapter(typeOfServiceAdapter)
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.total.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

            Log.d(TAG, "onCreateView: ${binding.typeOfServiceText.text.toString()}")
            Log.d(TAG, "onCreateView: ${binding.menuItemText.text.toString()}")
            if (binding.menuItemText.text.isNotEmpty()) {
                var service = if (binding.typeOfServiceText.text.toString() == "Personal Chef Items") {
                    "Executive Items" } else { binding.typeOfServiceText.text.toString() }
                if (toggle == "Total") {
                    loadTotalData(service)
                } else {
                    loadTotalData(service)
                }

            }
            toggle = "Total"
            binding.weekly.isSelected = false
            binding.monthly.isSelected = false
            binding.total.isSelected = true

            binding.weeklyBarChart.isVisible = false
            binding.monthlyBarChart.isVisible = false
            binding.totalPieChart.isVisible = true
            binding.menuItemLayout.visibility = View.GONE

            binding.weekly.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.weekly.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.monthly.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.monthly.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.total.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.secondary
                )
            )
            binding.total.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            typeOfServiceAdapter =
                ArrayAdapter(requireContext(), R.layout.dropdown_item, typeOfServiceAll)
            binding.typeOfServiceText.setAdapter(typeOfServiceAdapter)
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.typeOfServiceText.setOnItemClickListener { _, _, _, _ ->
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

            if (binding.typeOfServiceText.text.isNotEmpty()) {
                val service =
                    if (binding.typeOfServiceText.text.toString() == "Personal Chef Items") {
                        "Executive Items"
                    } else {
                        binding.typeOfServiceText.text.toString()
                    }
                if (FirebaseAuth.getInstance().currentUser != null) {
                    if (binding.typeOfServiceText.text.toString() != "All Items") {
                        if (toggle == "Total") {
                            loadTotalData(service)
                        } else {
                            loadMenuItems(service)
                        }

                    } else {
                        loadAllTotalData()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.menuItemText.setOnItemClickListener { _, _, _, _ ->
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

            if (binding.menuItemText.text.isNotEmpty()) {
                val service =
                    if (binding.typeOfServiceText.text.toString() == "Personal Chef Items") {
                        "Executive Items"
                    } else {
                        binding.typeOfServiceText.text.toString()
                    }
                val index =
                    menuItems.indexOfFirst { it.itemTitle == binding.menuItemText.text.toString() }
                if (FirebaseAuth.getInstance().currentUser != null) {
                    if (toggle == "Weekly") {
                        loadWeeklyData(service, menuItems[index].documentId)
                    } else if (toggle == "Monthly") {
                        loadMonthlyData(service, menuItems[index].documentId)
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }

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
            binding.progressBar.isVisible = false
            loadNotifications()
        } else {
            Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }

        return binding.root
    }

    private fun loadNotifications() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).addSnapshotListener { document, _ ->
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




    private fun loadMenuItems(service: String) {
        menuItems.clear()
        menuArray.clear()

        Log.d(TAG, "loadMenuItems: $service")
        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection(service).get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    for (doc in documents.documents) {
                        val data = doc.data

                        val typeOfService = data?.get("typeOfService")
                        if (typeOfService != "info") {
                            val itemTitle = data?.get("itemTitle") as String
                            val newItem = MenuItems(doc.id, itemTitle)

                            if (menuItems.isEmpty()) {
                                menuItems.add(newItem)
                                menuArray.add(itemTitle)

                                menuArrayAdapter =
                                    ArrayAdapter(
                                        requireContext(),
                                        R.layout.dropdown_item,
                                        menuArray
                                    )
                                binding.menuItemText.setAdapter(menuArrayAdapter)
                            } else {
                                val index = menuItems.indexOfFirst { it.documentId == doc.id }
                                if (index == -1) {
                                    menuItems.add(newItem)
                                    menuArray.add(itemTitle)

                                    menuArrayAdapter = ArrayAdapter(
                                        requireContext(),
                                        R.layout.dropdown_item,
                                        menuArray
                                    )
                                    binding.menuItemText.setAdapter(menuArrayAdapter)
                                }
                            }
                        }
                    }
                }
            }
    }


//    private fun setWeeklyDates() {
//        val calendar = Calendar.getInstance()
//        var weekDay = sdfDay.format(Date())
//        var weekMonth = sdfMonth.format(Date())
//        var weekYear = sdfYear.format(Date())
//
//        val dayOfWeek = Calendar.getInstance()[Calendar.DAY_OF_WEEK] - 1
//        var lengthOfMonth = 0
//
//        if (weekDay.toInt() - dayOfWeek <= 0) {
//            if (weekMonth.toInt() == 0) {
//                weekYear = "${weekYear.toInt() - 1}"
//                weekMonth = "12"
//                calendar.set(weekYear.toInt(), weekMonth.toInt(), 1)
//                lengthOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
//                weekDay = "${lengthOfMonth - dayOfWeek + 1}"
//                if (weekDay.toInt() < 10) {
//                    weekDay = "0$weekDay"
//                }
//
//
//            } else {
//                weekMonth = "${weekMonth.toInt() - 1}"
//                if (weekMonth.toInt() < 10) {
//                    weekMonth = "0$weekMonth"
//                }
//                calendar.set(weekYear.toInt(), weekMonth.toInt(), 1)
//                weekDay = "${lengthOfMonth - dayOfWeek + 1}"
//                if (weekDay.toInt() < 10) {
//                    weekDay = "0$weekDay"
//                }
//
//            }
//        } else {
//            weekDay = "${weekDay.toInt() - dayOfWeek}"
//            if (weekDay.toInt() < 10) {
//                weekDay = "0$weekDay"
//            }
//        }
//
//
//        for (i in 0 until 7) {
//
//            weeklyDates.add("$weekMonth-$weekDay-$weekYear")
//
//            calendar.set(weekYear.toInt(), weekMonth.toInt(), 1)
//            if (weekDay.toInt() + 1 > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
//                if (weekMonth == "12") {
//                    weekMonth = "01"
//                    weekYear = "${weekYear.toInt() + 1}"
//                    weekDay = "01"
//                } else {
//                    weekMonth = "${weekMonth.toInt() + 1}"
//                    if (weekMonth.toInt() < 10) {
//                        weekMonth = "0$weekMonth"
//                    }
//                    weekDay = "01"
//                }
//            } else {
//                weekDay = "${weekDay.toInt() + 1}"
//                if (weekDay.toInt() < 10) {
//                    weekDay = "0$weekDay"
//                }
//            }
//            Log.d(TAG, "setWeeklyDates: $weeklyDates")
//
//        }
//
//
//    }

    private fun payout() {
        @SuppressLint("SimpleDateFormat")
        var sdfT = SimpleDateFormat("MM-dd-yyyy hh:mm a")

        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").addSnapshotListener { documents, error ->
            if (error == null) {
                if (documents != null) {
                    for (doc in documents.documents) {

                        val data = doc.data

                        val eventDates = data?.get("eventDates") as ArrayList<String>
                        val eventTimes = data["eventTimes"] as ArrayList<String>
                        val chefImageId = data["chefImageId"] as String
                        val userImageId = data["userImageId"] as String
                        val totalCostOfEvent = data["totalCostOfEvent"] as Number
                        val menuItemId = data["menuItemId"] as String
                        var payoutDates = data["payoutDates"] as ArrayList<String>
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
    private fun displayAlert(message: String) {
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


    private fun loadWeeklyData(service: String, menuItemId: String) {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0F, 0F))
        entries.add(BarEntry(1F, 0F))
        entries.add(BarEntry(2F, 0F))
        entries.add(BarEntry(3F, 0F))


        val labels = ArrayList<String>()
        labels.add("Week 1")
        labels.add("Week 2")
        labels.add("Week 3")
        labels.add("Week 4")

        val colors = ArrayList<Int>()
        for (i in 0 until ColorTemplate.PASTEL_COLORS.size) {
            colors.add(ColorTemplate.PASTEL_COLORS[i])
        }

        val yearMonth = sdfYearMonth.format(Date())

        var weekOfMonth = Calendar.getInstance()[Calendar.WEEK_OF_MONTH]
        if (weekOfMonth > 5) { weekOfMonth = 4 }


        for (i in 1 until 5) {
            val week = "Week $i"
            Log.d(TAG, "loadWeeklyData: Week $i")
            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("Dashboard").document(service)
                .collection(menuItemId).document("Month").collection(yearMonth).document("Week")
                .collection(week).get()
                .addOnSuccessListener { documents ->
                    if (documents != null) {
                        for (doc in documents.documents) {
                            val data = doc.data
                            val total = data?.get("totalPay") as Number
                            val num = i-1
                            entries[num] = BarEntry(num.toFloat(), total.toFloat())
                        }

                        val barDataSet = BarDataSet(entries, service)
                        barDataSet.colors = colors
                        val barData = BarData(barDataSet)

                        binding.weeklyBarChart.xAxis.valueFormatter =
                            IndexAxisValueFormatter(labels)
                        binding.weeklyBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

                        binding.weeklyBarChart.setDrawGridBackground(false)
                        binding.weeklyBarChart.xAxis.setDrawAxisLine(false)
                        binding.weeklyBarChart.xAxis.setDrawGridLines(false)
                        binding.weeklyBarChart.xAxis.labelCount = 4
                        binding.weeklyBarChart.axisLeft.isEnabled = false
                        binding.weeklyBarChart.axisRight.isEnabled = false
                        binding.weeklyBarChart.description.isEnabled = false


//                        val groupSpace = 0.1F
//                        val barSpace = 0.05F
//                        val barWidth = 0.25F

                        val groupSpace = 0.2f
                        val barSpace = 0f
                        val barWidth = 0.16f

                        binding.weeklyBarChart.setScaleEnabled(false)
//                        binding.weeklyBarChart.groupBars(1f, groupSpace, barSpace)
//                        binding.weeklyBarChart.groupBars()
//                        binding.weeklyBarChart.xAxis.setCenterAxisLabels(true)
//                        binding.weeklyBarChart.xAxis.axisMaximum = labels.count() - 1.1f;
                        binding.weeklyBarChart.animateXY(1500, 1500)
                        binding.weeklyBarChart.data = barData
                        binding.weeklyBarChart.invalidate()

                    }
                }

        }


    }

    private fun loadMonthlyData(service: String, menuItemId: String) {
        val entries = ArrayList<BarEntry>()
        val year = sdfYear.format(Date())
        var month : String

        val labels = ArrayList<String>()
        if (Calendar.getInstance()[Calendar.MONTH] < 6) {
            labels.add("January")
            labels.add("February")
            labels.add("March")
            labels.add("April")
            labels.add("May")
            labels.add("June")
            month = "1"
        } else {
            labels.add("July")
            labels.add("August")
            labels.add("September")
            labels.add("October")
            labels.add("November")
            labels.add("December")
            month = "7"
        }

        val colors = ArrayList<Int>()
        for (i in 0 until ColorTemplate.PASTEL_COLORS.size) {
            colors.add(ColorTemplate.PASTEL_COLORS[i])
        }




        for (i in 0 until 6) {
            if (month.toInt() < 10) { month = "0$month" }
            val yearMonth = "$year, $month"
            month = "${month.toInt() + 1}"
            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Dashboard").document(service)
                .collection(menuItemId).document("Month").collection(yearMonth).document("Total")
                .get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    val total = data?.get("totalPay") as Number
                    entries.add(BarEntry(i.toFloat(), total.toFloat()))
                } else {
                    entries.add(BarEntry(i.toFloat(), 0F)) }

                val barDataSet = BarDataSet(entries, service)
                barDataSet.colors = colors
                val barData = BarData(barDataSet)

                binding.monthlyBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                binding.monthlyBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                binding.monthlyBarChart.setDrawGridBackground(false)
                binding.monthlyBarChart.xAxis.setDrawAxisLine(false)
                binding.monthlyBarChart.xAxis.setDrawGridLines(false)
                binding.monthlyBarChart.axisLeft.isEnabled = false
                binding.monthlyBarChart.axisRight.isEnabled = false
                binding.monthlyBarChart.description.isEnabled = false


                binding.monthlyBarChart.animateXY(1500, 1500)
                binding.monthlyBarChart.data = barData
                binding.monthlyBarChart.invalidate()

            }
        }
    }


    private fun loadTotalData(service: String) {

            val entries = ArrayList<PieEntry>()

            val colors = ArrayList<Int>()
            for (i in 0 until ColorTemplate.PASTEL_COLORS.size) {
                colors.add(ColorTemplate.PASTEL_COLORS[i])
            }


            binding.totalPieChart.isDrawHoleEnabled = true
            binding.totalPieChart.setUsePercentValues(false)
            binding.totalPieChart.setEntryLabelTextSize(0f)
            binding.totalPieChart.setEntryLabelColor(ContextCompat.getColor(requireContext(), com.aminography.primedatepicker.R.color.white))
            if (service == "Executive Items") {
                binding.totalPieChart.centerText = "Personal Chef Items"
            } else { binding.totalPieChart.centerText = service }

            binding.totalPieChart.setCenterTextColor(ContextCompat.getColor(requireContext() ,R.color.main))
            binding.totalPieChart.setCenterTextSize(17f)
            binding.totalPieChart.description.isEnabled = false

            val l = binding.totalPieChart.legend
            l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            l.orientation = Legend.LegendOrientation.VERTICAL
            l.setDrawInside(false)
            l.isEnabled = true

        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection(service).get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    if (data?.get("typeOfService") as String != "info") {

                        val itemTitle = data["itemTitle"] as String
                        val documentId = doc.id

                        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid)
                            .collection("Dashboard").document(service).collection(documentId)
                            .document("Total").get().addOnSuccessListener { document ->
                            if (document.exists()) {
                                val data1 = document.data

                                val total = data1?.get("totalPay") as Number
                                entries.add(PieEntry(total.toFloat(), itemTitle))
                            } else {
                                entries.add(PieEntry(0F, itemTitle))
                            }

                            val dataSet = PieDataSet(entries, "")
                            dataSet.colors = colors
                            dataSet.valueFormatter = PercentFormatter(binding.totalPieChart)
                            dataSet.valueTextSize = 12f
                            dataSet.valueTextColor = ContextCompat.getColor(
                                requireContext(),
                                com.aminography.primedatepicker.R.color.white
                            )
                            val data2 = PieData(dataSet)
                            binding.totalPieChart.data = data2
                            binding.totalPieChart.invalidate()

                            binding.totalPieChart.animateXY(1250, 1250)
                        }
                    }
                }
            }
        }

    }

    private fun loadAllTotalData() {

        val entries = ArrayList<PieEntry>()

        val colors = ArrayList<Int>()
        for (i in 0 until ColorTemplate.PASTEL_COLORS.size) {
            colors.add(ColorTemplate.PASTEL_COLORS[i])
        }

        binding.totalPieChart.isDrawHoleEnabled = true
        binding.totalPieChart.setUsePercentValues(false)
        binding.totalPieChart.setEntryLabelTextSize(12f)
        binding.totalPieChart.setEntryLabelColor(ContextCompat.getColor(requireContext(), com.aminography.primedatepicker.R.color.white))
        binding.totalPieChart.centerText = "All Items"
        binding.totalPieChart.setCenterTextColor(ContextCompat.getColor(requireContext() ,R.color.main))
        binding.totalPieChart.setCenterTextSize(17f)
        binding.totalPieChart.description.isEnabled = false

        val l = binding.totalPieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.isEnabled = true

        val typeOfService = arrayListOf("Cater Items", "Personal Chef Items", "MealKit Items")



        for (i in 0 until typeOfService.size) {
            var b = typeOfService[i]
            if (b == "Personal Chef Items") { b = "Executive Items" }
            Log.d(TAG, "loadAllTotalData: $b")
            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Dashboard").document(b).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val data = document.data
                        val total = data?.get("totalPay") as Number
                             entries.add(PieEntry(total.toFloat(), typeOfService[i]))
                        } else {
                        entries.add(PieEntry(0F, typeOfService[i]))
                    }

                    val dataSet = PieDataSet(entries, "")
                    dataSet.colors = colors
                    dataSet.setDrawValues(true)
                    dataSet.valueFormatter = PercentFormatter(binding.totalPieChart)
                    dataSet.valueTextSize = 12f
                    dataSet.valueTextColor = ContextCompat.getColor(requireContext(), com.aminography.primedatepicker.R.color.white)
                    val data = PieData(dataSet)
                    binding.totalPieChart.data = data
                    binding.totalPieChart.invalidate()

                    binding.totalPieChart.animateXY(1250,1250)


                }
        }
    }
}