package dev.carweb

import android.app.Activity
import android.os.Bundle

// Tela do celular: serve para fazer login (cookies são compartilhados com a tela do carro).
class MainActivity : Activity() {
    override fun onCreate(b: Bundle?) { super.onCreate(b); setContentView(newWebView(this)) }
}
