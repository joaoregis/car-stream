package dev.carweb

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.webkit.WebView

// Navegador do celular para um provedor: serve para fazer login (cookies são compartilhados com a tela do carro).
class BrowserActivity : Activity() {
    private lateinit var web: WebView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        web = newWebView(this, intent.getStringExtra("url") ?: PROVIDERS[0].url)
        // targetSdk 35 desenha sob as barras do sistema; sem este recuo os controles do player ficam escondidos.
        web.setOnApplyWindowInsetsListener { v, i ->
            if (Build.VERSION.SDK_INT >= 30) {
                val s = i.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                v.setPadding(s.left, s.top, s.right, s.bottom)
            } else {
                @Suppress("DEPRECATION")
                v.setPadding(i.systemWindowInsetLeft, i.systemWindowInsetTop, i.systemWindowInsetRight, i.systemWindowInsetBottom)
            }
            i
        }
        setContentView(web)
        // Tela cheia: esconde status/navegação (voltam com um deslize na borda).
        if (Build.VERSION.SDK_INT >= 30) window.insetsController?.apply {
            hide(WindowInsets.Type.systemBars())
            systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    // Ao sair (ex.: enviar ao carro) o vídeo do celular para, para não tocar áudio duplicado.
    override fun onStop() {
        web.evaluateJavascript("document.querySelectorAll('video,audio').forEach(m=>m.pause())", null)
        super.onStop()
    }
}
