package com.amarina.powergym.ui.adapter.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarina.powergym.database.entities.Ejercicio
import com.amarina.powergym.databinding.ItemExerciseAdminBinding
import com.squareup.picasso.Picasso

class ExerciseAdapter(
    private val onEditClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : ListAdapter<Ejercicio, ExerciseAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExerciseAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ejercicio = getItem(position)
        holder.bind(ejercicio)
    }

    inner class ViewHolder(private val binding: ItemExerciseAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ejercicio: Ejercicio) {
            with(binding) {
                tvExerciseName.text = ejercicio.nombre
                tvMuscleGroup.text = "Muscle Group: ${ejercicio.grupoMuscular}"
                tvDifficulty.text = "Difficulty: ${ejercicio.dificultad}"

                // Load exercise image with Picasso
                Picasso.get()
                    .load(ejercicio.imagenEjercicio)
                    .fit()
                    .centerCrop()
                    .into(ivExerciseImage)

                // Set click listeners for edit and delete buttons
                btnEdit.setOnClickListener { onEditClick(ejercicio.id) }
                btnDelete.setOnClickListener { onDeleteClick(ejercicio.id) }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Ejercicio>() {
            override fun areItemsTheSame(oldItem: Ejercicio, newItem: Ejercicio): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Ejercicio, newItem: Ejercicio): Boolean {
                return oldItem == newItem
            }
        }
    }
}