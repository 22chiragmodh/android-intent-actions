package com.genesys.intentapp.core

/**
 * A structured, app-agnostic description of "what the user wants to do".
 *
 * In the demo, [com.genesys.intentapp.MainActivity] builds these from button
 * taps. In the real product your reasoning engine (C++/Rust) would emit the
 * same shape as JSON, and only this Kotlin layer would know how to run it on
 * Android. Keeping the intent details out of here is the whole point.
 */
sealed interface AppAction {

    /** Just open an app at its default screen. */
    data class OpenApp(val target: TargetApp) : AppAction

    /** Open Instagram straight into Direct (DM inbox), or a chat with someone. */
    data class OpenInstagramChat(val username: String? = null) : AppAction

    /** Open Spotify on a specific track/album/playlist URI, e.g. "spotify:track:xxxx". */
    data class OpenSpotifyContent(val spotifyUri: String) : AppAction

    /** Transport controls for whatever is currently the active media app. */
    data class MediaControl(val command: MediaCommand) : AppAction
}

enum class TargetApp {
    SPOTIFY,
    YOUTUBE_MUSIC,
    INSTAGRAM,
    YOUTUBE,
}

enum class MediaCommand {
    PLAY,
    PAUSE,
    PLAY_PAUSE,
    NEXT,
    PREVIOUS,
}
