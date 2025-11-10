#!/bin/sh

# cp (aosp source)/out/soong/.intermediates/frameworks/base/framework/android_common/repackaged-jarjar/turbine/framework.jar .

mkdir -p out
javac -source 1.8 -target 1.8 -cp framework.jar Clip.java -d out
~/android-sdk/build-tools/36.0.0/d8 out/Clip.class --output out/clip.jar

# adb push clip.jar /data/local/tmp

# Run on adb shell:
# ANDROID_ROOT=/system ANDROID_DATA=/data CLASSPATH=/data/local/tmp/clip.jar app_process /system/bin Clip
