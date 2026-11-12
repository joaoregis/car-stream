#!/usr/bin/env bash
# Abre o Desktop Head Unit (simulador do carro) ligado ao celular via adb.
# Antes: celular conectado no adb e Android Auto > Servidor da unidade principal ligado.
SDK="${ANDROID_HOME:-E:/Android/sdk}"
"$SDK/platform-tools/adb.exe" forward tcp:5277 tcp:5277
cd "$SDK/extras/google/auto" && ./desktop-head-unit.exe
