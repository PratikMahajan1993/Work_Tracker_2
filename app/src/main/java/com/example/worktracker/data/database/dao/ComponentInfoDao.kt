package com.example.worktracker.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.worktracker.data.database.entity.ComponentInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ComponentInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Changed to REPLACE for upsert logic
    suspend fun insert(componentInfo: ComponentInfo): Long

    @Update
    suspend fun update(componentInfo: ComponentInfo)

    @Delete
    suspend fun delete(componentInfo: ComponentInfo)

    @Query("SELECT * FROM component_info ORDER BY component_name ASC")
    fun getAllComponents(): Flow<List<ComponentInfo>>

    @Query("SELECT * FROM component_info WHERE id = :id")
    fun getComponentById(id: Long): Flow<ComponentInfo?>

    @Query("SELECT * FROM component_info WHERE component_name = :name")
    suspend fun getComponentByName(name: String): ComponentInfo?

    @Transaction
    suspend fun upsert(componentInfo: ComponentInfo) {
        val existingComponent = getComponentByName(componentInfo.componentName)
        if (existingComponent != null) {
            // Update existing component, ensuring we keep the Room-generated ID
            update(componentInfo.copy(id = existingComponent.id))
        } else {
            // Insert new component; ID is 0 from mapper, Room will auto-generate
            insert(componentInfo)
        }
    }

    suspend fun upsertAllComponents(components: List<ComponentInfo>) {
        components.forEach { upsert(it) }
    }
}
