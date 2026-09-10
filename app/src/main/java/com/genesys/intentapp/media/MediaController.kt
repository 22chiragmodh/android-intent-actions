package com.genesys.intentapp.media

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import com.genesys.intentapp.core.MediaCommand

/**
 * Play / pause / skip for the *currently active* media app.
 *
 * How it works: Android routes hardware media-button presses (the buttons on a
 * headset) to whichever app owns the active MediaSession. We synthesise those
 * same presses with [AudioManager.dispatchMediaKeyEvent]. No permissions, no SDK.
 *
 * Caveat: it targets the active session, not "Spotify" specifically. If Spotify
 * played most recently, that's Spotify. For guaranteed Spotify-only control
 * you'd use the Spotify App Remote SDK (needs auth) — out of scope for the demo.
 */
class MediaController(context: Context) {

    private val audioManager =
        context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun send(command: MediaCommand) {
        val keyCode = when (command) {
            MediaCommand.PLAY -> KeyEvent.KEYCODE_MEDIA_PLAY
            MediaCommand.PAUSE -> KeyEvent.KEYCODE_MEDIA_PAUSE
            MediaCommand.PLAY_PAUSE -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
            MediaCommand.NEXT -> KeyEvent.KEYCODE_MEDIA_NEXT
            MediaCommand.PREVIOUS -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
        }
        // A real button press is a DOWN followed by an UP.
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }
}
