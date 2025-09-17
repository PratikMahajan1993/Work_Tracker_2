package com.example.worktracker

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
// import androidx.work.ExistingWorkPolicy // No longer needed for one-time work here
import androidx.work.NetworkType
// import androidx.work.OneTimeWorkRequestBuilder // No longer needed for one-time work here
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.worktracker.data.sync.SyncWorker
import com.example.worktracker.workers.UploadAllDataWorker // Added import for UploadAllDataWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        // Explicitly initialize WorkManager with the Hilt-provided factory.
        // This ensures WorkManager uses Hilt for worker instantiation from the start.
        Log.d("MyApplication_WM_Config", "Initializing WorkManager explicitly in onCreate")
        WorkManager.initialize(this, workManagerConfiguration)

        setupBackgroundWorkers() // Renamed method call
    }

    override val workManagerConfiguration: Configuration
        get() {
            val isWorkerFactoryInitialized = ::workerFactory.isInitialized
            Log.d("MyApplication_WM_Config", "workManagerConfiguration accessed. HiltWorkerFactory initialized: $isWorkerFactoryInitialized")
            if (!isWorkerFactoryInitialized) {
                // This would be highly unusual if Hilt is setup correctly and Application class is processed by Hilt.
                Log.e("MyApplication_WM_Config", "HiltWorkerFactory is NOT initialized! Hilt setup issue?")
                // Fallback to default to avoid immediate crash, though Hilt injection for workers will fail.
                return Configuration.Builder().build()
            }
            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .setMinimumLoggingLevel(Log.DEBUG) // Added for more verbose WorkManager logging
                .build()
        }

    private fun setupBackgroundWorkers() { // Renamed method
        // getInstance will now use the configuration provided above due to explicit initialize 
        // or because this Application class is a Configuration.Provider
        val workManager = WorkManager.getInstance(this)

        // Define constraints (e.g., network connected)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Schedule Periodic Sync Worker (Downloads from Firebase)
        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            1, TimeUnit.HOURS // Repeat interval: 1 hour (Kept as per user confirmation)
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "PeriodicFirestoreSync", // Unique name for the work
            ExistingPeriodicWorkPolicy.KEEP, // Policy for existing work
            periodicSyncRequest
        )

        // Schedule Periodic Upload Worker (Uploads Room to Firebase)
        val periodicUploadRequest = PeriodicWorkRequestBuilder<UploadAllDataWorker>(
            15, TimeUnit.MINUTES // Repeat interval: 15 minutes
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "PeriodicDataUpload", // Unique name for this upload work
            ExistingPeriodicWorkPolicy.KEEP,
            periodicUploadRequest
        )
    }
}
