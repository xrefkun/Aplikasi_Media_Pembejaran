package com.example.mediapembelajaran

data class UserProfile(
    val username: String = "",
    val email: String = ""
)

data class Lesson(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val shortDescription: String = "",
    val content: String = "",
    val codeSnippet: String = "",
    val difficulty: String = "" // Pemula, Menengah, Mahir
)

data class Question(
    val id: String = "",
    val category: String = "",
    val questionText: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctOption: String = "", // A, B, C, or D
    val imagePath: String = ""
)

data class QuizScore(
    val category: String = "",
    val score: Int = 0,
    val maxScore: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
