/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.google.android.material.appbar

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class AppBarLayout protected constructor(stub: Unit) : android.widget.LinearLayout(boost114Stub<android.content.Context>()), androidx.coordinatorlayout.widget.CoordinatorLayout.b {
    interface a {
    }
    interface g : com.google.android.material.appbar.AppBarLayout.a {
    }
}
