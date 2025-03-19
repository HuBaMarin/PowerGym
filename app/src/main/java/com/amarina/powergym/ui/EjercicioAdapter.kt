package com.amarina.powergym.ui


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amarina.powergym.database.entities.Ejercicio
import com.amarina.powergym.databinding.ItemExerciseBinding
import com.amarina.powergym.databinding.SectionHeaderBinding

class EjercicioAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var sections = mutableListOf<Pair<String, List<Ejercicio>>>()
    private var filteredSections = mutableListOf<Pair<String, List<Ejercicio>>>()

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = SectionHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemExerciseBinding.inflate(inflater, parent, false)
                EjercicioViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val sectionData = getSectionAndPosition(position)
        when (holder) {
            is HeaderViewHolder -> holder.bind(sectionData.first)
            is EjercicioViewHolder -> holder.bind(sectionData.second)
        }
    }

    private fun getSectionAndPosition(position: Int): Pair<String, Ejercicio> {
        var currentPos = 0
        for (section in filteredSections) {
            if (currentPos == position) {
                return section.first to section.second.first()
            }
            currentPos++

            for (ejercicio in section.second) {
                if (currentPos == position) {
                    return section.first to ejercicio
                }
                currentPos++
            }
        }
        throw IndexOutOfBoundsException("Position $position is out of bounds")
    }

    override fun getItemCount(): Int =
        filteredSections.sumOf { it.second.size + 1 }

    override fun getItemViewType(position: Int): Int {
        var currentPos = 0
        for (section in filteredSections) {
            if (currentPos == position) return TYPE_HEADER
            currentPos++
            if (position < currentPos + section.second.size) return TYPE_ITEM
            currentPos += section.second.size
        }
        return TYPE_ITEM
    }

    fun submitList(ejercicios: List<Ejercicio>) {
        sections = ejercicios.groupBy { it.seccion }
            .map { it.key to it.value }
            .toMutableList()
        filteredSections = sections.toMutableList()
        notifyDataSetChanged()
    }

    fun filter(selectedDays: Set<String>, selectedDifficulties: Set<String>, query: String) {
        filteredSections.clear()

        for (section in sections) {
            val filteredEjercicios = section.second.filter { ejercicio ->
                val days = ejercicio.dias.split(",").map { it.trim() }
                (selectedDays.isEmpty() || days.any { it in selectedDays }) &&
                (selectedDifficulties.isEmpty() || ejercicio.dificultad in selectedDifficulties) &&
                (ejercicio.nombre.contains(query, true) ||
                 ejercicio.grupoMuscular.contains(query, true))
            }

            if (filteredEjercicios.isNotEmpty()) {
                filteredSections.add(section.first to filteredEjercicios)
            }
        }

        notifyDataSetChanged()
    }
}