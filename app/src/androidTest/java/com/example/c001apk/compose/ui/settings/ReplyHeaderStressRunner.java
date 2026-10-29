package com.example.c001apk.compose.ui.settings;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/** Exercises the shipped APK without referencing classes renamed by R8. */
public class ReplyHeaderStressRunner extends Instrumentation {
    private volatile Activity current;
    private PrintWriter report;
    private int failures;
    private int checks;
    private int repeats;
    private boolean stopOnFailure;

    @Override public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        repeats = Integer.parseInt(arguments.getString("repeats", "1"));
        stopOnFailure = "true".equals(arguments.getString("stopOnFailure"));
        start();
    }
    @Override public void callActivityOnCreate(Activity activity, Bundle state) {
        super.callActivityOnCreate(activity, state);
        if (activity.getClass().getName().endsWith(".ReplyActivity")) current = activity;
    }
    private View view(String id) {
        return current.findViewById(getTargetContext().getResources().getIdentifier(id, "id", getTargetContext().getPackageName()));
    }
    private boolean visible(View view) {
        Rect rect = new Rect();
        return view != null && view.isShown() && view.getWidth() > 0 && view.getHeight() > 0
            && view.getGlobalVisibleRect(rect) && rect.width() >= view.getWidth() - 1 && rect.height() >= view.getHeight() - 1;
    }
    private String describe(View view) {
        if (view == null) return "null";
        Rect rect = new Rect(); view.getGlobalVisibleRect(rect);
        return view.getClass().getSimpleName() + " visibility=" + view.getVisibility() + " shown=" + view.isShown()
            + " size=" + view.getWidth() + "x" + view.getHeight() + " rect=" + rect
            + " alpha=" + view.getAlpha() + " translation=" + view.getTranslationX() + "," + view.getTranslationY()
            + " layoutRequested=" + view.isLayoutRequested();
    }
    private void capture(String name) {
        try {
            Bitmap bitmap = getUiAutomation().takeScreenshot();
            if (bitmap != null) {
                try (FileOutputStream out = new FileOutputStream(new File(getTargetContext().getExternalFilesDir(null), name + ".png"))) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                }
                bitmap.recycle();
            }
        } catch (Exception e) { report.println(e); }
    }
    private void check(String phase) {
        long deadline = SystemClock.uptimeMillis() + 4000;
        boolean[] ready = {false};
        while (SystemClock.uptimeMillis() < deadline) {
            runOnMainSync(() -> {
                if (current == null || current.isDestroyed()) return;
                ready[0] = visible(view("replyCancel")) && visible(view("replySend")) && visible(view("replyDragHandle"))
                    && ((ImageView)view("replyCancel")).getDrawable() != null && ((ImageView)view("replySend")).getDrawable() != null;
            });
            if (ready[0]) break;
            SystemClock.sleep(50);
        }
        checks++;
        report.println(phase + ": " + (ready[0] ? "PASS" : "FAIL"));
        runOnMainSync(() -> {
            for (String id : new String[]{"main", "inputLayout", "reply_header", "replyHeaderRow", "replyCancel", "replySend", "replyDragHandle", "title", "publish"})
                report.println("  " + id + " " + describe(view(id)));
        });
        report.flush();
        if (!ready[0]) { failures++; capture("header-failure-" + checks); if (stopOnFailure) throw new AssertionError(phase); }
    }
    @Override public void onStart() {
        Bundle result = new Bundle();
        try {
            report = new PrintWriter(new File(getTargetContext().getExternalFilesDir(null), "reply-header-stress.txt"));
            for (int i = 0; i < repeats; i++) {
                Intent intent = new Intent().setClassName(getTargetContext().getPackageName(), "com.example.c001apk.compose.ui.feed.reply.ReplyActivity")
                    .putExtra("type", i % 2 == 0 ? "createFeed" : "feed")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                if (i % 2 != 0) intent.putExtra("username", "回复目标测试").putExtra("rid", "0");
                current = startActivitySync(intent);
                SystemClock.sleep(800);
                check(i + "-open");
                runOnMainSync(() -> ((EditText)view("editText")).setText("不会发送的测试草稿"));
                SystemClock.sleep(250);
                check(i + "-typing");
                runOnMainSync(() -> ((InputMethodManager)current.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view("editText").getWindowToken(), 0));
                SystemClock.sleep(500);
                check(i + "-hide-ime");
                runOnMainSync(() -> { view("editText").requestFocus(); ((InputMethodManager)current.getSystemService(Activity.INPUT_METHOD_SERVICE)).showSoftInput(view("editText"), 0); });
                SystemClock.sleep(500);
                check(i + "-show-ime");
                runOnMainSync(() -> current.recreate());
                SystemClock.sleep(1000);
                check(i + "-recreate");
                if (i == 0) {
                    runOnMainSync(() -> current.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
                    SystemClock.sleep(1000); check(i + "-landscape");
                    runOnMainSync(() -> current.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
                    SystemClock.sleep(1000); check(i + "-portrait");
                }
                runOnMainSync(() -> current.finish());
                SystemClock.sleep(1000);
            }
            result.putString("stream", "Header checks=" + checks + " failures=" + failures + "\n");
        } catch (Throwable error) {
            if (report != null) { error.printStackTrace(report); report.flush(); }
            result.putString("stream", android.util.Log.getStackTraceString(error));
            failures++;
        } finally {
            if (report != null) report.close();
        }
        finish(failures == 0 ? Activity.RESULT_OK : Activity.RESULT_CANCELED, result);
    }
}
