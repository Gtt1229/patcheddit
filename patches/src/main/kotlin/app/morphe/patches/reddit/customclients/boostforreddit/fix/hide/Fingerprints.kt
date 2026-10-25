/*
 * Modifications Copyright 2026 brealorg.
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.hide

import app.morphe.patcher.Fingerprint

internal val subredditBaseHideToggleFingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf(
        "I",
        "Lcom/rubenmayayo/reddit/models/reddit/SubmissionModel;",
        "Z",
    ),
    custom = { method, classDef ->
        classDef.type == "Lcom/rubenmayayo/reddit/ui/submissions/subreddit/SubredditBaseActivity;" &&
            method.name == "o0"
    },
)

// All feed layouts now share identity-based hiding in this base fragment.
internal val hideFragmentFingerprint = Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/fragments/SubmissionRecyclerViewFragment;",
    name = "o0",
    returnType = "V",
    parameters = listOf("Lcom/rubenmayayo/reddit/models/reddit/SubmissionModel;"),
)

internal val feedActionS1InvalidIndexFingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf("I"),
    custom = { method, classDef ->
        classDef.type == "Lcom/rubenmayayo/reddit/ui/fragments/SubmissionRecyclerViewFragment;" &&
            method.name == "W0"
    },
)
