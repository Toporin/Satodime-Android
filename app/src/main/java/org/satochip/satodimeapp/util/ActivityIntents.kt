package org.satochip.satodimeapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import org.satochip.satodimeapp.WebviewActivity
import org.satochip.satodimeapp.data.IntentConstants

fun webviewActivityIntent(
    url: String,
    context: Context
) {
    context.startActivity(
        Intent(context, WebviewActivity::class.java)
            .putExtra(IntentConstants.URL_STRING.name, url)
    )
}

// Opens the system per-app language page for this app (Android 13+)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun openAppLanguageSettings(
    context: Context
) {
    context.startActivity(
        Intent(
            Settings.ACTION_APP_LOCALE_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
    )
}
