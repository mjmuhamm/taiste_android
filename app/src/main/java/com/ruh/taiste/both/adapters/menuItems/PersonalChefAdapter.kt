package com.ruh.taiste.both.adapters.menuItems

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.R
import com.ruh.taiste.both.ItemDetail
import com.ruh.taiste.both.ProfileAsUser
import com.ruh.taiste.chef.MenuItemAdd
import com.ruh.taiste.chef.PersonalChefAdd
import com.ruh.taiste.chef.fragments.chefLatitude
import com.ruh.taiste.chef.fragments.chefLongitude
import com.ruh.taiste.databinding.ChefItemPostBinding
import com.ruh.taiste.databinding.PersonalChefPostBinding
import com.ruh.taiste.user.OrderDetails
import com.ruh.taiste.user.PersonalChefOrderDetail
import com.ruh.taiste.user.models.FeedMenuItems
import com.ruh.taiste.user.models.PersonalChefInfo

private const val TAG = "PersonalChefAdapter"
class PersonalChefAdapter(private val context: Context, private var personalChefItems: MutableList<PersonalChefInfo>, private var chefOrUser: String, private var city: String, private var state: String, private var zipCode: String) : RecyclerView.Adapter<PersonalChefAdapter.ViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PersonalChefPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(personalChefItems[position])


        if (chefOrUser == "chef") { holder.editItemButton.visibility = View.VISIBLE
        } else { holder.editItemButton.visibility = View.GONE }


        val item = personalChefItems[position]

        holder.signatureImage.setOnClickListener {
            val intent = Intent(context, ItemDetail::class.java)
            intent.putExtra("type_of_item", "Executive Chef")
            intent.putExtra("chef_image_id", item.chefImageId)
            context.startActivity(intent)
        }

        holder.chefImage.setOnClickListener {
            val intent = Intent(context, ProfileAsUser::class.java)
            intent.putExtra("chef_or_user", "chef")
            intent.putExtra("user" , item.chefImageId)
            context.startActivity(intent)
        }

        holder.orderButton.setOnClickListener {
            if (item.chefName != "chefTest") {
                if (FirebaseAuth.getInstance().currentUser!!.displayName!! != "Chef") {
                    val intent = Intent(context, PersonalChefOrderDetail::class.java)
                    intent.putExtra("personal_chef_info", item)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(
                        context,
                        "Please create a user account to order.",
                        Toast.LENGTH_LONG
                    ).show()

                }
            } else {
                Toast.makeText(context, "This is a test account.", Toast.LENGTH_LONG).show()
            }
        }

        holder.editItemButton.setOnClickListener {
            val intent = Intent(context, PersonalChefAdd::class.java)
            context.startActivity(intent)
        }


        holder.likeButton.setOnClickListener {

        }


    }

    override fun getItemCount() = personalChefItems.size

    fun submitList(personalChefItems: MutableList<PersonalChefInfo>) {
        this.personalChefItems = personalChefItems
    }

    class ViewHolder(itemView: PersonalChefPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        private val context = itemView.root.context
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

            Log.d(TAG, "bind: chefemail ${item.chefEmail} ${item.chefImageId}")
            Log.d(TAG, "bind: chefemail ${item.signatureDishId} ")

            chefRef.child("chefs/${item.chefEmail}/profileImage/${item.chefImageId}.png").downloadUrl.addOnSuccessListener { chefUri ->
                Glide.with(context).load(chefUri).placeholder(R.drawable.default_profile).into(chefImage)
            }
                itemRef.child("chefs/${item.chefEmail}/Executive Items/${item.signatureDishId}0.png").downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(context).load(uri).placeholder(R.drawable.default_image).into(signatureImage)
                }




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