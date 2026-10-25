/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.rubenmayayo.reddit.ui.adapters

import androidx.recyclerview.widget.RecyclerView.a0 as RecyclerViewViewHolder

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class LoadMoreViewHolder protected constructor(stub: Unit) : RecyclerViewViewHolder(Unit), android.view.View.OnClickListener {
    interface b {
    }
}

abstract class SearchHeaderViewHolder protected constructor(stub: Unit) : RecyclerViewViewHolder(Unit) {
    interface b {
    }
}

abstract class SearchOptionViewHolder protected constructor(stub: Unit) : RecyclerViewViewHolder(Unit), android.view.View.OnClickListener, android.view.View.OnLongClickListener {
    interface a {
    }
}
