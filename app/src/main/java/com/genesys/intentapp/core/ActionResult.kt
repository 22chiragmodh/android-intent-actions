package com.genesys.intentapp.core

/** Outcome of trying to run an [AppAction]. Handy for speaking a reply later. */
sealed interface ActionResult {
    data class Ok(val message: String) : ActionResult
    data class Failed(val message: String) : ActionResult
}
