## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- When graphify-out/graph.json exists, run `graphify query "<question>"` before your first grep, search or source-file read for a codebase question, and put this rule in every subagent prompt that explores code. Read raw files only for lines the graph or an earlier read already located. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
- A committed graphify-out/graph.json is rewritten whole on every build, so a large diff to it in a pull request is expected. Do not flag it as churn or suggest skipping the graph rebuild or its hooks.

## CarWeb (projeto)

App Android pessoal, só para sideload (nunca Play Store): mostra um site de streaming (play.max.com) na tela do carro (Fiat Argo, Uconnect, Android Auto) via WebView.

- **Como funciona:** `CarWebService` (Car App Library, categoria NAVIGATION) recebe uma `Surface` do carro; um `VirtualDisplay` + `Presentation` hospeda o WebView e desenha nela. `MainActivity` mostra o mesmo site no celular, para login (cookies compartilhados).
- **Pré-requisito no celular:** Android Auto > modo desenvolvedor > "Fontes desconhecidas".
- **Fora de escopo:** burlar DRM (tela preta em captura do app oficial), publicar na Play Store.
- **Build/instalação:** `gradlew installDebug`; celular por adb wireless (`adb pair` + `adb connect`).
- **Marcos (parar se falhar):** 1) sync do Gradle; 2) app instala; 3) Max toca no WebView do celular (se não tocar, abordagem inviável); 4) app aparece no Android Auto/DHU; 5) vídeo na tela do carro (usuário testa); 6) home de provedores no celular, botões e UI do carro (bloqueio em movimento descartado).
- **Estado:** marcos 1-5 OK no DHU: compila, instala via adb wireless, HBO Max toca no WebView (Widevine L3, precisa PROTECTED_MEDIA_ID) e aparece/reproduz na tela do DHU. Falta: teste no carro real (usuário), provedores, UI. Gradle precisa de `org.gradle.java.home` (JDK 17) em `$GRADLE_USER_HOME/gradle.properties`. Android Auto: reiniciar o servidor da unidade principal e o DHU (`scripts/dhu.sh`, stdin aberto) a cada sessão.
- **Regras de trabalho:** mudanças mínimas, sem abstrações extras. Sem trava de movimento (decisão do dono do projeto). UI/UX visual será feita pelo Fable 5.2: parar e avisar antes de mexer nela.
- **Provedores:** só HBO Max e Disney+ (testados, funcionam no DHU). Netflix: login ok, mas perdeu a sessão ao trocar de provedor no carro e depois bloqueou login temporariamente; não resolvido. YouTube: Google recusa login em WebView ("navegador não seguro"), mesmo sem o cabeçalho X-Requested-With; sem solução. Prime Video e Globoplay não testados. Adicionar um por vez em `Providers.kt` e testar no celular e no DHU.
- **DHU:** o adb wireless cai quando o celular bloqueia a tela (derruba o DHU: "Failed to read from transport"). Manter a tela ligada durante testes.
