/*
 * Copyright 2026 brealorg.
 * Modifications Copyright 2026 wchill.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.extension.boost.navigation

import MorpheBoostNativeStatics
import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView.a0 as RecyclerViewViewHolder
import an2
import com.rubenmayayo.reddit.models.reddit.SubmissionModel
import com.rubenmayayo.reddit.models.reddit.SubredditModel
import com.rubenmayayo.reddit.models.reddit.SubscriptionViewModel
import com.rubenmayayo.reddit.ui.fragments.SubmissionRecyclerViewFragment
import com.rubenmayayo.reddit.ui.search.SearchAbstractActivity
import com.rubenmayayo.reddit.ui.sidebar.today.TrendingTodayFragment
import net.dean.jraw.RedditClient
import net.dean.jraw.models.Submission
import net.dean.jraw.paginators.Sorting
import net.dean.jraw.paginators.SubredditPaginator
import pd2
import qd3

/** Typed bindings for Boost 1.14.0's native JRAW, search rows and sidebar loader. */
object BoostNativeListings {
    private fun client(): RedditClient = checkNotNull(pd2.r.e) {
        "Boost RedditJRAW client is not initialized"
    }

    @JvmStatic
    fun hotSubmissions(source: String, limit: Int): List<Submission> {
        val paginator = SubredditPaginator(client(), source, *emptyArray<String>())
        paginator.setSorting(Sorting.HOT)
        paginator.setLimit(limit)
        return paginator.next().toList()
    }

    @JvmStatic
    fun fetchSubreddit(name: String): SubredditModel =
        SubredditModel(client().getSubreddit(name))

    @JvmStatic
    fun searchRow(name: String, model: SubredditModel): Any =
        an2(14, name).apply {
            c = SubscriptionViewModel(model)
            d = model
        }

    @JvmStatic
    fun searchHeader(title: String): Any = an2(6, title)

    @JvmStatic
    fun notifySearchRows(activity: Activity) {
        checkNotNull((activity as SearchAbstractActivity).b) {
            "Boost search adapter is not initialized"
        }.notifyDataSetChanged()
    }

    @JvmStatic
    fun nativeTrendingRows(): List<*> = checkNotNull(pd2.r.y()) {
        "Boost native trending loader returned null"
    }

    @JvmStatic
    fun trendingSubmissions(row: Any): List<SubmissionModel> =
        (row as qd3).b ?: emptyList()

    @JvmStatic
    fun submissionModel(submission: Submission): SubmissionModel =
        MorpheBoostNativeStatics.SubmissionModel_C(submission)

    @JvmStatic
    fun trendingRow(title: String, model: SubmissionModel): Any =
        qd3().apply {
            a = title
            b = arrayListOf(model)
        }

    @JvmStatic
    fun includeNsfw(): Boolean {
        val preferences = MorpheBoostNativeStatics.ns2_m()
        return preferences.a.getBoolean("pref_search_include_over_18", preferences.G0())
    }

    @JvmStatic
    fun isPromoted(submission: Submission): Boolean {
        val data = submission.getDataNode()
        return data.get("promoted")?.asBoolean() == true ||
            data.get("is_ad")?.asBoolean() == true
    }

    @JvmStatic
    fun fragmentActivity(fragment: Any?): Activity? =
        (fragment as Fragment?)?.getActivity()

    @JvmStatic
    fun reloadTrending(fragment: Any) {
        val target = fragment as TrendingTodayFragment
        checkNotNull(target.a) { "Boost trending view model is not initialized" }
        // The old ViewModel.h() was inlined into this retained native Runnable.
        TrendingTodayFragment.g(target).run()
    }

    @JvmStatic
    fun holderView(holder: Any): View = (holder as RecyclerViewViewHolder).a

    @JvmStatic
    fun refreshFeed(fragment: Any) =
        (fragment as SubmissionRecyclerViewFragment).onRefresh()
}
