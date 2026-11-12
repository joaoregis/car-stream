#!/usr/bin/env bash
# Abre o Desktop Head Unit (simulador do carro) ligado ao celular via adb.
# Antes: celular no adb e Android Auto > ⋮ > Iniciar servidor da unidade principal. Manter a tela ligada.
# Uso: scripts/dhu.sh [argo|wide|720p|1080p]
#   argo  = 800x480, como o Uconnect 7" do Fiat Argo (padrão)
#   wide  = 1280x720 com margem: tela larga, onde o Android Auto costuma dividir a tela (Waze + cartões)
#   720p / 1080p = telas maiores, para testar o layout dividido
SDK="${ANDROID_HOME:-E:/Android/sdk}"
DHU="$SDK/extras/google/auto"
case "${1:-argo}" in
  argo)  CFG=default.ini ;;
  wide)  CFG=default_wide.ini ;;
  720p)  CFG=default_720p.ini ;;
  1080p) CFG=default_1080p.ini ;;
  *) echo "uso: $0 [argo|wide|720p|1080p]"; exit 1 ;;
esac
"$SDK/platform-tools/adb.exe" forward tcp:5277 tcp:5277
cd "$DHU" && tail -f /dev/null | ./desktop-head-unit.exe -c "config/$CFG"   # stdin aberto: sem isso o DHU sai na hora
