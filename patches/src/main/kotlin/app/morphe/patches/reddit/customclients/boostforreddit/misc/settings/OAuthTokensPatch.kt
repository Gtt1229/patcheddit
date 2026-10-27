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
    description = "Adds an editable JSON field in Morphe settings to view or replace Reddit account tokens.",
    default = true,
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
                        android:summary="Copy or replace the JSON below. Tokens grant access to your Reddit account; keep them private. Saving validates tokens with Reddit and restarts Boost with their account selected. The same OAuth client ID must be configured in both apps."
                        android:selectable="false" />
                    <Preference
                        android:key="morphe_boost_oauth_account"
                        android:title="Selected account"
                        android:selectable="false" />
                </PreferenceScreen>
                """.trimIndent(),
            )
        }
        get("res/layout/morphe_boost_oauth_editor.xml").utf8Writer().use { writer ->
            writer.write(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">
                    <View
                        android:layout_width="match_parent"
                        android:layout_height="1dp"
                        android:layout_marginBottom="16dp"
                        android:background="?android:attr/listDivider"
                        android:importantForAccessibility="no" />
                    <EditText
                        android:id="@android:id/edit"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:fontFamily="monospace"
                        android:gravity="top|start"
                        android:hint="OAuth token JSON"
                        android:minLines="8"
                        android:maxLines="14"
                        android:textSize="12sp" />
                    <Button
                        android:id="@android:id/button1"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="end"
                        android:text="Save" />
                </LinearLayout>
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
                setAttribute("android:summary", "View/edit OAuth tokens")
                setAttribute(
                    "android:fragment",
                    "app.morphe.extension.boost.settings.OAuthTokensFragment",
                )
            })
            document.documentElement.appendChild(category)
        }
    }
}
