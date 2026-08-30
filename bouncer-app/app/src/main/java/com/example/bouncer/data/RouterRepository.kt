package com.example.bouncer.data

/**
 * Abstraction for router interaction. The concrete implementation handles
 * the specific HTTP endpoints and HTML parsing for a given router model.
 * Keeping this as an interface allows swapping to a different router adapter later.
 */
interface RouterRepository {
    suspend fun login(username: String, password: String): Boolean
    suspend fun getConnectedDevices(): List<ConnectedDevice>
    suspend fun setMacBanStatus(macAddress: String, ban: Boolean): Boolean
}
