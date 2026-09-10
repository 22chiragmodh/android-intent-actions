package com.genesys.intentapp.launcher

import com.genesys.intentapp.core.TargetApp

/**
 * One place for every third-party package name we care about.
 * Add a new app here and the launcher/deep-link code just works.
 */
object KnownApps {

    const val SPOTIFY = "com.spotify.music"
    const val YOUTUBE_MUSIC = "com.google.android.apps.youtube.music"
    const val INSTAGRAM = "com.instagram.android"
    const val YOUTUBE = "com.google.android.youtube"

    fun packageOf(target: TargetApp): String = when (target) {
        TargetApp.SPOTIFY -> SPOTIFY
        TargetApp.YOUTUBE_MUSIC -> YOUTUBE_MUSIC
        TargetApp.INSTAGRAM -> INSTAGRAM
        TargetApp.YOUTUBE -> YOUTUBE
    }

    /** Play Store fallback when an app is not installed. */
    fun playStoreUri(pkg: String) = "https://play.google.com/store/apps/details?id=$pkg"
}
