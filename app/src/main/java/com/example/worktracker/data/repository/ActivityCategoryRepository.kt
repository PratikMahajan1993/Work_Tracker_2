package com.example.worktracker.data.repository

import com.example.worktracker.data.database.entity.ActivityCategory
import kotlinx.coroutines.flow.Flow

interface ActivityCategoryRepository {
    fun getAllCategories(): Flow<List<ActivityCategory>>
    suspend fun insertCategory(category: ActivityCategory)
    suspend fun updateCategory(category: ActivityCategory)
    suspend fun deleteCategory(category: ActivityCategory)
    suspend fun getCategoryByName(name: String): ActivityCategory?
}