/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.google.android.exoplayer2

import com.google.android.exoplayer2.source.a as BaseMediaSource
import com.google.android.exoplayer2.d as BasePlayer
import com.google.android.exoplayer2.f as Bundleable
import com.google.android.exoplayer2.o as MediaItem
import com.google.android.exoplayer2.u as Player

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class d protected constructor(stub: Unit) : Player {
}

interface f {
}

abstract class i protected constructor(stub: Unit) : BasePlayer(Unit) {
    fun a(): Unit = boost114Stub()
    fun f0(): Unit = boost114Stub()
    fun i0(p0: BaseMediaSource?): Unit = boost114Stub()
    fun l0(p0: Boolean): Unit = boost114Stub()
    fun o0(p0: Float): Unit = boost114Stub()
    fun q(): Int = boost114Stub()
    fun y(p0: Int): Unit = boost114Stub()
}

abstract class o protected constructor(stub: Unit) : Bundleable {
    companion object {
        @JvmStatic fun a(p0: android.net.Uri?): MediaItem? = boost114Stub()
    }
}

interface u {
}
