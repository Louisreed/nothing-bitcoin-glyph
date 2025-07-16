# Instructions for getting Nothing Glyph SDK

1. Download from: https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit/tree/main/sdk
2. Get the KetchumSDK_Community_20250319.jar file
3. Apply for API key at: https://intl.nothing.tech/pages/glyph-developer-kit
4. Add API key to AndroidManifest.xml

For testing without API key:

- Enable debug mode: adb shell settings put global nt_glyph_interface_debug_enable 1
- Use test key in manifest: android:value="test"
