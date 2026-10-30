/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */

package app.morphe.patches.reddit.customclients.sync.syncforreddit.api

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

internal object GetAuthorizationStringFingerprint : Fingerprint(
    // This is evaluated after replaceStringPatch, so the modified string is used for matching.
    strings = listOf("https://www.reddit.com/api/v1/authorize.compact?client_id=Q43fSpTe8LckEg&response_type=code&state=")
)

// Fingerprint strings must match exactly, but these are only part of the app's strings.
private fun Method.containsString(string: String) = implementation?.instructions?.any {
    ((it as? ReferenceInstruction)?.reference as? StringReference)?.string?.contains(string) == true
} == true

internal object GetBearerTokenFingerprint : Fingerprint(
    custom = { method, classDef ->
        method.containsString("Basic ") &&
            classDef.methods.any { it.containsString("authorize.compact?client_id") }
    }
)

internal object GetUserAgentFingerprint : Fingerprint(
    custom = { method, _ -> method.containsString("android:com.laurencedawson.reddit_sync") }
)

internal object ImgurImageAPIFingerprint : Fingerprint(
    strings = listOf("https://imgur-apiv3.p.rapidapi.com/3/image")
)

internal object GetRedirectUriFingerprint : Fingerprint(
    strings = listOf("http://redditsync/auth")
)
