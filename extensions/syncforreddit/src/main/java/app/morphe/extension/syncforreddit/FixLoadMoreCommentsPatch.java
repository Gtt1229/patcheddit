package app.morphe.extension.syncforreddit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.morphe.extension.shared.Logger;

/**
 * Reddit answers /api/morechildren with 403 "Blocked" for third party clients since Oct 2026, so "load more
 * comments" fails in Sync. The request is turned into a GET of the parent's subtree from /comments, and the
 * reply is converted back into the flat morechildren shape: {"json":{"data":{"things":[...]}}}.
 *
 * @noinspection unused
 */
public class FixLoadMoreCommentsPatch {
    private static final String MARK = "morphe_more=";
    private static final String MORECHILDREN = "api/morechildren";

    // Set on the UI thread right before Sync builds the request, read back by its constructor.
    private static final ThreadLocal<String> PARENT = new ThreadLocal<>();
    private static final ThreadLocal<String[]> MORE = new ThreadLocal<>();

    // The id list can run to hundreds of ids (Reddit answers 414), so the URL only carries a key to it.
    private static final Map<String, String[]> REQUESTS = new ConcurrentHashMap<>();
    private static final AtomicInteger NEXT_KEY = new AtomicInteger();

    /** Called with the parent_id (t1_ or t3_) of the "load more" item that was tapped. */
    public static void patchSetParent(String parentId) {
        PARENT.set(parentId);
    }

    /** Called at the start of the morechildren request constructor. */
    public static void patchSetRequest(String linkId, String children, String sort) {
        MORE.set(new String[]{linkId, children, sort});
    }

    /** Replaces the morechildren URL. Returns it unchanged if the request can't be rewritten. */
    public static String patchMoreUrl(String url) {
        String parent = PARENT.get();
        String[] more = MORE.get();
        PARENT.remove();
        MORE.remove();

        int index = url == null ? -1 : url.indexOf(MORECHILDREN);
        if (index < 0 || more == null || more[0] == null) {
            return url;
        }

        String linkId = more[0].startsWith("t3_") ? more[0].substring(3) : more[0];
        boolean topLevel = parent == null || !parent.startsWith("t1_");

        StringBuilder sb = new StringBuilder(url.substring(0, index)).append("comments/").append(linkId);
        if (!topLevel) {
            sb.append("/_/").append(parent.substring(3));
        }
        // Reddit caps a fetch at 500 comments. At the top level, spend them on new top-level comments: deeper
        // replies come back as their own "load more" (the nested case above, which loads fully).
        sb.append(".json?always_show_media=1&profile_img=true&limit=500&depth=").append(topLevel ? 2 : 10);
        String sort = more[2];
        if (sort != null && !sort.isEmpty()) {
            sb.append("&sort=").append(sort.toLowerCase(Locale.ENGLISH));
        }

        if (REQUESTS.size() > 50) {
            REQUESTS.clear();
        }
        String key = Integer.toString(NEXT_KEY.incrementAndGet());
        REQUESTS.put(key, new String[]{topLevel ? "t3_" + linkId : parent, more[1] == null ? "" : more[1]});
        sb.append('&').append(MARK).append(key);
        return sb.toString();
    }

    /** Rewritten requests are listing fetches, so they go out as GET (Volley method 0). */
    public static int patchMoreMethod(String url, int method) {
        return param(url) == null ? method : 0;
    }

    /** Converts the /comments reply of a rewritten request into the morechildren shape Sync parses. */
    public static String patchMoreResponse(String body, String url) {
        String key = param(url);
        if (key == null) {
            return body;
        }
        String[] request = REQUESTS.get(key);
        if (request == null) {
            return body;
        }
        try {
            return convert(body, request[0], request[1]);
        } catch (JSONException e) {
            Logger.printException(() -> "Could not convert load more comments response", e);
            return body;
        }
    }

    private static String convert(String body, String parent, String children) throws JSONException {
        Set<String> wanted = new HashSet<>(Arrays.asList(children.split(",")));
        Set<String> shown = new HashSet<>();
        shown.add(parent);
        JSONArray things = new JSONArray();
        JSONArray top = new JSONArray(body).getJSONObject(1).getJSONObject("data").getJSONArray("children");
        if (walk(top, parent, wanted, shown, things) == 0) {
            // Nothing new within Reddit's 500-comment window (bottom of a huge thread). Without "data" Sync
            // reports an error instead of replacing the "load more" item with nothing.
            Logger.printInfo(() -> "No comments found for load more under " + parent);
            return "{\"json\":{\"errors\":[]}}";
        }
        return new JSONObject().put("json", new JSONObject()
                .put("errors", new JSONArray())
                .put("data", new JSONObject().put("things", things))).toString();
    }

    /** Pre-order walk, like morechildren: a parent always comes before its replies. Returns comments added. */
    private static int walk(JSONArray children, String parent, Set<String> wanted, Set<String> shown,
                            JSONArray things) throws JSONException {
        int added = 0;
        for (int i = 0; i < children.length(); i++) {
            JSONObject child = children.optJSONObject(i);
            if (child == null) {
                continue;
            }
            JSONObject data = child.optJSONObject("data");
            if (data == null) {
                continue;
            }
            String parentId = data.optString("parent_id");
            if ("t1".equals(child.optString("kind"))) {
                JSONObject replies = data.optJSONObject("replies");
                // Like morechildren: the asked-for comments plus everything under them.
                boolean underAdded = shown.contains(parentId) && !parentId.equals(parent);
                if (underAdded || (wanted.contains(data.optString("id")) && shown.contains(parentId))) {
                    // Sync's parser would otherwise also add the nested replies a second time.
                    data.put("replies", "");
                    things.put(child);
                    shown.add(data.optString("name"));
                    added++;
                }
                if (replies != null && replies.optJSONObject("data") != null) {
                    JSONArray sub = replies.optJSONObject("data").optJSONArray("children");
                    if (sub != null) {
                        added += walk(sub, parent, wanted, shown, things);
                    }
                }
            } else if ("more".equals(child.optString("kind")) && shown.contains(parentId)) {
                // Deeper "load more" under a comment we added: keep it. One directly under the parent is
                // only useful if it still holds comments that were asked for.
                if (!parentId.equals(parent) || overlaps(data.optJSONArray("children"), wanted)) {
                    things.put(child);
                }
            }
        }
        return added;
    }

    private static boolean overlaps(JSONArray ids, Set<String> wanted) {
        if (ids == null) {
            return false;
        }
        for (int i = 0; i < ids.length(); i++) {
            if (wanted.contains(ids.optString(i))) {
                return true;
            }
        }
        return false;
    }

    private static String param(String url) {
        if (url == null) {
            return null;
        }
        Matcher m = Pattern.compile("[?&]" + MARK + "([0-9]+)").matcher(url);
        return m.find() ? m.group(1) : null;
    }
}
