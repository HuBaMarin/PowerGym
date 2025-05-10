package com.amarina.powergym.ui.adapter.statistics

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarina.powergym.R
import com.amarina.powergym.database.entities.Ejercicio
import com.amarina.powergym.database.entities.Estadistica
import java.util.Date

/**
 * Data class combining exercise with its latest completion data
 */
data class ExerciseWithStats(
    val ejercicio: Ejercicio,
    val latestStats: Estadistica
)

/**
 * Adapter for displaying comprehensive exercise statistics
 */
class ComprehensiveStatsAdapter :
    ListAdapter<ExerciseWithStats, ComprehensiveStatsAdapter.ViewHolder>(
        ExerciseWithStatsDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comprehensive_stat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val exerciseNameText: TextView = itemView.findViewById(R.id.tvExerciseName)
        private val muscleGroupText: TextView = itemView.findViewById(R.id.tvMuscleGroup)
        private val difficultyText: TextView = itemView.findViewById(R.id.tvDifficulty)
        private val daysText: TextView = itemView.findViewById(R.id.tvDays)
        private val caloriesText: TextView = itemView.findViewById(R.id.tvCalories)
        private val dateText: TextView = itemView.findViewById(R.id.tvDate)
        private val repsSeriesText: TextView = itemView.findViewById(R.id.tvRepsSeries)

        fun bind(item: ExerciseWithStats) {
            // Exercise data
            exerciseNameText.text = item.ejercicio.nombre
            muscleGroupText.text = item.ejercicio.grupoMuscular
            difficultyText.text = item.ejercicio.dificultad
            daysText.text = item.ejercicio.dias

            // Stats data
            caloriesText.text = itemView.context.getString(
                R.string.calories_count,
                item.latestStats.caloriasQuemadas
            )

            // Format date from stats
            val dateStr = DateFormat.format("dd/MM/yyyy", Date(item.latestStats.fecha)).toString()
            dateText.text = dateStr
            dateText.visibility = View.VISIBLE

            // Show reps and series if available
            if (item.latestStats.repeticiones > 0 || item.latestStats.series > 0) {
                repsSeriesText.text = itemView.context.getString(
                    R.string.reps_series_format,
                    item.latestStats.series,
                    item.latestStats.repeticiones
                )
                repsSeriesText.visibility = View.VISIBLE
            } else {
                repsSeriesText.visibility = View.GONE
            }
        }
    }
}

/**
 * DiffUtil callback for efficient RecyclerView updates
 */
class ExerciseWithStatsDiffCallback : DiffUtil.ItemCallback<ExerciseWithStats>() {
    override fun areItemsTheSame(oldItem: ExerciseWithStats, newItem: ExerciseWithStats): Boolean {
        return oldItem.ejercicio.id == newItem.ejercicio.id &&
                oldItem.latestStats.id == newItem.latestStats.id
    }

    override fun areContentsTheSame(
        oldItem: ExerciseWithStats,
        newItem: ExerciseWithStats
    ): Boolean {
        return oldItem == newItem
    }
}