package com.namnh.novelreaderapp.user

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.namnh.novelreaderapp.user_adapter.HistoriesAdapter

class HistoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoriesBinding
    private lateinit var historiesAdapter: HistoriesAdapter
    private lateinit var historyList: MutableList<History>
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // hide status bar
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding = ActivityHistoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        // Khởi tạo historyList
        historyList = mutableListOf()

        mAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = mAuth.currentUser

        if (currentUser != null) {
            val uid: String = currentUser.uid
            mDatabase = FirebaseDatabase.getInstance().getReference("users").child(uid).child("reading_progress")
//            loadReadingHistory()

            historiesAdapter = HistoriesAdapter(historyList, this) { historyToDelete ->
                // Đây là lambda được gọi khi nút xóa trong một item được bấm
                val userId = mAuth.currentUser?.uid
                if (userId != null) {
                    // Tham chiếu đến mục cần xóa trong Firebase Realtime Database
                    // Đường dẫn là users/$uid/reading_progress/$storyId
                    val historyRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(userId)
                        .child("reading_progress")
                        .child(historyToDelete.id ?: "") // Sử dụng storyId từ đối tượng History

                    historyRef.removeValue()
                        .addOnSuccessListener {
                            // Xóa thành công trên Realtime Database
                            Log.d("HistoriesActivity", "History removed from DB: ${historyToDelete.id}")
                            // Cập nhật UI bằng cách xóa khỏi adapter's list và thông báo
                            historiesAdapter.removeHistory(historyToDelete)
                            Toast.makeText(this, "Đã xóa lịch sử đọc", Toast.LENGTH_SHORT).show() // Thông báo thành công
                        }
                        .addOnFailureListener { error ->
                            // Xóa thất bại
                            Log.e("HistoriesActivity", "Error removing history from DB", error)
                            Toast.makeText(this, "Lỗi khi xóa lịch sử: ${error.message}", Toast.LENGTH_SHORT).show() // Thông báo lỗi
                        }
                } else {
                    // Tình huống hiếm khi xảy ra nếu đã kiểm tra currentUser ở đầu onCreate
                    Log.e("HistoriesActivity", "User not authenticated when trying to delete history")
                    Toast.makeText(this, "Lỗi xác thực người dùng", Toast.LENGTH_SHORT).show()
                }
            }

            // Thiết lập LayoutManager và Adapter cho RecyclerView
            binding.recyclerViewHistory.layoutManager = LinearLayoutManager(this) // Hoặc GridLayoutManager
            binding.recyclerViewHistory.adapter = historiesAdapter
        }

    }

    override fun onResume() {
        super.onResume()
        loadReadingHistory()
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

                                    val history = History(storyId, title, imageUrl, lastChapterRead, story)
                                    historyList.add(history)
                                    historiesAdapter.notifyDataSetChanged()
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