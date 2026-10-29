package com.example.c001apk.compose.logic.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser

internal data class FeedMedia(val pic: String?, val images: List<String>, val feedType: String?)

internal object FeedMediaJson {
    fun encode(feed: HomeFeedResponse.Data): String = JsonObject().apply {
        addProperty("pic", feed.pic)
        addProperty("feedType", feed.feedType)
        add("images", JsonArray().apply { (feed.picArr?.takeIf { it.isNotEmpty() } ?: listOfNotNull(feed.pic?.takeIf { it.startsWith("https://") || it.startsWith("http://") })).forEach { add(it) } })
    }.toString()

    fun decode(value: String?): FeedMedia? = value?.let {
        runCatching {
            val data = JsonParser.parseString(it).asJsonObject
            fun string(key: String) = data.get(key)?.takeIf { field -> field.isJsonPrimitive && field.asJsonPrimitive.isString }?.asString
            val images = data.getAsJsonArray("images")?.mapNotNull { item ->
                item.takeIf { field -> field.isJsonPrimitive && field.asJsonPrimitive.isString }?.asString
                    ?.takeIf { url -> url.startsWith("https://") || url.startsWith("http://") }
            }.orEmpty()
            FeedMedia(string("pic"), images, string("feedType"))
        }.getOrNull()
    }
}
