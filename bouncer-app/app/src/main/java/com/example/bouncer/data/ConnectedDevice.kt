package com.example.bouncer.data

/**
 * Represents a device currently connected to the router's network.
 * Populated by scraping the router's DHCP client list page.
 */
data class ConnectedDevice(
    val name: String,
    val ipAddress: String,
    val macAddress: String
)
