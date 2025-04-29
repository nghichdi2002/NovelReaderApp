package com.namnh.novelreaderapp.user

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.namnh.novelreaderapp.R
import com.namnh.novelreaderapp.databinding.ActivityFavoriteStoriesBinding
import com.namnh.novelreaderapp.item.Chapter
import com.namnh.novelreaderapp.item.FavoriteStory
import com.namnh.novelreaderapp.item.Story
import com.namnh.novelreaderapp.user_adapter.FavoriteStoryAdapter

class FavoriteStoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteStoriesBinding
    private lateinit var favoriteStoryAdapter: FavoriteStoryAdapter
    private val favoriteStoryList = ArrayList<FavoriteStory>()
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFavoriteStoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageViewBack.setOnClickListener { onBackPressed() }

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        binding.recyclerViewFavoriteStories.layoutManager = LinearLayoutManager(this)
        favoriteStoryAdapter = FavoriteStoryAdapter(favoriteStoryList, this)
        binding.recyclerViewFavoriteStories.adapter = favoriteStoryAdapter

        currentUser?.let {
            val uid = it.uid
            mDatabase = FirebaseDatabase.getInstance().getReference("users").child(uid).child("favorites")
            loadFavoriteStories()
        }
    }

    private fun loadFavoriteStories() {
        mDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                favoriteStoryList.clear()
                for (snapshot in dataSnapshot.children) {
                    val storyId = snapshot.key
                    FirebaseDatabase.getInstance().getReference("stories").child(storyId!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(storySnapshot: DataSnapshot) {
                                if (storySnapshot.exists()) {
                                    val title = storySnapshot.child("title").getValue(String::class.java)?: ""
                                    val author = storySnapshot.child("author").getValue(String::class.java)?: ""
                                    val description = storySnapshot.child("description").getValue(String::class.java)?: ""
                                    val imageUrl = storySnapshot.child("imageUrl").getValue(String::class.java)?: ""
                                    val genre = storySnapshot.child("genre").getValue(String::class.java)?: ""

                                    val chapters = HashMap<String, Chapter>()
                                    for (chapterSnapshot in storySnapshot.child("chapters").children) {
                                        val chapter = chapterSnapshot.getValue(Chapter::class.java)
                                        chapters[chapterSnapshot.key!!] = chapter!!
                                    }

                                    val story = Story(storyId, title, author, description, genre, imageUrl, chapters)
                                    val favoriteStory = FavoriteStory(storyId, title, imageUrl, 0L, chapters.size.toLong(), story)

                                    favoriteStoryList.add(favoriteStory)
                                    favoriteStoryAdapter.notifyDataSetChanged()
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Xử lý lỗi truy vấn
                            }
                        })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Xử lý lỗi truy vấn
            }
        })
    }
}