package org.satochip.satodimeapp.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.os.SystemClock
import org.satochip.satodimeapp.services.SatoLog
import java.security.MessageDigest

private const val CLIPBOARD_TAG = "SecretClipboard"
private const val CLEAR_DELAY_MS = 30 * 1000L // 30 seconds
private const val CLIP_LABEL = "Satodime"

/**
 * Copies secret data to the clipboard and clears it after CLEAR_DELAY_MS.
 *
 * Before clearing, the clipboard content is compared with the copied secret (using its hash),
 * so that something copied by the user in the meantime is not erased.
 * On Android 10+, the clipboard cannot be read while the app is in background: in that case,
 * the clipboard is cleared without check.
 *
 * Two timers are used, the first one to fire clears the clipboard and cancels the other:
 * - an in-app timer, exact but paused while the app process is frozen in background (Android 14+),
 * - an alarm, which also fires in background but may be delayed by the system (inexact alarm).
 */
object SecretClipboard {

    // hash of the last secret copied, kept instead of the secret itself
    private var copiedSecretHash: ByteArray? = null

    private val handler = Handler(Looper.getMainLooper())
    private var clearRunnable: Runnable? = null

    fun copySecret(context: Context, secret: String) {
        val appContext = context.applicationContext
        val clipboard = appContext.getSystemService(ClipboardManager::class.java) ?: return

        val clip = ClipData.newPlainText(CLIP_LABEL, secret)
        // flag content as sensitive: hidden in the system clipboard preview and keyboard suggestions
        // (ClipDescription.EXTRA_IS_SENSITIVE on Android 13+, same key works on some older versions)
        clip.description.extras = PersistableBundle().apply {
            putBoolean(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ClipDescription.EXTRA_IS_SENSITIVE
                else "android.content.extra.IS_SENSITIVE",
                true
            )
        }
        clipboard.setPrimaryClip(clip)
        copiedSecretHash = sha256(secret)

        // schedule clipboard clearing (replaces any previously scheduled clearing)
        clearRunnable?.let { handler.removeCallbacks(it) }
        clearRunnable = Runnable { clearIfUnchanged(appContext) }.also {
            handler.postDelayed(it, CLEAR_DELAY_MS)
        }
        val alarmManager = appContext.getSystemService(AlarmManager::class.java) ?: return
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + CLEAR_DELAY_MS,
            clearPendingIntent(appContext)
        )
        SatoLog.d(CLIPBOARD_TAG, "copySecret: clipboard will be cleared in ${CLEAR_DELAY_MS / 1000}s")
    }

    /**
     * Clears the clipboard if it still contains the copied secret, or if its content cannot be read.
     */
    fun clearIfUnchanged(context: Context) {
        // cancel the other timer
        clearRunnable?.let { handler.removeCallbacks(it) }
        clearRunnable = null
        context.getSystemService(AlarmManager::class.java)?.cancel(clearPendingIntent(context))

        val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
        val expectedHash = copiedSecretHash
        copiedSecretHash = null

        // returns null if clipboard is empty, or on Android 10+ when the app is in background
        val clip = try {
            clipboard.primaryClip
        } catch (e: SecurityException) {
            null
        }

        if (clip == null) {
            SatoLog.d(CLIPBOARD_TAG, "clearIfUnchanged: clipboard cannot be read, clearing without check")
            clearClipboard(clipboard)
            return
        }

        val currentText = if (clip.itemCount > 0) clip.getItemAt(0).text?.toString() else null
        if (currentText != null && expectedHash != null && sha256(currentText).contentEquals(expectedHash)) {
            SatoLog.d(CLIPBOARD_TAG, "clearIfUnchanged: clipboard still contains the secret, clearing")
            clearClipboard(clipboard)
        } else if (currentText != null && expectedHash == null && clip.description.label == CLIP_LABEL) {
            // app process was restarted since the copy (hash lost), rely on clip label
            SatoLog.d(CLIPBOARD_TAG, "clearIfUnchanged: clipboard contains Satodime data, clearing")
            clearClipboard(clipboard)
        } else {
            SatoLog.d(CLIPBOARD_TAG, "clearIfUnchanged: clipboard content changed, not cleared")
        }
    }

    private fun clearClipboard(clipboard: ClipboardManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            clipboard.clearPrimaryClip()
        } else {
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        }
    }

    private fun clearPendingIntent(context: Context): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, ClipboardClearReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun sha256(text: String): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
    }
}

/**
 * Receives the alarm scheduled by SecretClipboard.copySecret.
 */
class ClipboardClearReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        SecretClipboard.clearIfUnchanged(context)
    }
}
