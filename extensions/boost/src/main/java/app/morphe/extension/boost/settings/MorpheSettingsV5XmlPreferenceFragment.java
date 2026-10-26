/*
 * Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.extension.boost.settings;

import app.morphe.extension.boost.api.BoostFieldNames;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import app.morphe.extension.boost.utils.BoostSystemBarInsetsFix;

import com.rubenmayayo.reddit.models.reddit.SubscriptionViewModel;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * V5-owned XML preference renderer.
 *
 * <p>Boost XML is used only as canonical metadata. This Fragment owns every
 * visible row and writes the canonical preference keys directly.</p>
 */
abstract class MorpheSettingsV5XmlPreferenceFragment extends Fragment {
    static final String CONTRACT_MARKER =
            "MORPHE_BOOST_SETTINGS_V5_XML_PREFERENCE_ENGINE_ISSUE121_V1";
    static final String NO_LEGACY_ROUTE_MARKER =
            "MORPHE_BOOST_SETTINGS_V5_XML_ENGINE_NO_LEGACY_ROUTE_ISSUE121_V1";
    static final String HIDDEN_CANONICAL_OVERRIDE_MARKER =
            "MORPHE_BOOST_SETTINGS_V5_HIDDEN_CANONICAL_OVERRIDE_ISSUE121_V1";
    static final String DATA_APP_SPECIAL_HANDLERS_MARKER =
            "MORPHE_BOOST_SETTINGS_V5_DATA_APP_SPECIAL_HANDLERS_ISSUE121_V1";
    private static final String EDITOR_BACK_STACK_MARKER =
            "morphe_v5_native_editor_back_stack";
    private static final String ANDROID_NS =
            "http://schemas.android.com/apk/res/android";
    private static final String APP_NS =
            "http://schemas.android.com/apk/res-auto";

    private String pageTitle = "";
    private final String[] resourceNames;
    private final List<Binding> bindings = new ArrayList<>();
    private final Map<String, Control> controlsByKey = new LinkedHashMap<>();

    private MorpheSettingsV4Theme.Tokens tokens;
    private SharedPreferences preferences;
    private BoostSettingsNative.SearchObservation savedSearchObserver;
    private FrameLayout pageHost;
    private View settingsPage;
    private final List<EditorPage> editorPages = new ArrayList<>();
    private BoostSettingsNative.EditorBackStack editorBackStack;
    private int editorBackStackBaseCount = -1;
    private int editorBackStackSequence;

    protected MorpheSettingsV5XmlPreferenceFragment(
            String... resourceNames
    ) {
        this.resourceNames = resourceNames == null
                ? new String[0]
                : resourceNames.clone();
    }

    protected void preparePage() {
    }

    protected final void setPageTitle(String value) {
        pageTitle = value == null ? "" : value;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        preparePage();
        if (TextUtils.isEmpty(pageTitle)) {
            throw new IllegalStateException("V5 page title is unresolved");
        }
        Context context = inflater.getContext();
        tokens = MorpheSettingsV4Theme.resolve(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        setHasOptionsMenu(true);

        pageHost = new FrameLayout(context);
        pageHost.setBackgroundColor(tokens.background);

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.setClipToPadding(false);
        scrollView.setBackgroundColor(tokens.background);
        settingsPage = scrollView;
        pageHost.addView(scrollView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(4), dp(16), dp(32));
        scrollView.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        List<Control> controls = readControls(context);
        if (controls.isEmpty()) {
            addSectionLabel(content, pageTitle);
            addSpace(content, 8);
            LinearLayout group = addGroup(content);
            addActionRow(
                    group,
                    "This settings page is unavailable",
                    "Boost's packaged preference contract could not be read.",
                    view -> showUnavailable()
            );
            return pageHost;
        }

        String intro = pageIntro();
        if (!TextUtils.isEmpty(intro)) {
            content.addView(MorpheSettingsV14Ui.pageIntro(
                    requireContext(),
                    tokens,
                    intro
            ));
            addSpace(content, 14);
        }

        renderControls(content, controls);
        updateDependencies();
        return pageHost;
    }

    @Override
    public void onResume() {
        super.onResume();
        syncRows();
        styleHost();
    }

    @Override
    public void onPause() {
        Activity activity = hostActivity();
        if (activity != null) {
            BoostSystemBarInsetsFix.clearMorpheSettingsV4SystemBars(activity);
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        bindings.clear();
        controlsByKey.clear();
        if (savedSearchObserver != null) {
            savedSearchObserver.close();
            savedSearchObserver = null;
        }
        if (editorBackStack != null) {
            editorBackStack.close();
            editorBackStack = null;
        }
        editorPages.clear();
        pageHost = null;
        settingsPage = null;
        editorBackStackBaseCount = -1;
        super.onDestroyView();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MorpheSettingsV5Search.prepareMenu(this, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (MorpheSettingsV5Search.handleMenuItem(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected boolean includeControl(String resourceName, String key) {
        return true;
    }

    protected boolean includeHiddenControl(
            String resourceName,
            String key
    ) {
        return false;
    }

    protected String sectionFor(
            String resourceName,
            String key,
            String currentSection
    ) {
        return currentSection;
    }

    protected String pageIntro() {
        return "";
    }

    protected boolean useAndroidSettingsGuidanceRows() {
        return true;
    }

    protected String displaySummary(
            String key,
            String currentSummary
    ) {
        return currentSummary;
    }

    protected String displayActionSummary(
            String key,
            String currentSummary,
            String tag
    ) {
        return currentSummary;
    }

    private List<Control> readControls(Context context) {
        List<Control> result = new ArrayList<>();
        java.util.Set<String> seenKeys = new java.util.LinkedHashSet<>();
        for (String resourceName : resourceNames) {
            for (Control control : readControls(context, resourceName)) {
                if (TextUtils.isEmpty(control.key) || seenKeys.add(control.key)) {
                    result.add(control);
                }
            }
        }
        return result;
    }

    private List<Control> readControls(
            Context context,
            String resourceName
    ) {
        int resourceId = MorpheSettingsV4Catalog.resourceId(
                context,
                "xml",
                resourceName
        );
        if (resourceId == 0) {
            return Collections.emptyList();
        }

        Resources resources = context.getResources();
        List<Control> result = new ArrayList<>();
        XmlResourceParser parser = null;
        String section = pageTitle;
        int categoryDepth = -1;
        int hiddenDepth = -1;
        try {
            parser = resources.getXml(resourceId);
            int event;
            while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
                int depth = parser.getDepth();
                if (event == XmlPullParser.END_TAG) {
                    if (depth == hiddenDepth) {
                        hiddenDepth = -1;
                    }
                    if (depth == categoryDepth) {
                        section = pageTitle;
                        categoryDepth = -1;
                    }
                    continue;
                }
                if (event != XmlPullParser.START_TAG) {
                    continue;
                }
                if (hiddenDepth >= 0 && depth > hiddenDepth) {
                    continue;
                }

                String rawTag = parser.getName();
                String tag = simpleTag(rawTag);
                if ("PreferenceScreen".equals(tag)
                        || "intent".equalsIgnoreCase(tag)) {
                    continue;
                }
                boolean visible = parser.getAttributeBooleanValue(
                        APP_NS,
                        "isPreferenceVisible",
                        true
                );
                String rawKey = attributeText(resources, parser, "key");
                boolean forcedVisible = !visible
                        && includeHiddenControl(resourceName, rawKey);
                if ("PreferenceCategory".equals(tag)) {
                    if (!visible) {
                        hiddenDepth = depth;
                        continue;
                    }
                    String categoryTitle = attributeText(
                            resources,
                            parser,
                            "title"
                    );
                    section = TextUtils.isEmpty(categoryTitle)
                            ? pageTitle
                            : categoryTitle;
                    categoryDepth = depth;
                    continue;
                }
                if (!visible && !forcedVisible) {
                    continue;
                }

                Control control = new Control();
                control.tag = tag;
                control.section = section;
                control.key = rawKey;
                control.title = attributeText(resources, parser, "title");
                control.summary = attributeText(resources, parser, "summary");
                control.summaryOn = attributeText(resources, parser, "summaryOn");
                control.summaryOff = attributeText(resources, parser, "summaryOff");
                control.defaultValue = attributeText(
                        resources,
                        parser,
                        "defaultValue"
                );
                control.dependency = attributeText(
                        resources,
                        parser,
                        "dependency"
                );
                control.fragmentName = parser.getAttributeValue(
                        ANDROID_NS,
                        "fragment"
                );
                control.disableDependentsState =
                        parser.getAttributeBooleanValue(
                                ANDROID_NS,
                                "disableDependentsState",
                                false
                        );
                control.min = parser.getAttributeIntValue(
                        ANDROID_NS,
                        "min",
                        0
                );
                control.max = parser.getAttributeIntValue(
                        ANDROID_NS,
                        "max",
                        100
                );
                control.entries = attributeArray(
                        resources,
                        parser,
                        "entries"
                );
                control.entryValues = attributeArray(
                        resources,
                        parser,
                        "entryValues"
                );
                configureMorpheControl(control);
                if (!includeControl(resourceName, control.key)) {
                    continue;
                }
                control.section = sectionFor(
                        resourceName,
                        control.key,
                        control.section
                );
                if ("pref_general_v2".equals(resourceName)
                        && !TextUtils.isEmpty(control.fragmentName)) {
                    // These six routes duplicate the dedicated Morphe hubs.
                    // Classic Boost settings remains available separately.
                    continue;
                }
                if (!TextUtils.isEmpty(control.title)) {
                    result.add(control);
                    if (!TextUtils.isEmpty(control.key)) {
                        controlsByKey.put(control.key, control);
                    }
                }
            }
        } catch (Exception ignored) {
            return Collections.emptyList();
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
        return result;
    }

    private void renderControls(
            LinearLayout content,
            List<Control> controls
    ) {
        String currentSection = null;
        LinearLayout group = null;
        for (Control control : controls) {
            if (!TextUtils.equals(currentSection, control.section)) {
                boolean repeatedPageTitle = TextUtils.equals(
                        control.section,
                        pageTitle
                );
                if (group != null) {
                    addSpace(content, 20);
                }
                currentSection = control.section;
                if (!repeatedPageTitle) {
                    addSectionLabel(content, currentSection);
                    addSpace(content, 6);
                }
                group = addGroup(content);
            }
            if (isToggle(control)) {
                addToggle(group, control);
            } else if (isChoice(control)) {
                addChoice(group, control);
            } else if ("SeekBarPreference".equals(control.tag)) {
                addSeek(group, control);
            } else {
                addAction(group, control);
            }
        }
    }

    private void addToggle(LinearLayout group, Control control) {
        boolean checked = preferences.getBoolean(
                control.key,
                booleanDefault(control)
        );
        String summaryValue = toggleSummary(control, checked);
        LinearLayout row = baseRow(!TextUtils.isEmpty(summaryValue));
        TextView summary = addLabels(
                row,
                control.title,
                summaryValue
        );

        MorpheSettingsV14Ui.Toggle toggle =
                new MorpheSettingsV14Ui.Toggle(
                        requireContext(),
                        tokens,
                        checked
                );
        toggle.setOnCheckedChangeListener((button, value) -> {
            preferences.edit().putBoolean(control.key, value).apply();
            applySideEffects(control.key);
            summary.setText(toggleSummary(control, value));
            summary.setVisibility(
                    TextUtils.isEmpty(summary.getText())
                            ? View.GONE
                            : View.VISIBLE
            );
            updateDependencies();
        });
        row.setOnClickListener(view -> {
            if (row.isEnabled()) {
                toggle.toggle();
            }
        });
        row.addView(toggle, wrapParams());
        addGroupedRow(group, row);
        bindings.add(new Binding(control, row, summary, toggle, null));
    }

    private void addChoice(LinearLayout group, Control control) {
        String summaryValue = selectedTitle(control);
        LinearLayout row = baseRow(!TextUtils.isEmpty(summaryValue));
        TextView summary = addLabels(
                row,
                control.title,
                summaryValue
        );
        addChevron(row);
        row.setOnClickListener(view -> {
            if (row.isEnabled()) {
                showChoiceDialog(control, summary);
            }
        });
        addGroupedRow(group, row);
        bindings.add(new Binding(control, row, summary, null, null));
    }

    private void addSeek(LinearLayout group, Control control) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(16), dp(10), dp(16), dp(12));
        row.setMinimumHeight(dp(82));
        row.setBackground(MorpheSettingsV4Theme.interactive(
                requireContext(),
                0x00000000,
                0,
                tokens.navigationAccent().color
        ));

        LinearLayout labels = new LinearLayout(requireContext());
        labels.setOrientation(LinearLayout.HORIZONTAL);
        labels.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = textView(control.title, 16, tokens.textPrimary);
        title.setTypeface(Typeface.create(
                "sans-serif-medium",
                Typeface.NORMAL
        ));
        labels.addView(title, labelsParams());
        int value = preferences.getInt(control.key, intDefault(control));
        TextView summary = textView(String.valueOf(value), 14, tokens.textSecondary);
        labels.addView(summary, wrapParams());
        row.addView(labels, matchWrapParams());

        SeekBar seek = new SeekBar(requireContext());
        seek.setMax(Math.max(0, control.max - control.min));
        seek.setProgress(Math.max(0, value - control.min));
        if (Build.VERSION.SDK_INT >= 21) {
            seek.setProgressTintList(ColorStateList.valueOf(
                    tokens.navigationAccent().color
            ));
            seek.setThumbTintList(ColorStateList.valueOf(
                    tokens.navigationAccent().color
            ));
        }
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(
                    SeekBar seekBar,
                    int progress,
                    boolean fromUser
            ) {
                summary.setText(String.valueOf(control.min + progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int selected = control.min + seekBar.getProgress();
                preferences.edit().putInt(control.key, selected).apply();
                applySideEffects(control.key);
            }
        });
        row.addView(seek, matchWrapParams());
        addGroupedRow(group, row);
        bindings.add(new Binding(control, row, summary, null, seek));
    }

    private void addAction(LinearLayout group, Control control) {
        String summaryValue = actionSummary(control);
        LinearLayout row = baseRow(!TextUtils.isEmpty(summaryValue));
        TextView summary = addLabels(
                row,
                control.title,
                summaryValue
        );
        addChevron(row);
        row.setOnClickListener(view -> {
            if (row.isEnabled()) {
                performAction(control);
            }
        });
        addGroupedRow(group, row);
        bindings.add(new Binding(control, row, summary, null, null));
    }

    private void addActionRow(
            LinearLayout group,
            String title,
            String summary,
            View.OnClickListener listener
    ) {
        LinearLayout row = baseRow(!TextUtils.isEmpty(summary));
        addLabels(row, title, summary);
        addChevron(row);
        row.setOnClickListener(listener);
        addGroupedRow(group, row);
    }

    private void performAction(Control control) {
        if ("FilterPreference".equals(control.tag)) {
            showFilterEditor(control);
            return;
        }
        if (control.tag.startsWith("Delete")) {
            confirmDelete(control);
            return;
        }
        if ("ColorPatternPreference".equals(control.tag)) {
            showColorPatternEditor();
            return;
        }
        String key = control.key;
        if ("pref_sort_per_sub".equals(key)) {
            showSavedSortsEditor();
        } else if ("pref_frontpage_sort".equals(key)) {
            showSortEditor(false, frontpageSubscription(), null);
        } else if ("pref_default_sort".equals(key)) {
            showSortEditor(true, emptySubscription(), null);
        } else if ("pref_saved_searches".equals(key)) {
            showSavedSearchesEditor();
        } else if ("pref_search_advanced_help".equals(key)) {
            showFieldSearchHelp();
        } else if ("pref_notifications_configure".equals(key)) {
            openNotificationSettings();
        } else if ("pref_edit_subscriptions".equals(key)) {
            runNativeAction(() -> BoostSettingsNative.openSubscriptions(hostActivity()));
        } else if ("pref_manage_drafts".equals(key)) {
            runNativeAction(() -> BoostSettingsNative.openDrafts(requireContext()));
        } else if ("pref_imgur_uploads".equals(key)) {
            runNativeAction(() -> BoostSettingsNative.openUploads(requireContext()));
        } else if ("remove_ads".equals(key)) {
            runNativeAction(() -> BoostSettingsNative.openBilling(requireContext(), false));
        } else if ("buy_pro".equals(key)) {
            runNativeAction(() -> BoostSettingsNative.openBilling(requireContext(), true));
        } else if ("pref_gdpr_revoke".equals(key)) {
            confirmGdprRevocation();
        } else if ("pref_download_folders".equals(key)) {
            runNativeAction(() -> BoostSettingsNative.openDownloadFolders(requireContext()));
        } else if ("pref_cache_current_size".equals(key)) {
            clearMediaCache();
        } else if ("reset_tips".equals(key)) {
            resetTips();
        } else if ("support_launch".equals(key)) {
            runNativeAction(() -> BoostSettingsNative.openSupport(requireContext()));
        } else if ("rate_app".equals(key)) {
            runNativeAction(() -> BoostSettingsNative.rateApp(hostActivity()));
        } else if ("about_subreddit".equals(key)) {
            runNativeAction(() -> BoostSettingsNative.openCommunity(hostActivity(), "BoostForReddit"));
        } else if ("about_faq".equals(key)) {
            runNativeAction(() -> BoostSettingsNative.openWiki(hostActivity(), "boostforreddit", "index"));
        } else if ("licenses_preference".equals(key)) {
            runNativeAction(() -> BoostSettingsNative.openLicenses(requireContext()));
        } else if ("privacy_policy".equals(key)) {
            openUrl("https://www.iubenda.com/privacy-policy/7976518");
        } else if ("about_reddit".equals(key)) {
            runNativeAction(() -> BoostSettingsNative.openUser(requireContext(), "rmayayo"));
        } else if ("about_twitter".equals(key)) {
            openUrl("https://twitter.com/rmayayo");
        } else if ("contact_dev_key".equals(key)) {
            openEmail("mayayo.dev@gmail.com");
        } else if ("about_version".equals(key)) {
            showVersion();
        } else {
            showUnavailable();
        }
    }

    private void showChoiceDialog(Control control, TextView summaryView) {
        if (control.entries.length == 0
                || control.entries.length != control.entryValues.length) {
            showUnavailable();
            return;
        }
        EditorPage page = openEditorPage(control.title);

        String selected = preferences.getString(
                control.key,
                stringDefault(control)
        );
        LinearLayout group = addGroup(page.content);
        for (int index = 0; index < control.entries.length; index++) {
            final String value = control.entryValues[index];
            boolean checked = TextUtils.equals(value, selected);
            LinearLayout row = materialChoiceRow(
                    control.entries[index],
                    "",
                    checked
            );
            row.setOnClickListener(view -> {
                preferences.edit().putString(control.key, value).apply();
                applySideEffects(control.key);
                summaryView.setText(selectedTitle(control));
                updateDependencies();
                page.dismiss();
            });
            addGroupedRow(group, row);
        }
    }

    private void showFilterEditor(Control control) {
        List<String> values = loadFilters(control.key);
        EditorPage page = openEditorPage(control.title);
        LinearLayout content = page.content;
        EditText input = addEditorInput(content, "Add filter", "", 4);

        TextView add = actionLabel("Add", true);
        content.addView(add, spacedMatchWrapParams(10));

        LinearLayout list = new LinearLayout(materialContext());
        list.setOrientation(LinearLayout.VERTICAL);
        content.addView(list, spacedMatchWrapParams(14));

        Runnable render = () -> renderFilterRows(
                list,
                values,
                control.key
        );
        render.run();
        add.setOnClickListener(view -> {
            String value = input.getText().toString().trim();
            if (!TextUtils.isEmpty(value) && !values.contains(value)) {
                values.add(0, value);
                persistFilters(control.key, values);
                input.setText("");
                render.run();
            }
        });

    }

    private void renderFilterRows(
            LinearLayout list,
            List<String> values,
            String key
    ) {
        list.removeAllViews();
        if (values.isEmpty()) {
            TextView empty = textView(
                    "No filters yet",
                    14,
                    tokens.textSecondary
            );
            empty.setPadding(dp(4), dp(12), dp(4), dp(12));
            list.addView(empty);
            return;
        }
        LinearLayout group = addGroup(list);
        for (String value : new ArrayList<>(values)) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(4), dp(5), dp(2), dp(5));
            TextView label = textView(value, 16, tokens.textPrimary);
            row.addView(label, labelsParams());
            TextView remove = actionLabel("Remove", false);
            remove.setOnClickListener(view -> {
                values.remove(value);
                persistFilters(key, values);
                renderFilterRows(list, values, key);
            });
            row.addView(remove, wrapParams());
            addGroupedRow(group, row);
        }
    }

    private List<String> loadFilters(String key) {
        return new ArrayList<>(BoostSettingsNative.loadFilters(key));
    }

    private void persistFilters(String key, List<String> values) {
        try {
            BoostSettingsNative.saveFilters(key, values);
            applySideEffects(key);
        } catch (RuntimeException exception) {
            showUnavailable();
        }
    }

    private void showColorPatternEditor() {
        Context context = requireContext();
        EditorPage page = openEditorPage("Color patterns");
        LinearLayout content = page.content;

        String[][] palettes = new String[][]{
                {"Standard", "standard"},
                {"Secondary", "secondary"},
                {"Diverging", "diverging"},
                {"Diverging translucent", "divergingTrans"},
                {"Pain", "pain"},
                {"Rainbow", "Rainbow"},
                {"Viridis", "viridis2"},
                {"Blues", "blues"},
                {"Yellow to red", "YlOrRd"},
                {"Grayscale", "gradient_gray"},
                {"Transparent", "transparent"}
        };
        LinearLayout group = addGroup(content);
        int available = 0;
        for (String[] palette : palettes) {
            int resourceId = MorpheSettingsV4Catalog.resourceId(
                    context,
                    "array",
                    palette[1]
            );
            if (resourceId == 0) {
                continue;
            }
            int[] colors;
            try {
                colors = context.getResources().getIntArray(resourceId);
            } catch (Resources.NotFoundException ignored) {
                continue;
            }
            available++;
            LinearLayout row = baseRow();
            addLabels(row, palette[0], "");
            LinearLayout preview = new LinearLayout(context);
            preview.setOrientation(LinearLayout.HORIZONTAL);
            preview.setGravity(Gravity.CENTER_VERTICAL);
            int shown = Math.min(colors.length, 7);
            for (int index = 0; index < shown; index++) {
                View swatch = new View(context);
                swatch.setBackground(MorpheSettingsV4Theme.rounded(
                        context,
                        colors[index],
                        5
                ));
                LinearLayout.LayoutParams swatchParams =
                        new LinearLayout.LayoutParams(dp(18), dp(30));
                if (index > 0) {
                    swatchParams.setMarginStart(dp(3));
                }
                preview.addView(swatch, swatchParams);
            }
            row.addView(preview, wrapParams());
            row.setOnClickListener(view -> {
                try {
                    BoostSettingsNative.setColorPattern(colors);
                } catch (RuntimeException exception) {
                    showUnavailable();
                    return;
                }
                setBoostStaticBoolean(BoostFieldNames.BoostPreferencesCommentTypographyDirty);
                page.dismiss();
            });
            addGroupedRow(group, row);
        }
        if (available == 0) {
            page.dismiss();
            showUnavailable();
            return;
        }
    }

    private SubscriptionViewModel frontpageSubscription() {
        return BoostSettingsNative.frontpageSubscription();
    }

    private SubscriptionViewModel emptySubscription() {
        return BoostSettingsNative.emptySubscription();
    }

    private void showSortEditor(
            boolean defaultSort,
            SubscriptionViewModel subscription,
            Runnable afterSelection
    ) {
        if (subscription == null) {
            showUnavailable();
            return;
        }
        try {
            List<BoostSettingsNative.SortOption> options =
                    BoostSettingsNative.sortOptions(requireContext(), subscription);
            List<Integer> optionIds = new ArrayList<>();
            List<CharSequence> optionLabels = new ArrayList<>();
            for (BoostSettingsNative.SortOption option : options) {
                int id = option.id;
                String title = option.title;
                if (TextUtils.isEmpty(title) || id < 0) {
                    continue;
                }
                optionIds.add(id);
                optionLabels.add(title);
            }
            if (optionIds.isEmpty()) {
                showUnavailable();
                return;
            }

            Integer selectedId = currentSortOptionId(
                    defaultSort,
                    subscription
            );
            EditorPage page = openEditorPage(
                    defaultSort ? "Default sort" : "Sort home by"
            );
            if (!defaultSort && Integer.valueOf(100).equals(selectedId)) {
                page.content.addView(MorpheSettingsV14Ui.supportingText(
                        requireContext(),
                        tokens,
                        "Using the default sort"
                ));
                addSpace(page.content, 10);
            }
            LinearLayout group = addGroup(page.content);
            boolean hasReset = false;
            for (int index = 0; index < optionIds.size(); index++) {
                int optionId = optionIds.get(index);
                if (!defaultSort && optionId == 100) {
                    hasReset = true;
                    continue;
                }
                final int which = index;
                MorpheSettingsV14Ui.ChoiceRow row = materialChoiceRow(
                        optionLabels.get(index).toString(),
                        "",
                        Integer.valueOf(optionId).equals(selectedId)
                );
                row.setOnClickListener(view -> {
                    int id = optionIds.get(which);
                    boolean saved = defaultSort
                            ? saveDefaultSort(id)
                            : saveSubscriptionSort(subscription, id);
                    if (!saved) {
                        showUnavailable();
                        return;
                    }
                    applySideEffects(defaultSort
                            ? "pref_default_sort"
                            : "pref_frontpage_sort");
                    page.dismiss();
                    if (afterSelection != null) {
                        afterSelection.run();
                    }
                });
                addGroupedRow(group, row);
            }
            if (hasReset) {
                addSpace(page.content, 12);
                TextView reset = actionLabel("Reset to default", false);
                boolean resetEnabled = !Integer.valueOf(100).equals(
                        selectedId
                );
                reset.setEnabled(resetEnabled);
                reset.setAlpha(resetEnabled ? 1.0f : 0.42f);
                reset.setOnClickListener(view -> {
                    if (!saveSubscriptionSort(subscription, 100)) {
                        showUnavailable();
                        return;
                    }
                    applySideEffects("pref_frontpage_sort");
                    page.dismiss();
                    if (afterSelection != null) {
                        afterSelection.run();
                    }
                });
                page.content.addView(reset, wrapParams());
            }
        } catch (RuntimeException exception) {
            showUnavailable();
        }
    }

    private int selectedSortIndex(
            boolean defaultSort,
            SubscriptionViewModel subscription,
            List<Integer> optionIds
    ) {
        Integer selectedId = currentSortOptionId(
                defaultSort,
                subscription
        );
        return selectedId == null ? -1 : optionIds.indexOf(selectedId);
    }

    private Integer currentSortOptionId(
            boolean defaultSort,
            SubscriptionViewModel subscription
    ) {
        try {
            return BoostSettingsNative.currentSortOptionId(defaultSort, subscription);
        } catch (RuntimeException exception) {
            showUnavailable();
            return null;
        }
    }

    private boolean saveDefaultSort(int id) {
        String sort = null;
        String period = null;
        if (id == 0 || id == 1 || id == 2 || id == 5) {
            sort = String.valueOf(id);
        } else if (id >= 30 && id <= 35) {
            sort = "3";
            period = String.valueOf(id - 30);
        } else if (id >= 40 && id <= 45) {
            sort = "4";
            period = String.valueOf(id - 40);
        }
        if (sort == null) {
            return false;
        }
        try {
            BoostSettingsNative.saveDefaultSort(sort, period);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private boolean saveSubscriptionSort(SubscriptionViewModel subscription, int id) {
        try {
            BoostSettingsNative.saveSubscriptionSort(subscription, id);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private void showSavedSortsEditor() {
        EditorPage page = openEditorPage("Manage saved sorts");
        LinearLayout content = page.content;
        LinearLayout list = new LinearLayout(materialContext());
        list.setOrientation(LinearLayout.VERTICAL);
        content.addView(list, matchWrapParams());

        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            list.removeAllViews();
            List<SubscriptionViewModel> subscriptions = BoostSettingsNative.savedSortSubscriptions();
            if (subscriptions.isEmpty()) {
                TextView empty = textView(
                        "No community-specific sorts yet.",
                        15,
                        tokens.textSecondary
                );
                empty.setPadding(dp(8), dp(16), dp(8), dp(16));
                list.addView(empty, matchWrapParams());
                return;
            }
            LinearLayout group = addGroup(list);
            for (SubscriptionViewModel subscription : subscriptions) {
                String name = BoostSettingsNative.subscriptionName(requireContext(), subscription);
                if (TextUtils.isEmpty(name)) {
                    name = "Community";
                }
                LinearLayout row = baseRow();
                addLabels(row, name, "Tap to change its saved sort");
                TextView remove = actionLabel("Remove", false);
                remove.setOnClickListener(view -> {
                    if (saveSubscriptionSort(subscription, 100)) {
                        render[0].run();
                    } else {
                        showUnavailable();
                    }
                });
                row.addView(remove, wrapParams());
                row.setOnClickListener(view -> showSortEditor(
                        false,
                        subscription,
                        render[0]
                ));
                addGroupedRow(group, row);
            }
        };
        render[0].run();

        LinearLayout actions = endAlignedActions();
        TextView clear = actionLabel("Clear all", false);
        clear.setOnClickListener(view -> showConfirmationPage(
                "Clear saved sorts?",
                "Community-specific sort choices will be removed.",
                "Clear",
                () -> {
                    BoostSettingsNative.clearSavedSorts();
                    render[0].run();
                }
        ));
        actions.addView(clear, wrapParams());
        content.addView(actions, matchWrapParams());
    }

    private void showSavedSearchesEditor() {
        BoostSettingsNative.SavedSearches model = savedSearchesViewModel();
        if (model == null) {
            showUnavailable();
            return;
        }
        EditorPage page = openEditorPage("Saved searches");
        LinearLayout content = page.content;

        LinearLayout list = new LinearLayout(materialContext());
        list.setOrientation(LinearLayout.VERTICAL);
        content.addView(list, matchWrapParams());

        SavedSearchRenderer renderer = values -> {
            if (!page.isShowing()) {
                return;
            }
            renderSavedSearches(list, model, values);
        };
        try {
            renderer.render(model.current());
            if (savedSearchObserver != null) {
                savedSearchObserver.close();
            }
            BoostSettingsNative.SearchObservation observer =
                    model.observe(this, renderer::render);
            savedSearchObserver = observer;
            page.onDismiss = () -> {
                observer.close();
                if (savedSearchObserver == observer) {
                    savedSearchObserver = null;
                }
            };
        } catch (RuntimeException exception) {
            page.dismiss();
            showUnavailable();
            return;
        }

        LinearLayout actions = endAlignedActions();
        TextView add = actionLabel("Add saved search", true);
        add.setOnClickListener(view -> showSavedSearchEditor(
                model,
                null
        ));
        actions.addView(add, wrapParams());
        content.addView(actions, matchWrapParams());
    }

    private BoostSettingsNative.SavedSearches savedSearchesViewModel() {
        Activity activity = hostActivity();
        if (activity == null) {
            return null;
        }
        try {
            return BoostSettingsNative.savedSearches(activity);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private void renderSavedSearches(
            LinearLayout list,
            BoostSettingsNative.SavedSearches model,
            List<BoostSettingsNative.SavedSearch> values
    ) {
        list.removeAllViews();
        if (values.isEmpty()) {
            TextView empty = textView(
                    "No saved searches yet.",
                    15,
                    tokens.textSecondary
            );
            empty.setPadding(dp(8), dp(16), dp(8), dp(16));
            list.addView(empty, matchWrapParams());
            return;
        }
        LinearLayout group = addGroup(list);
        for (BoostSettingsNative.SavedSearch entity : values) {
            String name = entity.name;
            String query = entity.query;
            if (TextUtils.isEmpty(name)) {
                name = query;
            }
            String scope = entity.community;
            String summary = TextUtils.isEmpty(scope)
                    ? query
                    : query + "  ·  r/" + scope;
            LinearLayout row = baseRow();
            addLabels(row, name, summary);
            TextView remove = actionLabel("Remove", false);
            remove.setOnClickListener(view -> deleteSavedSearch(
                    model,
                    entity
            ));
            row.addView(remove, wrapParams());
            row.setOnClickListener(view -> showSavedSearchEditor(
                    model,
                    entity
            ));
            addGroupedRow(group, row);
        }
    }

    private void deleteSavedSearch(
            BoostSettingsNative.SavedSearches model,
            BoostSettingsNative.SavedSearch entity
    ) {
        try {
            model.delete(entity, success -> {
                if (!success && isAdded()) {
                    showUnavailable();
                }
            });
        } catch (RuntimeException exception) {
            showUnavailable();
        }
    }

    private void showSavedSearchEditor(
            BoostSettingsNative.SavedSearches model,
            BoostSettingsNative.SavedSearch existing
    ) {
        SearchDraft draft = searchDraft(existing);
        EditorPage page = openEditorPage(
                existing == null ? "Add saved search" : "Edit saved search"
        );
        LinearLayout content = page.content;

        EditText name = addEditorInput(
                content,
                "Name (optional)",
                draft.name,
                4
        );
        EditText query = addEditorInput(
                content,
                "Search query",
                draft.query,
                12
        );
        EditText community = addEditorInput(
                content,
                "Community (optional)",
                draft.community,
                12
        );

        addSpace(content, 18);
        LinearLayout choices = addGroup(content);
        LinearLayout sortRow = baseRow();
        TextView sortSummary = addLabels(
                sortRow,
                "Sort",
                searchSortLabel(draft.sort)
        );
        addChevron(sortRow);
        sortRow.setOnClickListener(view -> showValueChoice(
                "Sort",
                new String[]{"Relevance", "New", "Top", "Hot", "Comments"},
                new String[]{"relevance", "new", "top", "hot", "comments"},
                draft.sort,
                value -> {
                    draft.sort = value;
                    sortSummary.setText(searchSortLabel(value));
                }
        ));
        addGroupedRow(choices, sortRow);

        LinearLayout periodRow = baseRow();
        TextView periodSummary = addLabels(
                periodRow,
                "Time period",
                searchPeriodLabel(draft.period)
        );
        addChevron(periodRow);
        periodRow.setOnClickListener(view -> showValueChoice(
                "Time period",
                new String[]{"Hour", "Day", "Week", "Month", "Year", "All"},
                new String[]{"hour", "day", "week", "month", "year", "all"},
                draft.period,
                value -> {
                    draft.period = value;
                    periodSummary.setText(searchPeriodLabel(value));
                }
        ));
        addGroupedRow(choices, periodRow);

        LinearLayout actions = endAlignedActions();
        TextView cancel = actionLabel("Cancel", false);
        cancel.setOnClickListener(view -> page.dismiss());
        actions.addView(cancel, wrapParams());
        TextView save = actionLabel("Save", true);
        LinearLayout.LayoutParams saveParams = wrapParams();
        saveParams.setMarginStart(dp(8));
        actions.addView(save, saveParams);
        save.setOnClickListener(view -> {
            draft.name = name.getText().toString().trim();
            draft.query = query.getText().toString().trim();
            String previousCommunity = draft.community;
            draft.community = community.getText().toString().trim();
            if (!TextUtils.equals(previousCommunity, draft.community)) {
                draft.scopeId = "";
            }
            if (TextUtils.isEmpty(draft.query)) {
                query.setError("A search query is required");
                return;
            }
            save.setEnabled(false);
            saveSavedSearch(model, existing, draft, success -> {
                if (!isAdded() || !page.isShowing()) {
                    return;
                }
                save.setEnabled(true);
                if (success) {
                    page.dismiss();
                } else {
                    showUnavailable();
                }
            });
        });
        content.addView(actions, matchWrapParams());
    }

    private SearchDraft searchDraft(BoostSettingsNative.SavedSearch entity) {
        SearchDraft draft = new SearchDraft();
        if (entity == null) {
            int sortIndex = integerPreference(
                    "pref_default_search_sorting",
                    0
            );
            int periodIndex = integerPreference(
                    "pref_default_search_period",
                    5
            );
            String[] sorts = new String[]{
                    "relevance", "new", "top", "hot", "comments"
            };
            String[] periods = new String[]{
                    "hour", "day", "week", "month", "year", "all"
            };
            draft.sort = sorts[Math.max(0, Math.min(sortIndex, 4))];
            draft.period = periods[Math.max(0, Math.min(periodIndex, 5))];
            return draft;
        }
        draft.id = entity.id;
        draft.name = entity.name;
        draft.community = entity.community;
        draft.scopeId = entity.scopeId;
        draft.query = entity.query;
        draft.sort = entity.sort;
        draft.period = entity.period;
        if (TextUtils.isEmpty(draft.sort)) {
            draft.sort = "relevance";
        }
        if (TextUtils.isEmpty(draft.period)) {
            draft.period = "all";
        }
        return draft;
    }

    private void saveSavedSearch(
            BoostSettingsNative.SavedSearches model,
            BoostSettingsNative.SavedSearch existing,
            SearchDraft draft,
            BoostSettingsNative.SearchCompletion completion
    ) {
        try {
            BoostSettingsNative.SavedSearch entity = new BoostSettingsNative.SavedSearch(
                    draft.id,
                    TextUtils.isEmpty(draft.name) ? draft.query : draft.name,
                    draft.community,
                    draft.scopeId,
                    draft.query,
                    draft.sort.toLowerCase(Locale.ROOT),
                    draft.period.toLowerCase(Locale.ROOT)
            );
            model.save(entity, existing != null, completion);
        } catch (RuntimeException exception) {
            completion.onComplete(false);
        }
    }

    private int integerPreference(String key, int fallback) {
        String value = preferences.getString(key, String.valueOf(fallback));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private String searchSortLabel(String value) {
        if ("new".equals(value)) return "New";
        if ("top".equals(value)) return "Top";
        if ("hot".equals(value)) return "Hot";
        if ("comments".equals(value)) return "Comments";
        return "Relevance";
    }

    private String searchPeriodLabel(String value) {
        if ("hour".equals(value)) return "Hour";
        if ("day".equals(value)) return "Day";
        if ("week".equals(value)) return "Week";
        if ("month".equals(value)) return "Month";
        if ("year".equals(value)) return "Year";
        return "All";
    }

    private void showValueChoice(
            String title,
            String[] labels,
            String[] values,
            String current,
            ValueConsumer consumer
    ) {
        EditorPage page = openEditorPage(title);
        LinearLayout group = addGroup(page.content);
        for (int index = 0; index < labels.length; index++) {
            LinearLayout option = materialChoiceRow(
                    labels[index],
                    "",
                    TextUtils.equals(values[index], current)
            );
            final String value = values[index];
            option.setOnClickListener(view -> {
                consumer.accept(value);
                page.dismiss();
            });
            addGroupedRow(group, option);
        }
    }

    private void showFieldSearchHelp() {
        EditorPage page = openEditorPage("Field search help");
        LinearLayout content = page.content;
        LinearLayout group = addGroup(content);
        TextView help = textView(
                "Use a field followed by a colon:\n\n"
                        + "author:username\n"
                        + "subreddit:android\n"
                        + "title:keyword\n"
                        + "selftext:keyword\n"
                        + "url:example.com\n"
                        + "site:example.com\n"
                        + "self:yes  or  self:no\n"
                        + "nsfw:yes  or  nsfw:no\n\n"
                        + "Combine terms with AND, OR and NOT. "
                        + "Use parentheses to group expressions.",
                15,
                tokens.textPrimary
        );
        help.setTextIsSelectable(true);
        help.setLineSpacing(0, 1.12f);
        help.setPadding(dp(18), dp(16), dp(18), dp(18));
        group.addView(help, matchWrapParams());
    }

    private boolean notificationAccessGranted() {
        String enabled = Settings.Secure.getString(
                requireContext().getContentResolver(),
                "enabled_notification_listeners"
        );
        if (TextUtils.isEmpty(enabled)) {
            return false;
        }
        String packageName = requireContext().getPackageName();
        for (String flat : enabled.split(":")) {
            ComponentName component = ComponentName.unflattenFromString(flat);
            if (component != null
                    && packageName.equals(component.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private void reconcileNotificationAccess() {
        boolean pushEnabled = preferences.getBoolean(
                "pref_check_messages_push",
                false
        );
        boolean accessGranted = notificationAccessGranted();
        if (pushEnabled == accessGranted) {
            return;
        }
        String message = pushEnabled
                ? "Reddit push needs Android notification access. "
                        + "Enable access for Boost on the next screen."
                : "Boost still has Android notification access. "
                        + "You can revoke it on the next screen.";
        showConfirmationPage(
                "Reddit push",
                message,
                "Open settings",
                () -> {
                    try {
                        startActivity(new Intent(
                                Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                        ));
                    } catch (Exception exception) {
                        showUnavailable();
                    }
                }
        );
    }

    private void confirmDelete(Control control) {
        showConfirmationPage(
                control.title,
                "Delete the selected local history data?",
                "Delete",
                () -> {
                    if (deleteForKey(control.key)) {
                        Toast.makeText(
                                requireContext(),
                                "Deleted",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private boolean deleteForKey(String key) {
        try {
            BoostSettingsNative.deleteHistory(key);
            return true;
        } catch (RuntimeException exception) {
            showUnavailable();
            return false;
        }
    }

    private void applySideEffects(String key) {
        // These are Boost's existing invalidation flags. Setting a broader
        // superset is safe and keeps changes visible without a hidden
        // PreferenceFragment listener.
        setBoostStaticBoolean(BoostFieldNames.BoostPreferencesLayoutDirty);
        setBoostStaticBoolean(BoostFieldNames.BoostPreferencesToolbarDirty);
        setBoostStaticBoolean(BoostFieldNames.BoostPreferencesPostViewsDirty);
        setBoostStaticBoolean(BoostFieldNames.BoostPreferencesPostTypographyDirty);
        setBoostStaticBoolean(BoostFieldNames.BoostPreferencesThemeDirty);
        setBoostStaticBoolean(BoostFieldNames.BoostPreferencesCommentTypographyDirty);

        if ("pref_subscriptions_drawer".equals(key)
                || "pref_subscriptions_drawer_show_icon".equals(key)
                || "pref_subscriptions_only_casual".equals(key)) {
            runNativeAction(() -> BoostSettingsNative.subscriptionsChanged(requireContext()));
        }
        if ("pref_check_messages".equals(key)) {
            runNativeAction(() -> BoostSettingsNative.updateNotifications(
                    hostActivity(), preferences.getBoolean(key, true)));
        } else if ("pref_check_messages_interval".equals(key)) {
            runNativeAction(() -> BoostSettingsNative.updateNotifications(hostActivity(), true));
        } else if ("pref_check_messages_push".equals(key)) {
            reconcileNotificationAccess();
        } else if ("pref_search_show_trending_searches".equals(key)
                && preferences.getBoolean(key, false)) {
            runNativeAction(() -> BoostSettingsNative.updateTrending(requireContext()));
        } else if ("pref_cache_max_size".equals(key)) {
            runNativeAction(() -> BoostSettingsNative.resizeMediaCache(requireContext()));
        }
    }

    private void syncRows() {
        if (preferences == null) {
            return;
        }
        for (Binding binding : bindings) {
            Control control = binding.control;
            if (binding.toggle != null) {
                boolean checked = preferences.getBoolean(
                        control.key,
                        booleanDefault(control)
                );
                binding.toggle.setCheckedSilently(checked);
                binding.summary.setText(toggleSummary(control, checked));
            } else if (isChoice(control)) {
                binding.summary.setText(selectedTitle(control));
            } else if (binding.seekBar != null) {
                int value = preferences.getInt(
                        control.key,
                        intDefault(control)
                );
                binding.seekBar.setProgress(value - control.min);
                binding.summary.setText(String.valueOf(value));
            }
        }
        updateDependencies();
    }

    private void updateDependencies() {
        for (Binding binding : bindings) {
            Control control = binding.control;
            boolean enabled = true;
            if (!TextUtils.isEmpty(control.dependency)) {
                Control parent = controlsByKey.get(control.dependency);
                if (parent != null && isToggle(parent)) {
                    boolean parentValue = preferences.getBoolean(
                            parent.key,
                            booleanDefault(parent)
                    );
                    enabled = parent.disableDependentsState
                            ? !parentValue
                            : parentValue;
                }
            }
            if ("pref_video_quality_min".equals(control.key)
                    || "pref_video_quality_max".equals(control.key)) {
                enabled = dataVideoQualityControlEnabled(control.key);
            }
            setRowEnabled(binding.row, enabled);
        }
    }

    private void setRowEnabled(View row, boolean enabled) {
        row.setEnabled(enabled);
        row.setAlpha(enabled ? 1.0f : 0.44f);
        if (row instanceof ViewGroup) {
            setChildrenEnabled((ViewGroup) row, enabled);
        }
    }

    private void setChildrenEnabled(ViewGroup group, boolean enabled) {
        for (int index = 0; index < group.getChildCount(); index++) {
            View child = group.getChildAt(index);
            child.setEnabled(enabled);
            if (child instanceof ViewGroup) {
                setChildrenEnabled((ViewGroup) child, enabled);
            }
        }
    }

    private String actionSummary(Control control) {
        String summary = control.summary;
        if (TextUtils.isEmpty(summary) && control.tag.startsWith("Delete")) {
            summary = "Remove local history data";
        }
        if (TextUtils.isEmpty(summary)
                && "ColorPatternPreference".equals(control.tag)) {
            summary = "Choose comment thread colors";
        }
        return displayActionSummary(control.key, summary, control.tag);
    }

    private String toggleSummary(Control control, boolean checked) {
        String summary = control.summary;
        if (checked && !TextUtils.isEmpty(control.summaryOn)) {
            summary = control.summaryOn;
        } else if (!checked && !TextUtils.isEmpty(control.summaryOff)) {
            summary = control.summaryOff;
        }
        return displaySummary(control.key, summary);
    }

    private String selectedTitle(Control control) {
        String value = preferences.getString(
                control.key,
                stringDefault(control)
        );
        for (int index = 0; index < control.entryValues.length; index++) {
            if (TextUtils.equals(value, control.entryValues[index])) {
                return control.entries[index];
            }
        }
        return value;
    }

    private boolean booleanDefault(Control control) {
        return "true".equalsIgnoreCase(control.defaultValue);
    }

    private int intDefault(Control control) {
        try {
            return Integer.parseInt(control.defaultValue);
        } catch (NumberFormatException ignored) {
            return control.min;
        }
    }

    private String stringDefault(Control control) {
        return TextUtils.isEmpty(control.defaultValue)
                ? ""
                : control.defaultValue;
    }

    private boolean isToggle(Control control) {
        return "CheckBoxPreference".equals(control.tag)
                || "SwitchPreference".equals(control.tag)
                || "SwitchPreferenceCompat".equals(control.tag);
    }

    private boolean isChoice(Control control) {
        return "ListPreference".equals(control.tag)
                || "PreviewSizePreference".equals(control.tag)
                || "PreviewAlignmentPreference".equals(control.tag)
                || "MediaTapActionPreference".equals(control.tag);
    }

    private void configureMorpheControl(Control control) {
        if ("PreviewSizePreference".equals(control.tag)) {
            control.entries = new String[]{"Compact", "Balanced", "Large"};
            control.entryValues = new String[]{"compact", "balanced", "large"};
        } else if ("PreviewAlignmentPreference".equals(control.tag)) {
            control.entries = new String[]{"Left", "Center", "Right"};
            control.entryValues = new String[]{"left", "center", "right"};
        } else if ("MediaTapActionPreference".equals(control.tag)) {
            control.entries = new String[]{
                    "Image viewer",
                    "Video viewer",
                    "Browser",
                    "Disabled"
            };
            control.entryValues = new String[]{
                    "image_viewer",
                    "video_viewer",
                    "browser",
                    "disabled"
            };
        }
    }

    private void openNotificationSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
            startActivity(intent);
        } catch (Exception exception) {
            showUnavailable();
        }
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception exception) {
            showUnavailable();
        }
    }

    private void openEmail(String address) {
        try {
            Uri uri = Uri.parse("mailto:" + address + "?subject=Boost");
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception exception) {
            showUnavailable();
        }
    }

    private void clearMediaCache() {
        try {
            BoostSettingsNative.clearMediaCache(requireContext());
        } catch (RuntimeException exception) {
            showUnavailable();
            return;
        }
        Toast.makeText(
                requireContext(),
                "Cache cleared",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void confirmGdprRevocation() {
        showConfirmationPage(
                "Revoke GDPR consent",
                "Withdraw the stored consent choice and reopen the privacy consent flow?",
                "Revoke",
                this::revokeGdprConsent
        );
    }

    private void revokeGdprConsent() {
        Toast.makeText(
                requireContext(),
                "Boost 1.14.1 does not include a GDPR revocation action.",
                Toast.LENGTH_LONG
        ).show();
    }

    private boolean dataVideoQualityControlEnabled(String key) {
        String mode = preferences.getString("pref_video_quality", "0");
        boolean reduceMobile = preferences.getBoolean(
                "pref_reduce_mobile",
                true
        );
        boolean reduceWifi = preferences.getBoolean(
                "pref_reduce_wifi",
                false
        );
        if ("0".equals(mode)) {
            return "pref_video_quality_min".equals(key);
        }
        if ("1".equals(mode)) {
            return "pref_video_quality_max".equals(key);
        }
        if ("2".equals(mode)) {
            if ("pref_video_quality_min".equals(key)) {
                return reduceMobile || reduceWifi;
            }
            return !reduceMobile || !reduceWifi;
        }
        return true;
    }

    private void showVersion() {
        String version = "Unknown";
        try {
            PackageInfo info = requireContext()
                    .getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);
            version = info.versionName;
        } catch (Exception ignored) {
        }
        EditorPage page = openEditorPage("Boost version");
        LinearLayout group = addGroup(page.content);
        TextView value = textView(version, 18, tokens.textPrimary);
        value.setPadding(dp(18), dp(16), dp(18), dp(18));
        group.addView(value, matchWrapParams());
    }

    private void resetTips() {
        try {
            BoostSettingsNative.resetTips();
        } catch (RuntimeException exception) {
            showUnavailable();
            return;
        }
        Toast.makeText(
                requireContext(),
                "Tips reset",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void runNativeAction(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            showUnavailable();
        }
    }

    private void setBoostStaticBoolean(String fieldName) {
        BoostSettingsNative.markDirty(fieldName);
    }

    private void styleHost() {
        Activity activity = hostActivity();
        if (activity == null || tokens == null) {
            return;
        }
        activity.setTitle(editorPages.isEmpty()
                ? pageTitle
                : editorPages.get(editorPages.size() - 1).title);
        BoostSystemBarInsetsFix.applyMorpheSettingsV4SystemBars(
                activity,
                tokens.background,
                tokens.dark
        );
        int toolbarId = MorpheSettingsV4Catalog.resourceId(
                activity,
                "id",
                "toolbar"
        );
        View toolbar = activity.findViewById(toolbarId);
        if (toolbar != null) {
            toolbar.setBackgroundColor(tokens.background);
            toolbar.setElevation(0);
        }
    }

    private Activity hostActivity() {
        Context context = getContext();
        return context instanceof Activity ? (Activity) context : null;
    }

    private LinearLayout addGroup(LinearLayout parent) {
        LinearLayout group = useAndroidSettingsGuidanceRows()
                ? MorpheSettingsV14Ui.standardList(requireContext())
                : MorpheSettingsV14Ui.group(requireContext());
        parent.addView(group, matchWrapParams());
        return group;
    }

    private LinearLayout baseRow() {
        return MorpheSettingsV14Ui.baseRow(requireContext(), tokens);
    }

    private LinearLayout baseRow(boolean hasSupportingText) {
        if (useAndroidSettingsGuidanceRows()) {
            return MorpheSettingsV14Ui.standardListRow(
                    requireContext(),
                    tokens,
                    hasSupportingText
            );
        }
        return baseRow();
    }

    private MorpheSettingsV14Ui.ChoiceRow materialChoiceRow(
            String title,
            String summary,
            boolean selected
    ) {
        return MorpheSettingsV14Ui.choiceRow(
                requireContext(),
                tokens,
                title,
                summary,
                selected
        );
    }

    private TextView addLabels(
            LinearLayout row,
            String titleValue,
            String summaryValue
    ) {
        LinearLayout labels = new LinearLayout(requireContext());
        labels.setOrientation(LinearLayout.VERTICAL);
        TextView title = textView(titleValue, 16, tokens.textPrimary);
        labels.addView(title);

        TextView summary = textView(
                summaryValue,
                14,
                tokens.textSecondary
        );
        summary.setLineSpacing(0, 1.04f);
        summary.setMaxLines(
                useAndroidSettingsGuidanceRows() ? 2 : 4
        );
        summary.setVisibility(
                TextUtils.isEmpty(summaryValue) ? View.GONE : View.VISIBLE
        );
        LinearLayout.LayoutParams summaryParams = wrapParams();
        summaryParams.topMargin = dp(3);
        labels.addView(summary, summaryParams);
        row.addView(labels, labelsParams());
        return summary;
    }

    private void addChevron(LinearLayout row) {
        row.addView(MorpheSettingsV14Ui.chevron(
                requireContext(),
                tokens
        ));
    }

    private void addGroupedRow(LinearLayout group, View row) {
        if (useAndroidSettingsGuidanceRows()) {
            MorpheSettingsV14Ui.addStandardListRow(group, row, tokens);
            return;
        }
        MorpheSettingsV14Ui.addSegmentedRow(group, row, tokens);
    }

    private Context materialContext() {
        return requireContext();
    }

    private EditorPage openEditorPage(String title) {
        if (pageHost == null) {
            throw new IllegalStateException("Settings page is not attached");
        }
        if (!editorPages.isEmpty()) {
            editorPages.get(editorPages.size() - 1).view.setVisibility(
                    View.GONE
            );
        } else if (settingsPage != null) {
            settingsPage.setVisibility(View.GONE);
        }

        ScrollView scroll = new ScrollView(materialContext());
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setBackgroundColor(tokens.background);

        LinearLayout content = new LinearLayout(materialContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(10), dp(16), dp(36));
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        EditorPage page = new EditorPage(title, scroll, content);
        editorPages.add(page);
        pageHost.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        try {
            pushEditorBackStackEntry();
        } catch (Throwable throwable) {
            removeTopEditorView();
            throw new IllegalStateException(
                    "Morphe V13 editor back-stack bridge failed",
                    throwable
            );
        }
        styleHost();
        return page;
    }

    private void closeTopEditorPage() {
        if (editorPages.isEmpty()) {
            return;
        }
        if (!popEditorBackStackEntry()) {
            throw new IllegalStateException(
                    "Morphe V13 editor back-stack pop failed"
            );
        }
        syncEditorPagesWithBackStack();
    }

    private void removeTopEditorView() {
        if (editorPages.isEmpty()) {
            return;
        }
        EditorPage page = editorPages.remove(editorPages.size() - 1);
        if (page.onDismiss != null) {
            page.onDismiss.run();
        }
        if (pageHost != null) {
            pageHost.removeView(page.view);
        }
        if (editorPages.isEmpty()) {
            if (settingsPage != null) {
                settingsPage.setVisibility(View.VISIBLE);
            }
            syncRows();
        } else {
            editorPages.get(editorPages.size() - 1).view.setVisibility(
                    View.VISIBLE
            );
        }
        styleHost();
    }

    private void ensureEditorBackStackBridge() {
        if (editorBackStack != null) {
            return;
        }
        editorBackStack = BoostSettingsNative.editorBackStack(this, this::syncEditorPagesWithBackStack);
        editorBackStackBaseCount = editorBackStackCount();
    }

    private void pushEditorBackStackEntry() {
        ensureEditorBackStackBridge();
        String name = "morphe-v13-editor-" + (++editorBackStackSequence);
        editorBackStack.push(name);
    }

    private boolean popEditorBackStackEntry() {
        if (editorBackStack == null) {
            return false;
        }
        try {
            return editorBackStack.pop();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private void syncEditorPagesWithBackStack() {
        if (editorBackStack == null
                || editorBackStackBaseCount < 0) {
            return;
        }
        int depth = Math.max(
                0,
                editorBackStackCount() - editorBackStackBaseCount
        );
        while (editorPages.size() > depth) {
            removeTopEditorView();
        }
    }

    private int editorBackStackCount() {
        return editorBackStack.count();
    }

    private EditText addEditorInput(
            LinearLayout parent,
            String hint,
            String value,
            int topMarginDp
    ) {
        MorpheSettingsV14Ui.Field field = MorpheSettingsV14Ui.outlinedField(
                materialContext(),
                tokens,
                hint,
                value
        );
        EditText input = field.input;
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        LinearLayout.LayoutParams params = matchWrapParams();
        params.topMargin = dp(topMarginDp);
        parent.addView(field.root, params);
        return input;
    }

    private TextView actionLabel(String value, boolean filled) {
        return MorpheSettingsV14Ui.action(
                materialContext(),
                tokens,
                value,
                filled
        );
    }

    private LinearLayout endAlignedActions() {
        LinearLayout actions = new LinearLayout(requireContext());
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(14), 0, 0);
        return actions;
    }

    private LinearLayout.LayoutParams spacedMatchWrapParams(int topDp) {
        LinearLayout.LayoutParams params = matchWrapParams();
        params.topMargin = dp(topDp);
        return params;
    }

    private void showConfirmationPage(
            String title,
            String message,
            String confirmLabel,
            Runnable confirmation
    ) {
        EditorPage page = openEditorPage(title);
        TextView body = textView(message, 16, tokens.textPrimary);
        body.setLineSpacing(0, 1.12f);
        body.setPadding(dp(4), dp(12), dp(4), dp(12));
        page.content.addView(body, matchWrapParams());

        LinearLayout actions = endAlignedActions();
        TextView cancel = actionLabel("Cancel", false);
        cancel.setOnClickListener(view -> page.dismiss());
        actions.addView(cancel, wrapParams());
        TextView confirm = actionLabel(confirmLabel, true);
        LinearLayout.LayoutParams confirmParams = wrapParams();
        confirmParams.setMarginStart(dp(8));
        actions.addView(confirm, confirmParams);
        confirm.setOnClickListener(view -> {
            confirmation.run();
            page.dismiss();
        });
        page.content.addView(actions, matchWrapParams());
    }

    private final class EditorPage {
        final String title;
        final View view;
        final LinearLayout content;
        Runnable onDismiss;

        EditorPage(String title, View view, LinearLayout content) {
            this.title = title;
            this.view = view;
            this.content = content;
        }

        boolean isShowing() {
            return pageHost != null && view.getParent() == pageHost;
        }

        void dismiss() {
            if (!editorPages.isEmpty()
                    && editorPages.get(editorPages.size() - 1) == this) {
                closeTopEditorPage();
            }
        }
    }

    private void hideMenuItem(Menu menu, String resourceName) {
        int id = MorpheSettingsV4Catalog.resourceId(
                requireContext(),
                "id",
                resourceName
        );
        if (id != 0) {
            MenuItem item = menu.findItem(id);
            if (item != null) {
                item.setVisible(false);
            }
        }
    }

    private void showUnavailable() {
        Toast.makeText(
                requireContext(),
                "This Boost setting is currently unavailable.",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void addSectionLabel(LinearLayout parent, String value) {
        parent.addView(MorpheSettingsV14Ui.sectionLabel(
                requireContext(),
                tokens,
                value
        ));
    }

    private TextView textView(String value, int sizeSp, int color) {
        TextView view = new TextView(requireContext());
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        return view;
    }

    private LinearLayout.LayoutParams labelsParams() {
        return new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f
        );
    }

    private LinearLayout.LayoutParams wrapParams() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams matchWrapParams() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void addSpace(LinearLayout parent, int heightDp) {
        parent.addView(new View(requireContext()), new LinearLayout.LayoutParams(
                1,
                dp(heightDp)
        ));
    }

    private int dp(float value) {
        return MorpheSettingsV4Theme.dp(requireContext(), value);
    }

    private static String simpleTag(String value) {
        if (value == null) {
            return "";
        }
        int index = value.lastIndexOf('.');
        return index < 0 ? value : value.substring(index + 1);
    }

    private static String attributeText(
            Resources resources,
            XmlResourceParser parser,
            String name
    ) {
        int resourceId = parser.getAttributeResourceValue(
                ANDROID_NS,
                name,
                0
        );
        if (resourceId != 0) {
            try {
                return resources.getText(resourceId).toString();
            } catch (Resources.NotFoundException ignored) {
                return "";
            }
        }
        String value = parser.getAttributeValue(ANDROID_NS, name);
        return value == null || "@null".equals(value) ? "" : value;
    }

    private static String[] attributeArray(
            Resources resources,
            XmlResourceParser parser,
            String name
    ) {
        int resourceId = parser.getAttributeResourceValue(
                ANDROID_NS,
                name,
                0
        );
        if (resourceId == 0) {
            return new String[0];
        }
        try {
            CharSequence[] values = resources.getTextArray(resourceId);
            String[] result = new String[values.length];
            for (int index = 0; index < values.length; index++) {
                result[index] = values[index].toString();
            }
            return result;
        } catch (Resources.NotFoundException ignored) {
            return new String[0];
        }
    }

    private static final class Control {
        String tag = "";
        String section = "";
        String key = "";
        String title = "";
        String summary = "";
        String summaryOn = "";
        String summaryOff = "";
        String defaultValue = "";
        String dependency = "";
        String fragmentName = "";
        boolean disableDependentsState;
        int min;
        int max;
        String[] entries = new String[0];
        String[] entryValues = new String[0];
    }

    private static final class Binding {
        final Control control;
        final View row;
        final TextView summary;
        final MorpheSettingsV14Ui.Toggle toggle;
        final SeekBar seekBar;

        Binding(
                Control control,
                View row,
                TextView summary,
                MorpheSettingsV14Ui.Toggle toggle,
                SeekBar seekBar
        ) {
            this.control = control;
            this.row = row;
            this.summary = summary;
            this.toggle = toggle;
            this.seekBar = seekBar;
        }
    }

    private static final class SearchDraft {
        int id;
        String name = "";
        String community = "";
        String scopeId = "";
        String query = "";
        String sort = "relevance";
        String period = "all";
    }

    private interface SavedSearchRenderer {
        void render(List<BoostSettingsNative.SavedSearch> values);
    }

    private interface ValueConsumer {
        void accept(String value);
    }

}
