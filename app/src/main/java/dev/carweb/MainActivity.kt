package dev.carweb

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

// Home provisória (sem design): UI/UX final será feita à parte.
// Provedor sem login: botão "Entrar". Já logado: um toque envia direto ao carro; "Refazer login" abre o site de novo.
class MainActivity : Activity() {
    private lateinit var col: LinearLayout

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 32, 32, 32) }
        setContentView(ScrollView(this).apply { addView(col, MATCH_PARENT, WRAP_CONTENT); padForBars() })
    }

    override fun onResume() { super.onResume(); render() }

    private fun login(p: Provider) =
        startActivity(Intent(this, BrowserActivity::class.java).putExtra("id", p.id))

    private fun render() {
        col.removeAllViews()
        val now = selected(this)
        PROVIDERS.forEach { p ->
            val ok = loggedIn(this, p)
            col.addView(TextView(this).apply {
                text = p.name + if (ok) "  ✓ logado" else ""
                textSize = 20f
            })
            col.addView(LinearLayout(this).apply {
                addView(Button(context).apply {
                    text = if (!ok) "Entrar" else if (p == now) "No carro ✓" else "Enviar ao carro"
                    setOnClickListener {
                        if (!ok) login(p)
                        else { select(context, p); Toast.makeText(context, "${p.name} → carro", Toast.LENGTH_SHORT).show(); render() }
                    }
                }, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
                if (ok) addView(Button(context).apply {
                    text = "Refazer login"
                    setOnClickListener { setLogged(context, p, false); login(p) }
                }, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
            })
        }
    }
}
