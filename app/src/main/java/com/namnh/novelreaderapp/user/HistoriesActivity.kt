package com.namnh.novelreaderapp.user

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.namnh.novelreaderapp.databinding.ActivityHistoriesBinding
import com.namnh.novelreaderapp.item.Chapter
import com.namnh.novelreaderapp.item.History
import com.namnh.novelreaderapp.item.Story
import com.namnh.novelreaderapp.user_adapter.StoryAdapter

class HistoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoriesBinding
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var historyList: MutableList<History>
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo historyList
        historyList = mutableListOf()

        /// Khởi tạo adapter
        storyAdapter = StoryAdapter(historyList, this) // Giả sử StoryAdapter nhận một list dữ liệu


        // Thiết lập LayoutManager và Adapter cho RecyclerView
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(this) // Hoặc GridLayoutManager
        binding.recyclerViewHistory.adapter = storyAdapter

        mAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = mAuth.currentUser

        if (currentUser != null) {
            val uid: String = currentUser.uid
            mDatabase = FirebaseDatabase.getInstance().getReference("users").child(uid).child("reading_progress")
            loadReadingHistory()
        }
    }

    private fun loadReadingHistory() {
        mDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                historyList.clear()
                for (snapshot in dataSnapshot.children) {
                    val storyId: String = snapshot.key!!
                    val lastChapterRead: Long = snapshot.child("last_chapter_read").getValue(Long::class.java)!!

                    FirebaseDatabase.getInstance().getReference("stories").child(storyId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(storySnapshot: DataSnapshot) {
                                if (storySnapshot.exists()) {
                                    val title: String = storySnapshot.child("title").getValue(String::class.java)?: ""
                                    val author: String = storySnapshot.child("author").getValue(String::class.java)?: ""
                                    val description: String = storySnapshot.child("description").getValue(String::class.java)?: ""
                                    val imageUrl: String = storySnapshot.child("imageUrl").getValue(String::class.java)?: ""

                                    val genre: String = storySnapshot.child("genre").getValue(String::class.java)?: ""

                                    val chapters: MutableMap<String, Chapter> = HashMap()
                                    for (chapterSnapshot in storySnapshot.child("chapters").children) {
                                        val chapter: Chapter = chapterSnapshot.getValue(Chapter::class.java)!!
                                        chapters[chapterSnapshot.key!!] = chapter
                                    }

                                    val story = Story(storyId, title, author, description, genre, imageUrl, chapters)

                                    val history = History(storyId, title, imageUrl, lastChapterRead, chapters.size.toLong(), story)
                                    historyList.add(history)
                                    storyAdapter.notifyDataSetChanged()
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Xử lý lỗi truy vấn
                            }
                        })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Xử lý lỗi truy vấn
            }
        })
    }
}