/*
 * Copyright 2026 wchill.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.misc.settings

import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.resource.utf8Writer
import app.morphe.patches.all.misc.PatchCategory
import app.morphe.patches.reddit.customclients.AppCompatibility

@Suppress("unused")
val oauthTokensPatch = resourcePatch(
    name = "Import and export OAuth tokens",
    description = "Adds native-style Morphe settings to transfer Reddit account tokens using JSON files or the clipboard.",
    default = false,
) {
    category(PatchCategory.LOGIN)
    dependsOn(boostMorpheSettingsPatch)
    compatibleWith(*AppCompatibility.Boost)

    execute {
        get("res/xml/morphe_boost_oauth_tokens.xml").utf8Writer().use { writer ->
            writer.write(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <PreferenceScreen xmlns:android="http://schemas.android.com/apk/res/android">
                    <Preference
                        android:title="Private account credentials"
                        android:summary="Tokens grant access to your Reddit account. Keep exported files and clipboard contents private. The same OAuth client ID must be configured in both apps."
                        android:selectable="false" />
                    <Preference
                        android:key="morphe_boost_oauth_account"
                        android:title="Selected account"
                        android:selectable="false" />
                    <Preference
                        android:key="morphe_boost_oauth_import"
                        android:title="Import tokens"
                        android:summary="Add or update an account from token JSON. Validates with Reddit and restarts Boost with the imported account selected."
                        android:persistent="false" />
                    <Preference
                        android:key="morphe_boost_oauth_export"
                        android:title="Export tokens"
                        android:summary="Copy private token JSON or save it to a local file."
                        android:persistent="false" />
                </PreferenceScreen>
                """.trimIndent(),
            )
        }
        document("res/xml/morphe_boost_settings_skeleton.xml").use { document ->
            val category = document.createElement("PreferenceCategory").apply {
                setAttribute("android:title", "Reddit account")
            }
            category.appendChild(document.createElement("Preference").apply {
                setAttribute("android:key", "morphe_boost_oauth_tokens")
                setAttribute("android:title", "OAuth tokens")
                setAttribute("android:summary", "Import or export account access and refresh tokens")
                setAttribute(
                    "android:fragment",
                    "app.morphe.extension.boost.settings.OAuthTokensFragment",
                )
            })
            document.documentElement.appendChild(category)
        }
    }
}
