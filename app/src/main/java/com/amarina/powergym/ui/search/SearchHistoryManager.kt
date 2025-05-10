package com.amarina.powergym.ui.search

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val maxHistoryItems = 10

    fun getSearchHistory(): List<String> {
        val json = prefs.getString("history", null) ?: return emptyList()
        val type = object : TypeToken<ArrayList<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun addSearchQuery(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return

        val history = getSearchHistory().toMutableList()

        // Quitar duplicados
        history.remove(trimmedQuery)

        // Añadir al principio
        history.add(0, trimmedQuery)

        // Limitar tamaño
        if (history.size > maxHistoryItems) {
            history.subList(maxHistoryItems, history.size).clear()
        }

        // Guardar
        saveHistory(history)
    }

    fun clearSearchHistory() {
        prefs.edit { remove("history") }
    }

    private fun saveHistory(history: List<String>) {
        val json = gson.toJson(history)
        prefs.edit { putString("history", json) }
    }
}
