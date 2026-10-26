/*
 * Copyright 2026 wchill.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.extension.boost.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.preference.Preference;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import app.morphe.extension.boost.debug.ExceptionHook;

public final class ExceptionLogFragment extends MorpheSettingsFragment {
    private static final String TAG = "MorpheExceptionLog";
    private static final String VIEW = "morphe_boost_exception_log_view";
    private static final String SAVE = "morphe_boost_exception_log_save";
    private static final String CLEAR = "morphe_boost_exception_log_clear";
    private static final int SAVE_FILE = 7403;
    private static final int MAX_VIEW_CHARACTERS = 64 * 1024;

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private volatile boolean destroyed;
    private AlertDialog visibleDialog;

    @Override
    protected String resourceName() {
        return "morphe_boost_exception_log";
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        worker.shutdownNow();
        if (visibleDialog != null) visibleDialog.dismiss();
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        Context context = requireContext().getApplicationContext();
        if (VIEW.equals(preference.getKey())) {
            runTask(() -> ExceptionHook.getLogStore(context).read(), this::viewLog);
            return true;
        }
        if (SAVE.equals(preference.getKey())) {
            try {
                startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT)
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                        .putExtra(Intent.EXTRA_TITLE, "boost-exceptions.log"), SAVE_FILE);
            } catch (ActivityNotFoundException failure) {
                message("Exception log", "No document picker is available on this device.");
            }
            return true;
        }
        if (CLEAR.equals(preference.getKey())) {
            show(new AlertDialog.Builder(requireContext())
                    .setTitle("Clear exception log?")
                    .setMessage("This deletes all saved exception entries from Boost. "
                            + "Copies you previously exported are not deleted.")
                    .setPositiveButton("Clear", (dialog, which) -> runTask(() -> {
                        ExceptionHook.getLogStore(context).clear();
                        return true;
                    }, ignored -> message("Exception log", "The saved exception log was cleared.")))
                    .setNegativeButton(android.R.string.cancel, null));
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void viewLog(String text) {
        if (text.isEmpty()) {
            message("Exception log", "No exceptions have been recorded yet.");
            return;
        }
        if (text.length() > MAX_VIEW_CHARACTERS) {
            text = "[Showing the last 64 Ki characters. Save the log to view all retained entries.]\n\n"
                    + text.substring(text.length() - MAX_VIEW_CHARACTERS);
        }
        TextView content = new TextView(requireContext());
        content.setText(text);
        content.setTypeface(Typeface.MONOSPACE);
        content.setTextSize(12);
        content.setTextIsSelectable(true);
        content.setSaveEnabled(false);
        int padding = Math.round(16 * getResources().getDisplayMetrics().density);
        content.setPadding(padding, padding, padding, padding);
        ScrollView scroll = new ScrollView(requireContext());
        scroll.addView(content);
        show(new AlertDialog.Builder(requireContext())
                .setTitle("Exception log")
                .setView(scroll)
                .setPositiveButton(android.R.string.ok, null));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != SAVE_FILE || resultCode != Activity.RESULT_OK) return;
        Uri uri = data == null ? null : data.getData();
        if (uri == null || !"content".equals(uri.getScheme())) {
            message("Exception log", "The document picker did not return a writable document.");
            return;
        }
        Context context = requireContext().getApplicationContext();
        runTask(() -> {
            String text = ExceptionHook.getLogStore(context).read();
            try (OutputStream stream = context.getContentResolver().openOutputStream(uri, "wt")) {
                if (stream == null) throw new IOException("The selected document is unavailable.");
                try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
                    writer.write(text);
                }
            }
            return true;
        }, ignored -> message("Exception log", "A copy of the log was saved. Review it before sharing."));
    }

    private void show(AlertDialog.Builder builder) {
        visibleDialog = builder.create();
        if (visibleDialog.getWindow() != null) {
            visibleDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        visibleDialog.show();
    }

    private void message(String title, String text) {
        if (destroyed || !isAdded()) return;
        show(new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(android.R.string.ok, null));
    }

    private interface Task<T> {
        T run() throws IOException;
    }

    private <T> void runTask(Task<T> task, Consumer<T> success) {
        setBusy(true);
        worker.execute(() -> {
            try {
                T result = task.run();
                main.post(() -> {
                    if (destroyed || !isAdded()) return;
                    setBusy(false);
                    success.accept(result);
                });
            } catch (IOException | SecurityException failure) {
                Log.e(TAG, "Could not access the exception log or selected document.", failure);
                main.post(() -> {
                    if (destroyed || !isAdded()) return;
                    setBusy(false);
                    message("Exception log unavailable",
                            "Could not read, save or clear the log. Check free storage and document access, then retry.");
                });
            }
        });
    }

    private void setBusy(boolean busy) {
        findPreference(VIEW).setEnabled(!busy);
        findPreference(SAVE).setEnabled(!busy);
        findPreference(CLEAR).setEnabled(!busy);
    }
}
