/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.http.archive

import app.morphe.patcher.Fingerprint

internal object LinkBuildContextMenuFingerprint : Fingerprint(
    definingClass = "Lj32;",
    name = "b",
    parameters = listOf("Ljava/lang/String;"),
    returnType = "V",
    strings = listOf("Yandex", "Google", "TinEye"),
)

internal object OnClickContextMenuFingerprint : Fingerprint(
    definingClass = "Lj32\$a;",
    name = "a",
    parameters = listOf("Lcom/rubenmayayo/reddit/ui/customviews/menu/MenuOption;"),
    returnType = "V",
)
