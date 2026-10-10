/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 * Modifications Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.ads

import app.morphe.patcher.Fingerprint

internal object MaxMediationFingerprint : Fingerprint(
    strings = listOf("MaxMediation: Attempting to initialize SDK")
)

internal object AdmobMediationFingerprint : Fingerprint(
    strings = listOf("AdmobMediation: Attempting to initialize SDK")
)

private fun boostAdFragmentFingerprint(
    classType: String,
    methodName: String
) = Fingerprint(
    definingClass = classType,
    name = methodName
)

internal val boostAdFragmentFingerprints = arrayOf(
    boostAdFragmentFingerprint("Lcom/rubenmayayo/reddit/ui/fragments/j;", "W2"),
    boostAdFragmentFingerprint("Lcom/rubenmayayo/reddit/ui/fragments/e;", "c3"),
    boostAdFragmentFingerprint("Lcom/rubenmayayo/reddit/ui/fragments/h;", "W2"),
    boostAdFragmentFingerprint("Lcom/rubenmayayo/reddit/ui/fragments/g;", "W2"),
    boostAdFragmentFingerprint("Lcom/rubenmayayo/reddit/ui/fragments/c;", "c3")
)

internal object CommentsMaxAdViewCreateFingerprint : Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/comments/CommentsActivity;",
    strings = listOf("MaxMediation: Create MaxAdView")
)
