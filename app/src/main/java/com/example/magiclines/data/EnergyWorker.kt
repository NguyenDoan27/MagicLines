package com.example.magiclines.data

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit

class EnergyWorker(private val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    private val mutex = Mutex()

    override suspend fun doWork(): Result {
        val preferences = SettingDataStore(context)

        return try {
            val value = preferences.energy.first()
            if (value < 5) {
                val newEnergy = value + 1
                preferences.saveEnergy(newEnergy)
                Log.e("EnergyWorker", "Updated energy to: $newEnergy")
            } else {
                Log.e("EnergyWorker", "Energy is already at maximum. Cancelling all work.")
                WorkManager.getInstance(applicationContext).cancelUniqueWork("EnergyWork")
                return Result.success()  // Kết thúc nếu energy đã đủ
            }

            scheduleNextWork()
            Result.success()
        } catch (e: Exception) {
            Log.e("EnergyWorker", "Error: ${e.message}")
            Result.retry()
        }
    }


    private fun scheduleNextWork() {
        WorkManager.getInstance(applicationContext).cancelUniqueWork("EnergyWork")

        val nextWorkRequest = OneTimeWorkRequestBuilder<EnergyWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "EnergyWork",
            ExistingWorkPolicy.REPLACE,
            nextWorkRequest
        )
    }
}