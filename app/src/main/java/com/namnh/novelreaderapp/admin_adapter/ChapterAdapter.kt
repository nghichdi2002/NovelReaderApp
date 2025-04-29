package com.namnh.novelreaderapp.admin_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.namnh.novelreaderapp.databinding.ItemChapterBinding
import com.namnh.novelreaderapp.item.Chapter

class ChapterAdapter(private val chapterList: List<Chapter>, private val context: Context) :
    RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    // Interface cho sự kiện nhấn vào một item
    interface OnItemClickListener {
        fun onItemClick(chapter: Chapter, position: Int)
    }

    // Phương thức để thiết lập sự kiện nhấn
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val binding = ItemChapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val chapter = chapterList[position]
        holder.bind(chapter)
    }

    override fun getItemCount(): Int {
        return chapterList.size
    }

    inner class ChapterViewHolder(private val binding: ItemChapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val chapter = chapterList[position]
                    onItemClickListener?.onItemClick(chapter, position)
                }
            }
        }

        fun bind(chapter: Chapter) {
            binding.txtChapterTitle.text = chapter.title
            binding.txtChapterAuthor.text = chapter.author
            binding.txtChapterGenre.text = chapter.genre
        }
    }
}