/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.bumptech.glide

import ig2 as RequestManager

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class a protected constructor(stub: Unit) : android.content.ComponentCallbacks2 {
    companion object {
        @JvmStatic fun d(p0: android.content.Context?): RequestManager? = boost114Stub()
    }
}
