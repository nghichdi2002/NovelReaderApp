package com.namnh.novelreaderapp.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.namnh.novelreaderapp.databinding.FragmentSearchBinding
import com.namnh.novelreaderapp.item.Story
import com.namnh.novelreaderapp.user_adapter.StorySearchAdapter


class SearchFragment : Fragment() {
    // View Binding
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    // Danh sách chứa tất cả truyện được tải từ Firebase
    private val storyList = mutableListOf<Story>()

    // Adapter cho RecyclerView
    private lateinit var storyAdapter: StorySearchAdapter

    // Lưu lại listener để remove sau này
    private var storyListener: ValueEventListener? = null
    private val dbRef by lazy { FirebaseDatabase.getInstance().getReference("stories") }

    private val TAG = "SearchFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Thiết lập Adapter và RecyclerView
        storyAdapter = StorySearchAdapter(ArrayList(), requireContext())
        binding.rvStories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStories.adapter = storyAdapter

        Log.d(TAG, "RecyclerView adapter set.")

        // 2. Tải dữ liệu truyện từ Firebase Realtime Database
        loadStoriesFromFirebase()

        // 3. Thiết lập lắng nghe sự kiện gõ phím trên EditText
        setupSearchListener()

    }

    // Hàm tải tất cả truyện từ Firebase
    private fun loadStoriesFromFirebase() {
        // Nếu đã có listener cũ thì remove trước
        storyListener?.let {
            dbRef.removeEventListener(it)
            storyListener = null
        }

        // Khởi tạo listener mới
        storyListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                storyList.clear()
                Log.d(TAG, "Cleared local storyList. Found ${dataSnapshot.childrenCount} children in snapshot.")

                for (snapshot in dataSnapshot.children) {
                    try {
                        val story = snapshot.getValue(Story::class.java)
                        story?.let {
                            storyList.add(it)
                        } ?: run {
                            Log.w(TAG, "Failed to parse snapshot to Story: ${snapshot.key}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing story with key ${snapshot.key}: ${e.message}", e)
                    }
                }

                // Chỉ cập nhật UI nếu view còn sống
                _binding?.let { binding ->
                    storyAdapter.updateList(storyList)
                    Log.d(TAG, "Loaded ${storyList.size} stories from Firebase and updated adapter.")

                    // Nếu có query sẵn trong ô tìm kiếm khi dữ liệu tải xong
                    val currentQuery = binding.etSearch.text.toString()
                    if (currentQuery.isNotEmpty()) {
                        Log.d(TAG, "Filtering again with existing query: $currentQuery")
                        storyAdapter.filter.filter(currentQuery)
                    }
                } ?: run {
                    Log.w(TAG, "onDataChange called but binding is null (fragment view destroyed).")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Database error: ${databaseError.message}", databaseError.toException())
            }
        }
        dbRef.addValueEventListener(storyListener!!)
    }

    // Thiết lập lắng nghe sự kiện gõ phím trên EditText
    private fun setupSearchListener() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                storyAdapter.filter.filter(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        Log.d(TAG, "Search text listener setup.")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Gỡ adapter khỏi RecyclerView
        _binding?.rvStories?.adapter = null

        // Gỡ bỏ listener Firebase để callback không gọi về fragment đã destroy
        storyListener?.let {
            dbRef.removeEventListener(it)
            storyListener = null
            Log.d(TAG, "Firebase ValueEventListener removed.")
        }

        // Giải phóng binding
        _binding = null
        Log.d(TAG, "onDestroyView: binding set to null.")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Fragment is being destroyed.")
    }
}