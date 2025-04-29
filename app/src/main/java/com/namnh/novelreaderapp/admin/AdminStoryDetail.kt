package com.namnh.novelreaderapp.admin

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.namnh.novelreaderapp.R
import com.namnh.novelreaderapp.databinding.ActivityAdminStoryDetailBinding
import com.squareup.picasso.Picasso

class AdminStoryDetail : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var authorEditText: EditText
    private lateinit var linearGenre: Spinner
    private lateinit var descriptionEditText: EditText
    private lateinit var avatarImageView: ImageView
    private lateinit var btnBack: ImageButton
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button

    private lateinit var binding: ActivityAdminStoryDetailBinding
    private lateinit var storyRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private lateinit var storyId: String
    private var avatarUri: Uri? = null

    //bien luu du lieu ban dau
    private var initialTitle: String = ""
    private var initialAuthor: String = ""
    private var initialGenre: String = ""
    private var initialDescription: String = ""

    private lateinit var progressDialog: ProgressDialog

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Đang lưu...")
        progressDialog.setCancelable(false)

        // Initialize Firebase references
        storyRef = FirebaseDatabase.getInstance().getReference("stories")
        storageRef = FirebaseStorage.getInstance().getReference("avatars")

        titleEditText = binding.editTextTitle
        authorEditText = binding.editTextAuthor
        linearGenre = binding.spinnerGenre
        descriptionEditText = binding.editTextDescription
        avatarImageView = binding.avatarImageView
        btnBack = binding.btnBack
        saveButton = binding.buttonSave
        deleteButton = binding.buttonDelete

        // Receive intent data
        storyId = intent.getStringExtra("story_id") ?: ""
        if (storyId.isEmpty()) {
            Log.e("AdminStoryDetail", "Story ID is null")
            finish()
            return
        }

        initialTitle = intent.getStringExtra("story_title") ?: ""
        initialAuthor = intent.getStringExtra("story_author") ?: ""
        initialGenre = intent.getStringExtra("story_genre") ?: ""
        initialDescription = intent.getStringExtra("story_description") ?: ""
        val storyImageUrl = intent.getStringExtra("story_image_url")

        // Hien thi data
        titleEditText.setText(initialTitle)
        authorEditText.setText(initialAuthor)
        descriptionEditText.setText(initialDescription)

        // Load anh bang pikachu
        if (!storyImageUrl.isNullOrEmpty()) {
            Picasso.get().load(storyImageUrl).into(binding.avatarImageView)
        }

        // Set click listeners using ViewBinding
        binding.avatarImageView.setOnClickListener { openFileChooser() }
        binding.buttonSave.setOnClickListener { saveStoryDetails() }
        binding.buttonDelete.setOnClickListener {
            storyRef.child(storyId).removeValue()
            finish()
        }
        binding.btnBack.setOnClickListener { onBackPressed() }

        // Thiết lập Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.genre_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            linearGenre.adapter = adapter

            // Lấy vị trí của thể loại ban đầu trong mảng genre_options
            val initialGenrePosition = adapter.getPosition(initialGenre)
            linearGenre.setSelection(initialGenrePosition)
        }
    }

    override fun onBackPressed() {
        if (isStoryModified()) {
            // Show confirmation dialog if changes are made
            showConfirmDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh bìa"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            avatarUri = data.data
            binding.avatarImageView.setImageURI(avatarUri)
        }
    }

    private fun isStoryModified(): Boolean {
        return titleEditText.text.toString() != initialTitle ||
                authorEditText.text.toString() != initialAuthor ||
                linearGenre.selectedItem.toString() != initialGenre ||
                descriptionEditText.text.toString() != initialDescription ||
                avatarUri != null
    }

    private fun showConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Thoát mà không lưu?")
            .setMessage("Bạn có muốn lưu các thay đổi trước khi thoát?")
            .setPositiveButton("Lưu và thoát") { dialog, which ->
                saveStoryDetails()
                finish()
            }
            .setNegativeButton("Không lưu") { dialog, which -> finish() }
            .setNeutralButton("Hủy") { dialog, which -> dialog.dismiss() }
            .show()
    }

    private fun saveStoryDetails() {
        progressDialog.show()

        val updatedTitle = titleEditText.text.toString()
        val updatedAuthor = authorEditText.text.toString()
        val updatedGenre = linearGenre.selectedItem.toString()
        val updatedDescription = descriptionEditText.text.toString()

        val storyUpdates = hashMapOf<String, Any>()
        storyUpdates["title"] = updatedTitle
        storyUpdates["author"] = updatedAuthor
        storyUpdates["genre"] = updatedGenre
        storyUpdates["description"] = updatedDescription

        if (avatarUri != null) {
            val fileRef = storageRef.child("$storyId.jpg")
            fileRef.putFile(avatarUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        storyUpdates["imageUrl"] = uri.toString()
                        storyRef.child(storyId).updateChildren(storyUpdates)
                            .addOnCompleteListener { task -> onSaveComplete(task.isSuccessful) }
                    }.addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(this, "Lỗi khi lấy URL ảnh!", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Lỗi khi tải ảnh lên!", Toast.LENGTH_SHORT).show()
                }
        } else {
            storyRef.child(storyId).updateChildren(storyUpdates)
                .addOnCompleteListener { task -> onSaveComplete(task.isSuccessful) }
        }
    }

    private fun onSaveComplete(success: Boolean) {
        progressDialog.dismiss()
        if (success) {
            Toast.makeText(this, "Lưu thành công!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Lỗi khi lưu!", Toast.LENGTH_SHORT).show()
        }
    }
}