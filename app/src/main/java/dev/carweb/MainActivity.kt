package dev.carweb

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT as MATCH
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT as WRAP
import android.view.WindowInsets
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams as LP
import android.widget.ScrollView
import android.widget.TextView

// Home do celular. Em cima, os provedores (entrar / enviar ao carro). Embaixo, ao alcance do polegar,
// o painel "No carro" com o controle remoto: a tela do carro não tem botões, tudo se opera daqui.
class MainActivity : Activity() {
    private lateinit var list: LinearLayout
    private lateinit var dock: LinearLayout
    private var pending: String? = null // login aberto, resultado ainda desconhecido
    private var failed: String? = null  // voltou do login sem concluir

    private val Int.dp get() = (this * resources.displayMetrics.density).toInt()
    private fun c(id: Int) = getColor(id)

    private fun shape(fill: Int, radius: Int, stroke: Int? = null) = GradientDrawable().apply {
        setColor(fill); cornerRadius = radius.dp.toFloat()
        if (stroke != null) setStroke((1.5f * resources.displayMetrics.density).toInt(), stroke)
    }
    private fun pressable(d: Drawable) = RippleDrawable(ColorStateList.valueOf(c(R.color.ripple)), d, d)

    private fun text(s: CharSequence, sp: Float, color: Int, medium: Boolean = false) = TextView(this).apply {
        text = s; textSize = sp; setTextColor(color)
        typeface = Typeface.create(if (medium) "sans-serif-medium" else "sans-serif", Typeface.NORMAL)
    }
    private fun label(s: String) =
        text(s.uppercase(), 12f, c(R.color.text_muted), true).apply { letterSpacing = 0.14f }

    private fun button(s: String, fill: Int, fg: Int, onTap: () -> Unit) = Button(this).apply {
        text = s; isAllCaps = false; textSize = 16f; setTextColor(fg)
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        stateListAnimator = null; minHeight = 52.dp; minimumHeight = 52.dp
        background = pressable(shape(fill, 16))
        setOnClickListener { it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); onTap() }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val land = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20.dp, 4.dp, 20.dp, 20.dp) }
        dock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_VERTICAL
            val r = 28.dp.toFloat()
            background = GradientDrawable().apply {
                setColor(c(R.color.surface)); setStroke(1.dp, c(R.color.outline))
                cornerRadii = if (land) FloatArray(8) { r } else floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f)
            }
        }
        val main = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(header())
            addView(ScrollView(context).apply { isVerticalScrollBarEnabled = false; addView(list, MATCH, WRAP) }, LP(MATCH, 0, 1f))
        }
        // Retrato: painel ancorado embaixo. Paisagem (celular deitado no suporte): painel na lateral.
        val root = LinearLayout(this).apply {
            orientation = if (land) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
            if (land) { addView(main, LP(0, MATCH, 1f)); addView(dock, LP(300.dp, MATCH).apply { setMargins(0, 16.dp, 16.dp, 16.dp) }) }
            else { addView(main, LP(MATCH, 0, 1f)); addView(dock, LP(MATCH, WRAP)) }
        }
        // O app desenha sob as barras do sistema. Em retrato o painel vai até a borda de baixo e
        // absorve o recuo da barra de navegação, em vez de deixar uma faixa vazia sob ele.
        root.setOnApplyWindowInsetsListener { v, i ->
            val s = if (Build.VERSION.SDK_INT >= 30)
                i.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()).let { intArrayOf(it.left, it.top, it.right, it.bottom) }
            else @Suppress("DEPRECATION") intArrayOf(i.systemWindowInsetLeft, i.systemWindowInsetTop, i.systemWindowInsetRight, i.systemWindowInsetBottom)
            v.setPadding(s[0], s[1], s[2], if (land) s[3] else 0)
            dock.setPadding(20.dp, 18.dp, 20.dp, 20.dp + if (land) 0 else s[3])
            i
        }
        setContentView(root)
        if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        @Suppress("DEPRECATION") window.navigationBarColor = android.graphics.Color.TRANSPARENT
    }

    override fun onResume() {
        super.onResume()
        failed = pending?.takeIf { id -> PROVIDERS.any { it.id == id && !loggedIn(this, it) } }
        pending = null
        render()
    }

    private fun login(p: Provider) {
        pending = p.id
        startActivity(Intent(this, BrowserActivity::class.java).putExtra("id", p.id))
    }

    private fun header() = LinearLayout(this).apply {
        gravity = Gravity.CENTER_VERTICAL; setPadding(20.dp, 16.dp, 20.dp, 14.dp)
        addView(ImageView(context).apply {
            setImageResource(R.drawable.ic_launcher_foreground)
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(0xFFFFC24A.toInt(), 0xFFFF9A0A.toInt()))
                .apply { cornerRadius = 12.dp.toFloat() }
            clipToOutline = true
        }, LP(40.dp, 40.dp))
        addView(text("CarWeb", 22f, c(R.color.text), true), LP(WRAP, WRAP).apply { marginStart = 12.dp })
    }

    private fun render() {
        TransitionManager.beginDelayedTransition(window.decorView as ViewGroup, AutoTransition().setDuration(160))
        // "No carro" = último provedor enviado, se ainda estiver logado. (selected() sozinho devolve o 1º por padrão.)
        val now = if (PROVIDERS.isEmpty() || !prefs(this).contains("provider")) null
                  else selected(this).takeIf { loggedIn(this, it) }
        list.removeAllViews()
        list.addView(label("Provedores"), LP(WRAP, WRAP).apply { bottomMargin = 10.dp; marginStart = 4.dp })
        if (PROVIDERS.isEmpty()) list.addView(text("Nenhum provedor configurado.", 16f, c(R.color.text_muted)))
        PROVIDERS.forEachIndexed { i, p -> list.addView(card(p, i, p == now), LP(MATCH, WRAP).apply { bottomMargin = 12.dp }) }
        renderDock(now)
    }

    private fun card(p: Provider, i: Int, onCar: Boolean): View {
        val ok = loggedIn(this, p)
        val status = when {
            onCar -> text("No carro agora", 14f, c(R.color.accent_text), true).apply {
                val chk = getDrawable(R.drawable.ic_check)!!.mutate().apply { setBounds(0, 0, 16.dp, 16.dp); setTint(c(R.color.accent_text)) }
                setCompoundDrawablesRelative(chk, null, null, null); compoundDrawablePadding = 6.dp
            }
            ok -> text("Conectado", 14f, c(R.color.text_muted))
            failed == p.id -> text("Login não concluído. Tente de novo.", 14f, c(R.color.warn))
            else -> text("Não conectado", 14f, c(R.color.text_muted))
        }
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(16.dp, 14.dp, 16.dp, 16.dp)
            background = shape(c(R.color.surface), 22, c(if (onCar) R.color.accent else R.color.outline))
            addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                // Monograma em cor própria do app (não é a marca do provedor).
                addView(text(p.name.take(1), 20f, 0xFF14181D.toInt(), true).apply {
                    gravity = Gravity.CENTER; background = shape(BADGES[i % BADGES.size], 24)
                }, LP(48.dp, 48.dp))
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(text(p.name, 18f, c(R.color.text), true)); addView(status)
                }, LP(0, WRAP, 1f).apply { marginStart = 14.dp })
                if (ok) addView(text("Refazer login", 14f, c(R.color.text_muted), true).apply {
                    gravity = Gravity.CENTER; minHeight = 48.dp; setPadding(12.dp, 0, 12.dp, 0)
                    isClickable = true; isFocusable = true
                    background = RippleDrawable(ColorStateList.valueOf(c(R.color.ripple)), null, shape(-1, 14))
                    setOnClickListener { setLogged(context, p, false); login(p) }
                })
            })
            if (!onCar) addView(
                if (ok) button("Enviar ao carro", c(R.color.accent), c(R.color.on_accent)) { failed = null; select(context, p); render() }
                else button("Entrar", c(R.color.surface_high), c(R.color.text)) { login(p) },
                LP(MATCH, WRAP).apply { topMargin = 14.dp })
        }
    }

    private fun renderDock(now: Provider?) {
        dock.removeAllViews()
        dock.addView(label("No carro"))
        dock.addView(text(now?.name ?: "Nada enviado", 24f, c(if (now != null) R.color.text else R.color.text_muted), true))
        if (now == null) dock.addView(text("Entre em um provedor e toque em Enviar ao carro.", 14f, c(R.color.text_muted)))
        dock.addView(LinearLayout(this).apply {
            addView(remote("Voltar", R.drawable.ic_back, "back", now != null), LP(0, 60.dp, 1f))
            addView(remote("Recarregar", R.drawable.ic_reload, "reload", now != null), LP(0, 60.dp, 1f).apply { marginStart = 12.dp })
        }, LP(MATCH, WRAP).apply { topMargin = 14.dp })
    }

    // Botão do controle remoto: ícone + rótulo centralizados, alvo de 60dp.
    private fun remote(s: String, icon: Int, cmd: String, on: Boolean) = LinearLayout(this).apply {
        gravity = Gravity.CENTER; contentDescription = "$s no carro"
        background = pressable(shape(c(R.color.surface_high), 18))
        isEnabled = on; isClickable = on; isFocusable = on; alpha = if (on) 1f else 0.38f
        addView(ImageView(context).apply { setImageResource(icon); imageTintList = ColorStateList.valueOf(c(R.color.text)) }, LP(24.dp, 24.dp))
        addView(text(s, 16f, c(R.color.text), true), LP(WRAP, WRAP).apply { marginStart = 8.dp })
        setOnClickListener { it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); carCommand(context, cmd) }
    }
}

private val BADGES = intArrayOf(0xFF8FA2FF.toInt(), 0xFF5FD4C4.toInt(), 0xFFFF9E8A.toInt(), 0xFFC9A8FF.toInt(), 0xFF9BD77A.toInt())
