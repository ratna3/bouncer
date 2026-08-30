package com.example.bouncer.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bouncer.data.BanRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface BanRecordDao {
    @Query("SELECT * FROM ban_records")
    fun getAllBanRecords(): Flow<List<BanRecord>>

    @Query("SELECT * FROM ban_records")
    suspend fun getAllBanRecordsList(): List<BanRecord>

    @Query("SELECT * FROM ban_records WHERE macAddress = :mac")
    suspend fun getBanRecord(mac: String): BanRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(banRecord: BanRecord)

    @Delete
    suspend fun delete(banRecord: BanRecord)

    @Query("DELETE FROM ban_records WHERE macAddress = :mac")
    suspend fun deleteByMac(mac: String)

    @Query("DELETE FROM ban_records WHERE unbanAt <= :currentTime")
    suspend fun deleteExpired(currentTime: Long)
}
