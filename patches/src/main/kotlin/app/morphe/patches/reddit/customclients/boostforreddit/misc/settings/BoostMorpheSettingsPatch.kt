/*
 * Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (0226c2e).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.misc.settings

import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.resource.utf8Writer
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches
import app.morphe.patches.all.misc.PatchCategory
import org.w3c.dom.Element

internal val boostMorpheSettingsResourcesPatch = resourcePatch(
    default = false
) {
    compatibleWith(*AppCompatibility.Boost)

    execute {
        get("res/xml/morphe_boost_settings_skeleton.xml").utf8Writer().use { writer ->
            writer.write(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <PreferenceScreen
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    xmlns:app="http://schemas.android.com/apk/res-auto">
                    <PreferenceCategory android:title="Image viewer">
                        <androidx.preference.SeekBarPreference
                            android:key="morphe_boost_image_dismiss_sensitivity"
                            android:title="Swipe image away faster"
                            android:summary="Default 100 enables quick flicks and a moderate 10% drag threshold. 50 keeps Boost&#39;s 15% threshold with quick flicks; 12 restores original Boost behavior."
                            android:defaultValue="100"
                            android:min="12"
                            android:max="100"
                            app:min="12"
                            app:showSeekBarValue="true" />
                    </PreferenceCategory>

                    <PreferenceCategory android:title="Search">
                        <CheckBoxPreference
                            android:key="morphe_boost_search_open_keyboard_on_entry"
                            android:title="Open keyboard when entering Search"
                            android:summary="Focus the search field and show the keyboard immediately. When disabled, tap Search again to start typing."
                            android:defaultValue="false" />
                        <CheckBoxPreference
                            android:key="morphe_boost_search_show_active_subreddits"
                            android:title="Show active subreddits"
                            android:summary="List currently active subreddits when Search opens."
                            android:defaultValue="true" />
                    </PreferenceCategory>

                    <PreferenceCategory android:title="Display &amp; performance">
                        <CheckBoxPreference
                            android:key="morphe_boost_prefer_high_refresh_rate"
                            android:title="Prefer high refresh rate"
                            android:summary="Ask Android to use a high refresh rate for Boost windows on adaptive-refresh displays."
                            android:defaultValue="true" />
                    </PreferenceCategory>

                    <PreferenceCategory android:title="Recovery &amp; archives">
                        <CheckBoxPreference
                            android:key="morphe_boost_reddit_undelete_enabled"
                            android:title="Automatically undelete Reddit content"
                            android:summary="Restore removed Reddit posts and comments from Arctic Shift and the Wayback Machine. When a thread has removed content, its post and comment IDs are sent to these third-party archives. Off by default."
                            android:defaultValue="false" />

                        <CheckBoxPreference
                            android:key="morphe_boost_imgur_undelete_enabled"
                            android:title="Automatically undelete Imgur images"
                            android:summary="Restore missing Imgur images and albums from the Wayback Machine. Links to missing Imgur media you open are sent to this third-party archive. Off by default."
                            android:defaultValue="false" />
                    </PreferenceCategory>
                </PreferenceScreen>
                """.trimIndent()
            )
        }

        document("res/xml/pref_headers_v2.xml").use { document ->
            val root = document.documentElement
            require(root.tagName == "PreferenceScreen") { "Unexpected Boost settings header root" }
            val preferences = root.getElementsByTagName("Preference")
            require((0 until preferences.length).none {
                (preferences.item(it) as Element).getAttribute("android:key") == "morphe_boost_settings_entry"
            }) { "Boost settings already contain a Morphe entry" }
            val entry = document.createElement("Preference").apply {
                setAttribute("android:icon", "@drawable/ic_puzzle_24dp")
                setAttribute("android:key", "morphe_boost_settings_entry")
                setAttribute("android:title", "Morphe")
                setAttribute("android:summary", "Features added by Morphe patches")
                setAttribute(
                    "android:fragment",
                    "app.morphe.extension.boost.settings.MorpheSettingsFragment",
                )
            }
            root.insertBefore(entry, root.firstChild)
        }
    }
}

@Suppress("unused")
val boostMorpheSettingsPatch = resourcePatch(
    name = "Boost Morphe settings",
    description = "Adds a native-style Morphe section while keeping Boost's original settings screens.",
    default = false,
) {
    category(PatchCategory.INTERFACE)
    dependsOn(ExtensionPatches.Boost, boostMorpheSettingsResourcesPatch)
    compatibleWith(*AppCompatibility.Boost)
}
