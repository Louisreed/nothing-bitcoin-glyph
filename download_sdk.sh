#!/bin/bash

# Download Nothing Glyph SDK from official Nothing Developer Programme repository
# This script downloads the official GlyphMatrixSDK.aar file required for the Bitcoin Glyph Toy

echo "Downloading Nothing Glyph SDK from official repository..."

# Create libs directory if it doesn't exist
mkdir -p libs

# Download the official GlyphMatrixSDK.aar file
curl -L -o libs/GlyphMatrixSDK.aar https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit/raw/main/GlyphMatrixSDK.aar

# Check if download was successful
if [ -f "libs/GlyphMatrixSDK.aar" ]; then
    echo "✅ GlyphMatrixSDK.aar downloaded successfully!"
    echo "File size: $(ls -lh libs/GlyphMatrixSDK.aar | awk '{print $5}')"
    echo ""
    echo "The SDK is now ready for use. You can build the project with:"
    echo "  ./gradlew build"
else
    echo "❌ Failed to download GlyphMatrixSDK.aar"
    exit 1
fi 