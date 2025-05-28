package com.namnh.novelreaderapp.admin

import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.namnh.novelreaderapp.Login
import com.namnh.novelreaderapp.R

import com.namnh.novelreaderapp.admin_adapter.AdminPagerAdapter
import com.namnh.novelreaderapp.databinding.ActivityAdminBinding

@Suppress("DEPRECATION")
class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var adminPagerAdapter: AdminPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        // hide status bar
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Thiết lập Toolbar
        val toolbar: Toolbar = binding.adminToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Quản lý Truyện"

        // Thiết lập TabLayout và ViewPager
        tabLayout = binding.adminTabLayout
        viewPager = binding.adminViewPager

        // Adapter cho ViewPager
        adminPagerAdapter = AdminPagerAdapter(supportFragmentManager)
        viewPager.adapter = adminPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.admin_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                logout()  // Gọi phương thức đăng xuất
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logout() {

        FirebaseAuth.getInstance().signOut()  // Đăng xuất khỏi Firebase

        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()  // Kết thúc Activity hiện tại
    }
}