# Getting the Nothing Glyph SDK

## Official Repository

The Nothing Glyph SDK is available from the official Nothing Developer Programme repository:

**Repository:** https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit

## Quick Setup

### Option 1: Automated Script (Recommended)

```bash
./download_sdk.sh
```

### Option 2: Manual Download

1. Go to https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit
2. Download the `GlyphMatrixSDK.aar` file
3. Place it in the `libs/` directory of your project

### Option 3: Direct Download Command

```bash
curl -L -o libs/GlyphMatrixSDK.aar https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit/raw/main/GlyphMatrixSDK.aar
```

## SDK Details

- **File Name:** `GlyphMatrixSDK.aar`
- **Type:** Android Archive (AAR)
- **Size:** ~58KB
- **Source:** Official Nothing Developer Programme

## API Key Configuration

### For Development/Testing

Add to `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.nothing.glyph.api_key"
    android:value="test" />
```

Enable debug mode:

```bash
adb shell settings put global nt_glyph_interface_debug_enable 1
```

**Note:** Debug mode automatically disables after 48 hours.

### For Production

1. Apply for a real API key at: https://intl.nothing.tech/pages/glyph-developer-kit
2. Replace `"test"` with your actual API key in `AndroidManifest.xml`

## Documentation

The official repository includes comprehensive documentation:

- Getting Started
- Developing a Glyph Toy Service
- API Reference
- Example projects

## Support

For technical support, contact: GDKsupport@nothing.tech

## Build Instructions

After downloading the SDK:

```bash
./gradlew build
```

The project is configured to use the official `GlyphMatrixSDK.aar` file automatically.
