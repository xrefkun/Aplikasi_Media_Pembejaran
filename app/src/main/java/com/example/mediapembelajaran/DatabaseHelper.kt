package com.example.mediapembelajaran

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.UUID
import java.util.concurrent.Executors

object DatabaseHelper {
    private const val TAG = "DatabaseHelper"

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private var database: AppDatabase? = null
    private var sharedPrefs: SharedPreferences? = null

    fun init(context: Context) {
        database = AppDatabase.getDatabase(context)
        sharedPrefs = context.getSharedPreferences("media_pembelajaran_prefs", Context.MODE_PRIVATE)

        // Pre-populate default admin, teacher, lessons and questions
        runOnBackground {
            try {
                // Pre-populate default admin account
                val adminEmail = "admin@gmail.com"
                val existingAdmin = db.userDao().getUserByEmail(adminEmail)
                if (existingAdmin == null) {
                    val defaultAdmin = UserEntity(
                        id = UUID.randomUUID().toString(),
                        username = "Admin IPA",
                        email = adminEmail,
                        password = "admin123",
                        role = "ADMIN"
                    )
                    db.userDao().insert(defaultAdmin)
                    Log.d(TAG, "Default Admin pre-populated: admin@gmail.com")
                }

                // Pre-populate default teacher account
                val teacherEmail = "guru@gmail.com"
                val existingTeacher = db.userDao().getUserByEmail(teacherEmail)
                if (existingTeacher == null) {
                    val defaultTeacher = UserEntity(
                        id = UUID.randomUUID().toString(),
                        username = "Guru IPA",
                        email = teacherEmail,
                        password = "guru123",
                        role = "GURU"
                    )
                    db.userDao().insert(defaultTeacher)
                    Log.d(TAG, "Default Teacher pre-populated: guru@gmail.com")
                }

                // Pre-populate default lessons if database has no lessons
                val existingLessons = db.lessonDao().getAllLessons()
                if (existingLessons.isEmpty()) {
                    val defaults = getInitialStaticLessons()
                    for (d in defaults) {
                        db.lessonDao().insert(
                            LessonEntity(
                                id = d.id,
                                title = d.title,
                                category = d.category,
                                shortDescription = d.shortDescription,
                                content = d.content,
                                codeSnippet = d.codeSnippet,
                                difficulty = d.difficulty
                            )
                        )
                    }
                }

                // Pre-populate default questions if database has no questions
                val existingQuestions = db.questionDao().getAllQuestions()
                if (existingQuestions.isEmpty()) {
                    val defaults = getInitialStaticQuestionsList()
                    for (q in defaults) {
                        db.questionDao().insert(
                            QuestionEntity(
                                id = q.id,
                                category = q.category,
                                questionText = q.questionText,
                                optionA = q.optionA,
                                optionB = q.optionB,
                                optionC = q.optionC,
                                optionD = q.optionD,
                                correctOption = q.correctOption,
                                imagePath = q.imagePath
                            )
                        )
                    }
                    Log.d(TAG, "Default Questions pre-populated")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error populating initial database items", e)
            }
        }
    }

    private val db: AppDatabase
        get() = database ?: throw IllegalStateException("DatabaseHelper not initialized. Call init(context) first.")

    private val prefs: SharedPreferences
        get() = sharedPrefs ?: throw IllegalStateException("DatabaseHelper not initialized. Call init(context) first.")

    // --- HELPER THREADING METHODS ---
    private fun runOnBackground(action: () -> Unit) {
        executor.execute(action)
    }

    private fun runOnMain(action: () -> Unit) {
        mainHandler.post(action)
    }

    // --- LOCAL AUTHENTICATION ---

    val currentUserId: String?
        get() = prefs.getString("current_user_id", null)

    val currentUserEmail: String?
        get() = prefs.getString("current_user_email", null)

    val currentUserRole: String?
        get() = prefs.getString("current_user_role", null)

    fun isUserLoggedIn(): Boolean {
        return currentUserId != null
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        runOnBackground {
            try {
                val user = db.userDao().getUserByEmail(email)
                if (user != null && user.password == password) {
                    prefs.edit().apply {
                        putString("current_user_id", user.id)
                        putString("current_user_email", user.email)
                        putString("current_user_username", user.username)
                        putString("current_user_role", user.role)
                        apply()
                    }
                    runOnMain { onSuccess() }
                } else {
                    runOnMain { onFailure("Email atau password salah.") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error", e)
                runOnMain { onFailure("Terjadi kesalahan sistem.") }
            }
        }
    }

    fun register(username: String, email: String, password: String, role: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        runOnBackground {
            try {
                val existing = db.userDao().getUserByEmail(email)
                if (existing != null) {
                    runOnMain { onFailure("Email sudah terdaftar.") }
                    return@runOnBackground
                }

                val newId = UUID.randomUUID().toString()
                val newUser = UserEntity(newId, username, email, password, role)
                db.userDao().insert(newUser)

                prefs.edit().apply {
                    putString("current_user_id", newId)
                    putString("current_user_email", email)
                    putString("current_user_username", username)
                    putString("current_user_role", role)
                    apply()
                }
                runOnMain { onSuccess() }
            } catch (e: Exception) {
                Log.e(TAG, "Register error", e)
                runOnMain { onFailure("Gagal melakukan registrasi.") }
            }
        }
    }

    fun signOut() {
        prefs.edit().clear().apply()
    }

    // --- USER PROFILE ---
    fun saveUserProfile(username: String, email: String, onComplete: (Boolean) -> Unit) {
        val uid = currentUserId ?: return onComplete(false)
        runOnBackground {
            try {
                val user = db.userDao().getUserById(uid)
                if (user != null) {
                    val updatedUser = user.copy(username = username, email = email)
                    db.userDao().insert(updatedUser)

                    prefs.edit().apply {
                        putString("current_user_email", email)
                        putString("current_user_username", username)
                        apply()
                    }
                    runOnMain { onComplete(true) }
                } else {
                    runOnMain { onComplete(false) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving user profile", e)
                runOnMain { onComplete(false) }
            }
        }
    }

    fun getUserProfile(onComplete: (UserProfile?) -> Unit) {
        val uid = currentUserId ?: return onComplete(null)
        runOnBackground {
            try {
                val user = db.userDao().getUserById(uid)
                if (user != null) {
                    val profile = UserProfile(user.username, user.email)
                    runOnMain { onComplete(profile) }
                } else {
                    runOnMain { onComplete(null) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting user profile", e)
                runOnMain { onComplete(null) }
            }
        }
    }

    // --- ADMIN USER MANAGEMENT ---
    fun getAllUsers(onComplete: (List<UserEntity>) -> Unit) {
        runOnBackground {
            try {
                val list = db.userDao().getAllUsers()
                runOnMain { onComplete(list) }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting all users", e)
                runOnMain { onComplete(emptyList()) }
            }
        }
    }

    fun updateUserRole(userId: String, newRole: String, onComplete: (Boolean) -> Unit) {
        runOnBackground {
            try {
                db.userDao().updateUserRole(userId, newRole)
                // If currently logged-in user changed their own role, update prefs
                if (userId == currentUserId) {
                    prefs.edit().putString("current_user_role", newRole).apply()
                }
                runOnMain { onComplete(true) }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating user role", e)
                runOnMain { onComplete(false) }
            }
        }
    }

    fun deleteUser(userId: String, onComplete: (Boolean) -> Unit) {
        runOnBackground {
            try {
                db.userDao().deleteUser(userId)
                runOnMain { onComplete(true) }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting user", e)
                runOnMain { onComplete(false) }
            }
        }
    }

    // --- TEACHER VIEW SCORES ---
    fun getStudentScores(onComplete: (List<StudentScoreQueryRow>) -> Unit) {
        runOnBackground {
            try {
                val list = db.quizScoreDao().getStudentScores()
                runOnMain { onComplete(list) }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting student scores", e)
                runOnMain { onComplete(emptyList()) }
            }
        }
    }

    // --- LESSON CRUD ---
    fun getLessons(onComplete: (List<Lesson>) -> Unit) {
        runOnBackground {
            try {
                val list = db.lessonDao().getAllLessons().map {
                    Lesson(
                        id = it.id,
                        title = it.title,
                        category = it.category,
                        shortDescription = it.shortDescription,
                        content = it.content,
                        codeSnippet = it.codeSnippet,
                        difficulty = it.difficulty
                    )
                }
                runOnMain { onComplete(list) }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting lessons", e)
                runOnMain { onComplete(emptyList()) }
            }
        }
    }

    fun getLessonById(lessonId: String, onComplete: (Lesson?) -> Unit) {
        runOnBackground {
            try {
                val entity = db.lessonDao().getLessonById(lessonId)
                val lesson = entity?.let {
                    Lesson(
                        id = it.id,
                        title = it.title,
                        category = it.category,
                        shortDescription = it.shortDescription,
                        content = it.content,
                        codeSnippet = it.codeSnippet,
                        difficulty = it.difficulty
                    )
                }
                runOnMain { onComplete(lesson) }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting lesson by ID", e)
                runOnMain { onComplete(null) }
            }
        }
    }

    fun addLesson(lesson: Lesson, onComplete: (Boolean) -> Unit) {
        runOnBackground {
            try {
                val finalId = if (lesson.id.isEmpty()) UUID.randomUUID().toString() else lesson.id
                db.lessonDao().insert(
                    LessonEntity(
                        id = finalId,
                        title = lesson.title,
                        category = lesson.category,
                        shortDescription = lesson.shortDescription,
                        content = lesson.content,
                        codeSnippet = lesson.codeSnippet,
                        difficulty = lesson.difficulty
                    )
                )
                runOnMain { onComplete(true) }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding lesson", e)
                runOnMain { onComplete(false) }
            }
        }
    }

    fun updateLesson(lesson: Lesson, onComplete: (Boolean) -> Unit) {
        runOnBackground {
            try {
                db.lessonDao().insert(
                    LessonEntity(
                        id = lesson.id,
                        title = lesson.title,
                        category = lesson.category,
                        shortDescription = lesson.shortDescription,
                        content = lesson.content,
                        codeSnippet = lesson.codeSnippet,
                        difficulty = lesson.difficulty
                    )
                )
                runOnMain { onComplete(true) }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating lesson", e)
                runOnMain { onComplete(false) }
            }
        }
    }

    fun deleteLesson(lessonId: String, onComplete: (Boolean) -> Unit) {
        runOnBackground {
            try {
                val entity = db.lessonDao().getLessonById(lessonId)
                if (entity != null) {
                    db.lessonDao().delete(entity)
                    db.completedLessonDao().deleteCompletedLessonsByLessonId(lessonId)
                    db.bookmarkDao().deleteBookmarksByLessonId(lessonId)
                }
                runOnMain { onComplete(true) }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting lesson", e)
                runOnMain { onComplete(false) }
            }
        }
    }

    // --- LESSON PROGRESS ---
    fun saveUserProgress(lessonId: String, onComplete: (Boolean) -> Unit) {
        val uid = currentUserId ?: return onComplete(false)
        runOnBackground {
            try {
                val progress = CompletedLessonEntity(uid, lessonId, System.currentTimeMillis())
                db.completedLessonDao().insert(progress)
                runOnMain { onComplete(true) }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving progress", e)
                runOnMain { onComplete(false) }
            }
        }
    }

    fun getCompletedLessons(onComplete: (List<String>) -> Unit) {
        val uid = currentUserId ?: return onComplete(emptyList())
        runOnBackground {
            try {
                val list = db.completedLessonDao().getCompletedLessonIds(uid)
                runOnMain { onComplete(list) }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting completed lessons", e)
                runOnMain { onComplete(emptyList()) }
            }
        }
    }

    // --- BOOKMARKS ---
    fun toggleBookmark(lessonId: String, isBookmarked: Boolean, onComplete: (Boolean) -> Unit) {
        val uid = currentUserId ?: return onComplete(false)
        runOnBackground {
            try {
                if (isBookmarked) {
                    val bookmark = BookmarkEntity(uid, lessonId, System.currentTimeMillis())
                    db.bookmarkDao().insert(bookmark)
                    runOnMain { onComplete(true) }
                } else {
                    val bookmark = db.bookmarkDao().getBookmark(uid, lessonId)
                    if (bookmark != null) {
                        db.bookmarkDao().delete(bookmark)
                    }
                    runOnMain { onComplete(true) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling bookmark", e)
                runOnMain { onComplete(false) }
            }
        }
    }

    fun getBookmarkedLessons(onComplete: (List<String>) -> Unit) {
        val uid = currentUserId ?: return onComplete(emptyList())
        runOnBackground {
            try {
                val list = db.bookmarkDao().getBookmarkedLessonIds(uid)
                runOnMain { onComplete(list) }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting bookmarked lessons", e)
                runOnMain { onComplete(emptyList()) }
            }
        }
    }

    // --- QUIZ SCORES ---
    fun saveQuizScore(category: String, score: Int, maxScore: Int, onComplete: (Boolean) -> Unit) {
        val uid = currentUserId ?: return onComplete(false)
        runOnBackground {
            try {
                val quizScore = QuizScoreEntity(
                    userId = uid,
                    category = category,
                    score = score,
                    maxScore = maxScore,
                    timestamp = System.currentTimeMillis()
                )
                db.quizScoreDao().insert(quizScore)
                runOnMain { onComplete(true) }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving quiz score", e)
                runOnMain { onComplete(false) }
            }
        }
    }

    fun getQuizScores(onComplete: (List<QuizScore>) -> Unit) {
        val uid = currentUserId ?: return onComplete(emptyList())
        runOnBackground {
            try {
                val list = db.quizScoreDao().getQuizScores(uid).map {
                    QuizScore(it.category, it.score, it.maxScore, it.timestamp)
                }
                runOnMain { onComplete(list) }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting quiz scores", e)
                runOnMain { onComplete(emptyList()) }
            }
        }
    }

    // --- INITIAL STATIC LESSON DATA (USED FOR PRE-POPULATION ONLY) ---
    private fun getInitialStaticLessons(): List<Lesson> {
        return listOf(
            Lesson(
                id = "bio_1",
                title = "Sel sebagai Unit Terkecil Kehidupan",
                category = "Biologi",
                shortDescription = "Mempelajari bagian-bagian sel hewan dan tumbuhan serta fungsinya.",
                content = "Sel adalah unit struktural dan fungsional terkecil dari makhluk hidup. Berdasarkan ada tidaknya membran inti, sel dibagi menjadi prokariotik dan eukariotik.\n\nBagian utama sel eukariotik terdiri dari:\n1. Membran Sel: Mengatur transportasi zat masuk dan keluar sel.\n2. Sitoplasma: Cairan sel tempat berlangsungnya metabolisme.\n3. Inti Sel (Nukleus): Mengatur seluruh aktivitas sel.\n4. Organel Sel: Mitokondria (respirasi sel), Ribosom (sintesis protein), Kloroplas (fotosintesis pada tumbuhan).",
                codeSnippet = "Organisasi Kehidupan:\nSel -> Jaringan -> Organ -> Sistem Organ -> Organisme",
                difficulty = "Pemula"
            ),
            Lesson(
                id = "bio_2",
                title = "Sistem Pencernaan Manusia",
                category = "Biologi",
                shortDescription = "Memahami organ-organ pencernaan dan proses mencerna makanan.",
                content = "Pencernaan makanan manusia terbagi menjadi:\n1. Pencernaan Mekanis: Menghancurkan makanan menjadi ukuran kecil (di mulut oleh gigi, di lambung oleh dinding lambung).\n2. Pencernaan Kimiawi: Pemecahan zat makanan oleh enzim pencernaan.\n\nSaluran Pencernaan:\nMulut (enzim ptialin) -> Kerongkongan (gerak peristaltik) -> Lambung (asam klorida HCl, enzim pepsin & renin) -> Usus Halus (penyerapan sari makanan oleh vili) -> Usus Besar (penyerapan air) -> Anus (pembuangan feses).",
                codeSnippet = "Enzim Utama:\n- Ptialin: Amilum -> Maltosa\n- Pepsin: Protein -> Pepton\n- Amilase: Karbohidrat -> Glukosa",
                difficulty = "Menengah"
            ),
            Lesson(
                id = "fis_1",
                title = "Gaya dan Hukum Newton",
                category = "Fisika",
                shortDescription = "Belajar tentang pengaruh gaya terhadap benda dan Hukum I, II, III Newton.",
                content = "Gaya adalah tarikan atau dorongan yang dapat menyebabkan benda bergerak, berhenti, atau berubah bentuk.\n\nHukum Newton tentang Gerak:\n1. Hukum I Newton: Benda cenderung mempertahankan keadaannya (inersia). Jika gaya bekerja = 0, benda diam tetap diam, benda bergerak tetap bergerak lurus beraturan.\n2. Hukum II Newton: Percepatan benda sebanding dengan gaya dan berbanding terbalik dengan massa (F = m * a).\n3. Hukum III Newton: Gaya aksi-reaksi (Aksi = -Reaksi).",
                codeSnippet = "Rumus Hukum II Newton:\nF = m * a\nF: Gaya (Newton)\nm: Massa (kg)\na: Percepatan (m/s2)",
                difficulty = "Pemula"
            ),
            Lesson(
                id = "fis_2",
                title = "Usaha dan Energi",
                category = "Fisika",
                shortDescription = "Mengerti konsep usaha fisik dan macam-macam energi potensial & kinetik.",
                content = "Dalam fisika, usaha didefinisikan sebagai perkalian antara gaya dengan perpindahan benda (W = F * s).\n\nEnergi Mekanik terdiri dari:\n1. Energi Potensial (EP): Energi karena kedudukan benda. EP = m * g * h.\n2. Energi Kinetik (EK): Energi karena gerak benda. EK = 0.5 * m * v^2.\n\nHukum Kekekalan Energi menyatakan bahwa energi tidak dapat diciptakan atau dimusnahkan, melainkan hanya dapat berubah dari satu bentuk ke bentuk lainnya.",
                codeSnippet = "Rumus Usaha & Energi:\nW = F * s\nEP = m * g * h\nEK = 1/2 * m * v^2",
                difficulty = "Menengah"
            ),
            Lesson(
                id = "kim_1",
                title = "Unsur, Senyawa, dan Campuran",
                category = "Kimia",
                shortDescription = "Mengidentifikasi perbedaan zat tunggal dan campuran.",
                content = "Materi berdasarkan komposisinya dibagi menjadi zat tunggal dan campuran.\n\n1. Unsur: Zat tunggal yang paling sederhana dan tidak dapat dibagi lagi secara kimia biasa (contoh: Besi Fe, Oksigen O2, Emas Au).\n2. Senyawa: Zat tunggal hasil gabungan kimia dari dua atau lebih unsur berbeda dengan perbandingan tetap (contoh: Air H2O, Garam Dapur NaCl).\n3. Campuran: Penggabungan dua zat atau lebih tanpa melalui reaksi kimia. Ada campuran homogen (larutan) dan campuran heterogen (suspensi).",
                codeSnippet = "Perbedaan Senyawa vs Campuran:\n- Senyawa: Terbentuk lewat reaksi kimia, perbandingan massa tetap.\n- Campuran: Terbentuk secara fisik, perbandingan sembarang.",
                difficulty = "Pemula"
            ),
            Lesson(
                id = "kim_2",
                title = "Asam, Basa, dan Garam",
                category = "Kimia",
                shortDescription = "Mengenal sifat larutan asam basa serta skala pH.",
                content = "Larutan berdasarkan sifat keasamannya dibagi menjadi:\n1. Asam: pH < 7, rasa masam, korosif, mengubah lakmus biru menjadi merah, menghasilkan ion H+ di air.\n2. Basa: pH > 7, rasa pahit, licin (kaustik), mengubah lakmus merah menjadi biru, menghasilkan ion OH- di air.\n3. Garam: pH sekitar 7 (netral), hasil reaksi netralisasi antara asam dan basa (Asam + Basa -> Garam + Air).\n\nIndikator asam-basa digunakan untuk mendeteksi keasaman, contohnya kertas lakmus atau indikator alami seperti kunyit.",
                codeSnippet = "Reaksi Netralisasi:\nHCl (Asam) + NaOH (Basa) -> NaCl (Garam) + H2O (Air)",
                difficulty = "Menengah"
            )
        )
    }

    // --- QUESTIONS CRUD ---

    fun getQuestions(category: String, onComplete: (List<Question>) -> Unit) {
        runOnBackground {
            try {
                val list = db.questionDao().getQuestionsByCategory(category).map {
                    Question(
                        id = it.id,
                        category = it.category,
                        questionText = it.questionText,
                        optionA = it.optionA,
                        optionB = it.optionB,
                        optionC = it.optionC,
                        optionD = it.optionD,
                        correctOption = it.correctOption,
                        imagePath = it.imagePath
                    )
                }
                runOnMain { onComplete(list) }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting questions", e)
                runOnMain { onComplete(emptyList()) }
            }
        }
    }

    fun addQuestion(question: Question, onComplete: (Boolean) -> Unit) {
        runOnBackground {
            try {
                val finalId = if (question.id.isEmpty()) UUID.randomUUID().toString() else question.id
                db.questionDao().insert(
                    QuestionEntity(
                        id = finalId,
                        category = question.category,
                        questionText = question.questionText,
                        optionA = question.optionA,
                        optionB = question.optionB,
                        optionC = question.optionC,
                        optionD = question.optionD,
                        correctOption = question.correctOption,
                        imagePath = question.imagePath
                    )
                )
                runOnMain { onComplete(true) }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding question", e)
                runOnMain { onComplete(false) }
            }
        }
    }

    fun updateQuestion(question: Question, onComplete: (Boolean) -> Unit) {
        runOnBackground {
            try {
                db.questionDao().insert(
                    QuestionEntity(
                        id = question.id,
                        category = question.category,
                        questionText = question.questionText,
                        optionA = question.optionA,
                        optionB = question.optionB,
                        optionC = question.optionC,
                        optionD = question.optionD,
                        correctOption = question.correctOption,
                        imagePath = question.imagePath
                    )
                )
                runOnMain { onComplete(true) }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating question", e)
                runOnMain { onComplete(false) }
            }
        }
    }

    fun deleteQuestion(questionId: String, onComplete: (Boolean) -> Unit) {
        runOnBackground {
            try {
                val entity = db.questionDao().getAllQuestions().firstOrNull { it.id == questionId }
                if (entity != null) {
                    db.questionDao().delete(entity)
                }
                runOnMain { onComplete(true) }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting question", e)
                runOnMain { onComplete(false) }
            }
        }
    }

    // --- INITIAL STATIC QUESTIONS DATA (USED FOR PRE-POPULATION ONLY) ---
    private fun getInitialStaticQuestionsList(): List<Question> {
        return listOf(
            // --- BIOLOGI ---
            Question(
                id = "q_bio_1",
                category = "Biologi",
                questionText = "Bagian sel yang berfungsi sebagai pelindung dan pengatur zat yang keluar masuk sel adalah...",
                optionA = "Dinding sel",
                optionB = "Membran sel",
                optionC = "Sitoplasma",
                optionD = "Inti sel",
                correctOption = "B"
            ),
            Question(
                id = "q_bio_2",
                category = "Biologi",
                questionText = "Organ pada sistem pencernaan manusia yang berfungsi menyerap sari-sari makanan adalah...",
                optionA = "Lambung",
                optionB = "Usus halus",
                optionC = "Usus besar",
                optionD = "Kerongkongan",
                correctOption = "B"
            ),
            Question(
                id = "q_bio_3",
                category = "Biologi",
                questionText = "Hubungan timbal balik antara dua makhluk hidup yang saling menguntungkan disebut...",
                optionA = "Simbiosis parasitisme",
                optionB = "Simbiosis komensalisme",
                optionC = "Simbiosis mutualisme",
                optionD = "Predatorisme",
                correctOption = "C"
            ),
            // --- FISIKA ---
            Question(
                id = "q_fis_1",
                category = "Fisika",
                questionText = "Satuan Internasional (SI) untuk mengukur gaya adalah...",
                optionA = "Joule",
                optionB = "Watt",
                optionC = "Newton",
                optionD = "Pascal",
                correctOption = "C"
            ),
            Question(
                id = "q_fis_2",
                category = "Fisika",
                questionText = "Energi yang dimiliki oleh benda karena kedudukannya atau posisinya disebut...",
                optionA = "Energi kinetik",
                optionB = "Energi potensial",
                optionC = "Energi mekanik",
                optionD = "Energi kalor",
                correctOption = "B"
            ),
            Question(
                id = "q_fis_3",
                category = "Fisika",
                questionText = "Alat optik yang digunakan untuk melihat benda-benda yang sangat kecil seperti bakteri atau sel disebut...",
                optionA = "Kaca pembesar",
                optionB = "Mikroskop",
                optionC = "Teleskop",
                optionD = "Periskop",
                correctOption = "B"
            ),
            // --- KIMIA ---
            Question(
                id = "q_kim_1",
                category = "Kimia",
                questionText = "Zat tunggal yang tidak dapat diuraikan lagi menjadi zat lain yang lebih sederhana melalui reaksi kimia biasa disebut...",
                optionA = "Unsur",
                optionB = "Senyawa",
                optionC = "Campuran homogen",
                optionD = "Campuran heterogen",
                correctOption = "A"
            ),
            Question(
                id = "q_kim_2",
                category = "Kimia",
                questionText = "Larutan yang memiliki nilai pH kurang dari 7 dan dapat mengubah kertas lakmus biru menjadi merah disebut...",
                optionA = "Larutan basa",
                optionB = "Larutan asam",
                optionC = "Larutan netral",
                optionD = "Larutan garam",
                correctOption = "B"
            ),
            Question(
                id = "q_kim_3",
                category = "Kimia",
                questionText = "Partikel terkecil dari suatu unsur yang masih memiliki sifat unsur tersebut disebut...",
                optionA = "Molekul",
                optionB = "Ion",
                optionC = "Atom",
                optionD = "Senyawa",
                correctOption = "C"
            )
        )
    }
}

