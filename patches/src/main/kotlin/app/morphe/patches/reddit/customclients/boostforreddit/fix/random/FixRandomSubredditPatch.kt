/*
 * Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.random

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

private const val RESOLVER =
    "Lapp/morphe/extension/boost/subreddit/RandomSubredditResolver;"

private const val SUBSCRIPTION_VIEW_MODEL =
    "Lcom/rubenmayayo/reddit/models/reddit/SubscriptionViewModel;"

@Suppress("unused")
val fixRandomSubredditPatch = bytecodePatch(
    name = "Fix Random subreddit",
    description = "Normalizes Boost\'s broken r/random route by resolving a live non-NSFW subreddit through Reddit search and subscriber-count filtering.",
    default = true
) {
    dependsOn(ExtensionPatches.Boost)
    compatibleWith(*AppCompatibility.Boost)

    execute {
        subredditActivityIntentSubredditExtraFingerprint.method.apply {
            val subredditKeyIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.CONST_STRING &&
                    getReference<StringReference>()?.string == "subreddit"
            }

            val castIndex = indexOfFirstInstructionOrThrow(subredditKeyIndex) {
                opcode == Opcode.CHECK_CAST &&
                    getReference<TypeReference>()?.type == SUBSCRIPTION_VIEW_MODEL
            }

            val register = getInstruction<OneRegisterInstruction>(castIndex).registerA

            addInstructions(
                castIndex + 1,
                """
                    invoke-static {v$register}, $RESOLVER->normalize(Ljava/lang/Object;)Ljava/lang/Object;
                    move-result-object v$register
                    check-cast v$register, $SUBSCRIPTION_VIEW_MODEL
                    """
            )
        }
    }
}
