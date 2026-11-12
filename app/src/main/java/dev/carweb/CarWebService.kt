package dev.carweb

import android.app.Presentation
import android.content.Intent
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.graphics.Bitmap
import android.hardware.display.VirtualDisplay
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.Surface
import android.widget.FrameLayout
import android.webkit.WebView
import androidx.car.app.*
import androidx.car.app.model.*
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.car.app.validation.HostValidator
import androidx.core.graphics.drawable.IconCompat

class CarWebService : CarAppService() {
    override fun createHostValidator() = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR // ponytail: sideload only
    override fun onCreateSession() = object : Session() {
        override fun onCreateScreen(i: Intent) = WebScreen(carContext)
    }
}

private const val MATCH = FrameLayout.LayoutParams.MATCH_PARENT

class WebScreen(c: CarContext) : Screen(c), SurfaceCallback {
    private var vd: VirtualDisplay? = null
    private var web: WebView? = null
    private var pres: Presentation? = null
    private var focus: AudioFocusRequest? = null
    private var sw = 0; private var sh = 0          // tamanho total da Surface
    private var area: Rect? = null                  // parte da Surface realmente visível (sem cartões do Android Auto)

    // A Surface cobre a tela toda, mas no layout dividido (Coolwalk) cartões como o do Spotify ficam por cima de
    // parte dela. O site é encaixado na área estável: assim ele se adapta à largura que sobrou (layout responsivo).
    private fun fit() {
        val w = web ?: return
        val a = area ?: Rect(0, 0, sw, sh)
        w.layoutParams = FrameLayout.LayoutParams(MATCH, MATCH).apply {
            // top=0: a área estável reserva uma faixa no topo para a barra de ações do carro; o site usa essa faixa também.
            setMargins(a.left, 0, sw - a.right, sh - a.bottom)
        }
    }

    // Ao entrar em cena, o CarWeb pede o foco de áudio e manda "pausar" à sessão de mídia ativa
    // (Spotify, YouTube Music...). O foco sozinho nem todo app respeita, por isso os dois.
    private fun pauseOtherMedia() {
        val am = carContext.getSystemService(AudioManager::class.java)
        focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build())
            .setOnAudioFocusChangeListener { }.build().also { am.requestAudioFocus(it) }
        for (a in intArrayOf(KeyEvent.ACTION_DOWN, KeyEvent.ACTION_UP))
            am.dispatchMediaKeyEvent(KeyEvent(a, KeyEvent.KEYCODE_MEDIA_PAUSE))
    }

    // O celular opera o carro por SharedPreferences: "provider" troca o site; "cmd" ("back"/"reload" + marca de tempo) navega.
    private val onSelect = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
        when (key) {
            "provider" -> web?.loadUrl(selected(carContext).url)
            "cmd" -> when (p.getString("cmd", "")?.substringBefore(':')) {
                "back" -> web?.goBack()
                "reload" -> web?.reload()
            }
        }
    }

    init {
        c.getCarService(AppManager::class.java).setSurfaceCallback(this)
        prefs(c).registerOnSharedPreferenceChangeListener(onSelect)
    }

    // O template de navegação exige uma barra de ações com pelo menos uma ação. A tela do carro é só exibição,
    // então fica uma única ação com ícone transparente e sem efeito. Voltar/recarregar são comandos do celular.
    override fun onGetTemplate(): Template = NavigationTemplate.Builder()
        .setActionStrip(ActionStrip.Builder().addAction(Action.Builder()
            .setIcon(CarIcon.Builder(IconCompat.createWithBitmap(Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888))).build())
            .setOnClickListener { }.build()).build())
        .build()

    override fun onSurfaceAvailable(c: SurfaceContainer) {
        val s: Surface = c.surface ?: return
        pauseOtherMedia()
        vd = (carContext.getSystemService(DisplayManager::class.java)).createVirtualDisplay(
            "carweb", c.width, c.height, c.dpi, s,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION)
        sw = c.width; sh = c.height
        pres = Presentation(carContext, vd!!.display).also {
            web = newWebView(it.context, selected(carContext).url)
            it.setContentView(FrameLayout(it.context).apply { setBackgroundColor(android.graphics.Color.BLACK); addView(web) }); fit(); it.show()
        }
    }

    override fun onSurfaceDestroyed(c: SurfaceContainer) {
        focus?.let { carContext.getSystemService(AudioManager::class.java).abandonAudioFocusRequest(it) }; focus = null
        pres?.dismiss(); web?.destroy(); vd?.release(); pres = null; web = null; vd = null
    }

    override fun onStableAreaChanged(r: Rect) { area = Rect(r); fit() }

    // Toque na tela do carro -> toque no WebView (coordenadas da Surface, descontando a margem do site).
    override fun onClick(x: Float, y: Float) {
        val w = web ?: return
        val a = area ?: Rect()
        val t = SystemClock.uptimeMillis()
        for (m in intArrayOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP))
            MotionEvent.obtain(t, t, m, x - a.left, y, 0).also { w.dispatchTouchEvent(it); it.recycle() }
    }

    override fun onScroll(dx: Float, dy: Float) { web?.scrollBy(dx.toInt(), dy.toInt()) }
}
