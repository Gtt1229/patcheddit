package app.morphe.patches.reddit.customclients.boostforreddit.fix.slink

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object GetOAuthAccessTokenFingerprint : Fingerprint(
    definingClass = "Lnet/dean/jraw/http/oauth/OAuthData;",
    name = "getAccessToken",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    strings = listOf("access_token"),
)

internal object HandleNavigationFingerprint : Fingerprint(
    definingClass = "Lxy1;",
    name = "o",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;", "Z"),
    strings = listOf(
        "android.intent.action.SEARCH",
        "subscription",
        "sort",
        "period",
        "boostforreddit.com/themes",
    )
)