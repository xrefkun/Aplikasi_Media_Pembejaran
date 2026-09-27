package com.example.mediapembelajaran

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.UUID

class AddEditLessonActivity : AppCompatActivity() {

    private var lessonId: String? = null
    private var isEditMode = false

    private lateinit var tvToolbarTitle: TextView
    private lateinit var etTitle: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var rgDifficulty: RadioGroup
    private lateinit var rbPemula: RadioButton
    private lateinit var rbMenengah: RadioButton
    private lateinit var rbMahir: RadioButton
    private lateinit var etShortDesc: EditText
    private lateinit var etContent: EditText
    
    private lateinit var ivImagePreview: android.widget.ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnRemoveImage: Button
    private var selectedImageUriString: String? = null

    private lateinit var btnSave: Button

    private val categories = listOf(
        "Biologi",
        "Fisika",
        "Kimia"
    )

    private val pickImageLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
        if (uri != null) {
            handleSelectedImage(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_edit_lesson)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById<android.view.View>(R.id.bottom_save_bar).parent as android.view.View) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        lessonId = intent.getStringExtra("LESSON_ID")
        isEditMode = lessonId != null

        // View Bindings
        tvToolbarTitle = findViewById(R.id.tvAddEditTitle)
        etTitle = findViewById(R.id.etLessonTitle)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        rgDifficulty = findViewById(R.id.rgDifficulty)
        rbPemula = findViewById(R.id.rbPemula)
        rbMenengah = findViewById(R.id.rbMenengah)
        rbMahir = findViewById(R.id.rbMahir)
        etShortDesc = findViewById(R.id.etLessonShortDesc)
        etContent = findViewById(R.id.etLessonContent)
        
        ivImagePreview = findViewById(R.id.ivImagePreview)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnRemoveImage = findViewById(R.id.btnRemoveImage)
        
        btnSave = findViewById(R.id.btnSaveLesson)

        // Setup Spinner Adapter
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Cancel Button
        findViewById<ImageButton>(R.id.btnCancel).setOnClickListener {
            finish()
        }

        // Image Picker Buttons
        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnRemoveImage.setOnClickListener {
            removeSelectedImage()
        }

        if (isEditMode) {
            tvToolbarTitle.text = "Edit Materi"
            loadLessonData()
        } else {
            tvToolbarTitle.text = "Tambah Materi"
        }

        btnSave.setOnClickListener {
            saveLesson()
        }
    }

    private fun handleSelectedImage(uri: android.net.Uri) {
        try {
            val contentResolver = contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val dir = java.io.File(filesDir, "lesson_images")
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
            android.util.Log.e("AddEditLesson", "Error copying image file", e)
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

    private fun loadLessonData() {
        val id = lessonId ?: return
        DatabaseHelper.getLessonById(id) { lesson ->
            if (lesson != null) {
                etTitle.setText(lesson.title)
                
                // Select category in spinner
                val catIndex = categories.indexOf(lesson.category)
                if (catIndex >= 0) {
                    spinnerCategory.setSelection(catIndex)
                }

                // Select difficulty
                when (lesson.difficulty) {
                    "Pemula" -> rbPemula.isChecked = true
                    "Menengah" -> rbMenengah.isChecked = true
                    "Mahir" -> rbMahir.isChecked = true
                }

                etShortDesc.setText(lesson.shortDescription)
                etContent.setText(lesson.content)
                
                selectedImageUriString = lesson.codeSnippet
                if (!lesson.codeSnippet.isNullOrEmpty() && java.io.File(lesson.codeSnippet).exists()) {
                    showImagePreview(lesson.codeSnippet)
                }
            } else {
                Toast.makeText(this, "Gagal memuat materi", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun saveLesson() {
        val title = etTitle.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val shortDesc = etShortDesc.text.toString().trim()
        val content = etContent.text.toString().trim()

        val difficulty = when (rgDifficulty.checkedRadioButtonId) {
            R.id.rbMenengah -> "Menengah"
            R.id.rbMahir -> "Mahir"
            else -> "Pemula"
        }

        if (title.isEmpty()) {
            etTitle.error = "Judul tidak boleh kosong"
            return
        }
        if (shortDesc.isEmpty()) {
            etShortDesc.error = "Deskripsi tidak boleh kosong"
            return
        }
        if (content.isEmpty()) {
            etContent.error = "Isi materi tidak boleh kosong"
            return
        }

        btnSave.isEnabled = false
        btnSave.text = "Menyimpan..."

        val savedLesson = Lesson(
            id = lessonId ?: java.util.UUID.randomUUID().toString(),
            title = title,
            category = category,
            shortDescription = shortDesc,
            content = content,
            codeSnippet = selectedImageUriString ?: "",
            difficulty = difficulty
        )

        val onResult = { success: Boolean ->
            if (success) {
                Toast.makeText(this, "Materi berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                btnSave.isEnabled = true
                btnSave.text = "Simpan Materi"
                Toast.makeText(this, "Gagal menyimpan materi", Toast.LENGTH_SHORT).show()
            }
        }

        if (isEditMode) {
            DatabaseHelper.updateLesson(savedLesson, onResult)
        } else {
            DatabaseHelper.addLesson(savedLesson, onResult)
        }
    }
}
