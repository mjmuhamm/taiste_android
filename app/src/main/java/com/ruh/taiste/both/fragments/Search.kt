package com.ruh.taiste.both.fragments

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.both.adapters.SearchAdapter
import com.ruh.taiste.databinding.FragmentSearchBinding
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import com.ruh.taiste.user.userImage
import com.ruh.taiste.user.userImageId
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val TAG = "Search"
class Search : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding : FragmentSearchBinding? = null

    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchView: SearchView

    private var searchItems: MutableList<com.ruh.taiste.both.models.Search> = ArrayList()

    private val binding get() = _binding!!

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
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        searchAdapter = SearchAdapter(requireContext(), searchItems)
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = searchAdapter
        
        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }

        })
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(requireContext())
            } else {
                isOnline1(requireContext())
            }
        ) {

        if (FirebaseAuth.getInstance().currentUser != null) {
            loadUsers()
        } else {
            Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
        }} else {
            Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Search().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun filterList(query: String?) {
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(requireContext())
            } else {
                isOnline1(requireContext())
            }
        ) {

        if (query != null) {
            val filteredList = ArrayList<com.ruh.taiste.both.models.Search>()
            Log.d(TAG, "filterList: search items count ${searchItems.size}")
            for (i in searchItems) {
                if (i.userName.lowercase(Locale.ROOT).contains(query) || i.userFullName.lowercase(Locale.ROOT).contains(query)) {
                    filteredList.add(i)
                }

                if (filteredList.isEmpty()) {
                    Toast.makeText(requireContext(), "No users with those initials.", Toast.LENGTH_SHORT).show()
                } else {
                    searchAdapter.submitList(filteredList)
                    searchAdapter.notifyDataSetChanged()
                }
            }
            loadUsers()
        }
        } else {
            Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }
    }
    private fun loadUsers() {
        val storageRef = storage.reference

        db.collection("Usernames").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    if (searchItems.size != documents.documents.size) {
                        val data = doc.data

                        val chefOrUser = data?.get("chefOrUser") as String
                        val email = data["email"] as String
                        val fullName = data["fullName"] as String
                        val username = data["username"] as String


                        var x = if (chefOrUser == "Chef") {
                            "chefs"
                        } else {
                            "users"
                        }
                        Log.d(TAG, "loadUsers: x $x")

                            if (searchItems.size == 0) {
                                searchItems.add(
                                    com.ruh.taiste.both.models.Search(
                                        chefOrUser,
                                        Uri.EMPTY,
                                        doc.id,
                                        username,
                                        email,
                                        fullName
                                    )
                                )

                            } else {
                                val index =
                                    searchItems.indexOfFirst { it.userImageId == userImageId }
                                if (index == -1) {
                                    searchItems.add(
                                        com.ruh.taiste.both.models.Search(
                                            chefOrUser,
                                            Uri.EMPTY,
                                            doc.id,
                                            username,
                                            email,
                                            fullName
                                        )
                                    )
                                }
                            }

                        Log.d(TAG, "loadUsers: users $searchItems")
                    }


                }
            }
        }

    }



}