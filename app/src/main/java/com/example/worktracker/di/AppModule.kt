package com.example.worktracker.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.worktracker.GeminiProService
import com.example.worktracker.data.database.AppDatabase
import com.example.worktracker.data.database.dao.OperatorInfoDao
import com.example.worktracker.data.database.dao.WorkActivityDao
import com.example.worktracker.data.database.dao.ActivityCategoryDao
import com.example.worktracker.data.database.dao.TheBoysInfoDao
import com.example.worktracker.data.database.dao.ProductionActivityDao
import com.example.worktracker.data.database.dao.ComponentInfoDao
import com.example.worktracker.data.database.dao.WorkActivityComponentCrossRefDao
import com.example.worktracker.data.database.dao.WorkActivityTheBoyCrossRefDao // Import for TheBoy CrossRef DAO
import com.example.worktracker.data.repository.OperatorRepository
import com.example.worktracker.data.repository.WorkActivityRepository
import com.example.worktracker.data.repository.WorkActivityRepositoryImpl
import com.example.worktracker.data.repository.ActivityCategoryRepository
import com.example.worktracker.data.repository.ActivityCategoryRepositoryImpl
import com.example.worktracker.data.repository.TheBoysRepository
import com.example.worktracker.data.repository.TheBoysRepositoryImpl
import com.example.worktracker.data.repository.ProductionActivityRepository
import com.example.worktracker.data.repository.ProductionActivityRepositoryImpl
import com.example.worktracker.data.repository.ComponentInfoRepository
import com.example.worktracker.data.repository.ComponentInfoRepositoryImpl
import com.example.worktracker.ui.signin.GoogleAuthUiClient // Added import
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val PREFS_NAME = "WorkTrackerUserPrefs"
    const val KEY_MASTER_PASSWORD = "master_reset_password"
    const val KEY_SMS_CONTACT = "sms_contact"
    const val KEY_GEMINI_API_KEY = "gemini_api_key"

    @Provides
    @Singleton
    fun provideGoogleAuthUiClient(@ApplicationContext context: Context): GoogleAuthUiClient {
        return GoogleAuthUiClient(context = context)
    }

    @Provides
    @Singleton
    fun provideGeminiProService(sharedPreferences: SharedPreferences): GeminiProService {
        return GeminiProService(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "work_tracker_db"
        )
        .addMigrations(
            AppDatabase.MIGRATION_2_3, 
            AppDatabase.MIGRATION_4_5, 
            AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8,
            AppDatabase.MIGRATION_8_9,
            AppDatabase.MIGRATION_9_10,
            AppDatabase.MIGRATION_10_11,
            AppDatabase.MIGRATION_11_12,
            AppDatabase.MIGRATION_12_13,
            AppDatabase.MIGRATION_13_14 // Added MIGRATION_13_14
        )
        // .fallbackToDestructiveMigration(dropAllTables = true) // Keep this commented out for now
        .build()
    }

    // Operator Info Providers
    @Provides
    fun provideOperatorInfoDao(database: AppDatabase): OperatorInfoDao {
        return database.operatorInfoDao()
    }

    @Provides
    @Singleton
    fun provideOperatorRepository(operatorInfoDao: OperatorInfoDao): OperatorRepository {
        return OperatorRepository(operatorInfoDao) 
    }

    // Work Activity Providers
    @Provides
    @Singleton
    fun provideWorkActivityDao(appDatabase: AppDatabase): WorkActivityDao {
        return appDatabase.workActivityDao()
    }
    
    @Provides 
    fun provideWorkActivityComponentCrossRefDao(database: AppDatabase): WorkActivityComponentCrossRefDao {
        return database.workActivityComponentCrossRefDao()
    }

    @Provides
    fun provideWorkActivityTheBoyCrossRefDao(database: AppDatabase): WorkActivityTheBoyCrossRefDao {
        return database.workActivityTheBoyCrossRefDao()
    }

    @Provides
    @Singleton
    fun provideWorkActivityRepository(
        workActivityDao: WorkActivityDao,
        workActivityComponentCrossRefDao: WorkActivityComponentCrossRefDao,
        workActivityTheBoyCrossRefDao: WorkActivityTheBoyCrossRefDao // Added TheBoy CrossRef DAO parameter
    ): WorkActivityRepository {
        return WorkActivityRepositoryImpl(
            workActivityDao,
            workActivityComponentCrossRefDao,
            workActivityTheBoyCrossRefDao // Pass TheBoy CrossRef DAO to Impl
        )
    }

    // Activity Category Providers
    @Provides
    fun provideActivityCategoryDao(database: AppDatabase): ActivityCategoryDao {
        return database.activityCategoryDao()
    }

    @Provides
    @Singleton
    fun provideActivityCategoryRepository(activityCategoryDao: ActivityCategoryDao): ActivityCategoryRepository {
        return ActivityCategoryRepositoryImpl(activityCategoryDao)
    }

    // The Boys Info Providers
    @Provides
    fun provideTheBoysInfoDao(database: AppDatabase): TheBoysInfoDao {
        return database.theBoysInfoDao()
    }

    @Provides
    @Singleton
    fun provideTheBoysRepository(theBoysInfoDao: TheBoysInfoDao): TheBoysRepository {
        return TheBoysRepositoryImpl(theBoysInfoDao)
    }

    // Production Activity Providers
    @Provides
    fun provideProductionActivityDao(database: AppDatabase): ProductionActivityDao {
        return database.productionActivityDao()
    }

    @Provides
    @Singleton
    fun provideProductionActivityRepository(productionActivityDao: ProductionActivityDao): ProductionActivityRepository {
        return ProductionActivityRepositoryImpl(productionActivityDao)
    }

    // ComponentInfo Providers
    @Provides
    fun provideComponentInfoDao(database: AppDatabase): ComponentInfoDao {
        return database.componentInfoDao()
    }

    @Provides
    @Singleton
    fun provideComponentInfoRepository(componentInfoDao: ComponentInfoDao): ComponentInfoRepository {
        return ComponentInfoRepositoryImpl(componentInfoDao)
    }
}
