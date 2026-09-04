# Country Quartet — Google Play Release Checklist

Phase 10 review of the build, the artefact and the store requirements.
Last run: **4 September 2026**, against commit `eeded4f` plus the fixes in §5.

**Nothing has been published.** The bundle described below was built locally and
is signed with your real upload key. Uploading it is a manual step you take in
the Play Console.

## 1. Reviewed and ready

| Item | Value | Notes |
|---|---|---|
| Application ID | `com.countryquartet.game` | As specified. Debug builds use `.debug`, so both can sit on one device. |
| Application name | Country Quartet | `@string/app_name`, used by the launcher, splash and menu. |
| Developer name | Mindgrid Games | Also the `O=` field in the upload certificate. |
| Version name | 1.0 | First release. |
| Version code | 1 | Must increase on every upload. |
| Min SDK | 26 (Android 8.0) | As specified. |
| Target SDK | 37 | Current stable in this environment. |
| Compile SDK | 37 | |
| App icon | Adaptive, raster foreground | Globe ringed by four flags on brand navy, all five densities. |
| Themed icon | Dedicated monochrome vector | Drawn as strokes; the colour art would tint into a solid disc. |
| Permissions | **None requested** | Verified against the built APK, not the source. See §3. |
| Debug logging | None | No `Log`, `println` or `printStackTrace` anywhere in `src/main`. |
| Placeholder content | None | Re-checked after the 12 gameplay commits that followed the first review. |
| Offline | Fully offline | Content is a bundled asset; no network code exists. |
| Back navigation | Works | Every screen returns to the menu; game over offers Main Menu. |
| Release shrinking | R8 + resource shrinking on | Release APK **1.63 MB**. |
| Release bundle | `app/build/outputs/bundle/release/app-release.aab` (3.58 MB) | Built with `gradlew.bat clean test bundleRelease`. |
| Unit tests | **212 passing, 0 failures** | JVM only, no emulator needed. |

## 2. Signing — done

The build reads its upload key from `keystore.properties` in the project root,
which is git ignored and **not** in this repository. Without it the release
build is simply unsigned, so a fresh clone still builds.

A real upload key now exists and the bundle above is signed with it:

| | |
|---|---|
| Keystore | `C:\Users\georg\keys\country-quartet-upload.jks` (deliberately outside the repo) |
| Alias | `upload` |
| Algorithm | RSA 2048, valid 10,000 days (until January 2054) |
| Certificate | `CN=Country Quartet, O=Mindgrid Games` |
| SHA-256 | `62:9D:07:3A:70:33:9D:63:F6:38:A8:DB:D3:92:17:CD:69:B9:8A:51:04:44:80:92:85:C2:74:58:EA:44:8E:47` |

Verified with `jarsigner -verify`: *jar verified*, signed by the certificate
above.

> **Back the keystore up now, somewhere that is not this machine**, together
> with the password from `keystore.properties`. If the key is lost, the only
> recovery is a Play App Signing upload-key reset. Keep Play App Signing
> enabled when you create the app — without it, a lost key means the app can
> never be updated again.

## 3. Permissions

The app requests **no** permissions. Confirmed by dumping the permissions of
the built release APK, which lists only:

```text
com.countryquartet.game.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION
```

This is declared automatically by `androidx.core` with `protectionLevel="signature"`.
It is scoped to this app's own signature, is not a user-facing permission and
does not appear on the Play listing. **Leave it**: removing it with a manifest
override breaks `registerReceiver` on Android 13+.

There is no `INTERNET` permission.

## 4. Verified on a device

Run against an API 37 emulator (Android 17, 1080x2400, 420 dpi) with the
**signed, minified release build**, because R8 is the risk:

- Launches; menu, game, countries browser, how to play and settings all render.
- A game deals and plays: asking an opponent about a region, then picking the
  country, both work, and the success and failure messages appear in green and
  red respectively.
- Flags render from the bundled WebP images.
- No `FATAL`, `ClassNotFound` or `NoSuchMethod` behaviour observed.

Screenshots of each screen, captured from this build, are in
`docs/store/screenshots/`.

## 5. Fixed during this review

**The How to Play screen taught the wrong game.** Step 1 read "You get 13
cards" while the engine deals six (`GameEngine.DEFAULT_CARDS_PER_PLAYER`), and
step 4 never mentioned that a failed ask pays out a card from the deck. Both are
now correct, and the card count is formatted from the engine constant rather
than typed into the string, so it cannot drift again.

**The Sound switch was removed.** It stored a preference that nothing read,
because the game has no sounds. The setting, the repository method and the
DataStore key are all still there, so the row can come back unchanged on the day
there is something to mute.

## 6. Store assets — ready

| Asset | File |
|---|---|
| Listing copy, ratings and data-safety answers | `docs/store/LISTING.md` |
| Privacy policy text | `docs/store/PRIVACY.md` |
| App icon 512x512 | `docs/store/play-icon-512.png` |
| Feature graphic 1024x500 | `docs/store/play-feature-graphic-1024x500.png` |
| Phone screenshots (7, 1080x2400) | `docs/store/screenshots/` |

## 7. Before you upload

- [ ] Back up the keystore and its password off this machine (§2).
- [ ] Decide the **public contact email** for the listing; `LISTING.md` leaves
      it as a TODO rather than guessing at a personal address.
- [ ] **Host the privacy policy** and paste the URL into the Play Console. Play
      requires a live URL for every app. `PRIVACY.md` is ready to publish, and
      needs the contact address filled in.
- [ ] Settle the open content decisions in `docs/CONTENT-VERIFICATION.md` §8 —
      in particular the Israel capital entry, which is a deliberate choice.
- [ ] Re-check Indonesia's capital; the Constitutional Court ruling of May 2026
      keeps Jakarta, but a presidential decree could change it.
- [ ] Confirm the flag artwork licensing is acceptable to you (§8).
- [ ] Test on a physical device, ideally one running Android 8.0, which is the
      declared minimum and is still untested (§8).
- [ ] In the Play Console: create the app, keep Play App Signing enabled,
      complete the content rating questionnaire and data safety form using the
      answers in `LISTING.md`, then upload
      `app/build/outputs/bundle/release/app-release.aab` to internal testing
      before production.

## 8. Known gaps

- **The game no longer matches the specification in `CLAUDE.md`.** The spec in
  §3 and §8 describes 52 cards dealt 13 each with no draw pile. The engine now
  deals **six** cards each and keeps the remaining 28 as a draw pile that pays
  out a card on a failed ask (commit `ac8a08a`). The app, its rules screen and
  the listing copy all describe the game as actually implemented. If the change
  was deliberate, `CLAUDE.md` should be updated to match so the next review does
  not treat it as a defect.
- **Android 8.0 (API 26) is untested.** Everything was verified on API 37. The
  app uses no APIs above its minimum and the build enforces it, but an API 26
  device or emulator image should be run before release.
- **A long capital can break mid-word in the small card chips.** In the ask
  flow, "Copenhagen" wraps as "Copenhag / en". It is visible in
  `screenshots/07-ask-flow.png`. Cosmetic, not a blocker, but worth a fix.
- **Tablet layouts are untested.** Only phone screenshots exist. Either test on
  a tablet or leave tablet form factors off the listing.
- **Flag images are bundled and must be credited if required.** 52 flags ship as
  lossless WebP in `res/drawable-nodpi`. They come from flagcdn.com (Flagpedia),
  which publishes them for free use; the designs of national flags are
  themselves not subject to copyright. Confirm this is acceptable for your
  listing before release.
- **No crash reporting.** Deliberate: no analytics or third-party SDKs in v1, so
  crashes are only visible through Play Console vitals.
- **No automated instrumentation tests.** The 212 tests are JVM tests; the
  DataStore read/write path is covered only by manual device checks.
