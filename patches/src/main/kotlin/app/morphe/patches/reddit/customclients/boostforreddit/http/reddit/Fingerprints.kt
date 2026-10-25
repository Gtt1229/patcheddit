/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Modifications Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 */

package app.morphe.patches.reddit.customclients.boostforreddit.http.reddit

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Badge construction is inlined into the binders.
internal object SetCommentBabushkaTextFingerprint : Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/adapters/CommentViewHolder;",
    name = "w",
    returnType = "V",
    parameters = listOf(
        "Lcom/rubenmayayo/reddit/models/reddit/CommentModel;",
        "Z", "Z", "Z", "Lig2;",
    ),
)

internal object SetSubmissionBabushkaTextFingerprint : Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/adapters/SubmissionViewHolder;",
    name = "w",
    returnType = "V",
    parameters = listOf(
        "Lcom/rubenmayayo/reddit/models/reddit/SubmissionModel;",
        "Z", "Z", "Z", "Lig2;",
    ),
)

internal object ContributionModelConstructorFingerprint : Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/models/reddit/ContributionModel;",
    name = "<init>",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf("Landroid/os/Parcel;"),
    returnType = "V",
)

internal object ContributionModelWriteToParcelFingerprint : Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/models/reddit/ContributionModel;",
    name = "writeToParcel",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
)

internal object SubmissionModelDeserializeFingerprint : Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/models/reddit/SubmissionModel;",
    name = "C",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Lcom/rubenmayayo/reddit/models/reddit/SubmissionModel;",
    parameters = listOf("Lnet/dean/jraw/models/Submission;"),
)

internal object CommentModelDeserializeFingerprint : Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/models/reddit/CommentModel;",
    name = "y",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Lcom/rubenmayayo/reddit/models/reddit/CommentModel;",
    parameters = listOf("Lnet/dean/jraw/models/Comment;"),
)

internal object StateSubmissionViewSetStateFingerprint : Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/customviews/StateSubmissionView;",
    name = "setState",
    returnType = "V",
    parameters = listOf("Lcom/rubenmayayo/reddit/models/reddit/SubmissionModel;"),
    strings = listOf("reddit", "copyright_takedown"),
)

internal object GlideHttpUrlFetcherFetchFingerprint : Fingerprint(
    definingClass = "Lm81;",
    name = "f",
    returnType = "Ljava/io/InputStream;",
    parameters = listOf("Ljava/net/URL;", "I", "Ljava/net/URL;", "Ljava/util/Map;"),
)
