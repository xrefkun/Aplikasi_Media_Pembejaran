package com.example.mediapembelajaran

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class ProfileFragment : Fragment() {

    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvLessonsReadCount: TextView
    private lateinit var tvQuizzesCount: TextView

    private lateinit var ivBadgeAuth: ImageView
    private lateinit var tvBadgeAuthStatus: TextView
    private lateinit var ivBadgeKotlin: ImageView
    private lateinit var tvBadgeKotlinStatus: TextView
    private lateinit var ivBadgeChamp: ImageView
    private lateinit var tvBadgeChampStatus: TextView

    private lateinit var btnLogout: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        tvUsername = view.findViewById(R.id.tvProfileUsername)
        tvEmail = view.findViewById(R.id.tvProfileEmail)
        tvLessonsReadCount = view.findViewById(R.id.tvProfileLessonsRead)
        tvQuizzesCount = view.findViewById(R.id.tvProfileQuizzesTaken)

        ivBadgeAuth = view.findViewById(R.id.ivBadgeAuth)
        tvBadgeAuthStatus = view.findViewById(R.id.tvBadgeAuthStatus)
        ivBadgeKotlin = view.findViewById(R.id.ivBadgeKotlin)
        tvBadgeKotlinStatus = view.findViewById(R.id.tvBadgeKotlinStatus)
        ivBadgeChamp = view.findViewById(R.id.ivBadgeChamp)
        tvBadgeChampStatus = view.findViewById(R.id.tvBadgeChampStatus)

        btnLogout = view.findViewById(R.id.btnProfileLogout)

        setupLogout()
        loadProfileData()

        return view
    }

    private fun setupLogout() {
        btnLogout.setOnClickListener {
            DatabaseHelper.signOut()
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    private fun loadProfileData() {
        // 1. Set Email from Auth
        tvEmail.text = DatabaseHelper.currentUserEmail ?: "email@example.com"

        // 2. Load Profile Username
        DatabaseHelper.getUserProfile { profile ->
            if (isAdded && profile != null) {
                tvUsername.text = profile.username
            }
        }

        // Always unlock "Local Explorer" badge since user is logged in
        unlockBadge(ivBadgeAuth, tvBadgeAuthStatus)

        // 3. Load Stats & Unlock other badges
        DatabaseHelper.getCompletedLessons { completed ->
            if (!isAdded) return@getCompletedLessons
            tvLessonsReadCount.text = completed.size.toString()

            // Unlock Kotlin Novice badge if read at least 1 lesson
            if (completed.isNotEmpty()) {
                unlockBadge(ivBadgeKotlin, tvBadgeKotlinStatus)
            }
        }

        DatabaseHelper.getQuizScores { scores ->
            if (!isAdded) return@getQuizScores
            tvQuizzesCount.text = scores.size.toString()

            // Unlock Quiz Champ badge if score is perfect
            val hasPerfectScore = scores.any { it.maxScore > 0 && it.score == it.maxScore }
            if (hasPerfectScore) {
                unlockBadge(ivBadgeChamp, tvBadgeChampStatus)
            }
        }
    }

    private fun unlockBadge(imageView: ImageView, textView: TextView) {
        imageView.alpha = 1.0f
        textView.text = "Terbuka"
        textView.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
    }
}
