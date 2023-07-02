package com.ruh.taiste.user

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.aminography.primecalendar.civil.CivilCalendar
import com.aminography.primecalendar.common.operators.dayOfWeek
import com.aminography.primedatepicker.picker.PrimeDatePicker
import com.aminography.primedatepicker.picker.callback.MultipleDaysPickCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.R
import com.ruh.taiste.databinding.ActivityPersonalChefOrderDetailBinding
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import com.ruh.taiste.user.models.CheckoutItems
import com.ruh.taiste.user.models.FeedMenuItems
import com.ruh.taiste.user.models.PersonalChefInfo
import okio.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "PersonalChefOrderDetail"
class PersonalChefOrderDetail : AppCompatActivity() {
    private lateinit var binding: ActivityPersonalChefOrderDetailBinding

    private val db = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser!!.email!!


    private var eventDates : MutableList<String> = mutableListOf()
    private var eventTimes : MutableList<String> = mutableListOf()
    private var eventDatesForUser : MutableList<String> = mutableListOf()

    private lateinit var datePicker : PrimeDatePicker
    private lateinit var item : PersonalChefInfo
    private lateinit var checkoutItem : CheckoutItems

    private var chefLocation : Location = Location("chefLocation")
    private var userLocation : Location = Location("userLocation")

    private var travelFeeOption = "No"
    private var distance = 0.0

    private var timePeriod = ""

    private lateinit var personalChefInfo : PersonalChefInfo


    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalChefOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        val item1 = intent.getParcelableExtra<FeedMenuItems>("item")
        @Suppress("DEPRECATION")
        val checkoutItem1 = intent.getParcelableExtra<CheckoutItems>("checkout_item")

        val timePeriodOfEvent = resources.getStringArray(R.array.timePeriodOfEvent)
        val timePeriodOfEventAdapter = ArrayAdapter(this, R.layout.dropdown_item, timePeriodOfEvent)

        val personalChefQuantityOfEvent = resources.getStringArray(R.array.personal_chef_quantityOfEvent)
        val personalChefQuantityOfEventAdapter = ArrayAdapter(this, R.layout.dropdown_item, personalChefQuantityOfEvent)

        @Suppress("DEPRECATION")
        personalChefInfo = intent.getParcelableExtra("personal_chef_info")!!
        @Suppress("DEPRECATION")
        item = intent.getParcelableExtra("personal_chef_info")!!

        binding.timePeriodOfService.setAdapter(timePeriodOfEventAdapter)


        binding.itemTitle.text = "Executive Chef"
        binding.itemDescription.text = personalChefInfo.briefIntroduction


        val streetAddress = "${personalChefInfo.city}, ${personalChefInfo.state}"
        Log.d(TAG, "onCreate: item view ${streetAddress}")
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(this)
            } else {
                isOnline1(this)
            }
        ) {
            validateAddress(streetAddress, "")
        } else {
            Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }


        //Date Picker
        val callback = MultipleDaysPickCallback { days ->
                if (binding.timePeriodOfService.text.toString() == "Trial Run") {

                    for (i in 0 until days.size) {
                        val month = days[i].month
                        val day = days[i].dayOfMonth
                        val year = days[i].year

                        val userDate = "${days[i].monthDayString}, $year"

                        val newMonth = if (month < 10) { "0$month" } else { "$month" }
                        val newDay = if (day < 10) { "0$day" } else { "$day" }

                        if (!eventDates.contains("$newMonth-$newDay-$year")) {
                            eventDates.add("$newMonth-$newDay-$year")
                            eventDatesForUser.add(userDate)
                            val price = "%.2f".format(personalChefInfo.servicePrice.toDouble() * eventDates.size)
                            binding.priceOfEvent.text = "$$price"
                        }

                    }
                } else if (binding.timePeriodOfService.text.toString() == "Weeks") {
                    for (i in 0 until days.size) {
                        val cal = Calendar.getInstance()
                        cal.time = Date()

                        val datePicked = getWeeks(days[i].month, days[i].date, days[i].year, days[i].dayOfWeek, days[i].dayOfMonth)
                        val today = getWeeks(cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.YEAR), cal.get(Calendar.DAY_OF_WEEK), cal.get(Calendar.DAY_OF_MONTH))
                        val daysInWeeks = getDaysInWeeks(cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.YEAR), cal.get(Calendar.DAY_OF_WEEK), cal.get(Calendar.DAY_OF_MONTH))
                        if (today != datePicked) {
                            val date = "${getWeeks(days[i].month, days[i].date, days[i].year, days[i].dayOfWeek, days[i].dayOfMonth)[0]} through ${getWeeks(days[i].month, days[i].date, days[i].year, days[i].dayOfWeek, days[i].dayOfMonth)[1]}"

                            if (!eventDatesForUser.contains(date)) {
                                for (i in 0 until daysInWeeks.size) {
                                    eventDates.add(daysInWeeks[i])
                                }

                                eventDatesForUser.add(date)
                                val price = "%.2f".format(personalChefInfo.servicePrice.toDouble() * eventDates.size * binding.quantityOfEvent.text.toString().toInt())
                                binding.priceOfEvent.text = "$$price"
                            }

                        } else {
                            Toast.makeText(this, "Please select weeks beyond your current week.", Toast.LENGTH_LONG).show()
                        }
                    }
                } else if (binding.timePeriodOfService.text.toString() == "Months") {
                    if (eventDates.size == 12) {
                        Toast.makeText(this, "To service more months, please order this item again.", Toast.LENGTH_LONG).show()
                        binding.datePickerText.setTextColor(ContextCompat.getColor(this, R.color.main))
                        binding.clearButton.isVisible = true
                        binding.timePickerLayout.visibility = View.GONE
                        binding.quantityOfEvent.isVisible = true
                        binding.dateOfEventLabel.isVisible = true
                        binding.datePickedLayout.isVisible = true
                        binding.locationLabel.isVisible = true
                        binding.locationPickedLayout.isVisible = true
                        binding.notesLabel.isVisible = true
                        binding.notesToChef.isVisible = true
                        binding.addItemLayout.isVisible = true
                        binding.datePickerButton.isEnabled = false
                    } else {
                        for (i in 0 until days.size) {
                            val cal = Calendar.getInstance()
                            cal.time = Date()
                            Log.d(TAG, "onCreate: days in month ${days[i].month}")

                            val daysInMonth = days[i].monthLength
                            var month = "${days[i].month}"
                            if (month.toInt() < 10) {
                                month = "0${days[i].month}"
                            }
                            val date = "${days[i].month}, ${days[i].year}"
                            val today = "${cal.get(Calendar.MONTH)}, ${cal.get(Calendar.YEAR)}"

                            if (date == today) {
                                Toast.makeText(this, "Please select months beyond your current month.", Toast.LENGTH_LONG).show()
                            } else {
                                val dateForUser = "${days[i].monthName}, ${days[i].year}"

                                for (b in 0 until daysInMonth) {
                                    var day = "${days[i].dayOfMonth}"
                                    if (day.toInt() < 10) {
                                        day = "0${days[i].dayOfMonth}"
                                    }
                                    if (!eventDatesForUser.contains(dateForUser)) {
                                        eventDates.add("${days[i].year}-$month-$day")
                                        eventDatesForUser.add(dateForUser)

                                    }
                                }
                                val price = "%.2f".format(personalChefInfo.servicePrice.toDouble() * eventDates.size * binding.quantityOfEvent.text.toString().toInt())
                                binding.priceOfEvent.text = "$$price"
                            }
                        }

                }
            }

            if (eventDatesForUser.size > 0) {
                binding.timePickerLayout.visibility = View.VISIBLE

                binding.dateForTimePicker.text = eventDatesForUser[0]

                binding.quantityOfEvent.isVisible = false
                binding.dateOfEventLabel.isVisible = false
                binding.datePickedLayout.isVisible = false
                binding.locationLabel.isVisible = false
                binding.locationPickedLayout.isVisible = false
                binding.notesLabel.isVisible = false
                binding.notesToChef.isVisible = false
                binding.addItemLayout.isVisible = false
                binding.clearButton.isVisible = false
            }

        }
        val today = CivilCalendar()
        datePicker = PrimeDatePicker.bottomSheetWith(today).pickMultipleDays(callback).minPossibleDate(today).build()

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        binding.datePickerButton.setOnClickListener {
            val today = CivilCalendar()
            datePicker = PrimeDatePicker.bottomSheetWith(today).pickMultipleDays(callback).minPossibleDate(today).build()
            if (binding.quantityOfEvent.text.isEmpty()) {
                Toast.makeText(this, "Please enter a quantity for your event first.", Toast.LENGTH_LONG).show()
            } else if (binding.timePeriodOfService.text.isEmpty()) {
                Toast.makeText(this, "Please select a time period for this service before continuing.", Toast.LENGTH_LONG).show()
            } else {
                datePicker.show(supportFragmentManager, "Event Dates")
            }
        }

        binding.timePickerOkButton.setOnClickListener {
            var hour = binding.timePicker.hour
            val minute = binding.timePicker.minute

            val newHour : String

            val amOrPm : String

            if (hour > 12) {
                hour -= 12
                newHour = if (hour < 10) { "0$hour" } else { "$hour" }
                amOrPm = "PM"
            } else {
                newHour = if (hour < 10) { "0$hour" } else { "$hour" }
                amOrPm = "AM"
            }

            val newMinute = if (minute < 10) {
                "0$minute"
            } else {
                "$minute"
            }

            eventTimes.add("$newHour:$newMinute $amOrPm")

            if (eventTimes.size != eventDatesForUser.size) {
                binding.timePicker.hour = 12
                binding.timePicker.minute = 0
                binding.timePicker.clearFocus()
                binding.timePicker.refreshDrawableState()
                binding.dateForTimePicker.text = eventDatesForUser[eventTimes.size]
            } else {
                binding.datePickerText.setTextColor(ContextCompat.getColor(this, R.color.main))
                binding.clearButton.isVisible = true
                binding.timePickerLayout.visibility = View.GONE
                binding.quantityOfEvent.isVisible = true
                binding.dateOfEventLabel.isVisible = true
                binding.datePickedLayout.isVisible = true
                binding.locationLabel.isVisible = true
                binding.locationPickedLayout.isVisible = true
                binding.notesLabel.isVisible = true
                binding.notesToChef.isVisible = true
                binding.addItemLayout.isVisible = true
                binding.datePickerButton.isEnabled = false

                for (i in 0 until eventDatesForUser.size) {
                    if (i == 0) {
                        binding.datePickerText.text = "Dates: ${eventDatesForUser[i]} ${eventTimes[i]}"
                    } else if (i == 1) {
                        binding.datePickerText.text = "${binding.datePickerText.text}, ${eventDatesForUser[i]} ${eventTimes[i]}"
                        binding.seeAllDatesText.text = binding.datePickerText.text
                        if (eventDates.size > 2) {
                            binding.datePickerText.text = "${binding.datePickerText.text}..."
                            binding.seeAllDatesButton.isVisible = true
                        }
                    } else {
                        binding.seeAllDatesText.text = "${binding.seeAllDatesText.text}, ${eventDatesForUser[i]} ${eventTimes[i]}"
                    }
                }

            }

        }


        binding.seeAllDatesButton.setOnClickListener {
            binding.seeAllDatesLayout.visibility = View.VISIBLE
            binding.datePickedLayout.isVisible = false
            binding.locationLabel.isVisible = false
            binding.locationPickedLayout.isVisible = false
            binding.notesToChef.isVisible = false
            binding.notesLabel.isVisible = false
            binding.addItemLayout.isVisible = false
            //
            binding.allergiesLabel.isVisible = false
            binding.allergies.isVisible = false
            binding.additionalMenuRequirementsLabel.isVisible = false
            binding.additionalMenuRequirements.isVisible = false
        }

        binding.seeAllDatesOkButton.setOnClickListener {
            binding.seeAllDatesLayout.visibility = View.GONE
            binding.datePickedLayout.isVisible = true
            binding.locationLabel.isVisible = true
            binding.locationPickedLayout.isVisible = true
            binding.notesToChef.isVisible = true
            binding.notesLabel.isVisible = true
            binding.addItemLayout.isVisible = true
            //
            binding.allergiesLabel.isVisible = true
            binding.allergies.isVisible = true
            binding.additionalMenuRequirementsLabel.isVisible = true
            binding.additionalMenuRequirements.isVisible = true

        }

        binding.quantityOfEvent.setOnClickListener {
            binding.priceOfEvent.text = ""
            eventDates.clear()
            eventTimes.clear()
            eventDatesForUser.clear()
            binding.datePickerText.text = ""
            binding.seeAllDatesText.text = ""
            binding.seeAllDatesButton.isVisible = false
            binding.datePickerButton.isEnabled = true
            datePicker = PrimeDatePicker.bottomSheetWith(today).pickMultipleDays(callback).minPossibleDate(today).build()
        }

        binding.clearButton.setOnClickListener {
            binding.priceOfEvent.text = ""
            eventDates.clear()
            eventTimes.clear()
            eventDatesForUser.clear()
            binding.datePickerText.text = ""
            binding.seeAllDatesText.text = ""
            binding.seeAllDatesButton.isVisible = false
            binding.datePickerButton.isEnabled = true
            datePicker = PrimeDatePicker.bottomSheetWith(today).pickMultipleDays(callback).minPossibleDate(today).build()
        }

        binding.locationButton.setOnClickListener {
            binding.enterLocationLayout.visibility = View.VISIBLE
            binding.dateOfEventLabel.isVisible = false
            binding.datePickedLayout.isVisible = false
            binding.notesLabel.isVisible = false
            binding.locationLabel.isVisible = false
            binding.locationPickedLayout.isVisible = false
            binding.notesToChef.isVisible = false
            binding.addItemLayout.isVisible = false
            binding.datePickedLayout.isVisible = false
            binding.clearButton.isVisible = false
            binding.seeAllDatesButton.isVisible = false
            binding.quantityOfEvent.isVisible = false
            binding.timePeriodOfServiceLayout.isVisible = false

            //
            binding.allergiesLabel.isVisible = false
            binding.allergies.isVisible = false
            binding.additionalMenuRequirementsLabel.isVisible = false
            binding.additionalMenuRequirements.isVisible = false
        }

        binding.enterLocationOkButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            when {
                binding.streetAddress.text.isEmpty() -> {
                    Toast.makeText(this, "Please enter your event street address.", Toast.LENGTH_LONG)
                        .show()
                }
                binding.city.text.isEmpty() -> {
                    Toast.makeText(this, "Please enter your event city.", Toast.LENGTH_LONG).show()
                }
                binding.state.text.isEmpty() -> {
                    Toast.makeText(this, "Please enter your event state.", Toast.LENGTH_LONG).show()
                }
                binding.zipCide.text.isEmpty() -> {
                    Toast.makeText(this, "Please enter your event zip code.", Toast.LENGTH_LONG).show()
                }
                else -> {

                    val streetAddress = if (binding.streetAddress2.text.isNotEmpty()) {
                        "${binding.streetAddress.text} ${binding.streetAddress2.text}"
                    } else { "${binding.streetAddress.text}" }

                    val address = "$streetAddress ${binding.city.text}, ${binding.state.text}, ${binding.zipCide.text}"

                    validateAddress(address, "Yes")
                }
            }
            } else {
                binding.locationText.setTextColor(ContextCompat.getColor(this, R.color.main))
                binding.enterLocationLayout.visibility = View.GONE
                binding.dateOfEventLabel.isVisible = true
                binding.datePickedLayout.isVisible = true
                binding.locationLabel.isVisible = true
                binding.locationPickedLayout.isVisible = true
                binding.notesLabel.isVisible = true
                binding.notesToChef.isVisible = true
                binding.addItemLayout.isVisible = true
                binding.datePickedLayout.isVisible = true
                if (eventDates.size != 0) {
                    binding.clearButton.isVisible = true
                    if (eventDates.size > 2) { binding.seeAllDatesButton.isVisible = true }}
                binding.quantityOfEvent.isVisible = true
                binding.allergiesLabel.isVisible = true
                binding.allergies.isVisible = true
                binding.additionalMenuRequirementsLabel.isVisible = true
                binding.additionalMenuRequirements.isVisible = true
                binding.timePeriodOfServiceLayout.isVisible = true
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
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
                if (binding.timePeriodOfService.text.isEmpty()) {
                    Toast.makeText(this, "Please select a time period for your service.", Toast.LENGTH_LONG).show()
                } else if (binding.quantityOfEvent.text.isEmpty()) {
                    Toast.makeText(this, "Please select a quantity for your service.", Toast.LENGTH_LONG).show()
                } else if (eventDates.size == 0) {
                    Toast.makeText(this, "Please select the days of your service.", Toast.LENGTH_LONG).show()
                } else if (binding.locationText.text.toString() == "Enter the location of your event here." || binding.locationText.text.isEmpty()) {
                    Toast.makeText(this, "Please enter the location of your service.", Toast.LENGTH_LONG).show()
                } else {
                    addToCart()
                }
            } else {
                Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

    }
    private var no = ""
    private fun addToCart() {
        val item = this.item
        val intent = Intent(this, MainActivity::class.java)
        val documentId = UUID.randomUUID().toString()
        val priceToChef = binding.priceOfEvent.text.takeLast(binding.priceOfEvent.text.length - 1).toString().toDouble() * 0.93
        val totalCostOfEvent = binding.priceOfEvent.text.takeLast(binding.priceOfEvent.text.length - 1).toString().toDouble()
        intent.putExtra("where_to", "home")
        val data : Map<String, Any> = hashMapOf("chefEmail" to item.chefEmail, "chefImageId" to item.chefImageId, "chefUsername" to item.chefName, "city" to binding.city.text.toString(), "datesOfEvent" to eventDates, "distance" to "$distance", "itemDescription" to item.briefIntroduction, "itemTitle" to "Executive Chef", "latitudeOfEvent" to "$userLocation.latitude", "location" to binding.locationText.text, "longitudeOfEvent" to "$userLocation.longitude", "menuItemId" to item.signatureDishId, "notesToChef" to binding.notesToChef.text.toString(), "priceToChef" to priceToChef, "quantityOfEvent" to binding.quantityOfEvent.text.toString(), "state" to binding.state.text.toString(), "timesForDatesOfEvent" to eventTimes, "totalCostOfEvent" to totalCostOfEvent, "travelExpenseOption" to travelFeeOption, "typeOfEvent" to binding.timePeriodOfService.text.toString(), "typeOfService"  to "Executive Items", "unitPrice" to item.servicePrice, "user" to user, "imageCount" to 1, "liked" to item.liked, "itemOrders" to item.itemOrders, "itemRating" to item.itemRating, "itemCalories" to "0", "allergies" to binding.allergies.text.toString(), "additionalMenuItems" to binding.additionalMenuRequirements.text.toString(), "signatureDishId" to item.signatureDishId, "eventDatesForUser" to eventDatesForUser)
        db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Cart").document(documentId).set(data)
        db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener { document ->
            if (document != null) {
                val data1 = document.data

                val notificationToken = data1?.get("notificationToken") as String
                val data2: Map<String, Any> = hashMapOf("userNotificationToken" to notificationToken)
                db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Cart").document(documentId).update(data2)
            }
        }
        db.collection("Chef").document(item.chefImageId).get().addOnSuccessListener { document ->
            if (document != null) {
                val data1 = document.data

                val notificationToken = data1?.get("notificationToken") as String
                val data2: Map<String, Any> = hashMapOf("chefNotificationToken" to notificationToken)
                db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Cart").document(documentId).update(data2)
            }
        }
        Toast.makeText(this, "Item Saved", Toast.LENGTH_LONG).show()
        startActivity(intent)
    }

    private fun validateAddress(strAddress: String, second: String): GeoPoint? {
        val coder = Geocoder(this)
        val address: List<Address>?
        val p1: GeoPoint? = null
        try {
            @Suppress("DEPRECATION")
            address = coder.getFromLocationName(strAddress, 5)
            if (address == null) {
                if (no != "Yes") {
                    Toast.makeText(this, "We are unable to validate this address. Please check your information and try again.", Toast.LENGTH_LONG).show()
                    no = "Yes"
                } else {
                    Toast.makeText(this, "We are unable to validate this address. Please make sure to keep in touch with your chef.", Toast.LENGTH_LONG).show()
                    travelFeeOption = "Yes"
                    if (second == "Yes") {
                        binding.locationText.text = strAddress
                    }
                    binding.locationText.setTextColor(ContextCompat.getColor(this, R.color.main))
                    binding.enterLocationLayout.visibility = View.GONE
                    binding.dateOfEventLabel.isVisible = true
                    binding.datePickedLayout.isVisible = true
                    binding.locationLabel.isVisible = true
                    binding.locationPickedLayout.isVisible = true
                    binding.notesLabel.isVisible = true
                    binding.notesToChef.isVisible = true
                    binding.addItemLayout.isVisible = true
                    binding.datePickedLayout.isVisible = true
                    if (eventDates.size != 0) {
                        binding.clearButton.isVisible = true
                        if (eventDates.size > 2) { binding.seeAllDatesButton.isVisible = true }}
                    binding.quantityOfEvent.isVisible = true
                    binding.allergiesLabel.isVisible = true
                    binding.allergies.isVisible = true
                    binding.additionalMenuRequirementsLabel.isVisible = true
                    binding.additionalMenuRequirements.isVisible = true
                    binding.timePeriodOfServiceLayout.isVisible = true
                }
                return null
            }
            val location: Address = address[0]
            location.latitude
            location.longitude



            if (second == "Yes") {
            binding.streetAddress.setText(address[0].getAddressLine(0))
            binding.city.setText(address[0].getAddressLine(0))
            binding.state.setText(address[0].adminArea)
            binding.zipCide.setText(address[0].postalCode)


                userLocation.latitude = location.latitude
                userLocation.longitude = location.longitude


                distance = chefLocation.distanceTo(userLocation) / 1609.34
                Log.d(TAG, "validateAddress: $distance")
                if (distance > 45) {

                    Toast.makeText(
                        this,
                        "Please note that the chef, will have the option to request a travel fee. This will take place in 'Orders' after purchase.",
                        Toast.LENGTH_LONG
                    ).show()
                    travelFeeOption = "Yes"
                }
            } else {
                chefLocation.latitude = location.latitude
                chefLocation.longitude = location.longitude
            }


            if (second == "Yes") {
                binding.locationText.text = address[0].getAddressLine(0)
            }
            binding.locationText.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.enterLocationLayout.visibility = View.GONE
            binding.dateOfEventLabel.isVisible = true
            binding.datePickedLayout.isVisible = true
            binding.locationLabel.isVisible = true
            binding.locationPickedLayout.isVisible = true
            binding.notesLabel.isVisible = true
            binding.notesToChef.isVisible = true
            binding.addItemLayout.isVisible = true
            binding.timePeriodOfServiceLayout.isVisible = true
            binding.datePickedLayout.isVisible = true
            if (eventDates.size != 0) {
                binding.clearButton.isVisible = true
                if (eventDates.size > 2) { binding.seeAllDatesButton.isVisible = true }}
            binding.quantityOfEvent.isVisible = true
            binding.allergiesLabel.isVisible = true
            binding.allergies.isVisible = true
            binding.additionalMenuRequirementsLabel.isVisible = true
            binding.additionalMenuRequirements.isVisible = true
//            p1 = GeoPoint((location.getLatitude() * 1E6) as Double, (location.getLongitude() * 1E6) as Double)

            return p1
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
    private fun getDaysInWeeks(month: Int, date: Int, year: Int, dayOfWeek: Int, dayOfMonth: Int): ArrayList<String> {
        var month = month + 1
        var day = date
        var year = year
        val cal1 = Calendar.getInstance()
        var daysInWeek = arrayListOf<String>()
        var startOfWeek = ""
        var endOfWeek = ""
//
        val b = date
        if (b < 7 && (date - dayOfWeek + 1 <= 0)) {
            cal1.set(year,month-1,day)
            val daysInMonth = cal1.getActualMaximum(Calendar.DAY_OF_MONTH)
            if (month == 0) {
                month = 12
                year -= 1
                day = daysInMonth - (dayOfWeek - date - 1)
            } else {
                month -= 1
                day = daysInMonth - (dayOfWeek - date - 1)
            }
            var newMonth = ""
            var newDay = ""
            if (month < 10) {
                newMonth = "0$month"
            } else {
                newMonth = "$month"
            }
            if (day < 10) {
                newDay = "0$day"
            } else {
                newDay = "$day"
            }
            startOfWeek = "$newMonth-${newDay}-$year"
            daysInWeek.add(startOfWeek)
            for (i in 0 until 6) {

                if (day < daysInMonth) {
                    day += 1
                } else {
                    if (day > 10) {
                        day = 1
                        if (month == 12) {
                            month = 1
                        } else {
                            month += 1
                        }
                    } else {
                        day += 1
                    }
                }
                    var newMonth = ""
                    var newDay = ""
                    if (month < 10) {
                        newMonth = "0$month"
                    } else {
                        newMonth = "$month"
                    }
                    if (day < 10) {
                        newDay = "0$day"
                    } else {
                        newDay = "$day"
                    }
                daysInWeek.add("$newMonth-${newDay}-$year")
            }
        } else {
            cal1.set(year,month,day)
            val daysInMonth = cal1.getActualMaximum(Calendar.DAY_OF_MONTH)
            day = dayOfMonth - dayOfWeek + 1
            var newMonth = ""
            var newDay = ""
            if (month < 10) {
                newMonth = "0$month"
            } else {
                newMonth = "$month"
            }
            if (day < 10) {
                newDay = "0$day"
            } else {
                newDay = "$day"
            }
            daysInWeek.add("$newMonth-${newDay}-$year")

            for (i in 0 until 6) {
                if (day < daysInMonth) {
                    day += 1
                } else {
                    day = 1
                    if (month == 12) {
                        month = 1
                    } else {
                        month += 1
                    }
                }
                    var newMonth = ""
                    var newDay = ""
                    if (month < 10) {
                        newMonth = "0$month"
                    } else {
                        newMonth = "$month"
                    }
                    if (day < 10) {
                        newDay = "0$day"
                    } else {
                        newDay = "$day"
                    }
                daysInWeek.add("$newMonth-${newDay}-$year")

            }
        }
        return daysInWeek
    }

    private fun getWeeks(month: Int, date: Int, year: Int, dayOfWeek: Int, dayOfMonth: Int): ArrayList<String> {
        var month = month + 1
        var day = date
        var year = year
        val cal1 = Calendar.getInstance()
        var startOfWeek = ""
        var endOfWeek = ""
//
        val b = date
        if (b < 7 && (date - dayOfWeek + 1 <= 0)) {
            cal1.set(year,month-1,day)
            val daysInMonth = cal1.getActualMaximum(Calendar.DAY_OF_MONTH)
            if (month == 0) {
                month = 12
                year -= 1
                day = daysInMonth - (dayOfWeek - date - 1)
            } else {
                month -= 1
                day = daysInMonth - (dayOfWeek - date - 1)
            }
            var newMonth = ""
            var newDay = ""
            if (month < 10) {
                newMonth = "0$month"
            } else {
                newMonth = "$month"
            }
            if (day < 10) {
                newDay = "0$day"
            } else {
                newDay = "$day"
            }
            startOfWeek = "$newMonth-${newDay}-$year"
            for (i in 0 until 6) {

                if (day < daysInMonth) {
                    day += 1
                } else {
                    if (day > 10) {
                        day = 1
                        if (month == 12) {
                            month = 1
                        } else {
                            month += 1
                        }
                    } else {
                        day += 1
                    }
                }
                if (i == 5) {
                    var newMonth = ""
                    var newDay = ""
                    if (month < 10) {
                        newMonth = "0$month"
                    } else {
                        newMonth = "$month"
                    }
                    if (day < 10) {
                        newDay = "0$day"
                    } else {
                        newDay = "$day"
                    }
                    endOfWeek = "$newMonth-${newDay}-$year"
                }
            }
        } else {
            cal1.set(year,month,day)
            val daysInMonth = cal1.getActualMaximum(Calendar.DAY_OF_MONTH)
            day = dayOfMonth - dayOfWeek + 1
            var newMonth = ""
            var newDay = ""
            if (month < 10) {
                newMonth = "0$month"
            } else {
                newMonth = "$month"
            }
            if (day < 10) {
                newDay = "0$day"
            } else {
                newDay = "$day"
            }
            startOfWeek = "$newMonth-${newDay}-$year"
            for (i in 0 until 6) {
                if (day < daysInMonth) {
                    day += 1
                } else {
                    day = 1
                    if (month == 12) {
                        month = 1
                    } else {
                        month += 1
                    }
                }
                if (i == 5) {
                    var newMonth = ""
                    var newDay = ""
                    if (month < 10) {
                        newMonth = "0$month"
                    } else {
                        newMonth = "$month"
                    }
                    if (day < 10) {
                        newDay = "0$day"
                    } else {
                        newDay = "$day"
                    }
                    endOfWeek = "$newMonth-${newDay}-$year"
                }
            }
        }
        return arrayListOf(startOfWeek, endOfWeek)
    }


}