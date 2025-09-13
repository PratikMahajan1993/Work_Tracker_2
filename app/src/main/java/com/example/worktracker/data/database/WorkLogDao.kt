package com.example.worktracker.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkLog(workLog: WorkLog)

    @Query("SELECT * FROM work_logs ORDER BY timestamp DESC")
    fun getAllWorkLogs(): Flow<List<WorkLog>>

    @Query("SELECT * FROM work_logs WHERE id = :id")
    suspend fun getWorkLogById(id: Long): WorkLog?

    @Query("DELETE FROM work_logs WHERE id = :id")
    suspend fun deleteWorkLogById(id: Long)

    @Query("DELETE FROM work_logs")
    suspend fun deleteAllWorkLogs()
}