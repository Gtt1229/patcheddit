/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.rubenmayayo.reddit.ui.customviews

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class FormattingBar protected constructor(stub: Unit) : android.widget.LinearLayout(boost114Stub<android.content.Context>()) {
    @JvmField var b: android.widget.EditText = boost114Stub()
    @JvmField var z: com.rubenmayayo.reddit.ui.customviews.FormattingBar.c? = boost114Stub()
    interface c {
        fun a(): Unit
        fun b(): Unit
    }
}
