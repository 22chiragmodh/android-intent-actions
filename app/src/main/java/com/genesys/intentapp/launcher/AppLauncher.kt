package com.genesys.intentapp.launcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

/**
 * Opens installed apps.
 *
 * The key API is [android.content.pm.PackageManager.getLaunchIntentForPackage]:
 * given a package name it returns the Intent that Android itself uses when you
 * tap the app's icon in the launcher. If the app is missing we fall back to its
 * Play Store page.
 */
class AppLauncher(private val context: Context) {

    fun isInstalled(pkg: String): Boolean =
        context.packageManager.getLaunchIntentForPackage(pkg) != null

    /** @return true if we managed to start something. */
    fun openApp(pkg: String): Boolean {
        val launch = context.packageManager.getLaunchIntentForPackage(pkg)
        return if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launch)
            true
        } else {
            openInPlayStore(pkg)
            false
        }
    }

    /**
     * Fire a VIEW intent at a specific URI (a deep link) and prefer [preferredPkg]
     * to handle it. Used for "open Instagram DMs" / "open this Spotify track".
     */
    fun openDeepLink(uri: Uri, preferredPkg: String? = null): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (preferredPkg != null) setPackage(preferredPkg)
        }
        // resolveActivity respects the <queries> block in the manifest.
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            return true
        }
        // The chosen app can't handle this link — retry without forcing a package.
        if (preferredPkg != null) {
            val open = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (open.resolveActivity(context.packageManager) != null) {
                context.startActivity(open)
                return true
            }
        }
        return false
    }

    fun openInPlayStore(pkg: String) {
        val intent = Intent(Intent.ACTION_VIEW, KnownApps.playStoreUri(pkg).toUri())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
