/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.rubenmayayo.reddit.ui.profile

import pj0 as Boost_pj0

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class UserActivity protected constructor(stub: Unit) : Boost_pj0(Unit) {
    @JvmField var O: Array<String> = boost114Stub()
    @JvmField var P: Array<String> = boost114Stub()
    @JvmField var Q: String = boost114Stub()
    @JvmField var R: Byte = boost114Stub()
}
