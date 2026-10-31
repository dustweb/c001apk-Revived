package com.example.c001apk.compose.logic.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Assert.*
import org.junit.Test

class PersonalClientReleaseTest {
    private fun release(id: Int, code: Int, prerelease: Boolean = false): JsonObject {
        val tag = "composf-upstream-$id"
        return JsonObject().apply {
            addProperty("tag_name", tag); addProperty("draft", false); addProperty("prerelease", prerelease)
            addProperty("name", "build"); addProperty("body", "C001_VERSION_CODE: $code\n")
            add("assets", JsonArray().apply { add(JsonObject().apply {
                addProperty("state", "uploaded"); addProperty("name", "c001apk-composf.apk")
                addProperty("browser_download_url", "https://github.com/dustweb/c001apk-Revived/releases/download/$tag/c001apk-composf.apk")
            }) })
        }
    }
    private fun list(vararg r: JsonObject) = JsonArray().apply { r.forEach { add(it) } }.toString()
    @Test fun choosesHighestVersionIncludingPrerelease() {
        val x = ClientRelease.parse(list(release(30, 1006000, true), release(31, 1005000)))
        assertEquals(1006000, x.versionCode); assertTrue(x.name.contains("预发布版"))
        assertEquals("https://github.com/dustweb/c001apk-Revived/releases/tag/composf-upstream-30", x.pageUrl)
    }
    @Test fun hotfixTagAccepted() {
        val x = release(31, 1007000); val tag = "composf-upstream-31-fix-update-v1"
        x.addProperty("tag_name",tag)
        x.getAsJsonArray("assets")[0].asJsonObject.addProperty("browser_download_url", "https://github.com/dustweb/c001apk-Revived/releases/download/$tag/c001apk-composf.apk")
        assertEquals(1007000, ClientRelease.parse(list(x)).versionCode)
    }
    @Test fun rejectsUpstreamDownloadAndMissingMetadata() {
        val x=release(2,1006000)
        x.getAsJsonArray("assets")[0].asJsonObject.addProperty("browser_download_url","https://github.com/frisk1127/c001apk-Revived/releases/download/2/c001apk-composf.apk")
        assertThrows(IllegalArgumentException::class.java) { ClientRelease.parse(list(x)) }
        val y=release(3,1006000); y.addProperty("body","nothing")
        assertThrows(IllegalArgumentException::class.java) { ClientRelease.parse(list(y)) }
    }
    @Test fun rejectsAmbiguousVersionAndOversizedList() {
        assertThrows(IllegalArgumentException::class.java) { ClientRelease.parse(list(release(2,1006000),release(3,1006000))) }
        val many=JsonArray(); repeat(100) { many.add(release(it+1,1000000+it)) }
        assertThrows(IllegalArgumentException::class.java) { ClientRelease.parse(many.toString()) }
    }
}
