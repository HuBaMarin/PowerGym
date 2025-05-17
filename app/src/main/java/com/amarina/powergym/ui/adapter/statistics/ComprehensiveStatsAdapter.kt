package com.amarina.powergym.ui.adapter.statistics

import android.content.Context
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
            daysText.text = translateDays(itemView.context, item.ejercicio.dias)

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

        private fun translateDays(context: Context, days: String): String {
            return days.split(",").map { day ->
                when (day.trim()) {
                    // Spanish days
                    "Lunes" -> context.getString(R.string.day_monday)
                    "Martes" -> context.getString(R.string.day_tuesday)
                    "Miércoles" -> context.getString(R.string.day_wednesday)
                    "Jueves" -> context.getString(R.string.day_thursday)
                    "Viernes" -> context.getString(R.string.day_friday)
                    "Sábado" -> context.getString(R.string.day_saturday)
                    "Domingo" -> context.getString(R.string.day_sunday)
                    // English days
                    "Monday" -> context.getString(R.string.day_monday)
                    "Tuesday" -> context.getString(R.string.day_tuesday)
                    "Wednesday" -> context.getString(R.string.day_wednesday)
                    "Thursday" -> context.getString(R.string.day_thursday)
                    "Friday" -> context.getString(R.string.day_friday)
                    "Saturday" -> context.getString(R.string.day_saturday)
                    "Sunday" -> context.getString(R.string.day_sunday)
                    // Abbreviated days
                    "Mon" -> context.getString(R.string.day_mon)
                    "Tue" -> context.getString(R.string.day_tue)
                    "Wed" -> context.getString(R.string.day_wed)
                    "Thu" -> context.getString(R.string.day_thu)
                    "Fri" -> context.getString(R.string.day_fri)
                    "Sat" -> context.getString(R.string.day_sat)
                    "Sun" -> context.getString(R.string.day_sun)
                    else -> day.trim()
                }
            }.joinToString(", ")
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