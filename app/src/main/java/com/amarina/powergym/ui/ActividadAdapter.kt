package com.amarin.powergym.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amarin.powergym.database.entities.Actividad

class ActividadAdapter(
    private val actividades: List<Actividad>,
    private val onItemClick: (Actividad) -> Unit
): RecyclerView.Adapter<ActividadViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActividadViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_actividad, parent, false)
        return ActividadViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActividadViewHolder, position: Int) {
        val actividad = actividades[position]
        holder.bind(actividad)
        holder.itemView.setOnClickListener {
            onItemClick(actividad)
        }
    }

    override fun getItemCount(): Int = actividades.size
}