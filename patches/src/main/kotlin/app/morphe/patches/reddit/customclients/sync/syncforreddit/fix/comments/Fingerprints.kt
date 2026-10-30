package app.morphe.patches.reddit.customclients.sync.syncforreddit.fix.comments

import app.morphe.patcher.Fingerprint

private const val MORE_REQUEST_SOURCE_FILE = "OAuthCommentsMoreRequest.java"

// CommentsPresenterImpl's handler for tapping a "load more comments" item, called with that item (a Post).
internal object RequestMoreCommentsFingerprint : Fingerprint(
    returnType = "V",
    strings = listOf("Requesting more: "),
    custom = { method, classDef ->
        classDef.sourceFile == "CommentsPresenterImpl.java" && method.parameterTypes.size == 1
    }
)

// The AutoValue Post's toString(), used to find which field holds the parent_id.
internal object PostToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    strings = listOf(", parent_id="),
    custom = { _, classDef -> classDef.sourceFile == "\$\$AutoValue_Post.java" }
)

internal object MoreCommentsRequestConstructorFingerprint : Fingerprint(
    name = "<init>",
    parameters = listOf(
        "Landroid/content/Context;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "I",
        "I",
        "Ljava/lang/String;",
        "Lcom/android/volley/Response\$Listener;",
        "Lcom/android/volley/Response\$ErrorListener;",
    ),
    strings = listOf("api/morechildren?always_show_media=1&profile_img=true"),
    custom = { _, classDef -> classDef.sourceFile == MORE_REQUEST_SOURCE_FILE }
)

internal object MoreCommentsParseNetworkResponseFingerprint : Fingerprint(
    name = "parseNetworkResponse",
    strings = listOf("things"),
    custom = { _, classDef -> classDef.sourceFile == MORE_REQUEST_SOURCE_FILE }
)
