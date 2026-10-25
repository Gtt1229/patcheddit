/*
 * Modifications Copyright 2026 brealorg.
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (0226c2e).
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.media

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches
import app.morphe.patches.reddit.customclients.boostforreddit.wrapHook
import app.morphe.patches.all.misc.PatchCategory

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/morphe/extension/boost/media/ImageDismissTuning;"

private const val CONTRACT_MARKER =
    "MORPHE_BOOST_IMAGE_DISMISS_TUNING_ISSUE96_V4_1"

private val mediaImageActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/activities/MediaImageActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

private val legacyImageActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/activities/ImageActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

private val legacyImageActivity2OnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/activities/ImageActivity2;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

private val imageModelFragmentOnCreateViewFingerprint = Fingerprint(
    definingClass =
        "Lcom/rubenmayayo/reddit/ui/fragments/imagemodel/ImageModelFragment;",
    name = "onCreateView",
    returnType = "Landroid/view/View;",
    parameters = listOf(
        "Landroid/view/LayoutInflater;",
        "Landroid/view/ViewGroup;",
        "Landroid/os/Bundle;",
    ),
)

@Suppress("unused")
val imageDismissTuningPatch = bytecodePatch(
    name = "Tune Boost image dismissal",
    description =
        "Lets you close the image viewer with a quick flick, without changing GIF, video or other gestures.",
    default = true,
) {
    category(PatchCategory.MEDIA)
    dependsOn(ExtensionPatches.Boost)
    compatibleWith(*AppCompatibility.Boost)

    execute {
        check(CONTRACT_MARKER.endsWith("ISSUE96_V4_1"))

        arrayOf(
            mediaImageActivityOnCreateFingerprint,
            legacyImageActivityOnCreateFingerprint,
            legacyImageActivity2OnCreateFingerprint,
        ).forEach { fingerprint ->
            fingerprint.wrapHook(
                after = "invoke-static {p0}, $EXTENSION_CLASS_DESCRIPTOR->apply(Ljava/lang/Object;)V",
            )
        }

        imageModelFragmentOnCreateViewFingerprint.wrapHook(
            after = "invoke-static {p0}, $EXTENSION_CLASS_DESCRIPTOR->apply(Ljava/lang/Object;)V",
        )
    }
}
