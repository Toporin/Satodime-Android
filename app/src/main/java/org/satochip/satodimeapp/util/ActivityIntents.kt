package org.satochip.satodimeapp.util

import android.content.Context
import android.content.Intent
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