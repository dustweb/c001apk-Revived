package com.example.c001apk.compose.logic.datastore

import com.example.c001apk.compose.UiStyle
import com.example.c001apk.compose.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Test

class UiStylePreferencesTest {
    @Test
    fun olderPreferencesWithoutStyleRemainMaterial3() {
        val old = UserPreferencesCompat.default().toProto().toBuilder().clearUiStyle().build()
        assertEquals(UiStyle.MD3, UserPreferencesCompat(old).uiStyle)
    }

    @Test
    fun styleSurvivesSerializationWithoutChangingOtherPreferences() {
        val original = UserPreferencesCompat.default().copy(
            uiStyle = UiStyle.MIUIX, seedColor = "4388CC", fontScale = 1.2f, pureBlack = true,
        )
        val restored = UserPreferencesCompat(UserPreferences.parseFrom(original.toProto().toByteArray()))
        assertEquals(original, restored)
    }

    @Test
    fun unknownStyleFallsBackToMaterial3() {
        val future = UserPreferencesCompat.default().toProto().toBuilder().setUiStyleValue(99).build()
        assertEquals(UiStyle.MD3, UserPreferencesCompat(future).uiStyle)
    }
}
