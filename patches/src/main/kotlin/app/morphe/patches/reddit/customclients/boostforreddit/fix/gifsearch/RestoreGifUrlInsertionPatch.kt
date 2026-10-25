/*
 * Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (0226c2e).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.gifsearch

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches
import app.morphe.patches.all.misc.PatchCategory
import app.morphe.patches.reddit.customclients.boostforreddit.wrapHook
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

@Suppress("unused")
val restoreGifUrlInsertionPatch = bytecodePatch(
    name = "Restore GIF URL insertion",
    description = "Adds an option to the image menu to insert a GIF from its URL.",
    default = false
) {
    category(PatchCategory.COMMENTS)
    dependsOn(ExtensionPatches.Boost)
    compatibleWith(*AppCompatibility.Boost)

    execute {
        val imageMenuMethod = formattingBarImageMenuOptionsFingerprint.method
        val menuViewIndex = imageMenuMethod.indexOfFirstInstructionOrThrow {
            opcode == Opcode.NEW_INSTANCE &&
                getReference<TypeReference>()?.type == "Lcom/rubenmayayo/reddit/ui/customviews/menu/MenuView;"
        }

        imageMenuMethod.addInstructions(
            menuViewIndex,
            """
                new-instance v1, Lcom/rubenmayayo/reddit/ui/customviews/menu/MenuOption;
                invoke-direct {v1}, Lcom/rubenmayayo/reddit/ui/customviews/menu/MenuOption;-><init>()V
                const v2, 0x7f0a0273
                iput v2, v1, Lcom/rubenmayayo/reddit/ui/customviews/menu/MenuOption;->a:I
                const v2, 0x7f1306dd
                iput v2, v1, Lcom/rubenmayayo/reddit/ui/customviews/menu/MenuOption;->y:I
                const v2, 0x7f0801e8
                iput v2, v1, Lcom/rubenmayayo/reddit/ui/customviews/menu/MenuOption;->b:I
                invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            """
        )

        formattingBarHandleImageMenuOptionFingerprint.wrapHook(
            before = """
                iget v0, p1, Lcom/rubenmayayo/reddit/ui/customviews/menu/MenuOption;->a:I
                const v1, 0x7f0a0273
                if-ne v0, v1, :morphe_native_image_menu
                iget-object v0, p0, Lzy0;->a:Lcom/rubenmayayo/reddit/ui/customviews/FormattingBar;
                invoke-static {v0}, Lapp/morphe/extension/boost/gifsearch/GifUrlInsertion;->open(Ljava/lang/Object;)V
                iget-object v0, v0, Lcom/rubenmayayo/reddit/ui/customviews/FormattingBar;->A:Ldp1;
                if-eqz v0, :morphe_gif_menu_closed
                invoke-virtual {v0}, Ldp1;->dismiss()V
                :morphe_gif_menu_closed
                return-void
                :morphe_native_image_menu
            """,
        )
    }
}
