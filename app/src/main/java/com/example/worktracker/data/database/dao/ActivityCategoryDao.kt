package com.example.worktracker.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.worktracker.data.database.entity.ActivityCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityCategoryDao {

    @Query("SELECT * FROM activity_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<ActivityCategory>>

    @Query("SELECT * FROM activity_categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): ActivityCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Remains useful for direct inserts if needed
    suspend fun insert(category: ActivityCategory)

    @Update
    suspend fun update(category: ActivityCategory)

    @Delete
    suspend fun delete(category: ActivityCategory)

    @Transaction
    suspend fun upsert(category: ActivityCategory) {
        val existingCategory = getCategoryByName(category.name)
        if (existingCategory != null) {
            // Update existing category, ensuring we keep the Room-generated ID
            update(category.copy(id = existingCategory.id))
        } else {
            // Insert new category; ID is 0 from mapper, Room will auto-generate
            insert(category)
        }
    }

    suspend fun upsertAllCategories(categories: List<ActivityCategory>) {
        categories.forEach { upsert(it) }
    }
}
