/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.rubenmayayo.reddit.ui.fragments

import b23 as Boost_b23

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class SubmissionRecyclerViewFragment protected constructor(stub: Unit) : com.rubenmayayo.reddit.ui.fragments.d(Unit), Boost_b23 {
    open fun onRefresh(): Unit = boost114Stub()
}

abstract class d protected constructor(stub: Unit) : com.rubenmayayo.reddit.ui.fragments.p(Unit) {
}

abstract class p protected constructor(stub: Unit) : androidx.fragment.app.Fragment(Unit) {
    interface a {
    }
}
