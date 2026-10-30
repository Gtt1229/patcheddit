package app.morphe.patches.reddit.customclients.sync.syncforreddit.fix.comments

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/morphe/extension/syncforreddit/FixLoadMoreCommentsPatch;"

@Suppress("unused")
val fixLoadMoreCommentsPatch = bytecodePatch(
    name = "Fix load more comments",
    description = "Fixes \"load more comments\" failing since Reddit blocked /api/morechildren for third party " +
        "clients. Sync loads the missing comments from /comments instead. At the bottom of huge threads " +
        "Reddit's 500-comment limit can still leave some out.",
    default = true,
) {
    dependsOn(ExtensionPatches.Sync)

    compatibleWith(*AppCompatibility.SyncForReddit)

    execute {
        // region Pass the parent of the tapped "load more" item to the extension.

        // morechildren only needs the ids, so the request isn't given the parent. Find the Post getter for it.
        val parentIdGetter = PostToStringFingerprint.method.let { toString ->
            val labelIndex = toString.indexOfFirstInstructionOrThrow {
                getReference<StringReference>()?.string == ", parent_id="
            }
            val field = toString.getInstruction(
                toString.indexOfFirstInstructionOrThrow(labelIndex) { opcode == Opcode.IGET_OBJECT }
            ).getReference<FieldReference>()!!

            PostToStringFingerprint.classDef.methods.singleOrNull { method ->
                !AccessFlags.STATIC.isSet(method.accessFlags) &&
                    method.parameterTypes.isEmpty() &&
                    method.returnType == "Ljava/lang/String;" &&
                    method.instructions.count() == 2 &&
                    method.instructions.first().let {
                        it.opcode == Opcode.IGET_OBJECT && it.getReference<FieldReference>() == field
                    }
            } ?: throw PatchException("Could not find the Post parent_id getter")
        }

        RequestMoreCommentsFingerprint.method.apply {
            val postType = parameterTypes.single().toString()

            addInstructions(
                0,
                """
                    invoke-virtual { p1 }, $postType->${parentIdGetter.name}()Ljava/lang/String;
                    move-result-object v0
                    invoke-static { v0 }, $EXTENSION_CLASS_DESCRIPTOR->patchSetParent(Ljava/lang/String;)V
                """,
            )
        }

        // endregion

        // region Request the parent's subtree from /comments with GET instead of POSTing to morechildren.

        MoreCommentsRequestConstructorFingerprint.method.apply {
            // p3 is the link id, p5 the comma separated ids to load and p8 the comment sort.
            // v0 to v2 are only written to later.
            addInstructions(
                0,
                """
                    move-object/from16 v0, p3
                    move-object/from16 v1, p5
                    move-object/from16 v2, p8
                    invoke-static { v0, v1, v2 }, $EXTENSION_CLASS_DESCRIPTOR->patchSetRequest(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
                """,
            )

            // The superclass constructor: (Context, int method, String url, Listener, ErrorListener).
            val superclass = MoreCommentsRequestConstructorFingerprint.classDef.superclass
            val superIndex = indexOfFirstInstructionOrThrow {
                val reference = getReference<MethodReference>()
                (opcode == Opcode.INVOKE_DIRECT || opcode == Opcode.INVOKE_DIRECT_RANGE) &&
                    reference?.definingClass == superclass &&
                    reference.name == "<init>" &&
                    reference.parameterTypes.size == 5
            }
            val (methodRegister, urlRegister) = getInstruction(superIndex).let {
                when (it) {
                    is RegisterRangeInstruction -> it.startRegister + 2 to it.startRegister + 3
                    is FiveRegisterInstruction -> it.registerE to it.registerF
                    else -> throw PatchException("Unexpected superclass constructor call")
                }
            }
            if (methodRegister > 15 || urlRegister > 15) {
                throw PatchException("Unexpected superclass constructor registers")
            }

            addInstructions(
                superIndex,
                """
                    invoke-static { v$urlRegister }, $EXTENSION_CLASS_DESCRIPTOR->patchMoreUrl(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$urlRegister
                    invoke-static { v$urlRegister, v$methodRegister }, $EXTENSION_CLASS_DESCRIPTOR->patchMoreMethod(Ljava/lang/String;I)I
                    move-result v$methodRegister
                """,
            )
        }

        // endregion

        // region Convert the /comments reply into the morechildren reply Sync parses.

        MoreCommentsParseNetworkResponseFingerprint.method.apply {
            // The response body is decoded with new String(response.data) before it's parsed.
            val stringIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_DIRECT &&
                    getReference<MethodReference>()?.toString() == "Ljava/lang/String;-><init>([B)V"
            }
            val string = getInstruction<FiveRegisterInstruction>(stringIndex)
            val bodyRegister = string.registerC
            // Held response.data, which isn't read again.
            val tempRegister = string.registerD

            addInstructions(
                stringIndex + 1,
                """
                    invoke-virtual/range { p0 .. p0 }, Lcom/android/volley/Request;->getUrl()Ljava/lang/String;
                    move-result-object v$tempRegister
                    invoke-static { v$bodyRegister, v$tempRegister }, $EXTENSION_CLASS_DESCRIPTOR->patchMoreResponse(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$bodyRegister
                """,
            )
        }

        // endregion
    }
}
