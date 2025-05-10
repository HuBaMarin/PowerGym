package com.amarina.powergym.ui.viewholder.exercise

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.amarina.powergym.R
import com.amarina.powergym.database.entities.Ejercicio
import com.amarina.powergym.databinding.ItemExerciseBinding
import com.amarina.powergym.databinding.ItemExerciseListBinding
import com.squareup.picasso.Picasso

class EjercicioGridViewHolder(
    private val binding: ItemExerciseBinding,
    private val onItemClick: (Ejercicio) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(ejercicio: Ejercicio) {
        with(binding) {
            tvExerciseTitle.text = ejercicio.nombre
            tvMuscleGroup.text = ejercicio.grupoMuscular
            chipDifficulty.text = translateDifficulty(ejercicio.dificultad, itemView.context)

            Picasso.get()
                .load(ejercicio.imagenEjercicio)
                .placeholder(R.drawable.workout_placeholder_image)
                .error(R.drawable.baseline_error_24)
                .fit()
                .centerCrop()
                .into(ivExerciseImage)

            root.setOnClickListener {
                onItemClick(ejercicio)
            }
        }
    }

    private fun translateDifficulty(difficulty: String, context: Context): String {
        return when (difficulty.lowercase()) {
            "beginner", "basico" -> context.getString(R.string.basic)
            "intermediate", "intermedio" -> context.getString(R.string.medium)
            "advanced", "avanzado" -> context.getString(R.string.advanced)
            else -> difficulty
        }
    }
}

class EjercicioListViewHolder(
    private val binding: ItemExerciseListBinding,
    private val onItemClick: (Ejercicio) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(ejercicio: Ejercicio) {
        with(binding) {
            tvExerciseTitle.text = ejercicio.nombre
            tvMuscleGroup.text = ejercicio.grupoMuscular
            chipDifficulty.text = translateDifficulty(ejercicio.dificultad, itemView.context)

            Picasso.get()
                .load(ejercicio.imagenEjercicio)
                .placeholder(R.drawable.workout_placeholder_image)
                .error(R.drawable.baseline_error_24)
                .fit()
                .centerCrop()
                .into(ivExerciseImage)

            root.setOnClickListener {
                onItemClick(ejercicio)
            }
        }
    }

    private fun translateDifficulty(difficulty: String, context: Context): String {
        return when (difficulty.lowercase()) {
            "beginner", "basico" -> context.getString(R.string.basic)
            "intermediate", "intermedio" -> context.getString(R.string.medium)
            "advanced", "avanzado" -> context.getString(R.string.advanced)
            else -> difficulty
        }
    }
}