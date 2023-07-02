package com.ruh.taiste.both.fragments

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.ruh.taiste.both.adapters.VideoAdapter
import com.ruh.taiste.both.models.VideoModel
import com.ruh.taiste.databinding.FragmentFeedBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


private const val TAG = "Feed"

class Feed : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private var backEndUrl = "https://taiste-video.onrender.com/get-videos"
    private val httpClient = OkHttpClient()


    private val mHandler: Handler = Handler(Looper.getMainLooper())

    private var content: MutableList<VideoModel> = arrayListOf()
    private var chefOrFeed = ""
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var exoPlayer: ExoPlayer


    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)


        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        videoAdapter = VideoAdapter(requireContext(), exoPlayer, content, FirebaseAuth.getInstance().currentUser!!.displayName!!, "")
        binding.viewPager.adapter = videoAdapter

        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(requireContext())
            } else {
                isOnline1(requireContext())
            }
        ) {

        if (FirebaseAuth.getInstance().currentUser != null) {
            loadVideos()

            videoAdapter.submitList(content, "")
            videoAdapter.notifyItemInserted(0)

        } else {
            Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
        }
        } else {
            Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }
        return binding.root
    }


    override fun onPause() {
        super.onPause()
        binding.viewPager.adapter = null
    }

    override fun onStop() {
        super.onStop()
        binding.viewPager.adapter = null
    }

    override fun onDetach() {
        super.onDetach()
        binding.viewPager.adapter = null
    }

    override fun onResume() {
        super.onResume()
        videoAdapter = VideoAdapter(requireContext(), exoPlayer, content, FirebaseAuth.getInstance().currentUser!!.displayName!!, "")
        binding.viewPager.adapter = videoAdapter
    }


    private var createdAt = 0
    private fun loadVideos() {

        Log.d(TAG, "loadVideos: $createdAt")
        val body = FormBody.Builder()
            .add("created_at", createdAt.toString())
            .build()

        val request = Request.Builder()
            .url(backEndUrl)
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (backEndUrl != "http://taiste-video.onrender.com/get-videos") {
                        backEndUrl = "http://taiste-video.onrender.com/get-videos"
                        loadVideos()
                    }
                    mHandler.post {
                        if (context != null) {
                            Toast.makeText(
                                context,
                                "An error has occurred playing video. Please try again later.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        mHandler.post {
                            if (context != null) {
                                Toast.makeText(
                                    requireContext(),
                                    "An error has occurred playing video. Please try again later.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        if (backEndUrl != "http://taiste-video.onrender.com/get-videos") {
                            backEndUrl = "http://taiste-video.onrender.com/get-videos"
                            loadVideos()
                        }
                    } else {
                        val responseData = response.body!!.string()
                        Log.d(TAG, "onResponse: response data $responseData")
                        val responseJson =
                            JSONObject(responseData)

                        val videos = responseJson.getJSONArray("videos")


                        mHandler.post {
                            for (i in 0 until videos.length()) {
                                val id = videos.getJSONObject(i)["id"].toString()
                                val createdAtI = videos.getJSONObject(i)["createdAt"].toString()
                                if (i == videos.length() - 1) {
                                    createdAt = createdAtI.toInt()
                                }
                                var views = 0
                                var liked = arrayListOf<String>()
                                var comments = 0
                                var shared = 0

                                var name = if (videos.getJSONObject(i)["name"].toString() == "malik@cheftesting.com" || videos.getJSONObject(i)["name"].toString() == "malik@cheftesting2.com") {
                                    "@chefTest"
                                } else {
                                    videos.getJSONObject(i)["name"].toString()
                                }

                                if (videos.getJSONObject(i)["name"].toString() != "sample" && videos.getJSONObject(
                                        i
                                    )["name"].toString() != "sample1"
                                ) {

                                    db.collection("Videos").document(id).get()
                                        .addOnSuccessListener { document ->
                                            if (document.exists()) {
                                                val data = document.data

                                                if (data?.get("views") != null) {
                                                    val viewsI = data["views"] as Number
                                                    views = viewsI.toInt()
                                                }
                                                if (data?.get("comments") != null) {
                                                    val commentsI = data["comments"] as Number
                                                    comments = commentsI.toInt()
                                                }

                                                if (data?.get("liked") != null) {
                                                    val likedI = data["liked"] as ArrayList<String>
                                                    liked = likedI
                                                }

                                                if (data?.get("shared") != null) {
                                                    val sharedI = data["shared"] as Number
                                                    shared = sharedI.toInt()
                                                }
                                            }
                                        }


                                    val newVideo = VideoModel(
                                        videos.getJSONObject(i)["dataUrl"].toString(),
                                        id,
                                        createdAtI,
                                        name,
                                        videos.getJSONObject(i)["description"].toString(),
                                        views,
                                        liked,
                                        comments,
                                        shared,
                                        videos.getJSONObject(i)["thumbnailUrl"].toString()
                                    )

                                    if (content.isEmpty()) {
                                        content.add(newVideo)
                                        content.shuffle()
                                        videoAdapter.submitList(content, "")
                                        videoAdapter.notifyItemInserted(0)
                                    } else {
                                        val index = content.indexOfFirst { it.id == id }
                                        if (index == -1) {
                                            content.add(newVideo)
                                            content.shuffle()
                                            videoAdapter.submitList(content, "")
                                            videoAdapter.notifyItemInserted(content.size - 1)
                                        }
                                    }
                                }

                                // Set up PaymentConfiguration with your Stripe publishable key
                            }
                        }
                    }
                }
            })
    }


 }
