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
import com.rubenmayayo.reddit.ui.submit.v2.SubmitGifvFragment
import app.morphe.extension.boost.api.MediaUploader
import app.morphe.extension.boost.api.MediaUploaderListener
import java.util.Locale
import app.morphe.extension.boost.api.ProgressRequestBody

object ExternalImageUploadFactory {
    const val CONTRACT_MARKER = "MORPHE_BOOST_EXTERNAL_IMAGE_HOST_FACTORY_ISSUE66_V1"

    @JvmStatic
    fun create(provider: String?): MediaUploader {
        val normalized = provider?.trim()?.lowercase(Locale.US) ?: "reddit"
        if (normalized == "imgbb") return ImgBbNativeUploader()

        val nativeProvider = when (normalized) {
            "imgur_free", "imgur_paid", "vgy" -> normalized
            else -> "reddit"
        }
        return try {
            requireNotNull(MorpheBoostNativeStatics.MediaUploadFactory_createOriginal(nativeProvider))
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

    private class UnavailableUploader(private val message: String) : MediaUploader {
        override fun uploadBatch(uploads: List<*>?, progress: app.morphe.extension.boost.api.ProgressRequestBody.ProgressListener?, listener: MediaUploaderListener?) {
            listener?.onFailure(message)
        }

        override fun uploadGifv(upload: Upload?, progress: SubmitGifvFragment.ProgressListener?, listener: SubmitGifvFragment.UploadListener?) {
            this.upload(upload, progress, listener)
        }

        override fun upload(upload: Upload?, progress: app.morphe.extension.boost.api.ProgressRequestBody.ProgressListener?, listener: MediaUploaderListener?) {
            listener?.onFailure(message)
        }
    }
}

private class ImgBbNativeUploader : MediaUploader {
    private val transport = ImgBbUploader()

    override fun uploadBatch(uploads: List<*>?, progress: app.morphe.extension.boost.api.ProgressRequestBody.ProgressListener?, listener: MediaUploaderListener?) {
        transport.uploadFirst(uploads, callbacks(progress, listener))
    }

    override fun uploadGifv(upload: Upload?, progress: SubmitGifvFragment.ProgressListener?, listener: SubmitGifvFragment.UploadListener?) {
        // GIFV posts complete from Imgur metadata; this callback ignores URL success.
        listener?.onFailure("Boost's GIFV post uploader requires Imgur. Use ImgBB for editor attachments instead.")
    }

    override fun upload(upload: Upload?, progress: app.morphe.extension.boost.api.ProgressRequestBody.ProgressListener?, listener: MediaUploaderListener?) {
        transport.upload(upload, callbacks(progress, listener))
    }

    private fun callbacks(progress: app.morphe.extension.boost.api.ProgressRequestBody.ProgressListener?, listener: MediaUploaderListener?) = object : ImgBbUploader.Callback {
        override fun onProgress(percent: Int) {
            progress?.onProgress(percent)
        }

        override fun onSuccess(url: String) {
            listener?.onSuccess(url)
        }

        override fun onFailure(exception: Exception, message: String) {
            listener?.onFailure(message)
        }
    }
}
