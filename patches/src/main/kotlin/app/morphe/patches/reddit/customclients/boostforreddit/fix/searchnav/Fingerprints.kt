/*
 * Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.searchnav

import app.morphe.patcher.Fingerprint

internal val searchAbstractActivityOnCreateFingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    custom = { method, classDef ->
        classDef.type == "Lcom/rubenmayayo/reddit/ui/search/SearchAbstractActivity;" &&
            method.name == "onCreate"
    },
)

internal val drawerMaterialBottomNavigationFingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf("I"),
    custom = { method, classDef ->
        classDef.type == "Lcom/rubenmayayo/reddit/ui/activities/e;" &&
            method.name == "g3"
    },
)

internal val mainActivityOnResumeFingerprint = Fingerprint(
    returnType = "V",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type ==
            "Lcom/rubenmayayo/reddit/ui/submissions/subreddit/MainActivity;" &&
            method.name == "onResume"
    },
)

internal val subredditActivityOnCreateFingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    custom = { method, classDef ->
        classDef.type ==
            "Lcom/rubenmayayo/reddit/ui/submissions/subreddit/SubredditActivity;" &&
            method.name == "onCreate"
    },
)

internal val drawerBottomNavigationVisibilityFingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf("Z"),
    custom = { method, classDef ->
        classDef.type ==
            "Lcom/rubenmayayo/reddit/ui/activities/e;" &&
            method.name == "p3"
    },
)
internal val inboxCountUpdateFingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf("I"),
    custom = { method, classDef ->
        classDef.type ==
            "Lcom/rubenmayayo/reddit/ui/activities/e;" &&
            method.name == "N3"
    },
)

internal val searchAbstractSubredditsErrorCallbackFingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf("Ljava/lang/Exception;"),
    custom = { method, classDef ->
        classDef.type ==
            "Lcom/rubenmayayo/reddit/ui/search/SearchAbstractActivity\$e;" &&
            method.name == "a"
    },
)
