/*
 * Copyright 2026 brealorg.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("DEPRECATION")

package app.morphe.extension.boost.settings

import MorpheBoostNativeStatics
import ak1 as LiveData
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.`FragmentManager$l` as OnBackStackChangedListener
import androidx.fragment.app.a as BackStackRecord
import ao2
import com.rubenmayayo.reddit.FolderActivity
import com.rubenmayayo.reddit.FolderSAFActivity
import com.rubenmayayo.reddit.models.reddit.SubscriptionViewModel
import com.rubenmayayo.reddit.ui.activities.BillingActivity
import com.rubenmayayo.reddit.ui.activities.BillingProActivity
import com.rubenmayayo.reddit.ui.activities.LicensesActivity
import com.rubenmayayo.reddit.ui.customviews.menu.MenuOption
import com.rubenmayayo.reddit.ui.drafts.ManageDraftsActivity
import com.rubenmayayo.reddit.ui.subscriptions.SubscriptionsActivity
import com.rubenmayayo.reddit.ui.synccit.SynccitCreateActivity
import com.rubenmayayo.reddit.ui.upload.ManageUploadsActivity
import i61 as HasDefaultViewModelProviderFactory
import ns2
import o83
import pd2
import pm3 as ViewModelProviderImpl
import r12 as Observer
import rm2
import rm3 as ViewModelStoreOwner
import ui1 as LifecycleOwner
import ym2

/** Typed bindings to the official Boost 1.14.0 APK. */
object BoostSettingsNative {
    private const val TAG = "MorpheBoostSettings"
    private const val MULTI_SUFFIX = ".multi"

    @JvmStatic
    fun settingsClass(): Class<*> = ns2::class.java

    @JvmStatic
    fun preferences(): SharedPreferences = MorpheBoostNativeStatics.ns2_m().a

    @JvmStatic
    fun savedViewsPreferences(): SharedPreferences = MorpheBoostNativeStatics.fn3_a().a

    @JvmStatic
    fun markDirty(field: String) {
        when (field) {
            "d" -> ns2.d = true
            "e" -> ns2.e = true
            "f" -> ns2.f = true
            "g" -> ns2.g = true
            "h" -> ns2.h = true
            "i" -> ns2.i = true
            else -> throw IllegalArgumentException("Unknown Boost settings flag: $field")
        }
    }

    @JvmStatic
    fun invalidateFonts(key: String) {
        when (key) {
            "pref_title_font" -> ns2.g = true
            "pref_comments_font" -> ns2.i = true
            "pref_font_size", "pref_font_size_title" -> {
                // 1.14 reads sizes directly; invalidate existing post/comment views instead.
                ns2.g = true
                ns2.i = true
            }
            else -> throw IllegalArgumentException("Unknown Boost typography preference: $key")
        }
    }

    @JvmStatic
    fun setSubredditPrefix(value: String) {
        ns2.c = value
    }

    @JvmStatic
    fun loadFilters(key: String): List<String> {
        val filters = checkNotNull(MorpheBoostNativeStatics.ns2_m().k(key)) {
            "Boost returned no filter list for $key"
        }
        return filters.map {
            require(it is String) { "Invalid Boost filter entry for $key" }
            it
        }
    }

    @JvmStatic
    fun saveFilters(key: String, values: List<String>) {
        MorpheBoostNativeStatics.ns2_m().v0(key, ArrayList(values))
    }

    @JvmStatic
    fun setColorPattern(colors: IntArray) {
        MorpheBoostNativeStatics.ns2_m().w0(colors)
    }

    @JvmStatic
    fun resolveFont(context: Context, value: String): Typeface? =
        MorpheBoostNativeStatics.ns2_c0(context, value)

    @JvmStatic
    fun themeTitle(context: Context, theme: Int): String =
        context.getString(o83.e[MorpheBoostNativeStatics.o83_q(theme)])

    @JvmStatic
    fun updateThemeMode() {
        MorpheBoostNativeStatics.g02_a()
    }

    @JvmStatic
    fun viewTitle(context: Context, viewType: Int): String {
        val titles = context.resources.getStringArray(
            MorpheSettingsV4Catalog.resourceId(context, "array", "pref_view_titles")
        )
        val values = context.resources.getStringArray(
            MorpheSettingsV4Catalog.resourceId(context, "array", "pref_view_values")
        )
        check(titles.size == values.size) { "Invalid Boost view arrays" }
        return values.indexOf(viewType.toString()).let { index ->
            if (index >= 0) titles[index] else viewType.toString()
        }
    }

    private fun subscriptionForKey(key: String): SubscriptionViewModel =
        if (key.endsWith(MULTI_SUFFIX)) {
            SubscriptionViewModel(key.removeSuffix(MULTI_SUFFIX), true)
        } else {
            SubscriptionViewModel(key)
        }

    @JvmStatic
    fun savedViewName(context: Context, key: String): String =
        MorpheBoostNativeStatics.hk3_X(context, subscriptionForKey(key))

    @JvmStatic
    fun frontpageSubscription(): SubscriptionViewModel =
        MorpheBoostNativeStatics.SubscriptionViewModel_c()

    @JvmStatic
    fun emptySubscription(): SubscriptionViewModel = SubscriptionViewModel("")

    class SortOption(@JvmField val id: Int, @JvmField val title: String)

    @JvmStatic
    fun sortOptions(context: Context, subscription: SubscriptionViewModel): List<SortOption> {
        val result = ArrayList<SortOption>()
        fun append(values: List<*>, parent: String = "") {
            for (value in values) {
                require(value is MenuOption) { "Invalid Boost sort menu option" }
                if (value.H || !value.D) continue
                val label = value.z?.takeIf { it.isNotEmpty() }
                    ?: if (value.y != 0) context.getString(value.y) else ""
                val title = if (parent.isEmpty()) label else "$parent — $label"
                val children = value.F
                if (children != null && children.isNotEmpty()) {
                    append(children, title)
                } else if (value.a == 100 || MorpheBoostNativeStatics.bx2_d(value.a) != null) {
                    result.add(SortOption(value.a, title))
                }
            }
        }
        append(MorpheBoostNativeStatics.o80_j(subscription, true, false))
        return result
    }

    @JvmStatic
    fun currentSortOptionId(defaultSort: Boolean, subscription: SubscriptionViewModel): Int? {
        if (!defaultSort) {
            val id = MorpheBoostNativeStatics.bx2_b().a.getInt(
                MorpheBoostNativeStatics.bx2_c(subscription), -1
            )
            return if (MorpheBoostNativeStatics.bx2_d(id) != null) id else 100
        }
        val settings = MorpheBoostNativeStatics.ns2_m()
        return when (settings.E()?.name) {
            "HOT" -> 0
            "NEW" -> 1
            "RISING" -> 2
            "GILDED" -> 5
            "BEST" -> 6
            "TOP", "CONTROVERSIAL" -> {
                val period = listOf("HOUR", "DAY", "WEEK", "MONTH", "YEAR", "ALL")
                    .indexOf(settings.D()?.name)
                if (period < 0) null else {
                    (if (settings.E()?.name == "TOP") 30 else 40) + period
                }
            }
            else -> null
        }
    }

    @JvmStatic
    fun saveDefaultSort(sort: String, period: String?) {
        MorpheBoostNativeStatics.ns2_m().x0(sort)
        if (period != null) {
            // The period setter was inlined by R8 in 1.14.0.
            preferences().edit().putString("pref_default_post_period", period).apply()
        }
    }

    @JvmStatic
    fun saveSubscriptionSort(subscription: SubscriptionViewModel, id: Int) {
        val manager = MorpheBoostNativeStatics.bx2_b()
        if (id == 100) {
            manager.a(subscription)
        } else {
            require(MorpheBoostNativeStatics.bx2_d(id) != null) { "Invalid Boost sort: $id" }
            manager.e(id, MorpheBoostNativeStatics.bx2_c(subscription))
        }
    }

    @JvmStatic
    fun savedSortSubscriptions(): List<SubscriptionViewModel> =
        MorpheBoostNativeStatics.bx2_b().a.all.keys.map(::subscriptionForKey)

    @JvmStatic
    fun subscriptionName(context: Context, subscription: SubscriptionViewModel): String =
        MorpheBoostNativeStatics.hk3_X(context, subscription)

    @JvmStatic
    fun clearSavedSorts() {
        MorpheBoostNativeStatics.bx2_b().a.edit().clear().apply()
    }

    @JvmStatic
    fun downloadFolder(type: Int): String {
        val settings = MorpheBoostNativeStatics.ns2_m()
        val folder = if (Build.VERSION.SDK_INT >= 30) {
            settings.i(type)
        } else {
            settings.h(type)
        }
        return checkNotNull(folder) { "Boost returned no download folder for type $type" }
    }

    @JvmStatic
    fun hasDownloadFolder(type: Int): Boolean =
        preferences().contains(downloadFolderKey(type))

    private fun downloadFolderKey(type: Int): String {
        require(type in 0..3) { "Invalid Boost folder type: $type" }
        return MorpheBoostNativeStatics.ns2_j(type) + if (Build.VERSION.SDK_INT >= 30) "_saf" else ""
    }

    @JvmStatic
    fun setDownloadFolder(type: Int, value: String?) {
        if (value == null) {
            if (Build.VERSION.SDK_INT >= 30) {
                MorpheBoostNativeStatics.ns2_m().m0(type)
            } else {
                MorpheBoostNativeStatics.ns2_m().l0(type)
            }
        } else {
            preferences().edit().putString(downloadFolderKey(type), value).apply()
        }
    }

    @JvmStatic
    fun communitySubfoldersEnabled(): Boolean =
        preferences().getBoolean("pref_download_folder_per_subreddit", false)

    @JvmStatic
    fun setCommunitySubfoldersEnabled(enabled: Boolean) {
        preferences().edit().putBoolean("pref_download_folder_per_subreddit", enabled).apply()
    }

    @JvmStatic
    fun customDownloadFoldersEnabled(): Boolean =
        Build.VERSION.SDK_INT < 30 || MorpheBoostNativeStatics.xe2_a("download_custom_folder")

    @JvmStatic
    fun typedDownloadFoldersEnabled(): Boolean =
        Build.VERSION.SDK_INT < 30 || MorpheBoostNativeStatics.xe2_a("download_custom_folder_by_type")

    @JvmStatic
    fun legacyDownloadFolderIntent(context: Context, type: Int): Intent =
        Intent(context, FolderActivity::class.java).putExtra("type", type)

    @JvmStatic
    fun clearMediaCache(context: Context) {
        MorpheBoostNativeStatics.mo_a(context)
    }

    @JvmStatic
    fun mediaCacheSize(context: Context): String = MorpheBoostNativeStatics.mo_b(context)

    @JvmStatic
    fun resizeMediaCache(context: Context) {
        require(context is ContextWrapper) { "Boost media cache needs an activity context" }
        MorpheBoostNativeStatics.dr_g(context)
    }

    @JvmStatic
    fun synccitUsername(): String = preferences().getString("synccit_username", "") ?: ""

    @JvmStatic
    fun synccitAuth(): String = preferences().getString("synccit_auth", "") ?: ""

    @JvmStatic
    fun saveSynccitCredentials(username: String, auth: String) {
        preferences().edit().putString("synccit_username", username)
            .putString("synccit_auth", auth).apply()
    }

    @JvmStatic
    fun synccitIntent(context: Context): Intent = Intent(context, SynccitCreateActivity::class.java)

    @JvmStatic
    fun deleteHistory(key: String) {
        when (key) {
            "pref_history_delete_read" -> MorpheBoostNativeStatics.cd2_a().a.edit().clear().apply()
            "pref_recent_posts_delete" -> MorpheBoostNativeStatics.d72_c().b()
            "pref_history_delete" -> MorpheBoostNativeStatics.h43_b().a.edit().clear().apply()
            "pref_searches_delete" -> MorpheBoostNativeStatics.tm2_a().a?.clear()
            "pref_all_delete" -> {
                MorpheBoostNativeStatics.d72_c().b()
                MorpheBoostNativeStatics.h43_b().a.edit().clear().apply()
                MorpheBoostNativeStatics.tm2_a().a?.clear()
            }
            else -> throw IllegalArgumentException("Unknown Boost history action: $key")
        }
    }

    @JvmStatic
    fun subscriptionsChanged(context: Context) {
        MorpheBoostNativeStatics.hk3_E(context)
    }

    @JvmStatic
    fun updateNotifications(activity: Activity, enabled: Boolean) {
        if (enabled) {
            MorpheBoostNativeStatics.CheckNotificationsWorker_i(activity)
        } else {
            MorpheBoostNativeStatics.CheckNotificationsWorker_h(activity)
        }
    }

    @JvmStatic
    fun updateTrending(context: Context) {
        val searches = checkNotNull(pd2.r.i()) { "Boost trending searches are not initialized" }
        if (searches.isEmpty()) MorpheBoostNativeStatics.TrendingWorker_h(context, 2)
    }

    @JvmStatic
    fun resetTips() {
        MorpheBoostNativeStatics.qh0_b().d()
    }

    @JvmStatic
    fun openSubscriptions(activity: Activity) {
        activity.startActivity(Intent(activity, SubscriptionsActivity::class.java))
    }

    @JvmStatic
    fun openDrafts(context: Context) {
        context.startActivity(Intent(context, ManageDraftsActivity::class.java))
    }

    @JvmStatic
    fun openUploads(context: Context) {
        context.startActivity(Intent(context, ManageUploadsActivity::class.java))
    }

    @JvmStatic
    fun openBilling(context: Context, pro: Boolean) {
        context.startActivity(Intent(context, if (pro) BillingProActivity::class.java else BillingActivity::class.java))
    }

    @JvmStatic
    fun openDownloadFolders(context: Context) {
        val target = if (Build.VERSION.SDK_INT >= 30) FolderSAFActivity::class.java else FolderActivity::class.java
        context.startActivity(Intent(context, target))
    }

    @JvmStatic
    fun openSupport(context: Context) {
        MorpheBoostNativeStatics.xy1_I(context)
    }

    @JvmStatic
    fun rateApp(activity: Activity) {
        MorpheBoostNativeStatics.xy1_O(activity)
    }

    @JvmStatic
    fun openCommunity(activity: Activity, community: String) {
        MorpheBoostNativeStatics.xy1_F(activity, community)
    }

    @JvmStatic
    fun openWiki(activity: Activity, community: String, page: String) {
        MorpheBoostNativeStatics.xy1_L(activity, community, page)
    }

    @JvmStatic
    fun openLicenses(context: Context) {
        context.startActivity(Intent(context, LicensesActivity::class.java))
    }

    @JvmStatic
    fun openUser(context: Context, username: String) {
        MorpheBoostNativeStatics.xy1_B(context, username)
    }

    @JvmStatic
    fun auditValue(key: String): Any {
        val settings = MorpheBoostNativeStatics.ns2_m()
        val prefs = settings.a
        return when (key) {
            "pref_left_handed" -> settings.I()
            "pref_cards_full_preview" -> settings.r()
            "pref_cards_rounded_corners" -> settings.t()
            "pref_colored_nav_bar" -> prefs.getBoolean(key, Build.VERSION.SDK_INT >= 26)
            "pref_colored_status_bar", "pref_cards_subreddit_icon", "pref_cards_gallery_carousel",
            "pref_cards_links_as_thumbnails", "pref_cards_preview_self",
            "pref_mini_cards_rounded_corners", "pref_mini_cards_truncate_title" -> prefs.getBoolean(key, true)
            "pref_dynamic_colors", "pref_view_per_subscription", "pref_show_subreddit_prefix",
            "pref_mini_cards_buttons_visible", "pref_dense_buttons_visible", "pref_load_readability",
            "pref_lock_sidebar" -> prefs.getBoolean(key, false)
            "pref_view" -> prefs.getString(key, "0") ?: "0"
            "pref_title_font" -> prefs.getString(key, "sans-serif-medium") ?: ""
            "pref_comments_font" -> prefs.getString(key, "") ?: ""
            "pref_font_size_title", "pref_font_size" -> prefs.getString(key, "Medium") ?: "Medium"
            "pref_cards_preview_self_lines" -> prefs.getInt(key, 5)
            else -> throw IllegalArgumentException("Unknown Boost audit preference: $key")
        }
    }

    class SavedSearch(
        @JvmField val id: Int,
        @JvmField val name: String,
        @JvmField val community: String,
        @JvmField val scopeId: String,
        @JvmField val query: String,
        @JvmField val sort: String,
        @JvmField val period: String,
    )

    fun interface SearchesListener {
        fun onChanged(values: List<SavedSearch>)
    }

    fun interface SearchCompletion {
        fun onComplete(success: Boolean)
    }

    private fun searchRows(value: Any?): List<SavedSearch> {
        if (value == null) return emptyList()
        require(value is List<*>) { "Invalid Boost saved-search result" }
        return value.map {
            require(it is rm2) { "Invalid Boost saved-search entity" }
            SavedSearch(it.a, it.b ?: "", it.c ?: "", it.d ?: "", it.e ?: "", it.f ?: "", it.g ?: "")
        }
    }

    class SearchObservation internal constructor(
        private val data: LiveData,
        owner: LifecycleOwner,
        listener: SearchesListener,
    ) : AutoCloseable {
        private var closed = false
        private val observer = object : Observer {
            override fun b(value: Any?) {
                if (!closed) listener.onChanged(searchRows(value))
            }
        }

        init {
            data.e(owner, observer)
        }

        override fun close() {
            if (!closed) {
                closed = true
                data.j(observer)
            }
        }
    }

    class SavedSearches internal constructor(private val model: ao2) {
        private val data: LiveData = checkNotNull(model.c) { "Boost saved-search LiveData is not initialized" }

        fun current(): List<SavedSearch> = searchRows(data.d())

        fun observe(owner: Fragment, listener: SearchesListener): SearchObservation {
            require(owner is LifecycleOwner) { "Boost fragment is not a lifecycle owner" }
            return SearchObservation(data, owner, listener)
        }

        fun save(value: SavedSearch, update: Boolean, completion: SearchCompletion) {
            val entity = rm2()
            entity.a = value.id
            entity.b = value.name
            entity.c = value.community
            entity.d = value.scopeId
            entity.e = value.query
            entity.f = value.sort
            entity.g = value.period
            mutate(completion) { dao ->
                if (update) dao.d.e(entity) else dao.b.e(entity)
            }
        }

        fun delete(value: SavedSearch, completion: SearchCompletion) {
            val entity = rm2()
            entity.a = value.id
            mutate(completion) { dao -> dao.c.e(entity) }
        }

        private fun mutate(completion: SearchCompletion, action: (ym2) -> Unit) {
            val dao = model.b.a
            // These are the same Room transaction/adapter calls as cn2's inlined AsyncTasks.
            AsyncTask.execute(Runnable {
                val success = try {
                    dao.a.b()
                    dao.a.c()
                    try {
                        action(dao)
                        dao.a.i()
                    } finally {
                        dao.a.f()
                    }
                    true
                } catch (exception: Exception) {
                    Log.e(TAG, "Saved-search database update failed", exception)
                    false
                }
                Handler(Looper.getMainLooper()).post { completion.onComplete(success) }
            })
        }
    }

    @JvmStatic
    fun savedSearches(activity: Activity): SavedSearches {
        require(activity is ViewModelStoreOwner && activity is HasDefaultViewModelProviderFactory) {
            "Boost activity cannot own saved-search ViewModels"
        }
        val provider = ViewModelProviderImpl(
            activity.getViewModelStore(),
            MorpheBoostNativeStatics.tc1_a(activity),
            activity.getDefaultViewModelCreationExtras()
        )
        val type = MorpheBoostNativeStatics.ce2_a(ao2::class.java)
        val name = checkNotNull(type.b()) { "Saved-search ViewModel has no canonical name" }
        val model = provider.a(type, "androidx.lifecycle.ViewModelProvider.DefaultKey:$name")
        check(model is ao2) { "Incorrect Boost saved-search ViewModel" }
        return SavedSearches(model)
    }

    class EditorBackStack internal constructor(
        private val manager: FragmentManager,
        changed: Runnable,
    ) : AutoCloseable {
        private val listener = object : OnBackStackChangedListener {
            override fun onBackStackChanged() {
                changed.run()
            }
        }

        init {
            if (manager.l == null) manager.l = ArrayList<OnBackStackChangedListener>()
            checkNotNull(manager.l).add(listener)
        }

        fun count(): Int = manager.d?.size ?: 0

        fun push(name: String) {
            val transaction = BackStackRecord(manager)
            check(transaction.h) { "Boost transaction cannot be added to the back stack" }
            // beginTransaction/addToBackStack/commit were inlined in the shipped APK.
            transaction.g = true
            transaction.i = name
            transaction.g(false)
        }

        fun pop(): Boolean = manager.O()

        override fun close() {
            manager.l?.remove(listener)
        }
    }

    @JvmStatic
    fun editorBackStack(fragment: Fragment, changed: Runnable): EditorBackStack =
        EditorBackStack(
            checkNotNull(fragment.getParentFragmentManager()) { "Boost settings fragment has no parent FragmentManager" },
            changed
        )
}
