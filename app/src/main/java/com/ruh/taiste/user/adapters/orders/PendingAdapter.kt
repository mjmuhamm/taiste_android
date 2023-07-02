package com.ruh.taiste.user.adapters.orders

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.ruh.taiste.both.Messages
import com.ruh.taiste.databinding.UserOrderPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.R
import com.ruh.taiste.both.guserName
import com.ruh.taiste.both.models.PendingOrders
import com.ruh.taiste.both.models.VideoModel
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "PendingAdapter"
class PendingAdapter(private val context: Context, private var pendingOrders: MutableList<PendingOrders>) : RecyclerView.Adapter<PendingAdapter.ViewHolder>()  {

    @SuppressLint("SimpleDateFormat")
    private var sdfMonth = SimpleDateFormat("MM")

    @SuppressLint("SimpleDateFormat")
    private var sdfDay = SimpleDateFormat("dd")

    @SuppressLint("SimpleDateFormat")
    private var sdfYear = SimpleDateFormat("yyyy")

    @SuppressLint("SimpleDateFormat")
    private var sdf = SimpleDateFormat("MM-dd-yyyy")


    @SuppressLint("SimpleDateFormat")
    private var sdfT = SimpleDateFormat("MM-dd-yyyy hh:mm a")

    private var db = Firebase.firestore
    private var user = FirebaseAuth.getInstance().currentUser!!.uid


    private val httpClient = OkHttpClient()

    private val mHandler: Handler = Handler(Looper.getMainLooper())



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(UserOrderPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }


    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pendingOrders[position])
        val item = pendingOrders[position]

        var date = ""
        for (i in 0 until item.eventDates.size) {
            if (i == 0) {
                date = "${item.eventDates[i]} ${item.eventTimes[i]}"
            } else {
                val time = sdfT.parse(date).time
                val day = "${item.eventDates[i]} ${item.eventTimes[i]}"
                val dayT = sdfT.parse(day).time
                if (dayT < time) {
                    date = day
                }
            }
//            if (i == item.eventDates.size - 1) {
//                val time = sdfT.parse(date).time / 1000 / 60 / 60
//                val today = Timestamp(System.currentTimeMillis()).time / 1000 / 60 / 60 + 1

//              if (today - time <= 0) {
//                    holder.infoLabel.text = "Event"
//                    holder.infoText.text = "We have credited this event due to lack of response from the chef. Please check your 'Me' -> 'Credits' tab for more info."
//                    holder.infoOkButton.text = "Ok"
//                    db.collection("Chef").document(item.chefEmail).collection("PendingOrders").document(item.orderId).delete()
//
//
//                    holder.itemTitle.isVisible = false
//                    holder.eventTypeAndQuantity.isVisible = false
//                    holder.location.isVisible = false
//                    holder.showNotes.isVisible = false
//                    holder.showDates.isVisible = false
//                    if (item.travelFeeOption == "Yes") {
//                        holder.messagesForTravelFee.isVisible = true
//                    }
//
//                    holder.cancel.isVisible = false
//                }
//            }
        }


        if (item.cancelled != "") {
            holder.infoLayout.visibility = View.VISIBLE
            holder.itemTitle.isVisible = false
            holder.eventTypeAndQuantity.isVisible = false
            holder.location.isVisible = false
            holder.showNotes.isVisible = false
            holder.showDates.isVisible = false
            if (item.travelFeeOption == "Yes") {
                holder.messagesForTravelFee.isVisible = false
            }
            holder.cancel.isVisible = false
            holder.accept.isVisible = false

            holder.infoLabel.text = "Order Cancelled"
            holder.infoText.text = "The chef has cancelled this order. A full refund has been issued. We apologize for this inconvenience and good luck with your event."
            holder.infoLabel.setTextColor(ContextCompat.getColor(context, com.aminography.primedatepicker.R.color.red400))
            holder.infoText.setTextColor(ContextCompat.getColor(context, com.aminography.primedatepicker.R.color.red400))
            holder.infoOkButton.setTextColor(ContextCompat.getColor(context, com.aminography.primedatepicker.R.color.red400))
            val data : Map<String, Any> = hashMapOf("orderUpdate" to "cancelled")
            db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").document(item.orderId).update(data)


        }



        holder.showDates.setOnClickListener {
            holder.infoLayout.visibility = View.VISIBLE
            holder.itemTitle.isVisible = false
            holder.eventTypeAndQuantity.isVisible = false
            holder.location.isVisible = false
            holder.showNotes.isVisible = false
            holder.showDates.isVisible = false
            if (item.travelFeeOption == "Yes") {
                holder.messagesForTravelFee.isVisible = false
            }
            holder.cancel.isVisible = false
            holder.accept.isVisible = false

            if (item.eventDates.size > 1) { holder.infoLabel.text = "Event Dates"
            } else { holder.infoLabel.text = "Event Date" }

            for (i in 0 until item.eventDates.size) {
             if (i == 0) {
                 holder.infoText.text = "${item.eventDates[i]} ${item.eventTimes[i]}"
             } else {
                 holder.infoText.text = "${holder.infoText.text}, ${item.eventDates[i]} ${item.eventTimes[i]}"
             }
            }
            holder.infoOkButton.isVisible = true
        }

        holder.cancel.setOnClickListener {
            Log.d(TAG, "onBindViewHolder: ${item.orderId}")


            AlertDialog.Builder(context)
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel this order?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, _ ->
//                    transfer()
                    db.collection("Orders").document(item.orderId).get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val data = document.data

                            if (data?.get("paymentIntent") != null) {

                                val paymentId = data["paymentIntent"] as String
                                Log.d(TAG, "onBindViewHolder: before refund ${item.totalCostOfEvent.toDouble() + item.taxesAndFees.toDouble()}")
                                refundOrder(paymentId, (item.totalCostOfEvent.toDouble() + item.taxesAndFees.toDouble()).toString(), item.orderId, item.userImageId, item.chefImageId, 0.0)

                                Toast.makeText(context, "Item cancelled. Your refund is on its way.", Toast.LENGTH_LONG).show()

                                val sdfT = SimpleDateFormat("MM-dd-yyyy hh:mm a")
                                val date1 = sdfT.format(Date())
                                val data3: Map<String, Any> = hashMapOf("notification" to "$guserName has just cancelled your item (${item.eventType}) about ${item.itemTitle}", "date" to date1)
                                val data4: Map<String, Any> = hashMapOf("notifications" to "yes")
                                db.collection("Chef").document(item.chefImageId).collection("Notifications").document().set(data3)
                                db.collection("Chef").document(item.chefImageId).update(data4)
                                pendingOrders.removeAt(position)
                                notifyItemRemoved(position)
                                dialog.dismiss()
                            }

                        }
                    }

                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        holder.showNotes.setOnClickListener {
            holder.infoLayout.visibility = View.VISIBLE

            holder.itemTitle.isVisible = false
            holder.eventTypeAndQuantity.isVisible = false
            holder.location.isVisible = false
            holder.showNotes.isVisible = false
            holder.showDates.isVisible = false
            if (item.travelFeeOption == "Yes") { holder.messagesForTravelFee.isVisible = false }
            holder.cancel.isVisible = false
            holder.accept.isVisible = false
            holder.infoLabel.text = "Notes to Chef"
            if (item.typeOfService == "Executive Item") {
                holder.infoText.text = "Allergies: ${item.allergies}  | Additional Menu Requests: ${item.additionalMenuItems}  |  Notes: ${item.eventNotes}"
            } else {
                holder.infoText.text = item.eventNotes
            }
            holder.infoOkButton.isVisible = true


        }

        holder.infoOkButton.setOnClickListener {
            if (holder.infoLabel.text == "Order Cancelled") {
                pendingOrders.removeAt(position)
                notifyItemRemoved(position)
            } else {
                holder.infoLayout.visibility = View.GONE
                holder.itemTitle.isVisible = true
                holder.eventTypeAndQuantity.isVisible = true
                holder.location.isVisible = true
                holder.showNotes.isVisible = true
                holder.showDates.isVisible = true
                if (item.travelFeeOption == "Yes") {
                    holder.messagesForTravelFee.isVisible = true
                }

                holder.cancel.isVisible = true
                holder.accept.isVisible = true
                if (holder.infoLabel.text == "Event") {

                    db.collection("Orders").document(item.orderId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val data = document.data
                                if (data?.get("paymentId") != null) {
                                    val paymentId = data["paymentId"] as String
                                    val data1: Map<String, Any> = hashMapOf(
                                        "refund" to (item.totalCostOfEvent.toDouble() - (item.taxesAndFees.toDouble() / item.numberOfEvents.toDouble())),
                                        "orderDate" to item.orderDate,
                                        "paymentIntent" to paymentId,
                                        "itemTitle" to item.itemTitle,
                                        "credits" to (item.totalCostOfEvent.toDouble() / item.numberOfEvents.toDouble())
                                    )
                                    db.collection("User").document(user).collection("Credits")
                                        .document(item.orderId).set(data1)
                                }
                            }
                        }

                    db.collection("User").document(user).collection("PendingOrders")
                        .document(item.orderId).delete()
                }}

        }

        holder.messagesForTravelFee.setOnClickListener {

            val intent = Intent(context, Messages::class.java)
            intent.putExtra("event_type", item.eventType)
            intent.putExtra("location", item.location)
            intent.putExtra("menu_item_id", item.menuItemId)
            intent.putExtra("total_cost_of_event", item.totalCostOfEvent)
            intent.putExtra("event_quantity", item.eventQuantity)
            intent.putExtra("travel_fee_or_message", "TravelFeeMessages")
            intent.putExtra("item_title", item.itemTitle)

            //
            val senderImageId = FirebaseAuth.getInstance().currentUser!!.uid
            val receiverImageId = item.chefImageId
            val chefOrUser = FirebaseAuth.getInstance().currentUser!!.displayName
            val documentId = item.orderId
            val senderUserName = guserName
            val receiverUserName = item.chefUsername
            val senderEmail = FirebaseAuth.getInstance().currentUser!!.email!!
            val receiverEmail = item.chefEmail
            val receiverChefOrUser = "Chef"

            intent.putExtra("order_message_sender_image_id", senderImageId)
            intent.putExtra("order_message_receiver_image_id",  receiverImageId)
            intent.putExtra("order_message_chef_or_user", chefOrUser)
            intent.putExtra("order_message_document_id", documentId)
            intent.putExtra("order_message_receiver_name", receiverUserName)
            intent.putExtra("order_message_sender_name", senderUserName)
            intent.putExtra("order_message_receiver_email", receiverEmail)
            intent.putExtra("order_message_sender_email", senderEmail)
            intent.putExtra("order_message_receiver_chef_or_user", receiverChefOrUser)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = pendingOrders.size

    fun submitList(pendingOrders: MutableList<PendingOrders>) {
        this.pendingOrders = pendingOrders
    }


    private fun transfer() {


                        val body = FormBody.Builder()
                            .add("stripeAccountId", "acct_1N3nHBH5i07sliKT")
                            .add("amount", "1000000")
                            .build()

                        val request = Request.Builder()
                            .url("https://taiste-payments.onrender.com/transfer")
                            .addHeader("Content-Type", "application/json; charset=utf-8")
                            .post(body)
                            .build()

                        httpClient.newCall(request)
                            .enqueue(object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    if (!response.isSuccessful) {
                                        mHandler.post {
                                        displayAlert(
                                            "Error: $response"
                                        )
                                    }
                                    } else {
                                        val responseData = response.body!!.string()
                                        val json =
                                            JSONObject(responseData)

                                        val transferId = json.getString("transferId")

                                        mHandler.post {
                                            Log.d(TAG, "onResponse: transferid $transferId")

                                        }}
                                    }
                            })
    }
    private fun refundOrder(paymentId: String, amount: String, orderId: String, userImageId: String, chefImageId: String, chargeForPayout: Double) {

        val a = "%.0f".format(amount.toDouble() * 100)
        Log.d(TAG, "refundOrder: $a")
        Log.d(TAG, "refundOrder: $amount")
        
        val body = FormBody.Builder()
            .add("paymentId", paymentId)
            .add("amount", a)
            .build()


        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/refund")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        mHandler.post {
                        displayAlert(
                            "Error: $response"
                        )
                    }
                    } else {
                        val responseData = response.body!!.string()
                        val json =
                            JSONObject(responseData)

                        val refundId = json.getString("refund_id")

                        mHandler.post {
                            val data: Map<String, Any> = hashMapOf(
                                "refundId" to refundId,
                                "paymentIntent" to paymentId,
                                "orderId" to orderId,
                                "date" to "",
                                "userImageId" to userImageId,
                                "chefImageId" to chefImageId,
                                "chargeForPayout" to chargeForPayout
                            )
                            val data1: Map<String, Any> = hashMapOf(
                                "cancelled" to "refunded",
                                "orderUpdate" to "cancelled"
                            )
                            val data2: Map<String, Any> = hashMapOf("cancelled" to "cancelled")

                            db.collection("Refunds").document(orderId).set(data)
                            db.collection("Chef").document(chefImageId).collection("Orders")
                                .document(orderId).update(data2)
                            db.collection("User").document(userImageId).collection("Orders")
                                .document(orderId).update(data1)
                            db.collection("Orders").document(orderId).update(data1)

                        }}
                }
            })



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

    class ViewHolder(itemView: UserOrderPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        val context = itemView.root.context!!
        val itemType = itemView.itemType
        val orderDate = itemView.orderDate
        val itemTitle = itemView.itemTitle
        val eventTypeAndQuantity = itemView.eventTypeAndQuantity
        val location = itemView.location
        val showDates = itemView.showDates
        val showNotes = itemView.showNotes
        val messagesForTravelFee = itemView.messagesForTravelFee
        val cancel = itemView.cancelButton
        val accept = itemView.messagesButton
        val infoLayout = itemView.infoLayout
        val infoLabel = itemView.infoLabel
        val infoText = itemView.infoText
        val infoOkButton = itemView.infoOkButton


        @SuppressLint("SetTextI18n")
        fun bind(item: PendingOrders) {
            if (item.typeOfService == "Executive Item") {
                itemType.text = "Personal Chef Item"
            } else {
                itemType.text = item.typeOfService
            }
            orderDate.text = "Ordered: ${item.orderDate}"
            itemTitle.text = item.itemTitle
            eventTypeAndQuantity.text = "Event Type: ${item.eventType}     Event Quantity: ${item.eventQuantity}"
            location.text = "Location: ${item.location}"

            Log.d(TAG, "bind: item title ${item.itemTitle}")
            Log.d(TAG, "bind: item title text ${itemTitle.text}")
//            Log.d(TAG, "bind: event type ${}")
            if (item.travelFeeOption == "Yes") { messagesForTravelFee.visibility = View.VISIBLE
            } else { messagesForTravelFee.visibility = View.GONE }
            accept.visibility = View.INVISIBLE


//            orderUpdate.text = "* pending chef acceptance *"



        }

    }


}