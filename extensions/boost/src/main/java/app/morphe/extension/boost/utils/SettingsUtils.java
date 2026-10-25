/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */

package app.morphe.extension.boost.utils;

import java.util.List;

import com.rubenmayayo.reddit.ui.customviews.menu.MenuOption;

import app.morphe.extension.shared.ResourceUtils;

/**
 * @noinspection unused
 */
public class SettingsUtils {
    public static void addMenuOptions(List<MenuOption> options) {
        MenuOption divider = new MenuOption();
        divider.H = true;
        options.add(divider);

        int drawableId = ResourceUtils.getDrawableIdentifier("ic_library_24dp");
        MenuOption wayback = new MenuOption();
        wayback.a = 101;
        wayback.z = "Open with Wayback Machine";
        wayback.b = drawableId;
        options.add(wayback);

        MenuOption archive = new MenuOption();
        archive.a = 102;
        archive.z = "Open with Archive.is";
        archive.b = drawableId;
        options.add(archive);
    }
}
