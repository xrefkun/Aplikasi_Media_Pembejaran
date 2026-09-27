package com.example.mediapembelajaran

import android.content.Context
import androidx.room.*

// --- ENTITIES ---

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val password: String,
    val role: String // "SISWA", "GURU", "ADMIN"
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val shortDescription: String,
    val content: String,
    val codeSnippet: String,
    val difficulty: String // Pemula, Menengah, Mahir
)

@Entity(
    tableName = "completed_lessons",
    primaryKeys = ["userId", "lessonId"]
)
data class CompletedLessonEntity(
    val userId: String,
    val lessonId: String,
    val completedAt: Long
)

@Entity(
    tableName = "bookmarks",
    primaryKeys = ["userId", "lessonId"]
)
data class BookmarkEntity(
    val userId: String,
    val lessonId: String,
    val bookmarkedAt: Long
)

@Entity(tableName = "quiz_scores")
data class QuizScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val category: String,
    val score: Int,
    val maxScore: Int,
    val timestamp: Long
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val category: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String, // A, B, C, or D
    val imagePath: String = ""
)

// --- QUERY DATA CLASS FOR GURU ---
data class StudentScoreQueryRow(
    val studentName: String,
    val studentEmail: String,
    val category: String,
    val score: Int,
    val maxScore: Int,
    val timestamp: Long
)

// --- DAOS ---

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY username ASC")
    fun getAllUsers(): List<UserEntity>

    @Query("UPDATE users SET role = :newRole WHERE id = :id")
    fun updateUserRole(id: String, newRole: String)

    @Query("DELETE FROM users WHERE id = :id")
    fun deleteUser(id: String)
}

@Dao
interface LessonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(lesson: LessonEntity)

    @Update
    fun update(lesson: LessonEntity)

    @Delete
    fun delete(lesson: LessonEntity)

    @Query("SELECT * FROM lessons")
    fun getAllLessons(): List<LessonEntity>

    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    fun getLessonById(id: String): LessonEntity?
}

@Dao
interface CompletedLessonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(completed: CompletedLessonEntity)

    @Query("SELECT lessonId FROM completed_lessons WHERE userId = :userId")
    fun getCompletedLessonIds(userId: String): List<String>
    
    @Query("DELETE FROM completed_lessons WHERE lessonId = :lessonId")
    fun deleteCompletedLessonsByLessonId(lessonId: String)
}

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bookmark: BookmarkEntity)

    @Delete
    fun delete(bookmark: BookmarkEntity)

    @Query("SELECT * FROM bookmarks WHERE userId = :userId AND lessonId = :lessonId LIMIT 1")
    fun getBookmark(userId: String, lessonId: String): BookmarkEntity?

    @Query("SELECT lessonId FROM bookmarks WHERE userId = :userId")
    fun getBookmarkedLessonIds(userId: String): List<String>

    @Query("DELETE FROM bookmarks WHERE lessonId = :lessonId")
    fun deleteBookmarksByLessonId(lessonId: String)
}

@Dao
interface QuizScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(score: QuizScoreEntity)

    @Query("SELECT * FROM quiz_scores WHERE userId = :userId ORDER BY timestamp DESC")
    fun getQuizScores(userId: String): List<QuizScoreEntity>

    @Query("""
        SELECT users.username as studentName, users.email as studentEmail, 
               quiz_scores.category as category, quiz_scores.score as score, 
               quiz_scores.maxScore as maxScore, quiz_scores.timestamp as timestamp
        FROM quiz_scores 
        INNER JOIN users ON quiz_scores.userId = users.id 
        ORDER BY quiz_scores.timestamp DESC
    """)
    fun getStudentScores(): List<StudentScoreQueryRow>
}

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(question: QuestionEntity)

    @Update
    fun update(question: QuestionEntity)

    @Delete
    fun delete(question: QuestionEntity)

    @Query("SELECT * FROM questions WHERE category = :category")
    fun getQuestionsByCategory(category: String): List<QuestionEntity>

    @Query("SELECT * FROM questions")
    fun getAllQuestions(): List<QuestionEntity>
}

// --- DATABASE ---

@Database(
    entities = [
        UserEntity::class,
        LessonEntity::class,
        CompletedLessonEntity::class,
        BookmarkEntity::class,
        QuizScoreEntity::class,
        QuestionEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun lessonDao(): LessonDao
    abstract fun completedLessonDao(): CompletedLessonDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun quizScoreDao(): QuizScoreDao
    abstract fun questionDao(): QuestionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "media_pembelajaran_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

