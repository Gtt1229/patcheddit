/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.rubenmayayo.reddit.aa

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class User protected constructor(stub: Unit) : com.activeandroid.Model(Unit) {
    @JvmField var name: String = boost114Stub()
}
