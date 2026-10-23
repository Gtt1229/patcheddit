/*
 * Modifications Copyright 2026 brealorg.
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (0226c2e).
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.flair

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches
import app.morphe.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import app.morphe.patches.all.misc.PatchCategory

private const val FLAIR_CONTRAST_EXTENSION_DESCRIPTOR =
    "Lapp/morphe/extension/boost/utils/BoostFlairContrast;"

// FlairColors(FlairModel, defaultTextColor, defaultBackgroundColor):
// field a = background color, field b = text color.
private const val FLAIR_COLORS =
    "Lcom/rubenmayayo/reddit/ui/customviews/m;"

private val flairColorsConstructorFingerprint = Fingerprint(
    definingClass = FLAIR_COLORS,
    name = "<init>",
    returnType = "V",
    parameters = listOf(
        "Lcom/rubenmayayo/reddit/models/reddit/FlairModel;",
        "I",
        "I",
    ),
)

@Suppress("unused")
val readableFlairLabelsPatch = bytecodePatch(
    name = "Readable Boost flair labels",
    description =
        "Uses black or white flair text when Reddit's flair text color is unreadable on its background.",
    default = true,
) {
    category(PatchCategory.FEED)
    dependsOn(ExtensionPatches.Boost)
    compatibleWith(*AppCompatibility.Boost)

    execute {
        flairColorsConstructorFingerprint.method.apply {
            val returnIndex = indexOfFirstInstructionReversedOrThrow {
                opcode == Opcode.RETURN_VOID
            }

            addInstructions(
                returnIndex,
                """
                    iget p1, p0, $FLAIR_COLORS->b:I
                    iget p2, p0, $FLAIR_COLORS->a:I
                    invoke-static {p1, p2}, $FLAIR_CONTRAST_EXTENSION_DESCRIPTOR->readableTextColor(II)I
                    move-result p1
                    iput p1, p0, $FLAIR_COLORS->b:I
                """,
            )
        }
    }
}
