/*
 * Modifications Copyright 2026 brealorg.
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.gallery

import app.morphe.patcher.Fingerprint

internal val galleryPageSelectedFingerprint = Fingerprint(
    definingClass = "Lg21;",
    name = "c",
    returnType = "V",
    parameters = listOf("I"),
)

internal val galleryPagerStateFingerprint = Fingerprint(
    definingClass = "Lg21;",
    name = "b",
    returnType = "V",
    parameters = listOf("I"),
)

internal val galleryPageFactoryFingerprint = Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/activities/GalleryActivity\$b;",
    name = "m",
    returnType = "Landroidx/fragment/app/Fragment;",
    parameters = listOf("I"),
)

internal val imageModelFragmentOnCreateViewFingerprint = Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/fragments/imagemodel/ImageModelFragment;",
    name = "onCreateView",
    returnType = "Landroid/view/View;",
    parameters = listOf(
        "Landroid/view/LayoutInflater;",
        "Landroid/view/ViewGroup;",
        "Landroid/os/Bundle;",
    ),
)

internal val imageModelFragmentOnDestroyFingerprint = Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/fragments/imagemodel/ImageModelFragment;",
    name = "onDestroy",
    returnType = "V",
    parameters = emptyList(),
)
