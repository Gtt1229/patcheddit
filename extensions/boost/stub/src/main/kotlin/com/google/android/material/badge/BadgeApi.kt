/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.google.android.material.badge

import y73 as TextDrawableHelper

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class BadgeState protected constructor(stub: Unit) {
    @JvmField val a: com.google.android.material.badge.BadgeState.State = boost114Stub()
    @JvmField val b: com.google.android.material.badge.BadgeState.State = boost114Stub()
    fun a(): Boolean = boost114Stub()
    abstract class State protected constructor(stub: Unit) : android.os.Parcelable {
        @JvmField var F: Int = boost114Stub()
        @JvmField var O: Boolean? = boost114Stub()
    }
}

open class a protected constructor(stub: Unit) : android.graphics.drawable.Drawable(), TextDrawableHelper.b {
    constructor(p0: android.content.Context?, p1: com.google.android.material.badge.BadgeState.State?) : this(Unit) { boost114Stub<Unit>() }
    @JvmField val c: TextDrawableHelper = boost114Stub()
    @JvmField val z: com.google.android.material.badge.BadgeState = boost114Stub()
    fun e(): android.widget.FrameLayout? = boost114Stub()
    fun i(): Unit = boost114Stub()
    fun l(): Unit = boost114Stub()
    override fun draw(p0: android.graphics.Canvas): Unit = boost114Stub()
    override fun getOpacity(): Int = boost114Stub()
    override fun setAlpha(p0: Int): Unit = boost114Stub()
    override fun setColorFilter(p0: android.graphics.ColorFilter?): Unit = boost114Stub()
}
