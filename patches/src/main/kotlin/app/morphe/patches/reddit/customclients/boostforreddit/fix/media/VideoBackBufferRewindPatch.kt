/*
 * Modifications Copyright 2026 brealorg.
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (0226c2e).
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.media

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import com.android.tools.smali.dexlib2.Opcode
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import app.morphe.patches.all.misc.PatchCategory

private const val CONTRACT_MARKER =
    "MORPHE_BOOST_VIDEO_REWIND_BACKBUFFER_ISSUE188_V2"

// The getter is inlined in 1.14.0; initialize its microsecond field instead.
private val backBufferDurationUsFingerprint = Fingerprint(
    definingClass = "Lhc0;",
    name = "<init>",
    returnType = "V",
    parameters = emptyList(),
    strings = listOf("backBufferDurationMs"),
)

@Suppress("unused")
val fixBoostVideoRewindBackBufferPatch = bytecodePatch(
    name = "Fix Boost video rewind back buffer",
    description =
        "Keeps the last 30 seconds of a video, so rewinding plays right away instead of reloading.",
    default = true,
) {
    category(PatchCategory.MEDIA)
    compatibleWith(*AppCompatibility.Boost)

    execute {
        check(CONTRACT_MARKER.endsWith("ISSUE188_V2"))

        backBufferDurationUsFingerprint.method.apply {
            val assignmentIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.IPUT_WIDE &&
                    getReference<FieldReference>()?.toString() == "Lhc0;->f:J"
            }
            val durationRegister = getInstruction<TwoRegisterInstruction>(assignmentIndex).registerA
            addInstructions(
                assignmentIndex,
                "const-wide/32 v$durationRegister, 0x1c9c380",
            )
        }
    }
}
