package com.example.worktracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.worktracker.data.database.dao.WorkActivityDao
import com.example.worktracker.data.database.dao.OperatorInfoDao
import com.example.worktracker.data.database.dao.ActivityCategoryDao
import com.example.worktracker.data.database.dao.TheBoysInfoDao
import com.example.worktracker.data.database.dao.ProductionActivityDao
import com.example.worktracker.data.database.dao.ComponentInfoDao
import com.example.worktracker.data.database.dao.WorkActivityComponentCrossRefDao
import com.example.worktracker.data.database.dao.WorkActivityTheBoyCrossRefDao
import com.example.worktracker.data.database.entity.WorkActivityLog
import com.example.worktracker.data.database.entity.OperatorInfo
import com.example.worktracker.data.database.entity.ActivityCategory
import com.example.worktracker.data.database.entity.TheBoysInfo
import com.example.worktracker.data.database.entity.ProductionActivity
import com.example.worktracker.data.database.entity.ComponentInfo
import com.example.worktracker.data.database.entity.WorkActivityComponentCrossRef
import com.example.worktracker.data.database.entity.WorkActivityTheBoyCrossRef

@Database(
    entities = [
        WorkActivityLog::class, 
        OperatorInfo::class, 
        ActivityCategory::class,
        TheBoysInfo::class,
        ProductionActivity::class,
        ComponentInfo::class,
        WorkActivityComponentCrossRef::class,
        WorkActivityTheBoyCrossRef::class
    ], 
    version = 14, // Incremented version to 14
    exportSchema = false // Consider setting to true and committing schemas to version control
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workActivityDao(): WorkActivityDao
    abstract fun operatorInfoDao(): OperatorInfoDao
    abstract fun activityCategoryDao(): ActivityCategoryDao
    abstract fun theBoysInfoDao(): TheBoysInfoDao
    abstract fun productionActivityDao(): ProductionActivityDao
    abstract fun componentInfoDao(): ComponentInfoDao
    abstract fun workActivityComponentCrossRefDao(): WorkActivityComponentCrossRefDao
    abstract fun workActivityTheBoyCrossRefDao(): WorkActivityTheBoyCrossRefDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE work_activity_logs ADD COLUMN logDate INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE work_activity_logs ADD COLUMN taskSuccessful INTEGER")
                db.execSQL("ALTER TABLE work_activity_logs ADD COLUMN assignedBy TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `activity_categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `the_boys_info` (`boyId` INTEGER NOT NULL, `name` TEXT NOT NULL, `role` TEXT NOT NULL, `notes` TEXT, `notesForAi` TEXT, PRIMARY KEY(`boyId`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `production_activity` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `boyId` INTEGER NOT NULL, `componentName` TEXT NOT NULL, `machineNumber` INTEGER NOT NULL, `productionQuantity` INTEGER NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `duration` INTEGER NOT NULL)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE work_activity_logs ADD COLUMN duration INTEGER")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `component_info` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `component_name` TEXT NOT NULL, `customer` TEXT NOT NULL, `priority_level` INTEGER NOT NULL, `cycle_time_minutes` INTEGER NOT NULL, `notes_for_ai` TEXT)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_component_info_component_name` ON `component_info` (`component_name`)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `work_activity_component_cross_ref` (
                        `workActivityId` INTEGER NOT NULL, 
                        `componentId` INTEGER NOT NULL, 
                        PRIMARY KEY(`workActivityId`, `componentId`), 
                        FOREIGN KEY(`workActivityId`) REFERENCES `work_activity_logs`(`id`) ON DELETE CASCADE, 
                        FOREIGN KEY(`componentId`) REFERENCES `component_info`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_work_activity_component_cross_ref_workActivityId` ON `work_activity_component_cross_ref` (`workActivityId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_work_activity_component_cross_ref_componentId` ON `work_activity_component_cross_ref` (`componentId`)")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `work_activity_the_boy_cross_ref` (
                        `workActivityId` INTEGER NOT NULL, 
                        `theBoyId` INTEGER NOT NULL, 
                        PRIMARY KEY(`workActivityId`, `theBoyId`), 
                        FOREIGN KEY(`workActivityId`) REFERENCES `work_activity_logs`(`id`) ON DELETE CASCADE, 
                        FOREIGN KEY(`theBoyId`) REFERENCES `the_boys_info`(`boyId`) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_work_activity_the_boy_cross_ref_workActivityId` ON `work_activity_the_boy_cross_ref` (`workActivityId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_work_activity_the_boy_cross_ref_theBoyId` ON `work_activity_the_boy_cross_ref` (`theBoyId`)")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE work_activity_logs_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        categoryName TEXT NOT NULL,
                        categoryId INTEGER DEFAULT NULL,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER DEFAULT NULL,
                        description TEXT NOT NULL,
                        operatorId INTEGER DEFAULT NULL,
                        expenses REAL DEFAULT NULL,
                        logDate INTEGER NOT NULL,
                        taskSuccessful INTEGER DEFAULT NULL,
                        assignedBy TEXT DEFAULT NULL,
                        duration INTEGER DEFAULT NULL,
                        FOREIGN KEY(categoryId) REFERENCES activity_categories(id) ON DELETE SET NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO work_activity_logs_new (id, categoryName, categoryId, startTime, endTime, description, operatorId, expenses, logDate, taskSuccessful, assignedBy, duration)
                    SELECT id, categoryName, 
                           CASE WHEN (SELECT sql FROM sqlite_master WHERE name = 'work_activity_logs' AND type = 'table' AND sql LIKE '%categoryId%') THEN categoryId ELSE NULL END, 
                           startTime, endTime, description, operatorId, expenses, logDate, taskSuccessful, assignedBy, duration
                    FROM work_activity_logs
                """.trimIndent())
                db.execSQL("DROP TABLE work_activity_logs")
                db.execSQL("ALTER TABLE work_activity_logs_new RENAME TO work_activity_logs")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_work_activity_logs_categoryId` ON `work_activity_logs` (`categoryId`)")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create new table with cycle_time_minutes as REAL
                db.execSQL("""
                    CREATE TABLE component_info_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        component_name TEXT NOT NULL,
                        customer TEXT NOT NULL,
                        priority_level INTEGER NOT NULL,
                        cycle_time_minutes REAL NOT NULL, -- Changed to REAL
                        notes_for_ai TEXT
                    )
                """.trimIndent())

                // 2. Copy data from old table to new table, casting cycle_time_minutes
                db.execSQL("""
                    INSERT INTO component_info_new (id, component_name, customer, priority_level, cycle_time_minutes, notes_for_ai)
                    SELECT id, component_name, customer, priority_level, CAST(cycle_time_minutes AS REAL), notes_for_ai
                    FROM component_info
                """.trimIndent())

                // 3. Drop the old table
                db.execSQL("DROP TABLE component_info")

                // 4. Rename the new table to the original name
                db.execSQL("ALTER TABLE component_info_new RENAME TO component_info")

                // 5. Re-create index
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_component_info_component_name` ON `component_info` (`component_name`)")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE production_activity ADD COLUMN downtimeMinutes INTEGER NULL")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE production_activity ADD COLUMN rejectionQuantity INTEGER NULL")
            }
        }
    }
}
