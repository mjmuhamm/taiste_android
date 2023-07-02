package com.ruh.taiste.chef.adapters.orders

import android.annotation.SuppressLint
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.R
import com.ruh.taiste.both.ItemDetail
import com.ruh.taiste.both.Messages
import com.ruh.taiste.both.ReportAnIssue
import com.ruh.taiste.both.guserName
import com.ruh.taiste.both.models.ScheduledOrders
import com.ruh.taiste.databinding.ChefOrderPostBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "ScheduledAdapter"
class ScheduledAdapter(private val context: Context, private var scheduledOrders: MutableList<ScheduledOrders>) : RecyclerView.Adapter<ScheduledAdapter.ViewHolder>()  {


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

    private val db = Firebase.firestore
    private val chef = FirebaseAuth.getInstance().currentUser!!.email!!

    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ChefOrderPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(scheduledOrders[position])
        val item = scheduledOrders[position]

        holder.infoOkButton.isVisible = false


        if (item.cancelled != "") {
            AlertDialog.Builder(context)
                .setTitle("Event Cancelled.")
                .setMessage("The user cancelled this event. $${item.cancelled} will be awarded to you on your next payout for this inconvenience.")
                .setCancelable(false)
                .setPositiveButton("Ok") { dialog, _ ->

                    val data : Map<String, Any> = hashMapOf("orderUpdate" to "cancelled")
                    db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").document(item.orderId).update(data)
                    scheduledOrders.removeAt(position)
                    notifyItemInserted(position)
                    dialog.dismiss()
                }

                .create()
                .show()
        }



        holder.messages.setOnClickListener {

            val intent = Intent(context, Messages::class.java)
            intent.putExtra("event_type", item.eventType)
            intent.putExtra("location", item.location)
            intent.putExtra("menu_item_id", item.menuItemId)
            intent.putExtra("total_cost_of_event", item.totalCostOfEvent)
            intent.putExtra("event_quantity", item.eventQuantity)
            intent.putExtra("travel_fee_or_message", "Messages")
            intent.putExtra("item_title", item.itemTitle)

            //
            val senderImageId = FirebaseAuth.getInstance().currentUser!!.uid
            val receiverImageId = item.userImageId
            val chefOrUser = FirebaseAuth.getInstance().currentUser!!.displayName
            val documentId = item.orderId
            val senderUserName = guserName
            val receiverUserName = item.userName
            val senderEmail = FirebaseAuth.getInstance().currentUser!!.email!!
            val receiverEmail = item.userEmail
            val receiverChefOrUser = "User"

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

        holder.showDates.setOnClickListener {
            holder.infoLayout.visibility = View.VISIBLE
            holder.itemTitle.isVisible = false
            holder.eventTypeAndQuantity.isVisible = false
            holder.location.isVisible = false
            holder.showNotes.isVisible = false
            holder.showDates.isVisible = false
            holder.infoOkButton.isVisible = true

            if (item.travelFeeOption == "Yes") {
                holder.messagesForTravelFee.isVisible = false
                if (item.eventType == "MealKit Items") {
                    holder.messagesForTravelFee.isVisible = true
                }
            }
            holder.cancel.isVisible = false
            holder.messages.isVisible = false

            if (item.eventDates.size > 1) { holder.infoLabel.text = "Event Dates"
            } else { holder.infoLabel.text = "Event Date" }

            if (item.typeOfService == "Cater Items") {
                for (i in 0 until item.eventDates.size) {
                    if (i == 0) {
                        holder.infoText.text = "${item.eventDates[i]} ${item.eventTimes[i]}"
                    } else {
                        holder.infoText.text =
                            "${holder.infoText.text}, ${item.eventDates[i]} ${item.eventTimes[i]}"
                    }
                }
            } else {
                for (i in 0 until item.eventDates.size) {
                    if (i == 0) {
                        holder.infoText.text = "Dates: ${item.eventDatesForUser[i]}"
                    } else {
                        holder.infoText.text =
                            "${holder.infoText.text}, ${item.eventDatesForUser[i]}"
                    }
                }
            }
        }

        holder.showNotes.setOnClickListener {

            holder.infoYesButton.visibility = View.GONE
            holder.infoOkButton.text = "Ok"
            holder.infoOkButton.setTextColor(ContextCompat.getColor(context, R.color.main))
            holder.infoLayout.visibility = View.VISIBLE
            holder.infoOkButton.isVisible = true
            holder.infoLayout.visibility = View.VISIBLE

            holder.itemTitle.isVisible = false
            holder.eventTypeAndQuantity.isVisible = false
            holder.location.isVisible = false
            holder.showNotes.isVisible = false
            holder.showDates.isVisible = false
            holder.cancel.isVisible = false

            holder.messages.isVisible = false
            holder.infoLabel.text = "Notes to Chef"
            if (item.typeOfService == "Executive Item") {
                holder.infoText.text = "Allergies: ${item.allergies}  | Additional Menu Requests: ${item.additionalMenuItems}  |  Notes: ${item.eventNotes}"
            } else {
                holder.infoText.text = item.eventNotes
            }
            holder.infoOkButton.isVisible = true

        }
        holder.infoYesButton.setOnClickListener {
            Toast.makeText(context, "Great! You will be paid 1 hour after the event. If something happens, please click no.", Toast.LENGTH_LONG).show()
            holder.cancel.isVisible = true
        }
        holder.cancel.setOnClickListener {
            val refund = getPercentage(item, item.eventDates)
            val refund1 = "%.2f".format(refund[1].toDouble())

            Log.d(TAG, "onBindViewHolder: refund $refund")
            AlertDialog.Builder(context)
                .setTitle("Cancel Item.")
                .setMessage("Are you sure you want to cancel? If you cancel, you will owe $$refund1 on your next payout.")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, _ ->

                    db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener { document ->
                        if (document != null) {
                            val data = document.data

                            val previousChargeForPayout = data?.get("chargeForPayout") as Number
                            val data1: Map<String, Any> = hashMapOf("chargeForPayout" to previousChargeForPayout.toDouble() - refund[1].toDouble())
                            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).update(data1)
                        }
                        db.collection("Orders").document(item.orderId).get().addOnSuccessListener { document1 ->
                            if (document1 != null) {
                                val data = document1.data

                                val paymentId = data?.get("paymentIntent") as String
                                refundOrder(paymentId, (item.totalCostOfEvent.toDouble() + item.taxesAndFees.toDouble()).toString(), item.orderId, item.userImageId, item.chefImageId, refund[1].toDouble())

                            }
                        }
                    }
                    val sdfT = SimpleDateFormat("MM-dd-yyyy hh:mm a")
                    val date1 = sdfT.format(Date())
                    val data3: Map<String, Any> = hashMapOf("notification" to "$guserName has just cancelled your item (${item.eventType}) about ${item.itemTitle}. You will receive a full refund.", "date" to date1)
                    val data4: Map<String, Any> = hashMapOf("notifications" to "yes")
                    db.collection("User").document(item.chefImageId).collection("Notifications").document().set(data3)
                    db.collection("User").document(item.chefImageId).update(data4)
                    Toast.makeText(context, "Item cancelled. Please be weary of doing this in the future.", Toast.LENGTH_LONG).show()
                    scheduledOrders.removeAt(position)
                    notifyItemRemoved(position)
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()


        }

        holder.infoOkButton.setOnClickListener {
            if(holder.infoLabel.text == "Notes to Chef" || holder.infoLabel.text == "Event Date" || holder.infoLabel.text == "Event Dates") {
                holder.infoYesButton.visibility = View.GONE
                holder.infoOkButton.text = "Ok"
                holder.infoOkButton.setTextColor(ContextCompat.getColor(context, R.color.main))
                holder.infoLayout.visibility = View.VISIBLE
                holder.infoOkButton.isVisible = false
                holder.infoLayout.visibility = View.GONE
                holder.itemTitle.isVisible = true
                holder.eventTypeAndQuantity.isVisible = true
                holder.location.isVisible = true
                holder.showNotes.isVisible = true
                holder.showDates.isVisible = true
                holder.messages.isVisible = true


                holder.cancel.isVisible = true
            } else if (holder.infoLabel.text == "Event Complete") {
                scheduledOrders.removeAt(position)
                notifyItemRemoved(position)
            } else {
                val intent = Intent(context, ReportAnIssue::class.java)
                context.startActivity(intent)
            }
        }

        if (item.eventType == "MealKit Items") {
            holder.messagesForTravelFee.setOnClickListener {
                val intent = Intent(context, ItemDetail::class.java)
                intent.putExtra("meal_kit_order_id", item.orderId)
                intent.putExtra("receiver_image_id", item.userImageId)
                intent.putExtra("receiver_user_name", item.userName)
                intent.putExtra("meal_kit_item_title", item.itemTitle)
                intent.putExtra("is_meal_kit", "yes")
                context.startActivity(intent)
            }
        } else {
            holder.messagesForTravelFee.isVisible = false
        }
    }

    override fun getItemCount() = scheduledOrders.size

    fun submitList(pendingOrders: MutableList<ScheduledOrders>) {
        this.scheduledOrders = pendingOrders
    }



    private fun refundOrder(paymentId: String, amount: String, orderId: String, userImageId: String, chefImageId: String, chargeForPayout: Double) {

        val body = FormBody.Builder()
            .add("paymentId", paymentId)
            .add("amount", ("%.0f".format(amount.toDouble() * 100)).toString())
            .build()

        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/refund")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    mHandler.post {
                        displayAlert("Error: $e")
                    }
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

                            val data: Map<String, Any> = hashMapOf("refundId" to refundId, "paymentIntent" to paymentId, "orderId" to orderId, "date" to "", "userImageId" to userImageId, "chefImageId" to chefImageId)
                            val data1: Map<String, Any> = hashMapOf("cancelled" to "refunded", "orderUpdate" to "cancelled")
                            val data2: Map<String, Any> = hashMapOf("cancelled" to "cancelled")

                            db.collection("Refunds").document(orderId).set(data)
                            db.collection("Chef").document(chefImageId).collection("Orders").document(orderId).update(data1)
                            db.collection("User").document(userImageId).collection("Orders").document(orderId).update(data2)
                            db.collection("Orders").document(orderId).update(data1)
                        }
                    }
                }
            })


    }


    class ViewHolder(itemView: ChefOrderPostBinding) : RecyclerView.ViewHolder(itemView.root) {

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
        val messages = itemView.messagesButton
        val infoLayout = itemView.infoLayout
        val infoLabel = itemView.infoLabel
        val infoText = itemView.infoText
        val infoOkButton = itemView.infoOkButton
        val infoYesButton = itemView.infoYesButton

        @SuppressLint("SetTextI18n")
        fun bind(item: ScheduledOrders) {
            itemType.text = item.typeOfService


            orderDate.text = "Ordered: ${item.orderDate}"
            itemTitle.text = item.itemTitle
            eventTypeAndQuantity.text = "Event Type: ${item.eventType}     Event Quantity: ${item.eventQuantity}"
            location.text = "Location: ${item.location}"

            messagesForTravelFee.visibility = View.GONE
            messages.text = "Messages"


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


    private fun getPercentage(item: ScheduledOrders, eventDates: MutableList<String>) : MutableList<String> {
        var travelFee = 0.0
        if (item.travelFee != "") { travelFee = item.travelFee.toDouble() }
        val cost = (item.totalCostOfEvent.toDouble() - (item.taxesAndFees.toDouble() / item.numberOfEvents.toDouble())) + travelFee
        var date = ""
        var percentage = 0.0
        var chefOwes = ""
        var eventRefund = ""
        Log.d(TAG, "getPercentage: cost ${cost}")
        Log.d(TAG, "getPercentage: ${item.totalCostOfEvent.toDouble()}")
        Log.d(TAG, "getPercentage: ${item.numberOfEvents.toDouble()} ")
        Log.d(TAG, "getPercentage: ${item.taxesAndFees.toDouble()}")
        for (i in 0 until eventDates.size) {
            if (i == 0) {
                date = eventDates[i]
            } else {
                val time = sdf.parse(date).time
                val day = eventDates[i]
                val dayT = sdf.parse(day).time
                if (dayT < time) {
                    date = day
                }
            }
            //change
            if (i == eventDates.size - 1) {
                val day = sdf.parse(date).time / 1000 / 60 / 60 / 24
                val today = Timestamp(System.currentTimeMillis()).time / 1000 / 60 / 60 / 24

                percentage = if (today - day <= 7) {
                    0.85
                } else {
                    0.95
                }


                Log.d(TAG, "getPercentage: percentage ${today - day}")
                Log.d(TAG, "getPercentage: $day")
                eventRefund = "${(cost + (cost * (1 - (percentage + .05))))}"
                val chefOwesRefund = (cost + (cost * (1 - (percentage))))
                chefOwes = "${chefOwesRefund - cost}"

            }
        }

        return arrayListOf(eventRefund, chefOwes, "$percentage")
    }
}