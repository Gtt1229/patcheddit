# 👋🧩 Patcheddit

Morphe compatible patches for third party Reddit apps.

## ❓ About

Patcheddit is a Morphe compatible patch bundle that supports patching the OAuth login information for 3rd party reddit apps.

In addition, patches for some apps contain additional features that were not present in ReVanced.

## Differences from official patch set

### All clients
* Client ID, redirect URI and user agent are all patchable.
    * By default, `Spoof client` uses RedReader 1.27's client ID, redirect URI and user agent. Leave the OAuth client ID option empty to use its default.
    * Custom patch options override these defaults. In Boost 1.14.0 and up, saved values in the native API settings override the patch-provided defaults.

### Boost for Reddit
* Morphe options use Boost's native preference UI. Boost's original settings pages, theme and navigation are kept; no replacement settings menu is installed.
* **Import and export OAuth tokens** is an opt-in patch that adds **Settings > Morphe > OAuth tokens**. It supports local JSON files and copy/paste, including signing in or adding an account without the browser login flow.
    * Import checks the access-token owner with Reddit, validates the refresh token using Boost's configured OAuth client, and obtains fresh tokens before adding or updating the verified account. An expired access token can be restored with a valid refresh token. Boost then restarts with that account selected.
    * Tokens are bound to their OAuth client. Configure the same client ID in Boost's native API settings before importing; the patch does not change API settings automatically.
    * Exports are **unencrypted account credentials**. Keep them private and delete files and clipboard copies after use. Clipboard exports are marked sensitive; Boost attempts to clear its own copy after one minute and retries when the token screen resumes if Android blocked background clipboard access.
    * The JSON requires string fields `access_token` and `refresh_token`. Exports also include `version: 1`, `username` and `client_id`; these metadata fields are optional when importing. Duplicate fields, unsupported fields and files larger than 64 KiB of text are rejected. Tokens are stored in Boost's native account database, not Morphe preferences.
* Image-upload features are separate opt-in patches:
    * **Use Reddit for image uploads** selects Reddit hosting for image posts and galleries, without changing editor attachments.
    * **Configure editor image hosting** adds an editor-hosting setting for Imgur Free or ImgBB. It does not change post or gallery hosting.
    * **Preview images in the editor** adds previews of supported image URLs without changing upload providers.
* The content of deleted Reddit posts and comments can be loaded from [Project Arctic Shift](https://github.com/ArthurHeitmann/arctic_shift).
    * This only works for text and images, and not all content can be restored. Videos are unlikely to be available.
    * Posts/comments that were deleted are marked as follows:
        * 🗑️ if removed by the author
        * 🧹 if removed by a subreddit mod
        * 🚓 if removed by reddit admins
        * 🤖 if removed by anti-spam
        * ©️ if removed due to a takedown notice
        * 👿 if removed by Anti-Evil Operations
        * 🚫 if the subreddit is banned
    * Banned subreddits can be browsed with this functionality; however, rules/wiki pages are unavailable and the only sort option is by new.
    * In order to view a deleted post, you need to already have a direct link to the post. Subreddit feeds will not show deleted posts (unless the subreddit was banned); this is because of the lack of sorting options and the high likelihood of seeing a ton of spam posts.
    * Currently, this functionality is not working on user profiles. Removed posts you encounter on user pages need to be opened and refreshed for their content to load.
    * This feature makes extra network requests, so posts will load a little slower. In particular, retrieving images from the Wayback Machine is heavily ratelimited.
        * Caching is implemented to try and limit the impact of this.
* Imgur images and albums are automatically loaded from the Wayback Machine if a 404 is detected.
* The context menu for posts now contains additional options for opening a site in the Wayback Machine or archive.is. This is useful for if the undeleting functionality has issues or you want to bypass a content paywall.

### reddit is fun
* Imgur album URLs are rewritten to use the newer format.
* Enables pro features without ads.

## 🚀 Get started

1. Install [Morphe Manager](https://morphe.software/) and switch it to advanced/expert mode.
2. Click [this link](https://morphe.software/add-source?github=wchill/patcheddit) to add this patch source to Morphe Manager.
3. In Morphe Manager, find the app you want to patch.
   * The supported apps are Boost, RIF, Sync, Relay, BaconReader, Joey, and Infinity+.
4. Get the APK for your preferred, supported 3rd party reddit app. If you don't have it, you can download it from in the app. APKM bundles (aka split APKs) will also work.
   * You should get the last available version for your reddit app, unless you are patching Relay. Relay v10.2.40 should be used instead of the latest.
5. Select desired patches, then patch.
   * `Spoof client` includes RedReader 1.27 defaults, so you do not need to supply a client ID. See `What if I don't have a client ID?` below.
   * If you use a custom OAuth client ID, make sure its redirect URI matches the one in `Spoof client` options and on https://www.reddit.com/prefs/apps/.
6. Once patching is complete, install the app and set it up as usual.

For assistance with patching, please post a thread in [/r/Patcheddit](https://www.reddit.com/r/Patcheddit/) or [/r/MorpheApp](https://www.reddit.com/r/MorpheApp/). Only use the GitHub issue tracker for legitimate bug reports or feature requests.

### What if I don't have a client ID?

Use the [RedReader 1.27](https://github.com/QuantumBadger/RedReader/releases/tag/v1.27) defaults included in `Spoof client`:

* Leave `OAuth client ID` empty.
* Keep `Redirect URI` set to `redreader://rr_oauth_redir`.
* Keep `User agent` set to `org.quantumbadger.redreader/1.27`.

For another client's credentials, enter its client ID and matching redirect URI and user agent instead.

### Resolving login problems

I am unable to select the username/password fields on the login page - my keyboard pops up and then disappears quickly

* Switch your phone to landscape mode. There is a cookie banner that needs to be dismissed, but it is bugged and only shows up on landscape mode.

I either get `{}` or `Error: Invalid request to Oauth API`

* Your redirect URI is probably wrong. It has to match exactly between the patching settings and your reddit installed app. If you're reusing an installed app you made before, then update the URI at https://www.reddit.com/prefs/apps/. Make sure you don't add `/` at the end.

I cannot login, reddit is telling me that my username/password are invalid even though they are correct

* Reddit doesn't like the network you're logging in from (corporate/work network, country with banned IP ranges due to abuse, etc). Try using a VPN or switching to cellular data.
* Alternatively, this problem may go away if you restart your phone, clear cookies for Chrome, clear app data for Android System WebView, or update Android System WebView to the latest version.
* See https://github.com/cygnusx-1-org/continuum/blob/master/SETUP.md#common-errors

I cannot hit Accept on the Authorize screen

* Change your reddit site language to English. There are reports that this screen does not work properly otherwise.

I still get 403 Blocked

* If you filled your user agent in with garbage, it will eventually be blocked by reddit. Also, reddit blocks any mention of rubenmayayo in the user agent.
* Your user agent can also be blocked due to other terms: a user reported that he was getting 403 after patching, and it turned out to be because his username included `isfun` (which triggers a block from reddit's side due to them blocking the app Reddit is Fun).

I get 401 when I open the app

* You probably created a web app instead of an installed app. Delete the app, create an installed app and then repatch with the new client ID.
* Don't use autofill when logging into Reddit. Manually type in or copy+paste your password.

I'm getting 400 Bad Request while logged in

* Either try logging out and logging back in, or uninstall the app (back up your settings first) then reinstall it.

I get a `null: null` error when I open the app

* Your client ID is incorrect, check that you copied it correctly.


## 🧑‍💻 Development

To develop and release Patcheddit, some things need to be considered:

- Development starts in feature branches. Once a feature branch is ready, it is squashed and merged into the `dev` branch
- The `dev` branch is merged into the `main` branch once it is ready for release
- Semantic versioning is used to version Patcheddit. Patcheddit has a public API for other patches to use
- Semantic commit messages are used for commits
- Commits on the `dev` branch and `main` branch are automatically released
via the [release.yml](.github/workflows/release.yml) workflow, which is also responsible for generating the changelog
and updating the version of Patcheddit. It is triggered by pushing to the `dev` or `main` branch.
The workflow uses the `publish` task to publish the release of Patcheddit
- The `buildAndroid` task is used to build Patcheddit so that it can be used on Android.
The `publish` task depends on the `buildAndroid` task, so it will be run automatically when publishing a release.

### Updating Boost bindings

Boost's compile-only Kotlin API is in `extensions/boost/stub/src/main/kotlin`. Feature code uses stable class and member names; patches use the matching typed `BoostApi` catalog rather than loading Android stub classes on the patcher JVM:

```kotlin
val fingerprint = BoostApi.DrawerActivity.setupBottomNavigation.fingerprint()
```

`patches/src/main/resources/boost/bindings.json` contains the version-specific native targets. Its keys are stable adapter descriptors; its values are complete native class, method and field descriptors, including declaring owners. For an obfuscation-only update, change the target values and declared version, update `AppCompatibility`, and refresh the partial library `boost/mapping.txt` when needed. Do not rename the adapter API or feature code to match obfuscated symbols.

`staticMethods` and `staticFields` describe the adapter API's dispatch. `injectedMethods` identifies bridges cloned from native factories. All targets, prototypes, dispatch kinds and instance receivers are checked against the input APK. Missing or conflicting bindings stop patching. Native signature or behavior changes can still require adapter or patch changes.

Only the extension payload is rebound; the app is not deobfuscated, and placeholder stubs are never packaged. Native callbacks are renamed through the same bindings. Reflective field access uses `BoostFieldNames`, whose non-constant values are resolved during binding, so callers do not embed obfuscated names or precompute hashes for renamed strings.

## 📚 Everything else

### 📙 Contributing

Thank you for considering contributing to Patcheddit.  
You can find the contribution guidelines [here](CONTRIBUTING.md).

### 🛠️ Building

To build Patcheddit,
you can follow the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation).

## 📜 License

Patcheddit is licensed under the [GNU General Public License v3.0](LICENSE), with additional conditions under Section 7:

- **Attribution (7b):** Any use of this code, including derivatives, must display a visible notice:

  > This app uses code from Patcheddit. To learn more, visit https://reddit.com/r/patcheddit

- **Name Restriction (7c):** The name **"Patcheddit"** may not be used for derivative works.  
  Derivatives must adopt a distinct identity unrelated to "Patcheddit".

The Morphe project (https://github.com/MorpheApp) may use code from this project without displaying the attribution notice.

The Boost for Reddit project (https://github.com/rubenmayayo/boost-for-reddit) is authorized to use any Patcheddit-originated source code under Apache 2.0 license terms.

See the [LICENSE](LICENSE) file for the full GPLv3 terms and the [NOTICE](NOTICE) file for full conditions of GPLv3 Section 7
