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

## Requisitos

- Celular Android 8+ com Android Auto.
- Android Auto > modo desenvolvedor > **Fontes desconhecidas**.
- JDK 17, Android SDK (platform 35) e Gradle.

## Build e instalação

```bash
./gradlew installDebug          # celular conectado por adb (USB ou wireless)
```

## Testar sem o carro

Use o Desktop Head Unit (DHU) do Android SDK: no celular, Android Auto > Servidor da unidade
principal; no PC, `adb forward tcp:5277 tcp:5277` e rode o `desktop-head-unit`.

## Estado

Compila (`assembleDebug` OK); ainda não testado em aparelho. Veja o `CLAUDE.md` para os marcos.

## Licença

MIT. Veja [LICENSE](LICENSE).
