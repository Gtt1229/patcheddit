/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.rubenmayayo.reddit.ui.customviews.menu

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

open class MenuOption protected constructor(stub: Unit) : android.os.Parcelable {
    constructor() : this(Unit) { boost114Stub<Unit>() }
    @JvmField var a: Int = boost114Stub()
    @JvmField var b: Int = boost114Stub()
    @JvmField var z: String? = boost114Stub()
    @JvmField var H: Boolean = boost114Stub()
    @JvmField var y: Int = boost114Stub()
    @JvmField var D: Boolean = boost114Stub()
    @JvmField var F: java.util.ArrayList<*>? = boost114Stub()
    override fun describeContents(): Int = boost114Stub()
    override fun writeToParcel(p0: android.os.Parcel, p1: Int): Unit = boost114Stub()
}
