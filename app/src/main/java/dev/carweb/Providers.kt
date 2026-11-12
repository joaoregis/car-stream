package dev.carweb

import android.content.Context

// detects=false: a URL inicial não é de login, então não dá para saber pela URL quando o login acabou;
// nesses casos considera-se logado ao fechar a tela do site.
data class Provider(val id: String, val name: String, val url: String, val detects: Boolean = true)

// Só o HBO Max foi testado no WebView; os outros dependem do DRM de cada serviço.
val PROVIDERS = listOf(
    Provider("hbomax", "HBO Max", "https://www.hbomax.com/auth/login"),
    Provider("netflix", "Netflix", "https://www.netflix.com/login"),
    Provider("disney", "Disney+", "https://www.disneyplus.com/login"),
    Provider("prime", "Prime Video", "https://www.primevideo.com", false),
    Provider("youtube", "YouTube", "https://www.youtube.com", false),
    Provider("globoplay", "Globoplay", "https://globoplay.globo.com", false),
)

// Provedor escolhido para o carro. SharedPreferences: o serviço do Android Auto pode rodar em outro processo/instante.
fun prefs(c: Context) = c.getSharedPreferences("carweb", Context.MODE_PRIVATE)
fun selected(c: Context): Provider =
    PROVIDERS.firstOrNull { it.id == prefs(c).getString("provider", null) } ?: PROVIDERS[0]
fun select(c: Context, p: Provider) = prefs(c).edit().putString("provider", p.id).apply()

// "Logado" = marcado quando o login termina na tela do celular. Não valida expiração: se a sessão cair, use "Refazer login".
fun loggedIn(c: Context, p: Provider) = prefs(c).getBoolean("logged." + p.id, false)
fun setLogged(c: Context, p: Provider, v: Boolean) = prefs(c).edit().putBoolean("logged." + p.id, v).apply()
