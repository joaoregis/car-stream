# CarWeb

App Android **experimental, só para sideload** (não é para a Play Store): mostra um site de
streaming (por padrão `play.max.com`) na tela do carro via Android Auto, usando um `WebView`.

> ⚠️ Projeto pessoal e não oficial. Não tem relação com Google, HBO/Max ou Fiat.
> Não contorna DRM. Se o serviço não entregar vídeo no `WebView`, a tela fica preta ou dá erro.
> Use vídeo **somente com o carro parado**.

## Como funciona

- `CarWebService` (Car App Library, categoria `NAVIGATION`) recebe a `Surface` da tela do carro.
- Um `VirtualDisplay` + `Presentation` hospeda o `WebView` e desenha nessa `Surface`.
- `MainActivity` abre o mesmo site no celular, para fazer login (cookies compartilhados).
- Toques e rolagem da tela do carro são repassados ao `WebView`.

## Provedores

HBO Max e Disney+ (testados no DHU). Para adicionar outro: uma linha em `Providers.kt`, e teste no
celular e no DHU. Netflix e YouTube foram tentados e não funcionaram (detalhes no `CLAUDE.md`).

## Como usar

1. No celular, abra o CarWeb, toque em **Entrar** no provedor e faça o login (a tela fecha sozinha).
2. Toque em **Enviar ao carro** no provedor desejado.
3. Conecte o celular ao carro e abra o **CarWeb** na lista de apps do Android Auto
   (ele se declara app de navegação, então pode estar junto dos mapas).

## Requisitos

- Celular Android 8+ com Android Auto.
- Android Auto > modo desenvolvedor (toque 10 vezes em "Versão") > menu ⋮ > Configurações do
  desenvolvedor > **Fontes desconhecidas**. Sem isso, o app não aparece no carro.
- Para compilar: JDK 17, Android SDK (platform 35) e Gradle (`gradlew`).

## Build e instalação

```bash
./gradlew installDebug          # celular conectado por adb (USB ou wireless)
```

O Gradle precisa usar o JDK 17: defina `org.gradle.java.home` no `gradle.properties` do seu
`GRADLE_USER_HOME`.

## Se o app não aparecer no carro

- Confirme "Fontes desconhecidas" (acima) e **reinicie o Android Auto**: feche-o em Configurações >
  Apps > Android Auto > Forçar parada, e reconecte o celular ao carro.
- Tente a lista completa de apps do Android Auto. O CarWeb pode aparecer como app de navegação.
- Se a tela do carro ficar preta ou o vídeo não abrir, confirme que o login funciona no celular.
- Sessão expirada: use **Refazer login** no provedor.
- Use vídeo **somente com o carro parado**.

## Testar sem o carro

No celular: Android Auto > ⋮ > **Iniciar servidor da unidade principal**. No PC, `scripts/dhu.sh`.
O adb wireless cai se o celular bloquear a tela; mantenha-a ligada durante o teste.

## Estado

Funciona no DHU (simulador do carro). Falta testar no carro real. Veja o `CLAUDE.md` para os marcos.

## Licença

MIT. Veja [LICENSE](LICENSE).
