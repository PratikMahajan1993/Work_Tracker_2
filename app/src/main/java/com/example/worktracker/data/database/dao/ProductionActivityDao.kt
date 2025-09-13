package com.example.worktracker.data.database.dao

import androidx.room.*
import com.example.worktracker.data.database.entity.ProductionActivity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductionActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(productionActivity: ProductionActivity)

    @Update
    suspend fun update(productionActivity: ProductionActivity)

    @Delete
    suspend fun delete(productionActivity: ProductionActivity)

    @Query("SELECT * FROM production_activity ORDER BY startTime DESC")
    fun getAllProductionActivities(): Flow<List<ProductionActivity>>

    @Query("SELECT * FROM production_activity WHERE id = :id")
    suspend fun getProductionActivityById(id: Long): ProductionActivity?

    @Query("SELECT * FROM production_activity WHERE boyId = :boyId ORDER BY startTime DESC")
    fun getProductionActivitiesByBoyId(boyId: Int): Flow<List<ProductionActivity>>
}
