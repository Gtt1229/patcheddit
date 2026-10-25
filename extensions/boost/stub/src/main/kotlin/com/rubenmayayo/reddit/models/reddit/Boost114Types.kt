/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package com.rubenmayayo.reddit.models.reddit

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

abstract class CommentModel protected constructor(stub: Unit) : com.rubenmayayo.reddit.models.reddit.CommentViewModel(Unit) {
    @JvmField var b0: String = boost114Stub()
    @JvmField var e0: String = boost114Stub()
}

abstract class CommentViewModel protected constructor(stub: Unit) : com.rubenmayayo.reddit.models.reddit.PublicContributionModel(Unit) {
}

abstract class ContributionModel protected constructor(stub: Unit) : com.rubenmayayo.reddit.models.reddit.ThingModel(Unit) {
}

abstract class PublicContributionModel protected constructor(stub: Unit) : com.rubenmayayo.reddit.models.reddit.ContributionModel(Unit) {
}

open class SubmissionModel protected constructor(stub: Unit) : com.rubenmayayo.reddit.models.reddit.PublicContributionModel(Unit), java.io.Serializable {
    constructor() : this(Unit) { boost114Stub<Unit>() }
    @JvmField var G0: Int = boost114Stub()
    @JvmField var P0: String = boost114Stub()
    @JvmField var R: String = boost114Stub()
    @JvmField var Z: String = boost114Stub()
    override fun describeContents(): Int = boost114Stub()
    override fun writeToParcel(p0: android.os.Parcel, p1: Int): Unit = boost114Stub()
    companion object {
        @JvmStatic fun C(p0: net.dean.jraw.models.Submission?): com.rubenmayayo.reddit.models.reddit.SubmissionModel? = boost114Stub()
    }
}

open class SubredditModel protected constructor(stub: Unit) : com.rubenmayayo.reddit.models.reddit.SubscriptionModel(Unit), android.os.Parcelable, Comparable<Any?> {
    constructor(p0: String?) : this(Unit) { boost114Stub<Unit>() }
    constructor(p0: net.dean.jraw.models.Subreddit?) : this(Unit) { boost114Stub<Unit>() }
    @JvmField var C: String = boost114Stub()
    @JvmField var I: String = boost114Stub()
    @JvmField var L: Long = boost114Stub()
    override fun compareTo(p0: Any?): Int = boost114Stub()
    override fun describeContents(): Int = boost114Stub()
    override fun writeToParcel(p0: android.os.Parcel, p1: Int): Unit = boost114Stub()
}

abstract class SubscriptionModel protected constructor(stub: Unit) : com.rubenmayayo.reddit.models.reddit.ThingModel(Unit), android.os.Parcelable {
    @JvmField var y: String = boost114Stub()
    @JvmField var z: String = boost114Stub()
}

open class SubscriptionViewModel protected constructor(stub: Unit) : android.os.Parcelable {
    constructor(p0: com.rubenmayayo.reddit.models.reddit.SubredditModel?) : this(Unit) { boost114Stub<Unit>() }
    constructor(p0: String?) : this(Unit) { boost114Stub<Unit>() }
    constructor(p0: String?, p1: Boolean) : this(Unit) { boost114Stub<Unit>() }
    @JvmField var a: String = boost114Stub()
    override fun describeContents(): Int = boost114Stub()
    override fun writeToParcel(p0: android.os.Parcel, p1: Int): Unit = boost114Stub()
    companion object {
        @JvmStatic fun c(): com.rubenmayayo.reddit.models.reddit.SubscriptionViewModel? = boost114Stub()
    }
}

abstract class ThingModel protected constructor(stub: Unit) : android.os.Parcelable {
}
