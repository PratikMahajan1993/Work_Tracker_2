package com.example.worktracker.data.database.dao

import androidx.room.*
import com.example.worktracker.data.database.entity.TheBoysInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface TheBoysInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(theBoyInfo: TheBoysInfo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllTheBoys(theBoys: List<TheBoysInfo>)

    @Update
    suspend fun update(theBoyInfo: TheBoysInfo)

    @Delete
    suspend fun delete(theBoyInfo: TheBoysInfo)

    @Query("SELECT * FROM the_boys_info ORDER BY boyId ASC")
    fun getAllTheBoys(): Flow<List<TheBoysInfo>>

    @Query("SELECT * FROM the_boys_info WHERE boyId = :id")
    suspend fun getTheBoyById(id: Int): TheBoysInfo?

    @Query("DELETE FROM sqlite_sequence WHERE name='the_boys_info'")
    suspend fun resetAutoIncrement()
}
