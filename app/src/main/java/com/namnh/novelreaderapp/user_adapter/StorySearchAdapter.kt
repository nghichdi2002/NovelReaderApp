package com.namnh.novelreaderapp.user_adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.namnh.novelreaderapp.R
import com.namnh.novelreaderapp.databinding.ItemStorySearchBinding
import com.namnh.novelreaderapp.item.Story
import com.namnh.novelreaderapp.user.StoryDetailActivity
import com.squareup.picasso.Picasso
import java.util.Locale

class StorySearchAdapter(
    private var originalStoryList: List<Story>, // Danh sách truyện gốc đầy đủ, KHÔNG LỌC
    private val context: Context // Context để xử lý Intent
) : RecyclerView.Adapter<StorySearchAdapter.StoryViewHolder>(), Filterable {

    // Danh sách hiện tại đang hiển thị (có thể là đã lọc)
    // Biến này sẽ được CẬP NHẬT MỖI KHI LỌC HOẶC CẬP NHẬT DANH SÁCH GỐC
    private var filteredStoryList: MutableList<Story> = ArrayList(originalStoryList) // <-- ĐÃ SỬA TÊN BIẾN CHÍNH XÁC

    // Lưu ý: Để truyền toàn bộ đối tượng Story qua Intent bằng putExtra("story", story),
    // class Story cần implements Serializable hoặc Parcelable.

    // ViewHolder cho mỗi item truyện trong danh sách tìm kiếm
    // Sử dụng Binding cho item_story_search_result.xml
    inner class StoryViewHolder(private val binding: ItemStorySearchBinding) : RecyclerView.ViewHolder(binding.root) {
        val ivCover: ImageView = binding.imageViewCover
        val tvTitle: TextView = binding.txtStoryTitle
        val tvAuthor: TextView = binding.txtStoryAuthor
        val tvGenres: TextView = binding.txtStoryGenres
        // Không có tvViews trong layout item_story_search_result.xml
    }

    // Tạo ViewHolder mới
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStorySearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    // Gán dữ liệu từ danh sách vào các View trong ViewHolder
    // Gắn dữ liệu từ filteredStoryList vì đây là danh sách đang hiển thị
    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        // Lấy Story từ danh sách đã lọc (filteredStoryList)
        val story = filteredStoryList[position] // <-- SỬ DỤNG filteredStoryList

        // Gán text cho các TextView
        holder.tvTitle.text = story.title
        holder.tvAuthor.text = "Tác giả: ${story.author}" // Thêm prefix như mẫu
        holder.tvGenres.text = "Thể loại: ${story.genre}" // Thêm prefix như mẫu

        // Load ảnh bìa sử dụng Picasso
        if (!story.imageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(story.imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder) // hoặc ic_error nếu bạn có
                .into(holder.ivCover)
        } else {
            holder.ivCover.setImageResource(R.drawable.ic_placeholder)
        }

        // Xử lý click vào item TẠI ĐÂY (như mẫu UserStoryAdapter bạn cung cấp)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, StoryDetailActivity::class.java)
            intent.putExtra("story", story) // Truyền toàn bộ đối tượng Story
            context.startActivity(intent)
        }
    }

    // Trả về tổng số item trong danh sách đang hiển thị (sau khi lọc)
    override fun getItemCount(): Int {
        return filteredStoryList.size // <-- SỬ DỤNG filteredStoryList
    }

    // Hàm này có thể dùng để cập nhật danh sách truyện ban đầu (ví dụ: sau khi tải từ Firebase)
    // Khi danh sách gốc được cập nhật, reset danh sách hiển thị bằng danh sách gốc mới
    fun updateList(newList: List<Story>) {
        originalStoryList = newList // Cập nhật tham chiếu đến danh sách gốc

        // Xóa nội dung danh sách filteredList hiện tại và thêm toàn bộ nội dung mới từ originalStoryList
        // Tạo ArrayList mới để tránh potential issues với việc clear/addAll trên List
        filteredStoryList = ArrayList(originalStoryList) // <-- CẬP NHẬT filteredStoryList

        // Thông báo cho Adapter biết toàn bộ dữ liệu đã thay đổi
        notifyDataSetChanged()
    }

    // Phương thức của Filterable để cung cấp đối tượng Filter
    override fun getFilter(): Filter {
        return storyFilter
    }

    // Đối tượng Filter tùy chỉnh để thực hiện việc lọc
    private val storyFilter = object : Filter() {
        // Thực hiện việc lọc ở Background thread
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()

            if (filterPattern.isEmpty()) {
                // Nếu query rỗng, kết quả là toàn bộ danh sách gốc
                results.values = originalStoryList // Lọc trên originalStoryList
            } else {
                // Nếu có query, lọc trên danh sách gốc originalStoryList
                val filteredListResult = ArrayList<Story>() // <-- Tên biến cục bộ OK
                for (story in originalStoryList) { // Lặp qua originalStoryList
                    val titleMatch = story.title?.lowercase(Locale.getDefault())?.contains(filterPattern) ?: false
                    val authorMatch = story.author?.lowercase(Locale.getDefault())?.contains(filterPattern) ?: false
                    val genreMatch = story.genre?.lowercase(Locale.getDefault())?.contains(filterPattern) ?: false

                    if (titleMatch || authorMatch || genreMatch) {
                        filteredListResult.add(story)
                    }
                }
                results.values = filteredListResult // Gán danh sách kết quả cục bộ
            }

            return results
        }

        // Publish kết quả lọc lên UI thread
        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            // Cập nhật danh sách hiển thị của Adapter với kết quả lọc
            // Cần kiểm tra kiểu dữ liệu an toàn và gán vào filteredStoryList
            filteredStoryList = if (results.values is List<*>) {
                results.values as MutableList<Story> // <-- CẬP NHẬT filteredStoryList
            } else {
                ArrayList()
            }
            notifyDataSetChanged() // Thông báo cho RecyclerView vẽ lại
        }
    }
}