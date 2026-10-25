/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.rubenmayayo.reddit.ui.search

import androidx.recyclerview.widget.RecyclerView.e as RecyclerViewAdapter

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class GoToGenericActivity protected constructor(stub: Unit) : com.rubenmayayo.reddit.ui.search.SearchAbstractActivity(Unit) {
}

abstract class SearchAbstractActivity protected constructor(stub: Unit) : com.rubenmayayo.reddit.ui.activities.ToolbarActivity(Unit), com.rubenmayayo.reddit.ui.adapters.SearchOptionViewHolder.a, com.rubenmayayo.reddit.ui.adapters.SearchHeaderViewHolder.b {
    @JvmField var b: com.rubenmayayo.reddit.ui.search.SearchAbstractActivity.e = boost114Stub()
    @JvmField var searchEditText: android.widget.EditText = boost114Stub()
    abstract class e protected constructor(stub: Unit) : RecyclerViewAdapter(Unit) {
    }
}

abstract class SearchGenericActivity protected constructor(stub: Unit) : com.rubenmayayo.reddit.ui.search.SearchAbstractActivity(Unit) {
}
