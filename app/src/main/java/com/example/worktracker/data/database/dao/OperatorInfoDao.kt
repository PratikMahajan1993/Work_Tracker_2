package com.example.worktracker.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.worktracker.data.database.entity.OperatorInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface OperatorInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operatorInfo: OperatorInfo)

    @Update
    suspend fun update(operatorInfo: OperatorInfo)

    @Delete
    suspend fun delete(operatorInfo: OperatorInfo)

    @Query("SELECT * FROM operator_info ORDER BY name ASC")
    fun getAllOperators(): Flow<List<OperatorInfo>>

    @Query("SELECT * FROM operator_info WHERE operatorId = :id")
    suspend fun getOperatorById(id: Int): OperatorInfo?
}
