package com.namnh.novelreaderapp.user

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.namnh.novelreaderapp.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // hide status bar
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        // Back button functionality
        binding.buttonBack.setOnClickListener { finish() }

        // Change password button functionality
        binding.changePasswordButton.setOnClickListener { changePassword() }
    }

    private fun changePassword() {
        val oldPassword = binding.inputOldPass.text.toString().trim()
        val newPassword = binding.inputNewPass.text.toString().trim()
        val confirmPassword = binding.inputConfirmNewPass.text.toString().trim()

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Làm ơn nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            binding.changePasswordResult.text = "Mật khẩu không khớp."
            return
        }

        val user: FirebaseUser? = mAuth.currentUser
        if (user != null) {
            // Re-authenticate the user with the old password before changing it
            user.email?.let { email ->
                mAuth.signInWithEmailAndPassword(email, oldPassword).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Change the user's password
                        user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                binding.changePasswordResult.text = "Đổi mật khẩu thành công!"
                                binding.changePasswordResult.setTextColor(getColor(android.R.color.black))
                                finish()
                            } else {
                                binding.changePasswordResult.text = "Đổi mật khẩu thất bại: ${updateTask.exception?.message}"
                            }
                        }
                    } else {
                        binding.changePasswordResult.text = "Mật khẩu cũ không chính xác."
                    }
                }
            }
        } else {
            //
        }
    }
}