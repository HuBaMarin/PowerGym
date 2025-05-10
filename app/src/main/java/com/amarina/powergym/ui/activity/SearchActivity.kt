package com.amarina.powergym.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amarina.powergym.PowerGymApplication
import com.amarina.powergym.R
import com.amarina.powergym.databinding.ActivitySearchBinding
import com.amarina.powergym.ui.adapter.exercise.EjercicioAdapter
import com.amarina.powergym.ui.adapter.exercise.EjercicioAdapterItem
import com.amarina.powergym.ui.viewmodel.search.SearchViewModel
import com.amarina.powergym.utils.LanguageHelper
import com.google.android.material.chip.Chip
import com.google.android.material.search.SearchView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: SearchViewModel
    private lateinit var adapter: EjercicioAdapter
    private lateinit var searchViewAdapter: EjercicioAdapter

    override fun attachBaseContext(newBase: Context) = super.attachBaseContext(LanguageHelper.establecerIdioma(newBase))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            (application as PowerGymApplication).searchViewModelFactory
        )[SearchViewModel::class.java]

        setupAdapters()
        setupViews()
        setupSearchBar()
        setupSearchView()
        setupFilters()
        setupObservers()
        setupBottomNavigation()

        // Load initial results
        viewModel.loadInitialResults()
    }

    private fun setupAdapters() {
        // Main adapter for search results
        adapter = EjercicioAdapter { ejercicioId ->
            navigateToExerciseDetail(ejercicioId)
        }

        // Adapter for search view results
        searchViewAdapter = EjercicioAdapter { ejercicioId ->
            binding.searchView.hide()
            navigateToExerciseDetail(ejercicioId)
        }
    }

    private fun navigateToExerciseDetail(ejercicioId: Int) {
        val intent = Intent(this, EjercicioDetailActivity::class.java)
        intent.putExtra("ejercicio_id", ejercicioId)
        startActivity(intent)
    }

    private fun setupViews() {
        // Setup main RecyclerView
        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = this@SearchActivity.adapter
            addItemDecoration(
                DividerItemDecoration(
                    this@SearchActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        // Setup search view RecyclerView
        binding.rvSearchViewResults.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = searchViewAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this@SearchActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        // Setup try again button
        binding.btnTryAgain.setOnClickListener {
            binding.searchBar.performClick()
            binding.searchView.setText("")
            viewModel.setSearchQuery("", false)
            viewModel.setDifficultyFilter(null)
        }
    }

    private fun setupSearchBar() {
        binding.searchBar.apply {
            hint = getString(R.string.search_hint)
            setOnClickListener {
                binding.searchView.show()
            }
        }
    }

    private fun setupSearchView() {
        binding.searchView.apply {
            setupWithSearchBar(binding.searchBar)

            addTransitionListener { _, _, newState ->
                if (newState == SearchView.TransitionState.SHOWING) {
                    // When search view opens, update results in search view
                    updateSearchViewResults(viewModel.searchState.value)

                    // Show the filters when SearchView is open
                    binding.scrollFilters.visibility = View.VISIBLE
                }
            }

            // Handle search action
            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    val query = text.toString().trim()
                    viewModel.setSearchQuery(query, true)
                    hide()
                    binding.searchBar.setText(query)
                    return@setOnEditorActionListener true
                }
                false
            }

            // Handle real-time search
            editText.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val query = s?.toString()?.trim() ?: ""
                    viewModel.setSearchQuery(query, false)
                }

                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
    }

    private fun setupFilters() {
        binding.chipGroupQuickFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when {
                checkedIds.isEmpty() -> null
                else -> {
                    val selectedChipId = checkedIds.first()
                    val selectedChip =
                        binding.chipGroupQuickFilters.findViewById<Chip>(selectedChipId)
                    Log.d("SearchActivity", "Selected chip tag: ${selectedChip.tag}")

                    // If "all" is selected, explicitly set filter to null and log it
                    if (selectedChip.tag == "all") {
                        Log.d("SearchActivity", "ALL filter selected, setting filter to null")
                        null
                    } else {
                        selectedChip.tag as? String
                    }
                }
            }

            Log.d("SearchActivity", "Filter selected: $filter")
            // Force refresh with selected filter
            viewModel.setDifficultyFilter(filter)
        }

        // Select "All" chip by default
        binding.chipAll?.isChecked = true

        // Dynamically load difficulty values
        lifecycleScope.launch {
            viewModel.difficultyValues.collectLatest { difficulties ->
                binding.chipGroupQuickFilters.apply {
                    removeAllViews()

                    // Add "All" chip first
                    addView(createFilterChip(null, getString(R.string.all), true))

                    // Add difficulty chips
                    difficulties.forEach { difficulty ->
                        val translatedText = when (difficulty.lowercase()) {
                            "beginner" -> getString(R.string.basic)
                            "intermediate" -> getString(R.string.medium)
                            "advanced" -> getString(R.string.advanced)
                            else -> difficulty
                        }
                        addView(createFilterChip(difficulty, translatedText, false))
                    }

                    // Check the first chip by default
                    if (checkedChipId == View.NO_ID) {
                        (getChildAt(0) as? Chip)?.isChecked = true
                    }
                }
            }
        }
    }

    private fun createFilterChip(value: String?, displayText: String, isChecked: Boolean) =
        Chip(this).apply {
            id = View.generateViewId()
            text = displayText
            isCheckable = true
            this.isChecked = isChecked
            // Special handling for "all" tag
            tag = if (value == null) "all" else value
            setChipBackgroundColorResource(R.color.chip_background_state)
            setTextColor(resources.getColorStateList(R.color.chip_text_state, theme))
            setChipStrokeColorResource(R.color.chip_background_state)
            chipStrokeWidth = resources.getDimension(R.dimen.chip_stroke_width)

            // Add click listener for specific handling of "all" filter
            setOnClickListener {
                if (tag == "all") {
                    Log.d("SearchActivity", "All chip clicked, forcing reload of all exercises")
                    viewModel.setDifficultyFilter(null)
                    check(true)
                }
            }
        }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.searchState.collectLatest { state ->
                updateMainSearchResults(state)
                updateSearchViewResults(state)
            }
        }
    }

    private fun updateMainSearchResults(state: SearchViewModel.SearchState) {
        with(binding) {
            // Reset visibility
            progressBar.isVisible = false
            rvSearchResults.isVisible = false
            emptyStateView.isVisible = false
            tvResultsInfo.isVisible = false

            when (state) {
                is SearchViewModel.SearchState.Loading -> {
                    progressBar.isVisible = true
                }

                is SearchViewModel.SearchState.Success -> {
                    if (state.ejercicios.isEmpty()) {
                        // Show empty state
                        emptyStateView.isVisible = true
                        ivEmptyState.setImageResource(R.drawable.ic_search_empty)
                        tvEmpty.text = getString(R.string.empty_search)

                        // Show appropriate subtitle based on query
                        val currentQuery = viewModel.queryFlow.value
                        tvEmptySubtitle.text = if (currentQuery.isNotBlank()) {
                            getString(R.string.empty_search_query_message, currentQuery)
                        } else {
                            getString(R.string.empty_search_message)
                        }
                    } else {
                        // Show results
                        rvSearchResults.isVisible = true
                        adapter.submitList(state.ejercicios.map(EjercicioAdapterItem::EjercicioItemList))

                        // Update search bar text if needed
                        val currentQuery = viewModel.queryFlow.value
                        if (currentQuery.isNotBlank()) {
                            searchBar.setText(currentQuery)
                        }

                        // Show results count if there's a query
                        if (currentQuery.isNotBlank()) {
                            tvResultsInfo.text = resources.getQuantityString(
                                R.plurals.search_results_count,
                                state.ejercicios.size,
                                state.ejercicios.size,
                                currentQuery
                            )
                            tvResultsInfo.isVisible = true
                        }
                    }
                }

                is SearchViewModel.SearchState.Error -> {
                    // Show error state
                    emptyStateView.isVisible = true
                    ivEmptyState.setImageResource(R.drawable.baseline_error_24)
                    tvEmpty.text = getString(R.string.general_error)
                    tvEmptySubtitle.text = state.message
                }

                SearchViewModel.SearchState.Idle -> {
                    // Do nothing, waiting for initial load
                }
            }
        }
    }

    private fun updateSearchViewResults(state: SearchViewModel.SearchState) {
        if (!binding.searchView.isShowing) return

        when (state) {
            is SearchViewModel.SearchState.Success -> {
                searchViewAdapter.submitList(state.ejercicios.map(EjercicioAdapterItem::EjercicioItemList))
            }
            else -> {
                // Handle other states if needed
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.apply {
            selectedItemId = R.id.navigation_search
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_search -> true // Already on search screen
                    R.id.navigation_home -> {
                        startActivity(Intent(this@SearchActivity, PrincipalActivity::class.java))
                        finish()
                        true
                    }

                    R.id.navigation_statistics -> {
                        startActivity(Intent(this@SearchActivity, StatisticsActivity::class.java))
                        finish()
                        true
                    }

                    else -> false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload results if needed
        if (viewModel.searchState.value is SearchViewModel.SearchState.Idle) {
            viewModel.loadInitialResults()
        }
    }
}