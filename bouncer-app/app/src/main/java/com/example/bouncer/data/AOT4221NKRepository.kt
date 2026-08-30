package com.example.bouncer.data

import com.example.bouncer.network.RouterHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.IOException

/**
 * Concrete RouterRepository for the Airtel/Nokia AOT4221NK router.
 *
 * TODO: The CSS selectors and endpoint paths below are PLACEHOLDERS carried over
 * from the spec. They are educated guesses, NOT verified against real firmware.
 * Before running against a real AOT4221NK router, inspect the actual admin panel
 * HTML (view source at http://192.168.1.1 while logged in) and correct:
 *   - Login endpoint path and form field names
 *   - DHCP client list page path and table selectors
 *   - MAC filter endpoint path and form field names
 */
class AOT4221NKRepository(
    private val baseUrl: String = "http://192.168.1.1"
) : RouterRepository {

    override suspend fun login(username: String, password: String): Boolean =
        withContext(Dispatchers.IO) {
            val formBody = FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build()
            val request = Request.Builder()
                .url("$baseUrl/api/login")  // TODO: verify actual login endpoint
                .post(formBody)
                .build()
            try {
                RouterHttpClient.client.newCall(request).execute().use { it.isSuccessful }
            } catch (e: IOException) {
                false
            }
        }

    override suspend fun getConnectedDevices(): List<ConnectedDevice> =
        withContext(Dispatchers.IO) {
            val devices = mutableListOf<ConnectedDevice>()
            val request = Request.Builder()
                .url("$baseUrl/dhcp_clients.html")  // TODO: verify actual DHCP page path
                .get()
                .build()
            try {
                RouterHttpClient.client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val html = response.body?.string() ?: return@use
                    val doc = Jsoup.parse(html)
                    // TODO: verify actual table selectors against real router HTML
                    val rows = doc.select("table#dhcp_table tr, table.client-list tr")
                    for (row in rows) {
                        val cols = row.select("td")
                        if (cols.size >= 3) {
                            val name = cols[0].text().trim().ifEmpty { "Unknown Device" }
                            val ip = cols[1].text().trim()
                            val mac = cols[2].text().trim()
                            if (mac.contains(":") || mac.contains("-")) {
                                devices.add(ConnectedDevice(name, ip, mac))
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                // Return whatever we've collected so far
            }
            devices
        }

    override suspend fun setMacBanStatus(macAddress: String, ban: Boolean): Boolean =
        withContext(Dispatchers.IO) {
            val action = if (ban) "add" else "delete"
            val formBody = FormBody.Builder()
                .add("mac", macAddress)
                .add("action", action)
                .add("filter_type", "deny")
                .build()
            val request = Request.Builder()
                .url("$baseUrl/api/mac_filter")  // TODO: verify actual MAC filter endpoint
                .post(formBody)
                .build()
            try {
                RouterHttpClient.client.newCall(request).execute().use { it.isSuccessful }
            } catch (e: IOException) {
                false
            }
        }
}
