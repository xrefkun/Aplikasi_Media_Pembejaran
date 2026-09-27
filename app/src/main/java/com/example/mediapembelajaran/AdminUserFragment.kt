package com.example.mediapembelajaran

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AdminUserFragment : Fragment() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: AdminUsersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_user, container, false)

        rvUsers = view.findViewById(R.id.rvAdminUsers)
        tvEmpty = view.findViewById(R.id.tvEmptyUsers)

        rvUsers.layoutManager = LinearLayoutManager(context)
        adapter = AdminUsersAdapter(
            emptyList(),
            onEditRole = { user -> showEditRoleDialog(user) },
            onDeleteUser = { user -> showDeleteUserDialog(user) }
        )
        rvUsers.adapter = adapter

        loadUsers()

        return view
    }

    private fun loadUsers() {
        DatabaseHelper.getAllUsers { list ->
            if (isAdded) {
                if (list.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rvUsers.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    rvUsers.visibility = View.VISIBLE
                    adapter.updateList(list)
                }
            }
        }
    }

    private fun showEditRoleDialog(user: UserEntity) {
        val context = context ?: return
        val roles = arrayOf("Siswa", "Guru", "Admin")
        val dbRoles = arrayOf("SISWA", "GURU", "ADMIN")
        val currentRoleIndex = dbRoles.indexOf(user.role)

        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Ubah Peran ${user.username}")
            .setSingleChoiceItems(roles, currentRoleIndex) { dialog, which ->
                val selectedRole = dbRoles[which]
                DatabaseHelper.updateUserRole(user.id, selectedRole) { success ->
                    if (success) {
                        Toast.makeText(context, "Peran berhasil diubah ke $selectedRole", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    } else {
                        Toast.makeText(context, "Gagal mengubah peran", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteUserDialog(user: UserEntity) {
        val context = context ?: return
        if (user.id == DatabaseHelper.currentUserId) {
            Toast.makeText(context, "Anda tidak dapat menghapus akun Anda sendiri", Toast.LENGTH_SHORT).show()
            return
        }

        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Hapus Pengguna")
            .setMessage("Apakah Anda yakin ingin menghapus akun ${user.username}?")
            .setPositiveButton("Ya") { _, _ ->
                DatabaseHelper.deleteUser(user.id) { success ->
                    if (success) {
                        Toast.makeText(context, "Akun ${user.username} berhasil dihapus", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    } else {
                        Toast.makeText(context, "Gagal menghapus akun", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private class AdminUsersAdapter(
        private var list: List<UserEntity>,
        private val onEditRole: (UserEntity) -> Unit,
        private val onDeleteUser: (UserEntity) -> Unit
    ) : RecyclerView.Adapter<AdminUsersAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvUsername: TextView = view.findViewById(R.id.tvAdminUsername)
            val tvEmail: TextView = view.findViewById(R.id.tvAdminEmail)
            val tvRoleBadge: TextView = view.findViewById(R.id.tvAdminRoleBadge)
            val btnEdit: ImageButton = view.findViewById(R.id.btnAdminEditRole)
            val btnDelete: ImageButton = view.findViewById(R.id.btnAdminDeleteUser)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_admin_user, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvUsername.text = item.username
            holder.tvEmail.text = item.email
            holder.tvRoleBadge.text = item.role

            // Badge Color & Background Tint based on role
            val context = holder.itemView.context
            when (item.role) {
                "ADMIN" -> {
                    holder.tvRoleBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        context.resources.getColor(android.R.color.holo_red_dark, null)
                    )
                }
                "GURU" -> {
                    holder.tvRoleBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        context.resources.getColor(android.R.color.holo_green_dark, null)
                    )
                }
                else -> { // "SISWA"
                    holder.tvRoleBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        context.resources.getColor(android.R.color.holo_blue_dark, null)
                    )
                }
            }

            holder.btnEdit.setOnClickListener { onEditRole(item) }
            holder.btnDelete.setOnClickListener { onDeleteUser(item) }
        }

        override fun getItemCount() = list.size

        fun updateList(newList: List<UserEntity>) {
            list = newList
            notifyDataSetChanged()
        }
    }
}
