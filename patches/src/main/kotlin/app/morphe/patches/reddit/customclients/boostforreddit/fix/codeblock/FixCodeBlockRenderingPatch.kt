/*
 * Copyright 2026 brealorg.
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (0226c2e).
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.codeblock

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches
import app.morphe.patches.all.misc.PatchCategory
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val CODEBLOCK_NORMALIZER_DESCRIPTOR =
    "Lapp/morphe/extension/boost/codeblock/CodeBlockHtmlNormalizer;"

@Suppress("unused")
val fixCodeBlockRenderingPatch = bytecodePatch(
    name = "Fix Boost code block rendering",
    description = "Shows Reddit code blocks correctly and fixes links whose URL contains parentheses.",
    default = true
) {
    category(PatchCategory.COMMENTS)
    dependsOn(ExtensionPatches.Boost)
    compatibleWith(*AppCompatibility.Boost)

    execute {
        boostHtmlSanitizerFingerprint.method.apply {
            val codeIndex = boostHtmlSanitizerFingerprint.stringMatches.single {
                it.string == "<code>"
            }.index
            val sanitizerIndex = implementation!!.instructions.withIndex().first { (index, instruction) ->
                index > codeIndex &&
                    instruction.getReference<MethodReference>()?.toString() ==
                    "Ljava/lang/String;->replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"
            }.index
            val htmlRegister = getInstruction<FiveRegisterInstruction>(sanitizerIndex).registerC
            addInstructions(
                sanitizerIndex,
                """
                    invoke-static {v$htmlRegister}, $CODEBLOCK_NORMALIZER_DESCRIPTOR->normalize(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$htmlRegister
                """,
            )
        }

        boostTableTextViewSetTextHtmlFingerprint.method.addInstructions(
            0,
            """
                invoke-static/range {p1 .. p1}, $CODEBLOCK_NORMALIZER_DESCRIPTOR->normalize(Ljava/lang/String;)Ljava/lang/String;
                move-result-object p1
                """
        )

        boostLinkTextViewSetTextHtmlParsedFingerprint.method.addInstructions(
            0,
            """
                invoke-static/range {p1 .. p1}, $CODEBLOCK_NORMALIZER_DESCRIPTOR->normalize(Ljava/lang/String;)Ljava/lang/String;
                move-result-object p1
            """,
        )

        // Other patches can wrap w(). Locate the one native body containing the
        // self-preview conversion, rather than injecting into its wrapper.
        boostSubmissionViewHolderSelfPreviewHtmlFingerprint.classDef.methods.single { candidate ->
            candidate.parameters.map { it.type } ==
                boostSubmissionViewHolderSelfPreviewHtmlFingerprint.method.parameters.map { it.type } &&
                candidate.implementation?.instructions?.any {
                    it.getReference<MethodReference>()?.toString() ==
                        "Landroid/text/Html;->fromHtml(Ljava/lang/String;)Landroid/text/Spanned;"
                } == true
        }.apply {
            val htmlIndex = indexOfFirstInstructionOrThrow {
                getReference<MethodReference>()?.toString() ==
                    "Landroid/text/Html;->fromHtml(Ljava/lang/String;)Landroid/text/Spanned;"
            }
            val htmlRegister = getInstruction<FiveRegisterInstruction>(htmlIndex).registerC
            addInstructions(
                htmlIndex,
                """
                    invoke-static {v$htmlRegister}, $CODEBLOCK_NORMALIZER_DESCRIPTOR->normalize(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$htmlRegister
                """,
            )
        }
    }
}
