/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Modifications Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (0226c2e).
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.giphy

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches
import app.morphe.patches.reddit.customclients.boostforreddit.wrapHook
import app.morphe.patches.all.misc.PatchCategory

private const val COMMENT_VIEW_HOLDER_DESCRIPTOR =
    "Lcom/rubenmayayo/reddit/ui/adapters/CommentViewHolder;"

private const val INLINE_GIPHY_EXTENSION_DESCRIPTOR =
    "Lapp/morphe/extension/boost/giphy/InlineGiphyCommentPreview;"

private const val STABLE_INLINE_PREVIEW_HEIGHT_DESCRIPTOR =
    "Lapp/morphe/extension/boost/giphy/StableInlinePreviewHeight;"

private const val TABLE_TEXT_VIEW_DESCRIPTOR =
    "Lcom/rubenmayayo/reddit/ui/customviews/TableTextView;"

@Suppress("unused")
val inlineGiphyCommentPreviewPatch = bytecodePatch(
    name = "Show inline Giphy previews in comments",
    description = "Adds inline media previews below Boost comment text for Giphy links and direct static image URLs.",
    default = true
) {
    category(PatchCategory.COMMENTS)
    dependsOn(ExtensionPatches.Boost)
    compatibleWith(*AppCompatibility.Boost)

    execute {
        commentViewHolderBindFingerprint.wrapHook(
            before = """
                invoke-static {p0, p1}, $INLINE_GIPHY_EXTENSION_DESCRIPTOR->cleanCommentHtml(Ljava/lang/Object;Ljava/lang/Object;)V
                invoke-static {p0, p1, p5}, $STABLE_INLINE_PREVIEW_HEIGHT_DESCRIPTOR->bind(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
            """,
            after = """
                iget-object v0, p0, $COMMENT_VIEW_HOLDER_DESCRIPTOR->c0:Lcom/rubenmayayo/reddit/models/reddit/CommentModel;
                iget-object v1, p0, $COMMENT_VIEW_HOLDER_DESCRIPTOR->commentTv:$TABLE_TEXT_VIEW_DESCRIPTOR
                invoke-static {p0, v0, v1}, $INLINE_GIPHY_EXTENSION_DESCRIPTOR->applySourceTextPolicyAfterBind(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
            """,
        )

        commentViewHolderCollapseFingerprint.wrapHook(
            after = """
                invoke-static {p0}, $INLINE_GIPHY_EXTENSION_DESCRIPTOR->syncWithCommentState(Ljava/lang/Object;)V
            """,
        )
    }
}
