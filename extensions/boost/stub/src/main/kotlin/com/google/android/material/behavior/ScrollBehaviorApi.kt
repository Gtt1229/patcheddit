/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.google.android.material.behavior

import androidx.coordinatorlayout.widget.CoordinatorLayout.c as CoordinatorBehavior

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

open class HideBottomViewOnScrollBehavior protected constructor(stub: Unit) : CoordinatorBehavior(Unit) {
    constructor() : this(Unit) { boost114Stub<Unit>() }
}
