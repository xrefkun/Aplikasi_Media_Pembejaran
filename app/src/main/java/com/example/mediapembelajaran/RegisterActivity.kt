package com.example.mediapembelajaran

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etUsername = findViewById<EditText>(R.id.registerUsername)
        val etEmail = findViewById<EditText>(R.id.registerEmail)
        val etPassword = findViewById<EditText>(R.id.registerPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.registerConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnDoRegister)
        val btnBackToLogin = findViewById<Button>(R.id.btnBackToLogin)

        val rgRole = findViewById<android.widget.RadioGroup>(R.id.rgRole)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (username.isEmpty()) {
                etUsername.error = "Username tidak boleh kosong"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                etEmail.error = "Email tidak boleh kosong"
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Format email tidak valid"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Password tidak boleh kosong"
                return@setOnClickListener
            }
            if (password.length < 6) {
                etPassword.error = "Password minimal 6 karakter"
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                etConfirmPassword.error = "Konfirmasi password tidak cocok"
                return@setOnClickListener
            }

            val role = when (rgRole.checkedRadioButtonId) {
                R.id.rbGuru -> "GURU"
                else -> "SISWA"
            }

            btnRegister.isEnabled = false
            btnRegister.text = "Mendaftarkan..."

            DatabaseHelper.register(username, email, password, role,
                onSuccess = {
                    Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                },
                onFailure = { errorMessage ->
                    btnRegister.isEnabled = true
                    btnRegister.text = "Daftar Sekarang"
                    Toast.makeText(this, "Registrasi gagal: $errorMessage", Toast.LENGTH_LONG).show()
                }
            )
        }

        btnBackToLogin.setOnClickListener {
            finish() // Kembali ke MainActivity (Login screen)
        }
    }
}
