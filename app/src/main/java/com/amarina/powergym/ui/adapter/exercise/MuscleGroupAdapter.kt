package com.amarina.powergym.ui.adapter.exercise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amarina.powergym.R

/**
 * Simple data class to hold muscle group data
 */
data class MuscleGroupCount(
    val name: String,
    val count: Int
)

/**
 * Simplified adapter that shows muscle group name and workout count
 */
class MuscleGroupAdapter : RecyclerView.Adapter<MuscleGroupAdapter.ViewHolder>() {

    private var muscleGroups: List<MuscleGroupCount> = emptyList()

    fun submitList(newList: List<MuscleGroupCount>) {
        muscleGroups = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_muscle_group, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(muscleGroups[position])
    }

    override fun getItemCount() = muscleGroups.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvMuscleGroupName)
        private val countTextView: TextView = itemView.findViewById(R.id.tvMuscleGroupCount)
        private val iconView: ImageView = itemView.findViewById(R.id.ivMuscleGroupIcon)

        fun bind(item: MuscleGroupCount) {
            nameTextView.text = item.name
            countTextView.text = "${item.count} veces"
            iconView.setImageResource(R.drawable.gym_weight) // Default exercise icon
        }
    }
}