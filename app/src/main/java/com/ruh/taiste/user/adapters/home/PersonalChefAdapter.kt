package com.ruh.taiste.user.adapters.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseError
import com.ruh.taiste.R
import com.ruh.taiste.both.ItemDetail
import com.ruh.taiste.both.ProfileAsUser
import com.ruh.taiste.databinding.MenuFeedPostBinding
import com.ruh.taiste.user.OrderDetails
import com.ruh.taiste.user.models.FeedMenuItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.both.guserName
import com.ruh.taiste.databinding.PersonalChefPostBinding
import com.ruh.taiste.user.PersonalChefOrderDetail
import com.ruh.taiste.user.models.PersonalChefInfo
import java.text.SimpleDateFormat
import java.util.*

class PersonalChefAdapter(private val context: Context, private var personalChefItems: MutableList<PersonalChefInfo>) : RecyclerView.Adapter<PersonalChefAdapter.ViewHolder>()  {

    private val db = Firebase.firestore


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PersonalChefPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(personalChefItems[position])


        val item = personalChefItems[position]



            holder.signatureImage.setOnClickListener {
            val intent = Intent(context, ItemDetail::class.java)
            intent.putExtra("type_of_item", "Executive Chef")
            intent.putExtra("chef_image_id", item.chefImageId)
            context.startActivity(intent)
        }

        holder.orderButton.setOnClickListener {
            if (item.chefName == "chefTest") {
                Toast.makeText(context, "This is a test profile. You will not be able to order this item.", Toast.LENGTH_LONG).show()
                val intent = Intent(context, PersonalChefOrderDetail::class.java)
                intent.putExtra("personal_chef_info", item)
                context.startActivity(intent)
            } else {
                    val intent = Intent(context, PersonalChefOrderDetail::class.java)
                    intent.putExtra("personal_chef_info", item)
                    context.startActivity(intent)


            }
        }

        holder.chefImage.setOnClickListener {
            val intent = Intent(context, ProfileAsUser::class.java)
            intent.putExtra("chef_or_user", "chef")
            intent.putExtra("user" , item.chefImageId)
            context.startActivity(intent)
        }

        holder.likeButton.setOnClickListener {
            holder.likeButton.isSelected = !holder.likeButton.isSelected
            if (holder.likeButton.isSelected) {
                item.liked.add(FirebaseAuth.getInstance().currentUser!!.email!!)
                holder.likeImage.setImageResource(R.drawable.heart_filled)
                holder.likeText.text = "${holder.likeText.text.toString().toInt() + 1}"
                val data1: Map<String, Any> = hashMapOf("chefEmail" to item.chefEmail, "chefImageId" to item.chefImageId, "imageCount" to 0, "itemDescription" to item.briefIntroduction, "liked" to item.liked, "itemOrders" to item.itemOrders, "itemRating" to item.itemRating, "itemPrice" to item.servicePrice, "itemTitle" to "Executive Chef", "itemType" to "Executive Items", "itemCalories" to "0", "expectations" to 0, "chefRating" to 0, "quality" to 0, "city" to item.city, "state" to item.state, "chefPassion" to item.briefIntroduction)
                db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("UserLikes").document(item.documentId).set(data1)
                val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayUnion(FirebaseAuth.getInstance().currentUser!!.email!!))
                db.collection("Executive Items").document(item.documentId).update(data)
                val sdfT = SimpleDateFormat("MM-dd-yyyy hh:mm a")
                val date1 = sdfT.format(Date())
                val data3: Map<String, Any> = hashMapOf("notification" to "$guserName has just liked your item (Executive Chef)", "date" to date1)
                val data4: Map<String, Any> = hashMapOf("notifications" to "yes")
                db.collection("Chef").document(item.chefImageId).collection("Notifications").document().set(data3)
                db.collection("Chef").document(item.chefImageId).update(data4)
            } else {
                val index = item.liked.indexOfFirst { it == FirebaseAuth.getInstance().currentUser!!.email!! }
                if (index != -1) {
                    item.liked.removeAt(index)
                }
                holder.likeImage.setImageResource(R.drawable.heart_unfilled)
                holder.likeText.text = "${holder.likeText.text.toString().toInt() - 1}"
                db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("UserLikes").document(item.documentId).delete()
                val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayRemove(FirebaseAuth.getInstance().currentUser!!.email!!))
                db.collection("Executive Items").document(item.documentId).update(data)
            }

        }


    }

    override fun getItemCount() = personalChefItems.size

    fun submitList(personalChefItems: MutableList<PersonalChefInfo>) {
        this.personalChefItems = personalChefItems
    }

    class ViewHolder(itemView: PersonalChefPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        private val context = itemView.root.context
        val chefName = itemView.chefName
        val chefImage = itemView.chefImage
        val signatureImage = itemView.signatureImage
        private val briefIntroduction = itemView.briefIntroduction
        val expectation1 = itemView.expectations1
        val expectation2 = itemView.expectations2
        val expectation3 = itemView.expectations3
        val expectation4 = itemView.expectations4
        val expectation5 = itemView.expectations5
        val chefRating1 = itemView.chefRating1
        val chefRating2 = itemView.chefRating2
        val chefRating3 = itemView.chefRating3
        val chefRating4 = itemView.chefRating4
        val chefRating5 = itemView.chefRating5
        val quality1 = itemView.quality1
        val quality2 = itemView.quality2
        val quality3 = itemView.quality3
        val quality4 = itemView.quality4
        val quality5 = itemView.quality5
        val likeButton = itemView.likeButton
        val likeImage = itemView.likeImage
        val likeText = itemView.likeText
        private val orderText = itemView.orderText
        private val ratingText = itemView.ratingText
        private val itemPrice = itemView.itemPrice
        val orderButton = itemView.orderButton
        val editItemButton = itemView.editPersonalChef

        @SuppressLint("SetTextI18n")
        fun bind(item: PersonalChefInfo) {
            val storage = Firebase.storage

            val chefRef = storage.reference
            val itemRef = storage.reference

            chefRef.child("chefs/${item.chefEmail}/profileImage/${item.chefImageId}.png").downloadUrl.addOnSuccessListener { chefUri ->
                Glide.with(context).load(chefUri).placeholder(R.drawable.default_profile).into(chefImage)
            }

            itemRef.child("chefs/${item.chefEmail}/Executive Items/${item.signatureDishId}0.png").downloadUrl.addOnSuccessListener { uri ->
                Glide.with(context).load(uri).placeholder(R.drawable.default_image).into(signatureImage)
            }



            chefName.text = "@${item.chefName}"
            briefIntroduction.text = item.briefIntroduction
            itemPrice.text = "$${item.servicePrice}"
            likeText.text = "${item.liked.size}"
            orderText.text = "${item.itemOrders}"
            var num = 0.0
            for (i in 0 until item.itemRating.size) {
                num += item.itemRating[i].toDouble()
                if (i == item.itemRating.size - 1) {
                    num = (num / item.itemRating.size)
                }
            }
            when {
                item.expectations > 4 -> {
                    expectation1.setImageResource(R.drawable.star_filled)
                    expectation2.setImageResource(R.drawable.star_filled)
                    expectation3.setImageResource(R.drawable.star_filled)
                    expectation4.setImageResource(R.drawable.star_filled)
                    expectation5.setImageResource(R.drawable.star_filled)
                }
                item.expectations > 3 -> {
                    expectation1.setImageResource(R.drawable.star_filled)
                    expectation2.setImageResource(R.drawable.star_filled)
                    expectation3.setImageResource(R.drawable.star_filled)
                    expectation4.setImageResource(R.drawable.star_filled)
                    expectation5.setImageResource(R.drawable.star_unfilled)
                }
                item.expectations > 2 -> {
                    expectation1.setImageResource(R.drawable.star_filled)
                    expectation2.setImageResource(R.drawable.star_filled)
                    expectation3.setImageResource(R.drawable.star_filled)
                    expectation4.setImageResource(R.drawable.star_unfilled)
                    expectation5.setImageResource(R.drawable.star_unfilled)
                }
                item.expectations > 1 -> {
                    expectation1.setImageResource(R.drawable.star_filled)
                    expectation2.setImageResource(R.drawable.star_filled)
                    expectation3.setImageResource(R.drawable.star_unfilled)
                    expectation4.setImageResource(R.drawable.star_unfilled)
                    expectation5.setImageResource(R.drawable.star_unfilled)
                }
                item.expectations > 0 -> {
                    expectation1.setImageResource(R.drawable.star_filled)
                    expectation2.setImageResource(R.drawable.star_unfilled)
                    expectation3.setImageResource(R.drawable.star_unfilled)
                    expectation4.setImageResource(R.drawable.star_unfilled)
                    expectation5.setImageResource(R.drawable.star_unfilled)
                }
                else -> {
                    expectation1.setImageResource(R.drawable.star_unfilled)
                    expectation2.setImageResource(R.drawable.star_unfilled)
                    expectation3.setImageResource(R.drawable.star_unfilled)
                    expectation4.setImageResource(R.drawable.star_unfilled)
                    expectation5.setImageResource(R.drawable.star_unfilled)
                }
            }

            when {
                item.chefRating > 4 -> {
                    chefRating1.setImageResource(R.drawable.star_filled)
                    chefRating2.setImageResource(R.drawable.star_filled)
                    chefRating3.setImageResource(R.drawable.star_filled)
                    chefRating4.setImageResource(R.drawable.star_filled)
                    chefRating5.setImageResource(R.drawable.star_filled)
                }
                item.chefRating > 3 -> {
                    chefRating1.setImageResource(R.drawable.star_filled)
                    chefRating2.setImageResource(R.drawable.star_filled)
                    chefRating3.setImageResource(R.drawable.star_filled)
                    chefRating4.setImageResource(R.drawable.star_filled)
                    chefRating5.setImageResource(R.drawable.star_unfilled)
                }
                item.chefRating > 2 -> {
                    chefRating1.setImageResource(R.drawable.star_filled)
                    chefRating2.setImageResource(R.drawable.star_filled)
                    chefRating3.setImageResource(R.drawable.star_filled)
                    chefRating4.setImageResource(R.drawable.star_unfilled)
                    chefRating5.setImageResource(R.drawable.star_unfilled)
                }
                item.chefRating > 1 -> {
                    chefRating1.setImageResource(R.drawable.star_filled)
                    chefRating2.setImageResource(R.drawable.star_filled)
                    chefRating3.setImageResource(R.drawable.star_unfilled)
                    chefRating4.setImageResource(R.drawable.star_unfilled)
                    chefRating5.setImageResource(R.drawable.star_unfilled)
                }
                item.chefRating > 0 -> {
                    chefRating1.setImageResource(R.drawable.star_filled)
                    chefRating2.setImageResource(R.drawable.star_unfilled)
                    chefRating3.setImageResource(R.drawable.star_unfilled)
                    chefRating4.setImageResource(R.drawable.star_unfilled)
                    chefRating5.setImageResource(R.drawable.star_unfilled)
                }
                else -> {
                    chefRating1.setImageResource(R.drawable.star_unfilled)
                    chefRating2.setImageResource(R.drawable.star_unfilled)
                    chefRating3.setImageResource(R.drawable.star_unfilled)
                    chefRating4.setImageResource(R.drawable.star_unfilled)
                    chefRating5.setImageResource(R.drawable.star_unfilled)
                }
            }
            when {
                item.quality > 4 -> {
                    quality1.setImageResource(R.drawable.star_filled)
                    quality2.setImageResource(R.drawable.star_filled)
                    quality3.setImageResource(R.drawable.star_filled)
                    quality4.setImageResource(R.drawable.star_filled)
                    quality5.setImageResource(R.drawable.star_filled)
                }
                item.quality > 3 -> {
                    quality1.setImageResource(R.drawable.star_filled)
                    quality2.setImageResource(R.drawable.star_filled)
                    quality3.setImageResource(R.drawable.star_filled)
                    quality4.setImageResource(R.drawable.star_filled)
                    quality5.setImageResource(R.drawable.star_unfilled)
                }
                item.quality > 2 -> {
                    quality1.setImageResource(R.drawable.star_filled)
                    quality2.setImageResource(R.drawable.star_filled)
                    quality3.setImageResource(R.drawable.star_filled)
                    quality4.setImageResource(R.drawable.star_unfilled)
                    quality5.setImageResource(R.drawable.star_unfilled)
                }
                item.quality > 1 -> {
                    quality1.setImageResource(R.drawable.star_filled)
                    quality2.setImageResource(R.drawable.star_filled)
                    quality3.setImageResource(R.drawable.star_unfilled)
                    quality4.setImageResource(R.drawable.star_unfilled)
                    quality5.setImageResource(R.drawable.star_unfilled)
                }
                item.quality > 0 -> {
                    quality1.setImageResource(R.drawable.star_filled)
                    quality2.setImageResource(R.drawable.star_unfilled)
                    quality3.setImageResource(R.drawable.star_unfilled)
                    quality4.setImageResource(R.drawable.star_unfilled)
                    quality5.setImageResource(R.drawable.star_unfilled)
                }
                else -> {
                    quality1.setImageResource(R.drawable.star_unfilled)
                    quality2.setImageResource(R.drawable.star_unfilled)
                    quality3.setImageResource(R.drawable.star_unfilled)
                    quality4.setImageResource(R.drawable.star_unfilled)
                    quality5.setImageResource(R.drawable.star_unfilled)
                }
            }
            val index = item.liked.indexOfFirst { it == FirebaseAuth.getInstance().currentUser!!.email!! }
            if (index != -1) {
                likeImage.setImageResource(R.drawable.heart_filled)
            }
            ratingText.text = "$num"



        }

    }
}