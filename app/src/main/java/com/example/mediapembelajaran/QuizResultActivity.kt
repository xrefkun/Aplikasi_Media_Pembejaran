package com.example.mediapembelajaran

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.roundToInt

class QuizResultActivity : AppCompatActivity() {

    private lateinit var category: String
    private var score = 0
    private var totalQuestions = 0

    private lateinit var ivIllustration: ImageView
    private lateinit var tvHeader: TextView
    private lateinit var tvSub: TextView
    private lateinit var tvPercent: TextView
    private lateinit var tvFraction: TextView
    private lateinit var tvCorrectCount: TextView
    private lateinit var tvWrongCount: TextView
    private lateinit var btnRetry: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_quiz_result)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.quiz_result_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        category = intent.getStringExtra("QUIZ_CATEGORY") ?: "Kotlin Basics"
        score = intent.getIntExtra("QUIZ_SCORE", 0)
        totalQuestions = intent.getIntExtra("QUIZ_TOTAL", 1)

        // View Bindings
        ivIllustration = findViewById(R.id.ivResultIllustration)
        tvHeader = findViewById(R.id.tvResultHeader)
        tvSub = findViewById(R.id.tvResultSub)
        tvPercent = findViewById(R.id.tvResultPercent)
        tvFraction = findViewById(R.id.tvResultFraction)
        tvCorrectCount = findViewById(R.id.tvCorrectCount)
        tvWrongCount = findViewById(R.id.tvWrongCount)
        btnRetry = findViewById(R.id.btnRetryQuiz)
        btnBack = findViewById(R.id.btnBackToMenu)

        displayResults()
        saveResultToLocalDatabase()

        btnRetry.setOnClickListener {
            val intent = Intent(this, QuizPlayActivity::class.java).apply {
                putExtra("QUIZ_CATEGORY", category)
            }
            startActivity(intent)
            finish()
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun displayResults() {
        val pct = if (totalQuestions > 0) {
            ((score.toFloat() / totalQuestions.toFloat()) * 100).roundToInt()
        } else {
            0
        }

        tvPercent.text = "$pct%"
        tvFraction.text = "$score dari $totalQuestions benar"
        tvCorrectCount.text = score.toString()
        tvWrongCount.text = (totalQuestions - score).toString()

        // Dynamic visual cues based on score percent
        when {
            pct >= 80 -> {
                tvHeader.text = "Luar Biasa!"
                tvSub.text = "Anda menguasai materi ini dengan sangat baik."
                ivIllustration.setImageResource(android.R.drawable.btn_star_big_on)
                ivIllustration.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
            }
            pct >= 50 -> {
                tvHeader.text = "Cukup Baik!"
                tvSub.text = "Pertahankan progres belajar Anda untuk hasil maksimal."
                ivIllustration.setImageResource(android.R.drawable.btn_star_big_on)
                ivIllustration.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
            }
            else -> {
                tvHeader.text = "Tetap Semangat!"
                tvSub.text = "Pelajari kembali materinya dan coba lagi nanti."
                ivIllustration.setImageResource(android.R.drawable.btn_star_big_off)
                ivIllustration.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.darker_gray))
            }
        }
    }

    private fun saveResultToLocalDatabase() {
        DatabaseHelper.saveQuizScore(category, score, totalQuestions) { success ->
            if (success) {
                LogMessage("Quiz score successfully saved to local database.")
            } else {
                Toast.makeText(this, "Gagal menyimpan skor kuis", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun LogMessage(msg: String) {
        android.util.Log.d("QuizResultActivity", msg)
    }
}
