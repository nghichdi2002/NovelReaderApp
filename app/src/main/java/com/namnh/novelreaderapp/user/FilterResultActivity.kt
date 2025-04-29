package com.namnh.novelreaderapp.user

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.namnh.novelreaderapp.databinding.ActivityFilterResultBinding
import com.namnh.novelreaderapp.item.Story
import com.namnh.novelreaderapp.user_adapter.UserStoryAdapter

class FilterResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilterResultBinding
    private lateinit var genre: String
    private lateinit var storyAdapter: UserStoryAdapter
    private val storyList = mutableListOf<Story>()
    private val tag = "FilterResultActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Toolbar
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị nút quay lại
        toolbar.setNavigationOnClickListener {
            onBackPressed() // Xử lý sự kiện click vào nút quay lại
        }

        // Nhận thể loại từ Intent
        genre = intent.getStringExtra("genre") ?: ""

        // Khởi tạo RecyclerView và Adapter
        storyAdapter = UserStoryAdapter(storyList, this) // Truyền context vào adapter
        binding.rvFilteredStories.adapter = storyAdapter
        binding.rvFilteredStories.layoutManager = GridLayoutManager(this, 2) // Hiển thị 2 truyện trên một hàng

        // Lấy và lọc truyện từ Firebase
        loadAndFilterStories(genre)

        // Đặt tiêu đề cho Toolbar
        supportActionBar?.title = "Truyện thể loại: $genre" // Đặt tiêu đề là thể loại đã chọn
    }

    private fun loadAndFilterStories(genre: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("stories")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                storyList.clear()
                for (snapshot in dataSnapshot.children) {
                    val story = snapshot.getValue(Story::class.java)
                    story?.let {
                        // Lọc truyện theo thể loại
                        if (it.genre == genre) {
                            storyList.add(it)
                            Log.d(tag, "Loaded: ${it.title}")
                        }
                    }
                }
                // Cập nhật danh sách truyện trong adapter
                storyAdapter.updateList(storyList)

                // Hiển thị/ẩn thông báo khi không có kết quả
                if (storyList.isEmpty()) {
                    binding.tvNoResults.visibility = View.VISIBLE
                    binding.rvFilteredStories.visibility = View.GONE
                } else {
                    binding.tvNoResults.visibility = View.GONE
                    binding.rvFilteredStories.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(tag, "Database error: ${databaseError.message}")
            }
        })
    }
}