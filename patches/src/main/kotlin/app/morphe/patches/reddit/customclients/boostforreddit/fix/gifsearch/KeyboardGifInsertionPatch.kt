/*
 * Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.gifsearch

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches

@Suppress("unused")
val supportKeyboardGifInsertionPatch = bytecodePatch(
    name = "Support keyboard GIF insertion",
    description = "Enables keyboard GIF/image rich content insertion through Android receive-content when a public URL is available.",
    default = false
) {
    dependsOn(ExtensionPatches.Boost)
    compatibleWith(*AppCompatibility.Boost)

    execute {
        formattingBarSetEditTextFingerprint.method.addInstructions(
            0,
            """
                invoke-static {p1}, Lapp/morphe/extension/boost/gifsearch/ImeGifInputConnection;->enableForEditText(Landroid/widget/EditText;)V
            """.trimIndent()
        )
    }
}
