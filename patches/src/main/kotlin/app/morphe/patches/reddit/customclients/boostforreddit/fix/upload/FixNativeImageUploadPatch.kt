/*
 * Modifications Copyright 2026 brealorg.
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (0226c2e).
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.upload

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches
import app.morphe.patches.reddit.customclients.boostforreddit.misc.settings.boostMorpheSettingsSkeletonPatch
import app.morphe.patches.all.misc.PatchCategory

private const val REDDIT_UPLOAD_PROVIDER = "reddit"

private const val UPLOAD_SETTINGS_DESCRIPTOR =
    "Lapp/morphe/extension/boost/upload/ExternalImageUploadSettings;"

private const val UPLOAD_FACTORY_DESCRIPTOR =
    "Lapp/morphe/extension/boost/upload/ExternalImageUploadFactory;"

private const val NATIVE_REDDIT_UPLOAD_MARKER =
    "MORPHE_BOOST_NATIVE_REDDIT_IMAGE_UPLOAD_ISSUE66_V2"

private const val EXTERNAL_IMAGE_HOST_MARKER =
    "MORPHE_BOOST_EXTERNAL_IMAGE_HOST_ISSUE66_V1"

private const val IMAGE_HOST_POLICY_MARKER =
    "MORPHE_BOOST_IMAGE_HOST_POLICY_ISSUE66_V3"

@Suppress("unused")
val fixNativeImageUploadPatch = bytecodePatch(
    name = "Fix Boost native image upload",
    description =
        "Uploads image posts and galleries to Reddit, and images in comments or text posts to Imgur, or ImgBB if you choose it. GIF and video uploads are unchanged.",
    default = false
) {
    category(PatchCategory.COMMENTS)
    dependsOn(ExtensionPatches.Boost, boostMorpheSettingsSkeletonPatch)
    compatibleWith(*AppCompatibility.Boost)

    execute {
        check(NATIVE_REDDIT_UPLOAD_MARKER.endsWith("ISSUE66_V2"))
        check(EXTERNAL_IMAGE_HOST_MARKER.endsWith("ISSUE66_V1"))
        check(IMAGE_HOST_POLICY_MARKER.endsWith("ISSUE66_V3"))

        // The provider-specific getters were inlined into the editor and submit
        // callers. Override only these keys in their shared, surviving getter.
        remoteConfigStringFingerprint.method.apply {
            addInstructionsWithLabels(
                0,
                """
                    const-string v0, "uploader_format"
                    invoke-virtual {v0, p0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-eqz v0, :morphe_check_submit_provider
                    invoke-static {}, $UPLOAD_SETTINGS_DESCRIPTOR->getEditorProvider()Ljava/lang/String;
                    move-result-object v0
                    return-object v0
                    :morphe_check_submit_provider
                    const-string v0, "uploader_submit"
                    invoke-virtual {v0, p0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-nez v0, :morphe_reddit_provider
                    const-string v0, "uploader_submit_multiple"
                    invoke-virtual {v0, p0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-eqz v0, :morphe_native_remote_string
                    :morphe_reddit_provider
                    const-string v0, "$REDDIT_UPLOAD_PROVIDER"
                    return-object v0
                """,
                ExternalLabel("morphe_native_remote_string", getInstruction(0)),
            )
        }

        mediaUploaderFactoryFingerprint.method.addInstructions(
            0,
            """
                invoke-static {p0}, $UPLOAD_FACTORY_DESCRIPTOR->create(Ljava/lang/String;)Lcs1;
                move-result-object v0
                return-object v0
            """.trimIndent(),
        )

        remoteConfigBooleanFingerprint.method.apply {
            addInstructionsWithLabels(
                0,
                """
                    const-string v0, "submit_reddit_as_image_kind"
                    invoke-virtual {v0, p0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-eqz v0, :morphe_native_remote_boolean
                    const/4 v0, 0x1
                    return v0
                """,
                ExternalLabel("morphe_native_remote_boolean", getInstruction(0)),
            )
        }

        editorImagePreviewFormattingBarSetEditTextFingerprint.method.addInstructions(
            0,
            """
                invoke-static {p1}, Lapp/morphe/extension/boost/upload/EditorImagePreview;->bind(Landroid/widget/EditText;)V
            """.trimIndent(),
        )

        submitGallerySubmissionKindFingerprint.method.apply {
            addInstructions(
                0,
                """
                    const-string v0, "MORPHE_ISSUE17_NATIVE_IMAGE_UPLOAD_V1"
                    const-string v0, "$NATIVE_REDDIT_UPLOAD_MARKER"
                    const-string v0, "$EXTERNAL_IMAGE_HOST_MARKER"
                    const-string v0, "$IMAGE_HOST_POLICY_MARKER"
                """.trimIndent(),
            )
        }
    }
}
