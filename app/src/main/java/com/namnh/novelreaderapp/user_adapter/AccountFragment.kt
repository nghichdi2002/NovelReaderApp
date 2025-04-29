package com.namnh.novelreaderapp.user_adapter

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
import com.namnh.novelreaderapp.user.ChangePasswordActivity
import com.namnh.novelreaderapp.databinding.FragmentAccountBinding
import com.namnh.novelreaderapp.user.FavoriteStoriesActivity
import com.namnh.novelreaderapp.user.HistoriesActivity
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

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance()

        // Set up Image Picker Launcher
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

        // Handle profile image click
        binding.ivProfile.setOnClickListener {
            showImageOptions()
        }

        // Handle settings icon click
        binding.ivSettings.setOnClickListener {
            showSettingsMenu(it)
        }

        // Handle favorite stories button click
        binding.btnFavoriteStories.setOnClickListener {
            openFavoriteStoriesActivity()
        }

        binding.btnReadingHistory.setOnClickListener {
            openReadingHistoriesActivity()
        }

        // Display user info
        displayUserInfo()

        return view
    }

    /**
     * Show settings menu with options for changing password and logging out.
     */
    private fun showSettingsMenu(anchor: View) {
        val popupMenu = PopupMenu(requireContext(), anchor)
        popupMenu.menuInflater.inflate(R.menu.settings_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.change_password -> {
                    changePassword()
                    true
                }
                R.id.user_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    /**
     * Change password functionality.
     */
    private fun changePassword() {
        // Open activity or dialog for password change
        val intent = Intent(activity, ChangePasswordActivity::class.java)
        startActivity(intent)
    }

    /**
     * Logout functionality.
     */
    private fun logout() {
        mAuth.signOut()
        Toast.makeText(context, "Đăng xuất thành công", Toast.LENGTH_SHORT).show()
        // Navigate to login screen
        val intent = Intent(activity, Login::class.java)
        startActivity(intent)
        activity?.finish()
    }

    /**
     * Open favorite stories activity.
     */
    private fun openFavoriteStoriesActivity() {
        val intent = Intent(activity, FavoriteStoriesActivity::class.java)
        startActivity(intent)
    }

    private fun openReadingHistoriesActivity() {
        val intent = Intent(activity, HistoriesActivity::class.java)
        startActivity(intent)
    }

    /**
     * Show options for profile image.
     */
    private fun showImageOptions() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Chọn hành động")
        builder.setItems(arrayOf("Chọn ảnh từ thư viện")) { _, _ ->
            openImagePicker()
        }
        builder.show()
    }

    /**
     * Open image picker to select a profile image.
     */
    private fun openImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                launchImagePicker()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                launchImagePicker()
            }
        }
    }

    /**
     * Launch intent for image picker.
     */
    private fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }



    /**
     * Upload selected image to Firebase and update profile.
     */
    private fun uploadImageToFirebase(imageUri: Uri) {
        val currentUser = mAuth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Người dùng không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val fileRef = FirebaseStorage.getInstance().reference
            .child("users/$userId/profile.jpg")

        fileRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    // Cập nhật URL ảnh trong hồ sơ người dùng Firebase Auth
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setPhotoUri(uri)
                        .build()

                    currentUser.updateProfile(profileUpdates).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Hiển thị ảnh đại diện mới với hình tròn
                            Picasso.get().load(uri).into(binding.ivProfile)
                        }
                    }

                    // Cập nhật URL ảnh trong Realtime Database
                    val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
                    userRef.child("avatar").setValue(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Tải ảnh thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Display user information in the views.
     */
    private fun displayUserInfo() {
        val currentUser = mAuth.currentUser
        if (currentUser == null) {
            binding.tvUsername.text = "Người dùng chưa đăng nhập"
            binding.tvEmail.text = ""
            binding.ivProfile.setImageResource(R.drawable.default_avatar)
            return
        }

        val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.uid)
        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val snapshot = task.result
                val username = snapshot.child("username").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)
                val avatarUrl = snapshot.child("avatar").getValue(String::class.java)

                binding.tvUsername.text = "${username ?: "Không có tên"}"
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