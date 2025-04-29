package com.namnh.novelreaderapp.user_adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.namnh.novelreaderapp.databinding.ItemHistoryBinding
import com.namnh.novelreaderapp.item.History
import com.namnh.novelreaderapp.user.ChapterDetailActivity
import com.squareup.picasso.Picasso

class StoryAdapter(private val histories: List<History>, private val context: Context) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {
    inner class StoryViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val history = histories[position]
                    val intent = Intent(context, ChapterDetailActivity::class.java).apply {
                        putExtra("story", history.story)
                        putExtra("chapterIndex", history.currentChapter?.toInt() ?: 0)
                    }
                    context.startActivity(intent)
                }
            }
        }

        fun bind(history: History) {
            binding.apply {
                textViewTitle.text = history.title
                textViewCurrentChapter.text = "Chapter đang đọc: ${history.currentChapter?.plus(1) ?: "N/A"}"
                textViewLatestChapter.text = "Chapter mới nhất: ${history.latestChapter ?: "N/A"}"
                Picasso.get().load(history.imageUrl).into(imageViewCover)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val history = histories[position]
        Log.d("StoryAdapter", "onBindViewHolder: position = $position, history = $history")
        holder.bind(history)
    }

    override fun getItemCount(): Int {
        Log.d("StoryAdapter", "getItemCount: ${histories.size}")
        return histories.size
    }
}