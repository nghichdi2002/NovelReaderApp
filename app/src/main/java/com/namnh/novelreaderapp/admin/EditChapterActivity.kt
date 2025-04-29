package com.namnh.novelreaderapp.admin

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.firebase.database.FirebaseDatabase
import com.namnh.novelreaderapp.R
import com.namnh.novelreaderapp.databinding.ActivityChapterEditBinding
import com.namnh.novelreaderapp.item.Chapter

class EditChapterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChapterEditBinding
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button
    private lateinit var edtTitle: EditText
    private lateinit var edtContent: EditText
    private var chapterId: String? = null
    private var storyId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChapterEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        btnSave = binding.btnSaveChapter
        btnDelete = binding.btnDeleteChapter
        edtTitle = binding.edtChapterTitle
        edtContent = binding.edtChapterContent

        chapterId = intent.getStringExtra("chapter_id")
        storyId = intent.getStringExtra("story_id")
        edtTitle.setText(intent.getStringExtra("chapter_title"))
        edtContent.setText(intent.getStringExtra("chapter_content"))

        btnSave.setOnClickListener { updateChapter() }
        btnDelete.setOnClickListener { deleteChapter() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateChapter() {
        val title = edtTitle.text.toString()
        val content = edtContent.text.toString()

        if (chapterId == null || storyId == null) {
            Toast.makeText(this, "ID chương hoặc ID truyện không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        val dbRef = FirebaseDatabase.getInstance()
            .getReference("stories")
            .child(storyId!!)
            .child("chapters")
            .child(chapterId!!)

        val updatedChapter = Chapter(title, content, chapterId!!, storyId!!)

        dbRef.setValue(updatedChapter)
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật chương thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("EditChapterActivity", "Error updating chapter", e)
            }
    }

    private fun deleteChapter() {
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("stories")
            .child(storyId!!)
            .child("chapters")
            .child(chapterId!!)

        dbRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Xóa chương thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("EditChapterActivity", "Error deleting chapter", e)
            }
    }
}