/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.redgifs

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object CreateOkHttpClientFingerprint : Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/network/redgifs/RedGifsAPIv2;",
    name = "initApiService",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = emptyList(),
    returnType = "Lcom/rubenmayayo/reddit/network/redgifs/RedGifsService;",
)
