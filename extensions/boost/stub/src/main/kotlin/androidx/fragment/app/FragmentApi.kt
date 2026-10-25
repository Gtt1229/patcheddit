/*
 * Copyright 2026 wchill.
 *
 * Compile-only declarations for the verified Boost 1.14.0 API.
 * These placeholder implementations must never be packaged with the extension.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantVisibilityModifier")

package androidx.fragment.app

import s3 as ActivityCompat
import z3 as ActivityResultCallback
import a4 as ActivityResultContract
import e4 as ActivityResultLauncher
import j4 as ActivityResultRegistry
import androidx.fragment.app.a as BackStackRecord
import ny as Boost_ny
import b60 as CreationExtras
import androidx.fragment.app.g as FragmentActivity
import pz0 as FragmentContainer
import androidx.fragment.app.i as FragmentFactory
import rz0 as FragmentHostCallback
import androidx.fragment.app.m as FragmentStateManager
import androidx.fragment.app.p as FragmentTransaction
import i61 as HasDefaultViewModelProviderFactory
import ni1 as Lifecycle
import ui1 as LifecycleOwner
import ak1 as LiveData
import jk1 as LoaderManager
import androidx.fragment.app.`FragmentManager$l` as OnBackStackChangedListener
import kl2 as SavedStateRegistry
import pl2 as SavedStateRegistryOwner
import at2 as SharedElementCallback
import mx2 as SpecialEffectsControllerFactory
import om3 as ViewModelProvider
import qm3 as ViewModelStore
import rm3 as ViewModelStoreOwner

private fun <T> boost114Stub(): T = throw UnsupportedOperationException("Compile-only Boost 1.14 API stub")

open class Fragment protected constructor(stub: Unit) : android.content.ComponentCallbacks, android.view.View.OnCreateContextMenuListener, LifecycleOwner, ViewModelStoreOwner, HasDefaultViewModelProviderFactory, SavedStateRegistryOwner {
    constructor() : this(Unit) { boost114Stub<Unit>() }
    fun getActivity(): FragmentActivity? = boost114Stub()
    fun getTag(): String? = boost114Stub()
    open fun getUserVisibleHint(): Boolean = boost114Stub()
    fun getParentFragmentManager(): androidx.fragment.app.FragmentManager? = boost114Stub()
    open fun callStartTransitionListener(p0: Boolean): Unit = boost114Stub()
    open fun createFragmentContainer(): FragmentContainer? = boost114Stub()
    open fun dump(p0: String?, p1: java.io.FileDescriptor?, p2: java.io.PrintWriter?, p3: Array<String>?): Unit = boost114Stub()
    override fun equals(p0: Any?): Boolean = boost114Stub()
    open fun f0(): android.app.Activity? = boost114Stub()
    open fun findFragmentByWho(p0: String?): androidx.fragment.app.Fragment? = boost114Stub()
    open fun generateActivityResultKey(): String? = boost114Stub()
    open fun getAllowEnterTransitionOverlap(): Boolean = boost114Stub()
    open fun getAllowReturnTransitionOverlap(): Boolean = boost114Stub()
    open fun getAnimatingAway(): android.view.View? = boost114Stub()
    fun getArguments(): android.os.Bundle? = boost114Stub()
    fun getChildFragmentManager(): androidx.fragment.app.FragmentManager? = boost114Stub()
    open fun getContext(): android.content.Context? = boost114Stub()
    override fun getDefaultViewModelCreationExtras(): CreationExtras? = boost114Stub()
    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.b? = boost114Stub()
    open fun getEnterAnim(): Int = boost114Stub()
    open fun getEnterTransition(): Any? = boost114Stub()
    open fun getEnterTransitionCallback(): SharedElementCallback? = boost114Stub()
    open fun getExitAnim(): Int = boost114Stub()
    open fun getExitTransition(): Any? = boost114Stub()
    open fun getExitTransitionCallback(): SharedElementCallback? = boost114Stub()
    open fun getFocusedView(): android.view.View? = boost114Stub()
    fun getFragmentManager(): androidx.fragment.app.FragmentManager? = boost114Stub()
    fun getHost(): Any? = boost114Stub()
    fun getId(): Int = boost114Stub()
    fun getLayoutInflater(): android.view.LayoutInflater? = boost114Stub()
    open fun getLayoutInflater(p0: android.os.Bundle?): android.view.LayoutInflater? = boost114Stub()
    override fun getLifecycle(): Lifecycle? = boost114Stub()
    open fun getLoaderManager(): LoaderManager? = boost114Stub()
    open fun getNextTransition(): Int = boost114Stub()
    fun getParentFragment(): androidx.fragment.app.Fragment? = boost114Stub()
    open fun getPopDirection(): Boolean = boost114Stub()
    open fun getPopEnterAnim(): Int = boost114Stub()
    open fun getPopExitAnim(): Int = boost114Stub()
    open fun getPostOnViewCreatedAlpha(): Float = boost114Stub()
    open fun getReenterTransition(): Any? = boost114Stub()
    fun getResources(): android.content.res.Resources? = boost114Stub()
    fun getRetainInstance(): Boolean = boost114Stub()
    open fun getReturnTransition(): Any? = boost114Stub()
    fun getSavedStateRegistry(): SavedStateRegistry? = boost114Stub()
    open fun getSharedElementEnterTransition(): Any? = boost114Stub()
    open fun getSharedElementReturnTransition(): Any? = boost114Stub()
    open fun getSharedElementSourceNames(): java.util.ArrayList<*>? = boost114Stub()
    open fun getSharedElementTargetNames(): java.util.ArrayList<*>? = boost114Stub()
    fun getString(p0: Int): String? = boost114Stub()
    fun getString(p0: Int, p1: Array<Any>?): String? = boost114Stub()
    fun getTargetFragment(): androidx.fragment.app.Fragment? = boost114Stub()
    fun getTargetRequestCode(): Int = boost114Stub()
    fun getText(p0: Int): CharSequence? = boost114Stub()
    open fun getView(): android.view.View? = boost114Stub()
    open fun getViewLifecycleOwner(): LifecycleOwner? = boost114Stub()
    open fun getViewLifecycleOwnerLiveData(): LiveData? = boost114Stub()
    override fun getViewModelStore(): ViewModelStore? = boost114Stub()
    fun hasOptionsMenu(): Boolean = boost114Stub()
    override fun hashCode(): Int = boost114Stub()
    open fun initState(): Unit = boost114Stub()
    fun isAdded(): Boolean = boost114Stub()
    fun isDetached(): Boolean = boost114Stub()
    fun isHidden(): Boolean = boost114Stub()
    fun isInBackStack(): Boolean = boost114Stub()
    fun isInLayout(): Boolean = boost114Stub()
    fun isMenuVisible(): Boolean = boost114Stub()
    open fun isPostponed(): Boolean = boost114Stub()
    fun isRemoving(): Boolean = boost114Stub()
    fun isResumed(): Boolean = boost114Stub()
    fun isStateSaved(): Boolean = boost114Stub()
    fun isVisible(): Boolean = boost114Stub()
    open fun noteStateNotSaved(): Unit = boost114Stub()
    open fun onActivityCreated(p0: android.os.Bundle?): Unit = boost114Stub()
    open fun onActivityResult(p0: Int, p1: Int, p2: android.content.Intent?): Unit = boost114Stub()
    open fun onAttach(p0: android.app.Activity?): Unit = boost114Stub()
    open fun onAttach(p0: android.content.Context?): Unit = boost114Stub()
    open fun onAttachFragment(p0: androidx.fragment.app.Fragment?): Unit = boost114Stub()
    override fun onConfigurationChanged(p0: android.content.res.Configuration): Unit = boost114Stub()
    open fun onContextItemSelected(p0: android.view.MenuItem?): Boolean = boost114Stub()
    open fun onCreate(p0: android.os.Bundle?): Unit = boost114Stub()
    open fun onCreateAnimation(p0: Int, p1: Boolean, p2: Int): android.view.animation.Animation? = boost114Stub()
    open fun onCreateAnimator(p0: Int, p1: Boolean, p2: Int): android.animation.Animator? = boost114Stub()
    override fun onCreateContextMenu(p0: android.view.ContextMenu?, p1: android.view.View?, p2: android.view.ContextMenu.ContextMenuInfo?): Unit = boost114Stub()
    open fun onCreateOptionsMenu(p0: android.view.Menu?, p1: android.view.MenuInflater?): Unit = boost114Stub()
    open fun onCreateView(p0: android.view.LayoutInflater?, p1: android.view.ViewGroup?, p2: android.os.Bundle?): android.view.View? = boost114Stub()
    open fun onDestroy(): Unit = boost114Stub()
    open fun onDestroyOptionsMenu(): Unit = boost114Stub()
    open fun onDestroyView(): Unit = boost114Stub()
    open fun onDetach(): Unit = boost114Stub()
    open fun onGetLayoutInflater(p0: android.os.Bundle?): android.view.LayoutInflater? = boost114Stub()
    open fun onHiddenChanged(p0: Boolean): Unit = boost114Stub()
    open fun onInflate(p0: android.app.Activity?, p1: android.util.AttributeSet?, p2: android.os.Bundle?): Unit = boost114Stub()
    open fun onInflate(p0: android.content.Context?, p1: android.util.AttributeSet?, p2: android.os.Bundle?): Unit = boost114Stub()
    override fun onLowMemory(): Unit = boost114Stub()
    open fun onMultiWindowModeChanged(p0: Boolean): Unit = boost114Stub()
    open fun onOptionsItemSelected(p0: android.view.MenuItem?): Boolean = boost114Stub()
    open fun onOptionsMenuClosed(p0: android.view.Menu?): Unit = boost114Stub()
    open fun onPause(): Unit = boost114Stub()
    open fun onPictureInPictureModeChanged(p0: Boolean): Unit = boost114Stub()
    open fun onPrepareOptionsMenu(p0: android.view.Menu?): Unit = boost114Stub()
    open fun onPrimaryNavigationFragmentChanged(p0: Boolean): Unit = boost114Stub()
    open fun onRequestPermissionsResult(p0: Int, p1: Array<String>?, p2: IntArray?): Unit = boost114Stub()
    open fun onResume(): Unit = boost114Stub()
    open fun onSaveInstanceState(p0: android.os.Bundle?): Unit = boost114Stub()
    open fun onStart(): Unit = boost114Stub()
    open fun onStop(): Unit = boost114Stub()
    open fun onViewCreated(p0: android.view.View?, p1: android.os.Bundle?): Unit = boost114Stub()
    open fun onViewStateRestored(p0: android.os.Bundle?): Unit = boost114Stub()
    open fun performActivityCreated(p0: android.os.Bundle?): Unit = boost114Stub()
    open fun performAttach(): Unit = boost114Stub()
    open fun performConfigurationChanged(p0: android.content.res.Configuration?): Unit = boost114Stub()
    open fun performContextItemSelected(p0: android.view.MenuItem?): Boolean = boost114Stub()
    open fun performCreate(p0: android.os.Bundle?): Unit = boost114Stub()
    open fun performCreateOptionsMenu(p0: android.view.Menu?, p1: android.view.MenuInflater?): Boolean = boost114Stub()
    open fun performCreateView(p0: android.view.LayoutInflater?, p1: android.view.ViewGroup?, p2: android.os.Bundle?): Unit = boost114Stub()
    open fun performDestroy(): Unit = boost114Stub()
    open fun performDestroyView(): Unit = boost114Stub()
    open fun performDetach(): Unit = boost114Stub()
    open fun performGetLayoutInflater(p0: android.os.Bundle?): android.view.LayoutInflater? = boost114Stub()
    open fun performLowMemory(): Unit = boost114Stub()
    open fun performMultiWindowModeChanged(p0: Boolean): Unit = boost114Stub()
    open fun performOptionsItemSelected(p0: android.view.MenuItem?): Boolean = boost114Stub()
    open fun performOptionsMenuClosed(p0: android.view.Menu?): Unit = boost114Stub()
    open fun performPause(): Unit = boost114Stub()
    open fun performPictureInPictureModeChanged(p0: Boolean): Unit = boost114Stub()
    open fun performPrepareOptionsMenu(p0: android.view.Menu?): Boolean = boost114Stub()
    open fun performPrimaryNavigationFragmentChanged(): Unit = boost114Stub()
    open fun performResume(): Unit = boost114Stub()
    open fun performSaveInstanceState(p0: android.os.Bundle?): Unit = boost114Stub()
    open fun performStart(): Unit = boost114Stub()
    open fun performStop(): Unit = boost114Stub()
    open fun performViewCreated(): Unit = boost114Stub()
    open fun postponeEnterTransition(): Unit = boost114Stub()
    fun postponeEnterTransition(p0: Long, p1: java.util.concurrent.TimeUnit?): Unit = boost114Stub()
    fun registerForActivityResult(p0: ActivityResultContract?, p1: ActivityResultRegistry?, p2: ActivityResultCallback?): ActivityResultLauncher? = boost114Stub()
    fun registerForActivityResult(p0: ActivityResultContract?, p1: ActivityResultCallback?): ActivityResultLauncher? = boost114Stub()
    open fun registerForContextMenu(p0: android.view.View?): Unit = boost114Stub()
    fun requestPermissions(p0: Array<String>?, p1: Int): Unit = boost114Stub()
    fun requireActivity(): FragmentActivity? = boost114Stub()
    fun requireArguments(): android.os.Bundle? = boost114Stub()
    fun requireContext(): android.content.Context? = boost114Stub()
    fun requireFragmentManager(): androidx.fragment.app.FragmentManager? = boost114Stub()
    fun requireHost(): Any? = boost114Stub()
    fun requireParentFragment(): androidx.fragment.app.Fragment? = boost114Stub()
    fun requireView(): android.view.View? = boost114Stub()
    open fun restoreChildFragmentState(): Unit = boost114Stub()
    fun restoreViewState(p0: android.os.Bundle?): Unit = boost114Stub()
    open fun setAllowEnterTransitionOverlap(p0: Boolean): Unit = boost114Stub()
    open fun setAllowReturnTransitionOverlap(p0: Boolean): Unit = boost114Stub()
    open fun setAnimations(p0: Int, p1: Int, p2: Int, p3: Int): Unit = boost114Stub()
    open fun setArguments(p0: android.os.Bundle?): Unit = boost114Stub()
    open fun setEnterSharedElementCallback(p0: SharedElementCallback?): Unit = boost114Stub()
    open fun setEnterTransition(p0: Any?): Unit = boost114Stub()
    open fun setExitSharedElementCallback(p0: SharedElementCallback?): Unit = boost114Stub()
    open fun setExitTransition(p0: Any?): Unit = boost114Stub()
    open fun setFocusedView(p0: android.view.View?): Unit = boost114Stub()
    open fun setHasOptionsMenu(p0: Boolean): Unit = boost114Stub()
    open fun setInitialSavedState(p0: androidx.fragment.app.Fragment.SavedState?): Unit = boost114Stub()
    open fun setMenuVisibility(p0: Boolean): Unit = boost114Stub()
    open fun setNextTransition(p0: Int): Unit = boost114Stub()
    open fun setPopDirection(p0: Boolean): Unit = boost114Stub()
    open fun setPostOnViewCreatedAlpha(p0: Float): Unit = boost114Stub()
    open fun setReenterTransition(p0: Any?): Unit = boost114Stub()
    open fun setRetainInstance(p0: Boolean): Unit = boost114Stub()
    open fun setReturnTransition(p0: Any?): Unit = boost114Stub()
    open fun setSharedElementEnterTransition(p0: Any?): Unit = boost114Stub()
    open fun setSharedElementNames(p0: java.util.ArrayList<String>?, p1: java.util.ArrayList<String>?): Unit = boost114Stub()
    open fun setSharedElementReturnTransition(p0: Any?): Unit = boost114Stub()
    open fun setTargetFragment(p0: androidx.fragment.app.Fragment?, p1: Int): Unit = boost114Stub()
    open fun setUserVisibleHint(p0: Boolean): Unit = boost114Stub()
    open fun shouldShowRequestPermissionRationale(p0: String?): Boolean = boost114Stub()
    open fun startActivity(p0: android.content.Intent?): Unit = boost114Stub()
    open fun startActivity(p0: android.content.Intent?, p1: android.os.Bundle?): Unit = boost114Stub()
    open fun startActivityForResult(p0: android.content.Intent?, p1: Int): Unit = boost114Stub()
    open fun startActivityForResult(p0: android.content.Intent?, p1: Int, p2: android.os.Bundle?): Unit = boost114Stub()
    open fun startIntentSenderForResult(p0: android.content.IntentSender?, p1: Int, p2: android.content.Intent?, p3: Int, p4: Int, p5: Int, p6: android.os.Bundle?): Unit = boost114Stub()
    open fun startPostponedEnterTransition(): Unit = boost114Stub()
    override fun toString(): String = boost114Stub()
    open fun unregisterForContextMenu(p0: android.view.View?): Unit = boost114Stub()
    companion object {
        @JvmStatic fun instantiate(p0: android.content.Context?, p1: String?): androidx.fragment.app.Fragment? = boost114Stub()
        @JvmStatic fun instantiate(p0: android.content.Context?, p1: String?, p2: android.os.Bundle?): androidx.fragment.app.Fragment? = boost114Stub()
        @JvmStatic fun l1(p0: androidx.fragment.app.Fragment?): Unit = boost114Stub()
    }
    abstract class SavedState protected constructor(stub: Unit) : android.os.Parcelable {
    }
}

interface `FragmentManager$l` {
    fun onBackStackChanged(): Unit
}

abstract class FragmentManager protected constructor(stub: Unit) {
    @JvmField var d: java.util.ArrayList<BackStackRecord>? = boost114Stub()
    @JvmField var l: java.util.ArrayList<OnBackStackChangedListener>? = boost114Stub()
    fun O(): Boolean = boost114Stub()
    fun A(p0: java.util.ArrayList<BackStackRecord>?, p1: java.util.ArrayList<Boolean>?, p2: Int, p3: Int): Unit = boost114Stub()
    fun B(p0: Int): androidx.fragment.app.Fragment? = boost114Stub()
    fun C(p0: String?): androidx.fragment.app.Fragment? = boost114Stub()
    fun D(p0: androidx.fragment.app.Fragment?): android.view.ViewGroup? = boost114Stub()
    fun E(): FragmentFactory? = boost114Stub()
    fun F(): SpecialEffectsControllerFactory? = boost114Stub()
    fun G(p0: androidx.fragment.app.Fragment?): Unit = boost114Stub()
    fun J(): Boolean = boost114Stub()
    fun L(p0: Int, p1: Boolean): Unit = boost114Stub()
    fun M(): Unit = boost114Stub()
    fun N(p0: Int, p1: Boolean): Unit = boost114Stub()
    fun P(p0: Int, p1: Int): Boolean = boost114Stub()
    fun Q(p0: java.util.ArrayList<*>?, p1: java.util.ArrayList<*>?, p2: Int, p3: Int): Boolean = boost114Stub()
    fun R(p0: androidx.fragment.app.Fragment?): Unit = boost114Stub()
    fun S(p0: java.util.ArrayList<BackStackRecord>?, p1: java.util.ArrayList<Boolean>?): Unit = boost114Stub()
    fun T(p0: android.os.Bundle?): Unit = boost114Stub()
    fun U(): android.os.Bundle? = boost114Stub()
    fun V(): Unit = boost114Stub()
    fun W(p0: androidx.fragment.app.Fragment?, p1: Boolean): Unit = boost114Stub()
    fun X(p0: androidx.fragment.app.Fragment?, p1: Lifecycle.b?): Unit = boost114Stub()
    fun Y(p0: androidx.fragment.app.Fragment?): Unit = boost114Stub()
    fun Z(p0: androidx.fragment.app.Fragment?): Unit = boost114Stub()
    fun a(p0: androidx.fragment.app.Fragment?): FragmentStateManager? = boost114Stub()
    fun b(p0: FragmentHostCallback?, p1: FragmentContainer?, p2: androidx.fragment.app.Fragment?): Unit = boost114Stub()
    fun b0(): Unit = boost114Stub()
    fun c(p0: androidx.fragment.app.Fragment?): Unit = boost114Stub()
    fun c0(p0: java.lang.IllegalStateException?): Unit = boost114Stub()
    fun d(): Unit = boost114Stub()
    fun d0(): Unit = boost114Stub()
    fun e(): java.util.HashSet<*>? = boost114Stub()
    fun f(p0: androidx.fragment.app.Fragment?): FragmentStateManager? = boost114Stub()
    fun g(p0: androidx.fragment.app.Fragment?): Unit = boost114Stub()
    fun h(p0: Boolean, p1: android.content.res.Configuration?): Unit = boost114Stub()
    fun i(p0: android.view.MenuItem?): Boolean = boost114Stub()
    fun j(p0: android.view.Menu?, p1: android.view.MenuInflater?): Boolean = boost114Stub()
    fun k(): Unit = boost114Stub()
    fun l(p0: Boolean): Unit = boost114Stub()
    fun m(p0: Boolean, p1: Boolean): Unit = boost114Stub()
    fun n(): Unit = boost114Stub()
    fun o(p0: android.view.MenuItem?): Boolean = boost114Stub()
    fun p(p0: android.view.Menu?): Unit = boost114Stub()
    fun q(p0: androidx.fragment.app.Fragment?): Unit = boost114Stub()
    fun r(p0: Boolean, p1: Boolean): Unit = boost114Stub()
    fun s(p0: android.view.Menu?): Boolean = boost114Stub()
    fun t(p0: Int): Unit = boost114Stub()
    override fun toString(): String = boost114Stub()
    fun u(): Unit = boost114Stub()
    fun v(p0: String?, p1: java.io.FileDescriptor?, p2: java.io.PrintWriter?, p3: Array<String>?): Unit = boost114Stub()
    fun w(p0: androidx.fragment.app.FragmentManager.m?, p1: Boolean): Unit = boost114Stub()
    fun x(p0: Boolean): Unit = boost114Stub()
    fun y(p0: Boolean): Boolean = boost114Stub()
    fun z(p0: BackStackRecord?, p1: Boolean): Unit = boost114Stub()
    companion object {
        @JvmStatic fun H(p0: Int): Boolean = boost114Stub()
        @JvmStatic fun I(p0: androidx.fragment.app.Fragment?): Boolean = boost114Stub()
        @JvmStatic fun K(p0: androidx.fragment.app.Fragment?): Boolean = boost114Stub()
        @JvmStatic fun a0(p0: androidx.fragment.app.Fragment?): Unit = boost114Stub()
    }
    interface j {
    }
    interface m {
    }
}

open class a protected constructor(stub: Unit) : FragmentTransaction(Unit), androidx.fragment.app.FragmentManager.j, androidx.fragment.app.FragmentManager.m {
    constructor(p0: androidx.fragment.app.FragmentManager?) : this(Unit) { boost114Stub<Unit>() }
    fun g(p0: Boolean): Int = boost114Stub()
}

abstract class f protected constructor(stub: Unit) : androidx.fragment.app.Fragment(Unit), android.content.DialogInterface.OnCancelListener, android.content.DialogInterface.OnDismissListener {
}

abstract class g protected constructor(stub: Unit) : Boost_ny(Unit), ActivityCompat.a {
}

abstract class i protected constructor(stub: Unit) {
}

abstract class m protected constructor(stub: Unit) {
}

abstract class p protected constructor(stub: Unit) {
    @JvmField var g: Boolean = boost114Stub()
    @JvmField var h: Boolean = boost114Stub()
    @JvmField var i: String = boost114Stub()
}
