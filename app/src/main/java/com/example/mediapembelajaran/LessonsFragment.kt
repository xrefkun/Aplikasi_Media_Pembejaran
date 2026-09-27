package com.example.mediapembelajaran

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class LessonsFragment : Fragment() {

    private lateinit var etSearch: EditText
    private lateinit var rvLessons: RecyclerView
    private lateinit var adapter: LessonAdapter

    private var allLessons: List<Lesson> = emptyList()
    private var filteredLessons: List<Lesson> = emptyList()
    private var completedIds: List<String> = emptyList()
    private var bookmarkedIds: List<String> = emptyList()

    private var selectedCategory: String = "Semua"
    private var searchQuery: String = ""

    private lateinit var tabAll: TextView
    private lateinit var tabKotlin: TextView
    private lateinit var tabAndroid: TextView
    private lateinit var tabStorage: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lessons, container, false)

        etSearch = view.findViewById(R.id.etSearch)
        rvLessons = view.findViewById(R.id.rvLessons)

        tabAll = view.findViewById(R.id.tabAll)
        tabKotlin = view.findViewById(R.id.tabKotlin)
        tabAndroid = view.findViewById(R.id.tabAndroid)
        tabStorage = view.findViewById(R.id.tabStorage)

        val fabAddLesson = view.findViewById<FloatingActionButton>(R.id.fabAddLesson)
        val role = DatabaseHelper.currentUserRole
        if (role == "GURU" || role == "ADMIN") {
            fabAddLesson.visibility = View.VISIBLE
            fabAddLesson.setOnClickListener {
                val intent = Intent(context, AddEditLessonActivity::class.java)
                startActivity(intent)
            }
        } else {
            fabAddLesson.visibility = View.GONE
        }

        setupRecyclerView()
        setupCategoryFilters()
        setupSearch()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun setupRecyclerView() {
        rvLessons.layoutManager = LinearLayoutManager(context)
        adapter = LessonAdapter(filteredLessons, completedIds, bookmarkedIds) { lesson ->
            val intent = Intent(context, LessonDetailActivity::class.java).apply {
                putExtra("LESSON_ID", lesson.id)
            }
            startActivity(intent)
        }
        rvLessons.adapter = adapter
    }

    private fun loadUserData() {
        DatabaseHelper.getLessons { lessons ->
            DatabaseHelper.getCompletedLessons { completed ->
                DatabaseHelper.getBookmarkedLessons { bookmarked ->
                    if (isAdded) {
                        allLessons = lessons
                        completedIds = completed
                        bookmarkedIds = bookmarked
                        applyFilter()
                    }
                }
            }
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString().trim()
                applyFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupCategoryFilters() {
        val clickListener = View.OnClickListener { v ->
            // Reset all tab styles
            resetTabStyles()

            // Highlight selected tab
            val tv = v as TextView
            tv.setBackgroundResource(R.drawable.gradient_button)
            tv.backgroundTintList = null // Remove tint to show gradient
            tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

            selectedCategory = when (v.id) {
                R.id.tabKotlin -> "Biologi"
                R.id.tabAndroid -> "Fisika"
                R.id.tabStorage -> "Kimia"
                else -> "Semua"
            }

            applyFilter()
        }

        tabAll.setOnClickListener(clickListener)
        tabKotlin.setOnClickListener(clickListener)
        tabAndroid.setOnClickListener(clickListener)
        tabStorage.setOnClickListener(clickListener)
    }

    private fun resetTabStyles() {
        val tabs = listOf(tabAll, tabKotlin, tabAndroid, tabStorage)
        for (tab in tabs) {
            tab.setBackgroundResource(R.drawable.edittext_bg)
            tab.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.white)
            tab.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        }
    }

    private fun applyFilter() {
        var result = allLessons

        // Filter by category
        if (selectedCategory != "Semua") {
            result = result.filter { it.category == selectedCategory }
        }

        // Filter by search query
        if (searchQuery.isNotEmpty()) {
            val query = searchQuery.lowercase(Locale.ROOT)
            result = result.filter {
                it.title.lowercase(Locale.ROOT).contains(query) ||
                it.shortDescription.lowercase(Locale.ROOT).contains(query)
            }
        }

        filteredLessons = result
        adapter.updateData(filteredLessons, completedIds, bookmarkedIds)
    }
}
