package com.genesys.intentapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.genesys.intentapp.core.ActionDispatcher
import com.genesys.intentapp.core.ActionResult
import com.genesys.intentapp.core.AppAction
import com.genesys.intentapp.core.MediaCommand
import com.genesys.intentapp.core.TargetApp
import com.genesys.intentapp.databinding.ActivityMainBinding

/**
 * Demo UI. Every button does the same two steps the real assistant will do:
 *   1. build an [AppAction]  (this is what the reasoning engine will output)
 *   2. hand it to [ActionDispatcher]  (the Android side, unchanged)
 *
 * Swap the buttons for "STT -> reasoning -> AppAction" later and nothing below
 * MainActivity has to change.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val dispatcher by lazy { ActionDispatcher(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ---- Spotify ----
        binding.btnOpenSpotify.setOnClickListener {
            run(AppAction.OpenApp(TargetApp.SPOTIFY))
        }
        binding.btnSpotifyPlay.setOnClickListener {
            run(AppAction.MediaControl(MediaCommand.PLAY))
        }
        binding.btnSpotifyPause.setOnClickListener {
            run(AppAction.MediaControl(MediaCommand.PAUSE))
        }
        binding.btnSpotifyNext.setOnClickListener {
            run(AppAction.MediaControl(MediaCommand.NEXT))
        }
        binding.btnSpotifyPrev.setOnClickListener {
            run(AppAction.MediaControl(MediaCommand.PREVIOUS))
        }
        binding.btnSpotifyOpenUri.setOnClickListener {
            val uri = binding.inputSpotifyUri.text?.toString()?.trim().orEmpty()
            if (uri.isEmpty()) toast("Enter a spotify: URI first")
            else run(AppAction.OpenSpotifyContent(uri))
        }

        // ---- YouTube Music ----
        binding.btnOpenYtMusic.setOnClickListener {
            run(AppAction.OpenApp(TargetApp.YOUTUBE_MUSIC))
        }

        // ---- Instagram ----
        binding.btnOpenInstagram.setOnClickListener {
            run(AppAction.OpenApp(TargetApp.INSTAGRAM))
        }
        binding.btnInstagramInbox.setOnClickListener {
            run(AppAction.OpenInstagramChat(username = null))
        }
        binding.btnInstagramChat.setOnClickListener {
            val user = binding.inputInstagramUser.text?.toString()?.trim().orEmpty()
            if (user.isEmpty()) toast("Enter an Instagram username first")
            else run(AppAction.OpenInstagramChat(username = user))
        }

        // ---- YouTube ----
        binding.btnOpenYoutube.setOnClickListener {
            run(AppAction.OpenApp(TargetApp.YOUTUBE))
        }
    }

    private fun run(action: AppAction) {
        when (val result = dispatcher.dispatch(action)) {
            is ActionResult.Ok -> setStatus(result.message)
            is ActionResult.Failed -> setStatus("⚠️ " + result.message)
        }
    }

    private fun setStatus(text: String) {
        binding.status.text = text
    }

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}
