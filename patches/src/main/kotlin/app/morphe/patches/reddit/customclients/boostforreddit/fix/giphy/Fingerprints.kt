/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Modifications Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 */

package app.morphe.patches.reddit.customclients.boostforreddit.fix.giphy

import app.morphe.patches.reddit.customclients.boostforreddit.bindings.BoostApi

internal val commentViewHolderBindFingerprint = BoostApi.CommentViewHolder.bind.fingerprint()

internal val commentViewHolderCollapseFingerprint = BoostApi.CommentViewHolder.isCollapsed.fingerprint()
