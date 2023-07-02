package com.ruh.taiste.user.adapters.checkout

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.R
import com.ruh.taiste.both.ProfileAsUser
import com.ruh.taiste.chef.chefImage
import com.ruh.taiste.databinding.CheckoutPostBinding
import com.ruh.taiste.user.ClickListener
import com.ruh.taiste.user.OrderDetails
import com.ruh.taiste.user.models.CheckoutItems

private const val TAG = "CheckoutAdapter"
class CheckoutAdapter(private val context: Context, private var checkoutItems: MutableList<CheckoutItems>, private var listener: ClickListener) : RecyclerView.Adapter<CheckoutAdapter.ViewHolder>()  {

    val db = Firebase.firestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(CheckoutPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(checkoutItems[position])
        val item = checkoutItems[position]

        holder.cancelButton.setOnClickListener {
            listener.updateCost(item.totalCostOfEvent.toDouble(), item.documentId)
            db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Cart").document(item.documentId).delete()
            checkoutItems.removeAt(position)
            notifyItemRemoved(position)
        }

        holder.chefImage.setOnClickListener {
            val intent = Intent(context, ProfileAsUser::class.java)
            intent.putExtra("chef_or_user", "chef")
            intent.putExtra("user" , item.chefImageId)
            context.startActivity(intent)
        }

        holder.detailButton.setOnClickListener {
            val intent = Intent(context, OrderDetails::class.java)
            intent.putExtra("checkout_item", item)
            context.startActivity(intent)
        }


    }

    override fun getItemCount() = checkoutItems.size

    fun submitList(checkoutItems: MutableList<CheckoutItems>) {
        this.checkoutItems = checkoutItems
    }

    class ViewHolder(itemView: CheckoutPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        private val context = itemView.root.context
        val chefImage = itemView.chefImage
        private val costOfEvent = itemView.costOfEvent
        private val itemTitle = itemView.itemTitle
        private val eventTypeAndQuantity = itemView.eventTypeAndQuantity
        private val dates = itemView.dates
        private val notes = itemView.notes
        val allergies = itemView.allergies
        val additionalMenuItems = itemView.additionalMenuItemsRequest
        private val location = itemView.location
        val cancelButton = itemView.cancelButton
        val detailButton = itemView.detailButton

        @SuppressLint("SetTextI18n")
        fun bind(item: CheckoutItems) {
            val storage = Firebase.storage

            storage.reference.child("chefs/${item.chefEmail}/profileImage/${item.chefImageId}.png").downloadUrl.addOnSuccessListener { chefUri ->
                Glide.with(context).load(chefUri).placeholder(R.drawable.default_profile).into(chefImage)
            }


            Log.d(TAG, "bind: lkjasdf ${item.typeOfService}")
            if (item.typeOfService == "Executive Items") {
                allergies.text = "Allergies: ${item.allergies}"
                additionalMenuItems.text = "Additional Item Requests: ${item.additionalMenuItems}"
                allergies.visibility = View.VISIBLE
                additionalMenuItems.visibility = View.VISIBLE
            }
            val cost = "%.2f".format(item.totalCostOfEvent)
            costOfEvent.text = "$$cost"
            itemTitle.text = item.itemTitle
            eventTypeAndQuantity.text = "Event Type: ${item.typeOfEvent}     Event Quantity: ${item.quantityOfEvent}"
            location.text = item.location

            for (i in 0 until item.datesOfEvent.size) {
                if (i < 3) {
                    if (i == 0) {
                        dates.text = "Dates: ${item.datesOfEvent[0]}"
                    } else {
                        if (i <= 3) {
                            dates.text = "${dates.text}, ${item.datesOfEvent[i]}"
                        } else {
                            dates.text = "${dates.text}..."
                        }

                    }
                }
            }

            if (item.notesToChef.length > 30) {
                notes.text = "${item.notesToChef.take(30)}..."
            } else {
                notes.text = item.notesToChef
            }


        }

    }
}