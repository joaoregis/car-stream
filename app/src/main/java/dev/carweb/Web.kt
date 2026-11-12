package dev.carweb

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

// UA de desktop: sites de streaming costumam recusar WebView mobile.
const val UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
fun newWebView(c: Context, url: String, onUrl: (String) -> Unit = {}) = WebView(c).apply {
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    settings.mediaPlaybackRequiresUserGesture = false
    settings.userAgentString = UA
    // Sem WebViewClient o Android manda toda navegação para o navegador padrão.
    // Navegação http(s) fica aqui; intent:// e esquemas de app (que tentam abrir o app do Max) são bloqueados.
    webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest) =
            r.url.scheme !in setOf("http", "https")

        // Dispara também em navegação interna de SPA (pushState), onde onPageFinished não vem.
        override fun doUpdateVisitedHistory(v: WebView, u: String, isReload: Boolean) = onUrl(u)
    }
    // DRM (Widevine/EME): o WebView só libera se o app conceder PROTECTED_MEDIA_ID.
    webChromeClient = object : WebChromeClient() {
        override fun onPermissionRequest(r: PermissionRequest) {
            val ok = r.resources.filter { it == PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID }
            if (ok.isNotEmpty()) r.grant(ok.toTypedArray()) else r.deny()
        }
    }
    // Logins de streaming usam cookies de terceiros (SSO/antifraude); o WebView bloqueia por padrão.
    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
    loadUrl(url)
}

// targetSdk 35 desenha sob as barras do sistema (status, navegação, recorte da câmera): recua o conteúdo.
fun View.padForBars() = setOnApplyWindowInsetsListener { v, i ->
    if (Build.VERSION.SDK_INT >= 30) {
        val s = i.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
        v.setPadding(s.left, s.top, s.right, s.bottom)
    } else {
        @Suppress("DEPRECATION")
        v.setPadding(i.systemWindowInsetLeft, i.systemWindowInsetTop, i.systemWindowInsetRight, i.systemWindowInsetBottom)
    }
    i
}
