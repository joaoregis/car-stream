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
// Por provedor: "Entrar" abre o site no celular (login); "Enviar ao carro" define o que o carro mostra.
class MainActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 96, 32, 32) }
        PROVIDERS.forEach { p ->
            col.addView(TextView(this).apply { text = p.name; textSize = 20f })
            col.addView(LinearLayout(this).apply {
                addView(Button(context).apply {
                    text = "Entrar (celular)"
                    setOnClickListener { startActivity(Intent(context, BrowserActivity::class.java).putExtra("url", p.url)) }
                }, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
                addView(Button(context).apply {
                    text = "Enviar ao carro"
                    setOnClickListener { select(context, p); Toast.makeText(context, "${p.name} → carro", Toast.LENGTH_SHORT).show() }
                }, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
            })
        }
        setContentView(ScrollView(this).apply { addView(col, MATCH_PARENT, WRAP_CONTENT) })
    }
}
