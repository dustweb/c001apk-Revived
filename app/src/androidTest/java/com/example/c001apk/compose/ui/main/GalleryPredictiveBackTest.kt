package com.example.c001apk.compose.ui.main

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.activity.BackEventCompat
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import net.mikaelzero.mojito.Mojito
import net.mikaelzero.mojito.ui.ImageMojitoActivity
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class GalleryPredictiveBackTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun imagePreviewRestoresOnCancelAndClosesOnCommit() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val fixture = File(context.cacheDir, "predictive-gallery-test.png")
        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply { eraseColor(android.graphics.Color.BLUE) }
        fixture.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity { activity -> Mojito.start(activity) { urls(fixture.toURI().toString()) } }
                var gallery: ImageMojitoActivity? = null
                compose.waitUntil(10_000) {
                    InstrumentationRegistry.getInstrumentation().runOnMainSync {
                        gallery = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                            .filterIsInstance<ImageMojitoActivity>().firstOrNull()
                    }
                    gallery != null
                }
                val activity = checkNotNull(gallery)
                lateinit var root: android.view.View
                InstrumentationRegistry.getInstrumentation().runOnMainSync {
                    root = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
                    activity.onBackPressedDispatcher.dispatchOnBackStarted(BackEventCompat(0f, 100f, 0f, BackEventCompat.EDGE_LEFT))
                    activity.onBackPressedDispatcher.dispatchOnBackProgressed(BackEventCompat(100f, 100f, .5f, BackEventCompat.EDGE_LEFT))
                    assertTrue(root.scaleX < 1f)
                    activity.onBackPressedDispatcher.dispatchOnBackCancelled()
                }
                compose.waitUntil(5_000) { root.scaleX == 1f && root.translationX == 0f }
                assertFalse(activity.isFinishing)
                InstrumentationRegistry.getInstrumentation().runOnMainSync {
                    activity.onBackPressedDispatcher.dispatchOnBackStarted(BackEventCompat(0f, 100f, 0f, BackEventCompat.EDGE_LEFT))
                    activity.onBackPressedDispatcher.dispatchOnBackProgressed(BackEventCompat(100f, 100f, .7f, BackEventCompat.EDGE_LEFT))
                    activity.onBackPressedDispatcher.onBackPressed()
                }
                compose.waitUntil(5_000) { activity.isDestroyed }
            }
        } finally {
            fixture.delete()
        }
    }
}
