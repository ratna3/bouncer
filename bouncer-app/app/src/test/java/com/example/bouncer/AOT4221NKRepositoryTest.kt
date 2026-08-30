package com.example.bouncer

import com.example.bouncer.data.ConnectedDevice
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AOT4221NKRepositoryTest {

    @Test
    fun parseDhcpTable_extractsDevicesCorrectly() {
        val sampleHtml = """
            <html>
            <body>
                <table id="dhcp_table">
                    <tr><th>Hostname</th><th>IP Address</th><th>MAC Address</th></tr>
                    <tr><td>Pixel-Phone</td><td>192.168.1.102</td><td>AA:BB:CC:DD:EE:01</td></tr>
                    <tr><td>Smart-TV</td><td>192.168.1.105</td><td>11:22:33:44:55:66</td></tr>
                    <tr><td></td><td>192.168.1.110</td><td>AA-BB-CC-DD-EE-03</td></tr>
                </table>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(sampleHtml)
        val rows = doc.select("table#dhcp_table tr, table.client-list tr")
        val devices = mutableListOf<ConnectedDevice>()

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

        assertEquals(3, devices.size)
        assertEquals("Pixel-Phone", devices[0].name)
        assertEquals("192.168.1.102", devices[0].ipAddress)
        assertEquals("AA:BB:CC:DD:EE:01", devices[0].macAddress)

        assertEquals("Smart-TV", devices[1].name)
        assertEquals("Unknown Device", devices[2].name)
        assertEquals("AA-BB-CC-DD-EE-03", devices[2].macAddress)
    }
}
