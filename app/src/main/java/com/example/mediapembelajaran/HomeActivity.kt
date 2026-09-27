package com.example.mediapembelajaran

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // Keep bottom padding for nav bar custom or handle carefully
            insets
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        val role = DatabaseHelper.currentUserRole ?: "SISWA"
        val menu = bottomNavigation.menu
        
        // Dynamic menu item configuration based on role
        val itemQuiz = menu.findItem(R.id.nav_quiz)
        if (itemQuiz != null) {
            when (role) {
                "GURU" -> {
                    itemQuiz.title = "Nilai Siswa"
                    itemQuiz.setIcon(android.R.drawable.ic_menu_agenda)
                }
                "ADMIN" -> {
                    itemQuiz.title = "Pengguna"
                    itemQuiz.setIcon(android.R.drawable.ic_menu_myplaces)
                }
                else -> {
                    itemQuiz.title = "Kuis"
                    itemQuiz.setIcon(android.R.drawable.ic_menu_help)
                }
            }
        }

        val itemManageQuiz = menu.findItem(R.id.nav_manage_quiz)
        if (itemManageQuiz != null) {
            itemManageQuiz.isVisible = (role == "GURU" || role == "ADMIN")
        }

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_dashboard -> DashboardFragment()
                R.id.nav_lessons -> LessonsFragment()
                R.id.nav_quiz -> {
                    when (role) {
                        "GURU" -> TeacherScoreFragment()
                        "ADMIN" -> AdminUserFragment()
                        else -> QuizFragment()
                    }
                }
                R.id.nav_manage_quiz -> TeacherQuizListFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> DashboardFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
