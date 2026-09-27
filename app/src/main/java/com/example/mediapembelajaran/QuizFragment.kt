package com.example.mediapembelajaran

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class QuizFragment : Fragment() {

    private lateinit var cardQuizKotlin: View
    private lateinit var cardQuizAndroid: View
    private lateinit var cardQuizStorage: View
    private lateinit var cardQuizNetwork: View

    private lateinit var tvScoreKotlin: TextView
    private lateinit var tvScoreAndroid: TextView
    private lateinit var tvScoreStorage: TextView
    private lateinit var tvScoreNetwork: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quiz, container, false)

        cardQuizKotlin = view.findViewById(R.id.cardQuizKotlin)
        cardQuizAndroid = view.findViewById(R.id.cardQuizAndroid)
        cardQuizStorage = view.findViewById(R.id.cardQuizStorage)
        cardQuizNetwork = view.findViewById(R.id.cardQuizNetwork)

        tvScoreKotlin = view.findViewById(R.id.tvScoreKotlin)
        tvScoreAndroid = view.findViewById(R.id.tvScoreAndroid)
        tvScoreStorage = view.findViewById(R.id.tvScoreStorage)
        tvScoreNetwork = view.findViewById(R.id.tvScoreNetwork)

        setupQuizSelection()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadQuizScores()
    }

    private fun setupQuizSelection() {
        val clickListener = View.OnClickListener { v ->
            val category = when (v.id) {
                R.id.cardQuizKotlin -> "Biologi"
                R.id.cardQuizAndroid -> "Fisika"
                R.id.cardQuizStorage -> "Kimia"
                else -> "Biologi"
            }

            val intent = Intent(context, QuizPlayActivity::class.java).apply {
                putExtra("QUIZ_CATEGORY", category)
            }
            startActivity(intent)
        }

        cardQuizKotlin.setOnClickListener(clickListener)
        cardQuizAndroid.setOnClickListener(clickListener)
        cardQuizStorage.setOnClickListener(clickListener)
    }

    private fun loadQuizScores() {
        DatabaseHelper.getQuizScores { scores ->
            if (!isAdded) return@getQuizScores

            val categories = listOf("Biologi", "Fisika", "Kimia")
            val maxScores = mutableMapOf<String, Int>()

            for (cat in categories) {
                val catScores = scores.filter { it.category == cat }
                if (catScores.isNotEmpty()) {
                    val max = catScores.maxOf { it.score }
                    maxScores[cat] = max
                }
            }

            // Update Views (each of the three new IPA categories has 3 questions)
            updateScoreText(tvScoreKotlin, maxScores["Biologi"], 3)
            updateScoreText(tvScoreAndroid, maxScores["Fisika"], 3)
            updateScoreText(tvScoreStorage, maxScores["Kimia"], 3)
        }
    }

    private fun updateScoreText(textView: TextView, score: Int?, totalQuestions: Int) {
        if (score != null) {
            textView.text = "Skor Tertinggi: $score / $totalQuestions"
        } else {
            textView.text = "Skor Tertinggi: Belum Ada"
        }
    }
}
