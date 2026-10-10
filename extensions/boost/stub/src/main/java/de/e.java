/*
 * Copyright 2026 brealorg.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package de;

import com.rubenmayayo.reddit.models.imgur.ImageResponse;

import java.util.List;

public interface e {
    void a(List<?> items);

    void b(ImageResponse.UploadedImage uploadedImage);

    void c(Exception exception, String message);

    void onSuccess(String url);
}
