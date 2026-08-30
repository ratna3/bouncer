package com.example.bouncer.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bouncer.data.AOT4221NKRepository
import com.example.bouncer.data.local.BouncerDatabase

/**
 * Background worker that automatically unbans a device after the scheduled duration.
 * Uses CoroutineWorker for proper suspend function support.
 *
 * Survives app kill and phone reboot thanks to WorkManager + RECEIVE_BOOT_COMPLETED.
 *
 * Note: For v1, the router credentials are passed via WorkManager's inputData.
 * This is stored in WorkManager's internal app-private database, which is acceptable
 * for a personal-use app but should be improved in future versions.
 */
class UnbanWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val targetMac = inputData.getString(KEY_TARGET_MAC) ?: return Result.failure()
        val username = inputData.getString(KEY_USERNAME) ?: return Result.failure()
        val password = inputData.getString(KEY_PASSWORD) ?: return Result.failure()
        val baseUrl = inputData.getString(KEY_BASE_URL) ?: "http://192.168.1.1"

        val repo = AOT4221NKRepository(baseUrl)

        // Attempt login
        if (!repo.login(username, password)) {
            return Result.retry()
        }

        // Attempt unban
        val success = repo.setMacBanStatus(targetMac, ban = false)
        if (success) {
            // Remove the ban record from Room
            val dao = BouncerDatabase.getInstance(applicationContext).banRecordDao()
            dao.deleteByMac(targetMac)
        }

        return if (success) Result.success() else Result.retry()
    }

    companion object {
        const val KEY_TARGET_MAC = "TARGET_MAC"
        const val KEY_USERNAME = "USERNAME"
        const val KEY_PASSWORD = "PASSWORD"
        const val KEY_BASE_URL = "BASE_URL"
    }
}
