package com.amarina.powergym.ui.adapter.statistics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.amarina.powergym.database.entities.Estadistica
import com.amarina.powergym.databinding.ItemStatisticsBinding
import com.amarina.powergym.ui.viewholder.statistics.StatisticsViewHolder

class StatisticsAdapter : ListAdapter<Estadistica, StatisticsViewHolder>(
    object : DiffUtil.ItemCallback<Estadistica>() {
        override fun areItemsTheSame(oldItem: Estadistica, newItem: Estadistica): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Estadistica, newItem: Estadistica): Boolean {
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