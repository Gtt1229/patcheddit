/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Modifications Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (0226c2e).
 */

package app.morphe.patches.reddit.customclients.boostforreddit.debug

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.resource.utf8Writer
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches
import app.morphe.patches.all.misc.PatchCategory
import app.morphe.patches.reddit.customclients.boostforreddit.bindings.BoostApi
import app.morphe.patches.reddit.customclients.boostforreddit.misc.settings.boostMorpheSettingsPatch
import app.morphe.patches.reddit.customclients.boostforreddit.misc.settings.boostMorpheSettingsResourcesPatch

const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/morphe/extension/boost/debug/ExceptionHook;"

private val exceptionLogResourcesPatch = resourcePatch(default = false) {
    dependsOn(boostMorpheSettingsResourcesPatch)

    execute {
        get("res/xml/morphe_boost_exception_log.xml").utf8Writer().use { writer ->
            writer.write(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <PreferenceScreen xmlns:android="http://schemas.android.com/apk/res/android">
                    <Preference
                        android:title="Private diagnostic log"
                        android:summary="Records errors reported through Boost's exception handler, not every app crash. Logs stay in private app storage in two rotating 4 MiB files (8 MiB total). Common credential fields are redacted, but messages may still contain personal or request data. Review before sharing."
                        android:selectable="false" />
                    <Preference
                        android:key="morphe_boost_exception_log_view"
                        android:title="View log"
                        android:summary="View the most recent saved exceptions"
                        android:persistent="false" />
                    <Preference
                        android:key="morphe_boost_exception_log_save"
                        android:title="Save log to file"
                        android:summary="Save a copy using Android's document picker"
                        android:persistent="false" />
                    <Preference
                        android:key="morphe_boost_exception_log_clear"
                        android:title="Clear log"
                        android:summary="Delete all saved exception log entries"
                        android:persistent="false" />
                </PreferenceScreen>
                """.trimIndent(),
            )
        }
        document("res/xml/morphe_boost_settings_skeleton.xml").use { document ->
            val category = document.createElement("PreferenceCategory").apply {
                setAttribute("android:title", "Diagnostics")
            }
            category.appendChild(document.createElement("Preference").apply {
                setAttribute("android:key", "morphe_boost_exception_log")
                setAttribute("android:title", "Exception log")
                setAttribute("android:summary", "View, save or clear recorded Boost errors")
                setAttribute(
                    "android:fragment",
                    "app.morphe.extension.boost.settings.ExceptionLogFragment",
                )
            })
            document.documentElement.appendChild(category)
        }
    }
}

@Suppress("unused")
val hookExceptionHandler = bytecodePatch(
    name = "Log non-fatal exceptions",
    description = "Logs errors reported by Boost, viewable in Morphe settings.",
    default = true
) {
    category(PatchCategory.ADVANCED)
    dependsOn(ExtensionPatches.Boost, boostMorpheSettingsPatch, exceptionLogResourcesPatch)
    compatibleWith(*AppCompatibility.Boost)

    execute {
        BoostApi.MyApplication.onCreate.fingerprint().method.addInstructions(
            0,
            "invoke-static {p0}, $EXTENSION_CLASS_DESCRIPTOR->initialize(Landroid/app/Application;)V",
        )
        ExceptionHandlerFingerprint.method.apply {
            addInstructions(
                0,
                """
                    const-string v0, "Non-fatal Boost error"
                    invoke-static {p0, v0}, $EXTENSION_CLASS_DESCRIPTOR->handleException(Ljava/lang/Throwable;Ljava/lang/String;)V
                """,
            )
        }
    }
}
