package com.example.bouncer.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Singleton HTTP client configured for router communication.
 * Uses a shared cookie jar so login session persists across requests.
 */
object RouterHttpClient {
    val cookieJar = RouterCookieJar()

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }
}
