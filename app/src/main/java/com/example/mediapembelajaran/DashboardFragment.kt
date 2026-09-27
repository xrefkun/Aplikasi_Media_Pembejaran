package com.example.mediapembelajaran

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.math.roundToInt

class DashboardFragment : Fragment() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvProgressTitle: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvProgressCount: TextView
    private lateinit var pbLearningProgress: ProgressBar
    private lateinit var tvStat1Label: TextView
    private lateinit var tvQuizCount: TextView
    private lateinit var tvStat2Label: TextView
    private lateinit var tvAverageScore: TextView
    private lateinit var tvAction1Title: TextView
    private lateinit var tvAction1Desc: TextView
    private lateinit var tvAction2Title: TextView
    private lateinit var tvAction2Desc: TextView
    private lateinit var btnQuickStudy: View
    private lateinit var btnQuickQuiz: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvProgressTitle = view.findViewById(R.id.tvProgressTitle)
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent)
        tvProgressCount = view.findViewById(R.id.tvProgressCount)
        pbLearningProgress = view.findViewById(R.id.pbLearningProgress)
        tvStat1Label = view.findViewById(R.id.tvStat1Label)
        tvQuizCount = view.findViewById(R.id.tvQuizCount)
        tvStat2Label = view.findViewById(R.id.tvStat2Label)
        tvAverageScore = view.findViewById(R.id.tvAverageScore)
        tvAction1Title = view.findViewById(R.id.tvAction1Title)
        tvAction1Desc = view.findViewById(R.id.tvAction1Desc)
        tvAction2Title = view.findViewById(R.id.tvAction2Title)
        tvAction2Desc = view.findViewById(R.id.tvAction2Desc)
        btnQuickStudy = view.findViewById(R.id.btnQuickStudy)
        btnQuickQuiz = view.findViewById(R.id.btnQuickQuiz)

        setupNavigationShortcuts()
        loadUserData()

        return view
    }

    private fun setupNavigationShortcuts() {
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)

        btnQuickStudy.setOnClickListener {
            bottomNav?.selectedItemId = R.id.nav_lessons
        }

        btnQuickQuiz.setOnClickListener {
            bottomNav?.selectedItemId = R.id.nav_quiz
        }
    }

    private fun loadUserData() {
        val role = DatabaseHelper.currentUserRole ?: "SISWA"

        // 1. Fetch User Profile for greeting
        DatabaseHelper.getUserProfile { profile ->
            if (isAdded && profile != null) {
                val roleTitle = when (role) {
                    "GURU" -> " (Guru)"
                    "ADMIN" -> " (Admin)"
                    else -> ""
                }
                tvGreeting.text = "Halo, ${profile.username}$roleTitle!"
            }
        }

        when (role) {
            "GURU" -> {
                // Guru Dashboard
                tvProgressTitle.text = "Statistik Materi"
                pbLearningProgress.visibility = View.GONE

                tvStat1Label.text = "Total Siswa"
                tvStat2Label.text = "Total Nilai Masuk"

                tvAction1Title.text = "Kelola Materi"
                tvAction1Desc.text = "Tambah, edit, atau hapus materi belajar pemrograman mobile."
                tvAction2Title.text = "Lihat Nilai Siswa"
                tvAction2Desc.text = "Pantau perkembangan dan nilai kuis seluruh siswa."

                // Load Guru Statistics
                DatabaseHelper.getLessons { lessons ->
                    if (!isAdded) return@getLessons
                    tvProgressPercent.text = "${lessons.size} Materi"
                    tvProgressCount.text = "Materi pelajaran aktif dalam sistem"
                }

                DatabaseHelper.getAllUsers { users ->
                    if (!isAdded) return@getAllUsers
                    val studentCount = users.count { it.role == "SISWA" }
                    tvQuizCount.text = studentCount.toString()
                }

                DatabaseHelper.getStudentScores { scores ->
                    if (!isAdded) return@getStudentScores
                    tvAverageScore.text = scores.size.toString()
                }
            }
            "ADMIN" -> {
                // Admin Dashboard
                tvProgressTitle.text = "Statistik Pengguna"
                pbLearningProgress.visibility = View.GONE

                tvStat1Label.text = "Total Guru"
                tvStat2Label.text = "Total Materi"

                tvAction1Title.text = "Kelola Materi"
                tvAction1Desc.text = "Tambah, edit, atau hapus materi belajar pemrograman mobile."
                tvAction2Title.text = "Kelola Pengguna"
                tvAction2Desc.text = "Atur peran pengguna dan bersihkan akun dalam sistem."

                // Load Admin Statistics
                DatabaseHelper.getAllUsers { users ->
                    if (!isAdded) return@getAllUsers
                    tvProgressPercent.text = "${users.size} Pengguna"
                    tvProgressCount.text = "Total seluruh pengguna terdaftar"

                    val teacherCount = users.count { it.role == "GURU" }
                    tvQuizCount.text = teacherCount.toString()
                }

                DatabaseHelper.getLessons { lessons ->
                    if (!isAdded) return@getLessons
                    tvAverageScore.text = lessons.size.toString()
                }
            }
            else -> {
                // Siswa Dashboard (Standard)
                tvProgressTitle.text = "Progres Belajar Anda"
                pbLearningProgress.visibility = View.VISIBLE
                
                tvStat1Label.text = "Kuis Diikuti"
                tvStat2Label.text = "Rata-rata Skor"

                tvAction1Title.text = "Mulai Belajar"
                tvAction1Desc.text = "Lanjutkan membaca materi pemrograman mobile."
                tvAction2Title.text = "Tantangan Kuis"
                tvAction2Desc.text = "Uji pemahaman Anda dengan mengerjakan kuis interaktif."

                // 2. Fetch completed lessons count and calculate progress percentage
                DatabaseHelper.getLessons { lessons ->
                    DatabaseHelper.getCompletedLessons { completedList ->
                        if (!isAdded) return@getCompletedLessons

                        val totalLessons = lessons.size
                        val completedCount = completedList.size

                        val percent = if (totalLessons > 0) {
                            ((completedCount.toFloat() / totalLessons.toFloat()) * 100).roundToInt()
                        } else {
                            0
                        }

                        tvProgressPercent.text = "$percent% Selesai"
                        pbLearningProgress.progress = percent
                        tvProgressCount.text = "$completedCount dari $totalLessons materi dibaca"
                    }
                }

                // 3. Fetch quiz scores and calculate statistics
                DatabaseHelper.getQuizScores { scoreList ->
                    if (!isAdded) return@getQuizScores

                    val quizCount = scoreList.size
                    tvQuizCount.text = quizCount.toString()

                    if (quizCount > 0) {
                        var totalPct = 0f
                        for (score in scoreList) {
                            if (score.maxScore > 0) {
                                totalPct += (score.score.toFloat() / score.maxScore.toFloat()) * 100
                            }
                        }
                        val avg = (totalPct / quizCount).roundToInt()
                        tvAverageScore.text = "$avg%"
                    } else {
                        tvAverageScore.text = "N/A"
                    }
                }
            }
        }
    }
}

