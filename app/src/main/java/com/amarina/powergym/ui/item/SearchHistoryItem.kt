package com.amarina.powergym.ui.item

/**
 * Data class to represent search history items with deletion capability
 */
data class SearchHistoryItem(
    val id: Long = 0,   // Unique identifier
    val query: String,  // The search query text
    val timestamp: Long // When the search was performed
)