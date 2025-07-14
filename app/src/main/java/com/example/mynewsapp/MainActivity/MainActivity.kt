package com.example.mynewsapp.MainActivity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.mynewsapp.R
import com.example.mynewsapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(activityMainBinding.mainLayout) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(statusBar.left, statusBar.top, statusBar.right, 0)
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(activityMainBinding.bottomNav, navController)

    }
}




//        ViewCompat.setOnApplyWindowInsetsListener(activityMainBinding.bottomNav) { view, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            view.setPadding(systemBars.left, 0, systemBars.right, 0)
//            insets
//        }

//        val navController = findNavController(R.id.nav_host_fragment)
//
//        activityMainBinding.bottomNav.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.home -> navController.navigate(R.id.homeFragment)
//                R.id.search -> navController.navigate(R.id.searchFragment)
//                R.id.profile -> navController.navigate(R.id.profileFragment)
//            }
//            true
//        }