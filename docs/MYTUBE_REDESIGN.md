# MyTube redesign: status and handoff

MyTube is this fork of SmartTube, being redesigned to look and behave like the official
YouTube app for TVs, without YouTube's name or logo. Target device: **Fire TV Stick 4K (AFTMM)**:
Fire OS 6 / Android 7.1 (API 25), 32-bit ARM, about 1.5–1.8 GB RAM (about 430 MB free in use),
1080p UI at 320 dpi.

Branch: `mytube-redesign`, built on top of `4e4ef9c6`. Each step is its own commit (see the history).

---

## 1. Build and test setup

| Item | Value |
|---|---|
| JDK | 17 (AGP 7.4.2, Gradle 7.5) |
| SDK | compileSdk 34, build-tools 30.0.3 |
| Submodules | `git submodule update --init` (MediaServiceCore, SharedModules; unchanged) |
| Build | `./gradlew :smarttubetv:assembleStstableDebug` |
| APKs | `smarttubetv/build/outputs/apk/ststable/debug/MyTube_stable_32.74_<abi>.apk` (use `armeabi-v7a` for the Fire TV and `x86` for the emulator; `universal` has no x86 libs) |
| Package | `app.mytube.stable` (also `app.mytube.beta` and `app.mytube.fdroid`) |
| Emulator | AVD `mytube_tv`: `system-images;android-25;android-tv;x86`, device `tv_1080p`, `hw.ramSize=1536`. Run headless: `emulator -avd mytube_tv -no-window -no-audio -gpu swiftshader_indirect -port 5580` |
| Firebase | Off. It is only applied with `-PenableFirebase=true` plus your own `google-services.json`, because the upstream json has no client for `app.mytube.*` |

**Device testing rules (learned the hard way):**
- Always pass `-s <serial>` to adb. A stray `emulator-5554` sometimes shows up.
- Do **not** `pm clear` once signed in, because that loses the account. Use `install -r`.
- The owner may be using the TV with the physical remote at the same time. Injected keys then mix with
  real ones, which twice switched the account to "None". Ask before sending keys to the TV;
  prefer the emulator.
- `uiautomator dump` fails while video is playing. Pause first (keyevent 127).
- Emulator sign-in: MyTube's profile entry → code → the owner enters it at yt.be/activate on a phone.

---

## 2. Done (all committed)

### Branding: `23f95cfa`
- App name "MyTube", new teal "M" icon, banner and logos (the red play-button alt icon was replaced).
- New applicationIds `app.mytube.*`; search content-provider authorities renamed to match, so MyTube
  can be installed alongside SmartTube.
- Upstream self-updater disabled (it would install upstream SmartTube). Firebase/Crashlytics gated off.
- MyTube ids added to `Utils.KNOWN_PACKAGES` (backup/restore).

### Theme and cards: `76843b6e`, `95c3e23a`, `468b371c`
- "MyTube" color scheme (default, index 0): #0F0F0F background, teal accent `#1EC8A5`.
- Cards **measured against the official app** by focus-ring size as a percentage of screen width:
  regular card 27.5% (528 px at 1080p) → `card_width/height` 264×148 dp (grids get 3 columns).
  Content left edge 8.2% → rail 72 dp + 8 dp. On the emulator the result is 27.8% and 8.6%.
- Text: title 20sp (2 lines max, no reserved blank line), metadata 16sp, shelf titles 17sp white.
- Metadata split as "Channel" on line 1 and "views • age" on line 2 (`VideoCardPresenter.splitChannelLine`).
- Focus: a white 3dp rounded ring, **no zoom** (`ViewUtil.FOCUS_ZOOM_FACTOR = NONE`); unfocused titles dimmed.
- Dynamic shadows disabled on the grid and row presenters.

### Sidebar: `b1070d5c`, `6914a247`, `4ec69eab`, `e76e49c9`
- Forked Leanback **rail mode** (`BrowseSupportFragment.setRailWidth/setRailTopPadding`,
  `HeadersSupportFragment.setRailMode`). Collapsed, the sidebar is a 72 dp icon rail; expanded, it overlays the content.
  The content never relays out. The list is pinned to the top (edge alignment), and the current section icon is highlighted.
- Order, matching the TV app: **Profile** (avatar + name or "Sign in") · Search · Home · Shorts | Subscriptions ·
  Library (= Playlists) | Music · Live · Gaming · News · Sports | Settings. Dividers between groups,
  a full-width white pill on focus. All items fit without scrolling.
  Other sections (History, Kids, Channels, ...) can still be enabled in Settings.
- One-time migration for existing installs (`SidebarService.applyMyTubeDefaultsOnce`, prefs `mytube_prefs`).
- Profile entry opens the account picker (now with **Add account**). With no account, it goes straight to sign-in.
  The title-bar account orb was removed: it switched to the next account on a single press.
- Rounded "Search" box in the top bar; it hides under the expanded sidebar.

### Home: `b1070d5c`, `468b371c`
- `BrowseSection.TYPE_CHIPS_ROWS` → `HomeRowsFragment`: horizontal shelves (the official Home has no chips;
  the chips row still exists behind `HomeRowsFragment.SHOW_CHIPS`).
- Paging stays per shelf on scroll (`isGridSection()` treats it like rows, which avoids request bursts).

### Subscriptions: `35853891`, `bc7e99b2`
- Two-column page (`TYPE_MULTI_GRID`): channel list on the left (newest content first) with **All** at the top,
  and the videos on the right ("All" shows the combined feed; a channel shows its uploads, loaded as focus moves).
- Right-column loads no longer cancel the channel-list load (`mRightColumnAction`).
- Channel rows: round avatars, a white pill on focus, a teal dot for new videos.

### Player: `0d818d28`, `5ab5d765`
- One row under the seek bar with **three groups**: left Channel · Description · Subscribe | centre
  Previous · Play/Pause · Next | right Like · Dislike · Comments/Chat · Save · [other enabled] · **gear**.
  A new third control bar is in `tweaks/PlaybackTransportRowPresenter` (`tertiary_controls_dock`).
- Gear → Quality / Captions / Playback speed (these reuse the existing dialogs; `PlayerUIController.showPlayerSettingsMenu`).
- Default player buttons are the TV-app set, with a one-time migration (`PlayerTweaksData`).
- First related row labelled "Up next"; the time and quality line sits above the seek bar.
- Verified on the emulator: the D-pad walks all 11 buttons, the gear menu works, and the seek thumbnail preview works.

### Performance: `bdb7d42b` (not yet a measured win, see pending)
- Thumbnails: `ViewUtil.thumbnailOptions()`, with memory cache on, RESOURCE disk cache, RGB_565 and no fade
  (the old `glideOptions()` skipped both caches for every card and storyboard).
- Glide disk cache 32 MB. On devices with < 2.5 GB RAM, memory cache and bitmap pool are 1.5 screens each.
- ExoPlayer on < 2.5 GB RAM: target buffer capped at 48 MB (was RAM/18 ≈ 100 MB) and back buffer at 15 s (was 50 s).
- Codecs unchanged: AFTMM defaults to 4K VP9 60 (hardware); AV1 is only used if picked manually.

---

## 3. Pending

### Matching the official app (priority order)
1. **Player title placement:** official has the title and "channel • views • age" at the **top-left of the screen**,
   with the controls at the bottom. MyTube still draws the title just above the time line (`controls_card` / description dock
   in `lb_playback_transport_controls_row.xml`).
2. **Player button styling:** official uses text pills for "Description" and "Subscribe" next to the channel avatar,
   round buttons with a subtle background, and a big white round Play/Pause. MyTube uses icons.
3. **Search screen:** official has a wide search bar at the top; the **left** column holds suggestions (white pill on focus);
   the **right** has an on-screen keyboard (A–Z in a 7-column grid, backspace, "&123", language key, SPACE / CLEAR / SEARCH buttons);
   below it is a "From recommended searches" shelf. MyTube still uses the SmartTube search UI.
4. **"Who's watching?" profile screen:** full-screen, with large round avatars and names, the current one ringed
   with its handle and a gear underneath, "Add account" and "Add a kid account", and a "Watch as guest" pill.
   MyTube currently shows the account picker dialog.
5. **Library:** a top row of colored gradient tiles (History, Watch later, Playlists, Music, Movies & TV, Podcasts),
   then shelves (History; Watch later "Videos you've added"; ...). MyTube's Library = Playlists rows.
6. **Home, focused shelf grows:** official focused shelf cards are 37.4% of the screen width, other shelves 27.5%.
   This needs per-row sizing on focus; measure jank on the Fire TV before shipping.
7. **Subscriptions polish:** a large "Subscriptions" page title, an "A–Z" header before the alphabetical list,
   and "All" with a list icon in a pill. Consider hiding the "Filter channels" box.
8. **Settings:** official uses two panes (a list with section headers on the left, detail on the right). MyTube uses an icon grid.
9. **Sidebar:** official expanding pushes the content right (MyTube overlays it, which is cheaper; keep unless asked).
   Official also has Movies & TV and Podcasts entries (no data source in this app).
10. Minor: on the official app, Up from the sidebar top doesn't leave the sidebar (MyTube now matches this).

### Performance (paused at the owner's request, resume at the end)
Home scroll benchmark on the Fire TV (15 Down + 15 Up, `dumpsys gfxinfo`), 3 runs each:

| build | janky frames | p95 | p99 | PSS |
|---|---|---|---|---|
| before (`23f95cfa`, original UI) | 129–201 | 27–38 ms | 44–97 ms | 142–146 MB |
| after (`bdb7d42b`) | 137–165 | 28–48 ms | 40–125 ms | 152–154 MB |

No real gain yet; atrace shows it is **render-bound** (DrawFrame avg 8.3 ms / max 22 ms, long dequeueBuffer waits;
binding is cheap). Since then the zoom animation has been removed and cards are larger, so **re-measure first**.
Next ideas: overdraw check (`debug.hwui.overdraw`), cheaper card text (no marquee, fewer layers), compare 4K60 vs 1080p
dropped frames during playback, and memory under long sessions.

### Other
- New strings are English-only: Library, Up next, Search, All, Settings, Quality, Captions, Playback speed, Add account, Live.
- Translations that mention "YouTube" in instructions (linking a phone, cast list) were kept on purpose (they describe interop).
- `HomeChipsGridFragment` was removed; `ChipsRowPresenter` is kept for the optional chips row.

---

## 4. Key files

| Area | Files |
|---|---|
| Rail sidebar | `leanback-1.0.0/.../app/BrowseSupportFragment.java`, `HeadersSupportFragment.java`, `smarttubetv/.../ui/browse/BrowseFragment.java`, `presenter/IconHeaderItemPresenter.java`, `res/layout/icon_header_item.xml` |
| Sections and defaults | `common/.../presenters/BrowsePresenter.java`, `presenters/service/SidebarService.java`, `models/data/BrowseSection.java` |
| Home | `ui/browse/home/HomeRowsFragment.java`, `presenter/HomeCardPresenter.java`, `ui/browse/video/MultipleRowsFragment.java` |
| Cards | `presenter/VideoCardPresenter.java`, `presenter/ChannelCardPresenter.java`, `ui/widgets/complexcardview/ComplexImageCardView.java` |
| Player | `ui/playback/other/VideoPlayerGlue.java`, `ui/mod/leanback/playerglue/tweaks/PlaybackTransportRowPresenter.java`, `res/layout/lb_playback_transport_controls_row.xml`, `common/.../controllers/PlayerUIController.java`, `common/.../prefs/PlayerTweaksData.java` |
| Theme / sizes | `smarttubetv/src/main/res/values/{styles,colors,dimens}.xml`, `common/.../prefs/MainUIData.java` |
| Perf | `tv/util/ViewUtil.java`, `tv/util/GlideCachingModule.java`, `common/.../exoplayer/other/ExoPlayerInitializer.java` |
