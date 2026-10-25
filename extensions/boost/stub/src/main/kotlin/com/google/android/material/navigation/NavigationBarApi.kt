/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.google.android.material.navigation

import fz1 as NavigationBarMenuView

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class NavigationBarView protected constructor(stub: Unit) : android.widget.FrameLayout(boost114Stub<android.content.Context>()) {
    @JvmField val b: NavigationBarMenuView = boost114Stub()
    open fun getMenu(): android.view.Menu? = boost114Stub()
    open fun getSelectedItemId(): Int = boost114Stub()
    open fun setItemIconTintList(p0: android.content.res.ColorStateList?): Unit = boost114Stub()
    open fun setItemTextColor(p0: android.content.res.ColorStateList?): Unit = boost114Stub()
    open fun setLabelVisibilityMode(p0: Int): Unit = boost114Stub()
}
