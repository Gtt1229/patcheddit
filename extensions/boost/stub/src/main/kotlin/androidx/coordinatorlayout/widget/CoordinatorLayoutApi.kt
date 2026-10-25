/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package androidx.coordinatorlayout.widget

import androidx.coordinatorlayout.widget.CoordinatorLayout.c as CoordinatorBehavior
import lz1 as NestedScrollingParent2
import mz1 as NestedScrollingParent3

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class CoordinatorLayout protected constructor(stub: Unit) : android.view.ViewGroup(boost114Stub<android.content.Context>()), NestedScrollingParent2, NestedScrollingParent3 {
    interface b {
    }
    abstract class c protected constructor(stub: Unit) {
    }
    abstract class f protected constructor(stub: Unit) : android.view.ViewGroup.MarginLayoutParams(0, 0) {
        @JvmField var a: CoordinatorBehavior? = boost114Stub()
        fun b(p0: CoordinatorBehavior?): Unit = boost114Stub()
    }
}
