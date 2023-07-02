package com.ruh.taiste.chef.adapters.orders

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.ruh.taiste.R
import com.ruh.taiste.both.Messages
import com.ruh.taiste.both.ReportAnIssue
import com.ruh.taiste.databinding.ChefOrderPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.both.models.CompleteOrders
import okhttp3.*
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "CompleteAdapter"
class CompleteAdapter(private val context: Context, private var completeOrders: MutableList<CompleteOrders>) : RecyclerView.Adapter<CompleteAdapter.ViewHolder>()  {


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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(completeOrders[position])
        val item = completeOrders[position]


        holder.messages.setOnClickListener {
            var date = ""
            val todayMonth = sdfMonth.format(Date())
            val todayDay = sdfDay.format(Date())
            val todayYear = sdfYear.format(Date())

            for(i in 0 until item.eventDates.size) {
                val dateI = item.eventDates[i]
                val year = item.eventDates[i].takeLast(4)
                val day = item.eventDates[i].take(5).takeLast(2)
                val month = item.eventDates[i].take(2)
                if (i == 0) {
                    date = dateI
                } else {
                    if (year != todayYear) {
                        date = dateI
                    } else {
                        if (month > todayMonth) {
                            date = dateI
                        } else {
                            if ((month == todayMonth) && day > todayDay) {
                                date = dateI
                            }
                        }
                    }
                }
            }

            val intent = Intent(context, Messages::class.java)
            intent.putExtra("chefOrUser", "Chef")
            intent.putExtra("user" ,item.user)
            intent.putExtra("event_type", item.eventType)
            intent.putExtra("location", item.location)
            intent.putExtra("order_id", item.orderId)
            intent.putExtra("menu_item_id", item.menuItemId)
            intent.putExtra("total_cost_of_event", item.totalCostOfEvent)
            intent.putExtra("event_quantity", item.eventQuantity)
            intent.putExtra("next_event_date", date)
            intent.putExtra("travel_fee_or_message", "Messages")
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
            }
            holder.cancel.isVisible = false
            holder.messages.isVisible = false

            if (item.eventDates.size > 1) { holder.infoLabel.text = "Event Dates"
            } else { holder.infoLabel.text = "Event Date" }

            for (i in 0 until item.eventDates.size) {
                if (i == 0) {
                    holder.infoText.text = "${item.eventDates[i]} ${item.eventTimes[i]}"
                } else {
                    holder.infoText.text = "${holder.infoText.text}, ${item.eventDates[i]} ${item.eventTimes[i]}"
                }
            }
        }

        holder.showNotes.setOnClickListener {

            holder.infoYesButton.visibility = View.GONE
            holder.infoOkButton.text = "Ok"
            holder.infoOkButton.setTextColor(ContextCompat.getColor(context, R.color.main))
            holder.infoLayout.visibility = View.VISIBLE

            holder.infoLayout.visibility = View.VISIBLE
            holder.infoOkButton.isVisible = true
            holder.itemTitle.isVisible = false
            holder.eventTypeAndQuantity.isVisible = false
            holder.location.isVisible = false
            holder.showNotes.isVisible = false
            holder.showDates.isVisible = false
            if (item.travelFeeOption == "Yes") { holder.messagesForTravelFee.isVisible = false }
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


        holder.infoOkButton.setOnClickListener {
            if(holder.infoLabel.text != "Event") {
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
                if (item.travelFeeOption == "Yes") {
                    holder.messagesForTravelFee.isVisible = true
                }

                holder.cancel.isVisible = true
            } else {
                val intent = Intent(context, ReportAnIssue::class.java)
                context.startActivity(intent)
            }
        }


    }

    override fun getItemCount() = completeOrders.size

    fun submitList(completeOrders: MutableList<CompleteOrders>) {
        this.completeOrders = completeOrders
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
        fun bind(item: CompleteOrders) {

            infoOkButton.isVisible = false
            messages.isEnabled = false
            itemType.text = item.typeOfService
            orderDate.text = "Ordered: ${item.orderDate}"
            itemTitle.text = item.itemTitle
            eventTypeAndQuantity.text = "Event Type: ${item.eventType}     Event Quantity: ${item.eventQuantity}"
            location.text = "Location: ${item.location}"

            messagesForTravelFee.visibility = View.GONE
            messages.text = "Messages"
            cancel.isVisible = false


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