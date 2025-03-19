package com.amarina.powergym.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarina.powergym.database.entities.Estadistica
import com.amarina.powergym.utils.Utils

class StatisticsAdapter : ListAdapter<Estadistica, StatisticsAdapter.StatisticsViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticsViewHolder {
        return StatisticsViewHolder(
            ItemEstadisticaBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: StatisticsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StatisticsViewHolder(
        private val binding: ItemEstadisticaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(estadistica: Estadistica) {
            binding.apply {
                tvFecha.text = Utils.formatDate(estadistica.fecha)
                tvEjercicios.text = estadistica.ejerciciosCompletados.toString()
                tvTiempo.text = Utils.formatTime(estadistica.tiempoTotal)
                tvCalorias.text = "${estadistica.caloriasQuemadas} cal"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Estadistica>() {
        override fun areItemsTheSame(oldItem: Estadistica, newItem: Estadistica): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Estadistica, newItem: Estadistica): Boolean {
            return oldItem == newItem
        }
    }
}
