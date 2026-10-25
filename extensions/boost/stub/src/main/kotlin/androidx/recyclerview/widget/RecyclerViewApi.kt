/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package androidx.recyclerview.widget

import jz1 as NestedScrollingChild

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class RecyclerView protected constructor(stub: Unit) : android.view.ViewGroup(boost114Stub<android.content.Context>()), NestedScrollingChild {
    abstract class a0 protected constructor(stub: Unit) {
        @JvmField val a: android.view.View = boost114Stub()
    }
    abstract class e protected constructor(stub: Unit) {
        fun notifyDataSetChanged(): Unit = boost114Stub()
    }
}
