package com.amarina.powergym.ui

import androidx.recyclerview.widget.RecyclerView
import com.amarina.powergym.R
import com.amarina.powergym.database.entities.Ejercicio
import com.amarina.powergym.databinding.ItemExerciseBinding
import com.squareup.picasso.Picasso

class EjercicioViewHolder(private val binding: ItemExerciseBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ejercicio: Ejercicio) {
        binding.apply {
            tvExerciseTitle.text = ejercicio.nombre
            tvMuscleGroup.text = ejercicio.grupoMuscular
            chipDifficulty.text = ejercicio.dificultad

            Picasso.get()
                .load(ejercicio.urlEjercicio)
                .fit()
                .centerCrop()
                .error(R.drawable.baseline_error_24)
                .placeholder(R.drawable.loading_16_svgrepo_com)
                .into(ivExerciseImage)
        }
    }
}