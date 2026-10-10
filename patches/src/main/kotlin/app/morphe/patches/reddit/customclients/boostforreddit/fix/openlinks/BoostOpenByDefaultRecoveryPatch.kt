/*
 * Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.openlinks

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches

private const val OPEN_BY_DEFAULT_RECOVERY_DESCRIPTOR =
    "Lapp/morphe/extension/boost/utils/OpenByDefaultRecovery;"

private val boostApplicationOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/MyApplication;",
    name = "onCreate",
)

@Suppress("unused")
val boostOpenByDefaultRecoveryPatch = bytecodePatch(
    name = "Restore Boost Open by default prompt",
    description = "Prompts after Boost patch updates to reopen Android's Open by default settings so supported Reddit links can be re-enabled.",
    default = true,
) {
    dependsOn(ExtensionPatches.Boost)
    compatibleWith(*AppCompatibility.Boost)

    execute {
        boostApplicationOnCreateFingerprint.method.addInstructions(
            1,
            """
                invoke-static {p0}, $OPEN_BY_DEFAULT_RECOVERY_DESCRIPTOR->install(Landroid/app/Application;)V
            """.trimIndent(),
        )
    }
}
