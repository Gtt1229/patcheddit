/*
 * Copyright 2026 brealorg.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("DEPRECATION")

package app.morphe.extension.boost.settings

import MorpheBoostNativeStatics
import androidx.lifecycle.LiveData
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
import androidx.fragment.app.FragmentManager.OnBackStackChangedListener
import androidx.fragment.app.BackStackRecord
import app.morphe.extension.boost.api.SavedSearchViewModel
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
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import app.morphe.extension.boost.api.BoostPreferences
import app.morphe.extension.boost.api.BoostFieldNames
import app.morphe.extension.boost.api.ThemeUtils
import app.morphe.extension.boost.api.RedditJraw
import app.morphe.extension.boost.api.ViewModelProviderImpl
import androidx.lifecycle.Observer
import app.morphe.extension.boost.api.SavedSearch as NativeSavedSearch
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.LifecycleOwner
import app.morphe.extension.boost.api.SavedSearchDao

/** Typed bindings to the official Boost 1.14.1 APK. */
object BoostSettingsNative {
    private const val TAG = "MorpheBoostSettings"
    private const val MULTI_SUFFIX = ".multi"

    @JvmStatic
    fun settingsClass(): Class<*> = BoostPreferences::class.java

    @JvmStatic
    fun preferences(): SharedPreferences = MorpheBoostNativeStatics.BoostPreferences_getInstance().preferences

    @JvmStatic
    fun savedViewsPreferences(): SharedPreferences = MorpheBoostNativeStatics.ViewUtils_getInstance().preferences

    @JvmStatic
    fun markDirty(field: String) {
        if (field == BoostFieldNames.BoostPreferencesLayoutDirty) BoostPreferences.layoutDirty = true
        else if (field == BoostFieldNames.BoostPreferencesToolbarDirty) BoostPreferences.toolbarDirty = true
        else if (field == BoostFieldNames.BoostPreferencesPostViewsDirty) BoostPreferences.postViewsDirty = true
        else if (field == BoostFieldNames.BoostPreferencesPostTypographyDirty) BoostPreferences.postTypographyDirty = true
        else if (field == BoostFieldNames.BoostPreferencesThemeDirty) BoostPreferences.themeDirty = true
        else if (field == BoostFieldNames.BoostPreferencesCommentTypographyDirty) BoostPreferences.commentTypographyDirty = true
        else throw IllegalArgumentException("Unknown Boost settings flag: $field")
    }

    @JvmStatic
    fun invalidateFonts(key: String) {
        when (key) {
            "pref_title_font" -> BoostPreferences.postTypographyDirty = true
            "pref_comments_font" -> BoostPreferences.commentTypographyDirty = true
            "pref_font_size", "pref_font_size_title" -> {
                // 1.14 reads sizes directly; invalidate existing post/comment views instead.
                BoostPreferences.postTypographyDirty = true
                BoostPreferences.commentTypographyDirty = true
            }
            else -> throw IllegalArgumentException("Unknown Boost typography preference: $key")
        }
    }

    @JvmStatic
    fun setSubredditPrefix(value: String) {
        BoostPreferences.subredditPrefix = value
    }

    @JvmStatic
    fun loadFilters(key: String): List<String> {
        val filters = checkNotNull(MorpheBoostNativeStatics.BoostPreferences_getInstance().loadFilters(key)) {
            "Boost returned no filter list for $key"
        }
        return filters.map {
            require(it is String) { "Invalid Boost filter entry for $key" }
            it
        }
    }

    @JvmStatic
    fun saveFilters(key: String, values: List<String>) {
        MorpheBoostNativeStatics.BoostPreferences_getInstance().saveFilters(key, ArrayList(values))
    }

    @JvmStatic
    fun setColorPattern(colors: IntArray) {
        MorpheBoostNativeStatics.BoostPreferences_getInstance().setColorPattern(colors)
    }

    @JvmStatic
    fun resolveFont(context: Context, value: String): Typeface? =
        MorpheBoostNativeStatics.BoostPreferences_resolveFont(context, value)

    @JvmStatic
    fun themeTitle(context: Context, theme: Int): String =
        context.getString(ThemeUtils.themeTitles[MorpheBoostNativeStatics.ThemeUtils_themeIndex(theme)])

    @JvmStatic
    fun updateThemeMode() {
        MorpheBoostNativeStatics.NightUtils_updateThemeMode()
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
        MorpheBoostNativeStatics.Utils_subscriptionName(context, subscriptionForKey(key))

    @JvmStatic
    fun frontpageSubscription(): SubscriptionViewModel =
        MorpheBoostNativeStatics.SubscriptionViewModel_frontpage()

    @JvmStatic
    fun emptySubscription(): SubscriptionViewModel = SubscriptionViewModel("")

    class SortOption(@JvmField val id: Int, @JvmField val title: String)

    @JvmStatic
    fun sortOptions(context: Context, subscription: SubscriptionViewModel): List<SortOption> {
        val result = ArrayList<SortOption>()
        fun append(values: List<*>, parent: String = "") {
            for (value in values) {
                require(value is MenuOption) { "Invalid Boost sort menu option" }
                if (value.divider || !value.enabled) continue
                val label = value.title?.takeIf { it.isNotEmpty() }
                    ?: if (value.titleResource != 0) context.getString(value.titleResource) else ""
                val title = if (parent.isEmpty()) label else "$parent — $label"
                val children = value.children
                if (children != null && children.isNotEmpty()) {
                    append(children, title)
                } else if (value.id == 100 || MorpheBoostNativeStatics.SortUtils_resolveSort(value.id) != null) {
                    result.add(SortOption(value.id, title))
                }
            }
        }
        append(MorpheBoostNativeStatics.SortMenu_sortOptions(subscription, true, false))
        return result
    }

    @JvmStatic
    fun currentSortOptionId(defaultSort: Boolean, subscription: SubscriptionViewModel): Int? {
        if (!defaultSort) {
            val id = MorpheBoostNativeStatics.SortUtils_getInstance().preferences.getInt(
                MorpheBoostNativeStatics.SortUtils_subscriptionKey(subscription), -1
            )
            return if (MorpheBoostNativeStatics.SortUtils_resolveSort(id) != null) id else 100
        }
        val settings = MorpheBoostNativeStatics.BoostPreferences_getInstance()
        return when (settings.defaultSorting()?.name) {
            "HOT" -> 0
            "NEW" -> 1
            "RISING" -> 2
            "GILDED" -> 5
            "BEST" -> 6
            "TOP", "CONTROVERSIAL" -> {
                val period = listOf("HOUR", "DAY", "WEEK", "MONTH", "YEAR", "ALL")
                    .indexOf(settings.defaultPeriod()?.name)
                if (period < 0) null else {
                    (if (settings.defaultSorting()?.name == "TOP") 30 else 40) + period
                }
            }
            else -> null
        }
    }

    @JvmStatic
    fun saveDefaultSort(sort: String, period: String?) {
        MorpheBoostNativeStatics.BoostPreferences_getInstance().saveDefaultSort(sort)
        if (period != null) {
            // The period setter was inlined by R8 in 1.14.0.
            preferences().edit().putString("pref_default_post_period", period).apply()
        }
    }

    @JvmStatic
    fun saveSubscriptionSort(subscription: SubscriptionViewModel, id: Int) {
        val manager = MorpheBoostNativeStatics.SortUtils_getInstance()
        if (id == 100) {
            manager.clearSubscriptionSort(subscription)
        } else {
            require(MorpheBoostNativeStatics.SortUtils_resolveSort(id) != null) { "Invalid Boost sort: $id" }
            manager.saveSubscriptionSort(id, MorpheBoostNativeStatics.SortUtils_subscriptionKey(subscription))
        }
    }

    @JvmStatic
    fun savedSortSubscriptions(): List<SubscriptionViewModel> =
        MorpheBoostNativeStatics.SortUtils_getInstance().preferences.all.keys.map(::subscriptionForKey)

    @JvmStatic
    fun subscriptionName(context: Context, subscription: SubscriptionViewModel): String =
        MorpheBoostNativeStatics.Utils_subscriptionName(context, subscription)

    @JvmStatic
    fun clearSavedSorts() {
        MorpheBoostNativeStatics.SortUtils_getInstance().preferences.edit().clear().apply()
    }

    @JvmStatic
    fun downloadFolder(type: Int): String {
        val settings = MorpheBoostNativeStatics.BoostPreferences_getInstance()
        val folder = if (Build.VERSION.SDK_INT >= 30) {
            settings.safDownloadFolder(type)
        } else {
            settings.legacyDownloadFolder(type)
        }
        return checkNotNull(folder) { "Boost returned no download folder for type $type" }
    }

    @JvmStatic
    fun hasDownloadFolder(type: Int): Boolean =
        preferences().contains(downloadFolderKey(type))

    private fun downloadFolderKey(type: Int): String {
        require(type in 0..3) { "Invalid Boost folder type: $type" }
        return MorpheBoostNativeStatics.BoostPreferences_downloadFolderKey(type) + if (Build.VERSION.SDK_INT >= 30) "_saf" else ""
    }

    @JvmStatic
    fun setDownloadFolder(type: Int, value: String?) {
        if (value == null) {
            if (Build.VERSION.SDK_INT >= 30) {
                MorpheBoostNativeStatics.BoostPreferences_getInstance().resetSafDownloadFolder(type)
            } else {
                MorpheBoostNativeStatics.BoostPreferences_getInstance().resetLegacyDownloadFolder(type)
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
        Build.VERSION.SDK_INT < 30 || MorpheBoostNativeStatics.RemoteConfigHelper_isEnabled("download_custom_folder")

    @JvmStatic
    fun typedDownloadFoldersEnabled(): Boolean =
        Build.VERSION.SDK_INT < 30 || MorpheBoostNativeStatics.RemoteConfigHelper_isEnabled("download_custom_folder_by_type")

    @JvmStatic
    fun legacyDownloadFolderIntent(context: Context, type: Int): Intent =
        Intent(context, FolderActivity::class.java).putExtra("type", type)

    @JvmStatic
    fun clearMediaCache(context: Context) {
        MorpheBoostNativeStatics.CacheUtils_clear(context)
    }

    @JvmStatic
    fun mediaCacheSize(context: Context): String = MorpheBoostNativeStatics.CacheUtils_size(context)

    @JvmStatic
    fun resizeMediaCache(context: Context) {
        require(context is ContextWrapper) { "Boost media cache needs an activity context" }
        MorpheBoostNativeStatics.DiskCacheAdapter_resizeMediaCache(context)
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
    fun synccitSettingsIntent(context: Context): Intent =
        Intent().setClassName(context, "com.rubenmayayo.reddit.ui.synccit.SynccitActivity")

    @JvmStatic
    fun deleteHistory(key: String) {
        when (key) {
            "pref_history_delete_read" -> MorpheBoostNativeStatics.ReadHistory_getInstance().preferences.edit().clear().apply()
            "pref_recent_posts_delete" -> MorpheBoostNativeStatics.PostHistoryUtils_getInstance().clear()
            "pref_history_delete" -> MorpheBoostNativeStatics.SubredditHistoryUtils_getInstance().preferences.edit().clear().apply()
            "pref_searches_delete" -> MorpheBoostNativeStatics.SearchCache_getInstance().cache?.clear()
            "pref_all_delete" -> {
                MorpheBoostNativeStatics.PostHistoryUtils_getInstance().clear()
                MorpheBoostNativeStatics.SubredditHistoryUtils_getInstance().preferences.edit().clear().apply()
                MorpheBoostNativeStatics.SearchCache_getInstance().cache?.clear()
            }
            else -> throw IllegalArgumentException("Unknown Boost history action: $key")
        }
    }

    @JvmStatic
    fun subscriptionsChanged(context: Context) {
        MorpheBoostNativeStatics.Utils_subscriptionsChanged(context)
    }

    @JvmStatic
    fun updateNotifications(activity: Activity, enabled: Boolean) {
        if (enabled) {
            MorpheBoostNativeStatics.CheckNotificationsWorker_enable(activity)
        } else {
            MorpheBoostNativeStatics.CheckNotificationsWorker_disable(activity)
        }
    }

    @JvmStatic
    fun updateTrending(context: Context) {
        val searches = checkNotNull(RedditJraw.instance.trendingSearches()) { "Boost trending searches are not initialized" }
        if (searches.isEmpty()) MorpheBoostNativeStatics.TrendingWorker_schedule(context, 2)
    }

    @JvmStatic
    fun resetTips() {
        MorpheBoostNativeStatics.DoneUtils_getInstance().resetTips()
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
        MorpheBoostNativeStatics.Navigation_openSupport(context)
    }

    @JvmStatic
    fun rateApp(activity: Activity) {
        MorpheBoostNativeStatics.Navigation_rateApp(activity)
    }

    @JvmStatic
    fun openCommunity(activity: Activity, community: String) {
        MorpheBoostNativeStatics.Navigation_openCommunity(activity, community)
    }

    @JvmStatic
    fun openWiki(activity: Activity, community: String, page: String) {
        MorpheBoostNativeStatics.Navigation_openWiki(activity, community, page)
    }

    @JvmStatic
    fun openLicenses(context: Context) {
        context.startActivity(Intent(context, LicensesActivity::class.java))
    }

    @JvmStatic
    fun openUser(context: Context, username: String) {
        MorpheBoostNativeStatics.Navigation_openUser(context, username)
    }

    @JvmStatic
    fun auditValue(key: String): Any {
        val settings = MorpheBoostNativeStatics.BoostPreferences_getInstance()
        val prefs = settings.preferences
        return when (key) {
            "pref_left_handed" -> settings.leftHanded()
            "pref_cards_full_preview" -> settings.fullPreview()
            "pref_cards_rounded_corners" -> settings.roundedCorners()
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
            require(it is NativeSavedSearch) { "Invalid Boost saved-search entity" }
            SavedSearch(it.id, it.name ?: "", it.community ?: "", it.scopeId ?: "", it.query ?: "", it.sort ?: "", it.period ?: "")
        }
    }

    class SearchObservation internal constructor(
        private val data: LiveData,
        owner: LifecycleOwner,
        listener: SearchesListener,
    ) : AutoCloseable {
        private var closed = false
        private val observer = object : Observer {
            override fun onChanged(value: Any?) {
                if (!closed) listener.onChanged(searchRows(value))
            }
        }

        init {
            data.observe(owner, observer)
        }

        override fun close() {
            if (!closed) {
                closed = true
                data.removeObserver(observer)
            }
        }
    }

    class SavedSearches internal constructor(private val model: SavedSearchViewModel) {
        private val data: LiveData = checkNotNull(model.searches) { "Boost saved-search LiveData is not initialized" }

        fun current(): List<SavedSearch> = searchRows(data.getValue())

        fun observe(owner: Fragment, listener: SearchesListener): SearchObservation {
            require(owner is LifecycleOwner) { "Boost fragment is not a lifecycle owner" }
            return SearchObservation(data, owner, listener)
        }

        fun save(value: SavedSearch, update: Boolean, completion: SearchCompletion) {
            val entity = NativeSavedSearch()
            entity.id = value.id
            entity.name = value.name
            entity.community = value.community
            entity.scopeId = value.scopeId
            entity.query = value.query
            entity.sort = value.sort
            entity.period = value.period
            mutate(completion) { dao ->
                if (update) dao.updateAdapter.execute(entity) else dao.insertAdapter.insert(entity)
            }
        }

        fun delete(value: SavedSearch, completion: SearchCompletion) {
            val entity = NativeSavedSearch()
            entity.id = value.id
            mutate(completion) { dao -> dao.deleteAdapter.execute(entity) }
        }

        private fun mutate(completion: SearchCompletion, action: (SavedSearchDao) -> Unit) {
            val dao = model.repository.dao
            // These are the same Room transaction/adapter calls as cn2's inlined AsyncTasks.
            AsyncTask.execute(Runnable {
                val success = try {
                    dao.database.assertNotSuspendingTransaction()
                    dao.database.beginTransaction()
                    try {
                        action(dao)
                        dao.database.setTransactionSuccessful()
                    } finally {
                        dao.database.endTransaction()
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
            MorpheBoostNativeStatics.InjectorUtils_savedSearchViewModelFactory(activity),
            activity.getDefaultViewModelCreationExtras()
        )
        val type = MorpheBoostNativeStatics.Reflection_getKotlinClass(SavedSearchViewModel::class.java)
        val name = checkNotNull(type.getQualifiedName()) { "Saved-search ViewModel has no canonical name" }
        val model = provider.getViewModel(type, "androidx.lifecycle.ViewModelProvider.DefaultKey:$name")
        check(model is SavedSearchViewModel) { "Incorrect Boost saved-search ViewModel" }
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
            if (manager.backStackChangeListeners == null) manager.backStackChangeListeners = ArrayList<OnBackStackChangedListener>()
            checkNotNull(manager.backStackChangeListeners).add(listener)
        }

        fun count(): Int = manager.backStack?.size ?: 0

        fun push(name: String) {
            val transaction = BackStackRecord(manager)
            check(transaction.allowAddToBackStack) { "Boost transaction cannot be added to the back stack" }
            // beginTransaction/addToBackStack/commit were inlined in the shipped APK.
            transaction.addToBackStack = true
            transaction.mName = name
            transaction.commitInternal(false)
        }

        fun pop(): Boolean = manager.popBackStackImmediate()

        override fun close() {
            manager.backStackChangeListeners?.remove(listener)
        }
    }

    @JvmStatic
    fun editorBackStack(fragment: Fragment, changed: Runnable): EditorBackStack =
        EditorBackStack(
            checkNotNull(fragment.getParentFragmentManager()) { "Boost settings fragment has no parent FragmentManager" },
            changed
        )
}
