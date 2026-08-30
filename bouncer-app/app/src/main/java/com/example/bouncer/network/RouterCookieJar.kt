package com.example.bouncer.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * In-memory cookie jar for maintaining session with the router admin panel.
 * Cookies are stored per-host and survive across requests within the same app session.
 */
class RouterCookieJar : CookieJar {
    private val cookieStore = mutableMapOf<String, List<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url.host] = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> =
        cookieStore[url.host] ?: emptyList()

    fun clear() {
        cookieStore.clear()
    }
}
