package dev.carweb

import android.content.Context

// detects=false: a URL inicial não é de login, então não dá para saber pela URL quando o login acabou;
// nesses casos considera-se logado ao fechar a tela do site.
data class Provider(val id: String, val name: String, val url: String, val detects: Boolean = true)

// Só provedores já testados no WebView. Para adicionar um: uma linha aqui.
val PROVIDERS = listOf(
    Provider("hbomax", "HBO Max", "https://www.hbomax.com/auth/login"),
    Provider("disney", "Disney+", "https://www.disneyplus.com/login"),
)

// Provedor escolhido para o carro. SharedPreferences: o serviço do Android Auto pode rodar em outro processo/instante.
fun prefs(c: Context) = c.getSharedPreferences("carweb", Context.MODE_PRIVATE)
fun selected(c: Context): Provider =
    PROVIDERS.firstOrNull { it.id == prefs(c).getString("provider", null) } ?: PROVIDERS[0]
fun select(c: Context, p: Provider) = prefs(c).edit().putString("provider", p.id).apply()

// "Logado" = marcado quando o login termina na tela do celular. Não valida expiração: se a sessão cair, use "Refazer login".
fun loggedIn(c: Context, p: Provider) = prefs(c).getBoolean("logged." + p.id, false)
fun setLogged(c: Context, p: Provider, v: Boolean) = prefs(c).edit().putBoolean("logged." + p.id, v).apply()
