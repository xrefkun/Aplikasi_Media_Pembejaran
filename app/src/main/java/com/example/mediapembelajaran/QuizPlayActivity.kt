package com.example.mediapembelajaran

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class QuizPlayActivity : AppCompatActivity() {

    private lateinit var category: String
    private lateinit var questions: List<Question>

    private var currentQuestionIndex = 0
    private var score = 0
    private var isAnswered = false

    private lateinit var tvToolbarTitle: TextView
    private lateinit var pbQuizProgress: ProgressBar
    private lateinit var tvQuestionCounter: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var ivQuestionImage: android.widget.ImageView

    private lateinit var btnOptionA: MaterialButton
    private lateinit var btnOptionB: MaterialButton
    private lateinit var btnOptionC: MaterialButton
    private lateinit var btnOptionD: MaterialButton
    private lateinit var btnNext: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_quiz_play)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.quiz_play_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        category = intent.getStringExtra("QUIZ_CATEGORY") ?: "Biologi"

        // View Bindings
        tvToolbarTitle = findViewById(R.id.tvToolbarQuizTitle)
        pbQuizProgress = findViewById(R.id.pbQuizProgress)
        tvQuestionCounter = findViewById(R.id.tvQuestionCounter)
        tvQuestion = findViewById(R.id.tvQuestion)
        ivQuestionImage = findViewById(R.id.ivQuestionImage)

        btnOptionA = findViewById(R.id.btnOptionA)
        btnOptionB = findViewById(R.id.btnOptionB)
        btnOptionC = findViewById(R.id.btnOptionC)
        btnOptionD = findViewById(R.id.btnOptionD)
        btnNext = findViewById(R.id.btnNextQuestion)

        tvToolbarTitle.text = "Kuis $category"

        findViewById<ImageButton>(R.id.btnQuitQuiz).setOnClickListener {
            showQuitConfirmationDialog()
        }

        btnNext.setOnClickListener {
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                loadQuestion()
            } else {
                val intent = Intent(this, QuizResultActivity::class.java).apply {
                    putExtra("QUIZ_CATEGORY", category)
                    putExtra("QUIZ_SCORE", score)
                    putExtra("QUIZ_TOTAL", questions.size)
                }
                startActivity(intent)
                finish()
            }
        }

        // Fetch questions asynchronously
        DatabaseHelper.getQuestions(category) { list ->
            if (isDestroyed || isFinishing) return@getQuestions
            if (list.isEmpty()) {
                Toast.makeText(this, "Tidak ada soal untuk kategori ini", Toast.LENGTH_SHORT).show()
                finish()
                return@getQuestions
            }
            questions = list
            loadQuestion()
            setupOptionClickListeners()
        }
    }

    private fun loadQuestion() {
        isAnswered = false
        val question = questions[currentQuestionIndex]

        // Update Counter and Progress Bar
        val progressPercent = ((currentQuestionIndex.toFloat() / questions.size.toFloat()) * 100).toInt()
        pbQuizProgress.progress = progressPercent

        tvQuestionCounter.text = "Pertanyaan ${currentQuestionIndex + 1} dari ${questions.size}"
        tvQuestion.text = question.questionText

        // Show/load question image if present
        if (!question.imagePath.isNullOrEmpty() && java.io.File(question.imagePath).exists()) {
            ivQuestionImage.visibility = View.VISIBLE
            ivQuestionImage.setImageURI(android.net.Uri.fromFile(java.io.File(question.imagePath)))
        } else {
            ivQuestionImage.visibility = View.GONE
        }

        // Set Option Texts
        btnOptionA.text = question.optionA
        btnOptionB.text = question.optionB
        btnOptionC.text = question.optionC
        btnOptionD.text = question.optionD

        // Reset Options Button Styling
        resetOptionButton(btnOptionA)
        resetOptionButton(btnOptionB)
        resetOptionButton(btnOptionC)
        resetOptionButton(btnOptionD)

        btnNext.visibility = View.INVISIBLE
    }

    private fun resetOptionButton(button: MaterialButton) {
        button.isEnabled = true
        button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))
        button.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        button.setStrokeColorResource(android.R.color.darker_gray)
        button.iconTint = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.black))
    }

    private fun setupOptionClickListeners() {
        val clickListener = View.OnClickListener { v ->
            if (isAnswered) return@OnClickListener
            isAnswered = true

            val selectedOption = when (v.id) {
                R.id.btnOptionA -> "A"
                R.id.btnOptionB -> "B"
                R.id.btnOptionC -> "C"
                R.id.btnOptionD -> "D"
                else -> "A"
            }

            val question = questions[currentQuestionIndex]
            val correctOption = question.correctOption

            // Disable all options
            btnOptionA.isEnabled = false
            btnOptionB.isEnabled = false
            btnOptionC.isEnabled = false
            btnOptionD.isEnabled = false

            // Highlight answers
            val selectedButton = v as MaterialButton
            if (selectedOption == correctOption) {
                score++
                styleButtonCorrect(selectedButton)
            } else {
                styleButtonWrong(selectedButton)
                // Highlight correct button
                val correctButton = when (correctOption) {
                    "A" -> btnOptionA
                    "B" -> btnOptionB
                    "C" -> btnOptionC
                    "D" -> btnOptionD
                    else -> btnOptionA
                }
                styleButtonCorrect(correctButton)
            }

            btnNext.visibility = View.VISIBLE
        }

        btnOptionA.setOnClickListener(clickListener)
        btnOptionB.setOnClickListener(clickListener)
        btnOptionC.setOnClickListener(clickListener)
        btnOptionD.setOnClickListener(clickListener)
    }

    private fun styleButtonCorrect(button: MaterialButton) {
        button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_green_light))
        button.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        button.setStrokeColorResource(android.R.color.holo_green_light)
        button.iconTint = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))
    }

    private fun styleButtonWrong(button: MaterialButton) {
        button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_red_light))
        button.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        button.setStrokeColorResource(android.R.color.holo_red_light)
        button.iconTint = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))
    }

    private fun showQuitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Keluar Kuis?")
            .setMessage("Kemajuan kuis ini akan hilang jika Anda keluar sekarang.")
            .setPositiveButton("Keluar") { _, _ ->
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onBackPressed() {
        showQuitConfirmationDialog()
    }
}
