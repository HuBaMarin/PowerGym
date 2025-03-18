package com.amarin.powergym.ui

import androidx.recyclerview.widget.RecyclerView
import com.amarin.powergym.database.entities.Actividad
import com.amarin.powergym.databinding.ActivityAutenticacionBinding

class ActividadViewHolder(val binding: ActivityAutenticacionBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(actividad: Actividad) {
//        binding.tvNombre.text = actividad.nombre
//        binding.tvDescripcion.text = actividad.descripcion
//        binding.tvFecha.text = actividad.fecha
    }
}
