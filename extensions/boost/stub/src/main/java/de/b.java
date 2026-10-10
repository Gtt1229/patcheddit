/*
 * Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package de;

import com.rubenmayayo.reddit.models.imgur.Upload;

import java.util.List;

public interface b {
    void a(String title, List<Upload> uploads, f.b progress, e listener);

    void b(Upload upload, f.b progress, e listener);

    void c(Upload upload, f.b progress, e listener);
}
