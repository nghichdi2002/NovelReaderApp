package com.namnh.novelreaderapp.user_adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.namnh.novelreaderapp.R
import com.namnh.novelreaderapp.databinding.ItemUserStoryBinding
import com.namnh.novelreaderapp.item.Story
import com.namnh.novelreaderapp.user.StoryDetailActivity
import com.squareup.picasso.Picasso

class UserStoryAdapter(private var storyList: List<Story>, private val context: Context) :
    RecyclerView.Adapter<UserStoryAdapter.StoryViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemUserStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = storyList[position]

        // Set data for UI components
        holder.tvTitle.text = story.title
        holder.tvAuthor.text = "Tác giả: ${story.author}"
        holder.tvGenres.text = "Thể loại: ${story.genre}"
        holder.tvViews.text = "Lượt xem: ${story.viewCount}"

        // Use Picasso to load cover image from URL
        Picasso.get()
            .load(story.imageUrl)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_error)
            .into(holder.ivCover)

        // Handle click on item
        holder.itemView.setOnClickListener {
            val intent = Intent(context, StoryDetailActivity::class.java)
            intent.putExtra("story", story)  // Pass story data via Intent
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return storyList.size
    }

    fun updateList(filteredList: List<Story>) {
        storyList = filteredList
        notifyDataSetChanged()
    }

    inner class StoryViewHolder(private val binding: ItemUserStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        var ivCover = binding.ivStoryCover
        var tvTitle = binding.tvStoryTitle
        var tvAuthor = binding.tvStoryAuthor
        var tvGenres = binding.tvStoryGenre
        var tvViews = binding.tvStoryViews
    }
}