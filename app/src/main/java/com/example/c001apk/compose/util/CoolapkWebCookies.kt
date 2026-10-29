package com.example.c001apk.compose.util

import android.webkit.CookieManager
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat

private var preparation = 0L

/** Completes cookie replacement before the WebView starts its first request. */
fun prepareCoolapkWebCookies(prefs: UserPreferencesCompat, loginPage: Boolean, onReady: () -> Unit) {
    val request = ++preparation
    val manager = CookieManager.getInstance()
    val hosts = listOf("www.coolapk.com", "account.coolapk.com", "api.coolapk.com")
    val removals = hosts.flatMap { host ->
        manager.getCookie("https://$host").orEmpty().split(';').mapNotNull { part ->
            part.substringBefore('=').trim().takeIf { it.isNotEmpty() }?.let { name ->
                listOf("https://$host" to "$name=; Max-Age=0; Path=/",
                    "https://$host" to "$name=; Max-Age=0; Path=/; Domain=.coolapk.com")
            }
        }.flatten()
    }
    fun install() {
        if (request != preparation) return
        val values = mutableListOf("displayVersion=v14", "forward=https://www.coolapk.com")
        if (prefs.szlmId.isNotBlank()) values += "DID=${prefs.szlmId}"
        if (!loginPage && prefs.isLogin) values += listOf("uid=${prefs.uid}", "username=${prefs.username}", "token=${prefs.token}")
        var pending = values.size
        values.forEach { value ->
            val httpOnly = if (value.startsWith("token=")) "; HttpOnly" else ""
            manager.setCookie("https://www.coolapk.com", "$value; Domain=.coolapk.com; Path=/; Secure$httpOnly") {
                if (--pending == 0 && request == preparation) { manager.flush(); onReady() }
            }
        }
    }
    if (removals.isEmpty()) install() else {
        var pending = removals.size
        removals.forEach { (url, cookie) -> manager.setCookie(url, cookie) { if (--pending == 0) install() } }
    }
}
