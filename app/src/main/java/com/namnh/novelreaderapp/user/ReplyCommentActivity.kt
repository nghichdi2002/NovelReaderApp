package com.namnh.novelreaderapp.user

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.namnh.novelreaderapp.R
import com.namnh.novelreaderapp.databinding.ActivityReplyCommentBinding
import com.namnh.novelreaderapp.item.Comment
import com.namnh.novelreaderapp.item.User
import com.namnh.novelreaderapp.user_adapter.CommentAdapter
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReplyCommentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReplyCommentBinding
    private lateinit var rootComment: Comment
    private lateinit var storyId: String
    private lateinit var replyAdapter: CommentAdapter
    private lateinit var repliesList: MutableList<Comment>
    private lateinit var repliesRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReplyCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // An status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Nhận comment gốc và storyId
        rootComment = intent.getSerializableExtra("comment") as Comment
        storyId = intent.getStringExtra("storyId") ?: ""

        // Hiển thị comment gốc (dùng include layout comment)
        binding.layoutRootComment.apply {
            tvCommentUsername.text = rootComment.username
            tvCommentContent.text = rootComment.content
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            tvCommentTime.text = dateFormat.format(Date(rootComment.timestamp))
            if (!rootComment.avatarUrl.isNullOrEmpty()) {
                Picasso.get().load(rootComment.avatarUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .into(ivCommentAvatar)
            } else {
                ivCommentAvatar.setImageResource(R.drawable.ic_placeholder)
            }
            // Ẩn nút trả lời cho comment gốc (nếu muốn)
            tvReply.visibility = View.GONE
        }

        // Khởi tạo danh sách comment trả lời
        repliesList = mutableListOf()
        replyAdapter = CommentAdapter(
            repliesList,
            emptyMap(),
            showReplyButton = false) { /* Không cho phép trả lời lồng */ }

        binding.recyclerViewReplies.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewReplies.adapter = replyAdapter
        binding.layoutRootComment.tvReply.visibility = View.GONE

        // Firebase ref đến comments của truyện này
        repliesRef = FirebaseDatabase.getInstance().getReference("comments").child(storyId)
        loadReplies()

        // Gửi trả lời
        binding.btnSendReply.setOnClickListener {
            val replyText = binding.etReply.text.toString().trim()
            if (replyText.isNotEmpty()) {
                postReply(replyText)
                binding.etReply.text.clear()
            }
        }
    }

    private fun loadReplies() {
        // Lấy tất cả comment có parentId == rootComment.id
        repliesRef.orderByChild("parentId").equalTo(rootComment.id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    repliesList.clear()
                    for (c in snapshot.children) {
                        val reply = c.getValue(Comment::class.java)
                        reply?.let {
                            loadCommentUserData(it)
                            repliesList.add(it)
                        }
                    }
                    replyAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) { }
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
                        replyAdapter.notifyDataSetChanged()
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

    private fun postReply(content: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val replyId = repliesRef.push().key ?: return
        val reply = Comment(
            id = replyId,
            storyId = storyId,
            userId = userId,
            content = content,
            timestamp = System.currentTimeMillis(),
            parentId = rootComment.id
        )
        repliesRef.child(replyId).setValue(reply)
    }
}