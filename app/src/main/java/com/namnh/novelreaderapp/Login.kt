package com.namnh.novelreaderapp

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.namnh.novelreaderapp.admin.AdminActivity
import com.namnh.novelreaderapp.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference


        binding.apply {
            signUpRedirect.setOnClickListener {
                val intent = Intent(this@Login, SignUp::class.java)
                startActivity(intent)
            }

            signInButton.setOnClickListener {
                loginUser()
            }
        }
    }

    private fun loginUser() {
        val email = binding.inputUsername.text.toString().trim()
        val password = binding.inputPass.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this@Login, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = mAuth.currentUser
                    if (user != null) {
                        mDatabase.child("users").child(user.uid).get().addOnCompleteListener { databaseTask ->
                            if (databaseTask.isSuccessful) {
                                val snapshot: DataSnapshot = databaseTask.result
                                if (snapshot.exists()) {
                                    val role = snapshot.child("role").getValue(String::class.java)
                                    val isActive = snapshot.child("isActive").getValue(Boolean::class.java)

                                    if (isActive == null || isActive) {
                                        val intent: Intent
                                        if ("admin" == role) {
                                            Toast.makeText(this@Login, "Đăng nhập với quyền quản trị!", Toast.LENGTH_SHORT).show()
                                            intent = Intent(this@Login, AdminActivity::class.java)
                                        } else {
                                            Toast.makeText(this@Login, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                            intent = Intent(this@Login, MainActivity::class.java)
                                        }
                                        startActivity(intent)
                                        finish()

                                    }
                                } else {
                                    Toast.makeText(this@Login, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@Login, "Lỗi khi truy vấn dữ liệu người dùng: " + databaseTask.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Đăng nhập thất bại"
                    Toast.makeText(this@Login, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

}