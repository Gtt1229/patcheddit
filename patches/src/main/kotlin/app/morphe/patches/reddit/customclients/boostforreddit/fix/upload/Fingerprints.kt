/*
 * Modifications Copyright 2026 brealorg.
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.upload

import app.morphe.patcher.Fingerprint

private const val BOOST_REMOTE_CONFIG_CLASS = "Lxe2;"

internal val editorImagePreviewFormattingBarSetEditTextFingerprint = Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/customviews/FormattingBar;",
    name = "setEditText",
    returnType = "V",
    parameters = listOf("Landroid/widget/EditText;"),
)

internal val submitGallerySubmissionKindFingerprint = Fingerprint(
    custom = { method, classDef ->
        classDef.type ==
            "Lcom/rubenmayayo/reddit/ui/submit/v2/SubmitGalleryFragment;" &&
            method.name == "p1"
    }
)

internal val remoteConfigStringFingerprint = Fingerprint(
    definingClass = BOOST_REMOTE_CONFIG_CLASS,
    name = "c",
    returnType = "Ljava/lang/String;",
    parameters = listOf("Ljava/lang/String;"),
)

internal val remoteConfigBooleanFingerprint = Fingerprint(
    definingClass = BOOST_REMOTE_CONFIG_CLASS,
    name = "a",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
)

internal val mediaUploaderFactoryFingerprint = Fingerprint(
    definingClass = "Lfl1;",
    name = "g",
    returnType = "Lcs1;",
    parameters = listOf("Ljava/lang/String;"),
    strings = listOf(
        "imgur_free",
        "imgur_paid",
        "vgy",
        "reddit",
    ),
)
