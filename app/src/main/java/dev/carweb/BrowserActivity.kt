package dev.carweb

import android.app.Activity
import android.os.Build
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowInsets
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast

// Navegador do celular para um provedor: serve para fazer login (cookies são compartilhados com a tela do carro).
class BrowserActivity : Activity() {
    private lateinit var web: WebView
    private var sawLogin = false
    private val provider get() = PROVIDERS.first { it.id == intent.getStringExtra("id") }

    // Login terminado = passou por uma página de login e saiu dela. Heurística pela URL:
    // se algum provedor fechar cedo demais (SSO com redirecionamentos), ajustar o regex.
    private fun onUrl(u: String) {
        if (LOGIN.containsMatchIn(u)) sawLogin = true
        else if (sawLogin && !isFinishing && provider.detects) { setLogged(this, provider, true); Toast.makeText(this, "Login concluído", Toast.LENGTH_SHORT).show(); finish() }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        web = newWebView(this, provider.url, ::onUrl)
        // Aviso curto por cima do site: explica a tela e some sozinho.
        val hint = TextView(this).apply {
            text = if (provider.detects) "Entre na sua conta. Esta tela fecha sozinha ao terminar."
                   else "Entre na sua conta e volte com o botão Voltar."
            textSize = 14f; setTextColor(0xFFE9EEF3.toInt()); gravity = Gravity.CENTER
            val d = resources.displayMetrics.density
            setPadding((16 * d).toInt(), (10 * d).toInt(), (16 * d).toInt(), (10 * d).toInt())
            background = GradientDrawable().apply { setColor(0xE6141A22.toInt()); cornerRadius = 24 * d }
            postDelayed({ animate().alpha(0f).withEndAction { visibility = View.GONE } }, 5000)
        }
        setContentView(FrameLayout(this).apply {
            addView(web)
            addView(hint, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.TOP or Gravity.CENTER_HORIZONTAL)
                .apply { topMargin = (12 * resources.displayMetrics.density).toInt() })
            padForBars()
        })
        // Tela cheia: esconde status/navegação (voltam com um deslize na borda).
        if (Build.VERSION.SDK_INT >= 30) window.insetsController?.apply {
            hide(WindowInsets.Type.systemBars())
            systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    // Ao sair (ex.: enviar ao carro) o vídeo do celular para, para não tocar áudio duplicado.
    override fun onStop() {
        if (isFinishing && !provider.detects) setLogged(this, provider, true)
        android.webkit.CookieManager.getInstance().flush() // grava a sessão em disco antes de sair
        web.evaluateJavascript("document.querySelectorAll('video,audio').forEach(m=>m.pause())", null)
        super.onStop()
    }
}

private val LOGIN = Regex("login|signin|sign-in|auth|oauth|sso|account|entrar", RegexOption.IGNORE_CASE)
