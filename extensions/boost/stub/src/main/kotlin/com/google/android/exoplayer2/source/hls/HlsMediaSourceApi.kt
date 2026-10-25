/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.google.android.exoplayer2.source.hls

import com.google.android.exoplayer2.source.a as BaseMediaSource
import l80.a as DataSourceFactory
import com.google.android.exoplayer2.o as MediaItem
import com.google.android.exoplayer2.source.h as MediaSource

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class HlsMediaSource protected constructor(stub: Unit) : BaseMediaSource(Unit) {
    open class Factory protected constructor(stub: Unit) : MediaSource.a {
        constructor(p0: DataSourceFactory?) : this(Unit) { boost114Stub<Unit>() }
        fun b(p0: MediaItem?): com.google.android.exoplayer2.source.hls.HlsMediaSource? = boost114Stub()
    }
}
