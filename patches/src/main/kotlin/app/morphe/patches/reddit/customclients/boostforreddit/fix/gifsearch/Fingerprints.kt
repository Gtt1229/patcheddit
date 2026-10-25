/*
 * Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.gifsearch

import app.morphe.patcher.Fingerprint

private const val FORMATTING_BAR_DESCRIPTOR =
    "Lcom/rubenmayayo/reddit/ui/customviews/FormattingBar;"

internal val formattingBarInsertMarkdownFingerprint = Fingerprint(
    definingClass = FORMATTING_BAR_DESCRIPTOR,
    name = "d",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;")
)

internal val formattingBarOpenLinkDialogWithUrlFingerprint = Fingerprint(
    definingClass = FORMATTING_BAR_DESCRIPTOR,
    name = "c",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;")
)

internal val formattingBarOpenLinkDialogFingerprint = Fingerprint(
    definingClass = FORMATTING_BAR_DESCRIPTOR,
    name = "b",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;")
)

internal val formattingBarSetEditTextFingerprint = Fingerprint(
    definingClass = FORMATTING_BAR_DESCRIPTOR,
    name = "setEditText",
    returnType = "V",
    parameters = listOf("Landroid/widget/EditText;")
)

internal val formattingBarInitFingerprint = Fingerprint(
    definingClass = FORMATTING_BAR_DESCRIPTOR,
    name = "<init>",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Landroid/util/AttributeSet;"),
)

internal val formattingBarImageMenuOptionsFingerprint = Fingerprint(
    definingClass = FORMATTING_BAR_DESCRIPTOR,
    name = "formatImage",
    returnType = "V",
    parameters = listOf("Landroid/view/View;"),
)

internal val formattingBarHandleImageMenuOptionFingerprint = Fingerprint(
    definingClass = "Lzy0;",
    name = "a",
    returnType = "V",
    parameters = listOf("Lcom/rubenmayayo/reddit/ui/customviews/menu/MenuOption;")
)
