package com.example.mediapembelajaran

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class TeacherQuizListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_quiz_list, container, false)

        // Hide back button in toolbar since it's a bottom navigation fragment now!
        val btnBack = view.findViewById<View>(R.id.btnBack)
        btnBack.visibility = View.GONE

        val clickListener = View.OnClickListener { v ->
            val category = when (v.id) {
                R.id.cardManageBio -> "Biologi"
                R.id.cardManageFis -> "Fisika"
                R.id.cardManageKim -> "Kimia"
                else -> "Biologi"
            }

            val intent = Intent(context, ManageQuestionsActivity::class.java).apply {
                putExtra("QUIZ_CATEGORY", category)
            }
            startActivity(intent)
        }

        view.findViewById<View>(R.id.cardManageBio).setOnClickListener(clickListener)
        view.findViewById<View>(R.id.cardManageFis).setOnClickListener(clickListener)
        view.findViewById<View>(R.id.cardManageKim).setOnClickListener(clickListener)

        return view
    }
}
