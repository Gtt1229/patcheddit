/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.rubenmayayo.reddit.ui.sidebar.today

import td3 as Boost_td3

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class TrendingTodayFragment protected constructor(stub: Unit) : androidx.fragment.app.Fragment(Unit) {
    @JvmField var a: Boost_td3 = boost114Stub()
    open class g protected constructor(stub: Unit) : java.lang.Runnable {
        constructor(p0: com.rubenmayayo.reddit.ui.sidebar.today.TrendingTodayFragment?) : this(Unit) { boost114Stub<Unit>() }
        override fun run(): Unit = boost114Stub()
    }
}
