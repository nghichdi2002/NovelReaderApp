package com.namnh.novelreaderapp.admin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.namnh.novelreaderapp.admin_adapter.AdminChapterAdapter
import com.namnh.novelreaderapp.databinding.FragmentManageChaptersBinding
import com.namnh.novelreaderapp.item.Story


class ManageChaptersFragment : Fragment() {
    private lateinit var binding: FragmentManageChaptersBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var chapterAdapter: AdminChapterAdapter
    private var storyOrChapterList: MutableList<Any> = mutableListOf()  // Dùng Any để chứa cả Story và Chapter
    private lateinit var searchView: SearchView

    companion object {
        private const val TAG = "ManageChaptersFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentManageChaptersBinding.inflate(inflater, container, false)
        val view = binding.root

        recyclerView = binding.recyclerViewChapters
        searchView = binding.searchViewChapters

        chapterAdapter = AdminChapterAdapter(storyOrChapterList, requireContext())

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = chapterAdapter

        loadStoriesFromFirebase()  // Load danh sách truyện ban đầu
        setupSearchView()

        return view
    }

    private fun loadStoriesFromFirebase() {
        val dbRef = FirebaseDatabase.getInstance().getReference("stories")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                storyOrChapterList.clear()
                for (storySnapshot in dataSnapshot.children) {
                    try {
                        val story = storySnapshot.getValue(Story::class.java)  // Tạo object Story
                        if (story != null) {
                            Log.d(TAG, "Title: ${story.title}")
                            storyOrChapterList.add(story)  // Thêm Story vào danh sách
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing story: ${e.message}")
                    }
                }
                chapterAdapter.updateList(storyOrChapterList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Database error: ${databaseError.message}")
            }
        })
    }

    private fun setupSearchView() {
        searchView.queryHint = "Tìm kiếm truyện"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                chapterAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                chapterAdapter.filter.filter(newText)
                return false
            }
        })
        searchView.isIconified = false
    }
}