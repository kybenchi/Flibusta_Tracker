package com.yuk.flibustatracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yuk.flibustatracker.ui.AboutFragment
import com.yuk.flibustatracker.ui.AuthorsFragment
import com.yuk.flibustatracker.ui.BooksFragment
import com.yuk.flibustatracker.ui.SettingsFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nav = findViewById<BottomNavigationView>(R.id.bottomNav)

        if (savedInstanceState == null) {
            openFragment(AuthorsFragment())
        }

        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_authors -> openFragment(AuthorsFragment())
                R.id.nav_books -> openFragment(BooksFragment())
                R.id.nav_settings -> openFragment(SettingsFragment())
                R.id.nav_about -> openFragment(AboutFragment())
            }
            true
        }
    }

    private fun openFragment(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, f)
            .commit()
    }
}