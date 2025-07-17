package com.louisreed.bitcoinglyph;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;

// Using the correct GlyphMatrix SDK classes for Nothing Phone 3
import com.nothing.ketchum.Common;
import com.nothing.ketchum.GlyphException;
import com.nothing.glyph.matrix.GlyphMatrixManager;
import com.nothing.glyph.matrix.GlyphMatrixFrame;
import com.nothing.glyph.matrix.GlyphMatrixObject;

public class BitcoinGlyphToyService extends Service {
    private static final String TAG = "BitcoinGlyphToyService";
    private static final int UPDATE_INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds

    
    private GlyphMatrixManager glyphMatrixManager;
    private Handler mainHandler;
    private Timer priceUpdateTimer;
    private boolean isServiceConnected = false;
    private double currentPrice = 0.0;
    private boolean showIcon = true; // Toggle between icon and price
    private boolean isPhone3 = false;
    private String deviceType = "Unknown";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "*** BITCOIN GLYPH TOY SERVICE CREATED ***");
        
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Check for Nothing Phone 3 by model instead of Common.is24111()
        isPhone3 = android.os.Build.MODEL.equals("A024") || 
                  android.os.Build.DEVICE.equals("Metroid") ||
                  android.os.Build.PRODUCT.contains("Metroid") ||
                  Common.is24111();
        
        Log.i(TAG, "Device info:");
        Log.i(TAG, "  Device model: " + android.os.Build.MODEL);
        Log.i(TAG, "  Device device: " + android.os.Build.DEVICE);
        Log.i(TAG, "  Device product: " + android.os.Build.PRODUCT);
        Log.i(TAG, "  Detected as Nothing Phone 3: " + isPhone3);
        
        if (!isPhone3) {
            Log.e(TAG, "*** UNSUPPORTED DEVICE - This app only supports Nothing Phone 3 ***");
            return;
        }
        
        // Initialize Glyph Matrix Manager
        Log.i(TAG, "Initializing GlyphMatrixManager...");
        glyphMatrixManager = GlyphMatrixManager.getInstance(getApplicationContext());
        
        if (glyphMatrixManager != null) {
            Log.i(TAG, "GlyphMatrixManager initialized successfully");
            glyphMatrixManager.init(mCallback);
        } else {
            Log.e(TAG, "Failed to initialize GlyphMatrixManager");
        }
        
        // Start price update timer
        startPriceUpdateTimer();
    }

    private GlyphMatrixManager.Callback mCallback = new GlyphMatrixManager.Callback() {
        @Override
        public void onServiceConnected(android.content.ComponentName componentName) {
            Log.i(TAG, "*** GLYPH MATRIX SERVICE CONNECTED ***");
            isServiceConnected = true;
            
            // No device registration needed for matrix SDK
            Log.i(TAG, "GlyphMatrix service ready - no registration required");
            
            // Open session directly
            try {
                glyphMatrixManager.openSession();
                Log.i(TAG, "GlyphMatrix session opened successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error opening GlyphMatrix session: " + e.getMessage(), e);
            }
        }

        @Override
        public void onServiceDisconnected(android.content.ComponentName componentName) {
            Log.i(TAG, "*** GLYPH MATRIX SERVICE DISCONNECTED ***");
            isServiceConnected = false;
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "*** SERVICE STARTED ***");
        
        if (!isPhone3) {
            Log.e(TAG, "Service started on unsupported device");
            return START_NOT_STICKY;
        }
        
        if (intent != null) {
            String action = intent.getAction();
            Log.i(TAG, "Service started with action: " + action);
            
            if ("com.nothing.glyph.TOY".equals(action)) {
                // Handle glyph button press
                handleGlyphButtonPress();
            } else if ("com.nothing.glyph.LONG_PRESS".equals(action)) {
                // Handle long press - toggle display mode
                toggleDisplayMode();
            }
        }
        
        return START_STICKY;
    }

    private void handleGlyphButtonPress() {
        Log.i(TAG, "Glyph button pressed");
        
        if (showIcon) {
            displayBitcoinIcon();
        } else {
            displayPrice();
        }
    }

    private void toggleDisplayMode() {
        Log.i(TAG, "Toggle display mode - Long press detected");
        showIcon = !showIcon;
        
        if (showIcon) {
            Log.i(TAG, "Switching to Bitcoin icon mode");
            displayBitcoinIcon();
        } else {
            Log.i(TAG, "Switching to price display mode");
            displayPrice();
        }
    }

    private void displayBitcoinIcon() {
        Log.i(TAG, "*** DISPLAYING BITCOIN ICON ***");
        
        if (!isServiceConnected || glyphMatrixManager == null) {
            Log.e(TAG, "Cannot display icon - service not ready");
            return;
        }
        
        try {
            // Create Bitcoin icon pattern for Nothing Phone 3 25x25 matrix
            Log.i(TAG, "Creating Bitcoin icon matrix pattern");
            
            // Create a Bitcoin "B" symbol pattern on the 25x25 matrix
            boolean[][] bitcoinPattern = createBitcoinIconPattern();
            
            // Create GlyphMatrixObject with the pattern
            GlyphMatrixObject matrixObject = new GlyphMatrixObject(bitcoinPattern);
            
            // Create GlyphMatrixFrame with the object
            GlyphMatrixFrame frame = new GlyphMatrixFrame(matrixObject);
            
            // Set display period and cycles
            frame.setPeriod(1000); // 1 second on
            frame.setCycles(3); // 3 cycles
            frame.setInterval(500); // 0.5 second interval
            
            // Send frame to display
            glyphMatrixManager.setMatrixFrame(frame);
            Log.i(TAG, "*** BITCOIN ICON MATRIX FRAME SENT SUCCESSFULLY ***");
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying Bitcoin icon: " + e.getMessage(), e);
        }
    }

    private boolean[][] createBitcoinIconPattern() {
        // Create a 25x25 matrix with a Bitcoin "B" symbol
        boolean[][] pattern = new boolean[25][25];
        
        // Simple Bitcoin "B" pattern in the center
        // This creates a stylized "B" shape
        for (int row = 5; row < 20; row++) {
            // Left vertical line
            pattern[row][8] = true;
            pattern[row][9] = true;
            
            // Top horizontal line
            if (row == 5 || row == 6) {
                for (int col = 8; col < 16; col++) {
                    pattern[row][col] = true;
                }
            }
            
            // Middle horizontal line
            if (row == 11 || row == 12) {
                for (int col = 8; col < 15; col++) {
                    pattern[row][col] = true;
                }
            }
            
            // Bottom horizontal line
            if (row == 18 || row == 19) {
                for (int col = 8; col < 17; col++) {
                    pattern[row][col] = true;
                }
            }
            
            // Right vertical segments
            if (row >= 7 && row <= 10) {
                pattern[row][15] = true;
                pattern[row][16] = true;
            }
            if (row >= 13 && row <= 17) {
                pattern[row][16] = true;
                pattern[row][17] = true;
            }
        }
        
        return pattern;
    }

    private void displayPrice() {
        Log.i(TAG, "*** DISPLAYING PRICE ***");
        
        if (!isServiceConnected || glyphMatrixManager == null) {
            Log.e(TAG, "Cannot display price - service not ready");
            return;
        }
        
        try {
            // Create price display pattern for Nothing Phone 3 25x25 matrix
            Log.i(TAG, "Creating price display matrix pattern");
            
            // Create a simple price indicator pattern
            boolean[][] pricePattern = createPricePattern();
            
            // Create GlyphMatrixObject with the pattern
            GlyphMatrixObject matrixObject = new GlyphMatrixObject(pricePattern);
            
            // Create GlyphMatrixFrame with the object
            GlyphMatrixFrame frame = new GlyphMatrixFrame(matrixObject);
            
            // Set display period and cycles
            frame.setPeriod(2000); // 2 seconds on
            frame.setCycles(1); // Single cycle
            
            // Send frame to display
            glyphMatrixManager.setMatrixFrame(frame);
            Log.i(TAG, "*** PRICE MATRIX FRAME SENT SUCCESSFULLY ***");
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying price: " + e.getMessage(), e);
        }
    }

    private boolean[][] createPricePattern() {
        // Create a 25x25 matrix with a price indicator pattern
        boolean[][] pattern = new boolean[25][25];
        
        // Create a dollar sign "$" pattern
        for (int row = 3; row < 22; row++) {
            // Vertical line for dollar sign
            if (row >= 6 && row <= 18) {
                pattern[row][12] = true;
                pattern[row][13] = true;
            }
            
            // Top curve of S
            if (row >= 3 && row <= 8) {
                for (int col = 8; col < 17; col++) {
                    if (row == 3 || row == 8 || col == 8 || col == 16) {
                        pattern[row][col] = true;
                    }
                }
            }
            
            // Middle line of S
            if (row == 11 || row == 12) {
                for (int col = 8; col < 17; col++) {
                    pattern[row][col] = true;
                }
            }
            
            // Bottom curve of S
            if (row >= 16 && row <= 21) {
                for (int col = 8; col < 17; col++) {
                    if (row == 16 || row == 21 || col == 8 || col == 16) {
                        pattern[row][col] = true;
                    }
                }
            }
        }
        
        return pattern;
    }

    private void startPriceUpdateTimer() {
        priceUpdateTimer = new Timer();
        priceUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchBitcoinPrice();
            }
        }, 0, UPDATE_INTERVAL);
    }

    private void fetchBitcoinPrice() {
        // Simulate Bitcoin price fetching
        // In production, this would make an actual API call
        currentPrice = 45000 + (Math.random() * 10000); // Random price between 45k-55k
        Log.i(TAG, "Bitcoin price updated: $" + String.format("%.2f", currentPrice));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "*** SERVICE DESTROYED ***");
        
        if (priceUpdateTimer != null) {
            priceUpdateTimer.cancel();
        }
        
        if (glyphMatrixManager != null && isServiceConnected) {
            try {
                glyphMatrixManager.closeSession();
                glyphMatrixManager.unInit();
            } catch (Exception e) {
                Log.e(TAG, "Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
} 