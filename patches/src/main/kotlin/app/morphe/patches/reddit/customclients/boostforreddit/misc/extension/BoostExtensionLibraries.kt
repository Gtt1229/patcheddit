/*
 * Copyright 2026 wchill.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.misc.extension

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.reddit.customclients.boostforreddit.bindings.BoostBindings
import app.morphe.patches.reddit.customclients.boostforreddit.misc.extension.mapping.DexMappingRewriter
import app.morphe.patches.reddit.customclients.boostforreddit.misc.extension.mapping.ProguardMapping
import app.morphe.patches.reddit.customclients.boostforreddit.misc.extension.mapping.forEachReference

import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import com.android.tools.smali.dexlib2.writer.io.MemoryDataStore
import com.android.tools.smali.dexlib2.writer.pool.DexPool
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.logging.Logger

private const val PRIVATE_LIBRARY_PACKAGE = "app.morphe.extension.boost.libraries"
private val privateLibraryTypePrefix = "L${PRIVATE_LIBRARY_PACKAGE.replace('.', '/')}/"
private val coroutineServices = mapOf(
    "kotlinx.coroutines.internal.MainDispatcherFactory" to "kotlinx.coroutines.android.AndroidDispatcherFactory",
    "kotlinx.coroutines.CoroutineExceptionHandler" to "kotlinx.coroutines.android.AndroidExceptionPreHandler",
)

internal object BoostNativeLibraries {
    var classes: List<ClassDef> = emptyList()

    val mapping: ProguardMapping by lazy {
        val input = checkNotNull(javaClass.classLoader.getResourceAsStream("boost/mapping.txt")) {
            "Bundled Boost mapping.txt was not found"
        }
        input.reader().use(ProguardMapping::read)
    }
}

internal val boostNativeLibrariesPatch = bytecodePatch(default = false) {
    execute {
        BoostNativeLibraries.classes = buildList { classDefForEach(::add) }
    }
}

internal val boostPrivateLibraryResourcesPatch = resourcePatch(default = false) {
    execute {
        for ((service, provider) in coroutineServices) {
            this["META-INF/services/$PRIVATE_LIBRARY_PACKAGE.$service", false]
                .writeText("$PRIVATE_LIBRARY_PACKAGE.$provider\n")
        }
    }
}

internal fun prepareBoostExtension(input: InputStream): InputStream {
    val dex = input.use { DexBackedDexFile.fromInputStream(null, it.buffered()) }
    val sourceDefinitions = dex.classes.toList()
    val privatePrefixes = listOf(
        "Landroidx/room/", "Landroidx/sqlite/", "Landroidx/arch/", "Landroidx/collection/",
        "Lkotlin/", "Lkotlinx/coroutines/",
        "Lcom/fasterxml/jackson/",
    )
    // Keep callback interfaces and singleton values compatible with calls into Boost.
    val sharedKotlinTypes = setOf(
        "Lkotlin/Function;", "Lkotlin/Unit;", "Lkotlin/jvm/internal/DefaultConstructorMarker;",
    )
    val privateNames = sourceDefinitions.filter { cls ->
        privatePrefixes.any(cls.type::startsWith) && cls.type !in sharedKotlinTypes &&
            !cls.type.startsWith("Lkotlin/jvm/functions/")
    }
        .associate { cls ->
            cls.type to "$privateLibraryTypePrefix${cls.type.removePrefix("L")}"
        }
    val isolation = DexMappingRewriter(
        ProguardMapping.classNames(privateNames), sourceDefinitions, resolveReflectiveFields = false,
    )
    val definitions = isolation.rewrite(sourceDefinitions)
    val libraries = BoostNativeLibraries.mapping
        .withNestedClasses(BoostNativeLibraries.classes.map { it.type })
        .reversed()
    val mapping = BoostBindings.table.mapping(libraries)
        .withNestedClasses(definitions.map { it.type })
    val adapterDefinitions = BoostBindings.table.sourceDefinitions(BoostNativeLibraries.classes, libraries)
    val nativeTypes = BoostNativeLibraries.classes.mapTo(HashSet()) { it.type }
    check(nativeTypes.isNotEmpty()) { "Boost extension loaded before native library preparation" }
    val kept = definitions.filterNot { mapping.type(it.type) in nativeTypes }
    val rewriter = DexMappingRewriter(mapping, adapterDefinitions + definitions)
    val rewritten = reachableExtensionClasses(rewriter.rewrite(kept))
    val pool = DexPool(dex.opcodes)
    rewritten.forEach(pool::internClass)
    val output = MemoryDataStore()
    pool.writeTo(output)
    Logger.getLogger("BoostExtensionLibraries").info(
        "Using ${definitions.size - kept.size} app-provided library classes; " +
            "binding ${rewritten.size} reachable extension classes to Boost's native API",
    )
    return ByteArrayInputStream(output.data)
}

private fun <T : ClassDef> reachableExtensionClasses(definitions: List<T>): List<T> {
    val classes = definitions.associateBy { it.type }
    // The crash-log dependency injects this entry point after extension preparation.
    val sharedPatchEntryPoints = setOf("Lapp/morphe/extension/shared/patches/CrashLogPatch;")
    val providers = coroutineServices.values.mapTo(HashSet()) {
        "$privateLibraryTypePrefix${it.replace('.', '/')};"
    }
    check(providers.all(classes::containsKey)) {
        "Boost extension is missing private coroutine service providers: ${providers - classes.keys}"
    }
    val pending = ArrayDeque(
        definitions.filter {
            it.type in providers || it.type in sharedPatchEntryPoints ||
                it.type.startsWith("Lapp/morphe/extension/boost/") &&
                !it.type.startsWith(privateLibraryTypePrefix)
        }.map { it.type }
    )
    val visited = mutableSetOf<String>()
    fun type(value: String) {
        val element = value.trimStart('[')
        if (element in classes && element !in visited) pending.add(element)
    }
    while (pending.isNotEmpty()) {
        val name = pending.removeFirst()
        if (!visited.add(name)) continue
        val cls = classes[name] ?: continue
        cls.forEachReference { reference ->
            when (reference) {
                is TypeReference -> type(reference.type)
                is StringReference -> {
                    type(reference.string)
                    type("L${reference.string.replace('.', '/')};")
                }
            }
        }
    }
    return definitions.filter { it.type in visited }
}
