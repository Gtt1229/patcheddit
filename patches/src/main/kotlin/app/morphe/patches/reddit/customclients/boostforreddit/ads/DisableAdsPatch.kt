/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Modifications Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (0226c2e).
 */

package app.morphe.patches.reddit.customclients.boostforreddit.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.util.getReference
import app.morphe.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import org.w3c.dom.Element
import app.morphe.patches.all.misc.PatchCategory

private const val MAX_AD_PLACER_SETTINGS_DESCRIPTOR =
    "Lcom/applovin/mediation/nativeAds/adPlacer/MaxAdPlacerSettings;"

private const val EMPTY_RECYCLER_VIEW_SET_ADAPTER_REFERENCE =
    "Lcom/rubenmayayo/reddit/ui/customviews/EmptyRecyclerView;->setAdapter(Landroidx/recyclerview/widget/RecyclerView\$h;)V"

private val removeBoostAdInitProvidersPatch = resourcePatch(
    name = "Remove Boost ad SDK auto-init providers",
    description = "Removes AppLovin and Google Mobile Ads auto-init providers and metadata.",
    default = false
) {
    compatibleWith(*AppCompatibility.Boost)

    execute {
        document("AndroidManifest.xml").use { document ->
            fun removeElementsByAndroidName(tagName: String, androidName: String) {
                val nodes = document.getElementsByTagName(tagName)
                val matches = (0 until nodes.length)
                    .map { nodes.item(it) as Element }
                    .filter { it.getAttribute("android:name") == androidName }

                matches.forEach { node ->
                    node.parentNode.removeChild(node)
                }
            }

            removeElementsByAndroidName("meta-data", "applovin.sdk.key")
            removeElementsByAndroidName("meta-data", "com.google.android.gms.ads.APPLICATION_ID")
            removeElementsByAndroidName("provider", "com.applovin.sdk.AppLovinInitProvider")
            removeElementsByAndroidName("provider", "com.google.android.gms.ads.MobileAdsInitProvider")
        }
    }
}

@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    name = "Disable ads",
    description = "Removes ads from feeds and comments and keeps the ad SDKs from starting.",
    default = true
) {
    category(PatchCategory.ADS)
    dependsOn(removeBoostAdInitProvidersPatch)
    compatibleWith(*AppCompatibility.Boost)

    execute {
        arrayOf(MaxMediationFingerprint, AdmobMediationFingerprint).forEach { fingerprint ->
            fingerprint.method.returnEarly()
        }

        CommentsMaxAdViewCreateFingerprint.method.returnEarly()

        boostAdFragmentFingerprints.forEach { fingerprint ->
            fingerprint.method.apply {
                val instructions = implementation!!.instructions
                val adBlockStartIndex = instructions.withIndex().first { (_, instruction) ->
                    instruction.opcode == Opcode.NEW_INSTANCE &&
                        instruction.getReference<TypeReference>()?.toString() ==
                        MAX_AD_PLACER_SETTINGS_DESCRIPTOR
                }.index
                val setAdapterIndex = instructions.withIndex().first { (index, instruction) ->
                    index > adBlockStartIndex &&
                        instruction.opcode == Opcode.INVOKE_VIRTUAL &&
                        instruction.getReference<MethodReference>()?.toString() ==
                        EMPTY_RECYCLER_VIEW_SET_ADAPTER_REFERENCE
                }.index

                removeInstructions(adBlockStartIndex, setAdapterIndex - adBlockStartIndex + 1)
                addInstructions(
                    adBlockStartIndex,
                    """
                        iget-object v0, p0, Lcom/rubenmayayo/reddit/ui/fragments/SubmissionRecyclerViewFragment;->mRecyclerView:Lcom/rubenmayayo/reddit/ui/customviews/EmptyRecyclerView;
                        iget-object v1, p0, Lcom/rubenmayayo/reddit/ui/fragments/SubmissionRecyclerViewFragment;->h:Lcom/rubenmayayo/reddit/ui/fragments/SubmissionRecyclerViewFragment${'$'}c0;
                        invoke-virtual {v0, v1}, Lcom/rubenmayayo/reddit/ui/customviews/EmptyRecyclerView;->setAdapter(Landroidx/recyclerview/widget/RecyclerView${'$'}h;)V
                    """
                )
            }
        }
    }
}
