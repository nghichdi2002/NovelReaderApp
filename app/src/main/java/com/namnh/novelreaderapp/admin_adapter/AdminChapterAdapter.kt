package com.namnh.novelreaderapp.admin_adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.namnh.novelreaderapp.admin.AdminChapterList
import com.namnh.novelreaderapp.admin.EditChapterActivity
import com.namnh.novelreaderapp.databinding.ItemChapterBinding
import com.namnh.novelreaderapp.databinding.ItemStoryAdminBinding
import com.namnh.novelreaderapp.item.Chapter
import com.namnh.novelreaderapp.item.Story

class AdminChapterAdapter(private var storyOrChapterList: List<Any>, private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    Filterable {
    private var filteredList: List<Any> = storyOrChapterList

    private lateinit var binding: ItemStoryAdminBinding
    private lateinit var chapterBinding: ItemChapterBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            binding = ItemStoryAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            StoryViewHolder(binding)
        } else {
            chapterBinding = ItemChapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ChapterViewHolder(chapterBinding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is StoryViewHolder) {
            val story = filteredList[position] as Story
            holder.bind(story)

            holder.itemView.setOnClickListener {
                val intent = Intent(context, AdminChapterList::class.java)
                intent.putExtra("story_uid", story.id)
                Log.d("AdminChapterAdapter", "Story UID: ${story.id}")
                context.startActivity(intent)
            }

        } else if (holder is ChapterViewHolder) {
            val chapter = filteredList[position] as Chapter
            holder.bind(chapter)

            holder.itemView.setOnClickListener {
                val intent = Intent(context, EditChapterActivity::class.java)
                intent.putExtra("chapter_id", chapter.chapterId)
                intent.putExtra("story_id", chapter.storyId)
                intent.putExtra("chapter_title", chapter.title)
                intent.putExtra("chapter_content", chapter.content)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (filteredList[position] is Story) {
            0
        } else {
            1
        }
    }

    fun updateList(newList: List<Any>) {
        storyOrChapterList = newList
        filteredList = ArrayList(newList)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtered = mutableListOf<Any>()

                if (constraint.isNullOrBlank()) {
                    filtered.addAll(storyOrChapterList)
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    for (item in storyOrChapterList) {
                        if (item is Story && item.title.lowercase().contains(filterPattern)) {
                            filtered.add(item)
                        } else if (item is Chapter && item.title.lowercase().contains(filterPattern)) {
                            filtered.add(item)
                        }
                    }
                }

                val results = FilterResults()
                results.values = filtered
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as List<Any>? ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    inner class StoryViewHolder(private val binding: ItemStoryAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story) {
            binding.txtStoryTitle.text = story.title
            binding.txtStoryAuthor.text = story.author
            binding.txtStoryGenres.text = story.genre
        }
    }

    inner class ChapterViewHolder(private val binding: ItemChapterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chapter: Chapter) {
            binding.apply {
                txtChapterTitle.text = chapter.title
            }
        }
    }
}