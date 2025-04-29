package com.namnh.novelreaderapp.user

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.namnh.novelreaderapp.R
import com.namnh.novelreaderapp.databinding.FragmentHomeBinding
import com.namnh.novelreaderapp.item.Story
import com.namnh.novelreaderapp.user_adapter.UserStoryAdapter
import com.namnh.novelreaderapp.user_adapter.ViewPagerAdapter
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var storyAdapter: UserStoryAdapter
    private val storyList = mutableListOf<Story>()
    private lateinit var viewPager2: ViewPager2
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private val imageIds = mutableListOf<Int>()
    private val sliderHandler = Handler(Looper.getMainLooper())
    private val tag = "HomeFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        val recyclerView = binding.recyclerViewHome
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        storyAdapter = UserStoryAdapter(storyList, requireContext())
        recyclerView.adapter = storyAdapter

        viewPager2 = binding.viewpagerHighlight

        // Thêm ID của các hình ảnh trong drawable vào danh sách
        imageIds.add(R.drawable.img1)
        imageIds.add(R.drawable.img2)
        imageIds.add(R.drawable.img3)
        imageIds.add(R.drawable.img4)

        viewPagerAdapter = ViewPagerAdapter(imageIds, requireContext())
        viewPager2.adapter = viewPagerAdapter

        sliderHandler.postDelayed(sliderRunnable, 3000)

        loadStoriesFromFirebase()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchStories(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchStories(newText)
                return false
            }
        })

        return view
    }

    private val sliderRunnable = object : Runnable {
        override fun run() {
            val currentItem = viewPager2.currentItem
            val totalItems = viewPagerAdapter.itemCount

            if (currentItem < totalItems - 1) {
                viewPager2.currentItem = currentItem + 1
            } else {
                viewPager2.currentItem = 0
            }
            sliderHandler.postDelayed(this, 3000)
        }
    }

    private fun loadStoriesFromFirebase() {
        val dbRef = Firebase.database.reference.child("stories")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                storyList.clear()
                for (snapshot in dataSnapshot.children) {
                    val story = snapshot.getValue(Story::class.java)
                    story?.let {
                        storyList.add(it)
                        Log.d(tag, "Loaded: ${story.title}")
                    }
                }
                storyAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(tag, "Database error: ${databaseError.message}")
            }
        })
    }

    private fun searchStories(query: String) {
        val filteredList = storyList.filter { story ->
            story.title.lowercase(Locale.getDefault()).contains(query.lowercase())
        }
        storyAdapter.updateList(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sliderHandler.removeCallbacks(sliderRunnable)
    }
}