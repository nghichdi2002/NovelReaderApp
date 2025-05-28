package com.namnh.novelreaderapp.user

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
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

        // hide status bar
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Nhận thể loại từ Intent
        genre = intent.getStringExtra("genre") ?: ""

        // Thiết lập cho nút Back mới
        binding.ivBack.setOnClickListener {
            onBackPressed() // Xử lý sự kiện click vào nút quay lại
        }

        // Đặt tiêu đề cho TextView mới
        binding.tvCustomTitle.text = "Thể loại: $genre"



        // Khởi tạo RecyclerView và Adapter
        storyAdapter = UserStoryAdapter(storyList, this) // Truyền context vào adapter
        binding.rvFilteredStories.adapter = storyAdapter
        binding.rvFilteredStories.layoutManager = GridLayoutManager(this, 2) // Hiển thị 2 truyện trên một hàng

        // Lấy và lọc truyện từ Firebase
        loadAndFilterStories(genre)

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
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(tag, "Database error: ${databaseError.message}")
            }
        })
    }
}