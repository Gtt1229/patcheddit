/*
 * Copyright 2026 brealorg.
 * Modifications Copyright 2026 wchill.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (0226c2e).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.upload

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.resource.utf8Writer
import app.morphe.patcher.util.smali.ExternalLabel
import app.morphe.patches.all.misc.PatchCategory
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches
import app.morphe.patches.reddit.customclients.boostforreddit.misc.settings.boostMorpheSettingsResourcesPatch
import app.morphe.patches.reddit.customclients.boostforreddit.misc.settings.boostMorpheSettingsPatch
import org.w3c.dom.Element

private val editorImageHostingResourcesPatch = resourcePatch(default = false) {
    dependsOn(boostMorpheSettingsResourcesPatch)

    execute {
        get("res/layout/preference_dialog_edittext.xml")
            .copyTo(get("res/layout/morphe_boost_imgbb_api_key_dialog.xml"), overwrite = true)
        document("res/layout/morphe_boost_imgbb_api_key_dialog.xml").use { document ->
            val views = document.getElementsByTagName("*")
            val input = (0 until views.length).map { views.item(it) as Element }
                .single { it.getAttribute("android:id") == "@android:id/edit" }
            input.setAttribute("android:inputType", "textPassword")
            input.setAttribute("android:singleLine", "true")
        }
        get("res/xml/morphe_boost_editor_image_hosting.xml").utf8Writer().use { writer ->
            writer.write(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <PreferenceScreen xmlns:android="http://schemas.android.com/apk/res/android">
                    <app.morphe.extension.boost.upload.EditorImageHostPreference
                        android:key="morphe_boost_external_image_host"
                        android:title="Image host"
                        android:dialogTitle="Editor image host"
                        android:defaultValue="imgur_free" />
                    <app.morphe.extension.boost.upload.ImgBbApiKeyPreference
                        android:key="morphe_boost_imgbb_api_key"
                        android:title="ImgBB API key"
                        android:dependency="morphe_boost_external_image_host"
                        android:dialogTitle="Personal ImgBB API key"
                        android:dialogMessage="Required when using ImgBB. Leave empty to clear the saved key."
                        android:dialogLayout="@layout/morphe_boost_imgbb_api_key_dialog"
                        android:defaultValue="" />
                </PreferenceScreen>
                """.trimIndent(),
            )
        }
        document("res/xml/morphe_boost_settings_skeleton.xml").use { document ->
            val category = document.createElement("PreferenceCategory").apply {
                setAttribute("android:title", "Media uploads")
            }
            category.appendChild(document.createElement("Preference").apply {
                setAttribute("android:icon", "@drawable/ic_photo_outline_24dp")
                setAttribute("android:key", "morphe_boost_image_hosting_settings")
                setAttribute("android:title", "Editor image hosting")
                setAttribute("android:summary", "Images added to text: Imgur - default")
                setAttribute(
                    "android:fragment",
                    "app.morphe.extension.boost.settings.EditorImageHostingFragment",
                )
            })
            val categories = document.getElementsByTagName("PreferenceCategory")
            val search = (0 until categories.length)
                .map { categories.item(it) as Element }
                .single { it.getAttribute("android:title") == "Search" }
            document.documentElement.insertBefore(category, search)
        }
    }
}

@Suppress("unused")
val editorImageHostingPatch = bytecodePatch(
    name = "Configure editor image hosting",
    description =
        "Uses Imgur Free or your configured ImgBB account for images attached to comments and text posts.",
    default = false,
) {
    category(PatchCategory.COMMENTS)
    dependsOn(ExtensionPatches.Boost, boostMorpheSettingsPatch, editorImageHostingResourcesPatch)
    compatibleWith(*AppCompatibility.Boost)

    execute {
        remoteConfigStringFingerprint.method.apply {
            addInstructionsWithLabels(
                0,
                """
                    const-string v0, "uploader_format"
                    invoke-virtual {v0, p0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-eqz v0, :morphe_native_editor_provider
                    invoke-static {}, Lapp/morphe/extension/boost/upload/ExternalImageUploadSettings;->getEditorProvider()Ljava/lang/String;
                    move-result-object v0
                    return-object v0
                """,
                ExternalLabel("morphe_native_editor_provider", getInstruction(0)),
            )
        }
        mediaUploaderFactoryFingerprint.method.apply {
            addInstructionsWithLabels(
                0,
                """
                    const-string v0, "imgbb"
                    invoke-virtual {v0, p0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-eqz v0, :morphe_native_upload_factory
                    invoke-static {}, Lapp/morphe/extension/boost/upload/ExternalImageUploadFactory;->createImgBbUploader()${returnType}
                    move-result-object v0
                    return-object v0
                """,
                ExternalLabel("morphe_native_upload_factory", getInstruction(0)),
            )
        }
    }
}
