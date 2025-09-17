package com.example.worktracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.worktracker.data.database.dao.*
import com.example.worktracker.data.database.entity.*

@Database(
    entities = [
        WorkActivityLog::class, 
        OperatorInfo::class, 
        ActivityCategory::class,
        TheBoysInfo::class,
        ProductionActivity::class,
        ComponentInfo::class,
        WorkActivityComponentCrossRef::class
    ], 
    version = 18,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workActivityDao(): WorkActivityDao
    abstract fun operatorInfoDao(): OperatorInfoDao
    abstract fun activityCategoryDao(): ActivityCategoryDao
    abstract fun theBoysInfoDao(): TheBoysInfoDao
    abstract fun productionActivityDao(): ProductionActivityDao
    abstract fun componentInfoDao(): ComponentInfoDao
    abstract fun workActivityComponentCrossRefDao(): WorkActivityComponentCrossRefDao

    companion object {
        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // For operator_info table
                db.execSQL("""
                    CREATE TABLE operator_info_new (
                        operatorId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        hourlySalary REAL NOT NULL,
                        role TEXT NOT NULL,
                        priority INTEGER NOT NULL,
                        notes TEXT,
                        notesForAi TEXT,
                        last_modified INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO operator_info_new (operatorId, name, hourlySalary, role, priority, notes, notesForAi, last_modified)
                    SELECT operatorId, name, hourlySalary, role, priority, notes, notesForAi, last_modified FROM operator_info
                """.trimIndent())
                db.execSQL("DROP TABLE operator_info")
                db.execSQL("ALTER TABLE operator_info_new RENAME TO operator_info")

                // For the_boys_info table
                db.execSQL("""
                    CREATE TABLE the_boys_info_new (
                        boyId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        role TEXT NOT NULL,
                        notes TEXT,
                        notesForAi TEXT,
                        last_modified INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO the_boys_info_new (boyId, name, role, notes, notesForAi, last_modified)
                    SELECT boyId, name, role, notes, notesForAi, last_modified FROM the_boys_info
                """.trimIndent())
                db.execSQL("DROP TABLE the_boys_info")
                db.execSQL("ALTER TABLE the_boys_info_new RENAME TO the_boys_info")
            }
        }
        
        // Previous migrations...
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE work_activity_logs ADD COLUMN logDate INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE work_activity_logs ADD COLUMN taskSuccessful INTEGER")
                db.execSQL("ALTER TABLE work_activity_logs ADD COLUMN assignedBy TEXT")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) { /* ... */ }
        val MIGRATION_5_6 = object : Migration(5, 6) { /* ... */ }
        val MIGRATION_6_7 = object : Migration(6, 7) { /* ... */ }
        val MIGRATION_7_8 = object : Migration(7, 8) { /* ... */ }
        val MIGRATION_8_9 = object : Migration(8, 9) { /* ... */ }
        val MIGRATION_9_10 = object : Migration(9, 10) { /* ... */ }
        val MIGRATION_10_11 = object : Migration(10, 11) { /* ... */ }
        val MIGRATION_11_12 = object : Migration(11, 12) { /* ... */ }
        val MIGRATION_12_13 = object : Migration(12, 13) { /* ... */ }
        val MIGRATION_13_14 = object : Migration(13, 14) { /* ... */ }
        val MIGRATION_14_15 = object : Migration(14, 15) { /* ... */ }
        val MIGRATION_15_16 = object : Migration(15, 16) { /* ... */ }
        val MIGRATION_16_17 = object : Migration(16, 17) { /* ... */ }
    }
}
