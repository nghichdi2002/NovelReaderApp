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

class FavoriteStoryAdapter(private val favoriteStories: List<FavoriteStory>, private val context: Context) : RecyclerView.Adapter<FavoriteStoryAdapter.StoryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemFavoriteStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val favoriteStory = favoriteStories[position]

        holder.binding.textViewTitle.text = favoriteStory.title

        favoriteStory.currentChapter?.let {
            holder.binding.textViewCurrentChapter.text = "Chapter đang đọc: ${it + 1}"
        } ?: run {
            holder.binding.textViewCurrentChapter.text = "Chapter đang đọc: N/A"
        }

        favoriteStory.latestChapter?.let {
            holder.binding.textViewLatestChapter.text = "Chapter mới nhất: $it"
        } ?: run {
            holder.binding.textViewLatestChapter.text = "Chapter mới nhất: N/A"
        }

        Picasso.get().load(favoriteStory.imageUrl).into(holder.binding.imageViewCover)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChapterDetailActivity::class.java)
            intent.putExtra("story", favoriteStory.story)

            val chapterIndex = favoriteStory.currentChapter?.let { Math.max(it.toInt() - 1, 0) } ?: 0
            intent.putExtra("chapterIndex", chapterIndex)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return favoriteStories.size
    }

    inner class StoryViewHolder(val binding: ItemFavoriteStoryBinding) : RecyclerView.ViewHolder(binding.root)
}