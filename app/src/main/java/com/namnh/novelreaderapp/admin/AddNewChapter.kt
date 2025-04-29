package com.namnh.novelreaderapp.admin

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.namnh.novelreaderapp.databinding.ActivityAddNewChapterBinding
import com.namnh.novelreaderapp.item.Chapter

class AddNewChapter : AppCompatActivity() {
    private lateinit var binding: ActivityAddNewChapterBinding
    private lateinit var edtChapterTitle: EditText
    private lateinit var edtChapterContent: EditText
    private lateinit var btnAddChapter: Button
    private var storyUid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewChapterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        edtChapterTitle = binding.edtChapterTitle
        edtChapterContent = binding.edtChapterContent
        btnAddChapter = binding.btnAddChapter

        storyUid = intent.getStringExtra("story_uid") ?: ""

        btnAddChapter.setOnClickListener {
            val title = edtChapterTitle.text.toString().trim()
            val content = edtChapterContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
            } else {
                addChapterToFirebase(title, content)
            }
        }
    }

    private fun addChapterToFirebase(title: String, content: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("stories").child(storyUid).child("chapters")

        val chapterId = dbRef.push().key

        if (chapterId != null) {
            val chapter = Chapter(title, content, chapterId, storyUid)

            dbRef.child(chapterId).setValue(chapter)
                .addOnSuccessListener {
                    Toast.makeText(this, "Thêm chương thành công", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e("AddChapterActivity", "Lỗi thêm chương", e)
                }
        } else {
            Log.e("AddChapterActivity", "Lỗi get id chương")
        }
    }
}