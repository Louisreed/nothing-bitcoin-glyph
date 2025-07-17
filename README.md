# Bitcoin Glyph Toy for Nothing Phone 3

A Bitcoin price tracker toy for the Nothing Phone 3 that displays live Bitcoin prices and a Bitcoin icon on the phone's Glyph Matrix interface.

## Features

- 🪙 **Live Bitcoin Price Tracking**: Fetches current Bitcoin prices every 5 minutes
- 📱 **Glyph Matrix Integration**: Displays information on Nothing Phone 3's iconic Glyph Matrix interface
- 🔄 **Toggle Display**: Long press to switch between Bitcoin icon and price display
- 🎨 **Bitcoin-themed UI**: Orange Bitcoin colors and modern design
- 🚀 **Automatic Updates**: Background service handles price updates seamlessly

## Requirements

- **Nothing Phone 3 ONLY** - This app is specifically designed for the Nothing Phone 3 with Glyph Matrix display
- Android 14 (API level 34) or higher
- Internet connection for price updates
- **GlyphMatrixSDK.aar** file from Nothing's developer program

⚠️ **Important**: This app does NOT work on Nothing Phone 1, Phone 2, Phone 2a, Phone 2a Plus, Phone 3a, or Phone 3a Pro. Only the Nothing Phone 3 has the Glyph Matrix display required for this app.

## Installation

### Option 1: Download from Releases

1. Go to the [Releases](https://github.com/louisreed/nothing-bitcoin-glyph/releases) page
2. Download the latest `bitcoin-glyph-toy-debug.apk`
3. Install the APK on your Nothing Phone 3
4. Enable the glyph toy in Settings

### Option 2: Build from Source

1. Clone this repository
2. Obtain the `GlyphMatrixSDK.aar` file from Nothing's developer program
3. Place the AAR file in the `libs/` directory
4. Open the project in Android Studio
5. Build and install the APK

## Setup Instructions

1. **Enable Debug Mode** (Required for development):

   ```bash
   adb shell settings put global nt_glyph_interface_debug_enable 1
   ```

2. **Install the App**: Install the APK on your Nothing Phone 3

3. **Enable Glyph Toy**:

   - Go to Settings > Glyph Interface > Glyph toys
   - Find "Bitcoin Tracker" and enable it

4. **Use the Toy**:

   - Press the Glyph button on the back of your phone to cycle through toys
   - When you reach the Bitcoin toy, you'll see the Bitcoin icon pattern
   - Long press the Glyph button to toggle between icon and price display

5. **Automatic Updates**: The toy will automatically fetch Bitcoin prices every 5 minutes

## Glyph Matrix Patterns

### Bitcoin Icon Mode (Default)

The Bitcoin icon uses the following Nothing Phone 3 Glyph Matrix channels:

- **A channels**: A1-A11 (camera strip)
- **B channels**: B1-B5 (top section)
- **C channels**: C1-C20 (main body)
- **Animation**: 3 cycles, 1 second on, 0.5 second interval

### Price Display Mode (Long Press)

The price display uses:

- **B channels**: B1-B5 (top section)
- **Animation**: Single cycle, 2 seconds on

## Development

### Project Structure

```
nothing-bitcoin-glyph/
├── .github/workflows/build.yml    # GitHub Actions CI/CD
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml     # App manifest with permissions
│   │   ├── java/com/louisreed/bitcoinglyph/
│   │   │   ├── MainActivity.java   # Main activity with setup instructions
│   │   │   └── BitcoinGlyphToyService.java  # Glyph toy service
│   │   └── res/                    # App resources
│   └── build.gradle                # App-level build configuration
├── libs/GlyphMatrixSDK.aar        # Nothing Glyph Matrix SDK (required)
├── build.gradle                    # Root build configuration
└── README.md                       # This file
```

### Key Components

#### BitcoinGlyphToyService

- Extends Android Service to run in background
- Implements Nothing's Glyph Matrix toy interface
- Handles Bitcoin price fetching and display logic
- Manages toggle between icon and price display
- **Phone 3 specific**: Uses device-specific LED patterns

#### MainActivity

- Simple setup activity with instructions
- Provides user guidance for enabling the glyph toy
- Bitcoin-themed UI with orange colors

### Building

1. **Prerequisites**:

   - Android Studio Arctic Fox or later
   - JDK 17
   - Android SDK API 34
   - Nothing GlyphMatrixSDK.aar file

2. **Build Commands**:

   ```bash
   # Debug build
   ./gradlew assembleDebug

   # Release build
   ./gradlew assembleRelease
   ```

3. **GitHub Actions**:
   - Automatically builds APK on push to main branch
   - Uploads debug APK as artifact
   - Creates releases on git tags

### Getting the Nothing Glyph Matrix SDK

To build this project, you need the `GlyphMatrixSDK.aar` file from Nothing's official repository:

**Quick Setup:**

```bash
./download_sdk.sh
```

**Manual Setup:**

1. Download from: https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit
2. Get the `GlyphMatrixSDK.aar` file
3. Place the AAR file in the `libs/` directory
4. The project will then build successfully

**API Key Configuration:**

- For development: Use `"test"` as API key and enable debug mode
- For production: Apply for real API key at https://intl.nothing.tech/pages/glyph-developer-kit

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test on a Nothing Phone 3
5. Submit a pull request

## Technical Details

- **Target SDK**: Android 14 (API 34)
- **Minimum SDK**: Android 14 (API 34) - Nothing Phone 3 only
- **Build Tool**: Gradle 8.1.4
- **Language**: Java
- **Architecture**: Service-based background processing with Glyph Matrix integration
- **Device Support**: Nothing Phone 3 (24111) exclusively

## Permissions

The app requires the following permissions:

- `INTERNET`: For fetching Bitcoin prices
- `ACCESS_NETWORK_STATE`: For checking network connectivity
- `com.nothing.ketchum.permission.ENABLE`: For accessing Nothing's Glyph Matrix interface

## Known Issues

- The actual Bitcoin price API integration needs to be implemented
- Price display formatting may need adjustment for different price ranges

## Future Enhancements

- [ ] Add price trend indicators (up/down arrows)
- [ ] Support for multiple cryptocurrencies
- [ ] Price alerts and notifications
- [ ] Historical price charts
- [ ] Customizable update intervals
- [ ] Different display modes (percentage change, market cap)

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Disclaimer

This is an unofficial project and is not affiliated with Nothing Technology Limited. Bitcoin prices are provided for informational purposes only and should not be considered as financial advice.

## Support

For issues and questions:

- Open an issue on GitHub
- Check the Nothing community forums
- Review the Nothing developer documentation

---

**Note**: This project requires the Nothing GlyphMatrixSDK.aar file which can be obtained from the official Nothing Developer Programme repository at https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit. Use the included `download_sdk.sh` script for easy setup.
