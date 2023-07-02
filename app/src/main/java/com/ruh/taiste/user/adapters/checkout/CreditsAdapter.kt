package com.ruh.taiste.user.adapters.checkout

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.R
import com.ruh.taiste.databinding.CheckoutPostBinding
import com.ruh.taiste.databinding.CreditsPostBinding
import com.ruh.taiste.user.ClickListener
import com.ruh.taiste.user.OrderDetails
import com.ruh.taiste.user.models.CheckoutItems
import com.ruh.taiste.user.models.Credits
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*

class CreditsAdapter(private val context: Context, private var credits: MutableList<Credits>) : RecyclerView.Adapter<CreditsAdapter.ViewHolder>() {

    private val db = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser!!.email!!

    private val httpClient = OkHttpClient()

    private val mHandler: Handler = Handler(Looper.getMainLooper())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(CreditsPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(credits[position])
        val item = credits[position]

        holder.refundButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Refund")
                .setMessage("Are you sure you want to refund?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, _ ->

                    refund(item.paymentIntent, item.refund.toDouble(), item.documentId)

                    credits.removeAt(position)
                    notifyItemRemoved(position)
                    dialog.dismiss()


                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }


    }

    override fun getItemCount() = credits.size

    fun submitList(credits: MutableList<Credits>) {
        this.credits = credits
    }


    private fun refund(paymentIntent: String, amount: Double, orderId: String) {

        val a = amount * 100
        val final = "%.0f".format(a.toInt())
        val body = FormBody.Builder()
            .add("payment_intent", paymentIntent)
            .add("amount", final)
            .build()

        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/refund")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    val data1: Map<String, Any> = hashMapOf(
                        "amount" to amount,
                        "paymentIntent" to paymentIntent,
                        "orderId" to orderId,
                        "user" to user
                    )
                    db.collection("RefundErrors").document(orderId).set(data1)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        displayAlert(
                            "Error: $response"
                        )
                        val data1: Map<String, Any> = hashMapOf(
                            "amount" to amount,
                            "paymentIntent" to paymentIntent,
                            "orderId" to orderId,
                            "user" to user
                        )
                        db.collection("RefundErrors").document(orderId).set(data1)
                    } else {
                        val responseData = response.body!!.string()
                        val json =
                            JSONObject(responseData)

                        val refundId = json.getString("refund_id")

                        mHandler.post {
                            val data1: Map<String, Any> = hashMapOf(
                                "amount" to amount,
                                "paymentIntent" to paymentIntent,
                                "orderId" to orderId,
                                "user" to user,
                                "refundId" to refundId
                            )
                            db.collection("Refunds").document(orderId)
                                .set(data1)
                            Toast.makeText(context, "Refund sent. Please give 7-10 days for this refund to show in your account.", Toast.LENGTH_LONG).show()

                            db.collection("User").document(user).collection("Credits")
                                .document(orderId).delete()

                        }
                    }
                }
            })
    }



class ViewHolder(itemView: CreditsPostBinding) : RecyclerView.ViewHolder(itemView.root) {

    private val context = itemView.root.context
    private val itemTitle = itemView.itemTitle
    private val orderDate = itemView.orderDate
    private val refund = itemView.refund
    private val creditsText = itemView.credits
    val refundButton = itemView.refundButton

    @SuppressLint("SetTextI18n")
    fun bind(item: Credits) {

        itemTitle.text = item.itemTitle
        orderDate.text = item.orderDate
        refund.text = "$${item.refund.toDouble()}"

        if (item.credits.toInt() == 0) {
            creditsText.visibility = View.GONE
        } else {
            creditsText.visibility = View.VISIBLE
            creditsText.text = "$${item.credits.toDouble()}"
        }

        if (item.refund.toInt() <= 0) {
            refundButton.isVisible = false
        }

    }

}
    private fun displayAlert(
        message: String
    ) {
        val builder = AlertDialog.Builder(context)
            .setTitle("Failed to load page.")
            .setMessage(message)

        builder.setPositiveButton("Ok", null)
        builder
            .create()
            .show()
    }
}