package com.namnh.novelreaderapp.user_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.namnh.novelreaderapp.R
import com.namnh.novelreaderapp.databinding.ItemCommentBinding
import com.namnh.novelreaderapp.item.Comment
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentAdapter(
    private val commentList: List<Comment>,
    private val replyCountMap: Map<String, Int>, // key: commentId, value: số reply
    private val showReplyButton: Boolean = true,
    private val onReplyClick: (Comment) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        with(holder.binding) {
            tvCommentUsername.text = comment.username
            tvCommentContent.text = comment.content

            Picasso.get().load(comment.avatarUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(ivCommentAvatar)

            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            tvCommentTime.text = dateFormat.format(Date(comment.timestamp))

            if (showReplyButton && comment.parentId.isNullOrEmpty()) {
                // Lấy số lượng trả lời từ map, nếu không có thì là 0
                val replyCount = replyCountMap[comment.id] ?: 0
                tvReply.text = if (replyCount > 0) "Trả lời ($replyCount)" else "Trả lời"
                tvReply.visibility = View.VISIBLE
                tvReply.setOnClickListener {
                    onReplyClick(comment)
                }
            } else {
                tvReply.visibility = View.GONE
            }

        }
    }

    override fun getItemCount(): Int = commentList.size

    inner class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)
}