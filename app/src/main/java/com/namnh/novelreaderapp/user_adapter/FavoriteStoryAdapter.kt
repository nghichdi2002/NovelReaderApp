package com.namnh.novelreaderapp.user_adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.namnh.novelreaderapp.databinding.ItemFavoriteStoryBinding
import com.namnh.novelreaderapp.item.FavoriteStory
import com.namnh.novelreaderapp.user.ChapterDetailActivity
import com.squareup.picasso.Picasso

class FavoriteStoryAdapter(
    private val favoriteStories: List<FavoriteStory>,
    private val context: Context,
    // Thêm lambda function xử lý click item
    private val onItemClick: (FavoriteStory) -> Unit // Hàm này nhận vào FavoriteStory object
) : RecyclerView.Adapter<FavoriteStoryAdapter.StoryViewHolder>() {

    // ViewHolder giữ các View bằng View Binding
    inner class StoryViewHolder(val binding: ItemFavoriteStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // *** THÊM LISTENER CHO TOÀN BỘ ITEM VIEW ***
            itemView.setOnClickListener {
                val position = adapterPosition
                // Kiểm tra vị trí hợp lệ
                if (position != RecyclerView.NO_POSITION) {
                    // Lấy đối tượng FavoriteStory tương ứng
                    val clickedItem = favoriteStories[position]
                    // Gọi lambda function được truyền từ Activity, truyền đối tượng FavoriteStory
                    onItemClick.invoke(clickedItem)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemFavoriteStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val favoriteStory = favoriteStories[position]

        // Lấy đối tượng Story đầy đủ từ FavoriteStory
        val story = favoriteStory.story

        // Hiển thị tiêu đề và ảnh bìa từ đối tượng Story (hoặc FavoriteStory nếu cần)
        // Sử dụng story?. để truy cập an toàn
        holder.binding.textViewTitle.text = story?.title ?: favoriteStory.title ?: "N/A" // Ưu tiên lấy từ story, nếu không có thì lấy từ favoriteStory, cuối cùng là N/A
        Picasso.get().load(story?.imageUrl ?: favoriteStory.imageUrl) // Ưu tiên lấy từ story, nếu không có thì lấy từ favoriteStory
            .placeholder(com.namnh.novelreaderapp.R.drawable.ic_placeholder) // Thay bằng placeholder của bạn
            .error(com.namnh.novelreaderapp.R.drawable.ic_placeholder) // Thay bằng error image của bạn
            .into(holder.binding.imageViewCover)


    }

    override fun getItemCount(): Int {
        return favoriteStories.size
    }
}