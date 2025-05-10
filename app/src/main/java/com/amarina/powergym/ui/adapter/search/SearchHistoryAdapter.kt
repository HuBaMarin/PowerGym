package com.amarina.powergym.ui.adapter.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarina.powergym.R
import com.amarina.powergym.ui.item.SearchHistoryItem

class SearchHistoryAdapter(
    private val onQuerySelected: (String) -> Unit,
    private val onDeleteClicked: ((String) -> Unit)? = null
) : ListAdapter<SearchHistoryItem, SearchHistoryAdapter.SearchHistoryViewHolder>(
    object : DiffUtil.ItemCallback<SearchHistoryItem>() {
        override fun areItemsTheSame(oldItem: SearchHistoryItem, newItem: SearchHistoryItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SearchHistoryItem, newItem: SearchHistoryItem) =
            oldItem.query == newItem.query && oldItem.timestamp == newItem.timestamp
    }
) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_history, parent, false)
        return SearchHistoryViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: SearchHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class SearchHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQueryText: TextView = itemView.findViewById(R.id.tvQueryText)
        private val btnDelete: ImageButton? = itemView.findViewById(R.id.btnDeleteHistory)

        fun bind(historyItem: SearchHistoryItem) {
            tvQueryText.text = historyItem.query
            itemView.setOnClickListener {
                onQuerySelected(historyItem.query)
            }

            // Configure delete button if available
            btnDelete?.apply {
                visibility = if (onDeleteClicked != null) View.VISIBLE else View.GONE
                setOnClickListener {
                    onDeleteClicked?.invoke(historyItem.query)
                }
            }
        }
    }
}