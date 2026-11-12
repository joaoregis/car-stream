package dev.carweb

import android.content.Context

data class Provider(val id: String, val name: String, val url: String)

// Só o HBO Max foi testado no WebView; os outros dependem do DRM de cada serviço.
val PROVIDERS = listOf(
    Provider("hbomax", "HBO Max", "https://www.hbomax.com/auth/login"),
    Provider("netflix", "Netflix", "https://www.netflix.com/login"),
    Provider("disney", "Disney+", "https://www.disneyplus.com/login"),
    Provider("prime", "Prime Video", "https://www.primevideo.com"),
    Provider("youtube", "YouTube", "https://www.youtube.com"),
    Provider("globoplay", "Globoplay", "https://globoplay.globo.com"),
)

// Provedor escolhido para o carro. SharedPreferences: o serviço do Android Auto pode rodar em outro processo/instante.
fun prefs(c: Context) = c.getSharedPreferences("carweb", Context.MODE_PRIVATE)
fun selected(c: Context): Provider =
    PROVIDERS.firstOrNull { it.id == prefs(c).getString("provider", null) } ?: PROVIDERS[0]
fun select(c: Context, p: Provider) = prefs(c).edit().putString("provider", p.id).apply()
