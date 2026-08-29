# Country Quartet — Google Play Release Checklist

Phase 10 review of the build, the artifact and the store requirements.

**Nothing has been published.** The bundle below was produced locally and signed
with a throwaway key for verification only. See §2 before uploading anything.

## 1. Reviewed and ready

| Item | Value | Notes |
|---|---|---|
| Application ID | `com.countryquartet.game` | As specified. Debug builds use `.debug`, so both can sit on one device. |
| Application name | Country Quartet | `@string/app_name`, used by the launcher, splash and menu. |
| Version name | 1.0 | First release. |
| Version code | 1 | Must increase on every upload. |
| Min SDK | 26 (Android 8.0) | As specified. |
| Target SDK | 37 | Current stable in this environment. |
| Compile SDK | 37 | |
| App icon | Adaptive, vector | Globe on brand blue, foreground inside the 66dp safe zone. |
| Themed icon | Dedicated monochrome layer | Drawn as strokes; reusing the colour art would tint into a solid disc. |
| Permissions | None requested | No `INTERNET`. See §3. |
| Debug logging | None | No `Log`, `println` or `printStackTrace` anywhere in `src/main`. |
| Placeholder content | None left | The last one — How to Play — was found by this review, see §5. |
| Offline | Fully offline | Content is a bundled asset; no network code exists. |
| Back navigation | Works | Every screen returns to the menu; game over offers Main Menu. |
| Release shrinking | R8 + resource shrinking on | APK 12 MB → **1.27 MB**. |
| Release bundle | `app-release.aab` (3.07 MB) | Built from `gradlew.bat bundleRelease`. |

## 2. Signing — action required before you upload

The build reads its upload key from `keystore.properties` in the project root,
which is **git ignored and not in this repository**. Without it the release build
is simply unsigned, so a fresh clone still builds.

The key used during this review is a **throwaway** created only to prove the
minified build runs. **Do not release with it.** Create your own upload key:

```bat
keytool -genkeypair -v -keystore country-quartet-upload.jks ^
  -alias upload -keyalg RSA -keysize 2048 -validity 10000
```

Then create `keystore.properties` (never commit it):

```properties
storeFile=/absolute/path/to/country-quartet-upload.jks
storePassword=...
keyAlias=upload
keyPassword=...
```

Back the keystore up somewhere durable. If it is lost, the only recovery is a
Play App Signing upload-key reset, and without Play App Signing there is no
recovery at all — the app can never be updated.

## 3. Permissions

The app requests **no** permissions. It does not request `INTERNET`, as required.

The merged manifest contains one entry:

```text
com.countryquartet.game.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION
```

This is declared automatically by `androidx.core` with `protectionLevel="signature"`.
It is scoped to this app's own signature, is not a user-facing permission and does
not appear on the Play listing. **Leave it**: removing it with a manifest override
breaks `registerReceiver` on Android 13+.

## 4. Verified on a device

Run against the API 37 emulator with the **signed, minified release build**, not
the debug build, because R8 is the risk:

- Launches; menu, game, countries browser and settings all render.
- A game deals and plays; the AI takes its turns; flags render.
- DataStore survives a force-stop: the animation setting stayed off across a
  process restart in the release build.
- No `FATAL`, `ClassNotFound` or `NoSuchMethod` in logcat.

**Screen sizes and fonts.** Tested at 360x640dp (about the smallest common phone)
with the system font scale at 1.3x. The menu, the game board and the cards remain
readable; long names such as "United States" and "Washington, D.C." wrap rather
than truncate. The board is cramped at that size — one card group visible at a
time — but usable.

## 5. Fixed during this review

**How to Play was still a placeholder.** The screen shipped the Phase 1 text
"The illustrated rules arrive in Phase 5." — a developer note, referring to an
internal phase number, in a menu item aimed at a seven-year-old. It was a release
blocker and is now a real screen: an intro line, a worked example showing the
Nordic quartet with flags, and the five steps from the specification.

The now-unused `PlaceholderContent` component was deleted, and a stale comment
was removed from `strings.xml`. No placeholder text remains in the app.

## 6. Before you upload

- [ ] Create a real upload key and `keystore.properties` (§2), and back it up.
- [ ] Rebuild: `gradlew.bat clean bundleRelease`, and confirm the AAB is signed
      with the upload key, not the throwaway.
- [ ] Settle the open content decisions in `docs/CONTENT-VERIFICATION.md` §8 —
      in particular the Israel capital entry, which is a deliberate choice.
- [ ] Re-check Indonesia's capital; the Constitutional Court ruling of May 2026
      keeps Jakarta, but a presidential decree could change it.
- [ ] Decide on the **Sound** setting: the switch is stored and honoured by
      nothing, because the game has no sounds. Either add sounds or hide the
      switch; shipping a control that does nothing is a support complaint.
- [ ] Store listing: title, short description ("Collect countries, complete
      quartets, and learn geography!"), full description, feature graphic and
      screenshots.
- [ ] Content rating questionnaire — the game has no ads, no purchases, no
      network and no user content.
- [ ] Data safety form: declare that no data is collected or shared. Statistics
      and settings never leave the device.
- [ ] Target audience: declare the children's age bands, which brings the
      Families policy requirements.
- [ ] Test on a physical device, ideally one running Android 8.0 to exercise
      minSdk, which has not been tested — see §7.

## 7. Known gaps

- **Android 8.0 (API 26) is untested.** Everything was verified on API 37. The
  app uses no APIs above its minimum and the build enforces it, but an API 26
  device or emulator image should be run before release.
- **Flags are emoji, not images.** They render from the country's ISO code, so
  they depend on the system emoji font. Verified on the emulator; a device with
  a non-Google font could show letter pairs instead. See
  `docs/CONTENT-VERIFICATION.md` §5.
- **The Sound switch does nothing.** There are no sounds in the app.
- **No crash reporting.** Deliberate: no analytics or third-party SDKs in v1, so
  crashes are only visible through Play Console vitals.
- **No automated instrumentation tests.** The 145 tests are JVM tests; the
  DataStore read/write path is covered only by the manual device check in §4.
