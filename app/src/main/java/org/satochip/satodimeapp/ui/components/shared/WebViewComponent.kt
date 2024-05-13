package org.satochip.satodimeapp.ui.components.shared

import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WebViewComponent(
    url: MutableState<String>,
    isSpinnerActive: MutableState<Boolean>
) {
    AndroidView(
        modifier = Modifier.alpha(
            if (isSpinnerActive.value) 0.0f else 1f
        ),
        factory = { context ->
            val webview = WebView(context)
            webview.apply {
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                this.clearCache(true)
                this.clearHistory()
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isSpinnerActive.value = false
                    }
                }

                loadUrl(url.value)

                CookieManager.getInstance().acceptCookie()
                CookieManager.getInstance().acceptThirdPartyCookies(this)
            }
        }
    )
}