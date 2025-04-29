package com.namnh.novelreaderapp.admin_adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.namnh.novelreaderapp.admin.AdminStoryDetail
import com.namnh.novelreaderapp.databinding.ItemStoryBinding
import com.namnh.novelreaderapp.item.Story

class AdminStoryAdapter(private var storyList: List<Story>, private val context: Context) :
    RecyclerView.Adapter<AdminStoryAdapter.StoryViewHolder>(), Filterable {

    private var filteredStoryList: List<Story> = ArrayList(storyList)

    inner class StoryViewHolder(private val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        val tvTitle: TextView = binding.txtStoryTitle
        val tvAuthor: TextView = binding.txtStoryAuthor
        val tvGenres: TextView = binding.txtStoryGenres

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val story = filteredStoryList[position]
                    val intent = Intent(context, AdminStoryDetail::class.java).apply {
                        putExtra("story_id", story.id)
                        putExtra("story_title", story.title)
                        putExtra("story_author", story.author)
                        putExtra("story_genre", story.genre)
                        putExtra("story_description", story.description)
                        putExtra("story_image_url", story.imageUrl)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = filteredStoryList[position]
        holder.tvTitle.text = story.title
        holder.tvAuthor.text = story.author
        holder.tvGenres.text = story.genre
    }

    override fun getItemCount(): Int {
        return filteredStoryList.size
    }

    fun updateList(newList: List<Story>) {
        storyList = newList
        filteredStoryList = ArrayList(newList)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return storyFilter
    }

    private val storyFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<Story>()
            val filterPattern = constraint.toString().toLowerCase().trim()

            if (filterPattern.isEmpty()) {
                filteredList.addAll(storyList)
            } else {
                for (story in storyList) {
                    if (story.title.toLowerCase().contains(filterPattern) ||
                        story.author.toLowerCase().contains(filterPattern) ||
                        (story.genre.toLowerCase().contains(filterPattern) == true)
                    ) {
                        filteredList.add(story)
                    }
                }
            }

            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            filteredStoryList = results.values as List<Story>
            notifyDataSetChanged()
        }
    }
}