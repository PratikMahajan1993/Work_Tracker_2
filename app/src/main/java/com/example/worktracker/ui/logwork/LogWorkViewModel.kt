package com.example.worktracker.ui.logwork

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.data.database.entity.ActivityCategory
import com.example.worktracker.data.repository.ActivityCategoryRepository
import com.example.worktracker.data.repository.WorkActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// Represents the display information for a single category on the LogWorkActivityScreen
data class CategoryDisplayInfo(
    val name: String,
    val isOngoing: Boolean = false
)

// Represents the UI state for the LogWorkActivityScreen
data class LogWorkUiState(
    val categories: List<CategoryDisplayInfo> = emptyList(),
    val isLoading: Boolean = true // Initially true until categories and ongoing status are loaded
)

@HiltViewModel
class LogWorkViewModel @Inject constructor(
    private val workActivityRepository: WorkActivityRepository,
    private val activityCategoryRepository: ActivityCategoryRepository // NEW: Inject ActivityCategoryRepository
) : ViewModel() {

    val uiState: StateFlow<LogWorkUiState> =
        workActivityRepository.getOngoingActivities()
            .combine(activityCategoryRepository.getAllCategories()) { ongoingActivities, allCategories -> // Use repository
                val ongoingCategoryNames = ongoingActivities.map { it.categoryName }.toSet()
                val categoriesInfo = allCategories.map { category -> // Map ActivityCategory entities
                    CategoryDisplayInfo(
                        name = category.name,
                        isOngoing = category.name in ongoingCategoryNames
                    )
                }
                LogWorkUiState(categories = categoriesInfo, isLoading = false)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = LogWorkUiState(isLoading = true) // Initial value without hardcoded categories
            )
}