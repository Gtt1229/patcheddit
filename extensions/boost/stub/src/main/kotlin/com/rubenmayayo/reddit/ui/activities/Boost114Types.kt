/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.rubenmayayo.reddit.ui.activities

import n7 as AppCompatActivity
import gq1 as Boost_gq1
import hm as Boost_hm
import ih as Boost_ih
import jm as Boost_jm
import ti as Boost_ti

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class BillingActivity protected constructor(stub: Unit) : com.rubenmayayo.reddit.ui.activities.a(Unit) {
}

abstract class BillingProActivity protected constructor(stub: Unit) : com.rubenmayayo.reddit.ui.activities.a(Unit) {
}

abstract class ImageActivity protected constructor(stub: Unit) : Boost_ti(Unit) {
}

abstract class LicensesActivity protected constructor(stub: Unit) : com.rubenmayayo.reddit.ui.activities.ToolbarActivity(Unit) {
}

abstract class MediaActivity protected constructor(stub: Unit) : Boost_gq1(Unit), Boost_hm.b {
}

abstract class MediaImageActivity protected constructor(stub: Unit) : com.rubenmayayo.reddit.ui.activities.MediaActivity(Unit) {
}

abstract class ToolbarActivity protected constructor(stub: Unit) : Boost_ih(Unit) {
}

abstract class a protected constructor(stub: Unit) : AppCompatActivity(Unit), Boost_jm.c {
    open fun openProfile(p0: String?): Unit = boost114Stub()
}
