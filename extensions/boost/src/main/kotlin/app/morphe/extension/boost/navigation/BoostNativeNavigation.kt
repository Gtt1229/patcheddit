/*
 * Copyright 2026 brealorg.
 * Modifications Copyright 2026 wchill.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (0226c2e).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.extension.boost.navigation

import MorpheBoostNativeStatics
import android.app.Activity
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.f as MenuBuilder
import androidx.appcompat.view.menu.f.a as MenuBuilderCallback
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout.f as CoordinatorLayoutParams
import az1 as NavigationBarItemView
import com.google.android.material.badge.a as BadgeDrawable
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rubenmayayo.reddit.aa.User
import com.rubenmayayo.reddit.ui.activities.a as BoostActivity
import com.rubenmayayo.reddit.ui.customviews.dialogs.UserSelectorView
import com.rubenmayayo.reddit.ui.submissions.subreddit.SubredditActivity
import dp1.b as MaterialDialogBuilder
import lp1 as MaterialShapeDrawable
import pd2
import pj0 as DrawerActivity

/** Typed bindings to the official Boost 1.14.0 UI, not upstream library headers. */
object BoostNativeNavigation {
    interface SelectionCallback {
        fun onSelected(item: MenuItem): Boolean
        fun onReselected(item: MenuItem)
    }

    interface ProfileCallback {
        fun onAddAccount()
        fun onAccountSelected(username: String?, anonymous: Boolean, index: Int)
    }

    @JvmStatic
    fun menu(view: View): Menu = checkNotNull((view as BottomNavigationView).getMenu()) {
        "Boost navigation menu is not initialized"
    }

    @JvmStatic
    fun setPresentation(view: View) {
        val navigation = view as BottomNavigationView
        navigation.setLabelVisibilityMode(2)
        navigation.setItemHorizontalTranslationEnabled(false)
    }

    @JvmStatic
    fun setTint(view: View, tint: ColorStateList) {
        val navigation = view as BottomNavigationView
        navigation.setItemIconTintList(tint)
        navigation.setItemTextColor(tint)
    }

    @JvmStatic
    fun setItemIconTint(view: View, tint: ColorStateList) {
        (view as NavigationBarItemView).setIconTintList(tint)
    }

    @JvmStatic
    fun fillColor(drawable: Drawable): ColorStateList? =
        (drawable as? MaterialShapeDrawable)?.b?.c

    @JvmStatic
    fun setSelectionCallback(view: View, callback: SelectionCallback) {
        val navigation = view as BottomNavigationView
        val nativeMenu = menu(view) as MenuBuilder
        // R8 erased NavigationBarView's listener methods and hard-casts their
        // implementations. The retained MenuBuilder callback is extensible.
        nativeMenu.u(object : MenuBuilderCallback {
            override fun a(menu: MenuBuilder?, item: MenuItem?): Boolean {
                val selected = checkNotNull(item) {
                    "Boost navigation callback did not supply a menu item"
                }
                if (selected.itemId == navigation.getSelectedItemId()) {
                    callback.onReselected(selected)
                    return true
                }
                return !callback.onSelected(selected)
            }

            override fun b(menu: MenuBuilder?) = Unit
        })
    }

    @JvmStatic
    fun coordinatorBehavior(view: View): Any? =
        (view.layoutParams as CoordinatorLayoutParams).a

    @JvmStatic
    fun applyHideOnScroll(view: View, revealScrolledBar: Boolean): Boolean {
        val enabled = MorpheBoostNativeStatics.ns2_m().a
            .getBoolean("pref_bottom_navigation_hide_on_scroll", true)
        val params = view.layoutParams as CoordinatorLayoutParams
        var behavior = params.a
        if (enabled) {
            if (revealScrolledBar && view.translationY != 0f) {
                view.clearAnimation()
                view.animate().cancel()
                view.translationY = 0f
                behavior = null
            }
            if (behavior !is HideBottomViewOnScrollBehavior) {
                params.b(HideBottomViewOnScrollBehavior())
            }
        } else {
            view.clearAnimation()
            view.animate().cancel()
            params.b(null)
            view.translationY = 0f
        }
        view.layoutParams = params
        return enabled
    }

    @JvmStatic
    fun applyInboxBadge(view: View, itemId: Int, count: Int) {
        val navigation = view as BottomNavigationView
        val menuView = navigation.b
        val itemView = navigation.findViewById<View>(itemId) as NavigationBarItemView?
        if (count <= 0) {
            val badge = itemView?.v0
            if (itemView != null && badge != null) {
                itemView.setClipChildren(true)
                itemView.setClipToPadding(true)
                val parent = badge.e()
                if (parent == null) {
                    itemView.O.overlay.remove(badge)
                } else {
                    parent.foreground = null
                }
                itemView.v0 = null
            }
            menuView.Q.put(itemId, null)
            return
        }

        val badge = (menuView.Q.get(itemId) as BadgeDrawable?)
            ?: BadgeDrawable(navigation.context, null).also { menuView.Q.put(itemId, it) }
        itemView?.setBadge(badge)

        // Boost inlined BadgeDrawable.setNumber/setVisible into DrawerActivity.d2.
        val state = badge.z
        val number = maxOf(0, count)
        if (state.b.F != number) {
            state.a.F = number
            state.b.F = number
            if (!state.a()) {
                badge.c.e = true
                badge.i()
                badge.l()
                badge.invalidateSelf()
            }
        }
        state.a.O = true
        state.b.O = true
        badge.setVisible(true, false)
    }

    @JvmStatic
    fun isDrawerActivity(activity: Activity): Boolean = activity is DrawerActivity

    @JvmStatic
    fun profileLongPress(activity: Activity): View.OnLongClickListener =
        View.OnLongClickListener {
            (activity as DrawerActivity).G1(4, false)
            true
        }

    @JvmStatic
    fun subscriptionsLongPress(activity: Activity): View.OnLongClickListener =
        View.OnLongClickListener {
            (activity as DrawerActivity).G1(2, false)
            true
        }

    @JvmStatic
    fun showProfileSelector(
        activity: Activity,
        titleResource: Int,
        callback: ProfileCallback,
    ): Dialog {
        val selector = UserSelectorView(activity, true)
        selector.setCallback(object : UserSelectorView.a {
            override fun U() = callback.onAddAccount()

            override fun h(user: User?, index: Int) =
                callback.onAccountSelected(user?.name, user == null, index)
        })
        val builder = MaterialDialogBuilder(activity)
        builder.q(titleResource)
        builder.e(selector, false)
        return checkNotNull(builder.p()) {
            "Boost profile selector dialog was not created"
        }
    }

    @JvmStatic
    fun addAccount(activity: Activity) = MorpheBoostNativeStatics.xy1_x(activity)

    @JvmStatic
    fun selectAccount(activity: Activity, index: Int) =
        (activity as DrawerActivity).K1(index)

    @JvmStatic
    fun isLoggedIn(): Boolean = pd2.r.a != null

    @JvmStatic
    fun currentUsername(): String? = pd2.r.a()

    @JvmStatic
    fun openProfile(activity: Activity, username: String) =
        (activity as BoostActivity).openProfile(username)

    @JvmStatic
    fun currentCommunity(activity: Activity?): String? =
        if (activity is DrawerActivity) activity.o1() else null

    @JvmStatic
    fun openCurrentSubredditSubscriptions(activity: Activity): Boolean =
        (activity as SubredditActivity).F1()

    @JvmStatic
    fun tintToolbar(view: View, color: Int) {
        if (view is Toolbar) {
            view.setTitleTextColor(color)
            view.setSubtitleTextColor(color)
            view.getNavigationIcon()?.let {
                it.mutate().setTint(color)
                view.setNavigationIcon(it)
            }
            view.getOverflowIcon()?.let {
                it.mutate().setTint(color)
                view.setOverflowIcon(it)
            }
        } else if (view is android.widget.Toolbar) {
            view.setTitleTextColor(color)
            view.setSubtitleTextColor(color)
            view.navigationIcon?.let {
                it.mutate().setTint(color)
                view.navigationIcon = it
            }
            view.overflowIcon?.let {
                it.mutate().setTint(color)
                view.overflowIcon = it
            }
        }
    }
}
