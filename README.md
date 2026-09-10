# IntentApp Demo App

A tiny Android app that opens / controls third-party apps (Spotify, YouTube
Music, Instagram, YouTube) using **Intents** and **deep links**. It's the
groundwork for a voice assistant: today buttons produce the actions, later
`STT -> reasoning engine -> action` will.

## Run it

1. Open this folder in **Android Studio** (Giraffe or newer). Let it sync.
   - Or from a terminal: `./gradlew installDebug` with a device/emulator attached.
2. Install Spotify / YT Music / Instagram / YouTube on the device so there's
   something to open.
3. Launch **IntentApp Demo**, tap the buttons, watch the blue status line.

Min SDK 24, target SDK 34.

## Folder architecture

```
app/src/main/java/com/genesys/intentapp/
│
├── MainActivity.kt          UI only. Each button builds an AppAction and
│                            hands it to the dispatcher. Nothing else.
│
├── core/                    App-agnostic "what to do" + the seam to Android
│   ├── AppAction.kt         sealed model of an action (OpenApp, MediaControl…)
│   │                        -> this is the JSON your reasoning engine will emit
│   ├── ActionResult.kt      Ok / Failed outcome (useful for a spoken reply)
│   └── ActionDispatcher.kt  THE seam: AppAction -> real Android calls
│
├── launcher/                "open an app"
│   ├── KnownApps.kt         every third-party package name, in one place
│   └── AppLauncher.kt       getLaunchIntentForPackage(), deep-link VIEW intents,
│                            Play Store fallback
│
├── deeplink/
│   └── DeepLinks.kt         builds the URIs that jump *inside* an app
│                            (Instagram DM inbox, ig.me chat, spotify: URIs)
│
└── media/
    └── MediaController.kt   play / pause / next via synthesised media-button
                             events (AudioManager.dispatchMediaKeyEvent)
```

Data flow:

```
button tap ─► AppAction ─► ActionDispatcher ─┬─► AppLauncher   ─► Intent ─► other app
                                             ├─► DeepLinks     ─► Uri  ─┘
                                             └─► MediaController ─► media key ─► active player
```

To grow it, replace `MainActivity` with your voice pipeline that outputs the
same `AppAction` objects. `core/`, `launcher/`, `deeplink/`, `media/` don't change.

## How each thing works

| Action | Mechanism |
| --- | --- |
| Open Spotify / YT Music / Instagram / YouTube | `PackageManager.getLaunchIntentForPackage(pkg)` |
| Open a Spotify track/playlist | `ACTION_VIEW` on a `spotify:...` URI, `setPackage("com.spotify.music")` |
| Open Instagram DM inbox | `ACTION_VIEW` on `instagram://direct-inbox` |
| Open Instagram chat with a user | `ACTION_VIEW` on `https://ig.me/m/<username>` |
| Play / Pause / Next / Prev | `AudioManager.dispatchMediaKeyEvent(KeyEvent(...))` |
| App not installed | fallback `ACTION_VIEW` to the Play Store listing |

## Button → Intent reference

Exactly what each button fires. `OpenApp` buttons send a real `Intent`; the
**media buttons do *not* send an Intent at all** — they dispatch a synthesised
hardware media-key event to whichever app owns the active media session.

```json
[
  {
    "button": "Open Spotify",
    "appAction": { "type": "OpenApp", "target": "SPOTIFY" },
    "sends": "Intent",
    "intent": {
      "builtBy": "PackageManager.getLaunchIntentForPackage(\"com.spotify.music\")",
      "action": "android.intent.action.MAIN",
      "categories": ["android.intent.category.LAUNCHER"],
      "package": "com.spotify.music",
      "component": "set by the system to Spotify's launcher activity",
      "data": null,
      "flags": ["FLAG_ACTIVITY_NEW_TASK"]
    },
    "fallbackIfNotInstalled": {
      "action": "android.intent.action.VIEW",
      "data": "https://play.google.com/store/apps/details?id=com.spotify.music",
      "flags": ["FLAG_ACTIVITY_NEW_TASK"]
    }
  },
  {
    "button": "Open YT Music  /  Open Instagram  /  Open YouTube",
    "note": "identical to \"Open Spotify\", only the package changes",
    "appAction": { "type": "OpenApp", "target": "YOUTUBE_MUSIC | INSTAGRAM | YOUTUBE" },
    "sends": "Intent",
    "intent": {
      "action": "android.intent.action.MAIN",
      "categories": ["android.intent.category.LAUNCHER"],
      "package": "com.google.android.apps.youtube.music | com.instagram.android | com.google.android.youtube",
      "data": null,
      "flags": ["FLAG_ACTIVITY_NEW_TASK"]
    }
  },
  {
    "button": "▶ Play",
    "appAction": { "type": "MediaControl", "command": "PLAY" },
    "sends": "NOT an Intent — a media key event",
    "call": "AudioManager.dispatchMediaKeyEvent(KeyEvent(...))",
    "keyEvents": [
      { "action": "ACTION_DOWN", "keyCode": "KEYCODE_MEDIA_PLAY", "code": 126 },
      { "action": "ACTION_UP",   "keyCode": "KEYCODE_MEDIA_PLAY", "code": 126 }
    ],
    "deliveredTo": "the current active MediaSession (whichever app played last)"
  },
  {
    "button": "⏸ Pause",
    "appAction": { "type": "MediaControl", "command": "PAUSE" },
    "sends": "NOT an Intent — a media key event",
    "keyEvents": [
      { "action": "ACTION_DOWN", "keyCode": "KEYCODE_MEDIA_PAUSE", "code": 127 },
      { "action": "ACTION_UP",   "keyCode": "KEYCODE_MEDIA_PAUSE", "code": 127 }
    ]
  },
  {
    "button": "⏭ Next",
    "appAction": { "type": "MediaControl", "command": "NEXT" },
    "sends": "NOT an Intent — a media key event",
    "keyEvents": [
      { "action": "ACTION_DOWN", "keyCode": "KEYCODE_MEDIA_NEXT", "code": 87 },
      { "action": "ACTION_UP",   "keyCode": "KEYCODE_MEDIA_NEXT", "code": 87 }
    ]
  },
  {
    "button": "⏮ Prev",
    "appAction": { "type": "MediaControl", "command": "PREVIOUS" },
    "sends": "NOT an Intent — a media key event",
    "keyEvents": [
      { "action": "ACTION_DOWN", "keyCode": "KEYCODE_MEDIA_PREVIOUS", "code": 88 },
      { "action": "ACTION_UP",   "keyCode": "KEYCODE_MEDIA_PREVIOUS", "code": 88 }
    ]
  },
  {
    "button": "Open this in Spotify",
    "appAction": { "type": "OpenSpotifyContent", "spotifyUri": "spotify:track:4cOdK2wGLETKBW3PvgPWqT" },
    "sends": "Intent",
    "intent": {
      "action": "android.intent.action.VIEW",
      "data": "spotify:track:4cOdK2wGLETKBW3PvgPWqT",
      "package": "com.spotify.music",
      "flags": ["FLAG_ACTIVITY_NEW_TASK"]
    },
    "fallback": "same VIEW intent with no setPackage() if Spotify can't resolve it"
  },
  {
    "button": "Open DM inbox",
    "appAction": { "type": "OpenInstagramChat", "username": null },
    "sends": "Intent",
    "intent": {
      "action": "android.intent.action.VIEW",
      "data": "instagram://direct-inbox",
      "package": "com.instagram.android",
      "flags": ["FLAG_ACTIVITY_NEW_TASK"]
    },
    "fallback": "AppAction.OpenApp(INSTAGRAM) → the MAIN/LAUNCHER intent above"
  },
  {
    "button": "Open chat with user",
    "appAction": { "type": "OpenInstagramChat", "username": "nasa" },
    "sends": "Intent",
    "intent": {
      "action": "android.intent.action.VIEW",
      "data": "https://ig.me/m/nasa",
      "package": "com.instagram.android",
      "flags": ["FLAG_ACTIVITY_NEW_TASK"]
    },
    "fallback": "AppAction.OpenApp(INSTAGRAM) → the MAIN/LAUNCHER intent above"
  }
]
```

### Notes / limitations

- **Media keys target the *active* media session**, not Spotify by name. If
  Spotify played last, that's Spotify. For guaranteed Spotify-only transport +
  "play this now", use the **Spotify App Remote SDK** (needs app registration +
  OAuth) — deliberately left out of the demo.
- **Deep links are app-version dependent.** `instagram://direct-inbox` and
  `ig.me` work today; always go through `AppLauncher.openDeepLink`, which falls
  back to just opening the app.
- **Android 11+ package visibility**: every package/scheme we touch is declared
  in `AndroidManifest.xml` under `<queries>`. Add an app there too, or
  `getLaunchIntentForPackage` returns null.
- A raw `spotify:` URI won't auto-play — Spotify opens on the item and the user
  (or Premium autoplay) starts it.
