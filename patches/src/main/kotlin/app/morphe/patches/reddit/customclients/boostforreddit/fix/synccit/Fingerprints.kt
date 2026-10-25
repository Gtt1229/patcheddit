/*
 * Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.synccit

import app.morphe.patcher.Fingerprint

internal const val LEGACY_SYNCCIT_API_URL =
    "https://api.synccit.com/api.php"

private const val SYNCCIT_CLIENT_CLASS = "Lo63;"

internal val synccitUpdateFingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf(
        "Lcom/rubenmayayo/reddit/models/synccit/LinkModel;",
    ),
    strings = listOf(LEGACY_SYNCCIT_API_URL),
    custom = { method, classDef ->
        classDef.type == SYNCCIT_CLIENT_CLASS &&
            method.name == "c"
    },
)

internal val synccitAddFingerprint = Fingerprint(
    returnType =
        "Lcom/rubenmayayo/reddit/models/synccit/AddModel;",
    parameters = listOf(
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
    ),
    strings = listOf(LEGACY_SYNCCIT_API_URL),
    custom = { method, classDef ->
        classDef.type == SYNCCIT_CLIENT_CLASS &&
            method.name == "a"
    },
)

internal val synccitAuthenticateFingerprint = Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/synccit/SynccitCreateActivity\$a;",
    name = "doInBackground",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("[Ljava/lang/Object;"),
    strings = listOf(LEGACY_SYNCCIT_API_URL, "create"),
)

internal val synccitReadFingerprint = Fingerprint(
    returnType = "Ljava/util/List;",
    parameters = listOf(
        "Ljava/util/ArrayList;",
    ),
    strings = listOf(LEGACY_SYNCCIT_API_URL),
    custom = { method, classDef ->
        classDef.type == SYNCCIT_CLIENT_CLASS &&
            method.name == "b"
    },
)
