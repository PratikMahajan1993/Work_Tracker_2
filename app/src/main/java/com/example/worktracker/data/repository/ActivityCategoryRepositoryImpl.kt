package com.example.worktracker.data.repository

import com.example.worktracker.data.database.dao.ActivityCategoryDao
import com.example.worktracker.data.database.entity.ActivityCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityCategoryRepositoryImpl @Inject constructor(
    private val activityCategoryDao: ActivityCategoryDao
) : ActivityCategoryRepository {

    override fun getAllCategories(): Flow<List<ActivityCategory>> {
        return activityCategoryDao.getAllCategories()
    }

    override suspend fun insertCategory(category: ActivityCategory) {
        activityCategoryDao.insert(category)
    }

    override suspend fun updateCategory(category: ActivityCategory) {
        activityCategoryDao.update(category)
    }

    override suspend fun deleteCategory(category: ActivityCategory) {
        activityCategoryDao.delete(category)
    }

    override suspend fun getCategoryByName(name: String): ActivityCategory? {
        return activityCategoryDao.getCategoryByName(name)
    }
}