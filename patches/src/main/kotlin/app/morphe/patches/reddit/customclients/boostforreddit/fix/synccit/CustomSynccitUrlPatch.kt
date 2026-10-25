/*
 * Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (0226c2e).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.synccit

import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.util.getReference
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import app.morphe.patches.all.misc.PatchCategory

@Suppress("unused")
val customSynccitUrlPatch = bytecodePatch(
    name = "Custom Synccit URL",
    description =
        "Allows Boost to use a custom self-hosted Synccit API endpoint.",
    default = true,
) {
    category(PatchCategory.LOGIN)
    compatibleWith(*AppCompatibility.Boost)

    val synccitUrlOption = stringOption(
        "synccit-url",
        LEGACY_SYNCCIT_API_URL,
        null,
        "Synccit API URL",
        "Full HTTP or HTTPS URL to a Synccit-compatible API endpoint, including api.php.",
        false,
        validator = { value ->
            value != null &&
                value.length in 10..2048 &&
                (
                    value.startsWith("https://") ||
                        value.startsWith("http://")
                ) &&
                '"' !in value &&
                '\n' !in value &&
                '\r' !in value &&
                ' ' !in value
        },
    )

    execute {
        val replacement =
            synccitUrlOption.value
                ?: throw Exception("Missing synccit-url option")

        val fingerprints = listOf(
            "update" to synccitUpdateFingerprint,
            "add" to synccitAddFingerprint,
            "authenticate" to synccitAuthenticateFingerprint,
            "read" to synccitReadFingerprint,
        )

        fingerprints.forEach { (operation, fingerprint) ->
            val method = fingerprint.method
            // Fingerprint anchors also include non-URL strings such as "create".
            val endpoints = method.implementation!!.instructions.withIndex().mapNotNull { (index, instruction) ->
                if (instruction.getReference<StringReference>()?.string == LEGACY_SYNCCIT_API_URL) {
                    index to (instruction as OneRegisterInstruction).registerA
                } else {
                    null
                }
            }

            require(endpoints.isNotEmpty()) {
                "No Synccit endpoint constant found in $operation"
            }

            endpoints.asReversed().forEach { (index, register) ->
                method.replaceInstruction(
                    index,
                    "const-string v$register, \"$replacement\"",
                )
            }
        }
    }
}
