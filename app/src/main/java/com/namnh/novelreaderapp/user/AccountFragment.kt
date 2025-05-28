package com.namnh.novelreaderapp.user

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.namnh.novelreaderapp.Login
import com.namnh.novelreaderapp.R
import com.namnh.novelreaderapp.databinding.FragmentAccountBinding
import com.squareup.picasso.Picasso


class AccountFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAuth: FirebaseAuth
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val view = binding.root

        mAuth = FirebaseAuth.getInstance()

        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    uploadImageToFirebase(imageUri)
                } else {
                    Toast.makeText(context, "Không chọn ảnh", Toast.LENGTH_SHORT).show()
                }
            }
        }

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launchImagePicker()
            } else {
                Toast.makeText(requireContext(), "Bạn cần chấp nhận quyền để chọn ảnh", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivProfile.setOnClickListener { showImageOptions() }
        binding.ivSettings.setOnClickListener { showSettingsMenu(it) }
        binding.btnFavoriteStories.setOnClickListener { openFavoriteStoriesActivity() }
        binding.btnReadingHistory.setOnClickListener { openReadingHistoriesActivity() }

        displayUserInfo()
        return view
    }

    private fun showSettingsMenu(anchor: View) {
        PopupMenu(requireContext(), anchor).apply {
            menuInflater.inflate(R.menu.settings_menu, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.change_password -> {
                        startActivity(Intent(activity, ChangePasswordActivity::class.java))
                        true
                    }
                    R.id.user_logout -> {
                        mAuth.signOut()
                        Toast.makeText(context, "Đăng xuất thành công", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(activity, Login::class.java))
                        activity?.finish()
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun openFavoriteStoriesActivity() {
        startActivity(Intent(activity, FavoriteStoriesActivity::class.java))
    }

    private fun openReadingHistoriesActivity() {
        startActivity(Intent(activity, HistoriesActivity::class.java))
    }

    private fun showImageOptions() {
        AlertDialog.Builder(requireContext())
            .setTitle("Chọn hành động")
            .setItems(arrayOf("Chọn ảnh từ thư viện")) { _, _ -> openImagePicker() }
            .show()
    }

    private fun openImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            } else { launchImagePicker() }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else { launchImagePicker() }
        }
    }

    private fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val userId = mAuth.currentUser!!.uid
        val fileRef = FirebaseStorage.getInstance().reference.child("users/$userId/profile.jpg")

        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setPhotoUri(uri)
                        .build()
                    mAuth.currentUser!!.updateProfile(profileUpdates).addOnCompleteListener {
                        Picasso.get().load(uri).into(binding.ivProfile)
                    }
                    FirebaseDatabase.getInstance().getReference("users")
                        .child(userId).child("avatar").setValue(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Tải ảnh thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayUserInfo() {
        val currentUser = mAuth.currentUser!!
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.uid)
        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val snapshot = task.result
                val username = snapshot.child("username").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)
                val avatarUrl = snapshot.child("avatar").getValue(String::class.java)

                binding.tvUsername.text = username ?: "Không có tên"
                binding.tvEmail.text = "Email: ${email ?: "Không có email"}"
                if (avatarUrl != null) {
                    Picasso.get().load(avatarUrl).into(binding.ivProfile)
                } else {
                    binding.ivProfile.setImageResource(R.drawable.default_avatar)
                }
            } else {
                Toast.makeText(context, "Lỗi khi tải thông tin người dùng", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}