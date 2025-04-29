package com.namnh.novelreaderapp

import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.namnh.novelreaderapp.databinding.ActivityMainBinding
import com.namnh.novelreaderapp.user_adapter.AccountFragment

import com.namnh.novelreaderapp.user_adapter.HomeFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        navigationView = binding.bottomNavigation

        // Set initial fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.body_container, HomeFragment())
                .commit()
        }
        navigationView.selectedItemId = R.id.nav_home

        // Handle bottom navigation item selection
        navigationView.setOnNavigationItemSelectedListener { item ->
            var fragment: Fragment? = null

            when (item.itemId) {
                R.id.nav_home -> fragment = HomeFragment()
//                R.id.nav_theloai -> fragment = HistoryFragment()
                R.id.nav_account -> fragment = AccountFragment()
            }

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.body_container, it)
                    .addToBackStack(null)
                    .commit()
            }

            true
        }
    }
}