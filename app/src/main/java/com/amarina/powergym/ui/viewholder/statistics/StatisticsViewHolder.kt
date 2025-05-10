package com.amarina.powergym.ui.viewholder.statistics

import androidx.recyclerview.widget.RecyclerView
import com.amarina.powergym.R
import com.amarina.powergym.database.entities.Estadistica
import com.amarina.powergym.databinding.ItemStatisticsBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class StatisticsViewHolder(private val binding: ItemStatisticsBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(estadistica: Estadistica) {
        // Datos principales
        binding.tvNombreEjercicio.text = estadistica.nombreEjercicio
        binding.tvGrupoMuscular.text = estadistica.grupoMuscular

        // Formatear fecha legible
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(Date(estadistica.fecha))
        binding.tvFecha.text = "Fecha: $fecha"

        // Datos de rendimiento con formato visual mejorado
        binding.tvCaloriasQuemadas.text = "**${estadistica.caloriasQuemadas}** cal"

        // Convertir tiempo a minutos para mejor visualización
        val tiempoMinutos = estadistica.tiempoTotal / 60000
        binding.tvTiempoTotal.text = "**$tiempoMinutos** min"

        // Series y repeticiones
        binding.tvSeries.text = "${estadistica.series} series"
        binding.tvRepeticiones.text = "${estadistica.repeticiones} reps"

        // Cargar imagen del ejercicio si existe
        estadistica.imagenUrl?.let { url ->
            if (url.isNotEmpty()) {
                Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.workout_placeholder_image)
                    .error(R.drawable.baseline_error_24)
                    .into(binding.ivEjercicio)
            }
        }
    }
}
