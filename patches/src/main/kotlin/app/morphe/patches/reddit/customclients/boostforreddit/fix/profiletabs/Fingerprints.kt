/*
 * Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.profiletabs

import app.morphe.patcher.Fingerprint

internal val userActivityBuildTabsFingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    name = "onCreate",
    custom = { _, classDef ->
        classDef.type == "Lcom/rubenmayayo/reddit/ui/profile/UserActivity;"
    },
)
