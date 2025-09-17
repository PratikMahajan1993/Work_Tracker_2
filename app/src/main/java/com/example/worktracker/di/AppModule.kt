package com.example.worktracker.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.work.WorkManager
import com.example.worktracker.GeminiProService
import com.example.worktracker.data.database.AppDatabase
import com.example.worktracker.data.database.dao.*
import com.example.worktracker.data.repository.*
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
            AppDatabase.MIGRATION_16_17,
            AppDatabase.MIGRATION_17_18
        )
        .build()
    }
    
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    fun provideOperatorInfoDao(database: AppDatabase): OperatorInfoDao = database.operatorInfoDao()

    @Provides
    @Singleton
    fun provideOperatorRepository(dao: OperatorInfoDao, syncManager: IFirestoreSyncManager, auth: FirebaseAuth): OperatorRepository =
        OperatorRepositoryImpl(dao, syncManager, auth)

    @Provides
    @Singleton
    fun provideWorkActivityDao(appDatabase: AppDatabase): WorkActivityDao = appDatabase.workActivityDao()
    
    @Provides 
    fun provideWorkActivityComponentCrossRefDao(database: AppDatabase): WorkActivityComponentCrossRefDao = database.workActivityComponentCrossRefDao()

    @Provides
    @Singleton
    fun provideWorkActivityRepository(
        workActivityDao: WorkActivityDao,
        crossRefDao: WorkActivityComponentCrossRefDao,
        syncManager: IFirestoreSyncManager, 
        auth: FirebaseAuth
    ): WorkActivityRepository = WorkActivityRepositoryImpl(workActivityDao, crossRefDao, syncManager, auth)

    @Provides
    fun provideActivityCategoryDao(database: AppDatabase): ActivityCategoryDao = database.activityCategoryDao()

    @Provides
    @Singleton
    fun provideActivityCategoryRepository(dao: ActivityCategoryDao, syncManager: IFirestoreSyncManager, auth: FirebaseAuth): ActivityCategoryRepository =
        ActivityCategoryRepositoryImpl(dao, syncManager, auth)

    @Provides
    fun provideTheBoysInfoDao(database: AppDatabase): TheBoysInfoDao = database.theBoysInfoDao()

    @Provides
    @Singleton
    fun provideTheBoysRepository(dao: TheBoysInfoDao, syncManager: IFirestoreSyncManager, auth: FirebaseAuth): TheBoysRepository =
        TheBoysRepositoryImpl(dao, syncManager, auth)

    @Provides
    fun provideProductionActivityDao(database: AppDatabase): ProductionActivityDao = database.productionActivityDao()

    @Provides
    @Singleton
    fun provideProductionActivityRepository(dao: ProductionActivityDao, syncManager: IFirestoreSyncManager, auth: FirebaseAuth): ProductionActivityRepository =
        ProductionActivityRepositoryImpl(dao, syncManager, auth)

    @Provides
    fun provideComponentInfoDao(database: AppDatabase): ComponentInfoDao = database.componentInfoDao()

    @Provides
    @Singleton
    fun provideComponentInfoRepository(dao: ComponentInfoDao, syncManager: IFirestoreSyncManager, auth: FirebaseAuth): ComponentInfoRepository =
        ComponentInfoRepositoryImpl(dao, syncManager, auth)
}
