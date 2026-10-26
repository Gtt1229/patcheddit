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
import android.os.Bundle;
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
import app.morphe.extension.boost.debug.FatalCrashLogs;

public final class ExceptionLogFragment extends MorpheSettingsFragment {
    private static final String TAG = "MorpheExceptionLog";
    private static final String VIEW = "morphe_boost_exception_log_view";
    private static final String SAVE = "morphe_boost_exception_log_save";
    private static final String CLEAR = "morphe_boost_exception_log_clear";
    private static final String CRASH_VIEW = "morphe_boost_crash_log_view";
    private static final String CRASH_BROWSE = "morphe_boost_crash_log_browse";
    private static final int SAVE_FILE = 7403;
    private static final int OPEN_CRASH_FILE = 7404;
    private static final int SAVE_CRASH_FILE = 7405;
    private static final int MAX_VIEW_CHARACTERS = 64 * 1024;

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private volatile boolean destroyed;
    private AlertDialog visibleDialog;
    private String crashExportSource;

    @Override
    protected String resourceName() {
        return "morphe_boost_exception_log";
    }

    @Override
    public void onCreatePreferences(Bundle state, String rootKey) {
        super.onCreatePreferences(state, rootKey);
        if (state != null) crashExportSource = state.getString("crash_export_source");
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("crash_export_source", crashExportSource);
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
        if (CRASH_VIEW.equals(preference.getKey())) {
            runTask(() -> FatalCrashLogs.list(context), this::chooseCrashLog);
            return true;
        }
        if (CRASH_BROWSE.equals(preference.getKey())) {
            browseCrashLogs();
            return true;
        }
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
                    .setTitle("Clear non-fatal log?")
                    .setMessage("This deletes the private non-fatal log. Fatal crash reports "
                            + "and copies you previously exported are not deleted.")
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
        showLog("Non-fatal exception log", text, false, null);
    }

    private void showLog(String title, String text, boolean truncated, Uri crashSource) {
        if (text.isEmpty()) {
            message(title, "The log is empty. No entries have been recorded yet.");
            return;
        }
        if (text.length() > MAX_VIEW_CHARACTERS) {
            text = text.substring(text.length() - MAX_VIEW_CHARACTERS);
            truncated = true;
        }
        if (truncated) {
            text = "[Showing the last 64 Ki characters. Save a copy to view the complete log.]\n\n" + text;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(scroll)
                .setPositiveButton(android.R.string.ok, null);
        if (crashSource != null) {
            builder.setNeutralButton("Save copy", (dialog, which) -> saveCrashLog(crashSource));
        }
        show(builder);
    }

    private void chooseCrashLog(FatalCrashLogs.Listing listing) {
        if (listing.incomplete) {
            show(new AlertDialog.Builder(requireContext())
                    .setTitle("Some crash reports are unavailable")
                    .setMessage("Android could not list every report location. You can still "
                            + "view accessible reports or browse Documents/Morphe manually.")
                    .setPositiveButton("Continue", (dialog, which) -> displayCrashLogs(listing))
                    .setNeutralButton("Browse files", (dialog, which) -> browseCrashLogs())
                    .setNegativeButton(android.R.string.cancel, null));
        } else {
            displayCrashLogs(listing);
        }
    }

    private void displayCrashLogs(FatalCrashLogs.Listing listing) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle("Fatal crash logs")
                .setNeutralButton("Browse files", (dialog, which) -> browseCrashLogs())
                .setNegativeButton(android.R.string.cancel, null);
        if (listing.entries.isEmpty()) {
            builder.setMessage("No accessible fatal reports were found for this app. "
                    + "You can browse for saved reports in Documents/Morphe.");
        } else {
            String[] names = new String[listing.entries.size()];
            for (int i = 0; i < names.length; i++) names[i] = listing.entries.get(i).name;
            builder.setItems(names, (dialog, which) -> viewCrashLog(listing.entries.get(which).uri));
        }
        show(builder);
    }

    private void viewCrashLog(Uri uri) {
        Context context = requireContext().getApplicationContext();
        runTask(() -> FatalCrashLogs.preview(context, uri, MAX_VIEW_CHARACTERS),
                preview -> showLog("Fatal crash log", preview.text, preview.truncated, uri));
    }

    private void browseCrashLogs() {
        try {
            startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_LOCAL_ONLY, true), OPEN_CRASH_FILE);
        } catch (ActivityNotFoundException failure) {
            message("Fatal crash logs", "No document picker is available on this device.");
        }
    }

    private void saveCrashLog(Uri source) {
        crashExportSource = source.toString();
        try {
            startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                    .putExtra(Intent.EXTRA_TITLE, "boost-fatal-crash-copy.txt"), SAVE_CRASH_FILE);
        } catch (ActivityNotFoundException failure) {
            crashExportSource = null;
            message("Fatal crash log", "No document picker is available on this device.");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != SAVE_FILE && requestCode != OPEN_CRASH_FILE && requestCode != SAVE_CRASH_FILE) return;
        String crashSource = crashExportSource;
        if (requestCode == SAVE_CRASH_FILE) crashExportSource = null;
        if (resultCode != Activity.RESULT_OK) return;
        Uri uri = data == null ? null : data.getData();
        if (uri == null || !"content".equals(uri.getScheme())) {
            message("Exception log", "The document picker did not return a valid document.");
            return;
        }
        if (requestCode == OPEN_CRASH_FILE) {
            viewCrashLog(uri);
            return;
        }
        if (requestCode == SAVE_CRASH_FILE && crashSource == null) {
            message("Fatal crash log", "The export request expired. Select the crash report again.");
            return;
        }
        if (requestCode == SAVE_CRASH_FILE && uri.equals(Uri.parse(crashSource))) {
            message("Fatal crash log", "Select a different destination to preserve the original report.");
            return;
        }
        Context context = requireContext().getApplicationContext();
        runTask(() -> {
            String text = requestCode == SAVE_FILE ? ExceptionHook.getLogStore(context).read() : null;
            try (OutputStream stream = context.getContentResolver().openOutputStream(uri, "wt")) {
                if (stream == null) throw new IOException("The selected document is unavailable.");
                if (requestCode == SAVE_CRASH_FILE) {
                    FatalCrashLogs.copy(context, Uri.parse(crashSource), stream);
                } else {
                    try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
                        writer.write(text);
                    }
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
        findPreference(CRASH_VIEW).setEnabled(!busy);
        findPreference(CRASH_BROWSE).setEnabled(!busy);
    }
}
