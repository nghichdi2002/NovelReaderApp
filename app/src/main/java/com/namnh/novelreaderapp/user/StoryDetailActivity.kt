package com.namnh.novelreaderapp.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy thông tin story từ Intent và gán vào biến instance story
        intent.getSerializableExtra("story")?.let {
            story = it as Story

            // Hiển thị thông tin truyện
                binding.tvStoryTitle.text = story.title
                binding.tvStoryAuthor.text = story.author
                binding.tvStoryGenres.text = story.genre
                binding.tvStoryDescription.text = story.description
                Picasso.get().load(story.imageUrl).into(binding.ivStoryCover)

                // Lấy reference đến mục yêu thích của người dùng
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                userId?.let { uid ->
                    userFavoritesRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("favorites")
                    readingProgressRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("reading_progress").child(story.id)

                    checkIfFavorite()

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
                    chapterList = mutableListOf()
                    commentAdapter = CommentAdapter(commentList, this@StoryDetailActivity)
                    chapterAdapter = ChapterAdapter(chapterList, this@StoryDetailActivity)
                    binding.recyclerViewComments.layoutManager = LinearLayoutManager(this@StoryDetailActivity)
                    binding.recyclerViewComments.adapter = commentAdapter
                    binding.recyclerViewChapters.layoutManager = LinearLayoutManager(this@StoryDetailActivity)
                    binding.recyclerViewChapters.adapter = chapterAdapter

                    // Lấy dữ liệu từ Firebase
                    chaptersRef = FirebaseDatabase.getInstance().getReference("stories").child(story.id).child("chapters")
                    commentRef = FirebaseDatabase.getInstance().getReference("comments").child(story.id)

                    // Truy vấn chapter đang đọc
                    loadReadingProgress()
                    loadComments()

                    // Xử lý khi nhấn nút đăng bình luận
                    binding.btnPostComment.setOnClickListener {
                        val commentText = binding.etComment.text.toString()
                        if (commentText.isNotEmpty()) {
                            postComment(commentText)
                            binding.etComment.text.clear()
                        }
                    }

                    // Xử lý nút back
                    binding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
                }
        }
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
        // Lấy userId từ phiên người dùng hiện tại (đăng nhập)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Tạo đối tượng Comment với userId và storyId từ biến story
        val commentId = commentRef.push().key ?: ""
        val comment = Comment(commentId,story.id,userId,content,System.currentTimeMillis())
        if (commentId.isNotEmpty()) {
            commentRef.child(commentId).setValue(comment)
        }
    }

    private fun loadReadingProgress() {
        readingProgressRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val lastChapterRead = dataSnapshot.child("last_chapter_read").getValue(Long::class.java)
                if (lastChapterRead != null) {
                    openLastReadChapter(lastChapterRead.toInt())
                } else {
                    loadChapterList()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("StoryDetailActivity", "Error loading reading progress: ${databaseError.message}")
                loadChapterList()
            }
        })
    }

    private fun openLastReadChapter(lastChapterIndex: Int) {
        if (lastChapterIndex in 0 until chapterList.size) {
            val lastReadChapter = chapterList[lastChapterIndex]

            val intent = Intent(this@StoryDetailActivity, ChapterDetailActivity::class.java)
            intent.putExtra("chapter", lastReadChapter)
            intent.putExtra("story", story)
            intent.putExtra("chapterIndex", lastChapterIndex)  // chuyền chỉ số của chương qua Intent
            startActivity(intent)
        } else {
            loadChapterList()  // Nếu không tìm thấy chapter đang đọc, load danh sách chapter
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
                        val intent = Intent(this@StoryDetailActivity, ChapterDetailActivity::class.java)
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
                for (snapshot in dataSnapshot.children) {
                    val comment = snapshot.getValue(Comment::class.java)
                    comment?.let {
                        loadCommentUserData(it)
                        commentList.add(it)
                    }
                }
                commentAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("StoryDetailActivity", "Error loading comments: ${databaseError.message}")
            }
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
                    Log.e("StoryDetailActivity", "Error loading user data: ${databaseError.message}")
                }
            })
        } else {
            Log.e("StoryDetailActivity", "User ID is null or empty")
        }
    }
}