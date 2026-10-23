/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Modifications Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (0226c2e).
 */

package app.morphe.patches.reddit.customclients.boostforreddit.debug

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches
import app.morphe.patches.all.misc.PatchCategory

const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/morphe/extension/boost/debug/ExceptionHook;"

@Suppress("unused")
val hookExceptionHandler = bytecodePatch(
    name="Hook exception handler",
    description = "Catches Boost's crashes for debugging. Only enable this for development.",
    default = false
) {
    category(PatchCategory.ADVANCED)
    dependsOn(ExtensionPatches.Boost)
    compatibleWith(*AppCompatibility.Boost)

    execute {
        ExceptionHandlerFingerprint.method.apply {
            addInstructions(
                0,
                """
                    invoke-static { p0, p1 }, $EXTENSION_CLASS_DESCRIPTOR->handleException(Ljava/lang/Throwable;Ljava/lang/String;)V
                """,
            )
        }
    }
}
