package com.namnh.novelreaderapp.admin

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

    private lateinit var binding: ActivityAdminStoryDetailBinding
    private lateinit var storyRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private lateinit var storyId: String
    private var avatarUri: Uri? = null

    private var initialTitle: String = ""
    private var initialAuthor: String = ""
    private var initialGenre: String = ""
    private var initialDescription: String = ""

    private lateinit var progressDialog: ProgressDialog

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            avatarUri = uri
            binding.avatarImageView.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // hide status bar
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = ActivityAdminStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this).apply {
            setMessage("Đang lưu...")
            setCancelable(false)
        }

        storyRef = FirebaseDatabase.getInstance().getReference("stories")
        storageRef = FirebaseStorage.getInstance().getReference("avatars")

        // Nhận dữ liệu từ Intent
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

        // Hiển thị dữ liệu
        binding.editTextTitle.setText(initialTitle)
        binding.editTextAuthor.setText(initialAuthor)
        binding.editTextDescription.setText(initialDescription)
        if (!storyImageUrl.isNullOrEmpty()) {
            Picasso.get().load(storyImageUrl).into(binding.avatarImageView)
        }

        // Set up Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.genre_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerGenre.adapter = adapter
            val initialGenrePosition = adapter.getPosition(initialGenre)
            binding.spinnerGenre.setSelection(initialGenrePosition)
        }

        binding.avatarImageView.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.buttonSave.setOnClickListener { saveStoryDetails() }
        binding.buttonDelete.setOnClickListener {
            storyRef.child(storyId).removeValue()
            finish()
        }
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    override fun onBackPressed() {
        if (isStoryModified()) {
            showConfirmDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun isStoryModified(): Boolean {
        return binding.editTextTitle.text.toString() != initialTitle ||
                binding.editTextAuthor.text.toString() != initialAuthor ||
                binding.spinnerGenre.selectedItem.toString() != initialGenre ||
                binding.editTextDescription.text.toString() != initialDescription ||
                avatarUri != null
    }

    private fun showConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Thoát mà không lưu?")
            .setMessage("Bạn có muốn lưu các thay đổi trước khi thoát?")
            .setPositiveButton("Lưu và thoát") { _, _ ->
                saveStoryDetails()
                finish()
            }
            .setNegativeButton("Không lưu") { _, _ -> finish() }
            .setNeutralButton("Hủy") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun saveStoryDetails() {
        progressDialog.show()
        val updatedTitle = binding.editTextTitle.text.toString()
        val updatedAuthor = binding.editTextAuthor.text.toString()
        val updatedGenre = binding.spinnerGenre.selectedItem.toString()
        val updatedDescription = binding.editTextDescription.text.toString()

        val storyUpdates = hashMapOf<String, Any>(
            "title" to updatedTitle,
            "author" to updatedAuthor,
            "genre" to updatedGenre,
            "description" to updatedDescription
        )

        if (avatarUri != null) {
            val fileRef = storageRef.child("$storyId.jpg")
            fileRef.putFile(avatarUri!!)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        storyUpdates["imageUrl"] = uri.toString()
                        storyRef.child(storyId).updateChildren(storyUpdates)
                            .addOnCompleteListener { task -> onSaveComplete(task.isSuccessful) }
                    }.addOnFailureListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Lỗi khi lấy URL ảnh!", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
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