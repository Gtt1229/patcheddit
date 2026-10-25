/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.google.android.exoplayer2.ui

import com.google.android.exoplayer2.u as Player

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

open class PlayerView protected constructor(stub: Unit) : android.widget.FrameLayout(boost114Stub<android.content.Context>()) {
    constructor(p0: android.content.Context?, p1: android.util.AttributeSet?) : this(Unit) { boost114Stub<Unit>() }
    open fun setPlayer(p0: Player?): Unit = boost114Stub()
    open fun setResizeMode(p0: Int): Unit = boost114Stub()
    open fun setShutterBackgroundColor(p0: Int): Unit = boost114Stub()
    open fun setUseController(p0: Boolean): Unit = boost114Stub()
}
