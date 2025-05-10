package com.amarina.powergym.ui.factory.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amarina.powergym.database.dao.EjercicioDao
import com.amarina.powergym.ui.search.SearchHistoryManager
import com.amarina.powergym.ui.viewmodel.search.SearchViewModel

class SearchViewModelFactory(
    private val ejercicioDao: EjercicioDao,
    private val searchHistoryManager: SearchHistoryManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(
                ejercicioDao,
                searchHistoryManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}