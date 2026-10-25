/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.rubenmayayo.reddit.ui.customviews.dialogs

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

open class UserSelectorView protected constructor(stub: Unit) : android.widget.LinearLayout(boost114Stub<android.content.Context>()) {
    constructor(p0: android.content.Context?, p1: Boolean) : this(Unit) { boost114Stub<Unit>() }
    open fun setCallback(p0: com.rubenmayayo.reddit.ui.customviews.dialogs.UserSelectorView.a?): Unit = boost114Stub()
    interface a {
        fun U(): Unit
        fun h(p0: com.rubenmayayo.reddit.aa.User?, p1: Int): Unit
    }
}
