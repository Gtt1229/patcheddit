/*
 * Modifications Copyright 2026 brealorg.
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 * Adapted from https://github.com/brealorg/breal-morphe-patches (82d70f7).
 */

package app.morphe.extension.boost.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

public final class ReceiverCompat {
    private ReceiverCompat() {
    }

    public static Intent registerReceiver(
            Context context,
            BroadcastReceiver receiver,
            IntentFilter filter
    ) {
        if (Build.VERSION.SDK_INT >= 33) {
            return context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        }

        return context.registerReceiver(receiver, filter);
    }
}
