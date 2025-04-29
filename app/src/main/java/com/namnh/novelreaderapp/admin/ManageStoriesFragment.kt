package com.namnh.novelreaderapp.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.namnh.novelreaderapp.databinding.ManageStoriesFragmentBinding
import com.namnh.novelreaderapp.item.Story
import androidx.appcompat.widget.SearchView
import com.namnh.novelreaderapp.admin_adapter.AdminStoryAdapter

class ManageStoriesFragment : Fragment() {

    private var _binding: ManageStoriesFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var storyAdapter: AdminStoryAdapter
    private var storyList: MutableList<Story> = mutableListOf()

    private val TAG = "ManageStoriesFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ManageStoriesFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.recyclerViewStories.layoutManager = LinearLayoutManager(requireContext())
        storyAdapter = AdminStoryAdapter(storyList, requireContext())
        binding.recyclerViewStories.adapter = storyAdapter

        loadStoriesFromFirebase()

        binding.btnAddStory.setOnClickListener { openAddStoryDialog() }
        setupSearchView()

        return view
    }

    private fun loadStoriesFromFirebase() {
        val dbRef = FirebaseDatabase.getInstance().getReference("stories")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                storyList.clear()
                for (snapshot in dataSnapshot.children) {
                    try {
                        val story = snapshot.getValue(Story::class.java)
                        story?.let {
                            storyList.add(it)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing story: ${e.message}")
                    }
                }
                storyAdapter.updateList(storyList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Database error: ${databaseError.message}")
            }
        })
    }

    private fun setupSearchView() {
        binding.searchViewStories.queryHint = "Tìm kiếm truyện"
        binding.searchViewStories.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                storyAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                storyAdapter.filter.filter(newText)
                return false
            }
        })
        binding.searchViewStories.isIconified = false
    }

    private fun openAddStoryDialog() {
        val intent = Intent(activity, ThemTruyen::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}