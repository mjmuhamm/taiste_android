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
import com.aminography.primedatepicker.picker.PrimeDatePicker
import com.aminography.primedatepicker.picker.callback.MultipleDaysPickCallback
import com.ruh.taiste.R
import com.ruh.taiste.databinding.ActivityOrderDetailsBinding
import com.ruh.taiste.user.models.CheckoutItems
import com.ruh.taiste.user.models.FeedMenuItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import okio.IOException
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "OrderDetails"
class OrderDetails : AppCompatActivity() {
    private lateinit var binding: ActivityOrderDetailsBinding

    private val db = Firebase.firestore


    private var eventDates : MutableList<String> = mutableListOf()
    private var eventTimes : MutableList<String> = mutableListOf()
    private var eventDatesForUser : MutableList<String> = mutableListOf()

    private lateinit var datePicker : PrimeDatePicker
    private var item : FeedMenuItems? = null
    private lateinit var checkoutItem : CheckoutItems

    private var chefLocation : Location = Location("chefLocation")
    private var userLocation : Location = Location("userLocation")

    private var travelFeeOption = "No"
    private var distance = 0.0

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        item = intent.getParcelableExtra<FeedMenuItems>("item")

        @Suppress("DEPRECATION")
        val checkoutItem1 = intent.getParcelableExtra<CheckoutItems>("checkout_item")

        val typeOfEvent = resources.getStringArray(R.array.typeOfEvent)
        val typeOfEventAdapter = ArrayAdapter(this, R.layout.dropdown_item, typeOfEvent)

        val cateringQuantityOfEvent = resources.getStringArray(R.array.catering_quantityOfEvent)
        val cateringQuantityOfEventAdapter =
            ArrayAdapter(this, R.layout.dropdown_item, cateringQuantityOfEvent)

        val personalChefQuantityOfEvent =
            resources.getStringArray(R.array.personal_chef_quantityOfEvent)
        val personalChefQuantityOfEventAdapter =
            ArrayAdapter(this, R.layout.dropdown_item, personalChefQuantityOfEvent)

        val mealKitQuantityOfEvent = resources.getStringArray(R.array.meal_kit_quantityOfEvent)
        val mealKitQuantityOfEventAdapter =
            ArrayAdapter(this, R.layout.dropdown_item, mealKitQuantityOfEvent)


        Log.d(TAG, "onCreate item: $item")
        val streetAddress = "${item!!.city}, ${item!!.state}"
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(this)
            } else {
                isOnline1(this)
            }
        ) {
            validateAddress(streetAddress, "")
        } else {
            Toast.makeText(
                this,
                "Seems to be a problem with your internet. Please check your connection.",
                Toast.LENGTH_LONG
            ).show()
        }


        if (item != null) {

            this.item = item
            val item = this.item!!
//            chefLocation.latitude = item.latitude.toDouble()
//            chefLocation.longitude = item.longitude.toDouble()

            binding.itemTitle.text = item.itemTitle
            binding.itemDescription.text = item.itemDescription

            when (item.itemType) {
                "Cater Items" -> {
                    binding.typeOfEvent.setAdapter(typeOfEventAdapter)
                }
                "Executive Items" -> {
                    binding.typeOfEvent.setAdapter(typeOfEventAdapter)
                }
                else -> {
                    binding.typeOfEvent.setText("Meal Kit")
                }
            }
        }
        if (checkoutItem1 != null) {
            this.checkoutItem = checkoutItem1


            when (checkoutItem.typeOfService) {
                "Cater Items" -> {
                    binding.typeOfEvent.setAdapter(typeOfEventAdapter)
                }
                "Executive Items" -> {
                    binding.typeOfEvent.setAdapter(typeOfEventAdapter)
                }
                else -> {
                    binding.typeOfEvent.setText("Meal Kit")
                }
            }
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

                if (FirebaseAuth.getInstance().currentUser != null) {
                    loadOrder()
                } else {
                    Toast.makeText(
                        this,
                        "Something went wrong. Please check your connection.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Seems to be a problem with your internet. Please check your connection.",
                    Toast.LENGTH_LONG
                ).show()
            }

        }


        //Date Picker
        val callback = MultipleDaysPickCallback { days ->

            for (i in 0 until days.size) {
                val month = days[i].month
                val day = days[i].dayOfMonth
                val year = days[i].year

                val userDate = "${days[i].monthDayString}, $year"


                val newMonth = if (month < 10) {
                    "0$month"
                } else {
                    "$month"
                }
                val newDay = if (day < 10) {
                    "0$day"
                } else {
                    "$day"
                }

                eventDates.add("$newMonth-$newDay-$year")
                eventDatesForUser.add(userDate)
            }

            if (item != null) {
                val item = this.item!!
                val quantity = binding.quantityOfEventText.text.toString().toInt()
                var percentage = 1.0
                if (quantity > 25) {
                    percentage = 0.90
                } else if (quantity > 50) {
                    percentage = 0.83
                } else if (quantity > 75) {
                    percentage = 0.75
                } else if (quantity > 100) {
                    percentage = 0.70
                }
                val a = "%.2f".format(
                    item.itemPrice.toDouble() * binding.quantityOfEventText.text.toString()
                        .toInt() * days.size * percentage
                )
                binding.priceOfEvent.text = "$$a"


            }

            binding.timePickerLayout.visibility = View.VISIBLE
            binding.dateForTimePicker.text = eventDatesForUser[0]
            binding.quantityOfEventText.isVisible = false
            binding.dateOfEventLabel.isVisible = false
            binding.datePickedLayout.isVisible = false
            binding.locationLabel.isVisible = false
            binding.locationPickedLayout.isVisible = false
            binding.notesLabel.isVisible = false
            binding.notesToChef.isVisible = false
            binding.addItemLayout.isVisible = false
            binding.clearButton.isVisible = false
        }







        val today = CivilCalendar()
        datePicker = PrimeDatePicker.bottomSheetWith(today).pickMultipleDays(callback).minPossibleDate(today).build()


        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.quantityOfEventText.setOnClickListener {
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

        binding.datePickerButton.setOnClickListener {
            if (binding.quantityOfEventText.text.isEmpty()) {
                Toast.makeText(this, "Please enter a quantity for your event first.", Toast.LENGTH_LONG).show()
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

            if (eventTimes.size != eventDates.size) {
                binding.timePicker.hour = 12
                binding.timePicker.minute = 0
                binding.timePicker.clearFocus()
                binding.timePicker.refreshDrawableState()
                binding.dateForTimePicker.text = eventDatesForUser[eventTimes.size]
            } else {
                binding.datePickerText.setTextColor(ContextCompat.getColor(this, R.color.main))
                binding.clearButton.isVisible = true
                binding.timePickerLayout.visibility = View.GONE
                binding.quantityOfEventText.isVisible = true
                binding.dateOfEventLabel.isVisible = true
                binding.datePickedLayout.isVisible = true
                binding.locationLabel.isVisible = true
                binding.locationPickedLayout.isVisible = true
                binding.notesLabel.isVisible = true
                binding.notesToChef.isVisible = true
                binding.addItemLayout.isVisible = true
                binding.datePickerButton.isEnabled = false

                for (i in 0 until eventDates.size) {
                    if (i == 0) {
                        binding.datePickerText.text = "${eventDates[i]} ${eventTimes[i]}"
                    } else if (i == 1) {
                        binding.datePickerText.text = "${binding.datePickerText.text}, ${eventDates[i]} ${eventTimes[i]}"
                        binding.seeAllDatesText.text = binding.datePickerText.text
                        if (eventDates.size > 2) {
                            binding.datePickerText.text = "${binding.datePickerText.text}..."
                            binding.seeAllDatesButton.isVisible = true
                        }
                    } else {
                        binding.seeAllDatesText.text = "${binding.seeAllDatesText.text}, ${eventDates[i]} ${eventTimes[i]}"
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
        }

        binding.seeAllDatesOkButton.setOnClickListener {
            binding.seeAllDatesLayout.visibility = View.GONE
            binding.datePickedLayout.isVisible = true
            binding.locationLabel.isVisible = true
            binding.locationPickedLayout.isVisible = true
            binding.notesToChef.isVisible = true
            binding.notesLabel.isVisible = true
            binding.addItemLayout.isVisible = true

        }

        binding.clearButton.setOnClickListener {
            eventDates.clear()
            binding.priceOfEvent.text = ""
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
            binding.quantityOfEventText.isVisible = false
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
                binding.zipCode.text.isEmpty() -> {
                    Toast.makeText(this, "Please enter your event zip code.", Toast.LENGTH_LONG).show()
                }
                else -> {

                    val streetAddress = if (binding.streetAddress2.text.isNotEmpty()) {
                        "${binding.streetAddress.text} ${binding.streetAddress2.text}"
                    } else { "${binding.streetAddress.text}" }

                    val address = "$streetAddress ${binding.city.text}, ${binding.state.text}, ${binding.zipCode.text}"

                    validateAddress(address, "Yes")
                }
            }
            } else {
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
                when {
                    binding.typeOfEvent.text.isEmpty() -> {
                        Toast.makeText(
                            this,
                            "Please select a type of event in the allotted field.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    binding.quantityOfEventText.text.isEmpty() -> {
                        Toast.makeText(
                            this,
                            "Please select a quantity of event in the allotted field.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    eventDates.isEmpty() -> {
                        Toast.makeText(
                            this,
                            "Please select the date(s) of the event.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    binding.streetAddress.text.isEmpty() -> {
                        Toast.makeText(
                            this,
                            "Please select a location for this event.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        if (FirebaseAuth.getInstance().currentUser != null) {
                            addToCart()
                        } else {
                            Toast.makeText(
                                this,
                                "Something went wrong. Please check your connection.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

    }
    @SuppressLint("SetTextI18n")
    @Suppress("UNCHECKED_CAST")
    private fun loadOrder() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("Cart")
                .document(checkoutItem.documentId).get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val data = document.data

                        val typeOfEvent = data?.get("typeOfEvent") as String
                        val quantityOfEvent = data["quantityOfEvent"] as String
                        val eventDates = data["datesOfEvent"] as ArrayList<String>
                        val eventTimes = data["timesForDatesOfEvent"] as ArrayList<String>
                        val location = data["location"] as String
                        val notesToChef = data["notesToChef"] as String
                        val costOfEvent = data["totalCostOfEvent"] as Number
                        val itemTitle = data["itemTitle"] as String
                        val itemDescription = data["itemDescription"] as String

                        binding.itemTitle.text = itemTitle
                        binding.itemDescription.text = itemDescription
                        binding.typeOfEvent.setText(typeOfEvent)
                        binding.quantityOfEventText.setText(quantityOfEvent)
                        binding.locationText.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.main
                            )
                        )
                        binding.datePickerText.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.main
                            )
                        )

                        for (i in 0 until eventDates.size) {
                            if (i == 0) {
                                binding.datePickerText.text = "${eventDates[i]} ${eventTimes[i]}"
                            } else {
                                if (i < 2) {
                                    binding.datePickerText.text =
                                        "${binding.datePickerText.text}, ${eventDates[i]} ${eventTimes[i]}"
                                    binding.seeAllDatesText.text = binding.datePickerText.text
                                } else {
                                    binding.seeAllDatesText.text =
                                        "${binding.seeAllDatesText.text}, ${eventDates[i]}, ${eventTimes[i]}"
                                    binding.seeAllDatesButton.visibility = View.VISIBLE
                                }
                            }
                        }

                        binding.locationText.text = location
                        binding.notesToChef.setText(notesToChef)

                        binding.locationButton.isEnabled = false
                        binding.datePickerButton.isEnabled = false
                        binding.addButton.isVisible = false
                        val a = "%.2f".format(costOfEvent)
                        binding.priceOfEvent.text = "$$a"


                    }
                }
        } else {
            Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
        }
    }

    private var no = ""
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
                    binding.quantityOfEventText.isVisible = true
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
                binding.zipCode.setText(address[0].postalCode)


                userLocation.latitude = location.latitude
                userLocation.longitude = location.longitude

                distance = chefLocation.distanceTo(userLocation) / 1609.34

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
            binding.datePickedLayout.isVisible = true
            if (eventDates.size != 0) {
                binding.clearButton.isVisible = true
                if (eventDates.size > 2) { binding.seeAllDatesButton.isVisible = true }}
            binding.quantityOfEventText.isVisible = true
//            p1 = GeoPoint((location.getLatitude() * 1E6) as Double, (location.getLongitude() * 1E6) as Double)

            return p1
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun addToCart() {
        val item = this.item!!
        val intent = Intent(this, MainActivity::class.java)
        val documentId = UUID.randomUUID().toString()
        val priceToChef = binding.priceOfEvent.text.takeLast(binding.priceOfEvent.text.length - 1).toString().toDouble() * 0.07
        val totalCostOfEvent = binding.priceOfEvent.text.takeLast(binding.priceOfEvent.text.length - 1).toString().toDouble()
        intent.putExtra("where_to", "home")
        val data : Map<String, Any> = hashMapOf("chefEmail" to item.chefEmail, "chefImageId" to item.chefImageId, "chefUsername" to item.chefUsername, "city" to binding.city.text.toString(), "datesOfEvent" to eventDates, "distance" to "$distance", "itemDescription" to item.itemDescription, "itemTitle" to item.itemTitle, "latitudeOfEvent" to "$userLocation.latitude", "location" to binding.locationText.text, "longitudeOfEvent" to "$userLocation.longitude", "menuItemId" to item.menuItemId, "notesToChef" to binding.notesToChef.text.toString(), "priceToChef" to priceToChef, "quantityOfEvent" to binding.quantityOfEventText.text.toString(), "state" to binding.state.text.toString(), "timesForDatesOfEvent" to eventTimes, "totalCostOfEvent" to totalCostOfEvent, "travelExpenseOption" to travelFeeOption, "typeOfEvent" to binding.typeOfEvent.text.toString(), "typeOfService"  to item.itemType, "unitPrice" to item.itemPrice, "user" to FirebaseAuth.getInstance().currentUser!!.uid, "imageCount" to item.imageCount, "liked" to item.liked, "itemOrders" to item.itemOrders, "itemRating" to item.itemRating, "itemCalories" to item.itemCalories, "allergies" to "", "additionalMenuItems" to "", "signatureDishId" to "", "eventDatesForUser" to "")
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




}