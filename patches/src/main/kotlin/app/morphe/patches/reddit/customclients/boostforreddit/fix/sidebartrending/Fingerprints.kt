/*
 * Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.sidebartrending

import app.morphe.patcher.Fingerprint

internal val trendingTodayAsyncLoaderFingerprint = Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("[Ljava/lang/Object;"),
    custom = { method, classDef ->
        classDef.type == "Lrd3;" && method.name == "doInBackground"
    },
)

internal val trendingTodayFragmentOnViewCreatedFingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
    custom = { method, classDef ->
        classDef.type == "Lcom/rubenmayayo/reddit/ui/sidebar/today/TrendingTodayFragment;" &&
            method.name == "onViewCreated"
    },
)

internal val trendingTodayViewHolderBindFingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf(
        "Landroidx/recyclerview/widget/RecyclerView\$a0;",
        "I",
    ),
    custom = { method, classDef ->
        classDef.type ==
            "Lcom/rubenmayayo/reddit/ui/sidebar/today/TrendingTodayAdapter;" &&
            method.name == "onBindViewHolder"
    },
)
