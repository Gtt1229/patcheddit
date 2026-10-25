/*
 * Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.widget

import app.morphe.patcher.Fingerprint

internal val imageWidgetProviderUpdateWithSubmissionFingerprint = Fingerprint(
    definingClass = "Lqg3;",
    name = "c",
    returnType = "V",
    parameters = listOf(
        "I",
        "Lcom/rubenmayayo/reddit/models/reddit/SubmissionModel;",
        "Ljava/lang/String;",
    ),
    strings = listOf("Widget updateWithSubmissions %d"),
)
