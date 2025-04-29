package com.namnh.novelreaderapp.admin

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.namnh.novelreaderapp.R
import com.namnh.novelreaderapp.databinding.ActivityThemTruyenBinding
import com.namnh.novelreaderapp.item.Chapter
import com.namnh.novelreaderapp.item.Story

class ThemTruyen : AppCompatActivity() {

    private lateinit var linearGenres: Spinner

    private lateinit var binding: ActivityThemTruyenBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private var imageUri: Uri? = null

    private lateinit var progressDialog: ProgressDialog

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemTruyenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbRef = FirebaseDatabase.getInstance().getReference("stories")
        storageRef = FirebaseStorage.getInstance().getReference("cover_images")


        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Đang thêm truyện...")
        progressDialog.setCancelable(false)

        val genreOptions = resources.getStringArray(R.array.genre_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genreOptions)
        linearGenres.adapter = adapter

        binding.btnSelectImage.setOnClickListener { openFileChooser() }
        binding.btnSaveStory.setOnClickListener { saveStory() }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            binding.imageCover.setImageURI(imageUri)
        }
    }

    private fun saveStory() {
        val title = binding.editTitle.text.toString().trim()
        val author = binding.editAuthor.text.toString().trim()
        val description = binding.editDescription.text.toString().trim()
        val selectedGenre = binding.spinnerGenre.selectedItem.toString()


        if (title.isEmpty() || author.isEmpty() || description.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin và chọn ảnh", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.show()

        val storyId = dbRef.push().key

        if (storyId != null) {
            val fileRef = storageRef.child("$storyId.jpg")

            fileRef.putFile(imageUri!!)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        fileRef.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            val chapters = hashMapOf<String, Chapter>()

                            val newStory = Story(storyId, title, author, description, selectedGenre, imageUrl, chapters,)

                            dbRef.child(storyId).setValue(newStory)
                                .addOnCompleteListener { dbTask ->
                                    progressDialog.dismiss()
                                    if (dbTask.isSuccessful) {
                                        Toast.makeText(this, "Thêm truyện thành công!", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Lỗi khi lưu truyện!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    } else {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Lỗi khi tải ảnh!", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            progressDialog.dismiss()
            Toast.makeText(this, "Lỗi khi tạo ID cho truyện!", Toast.LENGTH_SHORT).show()
        }
    }
}
