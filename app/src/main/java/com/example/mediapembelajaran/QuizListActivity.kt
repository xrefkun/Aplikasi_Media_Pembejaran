package com.example.mediapembelajaran

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class QuizListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_quiz_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById<android.view.View>(R.id.quizListToolbar).parent as android.view.View) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val clickListener = android.view.View.OnClickListener { v ->
            val category = when (v.id) {
                R.id.cardManageBio -> "Biologi"
                R.id.cardManageFis -> "Fisika"
                R.id.cardManageKim -> "Kimia"
                else -> "Biologi"
            }

            val intent = Intent(this, ManageQuestionsActivity::class.java).apply {
                putExtra("QUIZ_CATEGORY", category)
            }
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.cardManageBio).setOnClickListener(clickListener)
        findViewById<android.view.View>(R.id.cardManageFis).setOnClickListener(clickListener)
        findViewById<android.view.View>(R.id.cardManageKim).setOnClickListener(clickListener)
    }
}
