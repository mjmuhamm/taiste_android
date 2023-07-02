package com.ruh.taiste.chef.adapters.orders

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
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.ruh.taiste.both.Messages
import com.ruh.taiste.databinding.ChefOrderPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.R
import com.ruh.taiste.both.guserName
import com.ruh.taiste.both.models.PendingOrders
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PendingAdapter(private val context: Context, private var pendingOrders: MutableList<PendingOrders>) : RecyclerView.Adapter<PendingAdapter.ViewHolder>()  {

    private val db = Firebase.firestore
    private val chef = FirebaseAuth.getInstance().currentUser!!.email!!

    @SuppressLint("SimpleDateFormat")
    private var sdfMonth = SimpleDateFormat("MM")

    @SuppressLint("SimpleDateFormat")
    private var sdfDay = SimpleDateFormat("dd")

    @SuppressLint("SimpleDateFormat")
    private var sdfYear = SimpleDateFormat("yyyy")

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("MM-dd-yyyy")

    @SuppressLint("SimpleDateFormat")
    private var sdfYearMonth = SimpleDateFormat("yyyy, MM")


    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ChefOrderPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pendingOrders[position])
        val item = pendingOrders[position]

        if (item.cancelled != "") {
            holder.infoLayout.visibility = View.VISIBLE
            holder.itemTitle.isVisible = false
            holder.eventTypeAndQuantity.isVisible = false
            holder.location.isVisible = false
            holder.showNotes.isVisible = false
            holder.showDates.isVisible = false
            holder.infoLabel.text = "Event Cancelled"
            holder.infoText.text = "The user has cancelled this event."
            holder.infoLabel.setTextColor(ContextCompat.getColor(context, com.aminography.primedatepicker.R.color.red400))
            holder.infoText.setTextColor(ContextCompat.getColor(context, com.aminography.primedatepicker.R.color.red400))
            holder.infoOkButton.setTextColor(ContextCompat.getColor(context, com.aminography.primedatepicker.R.color.red400))

            val data : Map<String, Any> = hashMapOf("orderUpdate" to "cancelled")
            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").document(item.orderId).update(data)
            if (item.travelFeeOption == "Yes") {
                holder.messagesForTravelFee.isVisible = false
            }
            holder.cancel.isVisible = false
            holder.accept.isVisible = false


        }

        holder.showDates.setOnClickListener {
            holder.infoLayout.visibility = View.VISIBLE
            holder.itemTitle.isVisible = false
            holder.eventTypeAndQuantity.isVisible = false
            holder.location.isVisible = false
            holder.showNotes.isVisible = false
            holder.showDates.isVisible = false
            holder.infoLabel.setTextColor(ContextCompat.getColor(context, R.color.main))
            holder.infoText.setTextColor(ContextCompat.getColor(context, R.color.main))
            holder.infoOkButton.setTextColor(ContextCompat.getColor(context, R.color.main))

            if (item.travelFeeOption == "Yes") {
                holder.messagesForTravelFee.isVisible = false
            }
            holder.cancel.isVisible = false
            holder.accept.isVisible = false

            if (item.eventDates.size > 1) { holder.infoLabel.text = "Event Dates"
            } else { holder.infoLabel.text = "Event Date" }

            if (item.typeOfService == "Executive Items") {
                for (i in 0 until item.eventDates.size) {
                    if (i == 0) {
                        holder.infoText.text = "Dates: ${item.eventDatesForUser[i]}"
                    } else {
                        holder.infoText.text =
                            "${holder.infoText.text}, ${item.eventDatesForUser[i]}"
                    }
                }

            } else {
                for (i in 0 until item.eventDates.size) {
                    if (i == 0) {
                        holder.infoText.text = "Dates: ${item.eventDates[i]} ${item.eventTimes[i]}"
                    } else {
                        holder.infoText.text =
                            "${holder.infoText.text}, ${item.eventDates[i]} ${item.eventTimes[i]}"
                    }
                }
            }
            holder.infoOkButton.isVisible = true
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

            holder.infoLabel.setTextColor(ContextCompat.getColor(context, R.color.main))
            holder.infoText.setTextColor(ContextCompat.getColor(context, R.color.main))
            holder.infoOkButton.setTextColor(ContextCompat.getColor(context, R.color.main))
            holder.infoOkButton.isVisible = true

        }

        holder.infoOkButton.setOnClickListener {
            if (holder.infoLabel.text == "Event Cancelled") {
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
            }
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


        holder.cancel.setOnClickListener {

            AlertDialog.Builder(context)
                .setTitle("Cancel Item.")
                .setMessage("Are you sure you want to remove this order?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, _ ->
                    val data: Map<String, Any> = hashMapOf("cancelled" to "cancelled")

                    db.collection("Orders").get().addOnSuccessListener { documents ->
                        if (documents != null) {
                            for (doc in documents.documents) {
                                val data = doc.data

                                val paymentId = data?.get("paymentIntent") as String

                                refundOrder(paymentId, (item.totalCostOfEvent.toDouble() + item.taxesAndFees.toDouble()).toString(), item.orderId, item.userImageId, item.chefImageId, 0.0)
                                db.collection("User").document(item.userImageId).collection("Orders").document(item.orderId).update(data)
                                db.collection("Orders").document(item.orderId).update(data)
                                val sdfT = SimpleDateFormat("MM-dd-yyyy hh:mm a")
                                val date1 = sdfT.format(Date())
                                val data3: Map<String, Any> = hashMapOf("notification" to "$guserName has just cancelled your item (${item.eventType}) about ${item.itemTitle}. You will receive a full refund.", "date" to date1)
                                val data4: Map<String, Any> = hashMapOf("notifications" to "yes")
                                db.collection("User").document(item.chefImageId).collection("Notifications").document().set(data3)
                                db.collection("User").document(item.chefImageId).update(data4)
                            }
                        }
                    }
                    Toast.makeText(context, "Item cancelled", Toast.LENGTH_LONG).show()
                    pendingOrders.removeAt(position)
                    notifyItemInserted(position)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()


        }


        holder.accept.setOnClickListener {
            val week = if (Calendar.getInstance()[Calendar.WEEK_OF_MONTH] > 4) {
                4 } else { Calendar.getInstance()[Calendar.WEEK_OF_MONTH] }

            val yearMonth = sdfYearMonth.format(Date())
            val weekOfMonth = "Week $week"
            val day = sdf.format(Date())


            val data : Map<String, Any> = hashMapOf("orderUpdate" to "scheduled")
            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").document(item.orderId).update(data)
            db.collection("User").document(item.userImageId).collection("Orders").document(item.orderId).update(data)
            db.collection("Orders").document(item.orderId).update(data)

            val data1: Map<String, Any> = hashMapOf("user" to item.user, "date" to sdf.format(Date()), "itemTitle" to item.itemTitle, "orderDate" to item.orderDate, "totalCostOfEvent" to item.totalCostOfEvent, "travelFee" to item.travelFee)

            val docRef = db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Dashboard").document(item.typeOfService)
            val docRef1 = db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Dashboard").document(item.typeOfService).collection(item.menuItemId).document("Total")
            val docRef2 = db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Dashboard").document(item.typeOfService).collection(item.menuItemId).document("Month").collection(yearMonth).document("Total")
            val docRef3 = db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Dashboard").document(item.typeOfService).collection(item.menuItemId).document("Month").collection(yearMonth).document("Week").collection(weekOfMonth).document("Total")
            val docRef4 = db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Dashboard").document(item.typeOfService).collection(item.menuItemId).document("Month").collection(yearMonth).document("Week").collection(weekOfMonth).document()


            docRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val data2 = document.data

                    val totalPay = data2?.get("totalPay") as Number
                    val info : Map<String, Any> = hashMapOf("totalPay" to totalPay.toDouble() + item.totalCostOfEvent.toDouble())
                    docRef.update(info)
                } else {
                    val info : Map<String, Any> = hashMapOf("totalPay" to item.totalCostOfEvent.toDouble())
                    docRef.set(info)
                }
            }

            docRef1.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val data2 = document.data

                    val totalPay = data2?.get("totalPay") as Number
                    val info : Map<String, Any> = hashMapOf("totalPay" to totalPay.toDouble() + item.totalCostOfEvent.toDouble())
                    docRef1.update(info)
                } else {
                    val info : Map<String, Any> = hashMapOf("totalPay" to item.totalCostOfEvent.toDouble())
                    docRef1.set(info)
                }
            }

            docRef2.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val data2 = document.data

                    val totalPay = data2?.get("totalPay") as Number
                    val info : Map<String, Any> = hashMapOf("totalPay" to totalPay.toDouble() + item.totalCostOfEvent.toDouble())
                    docRef2.update(info)
                } else {
                    val info : Map<String, Any> = hashMapOf("totalPay" to item.totalCostOfEvent.toDouble())
                    docRef2.set(info)
                }
            }

            docRef3.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val data2 = document.data

                    val totalPay = data2?.get("totalPay") as Number
                    val info : Map<String, Any> = hashMapOf("totalPay" to totalPay.toDouble() + item.totalCostOfEvent.toDouble())
                    docRef3.update(info)
                } else {
                    val info : Map<String, Any> = hashMapOf("totalPay" to item.totalCostOfEvent.toDouble())
                    docRef3.set(info)
                }
            }
            val info : Map<String, Any> = hashMapOf("totalPay" to item.totalCostOfEvent.toDouble())
            docRef4.set(info)



            pendingOrders.removeAt(position)
            notifyItemRemoved(position)

            Toast.makeText(context, "Item Accepted. You can now communicate with ${item.user} about the event.", Toast.LENGTH_LONG).show()

        }

    }

    override fun getItemCount() = pendingOrders.size

    fun submitList(pendingOrders: MutableList<PendingOrders>) {
        this.pendingOrders = pendingOrders
    }

    private fun refundOrder(paymentId: String, amount: String, orderId: String, userImageId: String, chefImageId: String, chargeForPayout: Double) {

        val body = FormBody.Builder()
            .add("paymentId", paymentId)
            .add("amount", (amount.toDouble() * 100).toString())
            .build()

        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/refund")
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
                        val refundId = json.getString("refund_id")


                        mHandler.post {

                            val data: Map<String, Any> = hashMapOf("refundId" to refundId, "paymentIntent" to paymentId, "orderId" to orderId, "date" to "", "userImageId" to userImageId, "chefImageId" to chefImageId)
                            val data1: Map<String, Any> = hashMapOf("cancelled" to "refunded", "orderUpdate" to "cancelled")

                            db.collection("Refunds").document(orderId).set(data)
                            db.collection("Chef").document(chefImageId).collection("Orders").document(orderId).update(data1)
                            db.collection("Orders").document(orderId).update(data1)
                        }
                    }
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
        val accept = itemView.messagesButton
        val infoLayout = itemView.infoLayout
        val infoLabel = itemView.infoLabel
        val infoText = itemView.infoText
        val infoOkButton = itemView.infoOkButton

        @SuppressLint("SetTextI18n")
        fun bind(item: PendingOrders) {

            itemType.text = item.typeOfService
            orderDate.text = "Ordered: ${item.orderDate}"
            itemTitle.text = item.itemTitle
            eventTypeAndQuantity.text = "Event Type: ${item.eventType}     Event Quantity: ${item.eventQuantity}"
            location.text = "Location: ${item.location}"
            infoOkButton.isVisible = false

            if (item.travelFeeOption == "Yes") { messagesForTravelFee.visibility = View.VISIBLE
            } else { messagesForTravelFee.visibility = View.GONE }
            accept.text = "Accept"




        }

    }
}