package com.fasaldrishti.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasaldrishti.app.domain.model.ScanRecord
import com.fasaldrishti.app.domain.repository.ScanRepository
import kotlinx.coroutines.flow.*

class HistoryViewModel(
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    val filteredScans: StateFlow<List<ScanRecord>> = combine(
        scanRepository.getAllScans(),
        _searchQuery,
        _selectedFilter
    ) { scans, query, filter ->
        scans.filter { scan ->
            val matchesQuery = query.isBlank() ||
                    scan.diseaseName.contains(query, ignoreCase = true) ||
                    scan.cropName.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "All" -> true
                "Healthy" -> scan.severity.equals("none", ignoreCase = true) || scan.diseaseName.contains("healthy", ignoreCase = true)
                "Diseased" -> !scan.severity.equals("none", ignoreCase = true) && !scan.diseaseName.contains("healthy", ignoreCase = true)
                else -> scan.cropName.equals(filter, ignoreCase = true)
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }
}
