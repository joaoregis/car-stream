# Briefing: interface e UX do CarWeb

Cole este arquivo inteiro para o Fable 5.2. Ele descreve o que existe, o que deve ser desenhado
e o que não pode quebrar. **A lógica já funciona e foi testada no simulador do carro; falta o design
da tela do celular e do logotipo.**

## 1. O que é o app

CarWeb é um app Android pessoal, instalado por sideload (nunca vai para a Play Store). Ele mostra
sites de streaming (hoje HBO Max e Disney+) na tela do carro (Fiat Argo, Uconnect, Android Auto).

Fluxo do usuário:

1. No celular, ele abre o CarWeb e faz **login** no provedor (uma vez, em geral).
2. Ele toca em **Enviar ao carro** no provedor que quer ver.
3. No carro, o CarWeb mostra o site do provedor, já logado. O usuário toca nos títulos e no player
   do próprio site, na tela do carro.
4. Para voltar de página, recarregar ou trocar de provedor, ele usa o **celular**.

**Princípio central: a tela do carro é só exibição. Toda a operação do app acontece no celular.**
O dono do projeto decidiu que a tela do carro é pequena demais para botões ou controles do app.

Há **duas superfícies**, e só uma é trabalho de design:

- **Celular**: home com os provedores e o controle remoto. **É aqui que fica toda a interface a
  desenhar.** Hoje ela é provisória: botões padrão do Android, sem tema.
- **Carro**: só o site. **Já está pronto; não desenhe nada lá** (ver seção 5).

## 2. Como é por dentro (resumo)

Código em `app/src/main/java/dev/carweb/`. Kotlin, Android Views, sem Compose e sem AppCompat.
Dependência única: `androidx.car.app:app:1.4.0`. minSdk 26, targetSdk 35.

| Arquivo | O que faz | Seu trabalho |
|---|---|---|
| `Providers.kt` | Lista `PROVIDERS`, provedor escolhido, estado de login e comandos para o carro. Tudo em SharedPreferences. | Usar; não mudar a lógica |
| `MainActivity.kt` | **Home do celular** (provisória): linha de Voltar e Recarregar no carro; para cada provedor, nome, botão Entrar ou Enviar ao carro, e Refazer login. | **Redesenhar** |
| `BrowserActivity.kt` | Site no celular, só para login. Fecha sozinha ao detectar o fim do login e volta à home. Gira livremente, tela cheia. | Pouca UI; pode melhorar o feedback |
| `CarWebService.kt` | Tela do carro: hospeda o site, ajusta-o à área visível, repassa o toque, pausa a música e obedece aos comandos do celular. | **Não mexer** |
| `Web.kt` | `newWebView(...)` com DRM, cookies, links internos, e `padForBars()`. | Não mexer (pode usar `padForBars()`) |

Comandos úteis: `scripts/install.sh` (compila e instala no celular) e `scripts/dhu.sh` (simulador do
carro no PC; `argo` = 800x480, parecido com o Uconnect). Há instruções de ambiente no `README.md` e
no `CLAUDE.md`.

## 3. Contrato de dados (use estas funções, não crie outro estado)

- `PROVIDERS: List<Provider>`: o que mostrar (`Provider(id, name, url)`). Adicionar um provedor é uma
  linha, então a interface deve funcionar com 2, 5 ou 8 itens.
- `loggedIn(context, provider): Boolean`: logado ou não.
- `selected(context): Provider`: o que está escolhido para o carro.
- `select(context, provider)`: envia ao carro. O carro troca de site sozinho, ao vivo.
- `carCommand(context, "back")` e `carCommand(context, "reload")`: voltar e recarregar a página que
  está no carro. **Já funcionam de ponta a ponta.**
- Login: `Intent(context, BrowserActivity::class.java).putExtra("id", provider.id)`.
- Refazer login: `setLogged(context, provider, false)` e depois abrir o login.
- O logo do provedor **não existe ainda**: crie um recurso por provedor ou use a inicial e uma cor.
  Não use imagens oficiais de marcas sem direito de uso; prefira uma identidade própria neutra.

Limites que o app não sabe hoje (não invente o dado; se quiser mostrar, avise que exige código novo):

- Se o carro está **conectado** ou não.
- Se a sessão no provedor **expirou**. "Logado" só registra que o login terminou no celular.

## 4. Celular: o que desenhar

Objetivo: abrir o app, ver os provedores, **mandar um para o carro com um toque** e operar o carro
de dentro do app.

### Cartões de provedor

- Uma lista ou grade de **cartões de provedor**. Cada cartão mostra o nome, um destaque visual e o estado:
  - **Não logado**: ação principal "Entrar".
  - **Logado**: ação principal "Enviar ao carro", com "Refazer login" como ação secundária discreta.
  - **No carro agora**: estado claro e diferente (ex.: "No carro ✓"), sem ação redundante.
- Feedback imediato ao enviar (a mudança de estado do cartão já basta; evite Toast).

### Controle remoto do carro

- **Voltar** e **Recarregar** da página que está no carro (`carCommand`). Ficam sempre à mão,
  com o provedor atual em destaque, como um pequeno painel de "agora no carro".
- **Trocar de provedor** é o "Enviar ao carro" dos cartões.

### Para tudo

- Entendido **de relance**, com o celular na mão ou no suporte do carro: bons contrastes, textos
  curtos, alvos de toque grandes (mínimo 48dp).
- **Tema claro e escuro** seguindo o sistema. O uso à noite no carro é comum.
- Estado vazio e erro: se não houver provedores, ou se o login não terminar, uma mensagem curta.
- Todo `View` raiz novo precisa de `padForBars()` (o app desenha sob as barras do sistema).
- O app é em **português do Brasil**.
- Sem tela de configuração, sem menu, sem onboarding longo. É um app de um propósito.

## 4b. Logotipo e ícone do app

O app ainda **não tem ícone** (usa o padrão do Android). Crie a identidade do **CarWeb**:

- **Logotipo**: símbolo próprio que remeta a "tela do carro + streaming". Nada de marcas de terceiros
  (HBO, Disney, Android Auto, Google, Fiat) nem elementos parecidos com elas.
- **Ícone adaptativo** do Android: `res/drawable/ic_launcher_foreground.xml` e
  `ic_launcher_background.xml` (ou cor), `res/mipmap-anydpi-v26/ic_launcher.xml` com
  `<adaptive-icon>`, e a camada **`<monochrome>`** para os ícones temáticos do Android 13+.
  Use **vector drawables** (sem PNG de fora); respeite a zona segura (66dp de 108dp).
- Ligue no `AndroidManifest.xml` com `android:icon` (e `android:roundIcon`, se fizer sentido) em `<application>`.
- **O mesmo ícone aparece na lista de apps do Android Auto**, num círculo sobre fundo escuro e pequeno:
  precisa ser legível em tamanho pequeno e em fundo escuro. Confira no DHU.
- Se quiser, use o símbolo também na home do celular (cabeçalho), sem ocupar espaço útil.
- Entregue também o logotipo em SVG em `docs/logo.svg`, para uso no README.

## 5. Carro: só exibição, já pronto

**Não desenhe nem adicione nada na tela do carro: botões, menus, seletores, camadas, avisos.**
Ela é pequena (cerca de 7" no Uconnect do Argo) e o toque é impreciso. O estado atual, já testado:

- O site ocupa a tela toda e se **ajusta à largura** quando o Android Auto mostra um cartão lateral
  (layout dividido do Uconnect). Fundo preto fora do site.
- O toque no carro é repassado ao site, para tocar nos títulos e no player do próprio site.
- **Ao abrir, o CarWeb pausa a música** (Spotify, YouTube Music etc.).
- Há um **círculo pequeno e vazio no canto superior direito**. É uma exigência do Android Auto (o
  template precisa de uma ação), não dá para tirar. Não é trabalho de design.

Decidido e fora de escopo: dividir a tela com o Waze (ou é o Waze, ou o CarWeb, alternando),
travas de segurança em movimento, e qualquer controle na tela do carro.

## 6. O que não pode quebrar

Cada item abaixo existe por um motivo que custou tempo para descobrir. Todos ficam em arquivos que
você não precisa tocar; se precisar, pergunte antes.

- **DRM**: o `WebChromeClient` concede `RESOURCE_PROTECTED_MEDIA_ID`. Sem isso o vídeo não toca.
- **`WebViewClient` interno**: mantém a navegação dentro do app e bloqueia `intent://` e esquemas de app.
- **Cookies**: compartilhados entre o celular e o carro, com cookies de terceiros ativos. É o que faz
  o login do celular valer no carro.
- **Login automático**: `BrowserActivity` fecha ao sair de uma página de login (heurística por URL).
  Se mudar o fluxo, mantenha essa saída.
- **Tela do carro**: o `Presentation` sobre `VirtualDisplay`, o ajuste à área estável, a ação
  transparente e a pausa da música, em `CarWebService.kt`.
- **Comunicação celular para carro**: por SharedPreferences (`select`, `carCommand`). Não troque por
  outro mecanismo.

## 7. Como testar

1. Celular com depuração (adb wireless ou USB) e `scripts/install.sh`.
2. No celular: Android Auto > ⋮ > modo desenvolvedor (10 toques em "Versão") > **Fontes desconhecidas**
   > **Iniciar servidor da unidade principal**. Mantenha a tela ligada (o adb wireless cai ao bloquear).
3. No PC: `scripts/dhu.sh` (ou `scripts/dhu.sh wide` para o layout dividido). Abra o CarWeb na lista de apps.
4. No celular: faça login nos dois provedores, mande um ao carro, use Voltar e Recarregar, troque de provedor.
5. O teste final é no Uconnect real, pelo dono do projeto.

## 8. Entrega esperada

- Home do celular redesenhada: cartões com os três estados, controle remoto do carro, claro e escuro,
  alvos grandes.
- Logotipo e ícone adaptativo do app (seção 4b), conferidos no celular e na lista de apps do DHU.
- Nenhuma mudança na tela do carro nem nos arquivos da seção 6.
- Mudanças pequenas e focadas; sem dependências novas além do estritamente necessário (explique
  qualquer uma que adicionar, ex.: Material Components). Código simples, comentado só onde o motivo
  não é óbvio.
- Resumo curto do que mudou e o que ficou para depois.
