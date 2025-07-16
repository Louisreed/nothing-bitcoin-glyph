#!/bin/bash

# Download Nothing GlyphMatrixSDK.aar from official repository
# This script helps automate the SDK download process

SDK_URL="https://raw.githubusercontent.com/Nothing-Developer-Programme/Glyph-Developer-Kit/main/sdk/KetchumSDK_Community_20250319.jar"
SDK_DIR="libs"
SDK_FILE="$SDK_DIR/KetchumSDK_Community_20250319.jar"

echo "🚀 Downloading Nothing Glyph SDK..."
echo "📍 From: $SDK_URL"
echo "📁 To: $SDK_FILE"
echo ""

# Create libs directory if it doesn't exist
mkdir -p "$SDK_DIR"

# Remove existing placeholder file
if [ -f "$SDK_FILE" ]; then
    echo "🗑️  Removing existing file..."
    rm "$SDK_FILE"
fi

# Download the SDK
echo "⬇️  Downloading SDK..."
if curl -L -o "$SDK_FILE" "$SDK_URL"; then
    echo "✅ Download successful!"
    echo "📦 SDK file saved to: $SDK_FILE"
    
    # Verify the file
    if [ -f "$SDK_FILE" ]; then
        file_size=$(stat -f%z "$SDK_FILE" 2>/dev/null || stat -c%s "$SDK_FILE" 2>/dev/null)
        echo "📊 File size: $file_size bytes"
        
        # Check if it's actually a JAR file
        if file "$SDK_FILE" | grep -q "Java archive"; then
            echo "✅ File appears to be a valid JAR file"
        else
            echo "⚠️  Warning: File may not be a valid JAR file"
        fi
    fi
else
    echo "❌ Download failed!"
    echo "Please try downloading manually from:"
    echo "https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit"
    exit 1
fi

echo ""
echo "📋 Next steps:"
echo "1. Get API key from Nothing Developer Program"
echo "2. Add API key to AndroidManifest.xml"
echo "3. Build your project!"
echo ""
echo "🔧 For testing without API key:"
echo "   adb shell settings put global nt_glyph_interface_debug_enable 1"
echo "   Use android:value=\"test\" in AndroidManifest.xml"
echo ""
echo "🌐 Apply for API key at:"
echo "   https://intl.nothing.tech/pages/glyph-developer-kit" 