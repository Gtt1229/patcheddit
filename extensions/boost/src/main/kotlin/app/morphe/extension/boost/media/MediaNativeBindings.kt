/*
 * Copyright 2026 wchill.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.extension.boost.media

import MorpheBoostNativeStatics
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView.ViewHolder as RecyclerViewViewHolder
import com.rubenmayayo.reddit.models.generic.ImageModel
import com.rubenmayayo.reddit.models.reddit.CommentModel
import com.rubenmayayo.reddit.models.reddit.SubmissionModel
import com.rubenmayayo.reddit.ui.activities.ImageActivity
import com.rubenmayayo.reddit.ui.customviews.FormattingBar
import com.rubenmayayo.reddit.ui.customviews.menu.MenuOption
import com.rubenmayayo.reddit.ui.fragments.imagemodel.ImageModelFragment
import com.bumptech.glide.RequestManager

object MediaNativeBindings {
    @JvmStatic
    fun galleryModel(fragment: Any): ImageModel? = (fragment as ImageModelFragment).imageModel

    @JvmStatic
    fun fragmentActivity(fragment: Any): Activity? = (fragment as Fragment).getActivity()

    @JvmStatic
    fun fragmentTag(fragment: Any): String? = (fragment as Fragment).getTag()

    @JvmStatic
    fun fragmentVisible(fragment: Any): Boolean = (fragment as Fragment).getUserVisibleHint()

    @JvmStatic
    fun itemView(holder: Any): View = (holder as RecyclerViewViewHolder).itemView

    @JvmStatic
    fun commentBody(comment: Any): String? = (comment as CommentModel).body

    @JvmStatic
    fun commentHtml(comment: Any): String? = (comment as CommentModel).bodyHtml

    @JvmStatic
    fun setCommentHtml(comment: Any, html: String) {
        (comment as CommentModel).bodyHtml = html
    }

    @JvmStatic
    fun createSubmission(): SubmissionModel = SubmissionModel()

    @JvmStatic
    fun setSubmissionDomain(submission: SubmissionModel, host: String) {
        submission.domain = host
    }

    @JvmStatic
    fun setSubmissionMedia(submission: SubmissionModel, mediaType: Int, url: String) {
        submission.mediaType = mediaType
        submission.mediaUrl = url
    }

    @JvmStatic
    fun setSubmissionVideoUrl(submission: SubmissionModel, url: String) {
        submission.videoUrl = url
    }

    @JvmStatic
    fun openMedia(context: Context, submission: SubmissionModel, animated: Boolean) {
        if (animated) {
            MorpheBoostNativeStatics.Navigation_openAnimatedMedia(context, submission, false)
        } else {
            MorpheBoostNativeStatics.Navigation_openImage(context, submission, false)
        }
    }

    @JvmStatic
    fun imageIntent(context: Context, submission: SubmissionModel): Intent =
        MorpheBoostNativeStatics.Navigation_imageIntent(context, submission, true)

    @JvmStatic
    fun legacyImageIntent(context: Context, submission: SubmissionModel, commentMode: Boolean): Intent =
        Intent(context, ImageActivity::class.java)
            .putExtra("submission", submission as Parcelable)
            .putExtra("comment", commentMode)
            .putExtra("accent_color", MorpheBoostNativeStatics.ThemeUtils_accentColor(context))

    @JvmStatic
    fun loadImage(manager: Any?, context: Context, url: String, image: ImageView) {
        val requestManager = if (manager == null) {
            requireNotNull(MorpheBoostNativeStatics.Glide_with(context))
        } else {
            manager as RequestManager
        }
        requireNotNull(requestManager.load(url)).into(image)
    }

    @JvmStatic
    fun imageMenuOptionId(option: Any): Int = (option as MenuOption).id

    @JvmStatic
    fun attachCamera(formattingBar: Any) {
        (formattingBar as FormattingBar).attachmentCallback?.attachCamera()
    }

    @JvmStatic
    fun attachGallery(formattingBar: Any) {
        (formattingBar as FormattingBar).attachmentCallback?.attachGallery()
    }

    @JvmStatic
    fun insertRawUrl(formattingBar: Any, url: String) {
        val editor = requireNotNull((formattingBar as FormattingBar).editor) {
            "FormattingBar has no editor"
        }
        editor.text.replace(editor.selectionStart, editor.selectionEnd, url)
    }
}
