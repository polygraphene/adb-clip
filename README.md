# adb-clip

Clipboard access via adb shell. Supports Android 10-16.

# Usage
Download `clip.jar` and `clip` file from [releases](https://github.com/polygraphene/adb-clip/releases)
```
adb push clip.jar clip /data/local/tmp
adb shell chmod 755 /data/local/tmp/clip
# Get clipboard
adb shell /data/local/tmp/clip
# Set clipboard
adb shell /data/local/tmp/clip "Content here"
```

# Features

1. No app installation required
2. Small code and executable

# Notes

1. Screen has to be on and unlocked to access clipboard

# License
MIT
