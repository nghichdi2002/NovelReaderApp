package com.namnh.novelreaderapp.user

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.namnh.novelreaderapp.R
import com.namnh.novelreaderapp.databinding.FragmentHomeBinding
import com.namnh.novelreaderapp.item.Story
import com.namnh.novelreaderapp.user_adapter.GenreAdapter
import com.namnh.novelreaderapp.user_adapter.UserStoryAdapter
import com.namnh.novelreaderapp.user_adapter.ViewPagerAdapter
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var storyAdapter: UserStoryAdapter
    private val storyList = mutableListOf<Story>()
    private lateinit var viewPager2: ViewPager2
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private val imageIds = mutableListOf<Int>()
    private val sliderHandler = Handler(Looper.getMainLooper())
    private val tag = "HomeFragment"

    // Danh sách thể loại
    private val genres = listOf(
        "Võ hiệp", "Huyền Huyễn", "Lãng mạn", "Tiên Hiệp", "Đô Thị", "Hài hước", "Tự do"
    )

    // Adapter cho thể loại
    private lateinit var genreAdapter: GenreAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // --- Thiết lập RecyclerView cho Tất cả truyện (phần đã có) ---
        val recyclerView = binding.recyclerViewHome
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        storyAdapter = UserStoryAdapter(storyList, requireContext())
        recyclerView.adapter = storyAdapter

        // --- Thiết lập ViewPager2 cho slider (phần đã có) ---
        viewPager2 = binding.viewpagerHighlight
        // Thêm ID của các hình ảnh trong drawable vào danh sách
        imageIds.add(R.drawable.img1)
        imageIds.add(R.drawable.img2)
        imageIds.add(R.drawable.img3)
        imageIds.add(R.drawable.img4)
        viewPagerAdapter = ViewPagerAdapter(imageIds, requireContext())
        viewPager2.adapter = viewPagerAdapter
        sliderHandler.postDelayed(sliderRunnable, 3000)

        // --- Thiết lập RecyclerView cho Thể loại (phần mới được thêm) ---
        val genreRecyclerView = binding.rvGenres
        // Sử dụng LinearLayoutManager với orientation HORIZONTAL
        genreRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Khởi tạo GenreAdapter và gán listener
        genreAdapter = GenreAdapter(genres) { selectedGenre ->
            // Khi một thể loại được bấm, gọi hàm filterByGenre
            filterByGenre(selectedGenre)
        }
        genreRecyclerView.adapter = genreAdapter


        // Tải dữ liệu truyện
        loadStoriesFromFirebase()

        return view
    }

    private val sliderRunnable = object : Runnable {
        override fun run() {
            val currentItem = viewPager2.currentItem
            val totalItems = viewPagerAdapter.itemCount

            if (currentItem < totalItems - 1) {
                viewPager2.currentItem = currentItem + 1
            } else {
                viewPager2.currentItem = 0
            }
            sliderHandler.postDelayed(this, 3000)
        }
    }

    // Hàm xử lý khi bấm vào một thể loại (giống trong SearchFragment cũ)
    private fun filterByGenre(genre: String) {
        val intent = Intent(requireContext(), FilterResultActivity::class.java)
        intent.putExtra("genre", genre)
        startActivity(intent)
    }

    private fun loadStoriesFromFirebase() {
        // Sử dụng database mặc định mà bạn đã cung cấp thông tin: novel-reader-app-d98ed-default-rtdb
        val dbRef = FirebaseDatabase.getInstance().getReference("stories")


        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                storyList.clear()
                for (snapshot in dataSnapshot.children) {
                    val story = snapshot.getValue(Story::class.java)
                    story?.let {
                        storyList.add(it)
                        Log.d(tag, "Loaded: ${story.title}")
                    }
                }
                storyAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(tag, "Database error: ${databaseError.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sliderHandler.removeCallbacks(sliderRunnable)
        // Không cần binding = null ở đây vì bạn đang dùng lateinit
        // nếu bạn sử dụng _binding và get() thì mới cần set _binding = null
    }
}