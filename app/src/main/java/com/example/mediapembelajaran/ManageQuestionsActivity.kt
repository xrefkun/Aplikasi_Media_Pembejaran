package com.example.mediapembelajaran

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ManageQuestionsActivity : AppCompatActivity() {

    private lateinit var category: String
    private lateinit var rvQuestions: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvToolbarTitle: TextView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var adapter: QuestionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_questions)

        category = intent.getStringExtra("QUIZ_CATEGORY") ?: "Biologi"

        ViewCompat.setOnApplyWindowInsetsListener(findViewById<android.view.View>(R.id.manageQuestionsToolbar).parent as android.view.View) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        tvToolbarTitle = findViewById(R.id.tvToolbarTitle)
        tvToolbarTitle.text = "Kelola Soal: $category"

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        rvQuestions = findViewById(R.id.rvQuestions)
        tvEmpty = findViewById(R.id.tvEmptyQuestions)
        fabAdd = findViewById(R.id.fabAddQuestion)

        rvQuestions.layoutManager = LinearLayoutManager(this)
        adapter = QuestionsAdapter(emptyList(), ::onEditQuestion, ::onDeleteQuestion)
        rvQuestions.adapter = adapter

        fabAdd.setOnClickListener {
            val intent = Intent(this, AddEditQuestionActivity::class.java).apply {
                putExtra("QUIZ_CATEGORY", category)
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadQuestions()
    }

    private fun loadQuestions() {
        DatabaseHelper.getQuestions(category) { list ->
            if (list.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                rvQuestions.visibility = View.GONE
            } else {
                tvEmpty.visibility = View.GONE
                rvQuestions.visibility = View.VISIBLE
                adapter.updateList(list)
            }
        }
    }

    private fun onEditQuestion(question: Question) {
        val intent = Intent(this, AddEditQuestionActivity::class.java).apply {
            putExtra("QUIZ_CATEGORY", category)
            putExtra("QUESTION_ID", question.id)
        }
        startActivity(intent)
    }

    private fun onDeleteQuestion(question: Question) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Soal")
            .setMessage("Apakah Anda yakin ingin menghapus soal ini?")
            .setPositiveButton("Ya") { _, _ ->
                DatabaseHelper.deleteQuestion(question.id) { success ->
                    if (success) {
                        Toast.makeText(this, "Soal berhasil dihapus", Toast.LENGTH_SHORT).show()
                        loadQuestions()
                    } else {
                        Toast.makeText(this, "Gagal menghapus soal", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private class QuestionsAdapter(
        private var list: List<Question>,
        private val onEdit: (Question) -> Unit,
        private val onDelete: (Question) -> Unit
    ) : RecyclerView.Adapter<QuestionsAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvText: TextView = view.findViewById(R.id.tvQuestionText)
            val ivImage: android.widget.ImageView = view.findViewById(R.id.ivQuestionItemImage)
            val tvOptA: TextView = view.findViewById(R.id.tvOptionA)
            val tvOptB: TextView = view.findViewById(R.id.tvOptionB)
            val tvOptC: TextView = view.findViewById(R.id.tvOptionC)
            val tvOptD: TextView = view.findViewById(R.id.tvOptionD)
            val tvCorrect: TextView = view.findViewById(R.id.tvCorrectOption)
            val btnEdit: View = view.findViewById(R.id.btnEditQuestion)
            val btnDelete: View = view.findViewById(R.id.btnDeleteQuestion)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_question, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvText.text = item.questionText
            
            if (!item.imagePath.isNullOrEmpty() && java.io.File(item.imagePath).exists()) {
                holder.ivImage.visibility = View.VISIBLE
                holder.ivImage.setImageURI(android.net.Uri.fromFile(java.io.File(item.imagePath)))
            } else {
                holder.ivImage.visibility = View.GONE
            }

            holder.tvOptA.text = "A. ${item.optionA}"
            holder.tvOptB.text = "B. ${item.optionB}"
            holder.tvOptC.text = "C. ${item.optionC}"
            holder.tvOptD.text = "D. ${item.optionD}"
            holder.tvCorrect.text = "Jawaban Benar: ${item.correctOption}"

            holder.btnEdit.setOnClickListener { onEdit(item) }
            holder.btnDelete.setOnClickListener { onDelete(item) }
        }

        override fun getItemCount() = list.size

        fun updateList(newList: List<Question>) {
            list = newList
            notifyDataSetChanged()
        }
    }
}
