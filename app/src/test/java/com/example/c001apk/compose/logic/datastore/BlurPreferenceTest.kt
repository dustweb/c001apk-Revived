package com.example.c001apk.compose.logic.datastore

import com.example.c001apk.compose.UserPreferences
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlurPreferenceTest {
    @Test fun olderPreferencesEnableBlurByDefault() {
        assertTrue(UserPreferencesCompat(UserPreferences.getDefaultInstance()).blurEnabled)
    }

    @Test fun explicitlyDisabledBlurSurvivesSerialization() {
        val saved = UserPreferencesCompat.default().copy(blurEnabled = false).toProto()
        assertTrue(saved.hasBlurEnabled())
        assertFalse(UserPreferencesCompat(UserPreferences.parseFrom(saved.toByteArray())).blurEnabled)
    }
}
