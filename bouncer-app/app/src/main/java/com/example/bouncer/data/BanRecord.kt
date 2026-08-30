package com.example.bouncer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted record of an active ban. Stored in Room so that ban state survives
 * app kills and reboots, and the UI can show countdown badges immediately on launch.
 */
@Entity(tableName = "ban_records")
data class BanRecord(
    @PrimaryKey val macAddress: String,
    val deviceName: String,
    val bannedAt: Long,        // epoch millis
    val unbanAt: Long,         // epoch millis
    val workRequestId: String  // UUID string for WorkManager cancellation
)
