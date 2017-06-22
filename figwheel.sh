#!/bin/sh

re-natal use-android-device avd
re-natal use-figwheel
lein figwheel android 
