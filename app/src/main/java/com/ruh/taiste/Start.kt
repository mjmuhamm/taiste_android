package com.ruh.taiste

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.both.PrivacyPolicyActivity
import com.ruh.taiste.both.TermsOfServiceActivity
import com.ruh.taiste.databinding.ActivityStartBinding
import com.ruh.taiste.user.Login
import com.ruh.taiste.user.MainActivity
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


private const val TAG = "Start"
class Start : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        Log.d(TAG, "onCreate: ${Calendar.getInstance().timeInMillis * 1000}")
        val cal = Calendar.getInstance()
        cal.time = Date()
        var today = cal.get(Calendar.MONTH)

        Log.d(TAG, "onCreate: moasdf $today ${cal.get(Calendar.YEAR)}")

        binding.user.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        binding.chef.setOnClickListener {
            val intent = Intent(this, com.ruh.taiste.chef.Login::class.java)
            startActivity(intent)
        }

        binding.termsOfService.setOnClickListener {
            val intent = Intent(this, TermsOfServiceActivity::class.java)
            startActivity(intent)
        }

        binding.privacyPolicy.setOnClickListener {
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, com.ruh.taiste.chef.MainActivity::class.java)
        intent.putExtra("where_to", "login")
        val intent1 = Intent(this, MainActivity::class.java)
        intent1.putExtra("where_to", "login")

        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(this)
            } else {
                isOnline1(this)
            }
        ) {
            if (auth.currentUser != null) {
                Log.d(TAG, "onStart: online ${auth.currentUser!!.displayName}")
                if (auth.currentUser!!.displayName!! == "Chef") {
                    startActivity(intent)
                } else {
                    startActivity(intent1)
                }
                finish()
            }
        } else {
            Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }



    }
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

@RequiresApi(Build.VERSION_CODES.M)
fun isOnline(context: Context) =
    (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
        getNetworkCapabilities(activeNetwork)?.run {
            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } ?: false
    }

@Suppress("DEPRECATION")
fun isOnline1(context: Context): Boolean {
    var result = false
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        cm?.run {
            cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                result = when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            }
        }
    } else {
        cm?.run {
            cm.activeNetworkInfo?.run {
                if (type == ConnectivityManager.TYPE_WIFI) {
                    result = true
                } else if (type == ConnectivityManager.TYPE_MOBILE) {
                    result = true
                }
            }
        }
    }
    return result
}