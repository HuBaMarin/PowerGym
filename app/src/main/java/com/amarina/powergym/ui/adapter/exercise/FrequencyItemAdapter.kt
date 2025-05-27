package com.amarina.powergym.ui.adapter.exercise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amarina.powergym.R

class FrequencyItemAdapter : RecyclerView.Adapter<FrequencyItemAdapter.FrequencyViewHolder>() {

    private var items: List<Pair<String, Int>> = emptyList()
    private var maxCount: Int = 0

    fun submitList(newList: List<Pair<String, Int>>) {
        items = newList
        maxCount = if (newList.isNotEmpty()) newList.maxOf { it.second } else 0
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FrequencyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_frequency, parent, false)
        return FrequencyViewHolder(view)
    }

    override fun onBindViewHolder(holder: FrequencyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class FrequencyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvCount: TextView = itemView.findViewById(R.id.tvCount)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)

        fun bind(item: Pair<String, Int>) {
            tvName.text = item.first
            tvCount.text = itemView.context.getString(R.string.times_completed, item.second)

            // Configurar icono según el ejercicio
            setIconForExercise(item.first)
        }

        private fun setIconForExercise(exerciseName: String) {
            val iconResId = when {

                else -> R.drawable.gym_weight
            }
            ivIcon.setImageResource(iconResId)
        }
    }
}
