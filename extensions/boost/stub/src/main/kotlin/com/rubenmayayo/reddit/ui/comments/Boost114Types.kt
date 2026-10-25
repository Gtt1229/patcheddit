/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.rubenmayayo.reddit.ui.comments

import am0 as Boost_am0
import b23 as Boost_b23
import hx as Boost_hx
import kv as Boost_kv

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class CommentsFragment protected constructor(stub: Unit) : Boost_am0(Unit), Boost_hx, Boost_kv, com.rubenmayayo.reddit.ui.adapters.LoadMoreViewHolder.b, Boost_b23 {
    interface l {
    }
}
