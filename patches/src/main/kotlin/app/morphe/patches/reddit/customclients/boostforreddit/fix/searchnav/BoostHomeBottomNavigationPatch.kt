/*
 * Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (0226c2e).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.searchnav

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches
import com.android.tools.smali.dexlib2.Opcode
import app.morphe.patches.all.misc.PatchCategory

private const val HOME_NAV_EXTENSION_DESCRIPTOR =
    "Lapp/morphe/extension/boost/utils/BoostSearchBottomNavigation;"

@Suppress("unused")
val boostHomeBottomNavigationPatch = bytecodePatch(
    name = "Standardize Boost Home bottom navigation",
    description = "Shows the same five-tab bottom navigation on Home, with Home highlighted.",
    default = true,
) {
    category(PatchCategory.NAVIGATION)
    compatibleWith(*AppCompatibility.Boost)
    dependsOn(ExtensionPatches.Boost)

    execute {
        mainActivityOnResumeFingerprint.method.apply {
            val returnIndices =
                implementation!!.instructions
                    .withIndex()
                    .filter { (_, instruction) ->
                        instruction.opcode == Opcode.RETURN_VOID
                    }
                    .map { (index, _) -> index }

            require(returnIndices.isNotEmpty()) {
                "MainActivity.onResume has no RETURN_VOID instruction"
            }

            returnIndices.asReversed().forEach { returnIndex ->
                addInstructions(
                    returnIndex,
                    """
                        invoke-static {p0}, $HOME_NAV_EXTENSION_DESCRIPTOR->standardizeHome(Landroid/app/Activity;)V
                    """.trimIndent(),
                )
            }
        }

        inboxCountUpdateFingerprint.method.apply {
            addInstructions(
                0,
                """
                    invoke-static {p0, p1}, $HOME_NAV_EXTENSION_DESCRIPTOR->syncInboxBadge(Landroid/app/Activity;I)V
                """.trimIndent(),
            )
        }
    }
}
