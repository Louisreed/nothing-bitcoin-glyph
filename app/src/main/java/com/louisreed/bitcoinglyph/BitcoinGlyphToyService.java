package com.louisreed.bitcoinglyph;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;

// Using the correct matrix SDK classes for Nothing Phone 3
import com.nothing.ketchum.Common;
import com.nothing.ketchum.GlyphException;
import com.nothing.ketchum.GlyphMatrixManager;
import com.nothing.ketchum.GlyphMatrixFrame;
import com.nothing.ketchum.GlyphMatrixObject;

public class BitcoinGlyphToyService extends Service {
    private static final String TAG = "BitcoinGlyphToyService";
    private static final int UPDATE_INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds
    private static final int MATRIX_SIZE = 25; // Phone 3 has 25x25 matrix

    private GlyphMatrixManager glyphMatrixManager;
    private Handler mainHandler;
    private Timer priceUpdateTimer;
    private boolean isServiceConnected = false;
    private double currentPrice = 0.0;
    private boolean showIcon = true; // Toggle between icon and price
    private boolean isPhone3 = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "*** BITCOIN GLYPH TOY SERVICE CREATED ***");
        
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Check for Nothing Phone 3
        isPhone3 = Common.is24111();
        
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
            
            // Register for Phone 3 matrix
            if (glyphMatrixManager.register()) {
                Log.i(TAG, "Successfully registered for Phone 3 matrix");
                
                try {
                    glyphMatrixManager.openSession();
                    Log.i(TAG, "Glyph matrix session opened successfully");
                } catch (GlyphException e) {
                    Log.e(TAG, "Error opening Glyph matrix session: " + e.getMessage(), e);
                }
            } else {
                Log.e(TAG, "Failed to register for Phone 3 matrix");
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
            // Create Bitcoin icon matrix pattern
            Log.i(TAG, "Creating Bitcoin icon matrix pattern");
            
            // Create Bitcoin icon pattern as boolean array
            boolean[][] bitcoinPattern = createBitcoinIconPattern();
            
            // Create GlyphMatrixObject with the pattern
            GlyphMatrixObject matrixObject = new GlyphMatrixObject(bitcoinPattern);
            
            // Create GlyphMatrixFrame with animation settings
            GlyphMatrixFrame.Builder builder = new GlyphMatrixFrame.Builder();
            builder.setGlyphMatrixObject(matrixObject);
            builder.setPeriod(1000); // 1 second on
            builder.setCycles(3);    // 3 cycles
            builder.setInterval(500); // 0.5 second interval
            
            GlyphMatrixFrame frame = builder.build();
            
            // Display the matrix frame
            glyphMatrixManager.displayMatrixFrame(frame);
            
            Log.i(TAG, "*** BITCOIN ICON MATRIX DISPLAYED SUCCESSFULLY ***");
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying Bitcoin icon: " + e.getMessage(), e);
        }
    }

    private void displayPrice() {
        Log.i(TAG, "*** DISPLAYING PRICE ***");
        
        if (!isServiceConnected || glyphMatrixManager == null) {
            Log.e(TAG, "Cannot display price - service not ready");
            return;
        }
        
        try {
            // Create price display matrix pattern
            Log.i(TAG, "Creating price display matrix pattern");
            
            // Create price pattern as boolean array
            boolean[][] pricePattern = createPricePattern();
            
            // Create GlyphMatrixObject with the pattern
            GlyphMatrixObject matrixObject = new GlyphMatrixObject(pricePattern);
            
            // Create GlyphMatrixFrame with animation settings
            GlyphMatrixFrame.Builder builder = new GlyphMatrixFrame.Builder();
            builder.setGlyphMatrixObject(matrixObject);
            builder.setPeriod(2000); // 2 seconds on
            builder.setCycles(1);    // Single cycle
            
            GlyphMatrixFrame frame = builder.build();
            
            // Display the matrix frame
            glyphMatrixManager.displayMatrixFrame(frame);
            
            Log.i(TAG, "*** PRICE MATRIX DISPLAYED SUCCESSFULLY ***");
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying price: " + e.getMessage(), e);
        }
    }

    private boolean[][] createBitcoinIconPattern() {
        // Create a 25x25 matrix with a Bitcoin "B" symbol
        boolean[][] pattern = new boolean[MATRIX_SIZE][MATRIX_SIZE];
        
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

    private boolean[][] createPricePattern() {
        // Create a 25x25 matrix with a price indicator pattern
        boolean[][] pattern = new boolean[MATRIX_SIZE][MATRIX_SIZE];
        
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
            } catch (GlyphException e) {
                Log.e(TAG, "Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
} 