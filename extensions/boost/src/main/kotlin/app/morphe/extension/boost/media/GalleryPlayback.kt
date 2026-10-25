/*
 * Copyright 2026 wchill.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.extension.boost.media

import MorpheBoostNativeStatics
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.View
import com.google.android.exoplayer2.i as ExoPlayerImpl
import com.google.android.exoplayer2.source.a as BaseMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.m.b as ProgressiveMediaSourceFactory
import com.google.android.exoplayer2.ui.PlayerView
import nt
import sb0 as DefaultExtractorsFactory
import sq0 as ExoPlayerBuilder
import y12.a as OkHttpDataSourceFactory

class GalleryPlayback private constructor(private val player: ExoPlayerImpl) {
    fun attach(view: View) {
        (view as PlayerView).setPlayer(player)
    }

    fun prepare(context: Context, url: String, sourceType: Int) {
        val dataSource = OkHttpDataSourceFactory(nt.a)
        dataSource.c = userAgent(context)
        val item = requireNotNull(MorpheBoostNativeStatics.o_a(Uri.parse(url)))
        val source = when (sourceType) {
            2 -> DashMediaSource.Factory(dataSource).b(item)
            3 -> HlsMediaSource.Factory(dataSource).b(item)
            else -> ProgressiveMediaSourceFactory(dataSource, DefaultExtractorsFactory()).a(item)
        }

        player.o0(0.0f)
        player.y(2)
        player.i0(source as BaseMediaSource)
        player.a()
        player.l0(true)
    }

    fun getPlaybackState(): Int = player.q()

    fun release() {
        player.f0()
    }

    companion object {
        @JvmStatic
        fun create(context: Context): GalleryPlayback =
            GalleryPlayback(requireNotNull(ExoPlayerBuilder(context).a()))

        @JvmStatic
        fun createView(context: Context): View =
            PlayerView(context, null).apply {
                setUseController(false)
                setResizeMode(0)
                setShutterBackgroundColor(Color.TRANSPARENT)
                setBackgroundColor(Color.TRANSPARENT)
                isClickable = false
                isFocusable = false
            }

        @JvmStatic
        fun detach(view: View) {
            (view as PlayerView).setPlayer(null)
        }

        private fun userAgent(context: Context): String {
            val version = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (_: PackageManager.NameNotFoundException) {
                "?"
            }
            return "Boost/$version (Linux;Android ${Build.VERSION.RELEASE}) "
        }
    }
}
