package dev.carweb

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView

const val URL = "https://play.max.com"
// UA de desktop: sites de streaming costumam recusar WebView mobile.
const val UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
fun newWebView(c: Context) = WebView(c).apply {
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    settings.mediaPlaybackRequiresUserGesture = false
    settings.userAgentString = UA
    loadUrl(URL)
}
