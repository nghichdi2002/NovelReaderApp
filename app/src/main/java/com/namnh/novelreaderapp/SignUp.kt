package com.namnh.novelreaderapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.google.firebase.storage.storage
import com.namnh.novelreaderapp.databinding.ActivitySignUpBinding

class SignUp : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivitySignUpBinding

    //firebase auth
    private lateinit var mauth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialize Firebase Auth
        mauth = Firebase.auth
        mDatabase = Firebase.database.reference

        binding.apply {
            inputUsername.requestFocus()
            inputPass.requestFocus()
            // Xử lý khi người dùng nhấn nút đăng ký
            signUpButton.setOnClickListener {
                registerUser()
            }
            // Chuyển hướng về màn hình đăng nhập nếu người dùng đã có tài khoản
            alreadyHaveAccount.setOnClickListener {
                finish()
            }
        }
    }
    // Phương thức xử lý đăng ký người dùng mới
    private fun registerUser() {
        val username = binding.inputUsername.text.toString().trim()
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPass.text.toString().trim()
        val confirmPassword = binding.inputConfirmPass.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this@SignUp, "Vui lòng nhập đầy đủ thông tin.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this@SignUp, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
            return
        }

        mauth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mauth.currentUser
                    if (user != null) {
                        val defaultAvatarUrl = "https://firebasestorage.googleapis.com/v0/b/novel-reader-app-d98ed.firebasestorage.app/o/Logo%20maker%20project.png?alt=media&token=aa08b980-135e-41fe-b22d-1af8e7fa34ae"

                        val userProfile = hashMapOf(
                            "uid" to user.uid,
                            "username" to username,
                            "email" to email,
                            "avatar" to defaultAvatarUrl,
                            "role" to "user",
                            "favorites" to arrayListOf<String>(),
                            "reading_progress" to hashMapOf<String, Any>()
                        )

                        mDatabase.child("users").child(user.uid).setValue(userProfile)
                            .addOnCompleteListener { databaseTask ->
                                if (databaseTask.isSuccessful) {
                                    Toast.makeText(this@SignUp, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                                    mauth.signOut()
                                    val intent = Intent(this@SignUp, Login::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this@SignUp, "Lưu thông tin người dùng thất bại: " + databaseTask.exception?.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }

            }
    }
}



