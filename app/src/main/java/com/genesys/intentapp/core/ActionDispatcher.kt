package com.genesys.intentapp.core

import android.content.Context
import com.genesys.intentapp.deeplink.DeepLinks
import com.genesys.intentapp.launcher.AppLauncher
import com.genesys.intentapp.launcher.KnownApps
import com.genesys.intentapp.media.MediaController

/**
 * The one place that turns an [AppAction] into real Android calls.
 *
 * Everything above this (UI today, a reasoning engine tomorrow) only deals in
 * [AppAction]. Everything below (intents, deep links, media keys) is hidden in
 * the launcher/deeplink/media packages. This class is the seam between them.
 */
class ActionDispatcher(context: Context) {

    private val appContext = context.applicationContext
    private val launcher = AppLauncher(appContext)
    private val media = MediaController(appContext)

    fun dispatch(action: AppAction): ActionResult = when (action) {

        is AppAction.OpenApp -> {
            val pkg = KnownApps.packageOf(action.target)
            if (launcher.openApp(pkg)) ActionResult.Ok("Opening ${action.target.label()}")
            else ActionResult.Failed("${action.target.label()} isn't installed — opened the Play Store")
        }

        is AppAction.OpenInstagramChat -> {
            val uri = if (action.username != null) {
                DeepLinks.instagramChatWith(action.username)
            } else {
                DeepLinks.instagramDirectInbox()
            }
            val started = launcher.openDeepLink(uri, preferredPkg = KnownApps.INSTAGRAM)
            if (started) ActionResult.Ok("Opening Instagram chat")
            else {
                launcher.openApp(KnownApps.INSTAGRAM)
                ActionResult.Failed("Couldn't open the chat directly — opened Instagram instead")
            }
        }

        is AppAction.OpenSpotifyContent -> {
            val started = launcher.openDeepLink(
                DeepLinks.spotifyContent(action.spotifyUri),
                preferredPkg = KnownApps.SPOTIFY
            )
            if (started) ActionResult.Ok("Opening that in Spotify")
            else ActionResult.Failed("Couldn't open the Spotify link")
        }

        is AppAction.MediaControl -> {
            media.send(action.command)
            ActionResult.Ok("Sent ${action.command.name.lowercase()} to the active player")
        }
    }

    private fun TargetApp.label() = when (this) {
        TargetApp.SPOTIFY -> "Spotify"
        TargetApp.YOUTUBE_MUSIC -> "YouTube Music"
        TargetApp.INSTAGRAM -> "Instagram"
        TargetApp.YOUTUBE -> "YouTube"
    }
}
