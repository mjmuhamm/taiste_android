package com.ruh.taiste.both.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ruh.taiste.R
import com.ruh.taiste.both.models.Comments
import com.ruh.taiste.both.models.VideoModel
import com.ruh.taiste.chef.chefImage
import com.ruh.taiste.chef.chefImageId
import com.ruh.taiste.chef.chefUsername
import com.ruh.taiste.databinding.ItemVideoBinding
import com.ruh.taiste.user.userImage
import com.ruh.taiste.user.userImageId
import com.ruh.taiste.user.userName
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.both.ProfileAsUser
import com.ruh.taiste.chef.MainActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "VideoAdapter"
class VideoAdapter(private var context: Context, private var exoPlayer: ExoPlayer, private var videos: MutableList<VideoModel>, private var chefOrUser: String, private var chefImageId: String) : RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    private val db = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser!!.email!!
    private val chefOrUserI = FirebaseAuth.getInstance().currentUser!!.displayName

    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    @SuppressLint("SimpleDateFormat")
    private var sdf = SimpleDateFormat("MM-dd-yyyy hh:mm:ss.SSS a")

    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var commentsLabel : TextView
    private lateinit var exitButton : TextView
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var messageText : EditText
    private lateinit var sendMessage: ImageView

    private lateinit var commentsAdapter: CommentsAdapter
    private var comments: MutableList<Comments> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemVideoBinding.inflate(LayoutInflater.from(context), parent, false))
        }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(videos[position])

        val video = videos[position]


            holder.deleteButton.visibility = View.GONE
            holder.backButton.visibility = View.GONE


        bottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(context).inflate(R.layout.comment_bottom_sheet, R.id.comment_bottom_sheet as? RelativeLayout)


        commentsLabel = bottomSheetView.findViewById(R.id.comments_label)
        exitButton = bottomSheetView.findViewById(R.id.exit_button)
        commentsRecyclerView = bottomSheetView.findViewById(R.id.comments_recycler_view)
        messageText = bottomSheetView.findViewById(R.id.message_text)
        sendMessage = bottomSheetView.findViewById(R.id.send_message_button)

        commentsAdapter = CommentsAdapter(context, comments, video.id, messageText, sendMessage)
        commentsRecyclerView.layoutManager = LinearLayoutManager(context)
        commentsRecyclerView.adapter = commentsAdapter

        when (video.comments) {
            0 -> {
                commentsLabel.text = "No Comments"
            }
            1 -> {
                commentsLabel.text = "1 Comment"
            }
            else -> {
                commentsLabel.text = "${video.comments} Comments"
            }
        }
        val docRef = db.collection("Videos").document(video.id)
        sendMessage.setOnClickListener {
            if (messageText.text.isNotEmpty()) {
                val chefOrUser = if (chefOrUserI == "Chef") {
                    "chefs"
                } else {
                    "users"
                }
                val username = if (chefOrUser == "Chef") {
                    userName
                } else {
                    chefUsername
                }

                val imageId = if (chefOrUser == "Chef") {
                    chefImageId
                } else {
                    userImageId
                }

                val image = if (chefOrUser == "Chef") {
                    chefImage
                } else {
                    userImage
                }

                val date = sdf.format(Date())

                val documentId = UUID.randomUUID().toString()

                val data: Map<String, Any> = hashMapOf(
                    "chefOrUser" to chefOrUser,
                    "comment" to messageText.text.toString(),
                    "date" to date,
                    "likes" to ArrayList<String>(),
                    "userImageId" to FirebaseAuth.getInstance().currentUser!!.uid,
                    "username" to username,
                    "userEmail" to FirebaseAuth.getInstance().currentUser!!.email!!
                )

                db.collection("Videos").document(video.id).collection("UserComments")
                    .document(documentId).set(data)
                docRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val data1 = document.data

                        if (data1?.get("comments") != null) {
                            val comments = data1["comments"] as Number
                            val data2: Map<String, Any> =
                                hashMapOf("comments" to comments.toInt() + 1)
                            db.collection("Videos").document(video.id).update(data2)
                        } else {
                            val data2: Map<String, Any> = hashMapOf("comments" to 1)
                            db.collection("Videos").document(video.id).update(data2)
                        }
                    }
                }
                video.comments = video.comments.toInt() + 1
                commentsLabel.text = "${video.comments} Comments"
                val newComment = Comments(
                    chefOrUser,
                    messageText.text.toString(),
                    date,
                    "",
                    0,
                    imageId,
                    arrayListOf(),
                    username,
                    image,
                    documentId
                )
                comments.add(newComment)
                comments.sortBy { it.date }
                commentsAdapter.submitList(comments, video.id)
                messageText.setText("")
                commentsAdapter.notifyItemInserted(comments.size - 1)
                Toast.makeText(context, "Comment Sent", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Please enter a comment first.", Toast.LENGTH_LONG).show()
            }
        }

        bottomSheetDialog.setContentView(bottomSheetView)

        holder.commentButton.setOnClickListener {
            loadComments(video.id)
            bottomSheetDialog.show()
        }

        exitButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        holder.backButton.setOnClickListener {
            var intent = Intent(context, ProfileAsUser::class.java)
            intent.putExtra("chef_content_variable", "yes")
            if (FirebaseAuth.getInstance().currentUser!!.uid == chefImageId) {
                intent = Intent(context, MainActivity::class.java)
                intent.putExtra("where_to", "home")
            }
            intent.putExtra("chef_or_user", "chef")
            intent.putExtra("user" , chefImageId)
            context.startActivity(intent)
        }
        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this video?")
                // if the dialog is cancelable
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, _ ->
                    deleteVideo(video.id)


                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }






        holder.playPauseButton.setOnClickListener {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                holder.playImage.visibility = View.VISIBLE
            } else { exoPlayer.play()
                holder.playImage.visibility = View.GONE }
        }


        holder.likeLayout.setOnClickListener {
            holder.likeLayout.isSelected = !holder.likeLayout.isSelected
            if (holder.likeLayout.isSelected) {
                holder.likeButton.setImageResource(R.drawable.video_heart_filled)
                holder.likeText.text = "${holder.likeText.text.toString().toInt() + 1}"
                val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayUnion(user))
                db.collection("Videos").document(video.id).update(data)
            } else {
                holder.likeButton.setImageResource(R.drawable.video_heart_unfilled)
                holder.likeText.text = "${holder.likeText.text.toString().toInt() - 1}"
                val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayRemove(user))
                db.collection("Videos").document(video.id).update(data)
            }
        }



    }
    private fun deleteVideo(id: String) {
        val intent = Intent(context, MainActivity::class.java)
        val body = FormBody.Builder()
            .add("entryId", id)
            .build()

        val request = Request.Builder()
            .url("https://taiste-video.onrender.com/delete-video")
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

                        mHandler.post {

                        context.startActivity(intent)
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


    private fun loadComments(videoId: String) {
     db.collection("Videos").document(videoId).collection("Comments").get().addOnSuccessListener { documents ->
         if (documents != null) {
             for (doc in documents.documents) {
                 val data = doc.data

                 val chefOrUser = data?.get("chefOrUser") as String
                 val comment = data["comment"] as String
                 val date = data["date"] as String
                 val repliedToUser = data["repliedToUser"] as String
                 val secondComments = data["secondComments"] as Number
                 val userImageId = data["userImageId"] as String
                 val username = data["username"] as String
                 var liked = arrayListOf<String>()
                 val documentId = doc.id

                 if (data["liked"] != null) {
                     liked = data["liked"] as ArrayList<String>
                 }

//                 storage.reference.child("$chefOrUser/$user/profileImage/$userImageId.png").downloadUrl.addOnSuccessListener { userUri ->
//
//                 }

                 val newComment = Comments(chefOrUser, comment, date, repliedToUser, secondComments.toInt(), userImageId, liked, username, chefImage, documentId)

                 if (comments.size == 0) {
                     comments.add(newComment)
                     comments.sortBy { it.date }
                     commentsAdapter.submitList(comments, videoId)
                     commentsAdapter.notifyItemInserted(0)
                 } else {
                     val index = comments.indexOfFirst { it.documentId == doc.id }
                     if (index == -1) {
                         comments.add(newComment)
                         comments.sortBy { it.date }
                         commentsAdapter.submitList(comments, videoId)
                         commentsAdapter.notifyItemInserted(comments.size - 1)
                     }
                 }

             }
         }
     }
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        exoPlayer = ExoPlayer.Builder(context).build()


        holder.exoPlayer.player = exoPlayer
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(videos[holder.absoluteAdapterPosition].dataUri)))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        exoPlayer.play()
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE




    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.exoPlayer.player!!.pause()

    }

    override fun getItemCount() = videos.size

    fun submitList(content: MutableList<VideoModel>, chefImageId: String) {
        this.videos = content
        this.chefImageId = chefImageId

    }

    class ViewHolder(itemView: ItemVideoBinding) : RecyclerView.ViewHolder(itemView.root) {

        var exoPlayer = itemView.exoPlayer
        var playImage = itemView.playImage
        var playPauseButton = itemView.playPauseButton
        val likeButton = itemView.likeButton
        val likeText = itemView.likeText
        var likeLayout = itemView.likeLayout
        val user = itemView.user
        val description = itemView.description
        val commentButton = itemView.commentButton
        val commentText = itemView.commentText
        val shareButton = itemView.shareButton
        val shareText = itemView.shareText
        val backButton = itemView.backButton
        val deleteButton = itemView.delete


        @SuppressLint("SetTextI18n")
        fun bind(video: VideoModel) {

            val db = Firebase.firestore

            user.text = video.user
            description.text = video.description

            likeText.text = "${video.liked.size}"
            commentText.text = "${video.comments}"
            shareText.text = "${video.shared}"

            val data : Map<String, Any> = hashMapOf("views" to video.views.toInt() + 1)
            db.collection("Videos").document(video.id).update(data)



        }
    }
}