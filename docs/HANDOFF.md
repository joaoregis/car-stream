# Handoff: redesenho da interface do CarWeb (em andamento)

Contexto para continuar em outra sessão. Leia junto com `CLAUDE.md` (estado geral do projeto) e
`docs/BRIEFING-UI.md` (o pedido de design). Este arquivo cobre só o que foi feito e pensado no
redesenho, e o que falta.

## Estado em uma linha

O redesenho da home do celular, o logotipo e o ícone **estão escritos e compilam**
(`assembleDebug` OK), mas **ninguém olhou o resultado na tela ainda** e **nada foi commitado**.
Último commit: `6a7d7fa`. Branch: `main` (o dono pediu para ficar nela, sem branch nova).

## O que está no disco, sem commit

| Arquivo | O que é |
|---|---|
| `app/src/main/java/dev/carweb/MainActivity.kt` | Home nova, reescrita inteira |
| `app/src/main/java/dev/carweb/BrowserActivity.kt` | Aviso flutuante na tela de login |
| `app/src/main/AndroidManifest.xml` | `android:icon`/`roundIcon` e tema `Theme.CarWeb` na home |
| `app/src/main/res/drawable/ic_launcher_foreground.xml` | Símbolo do app (vetor 108dp) |
| `app/src/main/res/drawable/ic_launcher_background.xml` | Fundo âmbar em gradiente |
| `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` | Ícone adaptativo, com camada `monochrome` |
| `app/src/main/res/drawable/ic_back.xml`, `ic_reload.xml`, `ic_check.xml` | Ícones (Material, Apache 2.0) |
| `app/src/main/res/values/colors.xml`, `values-night/colors.xml` | Paleta clara e escura |
| `app/src/main/res/values/themes.xml`, `values-night/themes.xml` | `Theme.CarWeb` |
| `docs/logo.svg` | Logotipo com a palavra "CarWeb" |

Não toquei em `CarWebService.kt`, `Web.kt` nem na lógica de `Providers.kt`, como o briefing manda.

**Incerteza:** as duas últimas chamadas de ferramenta da sessão anterior voltaram sem resultado
(erro interno). Conferi depois: os dois arquivos foram gravados e o build está atualizado e passa.
**Não sei se o `installDebug` chegou a instalar no celular.** Rode `scripts/install.sh` de novo.

## Decisões de design e o porquê

**Direção: "painel do carro à noite".** Fundo quase preto azulado no escuro, papel quente no
claro, e um único acento **âmbar** (`#FFB020`), como luz de painel. Escolhi âmbar porque é legível
à noite e não se confunde com as cores de nenhum provedor (roxo do HBO, azul do Disney).

**Controle remoto ancorado embaixo, não em cima.** O painel "No carro" (nome do provedor atual,
Voltar e Recarregar) fica fixo na base da tela, ao alcance do polegar com o celular na mão ou no
suporte. A lista de provedores rola acima dele. Em paisagem, o painel vai para a lateral direita
(300dp), porque embaixo comeria a altura toda.

**Cartão de provedor em duas linhas.** Primeira linha: monograma, nome, estado, e "Refazer login"
discreto à direita quando logado. Segunda linha: o botão principal em largura total (52dp).
Tentei o botão na mesma linha do nome e não coube em 360dp de largura: "Enviar ao carro" espremia
o nome do provedor.

**Três estados, como pede o briefing:**
- Não logado: "Não conectado" e botão neutro "Entrar".
- Logado: "Conectado" e botão âmbar "Enviar ao carro". O âmbar cheio é reservado a essa ação.
- No carro agora: borda âmbar, "No carro agora" com um check, e **sem botão** (ação redundante).

**Sem Toast ao enviar.** O cartão muda de estado e o painel de baixo troca o nome, com uma
transição curta (`TransitionManager`, 160ms). Botões dão retorno tátil.

**Login que não terminou.** A home guarda qual login abriu (`pending`). Ao voltar, se o provedor
continua não logado, o cartão mostra "Login não concluído. Tente de novo." em cor de aviso.
Esse estado vive só em memória e se perde ao girar a tela, o que aceitei.

**Monograma em vez de logo de marca.** Inicial do provedor num círculo de cor própria do app,
escolhida pelo índice na lista (paleta `BADGES`). Não usei nenhuma marca de terceiro.

**"No carro" sem inventar dado.** `selected()` devolve o primeiro provedor por padrão, mesmo sem
nada enviado. Por isso a home só considera "no carro" se a preferência `provider` existe
(`prefs(this).contains("provider")`) **e** o provedor está logado. Sem isso, o painel diz "Nada
enviado" e os botões do controle ficam desativados (alpha 0.38). O app continua sem saber se o
carro está conectado, e a interface não afirma isso.

**Sem dependências novas.** Tudo em Android Views puras, montadas em código, com
`GradientDrawable` e `RippleDrawable`. Não adicionei Material Components nem AppCompat.

**Recuos do sistema feitos à mão na home.** Não usei `padForBars()` nela: em retrato o painel de
baixo precisa ir até a borda e absorver o recuo da barra de navegação, senão sobra uma faixa vazia
sob ele. A `BrowserActivity` continua com `padForBars()`, agora no `FrameLayout` raiz.

**Logotipo.** Um retângulo arredondado (a tela do carro) com um "play" dentro, sobre um arco (o
painel). Símbolo escuro `#0B0F14` em fundo âmbar: escolhi fundo claro e símbolo escuro para o ícone
se destacar na lista de apps do Android Auto, que é escura. A camada `monochrome` reusa o mesmo
vetor. O símbolo cabe na zona segura (27..81 de 108). A home reusa esse vetor no cabeçalho, num
quadrado âmbar de 40dp, ampliado 1,5x.

**Tela de login.** Um aviso em pílula no topo ("Entre na sua conta. Esta tela fecha sozinha ao
terminar.", ou a versão com o botão Voltar para provedores sem detecção), que some após 5s.
Mantive o Toast "Login concluído" que já existia.

## Já verificado (sessão seguinte)

- Instalado no celular. **Home em retrato, tema escuro: conferida por print** e corrigida:
  o logo do cabeçalho estava ampliado e cortado (tirei o `scale` 1,5x), o check de "No carro agora"
  estava grande (agora 16dp), e a barra de navegação de 3 botões tinha fundo preto próprio
  (desliguei `isNavigationBarContrastEnforced` e deixei a barra transparente: o painel passa por baixo).
- `docs/logo.svg`: conferido por cálculo, o símbolo fica centrado no quadrado (desvio de ~4px para baixo,
  o mesmo do ícone).

## O que falta verificar

1. **A home no celular**, em retrato e paisagem, no tema claro e no escuro. Conferir: o recuo do
   topo (barra de status e recorte da câmera do Galaxy S23), o painel de baixo colado na borda,
   e se o símbolo do cabeçalho fica do tamanho certo dentro do quadrado âmbar.
2. **O ícone** na gaveta de apps do celular e na **lista de apps do DHU**. O briefing pede as duas.
   Olhar também o ícone temático (Android 13+).
3. **O fluxo inteiro**: Entrar, voltar logado, Enviar ao carro, Voltar e Recarregar pelo painel,
   trocar de provedor, Refazer login, e sair do login sem concluir (mensagem de aviso).
4. **A pílula do login** não deve bloquear toques no site enquanto está visível. É um `TextView`
   não clicável sobre o `WebView`, então o toque deveria passar; não testei.
5. **`docs/logo.svg`**: abrir e conferir se o símbolo ficou centrado no quadrado. A transformação
   do grupo (`scale(2.18)` com translações) foi calculada de cabeça.

## Pontos de atenção no código

- `MainActivity.render()` chama `TransitionManager.beginDelayedTransition` na `decorView` a cada
  renderização, inclusive a primeira. Deve ser inofensivo; se a abertura piscar, pule na primeira.
- `themes.xml` usa `windowLightNavigationBar` (API 27) com `minSdk 26`. Compila; em API 26 o
  atributo é ignorado. O lint pode reclamar se alguém rodar `lint`.
- O botão "Refazer login" usa `shape(-1, 14)` como máscara do ripple (cor branca, só para a forma).
- Em `card()`, o cartão "no carro" não tem segunda linha, então fica mais baixo que os outros.
  É intencional, mas vale olhar se o salto de altura incomoda.

## Próximos passos

1. `scripts/install.sh`, abrir o app e percorrer a lista de verificação acima. Ajustar o que
   estiver feio ou quebrado.
2. Abrir o DHU (`scripts/dhu.sh argo`; exige o servidor da unidade principal ligado no celular e a
   tela desbloqueada) e conferir o ícone na lista de apps e se o carro continua funcionando.
3. Se o dono aprovar: pôr o logotipo no topo do `README.md`, atualizar o `CLAUDE.md` (a regra
   "parar e avisar antes de mexer na UI" já foi cumprida) e remover a seção de design pendente.
4. Commit e push **só quando o dono pedir**. Mensagens de commit terminam com a linha de
   coautoria indicada pela sessão.
5. Entregar ao dono um resumo curto do que mudou e do que ficou para depois, como pede a seção 8
   do briefing.

## Ambiente (resumo do que custou tempo)

- Sem Android Studio. SDK em `E:\Android\sdk`, JDK 17 em `E:\Android\jdk17`. Para compilar:
  `export JAVA_HOME='E:\Android\jdk17' ANDROID_HOME='E:\Android\sdk'` e `./gradlew.bat installDebug`.
  Use caminhos no estilo Windows nessas variáveis; os do Git Bash (`/e/...`) quebram o Gradle.
- `adb` em `/e/Android/sdk/platform-tools/adb.exe`. Celular: Galaxy S23, Android 16, por adb
  wireless. Tem Pasta Segura (usuário 150): use `--user 0` em `am start` e `pm`.
- O adb wireless cai quando o celular bloqueia a tela, e isso derruba o DHU.
- `am force-stop dev.carweb` (que eu uso ao reinstalar) pode derrubar sessões dos provedores.
- O DHU precisa de stdin aberto (o `scripts/dhu.sh` já cuida) e deve ser aberto **depois** de o
  servidor da unidade principal estar ligado no celular.
