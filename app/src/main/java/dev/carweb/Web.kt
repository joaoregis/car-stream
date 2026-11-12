package dev.carweb

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

const val URL = "https://www.hbomax.com/auth/login"
// UA de desktop: sites de streaming costumam recusar WebView mobile.
const val UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
fun newWebView(c: Context) = WebView(c).apply {
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    settings.mediaPlaybackRequiresUserGesture = false
    settings.userAgentString = UA
    // Sem WebViewClient o Android manda toda navegação para o navegador padrão.
    // Navegação http(s) fica aqui; intent:// e esquemas de app (que tentam abrir o app do Max) são bloqueados.
    webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest) =
            r.url.scheme !in setOf("http", "https")
    }
    // DRM (Widevine/EME): o WebView só libera se o app conceder PROTECTED_MEDIA_ID.
    webChromeClient = object : WebChromeClient() {
        override fun onPermissionRequest(r: PermissionRequest) {
            val ok = r.resources.filter { it == PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID }
            if (ok.isNotEmpty()) r.grant(ok.toTypedArray()) else r.deny()
        }
    }
    loadUrl(URL)
}
