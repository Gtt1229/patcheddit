package app.morphe.extension.syncforreddit;

import org.json.JSONArray;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.morphe.extension.shared.Logger;

/**
 * Reddit answers /api/info with 403 "Blocked" for third party clients since Oct 2026, which breaks Sync's
 * gallery viewer. /comments/<id> still works and its first element is the same Listing /api/info returned.
 *
 * @noinspection unused
 */
public class FixGalleriesPatch {
    private static final Pattern INFO_URL =
            Pattern.compile("^(https://[a-z]+\\.reddit\\.com/)api/info(?:\\.json)?/?\\?id=t3_([a-z0-9]+)$");

    /** Replaces the /api/info URL built by Sync's gallery request. */
    public static String patchGalleryUrl(String url) {
        if (url == null) {
            return null;
        }
        Matcher m = INFO_URL.matcher(url);
        if (!m.matches()) {
            return url;
        }
        return m.group(1) + "comments/" + m.group(2) + ".json?limit=1";
    }

    /** Converts the /comments reply ([post listing, comment listing]) back into the post listing. */
    public static String patchGalleryResponse(String body) {
        if (body == null || !body.trim().startsWith("[")) {
            return body;
        }
        try {
            return new JSONArray(body).getJSONObject(0).toString();
        } catch (Exception e) {
            Logger.printException(() -> "Could not convert gallery response", e);
            return body;
        }
    }
}
