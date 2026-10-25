/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.google.android.exoplayer2.source

import com.google.android.exoplayer2.source.a as BaseMediaSource
import l80.a as DataSourceFactory
import sb0 as DefaultExtractorsFactory
import com.google.android.exoplayer2.o as MediaItem
import com.google.android.exoplayer2.source.h as MediaSource

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class a protected constructor(stub: Unit) : MediaSource {
}

interface h {
    interface a {
    }
}

abstract class m protected constructor(stub: Unit) : BaseMediaSource(Unit) {
    open class b protected constructor(stub: Unit) : MediaSource.a {
        constructor(p0: DataSourceFactory?, p1: DefaultExtractorsFactory?) : this(Unit) { boost114Stub<Unit>() }
        fun a(p0: MediaItem?): MediaSource? = boost114Stub()
    }
}
