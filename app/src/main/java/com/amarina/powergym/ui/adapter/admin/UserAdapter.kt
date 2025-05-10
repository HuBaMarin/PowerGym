package com.amarina.powergym.ui.adapter.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarina.powergym.database.entities.Usuario
import com.amarina.powergym.databinding.ItemUserBinding

class UserAdapter(
    private val onEditClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : ListAdapter<Usuario, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    inner class UserViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: Usuario) {
            with(binding) {
                tvUserName.text = user.nombre.ifEmpty { "Sin nombre" }
                tvUserEmail.text = user.email
                tvUserRole.text = user.rol
                tvRegistrationDate.text = "Registrado: ${formatDate(user.fechaRegistro)}"

                btnEditUser.setOnClickListener {
                    onEditClick(user.id)
                }

                btnDeleteUser.setOnClickListener {
                    onDeleteClick(user.id)
                }

                // Highlight admin users
                if (user.rol.equals("admin", ignoreCase = true)) {
                    root.setBackgroundResource(android.R.color.holo_blue_light)
                } else {
                    root.setBackgroundResource(android.R.color.transparent)
                }
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<Usuario>() {
    override fun areItemsTheSame(oldItem: Usuario, newItem: Usuario): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Usuario, newItem: Usuario): Boolean {
        return oldItem == newItem
    }
}