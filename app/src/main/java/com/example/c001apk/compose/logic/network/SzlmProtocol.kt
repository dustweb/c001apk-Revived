package com.example.c001apk.compose.logic.network

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.security.SecureRandom
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

/** Wire format and sample report: kongwufang/szlm_res v1.0.0, MIT (assets/licenses). */
object SzlmProtocol {
    const val ENDPOINT = "https://auni.telecome.cn/a/mdna/report?v=8.7&t=m&p=F9BD0FBDACFE80FCF34370FBCFEE1A98&r=5bb7efdd4c794f0e9563317b7802b995&n=0&l=2&h=0"
    const val USER_AGENT = "Dalvik/2.1.0 (Linux; U; Android 8.1; TK Watch Build/OPM2.171019.012)"
    private val pool = "xp7j@&!v5]2k#+3z{n9\$q^r%[8f1*d4e:c>0l?m/6)yb-=w}".toByteArray(Charsets.US_ASCII)
    private val gson = GsonBuilder().disableHtmlEscaping().create()
    private val identifier = Regex("DU[A-Za-z0-9_-]{20,128}")

    fun parseId(text: String): String? {
        val value = text.trim()
        val candidate = if (identifier.matches(value)) value else
            Regex("(?i)(?:ShuzlmID|szlmId|数字联盟\\s*ID)\\s*[:：=]\\s*(DU[A-Za-z0-9_-]{20,128})").find(value)?.groupValues?.get(1)
        return candidate?.takeIf { identifier.matches(it) && it.drop(2).any { char -> char != '0' } }
    }

    fun newReport(template: String, random: SecureRandom = SecureRandom()): JsonObject {
        val report = JsonParser.parseString(template).asJsonObject
        require(report.size() == 79)
        fun hex(length: Int, upper: Boolean = false): String {
            val chars = if (upper) "0123456789ABCDEF" else "0123456789abcdef"
            return buildString { repeat(length) { append(chars[random.nextInt(chars.length)]) } }
        }
        report.addProperty("sfOo", hex(7, true))
        val alphabet = "!#\$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
        report.addProperty("foO", buildString { repeat(15) { append(alphabet[random.nextInt(alphabet.length)]) } })
        report.addProperty("2cO", "${System.currentTimeMillis() / 1000}.${(1 + random.nextInt(999999999)).toString().padStart(9, '0')}")
        report.addProperty("w91", hex(32, true))
        report.addProperty("gEd", hex(32))
        report.addProperty("6yY", hex(64))
        return report
    }

    fun encode(report: JsonObject): ByteArray {
        val output = ByteArrayOutputStream()
        val deflater = Deflater(6)
        try {
            DeflaterOutputStream(output, deflater).use { it.write(gson.toJson(report).replace("/", "\\/").toByteArray(Charsets.UTF_8)) }
        } finally { deflater.end() }
        return xor(output.toByteArray())
    }

    fun decode(response: ByteArray): String? {
        if (response.isEmpty() || response.size > 65536) return null
        val plain = xor(response)
        val decoded = runCatching { InflaterInputStream(ByteArrayInputStream(plain)).use(::readBounded) }.getOrElse { plain }
        return runCatching { parseId(JsonParser.parseString(decoded.toString(Charsets.UTF_8)).asJsonObject["n_a"].asString) }.getOrNull()
    }

    fun readBounded(input: InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            require(output.size() + count <= 65536) { "响应过大" }
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private fun xor(bytes: ByteArray) = ByteArray(bytes.size) { (bytes[it].toInt() xor pool[it % pool.size].toInt()).toByte() }
}
