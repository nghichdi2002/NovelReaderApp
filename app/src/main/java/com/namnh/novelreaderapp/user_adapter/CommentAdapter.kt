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
    private val context: Context
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]

        // Hiển thị tên người dùng và nội dung bình luận
        holder.binding.tvCommentUsername.text = comment.username
        holder.binding.tvCommentContent.text = comment.content

        // Tải ảnh đại diện của người dùng
        Picasso.get().load(comment.avatarUrl)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_error)
            .into(holder.binding.ivCommentAvatar)

        // Hiển thị thời gian bình luận dưới dạng "month day, year"
        val timestamp = comment.timestamp
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(timestamp))
        holder.binding.tvCommentTime.text = formattedDate
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemCommentBinding.bind(itemView)
    }
}