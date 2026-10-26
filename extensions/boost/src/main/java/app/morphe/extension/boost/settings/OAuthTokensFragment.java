/*
 * Copyright 2026 wchill.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.extension.boost.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.preference.Preference;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import app.morphe.extension.boost.oauth.OAuthTokenAccounts;
import app.morphe.extension.boost.oauth.OAuthTokenException;
import app.morphe.extension.boost.oauth.OAuthTokenPayload;
import app.morphe.extension.boost.oauth.OAuthTokenValidator;

public final class OAuthTokensFragment extends MorpheSettingsFragment {
    private static final String TAG = "MorpheOAuthTokens";
    private static final String IMPORT = "morphe_boost_oauth_import";
    private static final String EXPORT = "morphe_boost_oauth_export";
    private static final String STATUS = "morphe_boost_oauth_account";
    private static final String CLIP_ID = "morphe_oauth_export_id";
    private static final int IMPORT_FILE = 7401;
    private static final int EXPORT_FILE = 7402;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static String clipboardId;
    private static long clipboardDeadline;

    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private volatile boolean destroyed;
    private boolean busy;
    private String exportUsername;
    private String exportClientId;
    private AlertDialog visibleDialog;

    @Override
    protected String resourceName() {
        return "morphe_boost_oauth_tokens";
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        if (savedInstanceState != null) {
            exportUsername = savedInstanceState.getString("oauth_export_username");
            exportClientId = savedInstanceState.getString("oauth_export_client_id");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!busy) updateState();
        clearExpiredClipboard(requireContext());
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("oauth_export_username", exportUsername);
        state.putString("oauth_export_client_id", exportClientId);
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
        if (IMPORT.equals(preference.getKey())) {
            chooseImport();
            return true;
        }
        if (EXPORT.equals(preference.getKey())) {
            chooseExport();
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void updateState() {
        String username = OAuthTokenAccounts.currentUsername();
        findPreference(STATUS).setSummary(busy ? "Transferring tokens..." :
                username == null ? "Not signed in" : "u/" + username);
        findPreference(IMPORT).setEnabled(!busy);
        findPreference(EXPORT).setEnabled(!busy && username != null);
    }

    private AlertDialog show(AlertDialog.Builder builder) {
        visibleDialog = builder.create();
        if (visibleDialog.getWindow() != null) {
            visibleDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        visibleDialog.show();
        return visibleDialog;
    }

    private void error(String message) {
        if (destroyed || !isAdded()) return;
        show(new AlertDialog.Builder(requireContext())
                .setTitle("Token transfer failed")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null));
    }

    private void chooseImport() {
        show(new AlertDialog.Builder(requireContext())
                .setTitle("Import OAuth tokens")
                .setItems(new String[]{"Paste token JSON", "Choose JSON file"}, (dialog, which) -> {
                    if (which == 0) pasteImport();
                    else openFilePicker();
                })
                .setNegativeButton(android.R.string.cancel, null));
    }

    private void pasteImport() {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setMinLines(4);
        input.setMaxLines(10);
        input.setSaveEnabled(false);
        input.setImeOptions(EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING);
        if (Build.VERSION.SDK_INT >= 26) {
            input.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }
        AlertDialog dialog = show(new AlertDialog.Builder(requireContext())
                .setTitle("Import OAuth tokens")
                .setMessage("Paste JSON containing access_token and refresh_token. Import checks "
                        + "the account with Reddit, refreshes its tokens and restarts Boost. "
                        + "Only import credentials you trust.")
                .setView(input)
                .setPositiveButton("Import and restart", null)
                .setNegativeButton(android.R.string.cancel, null));
        dialog.setOnDismissListener(dismissed -> input.setText(""));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            try {
                OAuthTokenPayload payload = OAuthTokenPayload.parse(input.getText().toString());
                Intent home = homeIntent();
                dialog.dismiss();
                runTask(() -> importTokens(payload), username -> restart(home, username));
            } catch (OAuthTokenException exception) {
                input.setError(exception.getMessage());
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("*/*")
                .putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/json", "text/plain"})
                .putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        try {
            startActivityForResult(intent, IMPORT_FILE);
        } catch (ActivityNotFoundException exception) {
            error("No document picker is available on this device.");
        }
    }

    private void chooseExport() {
        final String username = OAuthTokenAccounts.currentUsername();
        if (username == null) {
            error("Select a signed-in Reddit account in Boost before exporting.");
            return;
        }
        try {
            final String clientId = OAuthTokenAccounts.clientId();
            show(new AlertDialog.Builder(requireContext())
                    .setTitle("Export u/" + username)
                    .setMessage("The JSON contains private access and refresh tokens. Anyone with "
                            + "them can use your Reddit account. Files are not encrypted. "
                            + "Clipboard copies are marked sensitive; Boost attempts to clear its "
                            + "copy after one minute. Keep exports private and delete them after use.")
                    .setPositiveButton("Copy JSON", (dialog, which) ->
                            runTask(() -> OAuthTokenAccounts.export(username, clientId).toJson(),
                                    this::copyTokens))
                    .setNeutralButton("Save JSON file", (dialog, which) -> {
                        exportUsername = username;
                        exportClientId = clientId;
                        try {
                            startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT)
                                    .addCategory(Intent.CATEGORY_OPENABLE)
                                    .setType("application/json")
                                    .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                                    .putExtra(Intent.EXTRA_TITLE, "boost-oauth-" + username + ".json"),
                                    EXPORT_FILE);
                        } catch (ActivityNotFoundException exception) {
                            exportUsername = null;
                            exportClientId = null;
                            error("No document picker is available on this device.");
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null));
        } catch (OAuthTokenException exception) {
            error(exception.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != IMPORT_FILE && requestCode != EXPORT_FILE) return;
        String username = exportUsername;
        String clientId = exportClientId;
        if (requestCode == EXPORT_FILE) {
            exportUsername = null;
            exportClientId = null;
        }
        if (resultCode != Activity.RESULT_OK) return;
        Uri uri = data == null ? null : data.getData();
        if (uri == null || !"content".equals(uri.getScheme())) {
            error("The document picker did not return a readable document.");
            return;
        }
        Context context = requireContext().getApplicationContext();
        if (requestCode == IMPORT_FILE) {
            show(new AlertDialog.Builder(requireContext())
                    .setTitle("Import OAuth tokens")
                    .setMessage("This checks the tokens with Reddit and adds or updates the account "
                            + "they identify. Boost will restart with that account selected.")
                    .setPositiveButton("Import and restart", (dialog, which) -> {
                        final Intent home;
                        try {
                            home = homeIntent();
                        } catch (OAuthTokenException exception) {
                            error(exception.getMessage());
                            return;
                        }
                        runTask(() -> {
                            final OAuthTokenPayload payload;
                            try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
                                if (stream == null) throw new IOException("Document unavailable.");
                                try (InputStreamReader reader =
                                             new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                                    payload = OAuthTokenPayload.parse(OAuthTokenPayload.read(reader));
                                }
                            }
                            return importTokens(payload);
                        }, importedUsername -> restart(home, importedUsername));
                    })
                    .setNegativeButton(android.R.string.cancel, null));
        } else if (username == null || clientId == null) {
            error("The export request expired. Start the export again.");
        } else {
            runTask(() -> {
                String json = OAuthTokenAccounts.export(username, clientId).toJson();
                try (OutputStream stream = context.getContentResolver().openOutputStream(uri, "wt")) {
                    if (stream == null) throw new IOException("Document unavailable.");
                    try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
                        writer.write(json);
                    }
                }
                return true;
            }, ignored -> Toast.makeText(requireContext(), "Private token file exported.", Toast.LENGTH_LONG).show());
        }
    }

    private String importTokens(OAuthTokenPayload payload) throws IOException, OAuthTokenException {
        String clientId = OAuthTokenAccounts.clientId();
        OAuthTokenValidator.VerifiedAccount verified =
                new OAuthTokenValidator().validate(payload, clientId, OAuthTokenAccounts.userAgent());
        OAuthTokenAccounts.save(verified);
        return verified.tokens.username;
    }

    private Intent homeIntent() throws OAuthTokenException {
        Intent intent = requireContext().getPackageManager()
                .getLaunchIntentForPackage(requireContext().getPackageName());
        if (intent == null) throw new OAuthTokenException("Boost's launcher activity could not be found.");
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    private void restart(Intent home, String username) {
        try {
            OAuthTokenAccounts.activate(username);
            startActivity(home);
        } catch (OAuthTokenException exception) {
            error(exception.getMessage());
        } catch (SQLiteException | ClassCastException | ActivityNotFoundException
                 | IllegalArgumentException | IllegalStateException exception) {
            error("The tokens were saved, but Boost could not reopen the imported account. "
                    + "Restart Boost and select that account in the account switcher.");
        }
    }

    private interface Task<T> {
        T run() throws IOException, OAuthTokenException;
    }

    private <T> void runTask(Task<T> task, Consumer<T> success) {
        busy = true;
        updateState();
        worker.execute(() -> {
            try {
                T result = task.run();
                MAIN.post(() -> {
                    if (destroyed || !isAdded()) return;
                    busy = false;
                    updateState();
                    success.accept(result);
                });
            } catch (OAuthTokenException exception) {
                failed(exception.getMessage());
            } catch (IOException exception) {
                if (!destroyed) failed("Could not transfer tokens. Check the file's access permissions "
                        + "and your connection to Reddit, then retry.");
            } catch (SQLiteException exception) {
                failed("Boost could not save the account. Check that the device has free storage.");
            } catch (SecurityException exception) {
                failed("Android denied access to the selected document or clipboard.");
            } catch (IllegalArgumentException | IllegalStateException exception) {
                failed("Boost's API settings or account storage are invalid. Check the native API "
                        + "settings before retrying.");
            }
        });
    }

    private void failed(String message) {
        Log.w(TAG, "OAuth token transfer failed.");
        MAIN.post(() -> {
            if (destroyed || !isAdded()) return;
            busy = false;
            updateState();
            error(message);
        });
    }

    private void copyTokens(String json) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            error("The system clipboard is unavailable.");
            return;
        }
        String id = UUID.randomUUID().toString();
        ClipData clip = ClipData.newPlainText("Private Boost OAuth tokens", json);
        PersistableBundle extras = new PersistableBundle();
        extras.putBoolean("android.content.extra.IS_SENSITIVE", true);
        extras.putString(CLIP_ID, id);
        clip.getDescription().setExtras(extras);
        try {
            clipboard.setPrimaryClip(clip);
            clipboardId = id;
            clipboardDeadline = SystemClock.elapsedRealtime() + 60000;
            Context context = requireContext().getApplicationContext();
            MAIN.postDelayed(() -> clearExpiredClipboard(context), 60000);
            Toast.makeText(requireContext(), "Private tokens copied. Paste them only into a trusted app.",
                    Toast.LENGTH_LONG).show();
        } catch (SecurityException exception) {
            error("Android denied access to the system clipboard.");
        }
    }

    private static void clearExpiredClipboard(Context context) {
        if (clipboardId == null || SystemClock.elapsedRealtime() < clipboardDeadline) return;
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) return;
        try {
            ClipDescription description = clipboard.getPrimaryClipDescription();
            // Background clipboard reads can be denied. Retry when this screen resumes.
            if (description == null) return;
            PersistableBundle extras = description.getExtras();
            if (extras != null && clipboardId.equals(extras.getString(CLIP_ID))) {
                if (Build.VERSION.SDK_INT >= 28) clipboard.clearPrimaryClip();
                else clipboard.setPrimaryClip(ClipData.newPlainText("", ""));
            }
            clipboardId = null;
        } catch (SecurityException exception) {
            Log.w(TAG, "Android blocked expiry of the private token clipboard copy.");
        }
    }
}
