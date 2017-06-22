#!/bin/sh

# npm install -g react-native

export ANDROID="~/Android/Sdk"

eval $ANDROID/tools/emulator -avd Nexus_5X_API_26 > /dev/null 2>&1  &
eval $ANDROID/platform-tools/adb wait-for-local-device
echo "Run app on android"
react-native run-android &
react-native start
