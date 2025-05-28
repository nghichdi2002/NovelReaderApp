package com.namnh.novelreaderapp.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.namnh.novelreaderapp.admin_adapter.ChapterAdapter
import com.namnh.novelreaderapp.databinding.ActivityAdminChapterListBinding
import com.namnh.novelreaderapp.item.Chapter

class AdminChapterList : AppCompatActivity() {
    private lateinit var binding: ActivityAdminChapterListBinding
    private lateinit var recyclerViewChapters: RecyclerView
    private lateinit var chapterAdapter: ChapterAdapter
    private var chapterList: MutableList<Chapter> = mutableListOf()
    private lateinit var btnAddChapter: FloatingActionButton  // Floating action button để thêm chương mới

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // hide status bar
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = ActivityAdminChapterListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Danh sách chương"
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        recyclerViewChapters = binding.recyclerViewChapters
        recyclerViewChapters.layoutManager = LinearLayoutManager(this)


        chapterAdapter = ChapterAdapter(chapterList, this)
        recyclerViewChapters.adapter = chapterAdapter

        val storyUid = intent.getStringExtra("story_uid")
        Log.d("ChapterListActivity", "Story UID: $storyUid")

        loadChaptersFromFirebase(storyUid)

        chapterAdapter.setOnItemClickListener(object : ChapterAdapter.OnItemClickListener {
            override fun onItemClick(chapter: Chapter, position: Int) {
                val intent = Intent(this@AdminChapterList, EditChapterActivity::class.java).apply {
                    putExtra("chapter_id", chapter.chapterId)
                    putExtra("story_id", storyUid)
                    putExtra("chapter_title", chapter.title)
                    putExtra("chapter_content", chapter.content)
                }
                startActivity(intent)
            }
        })

        btnAddChapter = binding.btnAddChapter
        btnAddChapter.setOnClickListener {
            val intent = Intent(this, AddNewChapter::class.java)
            intent.putExtra("story_uid", storyUid)
            startActivity(intent)
        }

    }

    private fun loadChaptersFromFirebase(storyUid: String?) {
        storyUid?.let { uid ->
            val dbRef =
                FirebaseDatabase.getInstance().getReference("stories").child(uid).child("chapters")

            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    chapterList.clear()
                    for (chapterSnapshot in dataSnapshot.children) {
                        val chapter = chapterSnapshot.getValue(Chapter::class.java)
//                        val chapterKey = chapterSnapshot.key

                        if (chapter != null) { //&& chapterKey != null) {
//                            chapter.uid = chapterKey
                            chapterList.add(chapter)
                        }
                    }
                    chapterAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Xử lý khi có lỗi xảy ra
                }
            })
        }
    }
}

