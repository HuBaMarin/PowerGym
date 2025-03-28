package com.amarina.powergym.ui.statistics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.amarina.powergym.database.entities.Estadistica
import com.amarina.powergym.database.entities.EstadisticaConEjercicio
import com.amarina.powergym.databinding.ItemStatisticsBinding

class StatisticsAdapter : ListAdapter<EstadisticaConEjercicio, StatisticsViewHolder>(
    object : DiffUtil.ItemCallback<EstadisticaConEjercicio>() {
        override fun areItemsTheSame(oldItem: EstadisticaConEjercicio, newItem: EstadisticaConEjercicio): Boolean {
            return oldItem.estadistica.id == newItem.estadistica.id
        }

        override fun areContentsTheSame(oldItem: EstadisticaConEjercicio, newItem: EstadisticaConEjercicio): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticsViewHolder {
        val binding = ItemStatisticsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StatisticsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatisticsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}