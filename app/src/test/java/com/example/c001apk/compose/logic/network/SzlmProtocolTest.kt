package com.example.c001apk.compose.logic.network

import com.google.gson.JsonParser
import java.io.ByteArrayInputStream
import java.io.File
import java.util.Base64
import org.junit.Assert.*
import org.junit.Test

class SzlmProtocolTest {
    @Test fun decodesIndependentCompressedAndPlainResponseFixtures() {
        // Fixtures encoded with Python's zlib and XOR, containing only a synthetic ID.
        listOf("A1JZNSEEG1RxCF0NRUdaFB4oUFwFKwBAagpVBR9SA10DUw8CXwtYGQERQFIPQA==",
            "AOycPIqtbiKHD0Bi7mR4sbKiclG68loMdnJTBRhSBVAJ1I4ELI/5RTd+RW9L").forEach {
            assertEquals("DUofflineFixture12345678901234567890", SzlmProtocol.decode(Base64.getDecoder().decode(it)))
        }
    }

    @Test fun invalidResponsesAndOversizedPayloadsAreRejected() {
        assertNull(SzlmProtocol.decode(byteArrayOf()))
        assertNull(SzlmProtocol.decode("not a report".toByteArray()))
        assertNull(SzlmProtocol.parseId("DU" + "0".repeat(34)))
        assertTrue(runCatching { SzlmProtocol.readBounded(ByteArrayInputStream(ByteArray(65537))) }.isFailure)
    }

    @Test fun requestChangesOnlyTheSixIdentityFields() {
        val template = File("src/main/assets/szlm_report.json").readText()
        val original = JsonParser.parseString(template).asJsonObject
        val report = SzlmProtocol.newReport(template)
        assertEquals(79, report.size())
        val changed = original.keySet().filter { original[it] != report[it] }.toSet()
        assertEquals(setOf("sfOo", "foO", "2cO", "w91", "gEd", "6yY"), changed)
        assertTrue(report["sfOo"].asString.matches(Regex("[0-9A-F]{7}")))
        assertEquals(15, report["foO"].asString.length)
        assertTrue(SzlmProtocol.encode(report).isNotEmpty())
    }

    @Test fun recognizesAnOfficialCopyWithoutAcceptingUnrelatedText() {
        val id = "DUofflineFixture12345678901234567890"
        assertEquals(id, SzlmProtocol.parseId("OAID: different\nShuzlmID: $id\n"))
        assertNull(SzlmProtocol.parseId("OAID: $id"))
    }
}
