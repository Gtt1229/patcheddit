/*
 * Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (0226c2e).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.searchnav

import app.morphe.patches.reddit.customclients.boostforreddit.bindings.BoostApi

internal val searchAbstractActivityOnCreateFingerprint = BoostApi.SearchAbstractActivity.onCreate.fingerprint()

internal val drawerMaterialBottomNavigationFingerprint = BoostApi.DrawerActivity.setupBottomNavigation.fingerprint()

internal val mainActivityOnResumeFingerprint = BoostApi.MainActivity.onResume.fingerprint()

internal val subredditActivityOnCreateFingerprint = BoostApi.SubredditActivity.onCreate.fingerprint()

internal val drawerBottomNavigationVisibilityFingerprint = BoostApi.DrawerActivity.setBottomNavigationVisible.fingerprint()
internal val inboxCountUpdateFingerprint = BoostApi.DrawerActivity.updateInboxCount.fingerprint()

internal val searchAbstractSubredditsErrorCallbackFingerprint = BoostApi.SearchAbstractActivitySubredditErrorCallback.onError.fingerprint()

internal val emptyRecyclerViewBottomNavigationPaddingFingerprint = BoostApi.EmptyRecyclerView.emptyRecyclerViewBottomNavigationPadding.fingerprint()
