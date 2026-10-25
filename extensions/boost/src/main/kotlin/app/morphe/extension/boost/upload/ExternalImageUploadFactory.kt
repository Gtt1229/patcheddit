/*
 * Modifications Copyright 2026 brealorg.
 * Copyright 2026 wchill.
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 */

package app.morphe.extension.boost.upload

import MorpheBoostNativeStatics
import android.util.Log
import com.rubenmayayo.reddit.models.imgur.Upload
import com.rubenmayayo.reddit.ui.submit.v2.g
import cs1
import ds1
import java.util.Locale
import z92

object ExternalImageUploadFactory {
    const val CONTRACT_MARKER = "MORPHE_BOOST_EXTERNAL_IMAGE_HOST_FACTORY_ISSUE66_V1"

    @JvmStatic
    fun create(provider: String?): cs1 {
        val normalized = provider?.trim()?.lowercase(Locale.US) ?: "reddit"
        if (normalized == "imgbb") return ImgBbNativeUploader()

        val nativeProvider = when (normalized) {
            "imgur_free", "imgur_paid", "vgy" -> normalized
            else -> "reddit"
        }
        return try {
            requireNotNull(MorpheBoostNativeStatics.fl1_morpheCreateOriginalUploader(nativeProvider))
        } catch (throwable: Throwable) {
            Log.w("MorpheImageUpload", "$CONTRACT_MARKER: native provider=$nativeProvider failed", throwable)
            val message = when (nativeProvider) {
                "imgur_free" -> "Boost's legacy Imgur uploader is unavailable."
                "imgur_paid" -> "Boost's paid Imgur uploader is unavailable."
                "vgy" -> "Boost's legacy vgy uploader is unavailable."
                else -> "Boost's native Reddit uploader is unavailable."
            }
            UnavailableUploader(message)
        }
    }

    private class UnavailableUploader(private val message: String) : cs1 {
        override fun a(uploads: List<*>?, progress: z92.b?, listener: ds1?) {
            listener?.a(message)
        }

        override fun b(upload: Upload?, progress: g.b?, listener: g.c?) {
            c(upload, progress, listener)
        }

        override fun c(upload: Upload?, progress: z92.b?, listener: ds1?) {
            listener?.a(message)
        }
    }
}

private class ImgBbNativeUploader : cs1 {
    private val transport = ImgBbUploader()

    override fun a(uploads: List<*>?, progress: z92.b?, listener: ds1?) {
        transport.uploadFirst(uploads, callbacks(progress, listener))
    }

    override fun b(upload: Upload?, progress: g.b?, listener: g.c?) {
        // GIFV posts complete from Imgur metadata; this callback ignores URL success.
        listener?.a("Boost's GIFV post uploader requires Imgur. Use ImgBB for editor attachments instead.")
    }

    override fun c(upload: Upload?, progress: z92.b?, listener: ds1?) {
        transport.upload(upload, callbacks(progress, listener))
    }

    private fun callbacks(progress: z92.b?, listener: ds1?) = object : ImgBbUploader.Callback {
        override fun onProgress(percent: Int) {
            progress?.a(percent)
        }

        override fun onSuccess(url: String) {
            listener?.b(url)
        }

        override fun onFailure(exception: Exception, message: String) {
            listener?.a(message)
        }
    }
}
