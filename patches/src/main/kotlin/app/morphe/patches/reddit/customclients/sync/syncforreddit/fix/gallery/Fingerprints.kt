package app.morphe.patches.reddit.customclients.sync.syncforreddit.fix.gallery

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object GalleryUrlFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Ljava/lang/String;",
    parameters = listOf("Ljava/lang/String;"),
    strings = listOf("https://oauth.reddit.com/api/info.json?id=t3_"),
    custom = { _, classDef -> classDef.sourceFile == "OAuthGalleryRequest.java" }
)

internal object GalleryParseNetworkResponseFingerprint : Fingerprint(
    name = "parseNetworkResponse",
    strings = listOf("Could not load gallery"),
    custom = { _, classDef -> classDef.sourceFile == "OAuthGalleryRequest.java" }
)
