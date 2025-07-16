#!/bin/bash

# Download Nothing GlyphMatrixSDK.aar from official repository
# This script helps automate the SDK download process

SDK_URL="https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit/raw/main/sdk/GlyphMatrixSDK.aar"
SDK_DIR="libs"
SDK_FILE="$SDK_DIR/GlyphMatrixSDK.aar"

echo "🚀 Downloading Nothing GlyphMatrixSDK.aar..."
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
        
        # Check if it's actually a JAR/AAR file
        if file "$SDK_FILE" | grep -q "Zip archive"; then
            echo "✅ File appears to be a valid AAR file"
        else
            echo "⚠️  Warning: File may not be a valid AAR file"
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