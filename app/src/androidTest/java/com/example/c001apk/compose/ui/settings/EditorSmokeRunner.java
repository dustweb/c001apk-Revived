package com.example.c001apk.compose.ui.settings;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;

/** Platform-only smoke runner so R8 can rename Kotlin and AndroidX classes freely. */
public class EditorSmokeRunner extends Instrumentation {
    private boolean captureTopic;
    @Override public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        captureTopic = arguments != null && "true".equals(arguments.getString("captureTopic"));
        start();
    }
    private View find(View view, String text, boolean description) {
        CharSequence value = description ? view.getContentDescription() : view instanceof TextView ? ((TextView) view).getText() : null;
        if (text.contentEquals(value == null ? "" : value)) return view;
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
            View found = find(((ViewGroup) view).getChildAt(i), text, description);
            if (found != null) return found;
        }
        return null;
    }
    private void capture(String name) throws Exception {
        Bitmap bitmap = getUiAutomation().takeScreenshot();
        if (bitmap != null) {
            try (FileOutputStream output = new FileOutputStream(new File(getTargetContext().getExternalFilesDir(null), name + ".png"))) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            }
            bitmap.recycle();
        }
    }
    @Override public void onStart() {
        Bundle result = new Bundle();
        Activity activity = null;
        try {
            activity = startActivitySync(new Intent().setClassName(getTargetContext().getPackageName(),
                "com.example.c001apk.compose.ui.feed.reply.ReplyActivity").putExtra("type", "createFeed")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            final Activity editor = activity;
            SystemClock.sleep(2500);
            waitForIdleSync();
            capture("release-editor-keyboard");
            final String[] error = {null};
            runOnMainSync(() -> {
                View root = editor.getWindow().getDecorView();
                TextView title = (TextView) find(root, "发布动态", false);
                View publish = find(root, "发送", true);
                Rect rect = new Rect();
                if (title == null || publish == null) error[0] = "Header controls are missing";
                else if (title.getLeft() + title.getPaddingLeft() < editor.getResources().getDisplayMetrics().density * 8) error[0] = "Title has no start inset";
                else if (!publish.getGlobalVisibleRect(rect) || rect.width() != publish.getWidth()) error[0] = "Publish button is clipped";
                else if (publish.getWidth() < editor.getResources().getDisplayMetrics().density * 48) error[0] = "Send touch target is too small";
            });
            final String headerError = error[0];
            runOnMainSync(() -> {
                View emoji = find(editor.getWindow().getDecorView(), "表情", true);
                if (emoji != null) emoji.performClick(); else error[0] = "Emoji button is missing";
            });
            SystemClock.sleep(1500);
            capture("release-editor-emoji");
            runOnMainSync(() -> {
                View recent = find(editor.getWindow().getDecorView(), "最近", false);
                if (recent == null || !recent.isShown()) error[0] = "Emoji panel did not open";
            });
            if (headerError != null || error[0] != null) throw new AssertionError(headerError + "; " + error[0]);
            if (captureTopic) {
                Activity picker = startActivitySync(new Intent().setClassName(getTargetContext().getPackageName(),
                    "com.example.c001apk.compose.ui.feed.reply.AtTopicActivity").putExtra("type", "topic")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                SystemClock.sleep(2500);
                waitForIdleSync();
                capture("release-topic-picker");
                runOnMainSync(picker::finish);
            }
            result.putString("stream", "Editor window smoke passed\n");
            finish(Activity.RESULT_OK, result);
        } catch (Throwable failure) {
            result.putString("stream", android.util.Log.getStackTraceString(failure));
            finish(Activity.RESULT_CANCELED, result);
        } finally {
            if (activity != null) { final Activity last = activity; runOnMainSync(last::finish); }
        }
    }
}
