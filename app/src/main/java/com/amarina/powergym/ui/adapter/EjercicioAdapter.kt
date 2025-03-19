package com.amarina.powergym.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarina.powergym.R
import com.amarina.powergym.database.entities.Ejercicio
import com.squareup.picasso.Picasso

class EjercicioAdapter(
    private val onEjercicioClick: (Ejercicio) -> Unit
) : ListAdapter<EjercicioAdapterItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private var sections = emptyList<Pair<String, List<Ejercicio>>>()

    fun updateSections(newSections: List<Pair<String, List<Ejercicio>>>) {
        sections = newSections

        val items = mutableListOf<EjercicioAdapterItem>()
        for ((section, ejercicios) in sections) {
            items.add(EjercicioAdapterItem.SectionHeader(section))
            items.addAll(ejercicios.map { EjercicioAdapterItem.EjercicioItem(it) })
        }

        submitList(items)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is EjercicioAdapterItem.SectionHeader -> TYPE_HEADER
            is EjercicioAdapterItem.EjercicioItem -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                HeaderViewHolder(
                    ItemSectionHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                EjercicioViewHolder(
                    ItemEjercicioBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    onEjercicioClick
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is EjercicioAdapterItem.SectionHeader -> (holder as HeaderViewHolder).bind(item.title)
            is EjercicioAdapterItem.EjercicioItem -> (holder as EjercicioViewHolder).bind(item.ejercicio)
        }
    }

    class HeaderViewHolder(
        private val binding: ItemSectionHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.tvSectionTitle.text = title
        }
    }

    class EjercicioViewHolder(
        private val binding: ItemEjercicioBinding,
        private val onEjercicioClick: (Ejercicio) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ejercicio: Ejercicio) {
            binding.apply {
                tvEjercicioNombre.text = ejercicio.nombre
                tvGrupoMuscular.text = ejercicio.grupoMuscular
                chipDificultad.text = ejercicio.dificultad

                Picasso.get()
                    .load(ejercicio.urlEjercicio)
                    .placeholder(R.drawable.placeholder_ejercicio)
                    .error(R.drawable.error_imagen)
                    .fit()
                    .centerCrop()
                    .into(ivEjercicio)

                root.setOnClickListener {
                    onEjercicioClick(ejercicio)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<EjercicioAdapterItem>() {
        override fun areItemsTheSame(oldItem: EjercicioAdapterItem, newItem: EjercicioAdapterItem): Boolean {
            return when {
                oldItem is EjercicioAdapterItem.SectionHeader && newItem is EjercicioAdapterItem.SectionHeader ->
                    oldItem.title == newItem.title
                oldItem is EjercicioAdapterItem.EjercicioItem && newItem is EjercicioAdapterItem.EjercicioItem ->
                    oldItem.ejercicio.id == newItem.ejercicio.id
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
}
