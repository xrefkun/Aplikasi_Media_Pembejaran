package com.example.mediapembelajaran

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LessonAdapter(
    private var lessons: List<Lesson>,
    private var completedIds: List<String>,
    private var bookmarkedIds: List<String>,
    private val onItemClick: (Lesson) -> Unit
) : RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {

    fun updateData(
        newLessons: List<Lesson>,
        newCompletedIds: List<String>,
        newBookmarkedIds: List<String>
    ) {
        lessons = newLessons
        completedIds = newCompletedIds
        bookmarkedIds = newBookmarkedIds
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lesson, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val lesson = lessons[position]
        holder.bind(lesson, completedIds.contains(lesson.id), bookmarkedIds.contains(lesson.id))
        holder.itemView.setOnClickListener { onItemClick(lesson) }
    }

    override fun getItemCount(): Int = lessons.size

    class LessonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDifficulty: TextView = itemView.findViewById(R.id.tvDifficulty)
        private val tvLessonTitle: TextView = itemView.findViewById(R.id.tvLessonTitle)
        private val tvLessonShortDesc: TextView = itemView.findViewById(R.id.tvLessonShortDesc)
        private val ivCompleted: ImageView = itemView.findViewById(R.id.ivCompleted)
        private val ivBookmarked: ImageView = itemView.findViewById(R.id.ivBookmarked)

        fun bind(lesson: Lesson, isCompleted: Boolean, isBookmarked: Boolean) {
            tvLessonTitle.text = lesson.title
            tvLessonShortDesc.text = lesson.shortDescription
            tvCategory.text = lesson.category
            tvDifficulty.text = lesson.difficulty

            // Handle completion indicator
            ivCompleted.visibility = if (isCompleted) View.VISIBLE else View.GONE

            // Handle bookmark indicator
            ivBookmarked.visibility = if (isBookmarked) View.VISIBLE else View.GONE
        }
    }
}
