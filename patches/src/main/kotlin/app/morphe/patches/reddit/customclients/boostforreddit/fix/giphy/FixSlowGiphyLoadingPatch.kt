/*
 * Copyright 2026 wchill.
 * Modifications Copyright 2026 brealorg.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.giphy

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.customclients.AppCompatibility

@Suppress("unused")
val fixSlowGiphyLoadingPatch = bytecodePatch(
    name = "Fix slow Giphy loading",
    description = "Bypasses Boost's slow Giphy API resolver and uses Boost's direct media.giphy.com MP4 fallback for Giphy posts.",
    default = true
) {
    compatibleWith(*AppCompatibility.Boost)

    execute {
        resolveGiphyApiFingerprint.method.addInstructions(
            0,
            """
                invoke-direct { p0 }, Lhe/r;->x()V
                return-void
                """
        )
    }
}
