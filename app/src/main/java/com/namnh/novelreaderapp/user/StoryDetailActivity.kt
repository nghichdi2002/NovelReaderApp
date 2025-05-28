package com.namnh.novelreaderapp.user

import android.text.method.ScrollingMovementMethod
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.namnh.novelreaderapp.R
import com.namnh.novelreaderapp.admin_adapter.ChapterAdapter
import com.namnh.novelreaderapp.databinding.ActivityUserStoryDetailBinding
import com.namnh.novelreaderapp.item.Chapter
import com.namnh.novelreaderapp.item.Story
import com.namnh.novelreaderapp.item.Comment
import com.namnh.novelreaderapp.item.User
import com.namnh.novelreaderapp.user_adapter.CommentAdapter
import com.squareup.picasso.Picasso

class StoryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserStoryDetailBinding
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var chapterAdapter: ChapterAdapter
    private lateinit var commentList: MutableList<Comment>
    private lateinit var chapterList: MutableList<Chapter>
    private lateinit var commentRef: DatabaseReference
    private lateinit var chaptersRef: DatabaseReference
    private lateinit var userFavoritesRef: DatabaseReference
    private lateinit var readingProgressRef: DatabaseReference
    private lateinit var story: Story
    private var isFavorite = false
    private lateinit var starIcon: ImageView  // Thêm ImageView
    private lateinit var tvAverageRating: TextView
    private lateinit var storyRatingsRef: DatabaseReference
    private var userRating: Float = 0.0f
    private var averageRating: Float = 0.0f
    private var ratingCount: Int = 0
    private lateinit var replyCountMap: MutableMap<String, Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // An status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Lấy thông tin story từ Intent và gán vào biến instance story
        intent.getSerializableExtra("story")?.let {
            story = it as Story

            starIcon = binding.starIcon
            tvAverageRating = binding.tvAverageRating

            // Hiển thị thông tin truyện
            binding.tvStoryTitle.text = story.title
            binding.tvStoryAuthor.text = buildString {
                append("Tác giả: ")
                append(story.author)
            }
            binding.tvStoryGenres.text = buildString {
                append("Thể loại: ")
                append(story.genre)
            }
            binding.tvStoryViews.text = buildString {
                append("Lượt xem: ")
                append(story.viewCount)
            }
            binding.tvStoryDescription.text = story.description
            val para = binding.tvStoryDescription
            para.movementMethod = ScrollingMovementMethod()
            Picasso.get().load(story.imageUrl).into(binding.ivStoryCover)

            // Lấy reference đến mục yêu thích của người dùng
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            userId.let { uid ->
                userFavoritesRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
                    .child("favorites")
                readingProgressRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
                    .child("reading_progress").child(story.id)
                storyRatingsRef =
                    FirebaseDatabase.getInstance().getReference("story_ratings").child(story.id)
                checkIfFavorite()
                loadRatings()

                // Xử lý khi người dùng nhấn nút yêu thích
                binding.btnFavorite.setOnClickListener {
                    if (isFavorite) {
                        removeFavorite()
                    } else {
                        addFavorite()
                    }
                }

                // Khởi tạo danh sách chapter và comment
                commentList = mutableListOf()
                replyCountMap = mutableMapOf()
                chapterList = mutableListOf()
                commentAdapter = CommentAdapter(
                    commentList,
                    replyCountMap,
                    showReplyButton = true) { comment ->
                    val intent = Intent(this, ReplyCommentActivity::class.java)
                    intent.putExtra("comment", comment)
                    intent.putExtra("storyId", story.id)
                    startActivity(intent)
                }
                binding.recyclerViewComments.adapter = commentAdapter
                chapterAdapter = ChapterAdapter(chapterList, this@StoryDetailActivity)
                binding.recyclerViewComments.layoutManager =
                    LinearLayoutManager(this@StoryDetailActivity)
                binding.recyclerViewComments.adapter = commentAdapter
                binding.recyclerViewChapters.layoutManager =
                    LinearLayoutManager(this@StoryDetailActivity)
                binding.recyclerViewChapters.adapter = chapterAdapter

                // Lấy dữ liệu từ Firebase
                chaptersRef = FirebaseDatabase.getInstance().getReference("stories").child(story.id)
                    .child("chapters")
                commentRef = FirebaseDatabase.getInstance().getReference("comments").child(story.id)

                loadChapterList()
                loadComments()

                // Xử lý khi nhấn nút đăng bình luận
                binding.btnPostComment.setOnClickListener {
                    val commentText = binding.etComment.text.toString()
                    if (commentText.isNotEmpty()) {
                        postComment(commentText)
                        binding.etComment.text.clear()
                    }
                }

                replyCountMap = mutableMapOf()
                commentAdapter = CommentAdapter(commentList, replyCountMap) { comment ->
                    val intent = Intent(this, ReplyCommentActivity::class.java)
                    intent.putExtra("comment", comment)
                    intent.putExtra("storyId", story.id)
                    startActivity(intent)
                }
                binding.recyclerViewComments.adapter = commentAdapter

                // Xử lý nút back
                binding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadViewCount()
    }

    // Hàm hiển thị dialog đánh giá
    fun showRatingDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_rating)  // Tạo layout dialog riêng

        val ratingBar = dialog.findViewById<RatingBar>(R.id.dialogRatingBar)
        val btnSubmit = dialog.findViewById<Button>(R.id.dialogButtonSubmit)

        ratingBar?.rating = userRating // Khởi tạo rating bar với rating hiện tại của user
        ratingBar?.stepSize = 0.5f

        btnSubmit?.setOnClickListener {
            val newRating = ratingBar?.rating ?: 0.0f
            submitRating(newRating)
            dialog.dismiss()
        }

        dialog.show()
    }

    // Load đánh giá trung bình và user rating
    private fun loadRatings() {
        storyRatingsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var totalRating = 0.0f
                ratingCount = 0
                val userId = FirebaseAuth.getInstance().currentUser!!.uid

                for (snapshot in dataSnapshot.children) {
                    val rating = snapshot.getValue(Float::class.java) ?: 0.0f
                    totalRating += rating
                    ratingCount++

                    if (userId != null && snapshot.key == userId) {
                        userRating = rating
                    }
                }

                averageRating = if (ratingCount > 0) totalRating / ratingCount else 0.0f
                updateRatingUI()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("StoryDetailActivity", "Lỗi tải đánh giá: ${databaseError.message}")
                Toast.makeText(this@StoryDetailActivity, "Lỗi tải đánh giá.", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun submitRating(newRating: Float) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        storyRatingsRef.child(userId).setValue(newRating)
            .addOnSuccessListener {
                Toast.makeText(
                    this@StoryDetailActivity,
                    "Đánh giá đã được gửi!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Log.e("StoryDetailActivity", "Lỗi gửi đánh giá: ${e.message}")
                Toast.makeText(this@StoryDetailActivity, "Lỗi gửi đánh giá.", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun updateRatingUI() {
        // Hiển thị điểm rating trung bình
        tvAverageRating.text = String.format("%.1f (%d đánh giá)", averageRating, ratingCount)

        // Thay đổi icon ngôi sao dựa trên đánh giá của người dùng (nếu có)
        val starResource = if (userRating > 0) R.drawable.ic_star else R.drawable.ic_star_border
        starIcon.setImageResource(starResource)
    }

    private fun addFavorite() {
        userFavoritesRef.child(story.id).setValue(true).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                isFavorite = true
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_checked)  // Icon đậm
            }
        }
    }

    private fun removeFavorite() {
        userFavoritesRef.child(story.id).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                isFavorite = false
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_unchecked)  // Icon viền
            }
        }
    }

    private fun checkIfFavorite() {
        userFavoritesRef.child(story.id).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isFavorite = snapshot.exists()
                if (isFavorite) {
                    binding.btnFavorite.setImageResource(R.drawable.ic_favorite_checked)  // Icon đậm
                } else {
                    binding.btnFavorite.setImageResource(R.drawable.ic_favorite_unchecked)  // Icon viền
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("StoryDetailActivity", "Error checking favorite status: ${error.message}")
            }
        })
    }

    private fun postComment(content: String) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        // Tạo đối tượng Comment với userId và storyId từ biến story
        val commentId = commentRef.push().key ?: ""
        val comment = Comment(commentId, story.id, userId, content, System.currentTimeMillis())
        if (commentId.isNotEmpty()) {
            commentRef.child(commentId).setValue(comment)
        }
    }

    private fun loadChapterList() {
        chaptersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chapterList.clear()
                for (snapshot in dataSnapshot.children) {
                    val chapter = snapshot.getValue(Chapter::class.java)
                    chapter?.let {
                        chapterList.add(it)
                    }
                }
                chapterAdapter.notifyDataSetChanged()

                // Xử lý khi người dùng nhấn vào chapter
                chapterAdapter.setOnItemClickListener(object : ChapterAdapter.OnItemClickListener {
                    override fun onItemClick(chapter: Chapter, position: Int) {
                        val intent =
                            Intent(this@StoryDetailActivity, ChapterDetailActivity::class.java)
                        intent.putExtra("chapter", chapter)
                        intent.putExtra("story", story)
                        intent.putExtra("chapterIndex", position)
                        startActivity(intent)
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("StoryDetailActivity", "Error loading chapters: ${databaseError.message}")
            }
        })
    }

    private fun loadComments() {
        commentRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                commentList.clear()
                replyCountMap.clear()
                for (snapshot in dataSnapshot.children) {
                    val comment = snapshot.getValue(Comment::class.java)
                    comment?.let {
                        // Chỉ lấy comment gốc
                        if (it.parentId.isNullOrEmpty()) {
                            loadCommentUserData(it)
                            commentList.add(it)
                            loadReplyCount(it.id ?: "")
                        }
                    }
                }
                commentAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("StoryDetailActivity", "Error loading comments: ${databaseError.message}")
            }
        })
    }

    // Hàm load số lượng reply cho một comment gốc
    private fun loadReplyCount(commentId: String) {
        commentRef.orderByChild("parentId").equalTo(commentId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    replyCountMap[commentId] = snapshot.childrenCount.toInt()
                    commentAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadCommentUserData(comment: Comment) {
        val userId = comment.userId

        if (!userId.isNullOrEmpty()) {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let {
                        comment.username = user.username
                        comment.avatarUrl = user.avatar
                        commentAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(
                        "StoryDetailActivity",
                        "Error loading user data: ${databaseError.message}"
                    )
                }
            })
        } else {
            Log.e("StoryDetailActivity", "User ID is null or empty")
        }
    }

    private fun loadViewCount() {
        val storyRef = FirebaseDatabase.getInstance().getReference("stories").child(story.id)
        storyRef.child("viewCount").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val viewCount = snapshot.getValue(Long::class.java) ?: 0L
                binding.tvStoryViews.text = "Lượt xem: $viewCount"
                // Nếu muốn cập nhật lại biến story.viewCount:
                story.viewCount = viewCount
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}