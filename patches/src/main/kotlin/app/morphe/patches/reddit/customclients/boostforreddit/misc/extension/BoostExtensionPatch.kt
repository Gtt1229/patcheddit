/*
 * Copyright 2026 wchill.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.misc.extension

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.morphe.patches.all.misc.extension.SHARED_UTILS_EXTENSION_CLASS
import app.morphe.patches.all.misc.extension.activityOnCreateExtensionHook
import app.morphe.patches.reddit.customclients.boostforreddit.misc.extension.mapping.verifyBoostLibraryReferences
import app.morphe.util.returnEarly
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import java.net.JarURLConnection

internal val boostExtensionPatch = bytecodePatch {
    dependsOn(boostNativeLibrariesPatch, boostPrivateLibraryResourcesPatch)
    extendWith {
        val input = checkNotNull(object {}.javaClass.classLoader.getResourceAsStream("extensions/boost.mpe")) {
            "Boost extension payload was not found"
        }
        prepareBoostExtension(input)
    }

    execute {
        classDefBy(SHARED_UTILS_EXTENSION_CLASS)
        val factory = Fingerprint(
            definingClass = "Lfl1;",
            name = "g",
            parameters = listOf("Ljava/lang/String;"),
            returnType = "Lcs1;"
        )
        val original = factory.method
        val bridgeName = "morpheCreateOriginalUploader"
        require(factory.classDef.methods.none { it.name == bridgeName }) {
            "Boost already contains the original uploader bridge"
        }

        // Preserve R8's constructor-inlined allocations before the upload patch replaces g().
        factory.classDef.methods.add(
            ImmutableMethod(
                original.definingClass,
                bridgeName,
                original.parameters,
                original.returnType,
                AccessFlags.PUBLIC.value or AccessFlags.STATIC.value or AccessFlags.SYNTHETIC.value,
                original.annotations,
                original.hiddenApiRestrictions,
                MutableMethodImplementation(
                    requireNotNull(original.implementation) { "Boost uploader factory has no implementation" }
                )
            ).toMutable()
        )
    }

    finalize {
        activityOnCreateExtensionHook("Lcom/rubenmayayo/reddit/MyApplication;")(SHARED_UTILS_EXTENSION_CLASS)
        val marker = object {}.javaClass
        val resource = checkNotNull(marker.classLoader.getResource(marker.name.replace('.', '/') + ".class")) {
            "Cannot locate the patch bundle for its version"
        }
        val connection = resource.openConnection()
        require(connection is JarURLConnection) { "Boost patches are not running from a patch bundle" }
        val version = checkNotNull(connection.manifest.mainAttributes.getValue("Version")) {
            "Patch bundle has no version"
        }
        Fingerprint(
            definingClass = SHARED_UTILS_EXTENSION_CLASS,
            name = "getPatchesReleaseVersion",
            parameters = emptyList(),
            returnType = "Ljava/lang/String;",
        ).method.returnEarly(version)
        val classes = buildList { classDefForEach(::add) }
        val nativeTypes = BoostNativeLibraries.classes.mapTo(HashSet()) { it.type }
        val extensionTypes = classes.filter { it.type !in nativeTypes }.mapTo(HashSet()) { it.type }
        verifyBoostLibraryReferences(classes, extensionTypes)
    }
}
