# Bitcoin Glyph Toy for Nothing Phone 3

A Bitcoin price tracker toy for Nothing phones that displays live Bitcoin prices and a Bitcoin icon on the phone's Glyph interface.

## Features

- 🪙 **Live Bitcoin Price Tracking**: Fetches current Bitcoin prices every 5 minutes
- 📱 **Glyph Interface Integration**: Displays information on Nothing phone's iconic Glyph interface
- 🔄 **Toggle Display**: Long press to switch between Bitcoin icon and price display
- 🎨 **Bitcoin-themed UI**: Orange Bitcoin colors and modern design
- 🚀 **Automatic Updates**: Background service handles price updates seamlessly

## Requirements

- Nothing Phone (Phone 1, Phone 2, or Phone 2a)
- Android 14 (API level 34) or higher
- Internet connection for price updates
- **GlyphMatrixSDK.aar** file from Nothing's developer program

## Installation

### Option 1: Download from Releases

1. Go to the [Releases](https://github.com/louisreed/nothing-bitcoin-glyph/releases) page
2. Download the latest `bitcoin-glyph-toy-debug.apk`
3. Install the APK on your Nothing phone
4. Enable the glyph toy in Settings

### Option 2: Build from Source

1. Clone this repository
2. Obtain the `GlyphMatrixSDK.aar` file from Nothing's developer program
3. Place the AAR file in the `libs/` directory
4. Open the project in Android Studio
5. Build and install the APK

## Setup Instructions

1. **Install the App**: Install the APK on your Nothing phone
2. **Enable Glyph Toy**:
   - Go to Settings > Glyph Interface > Glyph toys
   - Find "Bitcoin Tracker" and enable it
3. **Use the Toy**:
   - Press the Glyph button on the back of your phone to cycle through toys
   - When you reach the Bitcoin toy, you'll see the Bitcoin icon
   - Long press the Glyph button to toggle between icon and price display
4. **Automatic Updates**: The toy will automatically fetch Bitcoin prices every 5 minutes

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
├── libs/GlyphMatrixSDK.aar        # Nothing Glyph SDK (required)
├── build.gradle                    # Root build configuration
└── README.md                       # This file
```

### Key Components

#### BitcoinGlyphToyService

- Extends Android Service to run in background
- Implements Nothing's Glyph toy interface
- Handles Bitcoin price fetching and display logic
- Manages toggle between icon and price display

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

### Getting the Nothing Glyph SDK

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
4. Test on a Nothing phone
5. Submit a pull request

## Technical Details

- **Target SDK**: Android 14 (API 34)
- **Minimum SDK**: Android 14 (API 34) - Nothing phones only
- **Build Tool**: Gradle 8.1.4
- **Language**: Java
- **Architecture**: Service-based background processing with Glyph interface integration

## Permissions

The app requires the following permissions:

- `INTERNET`: For fetching Bitcoin prices
- `ACCESS_NETWORK_STATE`: For checking network connectivity
- `com.nothing.ketchum.permission.ENABLE`: For accessing Nothing's Glyph interface

## Known Issues

- The actual Bitcoin price API integration needs to be implemented
- Glyph Matrix SDK integration requires the actual SDK file
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
