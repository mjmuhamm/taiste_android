package com.ruh.taiste.user

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.ruh.taiste.R
import com.ruh.taiste.both.fragments.Feed
import com.ruh.taiste.both.fragments.Search
import com.ruh.taiste.databinding.ActivityMainBinding
import com.ruh.taiste.user.fragments.Home
import com.ruh.taiste.user.fragments.Me
import com.ruh.taiste.user.fragments.Orders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage


private const val TAG = "MainActivity"
var userName = ""
var userImageId = ""
var userPreferences = ""
var userLocation = ""
var userImage : Uri?= null
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "You are prompted for notifications", Toast.LENGTH_LONG).show()
            // FCM SDK (and your app) can post notifications.
        } else {
            Toast.makeText(this, "You will not receive important notifications about events and chef messages without notification permission.", Toast.LENGTH_LONG).show()
            // TODO: Inform user that that your app will not show notifications.
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()
        if (FirebaseAuth.getInstance().currentUser != null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result

                // Log and toast
                val data: Map<String, Any> = hashMapOf("notificationToken" to token.toString())
                db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .update(data)
            })
        } else {
            Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate: ${intent.getStringExtra("where_to")}")

        val iconColorStatesMain = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)), intArrayOf(Color.parseColor("#626348"), Color.parseColor("#626348")))
        val iconColorStatesGray = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)), intArrayOf(Color.parseColor("#A2A1A1"), Color.parseColor("#A2A1A1")))

         binding.navView.setOnItemSelectedListener { item ->
             when (item.itemId) {
                 R.id.nav_home -> {
                     b()
                     binding.navView.menu[0].setIcon(R.drawable.home_filled).iconTintList = iconColorStatesGray
                     binding.navView.menu[1].setIcon(R.drawable.search).iconTintList = iconColorStatesGray
                     binding.navView.menu[2].setIcon(R.drawable.feed).iconTintList = iconColorStatesGray
                     binding.navView.menu[3].setIcon(R.drawable.orders).iconTintList = iconColorStatesGray
                     binding.navView.menu[4].setIcon(R.drawable.me).iconTintList = iconColorStatesMain

                     binding.navView.menu[0].isChecked = true
                     binding.navView.menu[1].isChecked = false
                     binding.navView.menu[2].isChecked = false
                     binding.navView.menu[3].isChecked = false
                     binding.navView.menu[4].isChecked = false

                     moveToFragment(Home())
                     return@setOnItemSelectedListener true
                 }
                 R.id.nav_search -> {
                     b()
                     binding.navView.menu[0].setIcon(R.drawable.home).iconTintList = iconColorStatesGray
                     binding.navView.menu[1].setIcon(R.drawable.search_filled).iconTintList = iconColorStatesGray
                     binding.navView.menu[2].setIcon(R.drawable.feed).iconTintList = iconColorStatesGray
                     binding.navView.menu[3].setIcon(R.drawable.orders).iconTintList = iconColorStatesGray
                     binding.navView.menu[4].setIcon(R.drawable.me).iconTintList = iconColorStatesMain

                     binding.navView.menu[0].isChecked = false
                     binding.navView.menu[1].isChecked = true
                     binding.navView.menu[2].isChecked = false
                     binding.navView.menu[3].isChecked = false
                     binding.navView.menu[4].isChecked = false
                     moveToFragment(Search())
                     return@setOnItemSelectedListener true
                 }
                 R.id.nav_feed -> {
                     a()
                     binding.navView.menu[0].setIcon(R.drawable.home).iconTintList = iconColorStatesGray
                     binding.navView.menu[1].setIcon(R.drawable.search).iconTintList = iconColorStatesGray
                     binding.navView.menu[2].setIcon(R.drawable.feed_filled).iconTintList = iconColorStatesGray
                     binding.navView.menu[3].setIcon(R.drawable.orders).iconTintList = iconColorStatesGray
                     binding.navView.menu[4].setIcon(R.drawable.me).iconTintList = iconColorStatesMain

                     binding.navView.menu[0].isChecked = false
                     binding.navView.menu[1].isChecked = false
                     binding.navView.menu[2].isChecked = true
                     binding.navView.menu[3].isChecked = false
                     binding.navView.menu[4].isChecked = false
                     moveToFragment(Feed())
                     return@setOnItemSelectedListener true
                 }

                 R.id.nav_orders -> {
                     b()
                     binding.navView.menu[0].setIcon(R.drawable.home).iconTintList = iconColorStatesGray
                     binding.navView.menu[1].setIcon(R.drawable.search).iconTintList = iconColorStatesGray
                     binding.navView.menu[2].setIcon(R.drawable.feed).iconTintList = iconColorStatesGray
                     binding.navView.menu[3].setIcon(R.drawable.orders_filled).iconTintList = iconColorStatesGray
                     binding.navView.menu[4].setIcon(R.drawable.me).iconTintList = iconColorStatesMain

                     binding.navView.menu[0].isChecked = false
                     binding.navView.menu[1].isChecked = false
                     binding.navView.menu[2].isChecked = false
                     binding.navView.menu[3].isChecked = true
                     binding.navView.menu[4].isChecked = false
                     moveToFragment(Orders())
                     return@setOnItemSelectedListener true
                 }

                 R.id.nav_me -> {
                     b()
                     binding.navView.menu[0].setIcon(R.drawable.home).iconTintList = iconColorStatesGray
                     binding.navView.menu[1].setIcon(R.drawable.search).iconTintList = iconColorStatesGray
                     binding.navView.menu[2].setIcon(R.drawable.feed).iconTintList = iconColorStatesGray
                     binding.navView.menu[3].setIcon(R.drawable.orders).iconTintList = iconColorStatesGray
                     binding.navView.menu[4].setIcon(R.drawable.me_filled).iconTintList = iconColorStatesMain

                     binding.navView.menu[0].isChecked = false
                     binding.navView.menu[1].isChecked = false
                     binding.navView.menu[2].isChecked = false
                     binding.navView.menu[3].isChecked = false
                     binding.navView.menu[4].isChecked = true
                     moveToFragment(Me())
                     return@setOnItemSelectedListener true
                 }
             }

             false
         }

        when (intent.getStringExtra("where_to").toString()) {
            "login" -> {
                a()
                moveToFragment(Feed())
            }
            "home" -> {
                b()
                moveToFragment(Home())
            }
            "orders" -> {
                b()
                moveToFragment(Orders())
            }
        }


    }

    @SuppressLint("ResourceAsColor")
    private fun a() {
        val iconColorStates = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ), intArrayOf(
                Color.parseColor("#FFFFFFFF"),
                Color.parseColor("#FFFFFFFF")
            )
        )
        binding.container.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
        binding.navView.itemIconTintList = iconColorStates
        binding.navView.itemTextColor = iconColorStates
        binding.navView.setBackgroundColor(Color.TRANSPARENT)

    }

    @SuppressLint("ResourceAsColor")
    private fun b() {
        val iconColorStates = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)), intArrayOf(Color.parseColor("#A2A1A1"), Color.parseColor("#A0A268")))
        binding.container.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        binding.navView.itemIconTintList = iconColorStates
        binding.navView.itemTextColor = iconColorStates
        binding.navView.setBackgroundColor(Color.WHITE)

    }

    private fun moveToFragment(fragment: Fragment) {
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragment_container, fragment)
        fragmentTrans.commit()
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        Log.d(TAG, "askNotificationPermission: not happening")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                AlertDialog.Builder(this)
                    .setTitle("Notifications")
                    .setMessage("By accepting notifications, you will be notified with important event updates and chef messages.")
                    .setCancelable(false)
                    .setPositiveButton("Ok") { dialog, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        dialog.dismiss()
                    }
                    .setNegativeButton("No thanks") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
             } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }




}