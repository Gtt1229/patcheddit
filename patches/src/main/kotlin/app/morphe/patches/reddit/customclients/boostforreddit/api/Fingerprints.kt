/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.api

import app.morphe.patcher.Fingerprint

internal object GetApiPreferenceFingerprint : Fingerprint(
    definingClass = "Lns2;",
    name = "g",
    parameters = listOf("Ljava/lang/String;"),
    returnType = "Ljava/lang/String;",
)

internal object JrawNewUrlFingerprint : Fingerprint(
    definingClass = "Lnet/dean/jraw/util/JrawUtils;",
    name = "newUrl",
    parameters = listOf("Ljava/lang/String;"),
    returnType = "Ljava/net/URL;",
)
