package com.example.mediapembelajaran

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LessonDetailActivity : AppCompatActivity() {

    private lateinit var lessonId: String
    private lateinit var lesson: Lesson

    private lateinit var tvCategory: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvContent: TextView
    
    private lateinit var tvDetailImageLabel: TextView
    private lateinit var cvDetailImage: androidx.cardview.widget.CardView
    private lateinit var ivDetailImage: android.widget.ImageView

    private lateinit var btnBookmark: ImageButton
    private lateinit var btnMarkCompleted: Button
    private lateinit var layoutTeacherActions: LinearLayout
    private lateinit var btnEditLesson: Button
    private lateinit var btnDeleteLesson: Button

    private var isBookmarked = false
    private var isCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lesson_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        lessonId = intent.getStringExtra("LESSON_ID") ?: ""

        // View Bindings
        tvCategory = findViewById(R.id.tvDetailCategory)
        tvTitle = findViewById(R.id.tvDetailTitle)
        tvContent = findViewById(R.id.tvDetailContent)
        
        tvDetailImageLabel = findViewById(R.id.tvDetailImageLabel)
        cvDetailImage = findViewById(R.id.cvDetailImage)
        ivDetailImage = findViewById(R.id.ivDetailImage)

        btnBookmark = findViewById(R.id.btnBookmark)
        btnMarkCompleted = findViewById(R.id.btnMarkCompleted)
        layoutTeacherActions = findViewById(R.id.layoutTeacherActions)
        btnEditLesson = findViewById(R.id.btnEditLesson)
        btnDeleteLesson = findViewById(R.id.btnDeleteLesson)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        setupActions()
    }

    override fun onResume() {
        super.onResume()
        loadLessonDetails()
    }

    private fun loadLessonDetails() {
        DatabaseHelper.getLessonById(lessonId) { foundLesson ->
            if (foundLesson == null) {
                // If it was deleted during edit or otherwise, finish
                Toast.makeText(this, "Materi tidak ditemukan atau telah dihapus", Toast.LENGTH_SHORT).show()
                finish()
                return@getLessonById
            }

            lesson = foundLesson

            // Populate values
            tvCategory.text = lesson.category
            tvTitle.text = lesson.title
            tvContent.text = lesson.content.replace("\\n", "\n")

            // Show and load image if local file exists
            if (!lesson.codeSnippet.isNullOrEmpty() && java.io.File(lesson.codeSnippet).exists()) {
                tvDetailImageLabel.visibility = View.VISIBLE
                cvDetailImage.visibility = View.VISIBLE
                ivDetailImage.setImageURI(android.net.Uri.fromFile(java.io.File(lesson.codeSnippet)))
            } else {
                tvDetailImageLabel.visibility = View.GONE
                cvDetailImage.visibility = View.GONE
            }

            checkStatus()
        }
    }

    private fun checkStatus() {
        // 1. Check completion status
        DatabaseHelper.getCompletedLessons { completedList ->
            if (completedList.contains(lessonId)) {
                isCompleted = true
                btnMarkCompleted.text = "Selesai Membaca ✓"
                btnMarkCompleted.isEnabled = false
                btnMarkCompleted.alpha = 0.6f
            } else {
                isCompleted = false
                btnMarkCompleted.text = "Tandai Selesai Membaca"
                btnMarkCompleted.isEnabled = true
                btnMarkCompleted.alpha = 1.0f
            }
        }

        // 2. Check bookmark status
        DatabaseHelper.getBookmarkedLessons { bookmarkedList ->
            if (bookmarkedList.contains(lessonId)) {
                isBookmarked = true
                btnBookmark.setImageResource(android.R.drawable.btn_star_big_on)
            } else {
                isBookmarked = false
                btnBookmark.setImageResource(android.R.drawable.btn_star_big_off)
            }
        }
    }

    private fun setupActions() {
        val role = DatabaseHelper.currentUserRole

        // Configure actions visibility based on roles
        if (role == "GURU" || role == "ADMIN") {
            btnMarkCompleted.visibility = View.GONE
            layoutTeacherActions.visibility = View.VISIBLE

            btnEditLesson.setOnClickListener {
                val intent = Intent(this, AddEditLessonActivity::class.java).apply {
                    putExtra("LESSON_ID", lesson.id)
                }
                startActivity(intent)
            }

            btnDeleteLesson.setOnClickListener {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Hapus Materi")
                    .setMessage("Apakah Anda yakin ingin menghapus materi ini?")
                    .setPositiveButton("Ya") { _, _ ->
                        DatabaseHelper.deleteLesson(lesson.id) { success ->
                            if (success) {
                                Toast.makeText(this, "Materi berhasil dihapus", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, "Gagal menghapus materi", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Tidak", null)
                    .show()
            }
        } else {
            btnMarkCompleted.visibility = View.VISIBLE
            layoutTeacherActions.visibility = View.GONE

            // Mark Completed
            btnMarkCompleted.setOnClickListener {
                btnMarkCompleted.isEnabled = false
                btnMarkCompleted.text = "Menyimpan..."
                DatabaseHelper.saveUserProgress(lessonId) { success ->
                    if (success) {
                        isCompleted = true
                        btnMarkCompleted.text = "Selesai Membaca ✓"
                        btnMarkCompleted.isEnabled = false
                        btnMarkCompleted.alpha = 0.6f
                        Toast.makeText(this, "Materi selesai dipelajari!", Toast.LENGTH_SHORT).show()
                    } else {
                        btnMarkCompleted.isEnabled = true
                        btnMarkCompleted.text = "Tandai Selesai Membaca"
                        Toast.makeText(this, "Gagal menandai materi", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Toggle Bookmark (Siswa, Guru, or Admin can bookmark if they want, but standard feature)
        btnBookmark.setOnClickListener {
            val nextState = !isBookmarked
            btnBookmark.isEnabled = false
            DatabaseHelper.toggleBookmark(lessonId, nextState) { success ->
                btnBookmark.isEnabled = true
                if (success) {
                    isBookmarked = nextState
                    if (isBookmarked) {
                        btnBookmark.setImageResource(android.R.drawable.btn_star_big_on)
                        Toast.makeText(this, "Ditambahkan ke bookmark", Toast.LENGTH_SHORT).show()
                    } else {
                        btnBookmark.setImageResource(android.R.drawable.btn_star_big_off)
                        Toast.makeText(this, "Dihapus dari bookmark", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Gagal mengubah bookmark", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

