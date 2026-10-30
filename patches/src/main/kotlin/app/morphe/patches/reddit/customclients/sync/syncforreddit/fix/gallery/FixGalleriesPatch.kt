package app.morphe.patches.reddit.customclients.sync.syncforreddit.fix.gallery

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import app.morphe.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/morphe/extension/syncforreddit/FixGalleriesPatch;"

@Suppress("unused")
val fixGalleriesPatch = bytecodePatch(
    name = "Fix galleries",
    description = "Fixes Reddit galleries failing to load since Reddit blocked /api/info for third party " +
        "clients. Sync loads the post from /comments instead.",
    default = true,
) {
    dependsOn(ExtensionPatches.Sync)

    compatibleWith(*AppCompatibility.SyncForReddit)

    execute {
        // region Request the post from /comments instead of /api/info.

        GalleryUrlFingerprint.method.apply {
            val returnIndex = indexOfFirstInstructionReversedOrThrow { opcode == Opcode.RETURN_OBJECT }
            val urlRegister = getInstruction<OneRegisterInstruction>(returnIndex).registerA

            addInstructions(
                returnIndex,
                """
                    invoke-static { v$urlRegister }, $EXTENSION_CLASS_DESCRIPTOR->patchGalleryUrl(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$urlRegister
                """,
            )
        }

        // endregion

        // region Convert the /comments reply back into the /api/info listing.

        GalleryParseNetworkResponseFingerprint.method.apply {
            // The response body is decoded with new String(response.data) before it's parsed.
            val stringIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_DIRECT &&
                    getReference<MethodReference>()?.toString() == "Ljava/lang/String;-><init>([B)V"
            }
            val bodyRegister = getInstruction<FiveRegisterInstruction>(stringIndex).registerC

            addInstructions(
                stringIndex + 1,
                """
                    invoke-static { v$bodyRegister }, $EXTENSION_CLASS_DESCRIPTOR->patchGalleryResponse(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$bodyRegister
                """,
            )
        }

        // endregion
    }
}
