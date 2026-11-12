#!/usr/bin/env bash
# Compila e instala no celular (adb USB ou wireless já conectado).
export JAVA_HOME='E:\Android\jdk17' ANDROID_HOME='E:\Android\sdk'
cd "$(dirname "$0")/.." && ./gradlew.bat installDebug --console=plain
