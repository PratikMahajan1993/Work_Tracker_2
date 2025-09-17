package com.example.worktracker.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.work.WorkManager // Added import for WorkManager
import com.example.worktracker.GeminiProService
import com.example.worktracker.data.database.AppDatabase
import com.example.worktracker.data.database.dao.OperatorInfoDao
import com.example.worktracker.data.database.dao.WorkActivityDao
import com.example.worktracker.data.database.dao.ActivityCategoryDao
import com.example.worktracker.data.database.dao.TheBoysInfoDao
import com.example.worktracker.data.database.dao.ProductionActivityDao
import com.example.worktracker.data.database.dao.ComponentInfoDao
import com.example.worktracker.data.database.dao.WorkActivityComponentCrossRefDao
// Removed: import com.example.worktracker.data.database.dao.WorkActivityTheBoyCrossRefDao
import com.example.worktracker.data.repository.OperatorRepository
import com.example.worktracker.data.repository.OperatorRepositoryImpl
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
import com.example.worktracker.data.sync.FirestoreSyncManagerImpl
import com.example.worktracker.data.sync.IFirestoreSyncManager
import com.example.worktracker.ui.signin.GoogleAuthUiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirestoreSyncManager(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): IFirestoreSyncManager {
        return FirestoreSyncManagerImpl(firestore, firebaseAuth)
    }

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
            AppDatabase.MIGRATION_13_14,
            AppDatabase.MIGRATION_14_15,
            AppDatabase.MIGRATION_15_16,
            AppDatabase.MIGRATION_16_17 // Added MIGRATION_16_17
        )
        .build()
    }
    
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager { // Added WorkManager provider
        return WorkManager.getInstance(context)
    }

    // Operator Info Providers
    @Provides
    fun provideOperatorInfoDao(database: AppDatabase): OperatorInfoDao {
        return database.operatorInfoDao()
    }

    @Provides
    @Singleton
    fun provideOperatorRepository(
        operatorInfoDao: OperatorInfoDao,
        firestoreSyncManager: IFirestoreSyncManager,
        firebaseAuth: FirebaseAuth
    ): OperatorRepository { 
        return OperatorRepositoryImpl( 
            operatorInfoDao,
            firestoreSyncManager,
            firebaseAuth
        ) 
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

    // Removed provideWorkActivityTheBoyCrossRefDao method

    @Provides
    @Singleton
    fun provideWorkActivityRepository(
        workActivityDao: WorkActivityDao,
        workActivityComponentCrossRefDao: WorkActivityComponentCrossRefDao,
        // Removed: workActivityTheBoyCrossRefDao: WorkActivityTheBoyCrossRefDao,
        firestoreSyncManager: IFirestoreSyncManager, 
        firebaseAuth: FirebaseAuth
    ): WorkActivityRepository {
        return WorkActivityRepositoryImpl(
            workActivityDao,
            workActivityComponentCrossRefDao,
            // Removed: workActivityTheBoyCrossRefDao,
            firestoreSyncManager, 
            firebaseAuth
        )
    }

    // Activity Category Providers
    @Provides
    fun provideActivityCategoryDao(database: AppDatabase): ActivityCategoryDao {
        return database.activityCategoryDao()
    }

    @Provides
    @Singleton
    fun provideActivityCategoryRepository(
        activityCategoryDao: ActivityCategoryDao,
        firestoreSyncManager: IFirestoreSyncManager, 
        firebaseAuth: FirebaseAuth
    ): ActivityCategoryRepository {
        return ActivityCategoryRepositoryImpl(
            activityCategoryDao,
            firestoreSyncManager, 
            firebaseAuth
        )
    }

    // The Boys Info Providers
    @Provides
    fun provideTheBoysInfoDao(database: AppDatabase): TheBoysInfoDao {
        return database.theBoysInfoDao()
    }

    @Provides
    @Singleton
    fun provideTheBoysRepository(
        theBoysInfoDao: TheBoysInfoDao,
        firestoreSyncManager: IFirestoreSyncManager, 
        firebaseAuth: FirebaseAuth
    ): TheBoysRepository {
        return TheBoysRepositoryImpl(
            theBoysInfoDao,
            firestoreSyncManager, 
            firebaseAuth
        )
    }

    // Production Activity Providers
    @Provides
    fun provideProductionActivityDao(database: AppDatabase): ProductionActivityDao {
        return database.productionActivityDao()
    }

    @Provides
    @Singleton
    fun provideProductionActivityRepository(
        productionActivityDao: ProductionActivityDao,
        firestoreSyncManager: IFirestoreSyncManager, 
        firebaseAuth: FirebaseAuth
    ): ProductionActivityRepository {
        return ProductionActivityRepositoryImpl(
            productionActivityDao,
            firestoreSyncManager, 
            firebaseAuth
        )
    }

    // ComponentInfo Providers
    @Provides
    fun provideComponentInfoDao(database: AppDatabase): ComponentInfoDao {
        return database.componentInfoDao()
    }

    @Provides
    @Singleton
    fun provideComponentInfoRepository(
        componentInfoDao: ComponentInfoDao,
        firestoreSyncManager: IFirestoreSyncManager, 
        firebaseAuth: FirebaseAuth
    ): ComponentInfoRepository {
        return ComponentInfoRepositoryImpl(
            componentInfoDao,
            firestoreSyncManager, 
            firebaseAuth
        )
    }
}
