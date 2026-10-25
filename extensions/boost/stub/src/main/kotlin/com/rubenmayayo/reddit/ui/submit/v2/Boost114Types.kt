/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.rubenmayayo.reddit.ui.submit.v2

import am0 as Boost_am0
import ds1 as Boost_ds1
import l33 as Boost_l33
import z92 as Boost_z92

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class SubmitAbsctractFragment protected constructor(stub: Unit) : Boost_am0(Unit), Boost_l33 {
}

abstract class SubmitVideoFragment protected constructor(stub: Unit) : com.rubenmayayo.reddit.ui.submit.v2.SubmitAbsctractFragment(Unit) {
}

abstract class g protected constructor(stub: Unit) : com.rubenmayayo.reddit.ui.submit.v2.SubmitVideoFragment(Unit) {
    abstract class b protected constructor(stub: Unit) : Boost_z92.b {
        override fun a(p0: Int): Unit = boost114Stub()
    }
    abstract class c protected constructor(stub: Unit) : Boost_ds1 {
        override fun a(p0: String?): Unit = boost114Stub()
        override fun b(p0: String?): Unit = boost114Stub()
        override fun c(p0: com.rubenmayayo.reddit.models.imgur.ImageResponse.UploadedImage?): Unit = boost114Stub()
    }
}
