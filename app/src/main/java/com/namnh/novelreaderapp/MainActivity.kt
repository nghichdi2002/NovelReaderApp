package com.namnh.novelreaderapp

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.namnh.novelreaderapp.databinding.ActivityMainBinding
import com.namnh.novelreaderapp.user.AccountFragment

import com.namnh.novelreaderapp.user.HomeFragment
import com.namnh.novelreaderapp.user.SearchFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // An status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        navigationView = binding.bottomNavigation

        // Set initial fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.body_container, HomeFragment())
                .commit()
        }
        navigationView.selectedItemId = R.id.nav_home

        // Xu ly bottom nav
        navigationView.setOnItemSelectedListener { menuItem ->
            var fragment: Fragment? = null

            when (menuItem.itemId) {
                R.id.nav_home -> fragment = HomeFragment()
                R.id.nav_search -> fragment = SearchFragment()
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