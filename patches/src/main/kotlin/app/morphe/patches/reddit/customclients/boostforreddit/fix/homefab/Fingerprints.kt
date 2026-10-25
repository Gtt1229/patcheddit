/*
 * Modifications Copyright 2026 brealorg.
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.homefab

import app.morphe.patcher.Fingerprint

internal val fabNestedScrollFingerprint = Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/customviews/ScrollAwareFABBehavior;",
    name = "s",
    returnType = "V",
    parameters = listOf(
        "Landroidx/coordinatorlayout/widget/CoordinatorLayout;",
        "Landroid/view/View;",
        "Landroid/view/View;",
        "I", "I", "I", "I",
    ),
)

internal val fabMenuNestedScrollFingerprint = Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/customviews/ScrollAwareFABMenuBehavior;",
    name = "s",
    returnType = "V",
    parameters = listOf(
        "Landroidx/coordinatorlayout/widget/CoordinatorLayout;",
        "Landroid/view/View;",
        "Landroid/view/View;",
        "I", "I", "I", "I",
    ),
)

internal val fabMiniNestedScrollFingerprint = Fingerprint(
    definingClass = "Lcom/rubenmayayo/reddit/ui/customviews/ScrollAwareFABMiniBehavior;",
    name = "s",
    returnType = "V",
    parameters = listOf(
        "Landroidx/coordinatorlayout/widget/CoordinatorLayout;",
        "Landroid/view/View;",
        "Landroid/view/View;",
        "I", "I", "I", "I",
    ),
)

private fun dependentViewFingerprint(
    definingClass: String,
    name: String,
    returnType: String,
) = Fingerprint(
    definingClass = definingClass,
    name = name,
    returnType = returnType,
    parameters = listOf(
        "Landroidx/coordinatorlayout/widget/CoordinatorLayout;",
        "Landroid/view/View;",
        "Landroid/view/View;",
    ),
)

private const val SCROLL_AWARE_FAB_MENU_BEHAVIOR =
    "Lcom/rubenmayayo/reddit/ui/customviews/ScrollAwareFABMenuBehavior;"
private const val SCROLL_AWARE_FAB_BOTTOM_NAVIGATION_BEHAVIOR =
    "Lcom/rubenmayayo/reddit/ui/customviews/ScrollAwareFABBehaviorBottomNavigation;"
internal val fabMenuDependentViewChangedFingerprint = dependentViewFingerprint(
    SCROLL_AWARE_FAB_MENU_BEHAVIOR, "i", "Z",
)

internal val fabMenuDependentViewRemovedFingerprint = dependentViewFingerprint(
    SCROLL_AWARE_FAB_MENU_BEHAVIOR, "j", "V",
)

// Inbox/Profile fab + fab_submit, Search fab_save and Home fab_random.
internal val fabBottomNavigationDependentViewChangedFingerprint = dependentViewFingerprint(
    SCROLL_AWARE_FAB_BOTTOM_NAVIGATION_BEHAVIOR, "i", "Z",
)

internal val fabBottomNavigationDependentViewRemovedFingerprint = dependentViewFingerprint(
    SCROLL_AWARE_FAB_BOTTOM_NAVIGATION_BEHAVIOR, "j", "V",
)
