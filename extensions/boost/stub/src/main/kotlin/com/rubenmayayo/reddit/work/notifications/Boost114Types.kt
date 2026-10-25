/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.rubenmayayo.reddit.work.notifications

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class CheckNotificationsWorker protected constructor(stub: Unit) : androidx.work.Worker(Unit) {
    companion object {
        @JvmStatic fun h(p0: android.app.Activity?): Unit = boost114Stub()
        @JvmStatic fun i(p0: android.app.Activity?): Unit = boost114Stub()
    }
}
