package com.example.mediapembelajaran

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.UUID

class AddEditQuestionActivity : AppCompatActivity() {

    private var questionId: String? = null
    private var isEditMode = false
    private lateinit var category: String

    private lateinit var tvToolbarTitle: TextView
    private lateinit var etText: EditText
    
    private lateinit var ivImagePreview: android.widget.ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnRemoveImage: Button
    private var selectedImageUriString: String? = null

    private lateinit var etOptA: EditText
    private lateinit var etOptB: EditText
    private lateinit var etOptC: EditText
    private lateinit var etOptD: EditText
    private lateinit var rgCorrect: RadioGroup
    private lateinit var rbA: RadioButton
    private lateinit var rbB: RadioButton
    private lateinit var rbC: RadioButton
    private lateinit var rbD: RadioButton
    private lateinit var btnSave: Button

    private val pickImageLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
        if (uri != null) {
            handleSelectedImage(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_edit_question)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById<android.view.View>(R.id.bottom_save_bar).parent as android.view.View) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        category = intent.getStringExtra("QUIZ_CATEGORY") ?: "Biologi"
        questionId = intent.getStringExtra("QUESTION_ID")
        isEditMode = questionId != null

        // View Bindings
        tvToolbarTitle = findViewById(R.id.tvAddEditQuestionTitle)
        etText = findViewById(R.id.etQuestionText)
        
        ivImagePreview = findViewById(R.id.ivQuestionImagePreview)
        btnSelectImage = findViewById(R.id.btnSelectQuestionImage)
        btnRemoveImage = findViewById(R.id.btnRemoveQuestionImage)

        etOptA = findViewById(R.id.etOptionA)
        etOptB = findViewById(R.id.etOptionB)
        etOptC = findViewById(R.id.etOptionC)
        etOptD = findViewById(R.id.etOptionD)
        rgCorrect = findViewById(R.id.rgCorrectOption)
        rbA = findViewById(R.id.rbOptionA)
        rbB = findViewById(R.id.rbOptionB)
        rbC = findViewById(R.id.rbOptionC)
        rbD = findViewById(R.id.rbOptionD)
        btnSave = findViewById(R.id.btnSaveQuestion)

        findViewById<ImageButton>(R.id.btnCancel).setOnClickListener {
            finish()
        }

        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnRemoveImage.setOnClickListener {
            removeSelectedImage()
        }

        if (isEditMode) {
            tvToolbarTitle.text = "Edit Soal"
            loadQuestionData()
        } else {
            tvToolbarTitle.text = "Tambah Soal"
        }

        btnSave.setOnClickListener {
            saveQuestion()
        }
    }

    private fun handleSelectedImage(uri: android.net.Uri) {
        try {
            val contentResolver = contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val dir = java.io.File(filesDir, "question_images")
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val file = java.io.File(dir, "${java.util.UUID.randomUUID()}.jpg")
                val outputStream = java.io.FileOutputStream(file)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()

                selectedImageUriString = file.absolutePath
                showImagePreview(file.absolutePath)
            }
        } catch (e: Exception) {
            android.util.Log.e("AddEditQuestion", "Error copying image file", e)
            Toast.makeText(this, "Gagal memilih gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImagePreview(path: String) {
        ivImagePreview.setImageURI(android.net.Uri.fromFile(java.io.File(path)))
        ivImagePreview.visibility = android.view.View.VISIBLE
        btnRemoveImage.visibility = android.view.View.VISIBLE
    }

    private fun removeSelectedImage() {
        selectedImageUriString = null
        ivImagePreview.setImageDrawable(null)
        ivImagePreview.visibility = android.view.View.GONE
        btnRemoveImage.visibility = android.view.View.GONE
    }

    private fun loadQuestionData() {
        val qid = questionId ?: return
        DatabaseHelper.getQuestions(category) { list ->
            val question = list.firstOrNull { it.id == qid }
            if (question != null) {
                etText.setText(question.questionText)
                etOptA.setText(question.optionA)
                etOptB.setText(question.optionB)
                etOptC.setText(question.optionC)
                etOptD.setText(question.optionD)

                when (question.correctOption) {
                    "A" -> rbA.isChecked = true
                    "B" -> rbB.isChecked = true
                    "C" -> rbC.isChecked = true
                    "D" -> rbD.isChecked = true
                }

                selectedImageUriString = question.imagePath
                if (!question.imagePath.isNullOrEmpty() && java.io.File(question.imagePath).exists()) {
                    showImagePreview(question.imagePath)
                }
            } else {
                Toast.makeText(this, "Gagal memuat data soal", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun saveQuestion() {
        val qText = etText.text.toString().trim()
        val optA = etOptA.text.toString().trim()
        val optB = etOptB.text.toString().trim()
        val optC = etOptC.text.toString().trim()
        val optD = etOptD.text.toString().trim()

        val correctOpt = when (rgCorrect.checkedRadioButtonId) {
            R.id.rbOptionB -> "B"
            R.id.rbOptionC -> "C"
            R.id.rbOptionD -> "D"
            else -> "A"
        }

        if (qText.isEmpty()) {
            etText.error = "Teks pertanyaan tidak boleh kosong"
            return
        }
        if (optA.isEmpty()) {
            etOptA.error = "Pilihan A tidak boleh kosong"
            return
        }
        if (optB.isEmpty()) {
            etOptB.error = "Pilihan B tidak boleh kosong"
            return
        }
        if (optC.isEmpty()) {
            etOptC.error = "Pilihan C tidak boleh kosong"
            return
        }
        if (optD.isEmpty()) {
            etOptD.error = "Pilihan D tidak boleh kosong"
            return
        }

        btnSave.isEnabled = false
        btnSave.text = "Menyimpan..."

        val savedQuestion = Question(
            id = questionId ?: UUID.randomUUID().toString(),
            category = category,
            questionText = qText,
            optionA = optA,
            optionB = optB,
            optionC = optC,
            optionD = optD,
            correctOption = correctOpt,
            imagePath = selectedImageUriString ?: ""
        )

        val onResult = { success: Boolean ->
            if (success) {
                Toast.makeText(this, "Soal berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                btnSave.isEnabled = true
                btnSave.text = "Simpan Soal"
                Toast.makeText(this, "Gagal menyimpan soal", Toast.LENGTH_SHORT).show()
            }
        }

        if (isEditMode) {
            DatabaseHelper.updateQuestion(savedQuestion, onResult)
        } else {
            DatabaseHelper.addQuestion(savedQuestion, onResult)
        }
    }
}
