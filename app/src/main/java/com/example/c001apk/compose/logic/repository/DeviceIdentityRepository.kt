package com.example.c001apk.compose.logic.repository

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import com.example.c001apk.compose.logic.network.SzlmProtocol
import com.example.c001apk.compose.util.TokenDeviceUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceIdentityRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val store: DataStore<UserPreferencesCompat>,
) {
    private val acquisition = Mutex()
    private val client = OkHttpClient.Builder().callTimeout(20, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS).retryOnConnectionFailure(false).followRedirects(false).build()

    suspend fun acquire(expectedSession: Long? = null): String = withContext(Dispatchers.IO) {
        check(acquisition.tryLock()) { "正在获取，请稍候" }
        try {
            val before = store.data.first()
            check(expectedSession == null || (before.isLogin && before.sessionGeneration == expectedSession)) { "账号已切换" }
            val now = System.currentTimeMillis()
            check(now - before.szlmLastAttempt >= 60_000) { "获取过于频繁，请稍后再试" }
            store.updateData { it.copy(szlmLastAttempt = now) }
            val report = context.assets.open("szlm_report.json").bufferedReader().use { SzlmProtocol.newReport(it.readText()) }
            val request = Request.Builder().url(SzlmProtocol.ENDPOINT)
                .header("User-Agent", SzlmProtocol.USER_AGENT).header("Accept-Encoding", "identity")
                .post(SzlmProtocol.encode(report).toRequestBody("application/x-www-form-urlencoded".toMediaType())).build()
            val id = client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "服务暂不可用，请稍后再试" }
                response.body?.byteStream()?.use { SzlmProtocol.decode(SzlmProtocol.readBounded(it)) }
                    ?: error("服务未返回有效标识，可手动输入")
            }
            store.updateData { current ->
                check(expectedSession == null || (current.isLogin && current.sessionGeneration == expectedSession)) { "账号已切换" }
                check(current.szlmId == before.szlmId && current.xAppDevice == before.xAppDevice) { "设备参数已更新，已保留当前值" }
                current.withId(id)
            }
            id
        } finally { acquisition.unlock() }
    }

    suspend fun save(text: String) {
        val id = if (text.isBlank()) "" else SzlmProtocol.parseId(text) ?: error("请输入有效的数字联盟 ID")
        store.updateData { it.withId(id) }
    }

    private fun UserPreferencesCompat.withId(id: String): UserPreferencesCompat {
        val decoded = runCatching { Base64.decode(xAppDevice.reversed(), Base64.DEFAULT).toString(Charsets.UTF_8) }.getOrDefault("")
        val suffix = if (';' in decoded) decoded.substring(decoded.indexOf(';')) else
            "; ; ; ; $manufacturer; $brand; $model; $buildNumber; null"
        return copy(szlmId = id, xAppDevice = TokenDeviceUtils.encode(id + suffix))
    }
}
