/*
 * Modifications Copyright 2026 brealorg.
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (0226c2e).
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.feed

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches
import app.morphe.util.addInstructionsAtControlFlowLabel
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import app.morphe.patches.all.misc.PatchCategory

private const val FILTERED_PAGE_EXTENSION_DESCRIPTOR =
    "Lapp/morphe/extension/boost/utils/BoostFilteredPageAutoLoad;"

private const val THING_FRAGMENT =
    "Lcom/rubenmayayo/reddit/ui/fragments/p;"

// Native "Load next" Snackbar action: calls Callbacks.m0() (next page).
private const val LOAD_NEXT_LISTENER =
    "Lcom/rubenmayayo/reddit/ui/fragments/o;"

private const val SUBMISSION_FRAGMENT =
    "Lcom/rubenmayayo/reddit/ui/fragments/d;"

// ThingFragment.showFilteredNextPageSnackbar(View): shown when content-type
// filters removed every post on a page.
private val filteredNextPageSnackbarFingerprint = Fingerprint(
    definingClass = THING_FRAGMENT,
    name = "q1",
    returnType = "V",
    parameters = listOf("Landroid/view/View;"),
)

@Suppress("unused")
val autoLoadFilteredPagePatch = bytecodePatch(
    name = "Auto-load next page after filtered results",
    description =
        "Loads the next page automatically when content filters hide every post on a page, " +
            "instead of leaving the feed stuck behind a \"Results were filtered\" Snackbar.",
    default = true,
) {
    category(PatchCategory.FEED)
    dependsOn(ExtensionPatches.Boost)
    compatibleWith(*AppCompatibility.Boost)

    execute {
        filteredNextPageSnackbarFingerprint.method.apply {
            // q1 also shows "Review filter". Only replace its Load next branch.
            val nextPageIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.SGET_OBJECT &&
                    getReference<FieldReference>()?.toString() ==
                    "Lcom/google/android/material/snackbar/Snackbar;->E:[I"
            }
            addInstructionsAtControlFlowLabel(
                nextPageIndex,
                """
                    check-cast p0, $SUBMISSION_FRAGMENT
                    new-instance v0, $LOAD_NEXT_LISTENER
                    invoke-direct {v0, p0}, $LOAD_NEXT_LISTENER-><init>($SUBMISSION_FRAGMENT)V
                    invoke-static {p1, v0}, $FILTERED_PAGE_EXTENSION_DESCRIPTOR->onPageFiltered(Landroid/view/View;Landroid/view/View${'$'}OnClickListener;)Z
                    move-result v0
                    if-eqz v0, :morphe_show_native_snackbar
                    return-void
                    :morphe_show_native_snackbar
                    nop
                """,
            )
        }
    }
}
