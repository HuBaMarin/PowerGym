package com.amarina.powergym.ui

import androidx.recyclerview.widget.RecyclerView
import com.amarina.powergym.databinding.SectionHeaderBinding

class HeaderViewHolder(private val binding: SectionHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(header: String) {
        binding.headerTitle.text = header
    }
}