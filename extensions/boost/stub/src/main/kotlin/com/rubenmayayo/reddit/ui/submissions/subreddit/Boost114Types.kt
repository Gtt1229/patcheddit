/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.rubenmayayo.reddit.ui.submissions.subreddit

import hm as Boost_hm
import n43 as Boost_n43
import pj0 as Boost_pj0

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class MainActivity protected constructor(stub: Unit) : com.rubenmayayo.reddit.ui.submissions.subreddit.SubredditBaseActivity(Unit) {
}

abstract class SubmissionsActivity protected constructor(stub: Unit) : Boost_pj0(Unit), com.google.android.material.appbar.AppBarLayout.g {
    @JvmField var O: com.rubenmayayo.reddit.models.reddit.SubscriptionViewModel = boost114Stub()
}

abstract class SubredditActivity protected constructor(stub: Unit) : com.rubenmayayo.reddit.ui.submissions.subreddit.SubredditBaseActivity(Unit) {
    fun F1(): Boolean = boost114Stub()
}

abstract class SubredditBaseActivity protected constructor(stub: Unit) : com.rubenmayayo.reddit.ui.submissions.subreddit.SubmissionsActivity(Unit), Boost_n43, com.rubenmayayo.reddit.ui.fragments.p.a, com.rubenmayayo.reddit.ui.comments.CommentsFragment.l, Boost_hm.b {
}
