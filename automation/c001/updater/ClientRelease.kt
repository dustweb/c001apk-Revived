package com.example.c001apk.compose.logic.model

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/** Personal builds only. Never offer an upstream APK as an update. */
data class ClientRelease(
    val tag: String,
    val versionCode: Int,
    val name: String,
    val notes: String,
    val apkUrl: String?,
) {
    val pageUrl: String get() = "$PERSONAL_RELEASES_URL/tag/$tag"

    companion object {
        const val PERSONAL_RELEASES_URL = "https://github.com/dustweb/c001apk-Revived/releases"
        private val personalTag = Regex("^composf-upstream-[1-9][0-9]*(?:-fix-update-v1)?$")
        private val versionLine = Regex("(?m)^C001_VERSION_CODE: ([1-9][0-9]*)\\r?$")

        fun parse(json: String): ClientRelease {
            val root = JsonParser.parseString(json)
            require(root.isJsonArray) { "发布列表格式错误，请在 GitHub 查看。" }
            val items = root.asJsonArray
            require(items.size() < 100) { "发布列表过长，无法安全判断最新版本，请在 GitHub 查看。" }
            val candidates = items.mapNotNull { item ->
                require(item.isJsonObject) { "发布列表格式错误" }
                val obj = item.asJsonObject
                val tag = obj.string("tag_name")
                if (!tag.startsWith("composf-upstream-")) return@mapNotNull null
                require(personalTag.matches(tag)) { "个人版标签格式异常" }
                require(obj.has("draft") && obj.has("prerelease")) { "发布状态缺失" }
                if (obj.get("draft").asBoolean) return@mapNotNull null
                val body = obj.string("body")
                val matches = versionLine.findAll(body).toList()
                require(matches.size == 1) { "个人版版本号缺失或重复" }
                val version = matches.single().groupValues[1].toIntOrNull()
                require(version != null && version in 1..2_100_000_000) { "个人版版本号无效" }
                val assets = obj.getAsJsonArray("assets") ?: error("个人版安装包缺失")
                val apk = assets.mapNotNull { element ->
                    if (!element.isJsonObject) return@mapNotNull null
                    val asset = element.asJsonObject
                    val url = asset.string("browser_download_url").toHttpUrlOrNull()
                    if (asset.string("state") != "uploaded" || asset.string("name") != "c001apk-composf.apk" ||
                        url == null || url.scheme != "https" || url.host != "github.com" || url.port != 443 ||
                        url.username.isNotEmpty() || url.password.isNotEmpty() || url.query != null || url.fragment != null ||
                        url.encodedPath != "/dustweb/c001apk-Revived/releases/download/$tag/c001apk-composf.apk") null
                    else url.toString()
                }
                require(apk.size == 1) { "个人版安装包缺失或链接异常" }
                val notes = body.ifBlank { "此版本未提供更新说明。" }.take(24_000)
                val name = obj.string("name").ifBlank { tag } + if (obj.get("prerelease").asBoolean) "（预发布版）" else ""
                ClientRelease(tag, version!!, name, notes, apk.single())
            }
            require(candidates.isNotEmpty()) { "没有找到可用的个人版发布" }
            require(candidates.map { it.versionCode }.toSet().size == candidates.size) { "个人版版本号重复" }
            return candidates.maxBy { it.versionCode }
        }
        private fun JsonObject.string(key: String): String = get(key)?.takeUnless { it.isJsonNull }?.asString.orEmpty()
    }
}
