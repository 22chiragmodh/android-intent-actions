package com.genesys.intentapp.deeplink

import android.net.Uri

/**
 * Builders for the URIs that jump *inside* an app instead of just opening it.
 *
 * These are published/known deep links, not private APIs. They can still change
 * between app versions, so always launch them through
 * [com.genesys.intentapp.launcher.AppLauncher.openDeepLink], which has fallbacks.
 */
object DeepLinks {

    /** Instagram Direct inbox (all chats). */
    fun instagramDirectInbox(): Uri = Uri.parse("instagram://direct-inbox")

    /**
     * Open a chat with one person. ig.me is Instagram's official messaging link
     * and resolves inside the app when it's installed.
     */
    fun instagramChatWith(username: String): Uri =
        Uri.parse("https://ig.me/m/${username.trim().removePrefix("@")}")

    /** Instagram profile, as a secondary way to reach someone. */
    fun instagramProfile(username: String): Uri =
        Uri.parse("https://instagram.com/${username.trim().removePrefix("@")}")

    /**
     * Any Spotify content URI, e.g.
     *   spotify:track:4cOdK2wGLETKBW3PvgPWqT
     *   spotify:playlist:37i9dQZF1DXcBWIGoYBM5M
     * Spotify opens on that item (playback still needs a user tap / Premium).
     */
    fun spotifyContent(spotifyUri: String): Uri = Uri.parse(spotifyUri)

    /** A YouTube Music search, handy for "play <song> on YT Music". */
    fun youtubeMusicSearch(query: String): Uri =
        Uri.parse("https://music.youtube.com/search?q=${Uri.encode(query)}")
}
