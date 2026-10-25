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
import androidx.recyclerview.widget.RecyclerView.a0 as RecyclerViewViewHolder
import com.rubenmayayo.reddit.models.generic.ImageModel
import com.rubenmayayo.reddit.models.reddit.CommentModel
import com.rubenmayayo.reddit.models.reddit.SubmissionModel
import com.rubenmayayo.reddit.ui.activities.ImageActivity
import com.rubenmayayo.reddit.ui.customviews.FormattingBar
import com.rubenmayayo.reddit.ui.customviews.menu.MenuOption
import com.rubenmayayo.reddit.ui.fragments.imagemodel.ImageModelFragment
import ig2 as RequestManager

object MediaNativeBindings {
    @JvmStatic
    fun galleryModel(fragment: Any): ImageModel? = (fragment as ImageModelFragment).z

    @JvmStatic
    fun fragmentActivity(fragment: Any): Activity? = (fragment as Fragment).getActivity()

    @JvmStatic
    fun fragmentTag(fragment: Any): String? = (fragment as Fragment).getTag()

    @JvmStatic
    fun fragmentVisible(fragment: Any): Boolean = (fragment as Fragment).getUserVisibleHint()

    @JvmStatic
    fun itemView(holder: Any): View = (holder as RecyclerViewViewHolder).a

    @JvmStatic
    fun commentBody(comment: Any): String? = (comment as CommentModel).b0

    @JvmStatic
    fun commentHtml(comment: Any): String? = (comment as CommentModel).e0

    @JvmStatic
    fun setCommentHtml(comment: Any, html: String) {
        (comment as CommentModel).e0 = html
    }

    @JvmStatic
    fun createSubmission(): SubmissionModel = SubmissionModel()

    @JvmStatic
    fun setSubmissionDomain(submission: SubmissionModel, host: String) {
        submission.R = host
    }

    @JvmStatic
    fun setSubmissionMedia(submission: SubmissionModel, mediaType: Int, url: String) {
        submission.G0 = mediaType
        submission.Z = url
    }

    @JvmStatic
    fun setSubmissionVideoUrl(submission: SubmissionModel, url: String) {
        submission.P0 = url
    }

    @JvmStatic
    fun openMedia(context: Context, submission: SubmissionModel, animated: Boolean) {
        if (animated) {
            MorpheBoostNativeStatics.xy1_r(context, submission, false)
        } else {
            MorpheBoostNativeStatics.xy1_s(context, submission, false)
        }
    }

    @JvmStatic
    fun imageIntent(context: Context, submission: SubmissionModel): Intent =
        MorpheBoostNativeStatics.xy1_e(context, submission, true)

    @JvmStatic
    fun legacyImageIntent(context: Context, submission: SubmissionModel, commentMode: Boolean): Intent =
        Intent(context, ImageActivity::class.java)
            .putExtra("submission", submission as Parcelable)
            .putExtra("comment", commentMode)
            .putExtra("accent_color", MorpheBoostNativeStatics.o83_c(context))

    @JvmStatic
    fun loadImage(manager: Any?, context: Context, url: String, image: ImageView) {
        val requestManager = if (manager == null) {
            requireNotNull(MorpheBoostNativeStatics.a_d(context))
        } else {
            manager as RequestManager
        }
        requireNotNull(requestManager.p(url)).P(image)
    }

    @JvmStatic
    fun imageMenuOptionId(option: Any): Int = (option as MenuOption).a

    @JvmStatic
    fun attachCamera(formattingBar: Any) {
        (formattingBar as FormattingBar).z?.a()
    }

    @JvmStatic
    fun attachGallery(formattingBar: Any) {
        (formattingBar as FormattingBar).z?.b()
    }

    @JvmStatic
    fun insertRawUrl(formattingBar: Any, url: String) {
        val editor = requireNotNull((formattingBar as FormattingBar).b) {
            "FormattingBar has no editor"
        }
        editor.text.replace(editor.selectionStart, editor.selectionEnd, url)
    }
}
