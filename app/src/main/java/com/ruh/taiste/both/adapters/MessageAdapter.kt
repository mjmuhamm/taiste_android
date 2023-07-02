package com.ruh.taiste.both.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.R
import com.ruh.taiste.both.models.Messages
import com.ruh.taiste.chef.MainActivity
import com.ruh.taiste.databinding.MessagePostBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MessageAdapter(private val context: Context, private var messages: MutableList<Messages>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>()  {


    private val httpClient = OkHttpClient()
    private val db = Firebase.firestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(MessagePostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = messages[position]

        if (item.homeOrAway == "home") {
            holder.homeLayout.visibility = View.VISIBLE
            holder.awayLayout.visibility = View.GONE
            holder.systemMessage.visibility = View.GONE
            Glide.with(context).load(item.userImage).placeholder(R.drawable.default_profile).into(holder.homeUserImage)
            holder.homeMessage.text = item.message
            holder.homeDate.text = item.date
        } else if (item.homeOrAway == "away") {
            holder.homeLayout.visibility = View.GONE
            holder.awayLayout.visibility = View.VISIBLE
            holder.systemMessage.visibility = View.GONE
            Glide.with(context).load(item.userImage).placeholder(R.drawable.default_profile).into(holder.awayUserImage)
            holder.awayMessage.text = item.message
            holder.awayDate.text = item.date
        } else {
            holder.homeLayout.visibility = View.GONE
            holder.awayLayout.visibility = View.GONE
            holder.systemMessage.visibility = View.VISIBLE
            holder.systemMessage.text = item.message

            if (item.documentId == "payment") {

                Toast.makeText(context, "Payment Received.", Toast.LENGTH_LONG).show()


                if (FirebaseAuth.getInstance().currentUser!!.displayName!! == "Chef") {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                } else {
                    val intent = Intent(context, com.ruh.taiste.user.MainActivity::class.java)
                    context.startActivity(intent)
                }
            }

//            if (item.travelFee != "") {
//                holder.systemMessage.text = "A travel fee of $${item.message.takeLast(item.message.length - 9)} has been requested."
//            } else if (item.message.take(4) == "paid") {
//                holder.systemMessage.text = "Travel Fee Paid."
//            }



        }

    }

    override fun getItemCount() = messages.size

    private fun sendMessage(title: String, notification: String, topic: String) {

        val body = FormBody.Builder()
            .add("title", title)
            .add("notification", notification)
            .add("topic", topic)
            .build()

        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/send-message")
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
                        val responseJson =
                            JSONObject(responseData)



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

    fun submitList(messages: MutableList<Messages>) {
        this.messages = messages
    }

    class ViewHolder(itemView: MessagePostBinding) : RecyclerView.ViewHolder(itemView.root) {

        val context = itemView.root.context!!
        val homeLayout = itemView.homeLayout
        val homeUserImage = itemView.homeUserImage
        val homeMessage = itemView.homeMessage
        val homeDate = itemView.homeDate

        val awayLayout = itemView.awayLayout
        val awayUserImage = itemView.awayUserImage
        val awayMessage = itemView.awayMessage
        val awayDate = itemView.awayDate

        val systemMessage = itemView.systemMessage

    }
}