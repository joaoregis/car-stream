package dev.carweb

import android.app.Presentation
import android.content.Intent
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.os.SystemClock
import android.view.MotionEvent
import android.view.Surface
import android.webkit.WebView
import androidx.car.app.*
import androidx.car.app.model.*
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.car.app.validation.HostValidator

class CarWebService : CarAppService() {
    override fun createHostValidator() = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR // ponytail: sideload only
    override fun onCreateSession() = object : Session() {
        override fun onCreateScreen(i: Intent) = WebScreen(carContext)
    }
}

class WebScreen(c: CarContext) : Screen(c), SurfaceCallback {
    private var vd: VirtualDisplay? = null
    private var web: WebView? = null
    private var pres: Presentation? = null

    // Trocou o provedor no celular -> o carro carrega o novo site.
    private val onSelect = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        web?.loadUrl(selected(carContext).url)
    }

    init {
        c.getCarService(AppManager::class.java).setSurfaceCallback(this)
        prefs(c).registerOnSharedPreferenceChangeListener(onSelect)
    }

    override fun onGetTemplate(): Template = NavigationTemplate.Builder()
        .setActionStrip(ActionStrip.Builder()
            .addAction(Action.Builder().setTitle("Recarregar").setOnClickListener { web?.reload() }.build())
            .addAction(Action.Builder().setTitle("Voltar").setOnClickListener { web?.goBack() }.build())
            .build())
        .build()

    override fun onSurfaceAvailable(c: SurfaceContainer) {
        val s: Surface = c.surface ?: return
        vd = (carContext.getSystemService(DisplayManager::class.java)).createVirtualDisplay(
            "carweb", c.width, c.height, c.dpi, s,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION)
        pres = Presentation(carContext, vd!!.display).also {
            web = newWebView(it.context, selected(carContext).url); it.setContentView(web!!); it.show()
        }
    }

    override fun onSurfaceDestroyed(c: SurfaceContainer) {
        pres?.dismiss(); web?.destroy(); vd?.release(); pres = null; web = null; vd = null
    }

    // Toque na tela do carro -> toque no WebView.
    override fun onClick(x: Float, y: Float) {
        val w = web ?: return
        val t = SystemClock.uptimeMillis()
        for (a in intArrayOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP))
            MotionEvent.obtain(t, t, a, x, y, 0).also { w.dispatchTouchEvent(it); it.recycle() }
    }

    override fun onScroll(dx: Float, dy: Float) { web?.scrollBy(dx.toInt(), dy.toInt()) }
}
