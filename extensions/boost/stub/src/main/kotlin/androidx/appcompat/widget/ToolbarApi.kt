/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package androidx.appcompat.widget

import ks1 as MenuHost

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class Toolbar protected constructor(stub: Unit) : android.view.ViewGroup(boost114Stub<android.content.Context>()), MenuHost {
    open fun getNavigationIcon(): android.graphics.drawable.Drawable? = boost114Stub()
    open fun getOverflowIcon(): android.graphics.drawable.Drawable? = boost114Stub()
    open fun setNavigationIcon(p0: android.graphics.drawable.Drawable?): Unit = boost114Stub()
    open fun setOverflowIcon(p0: android.graphics.drawable.Drawable?): Unit = boost114Stub()
    open fun setSubtitleTextColor(p0: Int): Unit = boost114Stub()
    open fun setTitleTextColor(p0: Int): Unit = boost114Stub()
}
