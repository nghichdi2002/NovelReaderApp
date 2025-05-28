package com.namnh.novelreaderapp.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.namnh.novelreaderapp.R
import com.namnh.novelreaderapp.databinding.ActivityFavoriteStoriesBinding
import com.namnh.novelreaderapp.item.Chapter
import com.namnh.novelreaderapp.item.FavoriteStory
import com.namnh.novelreaderapp.item.Story
import com.namnh.novelreaderapp.user_adapter.FavoriteStoryAdapter

class FavoriteStoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteStoriesBinding
    // Giữ nguyên các khai báo biến
    private lateinit var favoriteStoryAdapter: FavoriteStoryAdapter
    private val favoriteStoryList = ArrayList<FavoriteStory>()
    private lateinit var userFavoritesRef: DatabaseReference // Reference đến node favorites của user
    private lateinit var storiesRef: DatabaseReference // Thêm Reference đến node stories chung

    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser // Khai báo currentUser và chắc chắn nó không null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // hide status bar
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding = ActivityFavoriteStoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!! // Sử dụng !! vì bạn đảm bảo user đã login


        binding.recyclerViewFavoriteStories.layoutManager = LinearLayoutManager(this)

        // *** CHỈNH SỬA DÒNG KHỞI TẠO ADAPTER NÀY ***
        // Thêm lambda function xử lý click vào constructor của Adapter
        favoriteStoryAdapter = FavoriteStoryAdapter(favoriteStoryList, this) { clickedFavoriteStory ->
            // *** Đây là nơi xử lý khi một item trong danh sách yêu thích được click ***
            Log.d("FavoriteClick", "Clicked on: ${clickedFavoriteStory.title}")

            // Lấy đối tượng Story đầy đủ từ FavoriteStory được click
            // Đối tượng này đã được tải và gán vào FavoriteStory.story trong loadFavoriteStories()
            val storyToPass = clickedFavoriteStory.story

            if (storyToPass != null) {
                // Tạo Intent để chuyển sang StoryDetailActivity
                val intent = Intent(this, StoryDetailActivity::class.java)

                // *** TRUYỀN ĐỐI TƯỢNG STORY ĐẦY ĐỦ QUA INTENT ***
                // Đối tượng Story cần implement Serializable hoặc Parcelable
                // Dựa trên khai báo Story của bạn có : Serializable là đúng
                intent.putExtra("story", storyToPass) // Key "story" phải khớp với key bạn dùng trong StoryDetailActivity để lấy object

                // Bắt đầu StoryDetailActivity
                startActivity(intent)
            } else {
                // Xử lý trường hợp đối tượng Story bên trong FavoriteStory là null (lỗi dữ liệu)
                Toast.makeText(this, "Thông tin truyện không đầy đủ!", Toast.LENGTH_SHORT).show()
                Log.e("FavoriteStories", "Clicked on favorite story with null Story object")
            }
        }
        // Giữ nguyên dòng set Adapter
        binding.recyclerViewFavoriteStories.adapter = favoriteStoryAdapter

        // *** Thêm khởi tạo Reference đến node "stories" ***
        storiesRef = FirebaseDatabase.getInstance().getReference("stories")
        val uid = currentUser.uid
        userFavoritesRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("favorites") // Nên chỉ định URL
        loadFavoriteStories() // Gọi hàm load dữ liệu ngay


    }
    // @SuppressLint("NotifyDataSetChanged") // Có thể thêm annotation này
    private fun loadFavoriteStories() {

        userFavoritesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                favoriteStoryList.clear() // Giữ nguyên clear list

                val totalFavoriteCount = dataSnapshot.childrenCount.toInt() // Số lượng mục yêu thích
                var loadedStoryCount = 0 // Đếm số truyện đã xử lý (tải chi tiết thành công/thất bại)



                for (snapshot in dataSnapshot.children) {
                    val storyId = snapshot.key // Lấy ID truyện từ key trong favorites của user


                    if (storyId != null) {
                        // *** Truy vấn chi tiết truyện từ node "stories" ***
                        storiesRef.child(storyId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(storySnapshot: DataSnapshot) {
                                    if (storySnapshot.exists()) {
                                        // Nếu truyện tồn tại trong node /stories, parse thành Story object
                                        val story = storySnapshot.getValue(Story::class.java)
                                        if (story != null) {
                                            // *** Tạo đối tượng FavoriteStory và gán Story object vào trường 'story' ***
                                            val favoriteStory = FavoriteStory(
                                                id = storyId, // ID của mục yêu thích
                                                title = story.title, // Lấy từ Story
                                                imageUrl = story.imageUrl, // Lấy từ Story
                                                story = story // *** ĐÂY LÀ ĐIỂM QUAN TRỌNG: GÁN STORY OBJECT ĐẦY ĐỦ ***
                                            )
                                            // *** THÊM FavoriteStory VÀO DANH SÁCH ***
                                            favoriteStoryList.add(favoriteStory)
                                        } else {
                                            Log.w("FavoriteStories", "Failed to parse Story object for ID $storyId")
                                        }
                                    } else {
                                        // Trường hợp truyện gốc bị xóa khỏi /stories nhưng vẫn còn trong favorites
                                        Log.w("FavoriteStories", "Story with ID $storyId not found in /stories. Skipping.")
                                        // Tùy chọn: Có thể xóa mục này khỏi favorites của user để làm sạch
                                        // userFavoritesRef.child(storyId).removeValue()
                                    }

                                    loadedStoryCount++
                                    // *** KHI TẤT CẢ CÁC TRUY VẤN CHI TIẾT HOÀN THÀNH ***
                                    // (Kiểm tra bằng cách so sánh loadedStoryCount và totalFavoriteCount)
                                    if (loadedStoryCount == totalFavoriteCount) {
                                        // Có thể sắp xếp favoriteStoryList ở đây nếu cần
                                        // favoriteStoryList.sortBy { it.story?.title } // Sắp xếp theo tiêu đề truyện

                                        // *** CHỈ GỌI notifyDataSetChanged MỘT LẦN SAU KHI TẤT CẢ DATA ĐÃ CÓ ***
                                        favoriteStoryAdapter.notifyDataSetChanged()
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Xử lý lỗi khi tải chi tiết một truyện cụ thể
                                    Log.e("FavoriteStories", "Failed to load story details for ID $storyId: ${databaseError.message}")
                                    loadedStoryCount++
                                }
                            })
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Xử lý lỗi khi tải danh sách favorites ban đầu của người dùng
                Log.e("FavoriteStories", "Failed to load favorite list for user: ${databaseError.message}")
                Toast.makeText(this@FavoriteStoriesActivity, "Lỗi khi tải danh sách yêu thích: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                favoriteStoryList.clear() // Clear list để chắc chắn rỗng
                favoriteStoryAdapter.notifyDataSetChanged() // Cập nhật Adapter (danh sách rỗng)
            }
        })
    }
}