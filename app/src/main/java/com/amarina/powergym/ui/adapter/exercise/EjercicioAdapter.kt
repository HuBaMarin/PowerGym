package com.amarina.powergym.ui.adapter.exercise

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarina.powergym.R
import com.amarina.powergym.database.entities.Ejercicio
import com.amarina.powergym.databinding.ItemExerciseBinding
import com.amarina.powergym.databinding.ItemExerciseListBinding
import com.amarina.powergym.databinding.SectionHeaderBinding
import com.amarina.powergym.ui.viewholder.exercise.EjercicioGridViewHolder
import com.amarina.powergym.ui.viewholder.exercise.EjercicioListViewHolder

class EjercicioAdapter(
    private val onEjercicioClick: (Int) -> Unit
) : ListAdapter<EjercicioAdapterItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
        private const val TYPE_ITEM_LIST = 2
        private const val TYPE_ITEM_GRID = 3
    }

    private var sections = emptyMap<String, List<Ejercicio>>()

    fun updateSections(ejercicios: List<Ejercicio>) {
        val groupedEjercicios = ejercicios.groupBy { it.seccion }
        sections = groupedEjercicios

        val items = mutableListOf<EjercicioAdapterItem>()
        for ((sectionTitle, ejerciciosList) in sections) {
            if (ejerciciosList.isNotEmpty()) {
                items.add(EjercicioAdapterItem.SectionHeader(sectionTitle))
                items.addAll(ejerciciosList.map { EjercicioAdapterItem.EjercicioItem(it) })
            }
        }



        submitList(items)
    }




    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is EjercicioAdapterItem.SectionHeader -> TYPE_HEADER
            is EjercicioAdapterItem.EjercicioItem -> TYPE_ITEM
            is EjercicioAdapterItem.EjercicioItemList -> TYPE_ITEM_LIST
            is EjercicioAdapterItem.EjercicioGridItem -> TYPE_ITEM_GRID
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = SectionHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            TYPE_ITEM -> {
                val binding = ItemExerciseBinding.inflate(inflater, parent, false)
                EjercicioGridViewHolder(binding) { ejercicio ->
                    onEjercicioSelected(ejercicio)
                }
            }
            TYPE_ITEM_LIST -> {
                val binding = ItemExerciseListBinding.inflate(inflater, parent, false)
                EjercicioListViewHolder(binding) { ejercicio ->
                    onEjercicioSelected(ejercicio)
                }
            }
            else -> {
                val binding = ItemExerciseBinding.inflate(inflater, parent, false)
                EjercicioGridViewHolder(binding) { ejercicio ->
                    onEjercicioSelected(ejercicio)
                }
            }
        }
    }

    private fun onEjercicioSelected(ejercicio: Ejercicio) {
        onEjercicioClick(ejercicio.id)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is EjercicioAdapterItem.SectionHeader -> (holder as HeaderViewHolder).bind(item.title)
            is EjercicioAdapterItem.EjercicioItem -> (holder as EjercicioGridViewHolder).bind(item.ejercicio)
            is EjercicioAdapterItem.EjercicioItemList -> (holder as EjercicioListViewHolder).bind(
                item.ejercicio
            )
            is EjercicioAdapterItem.EjercicioGridItem -> (holder as EjercicioGridViewHolder).bind(
                item.ejercicio
            )
        }
    }

    class HeaderViewHolder(
        private val binding: SectionHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            val context = binding.root.context
            val resourceId = getSectionResourceId(title, context)
            if (resourceId != 0) {
                binding.tvHeaderTitle.text = context.getString(resourceId)
            } else {
                binding.tvHeaderTitle.text = title
            }
        }

        private fun getSectionResourceId(sectionTitle: String, context: Context): Int {
            // Use the centralized translation helper to get the resource ID
            val translatedSection =
                com.amarina.powergym.utils.TranslationHelper.translateSection(sectionTitle, context)

            // If translation was successful, find the resource ID for the translated string
            return when (translatedSection) {
                context.getString(R.string.section_demo) -> R.string.section_demo
                context.getString(R.string.section_elderly) -> R.string.section_elderly
                context.getString(R.string.section_reduced_mobility) -> R.string.section_reduced_mobility
                context.getString(R.string.section_rehabilitation) -> R.string.section_rehabilitation
                context.getString(R.string.section_upper_body) -> R.string.section_upper_body
                context.getString(R.string.section_lower_body) -> R.string.section_lower_body
                context.getString(R.string.section_cardio) -> R.string.section_cardio
                else -> 0
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<EjercicioAdapterItem>() {
        override fun areItemsTheSame(oldItem: EjercicioAdapterItem, newItem: EjercicioAdapterItem): Boolean {
            return when {
                oldItem is EjercicioAdapterItem.SectionHeader && newItem is EjercicioAdapterItem.SectionHeader -> oldItem.title == newItem.title
                oldItem is EjercicioAdapterItem.EjercicioItem && newItem is EjercicioAdapterItem.EjercicioItem -> oldItem.ejercicio.id == newItem.ejercicio.id
                oldItem is EjercicioAdapterItem.EjercicioItemList && newItem is EjercicioAdapterItem.EjercicioItemList -> oldItem.ejercicio.id == newItem.ejercicio.id
                oldItem is EjercicioAdapterItem.EjercicioGridItem && newItem is EjercicioAdapterItem.EjercicioGridItem -> oldItem.ejercicio.id == newItem.ejercicio.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: EjercicioAdapterItem, newItem: EjercicioAdapterItem): Boolean {
            return oldItem == newItem
        }
    }
}

sealed class EjercicioAdapterItem {
    data class SectionHeader(val title: String) : EjercicioAdapterItem()
    data class EjercicioItem(val ejercicio: Ejercicio) : EjercicioAdapterItem()
    data class EjercicioItemList(val ejercicio: Ejercicio) : EjercicioAdapterItem()
    data class EjercicioGridItem(val ejercicio: Ejercicio) : EjercicioAdapterItem()
}
