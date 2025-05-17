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
            tvMuscleGroup.text = translateMuscleGroup(ejercicio.grupoMuscular, itemView.context)
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
            "beginner", "basico", "básico" -> context.getString(R.string.difficulty_basic)
            "intermediate", "intermedio" -> context.getString(R.string.difficulty_intermediate)
            "advanced", "avanzado" -> context.getString(R.string.difficulty_advanced)
            "adaptable" -> context.getString(R.string.difficulty_adaptable)
            else -> difficulty
        }
    }

    private fun translateMuscleGroup(muscleGroup: String, context: Context): String {
        return when (muscleGroup.lowercase()) {
            "legs", "piernas" -> context.getString(R.string.muscle_group_legs)
            "arms", "brazos" -> context.getString(R.string.muscle_group_arms)
            "core" -> context.getString(R.string.muscle_group_core)
            "multiple", "múltiple", "multiples", "múltiples" -> context.getString(R.string.muscle_group_multiple)
            "shoulders", "hombros" -> context.getString(R.string.muscle_group_shoulders)
            "respiratory", "respiratorio" -> context.getString(R.string.muscle_group_respiratory)
            "forearms", "antebrazos" -> context.getString(R.string.muscle_group_forearms)
            "core and legs", "core y piernas" -> context.getString(R.string.muscle_group_core_legs)
            "chest", "pecho" -> context.getString(R.string.muscle_group_chest)
            "back", "espalda" -> context.getString(R.string.muscle_group_back)
            "glutes", "glúteos" -> context.getString(R.string.muscle_group_glutes)
            "full body", "cuerpo completo" -> context.getString(R.string.muscle_group_full_body)
            else -> muscleGroup
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
            tvMuscleGroup.text = translateMuscleGroup(ejercicio.grupoMuscular, itemView.context)
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
            "beginner", "basico", "básico" -> context.getString(R.string.difficulty_basic)
            "intermediate", "intermedio" -> context.getString(R.string.difficulty_intermediate)
            "advanced", "avanzado" -> context.getString(R.string.difficulty_advanced)
            "adaptable" -> context.getString(R.string.difficulty_adaptable)
            else -> difficulty
        }
    }

    private fun translateMuscleGroup(muscleGroup: String, context: Context): String {
        return when (muscleGroup.lowercase()) {
            "legs", "piernas" -> context.getString(R.string.muscle_group_legs)
            "arms", "brazos" -> context.getString(R.string.muscle_group_arms)
            "core" -> context.getString(R.string.muscle_group_core)
            "multiple", "múltiple", "multiples", "múltiples" -> context.getString(R.string.muscle_group_multiple)
            "shoulders", "hombros" -> context.getString(R.string.muscle_group_shoulders)
            "respiratory", "respiratorio" -> context.getString(R.string.muscle_group_respiratory)
            "forearms", "antebrazos" -> context.getString(R.string.muscle_group_forearms)
            "core and legs", "core y piernas" -> context.getString(R.string.muscle_group_core_legs)
            "chest", "pecho" -> context.getString(R.string.muscle_group_chest)
            "back", "espalda" -> context.getString(R.string.muscle_group_back)
            "glutes", "glúteos" -> context.getString(R.string.muscle_group_glutes)
            "full body", "cuerpo completo" -> context.getString(R.string.muscle_group_full_body)
            else -> muscleGroup
        }
    }
}