package com.example.mediapembelajaran

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TeacherScoreFragment : Fragment() {

    private lateinit var rvScores: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: TeacherScoresAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacher_score, container, false)

        rvScores = view.findViewById(R.id.rvTeacherScores)
        tvEmpty = view.findViewById(R.id.tvEmptyScores)

        rvScores.layoutManager = LinearLayoutManager(context)
        adapter = TeacherScoresAdapter(emptyList())
        rvScores.adapter = adapter

        loadScores()

        return view
    }

    private fun loadScores() {
        DatabaseHelper.getStudentScores { list ->
            if (isAdded) {
                if (list.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rvScores.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    rvScores.visibility = View.VISIBLE
                    adapter.updateList(list)
                }
            }
        }
    }

    private class TeacherScoresAdapter(private var list: List<StudentScoreQueryRow>) :
        RecyclerView.Adapter<TeacherScoresAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvScoreStudentName)
            val tvEmail: TextView = view.findViewById(R.id.tvScoreStudentEmail)
            val tvCategory: TextView = view.findViewById(R.id.tvScoreCategory)
            val tvTimestamp: TextView = view.findViewById(R.id.tvScoreTimestamp)
            val tvValue: TextView = view.findViewById(R.id.tvScoreValue)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_teacher_score, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvName.text = item.studentName
            holder.tvEmail.text = item.studentEmail
            holder.tvCategory.text = item.category
            
            // Format timestamp
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            holder.tvTimestamp.text = sdf.format(Date(item.timestamp))

            holder.tvValue.text = "${item.score}/${item.maxScore}"
        }

        override fun getItemCount() = list.size

        fun updateList(newList: List<StudentScoreQueryRow>) {
            list = newList
            notifyDataSetChanged()
        }
    }
}
