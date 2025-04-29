package com.namnh.novelreaderapp.user_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.namnh.novelreaderapp.databinding.ItemSliderImageBinding

class ViewPagerAdapter(private val imageIds: List<Int>, private val context: Context) :
    RecyclerView.Adapter<ViewPagerAdapter.SliderViewHolder>() {

    inner class SliderViewHolder(private val binding: ItemSliderImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imageId: Int) {
            binding.imageSlider.setImageResource(imageId)  // Sử dụng binding.imageView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val itemBinding = ItemSliderImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SliderViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val imageId = imageIds[position]
        holder.bind(imageId)
    }

    override fun getItemCount(): Int {
        return imageIds.size
    }
}