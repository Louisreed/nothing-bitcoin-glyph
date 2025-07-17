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

// Using the correct package names from GlyphMatrixSDK.aar
import com.nothing.ketchum.Common;
import com.nothing.ketchum.GlyphException;
import com.nothing.ketchum.GlyphFrame;
import com.nothing.ketchum.GlyphManager;
import com.nothing.ketchum.Glyph;

public class BitcoinGlyphToyService extends Service {
    private static final String TAG = "BitcoinGlyphToyService";
    private static final int UPDATE_INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds

    
    private GlyphManager glyphManager;
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
        
        // Initialize Glyph Manager
        Log.i(TAG, "Initializing GlyphManager...");
        glyphManager = GlyphManager.getInstance(getApplicationContext());
        
        if (glyphManager != null) {
            Log.i(TAG, "GlyphManager initialized successfully");
            glyphManager.init(mCallback);
        } else {
            Log.e(TAG, "Failed to initialize GlyphManager");
        }
        
        // Start price update timer
        startPriceUpdateTimer();
    }

    private GlyphManager.Callback mCallback = new GlyphManager.Callback() {
        @Override
        public void onServiceConnected(android.content.ComponentName componentName) {
            Log.i(TAG, "*** GLYPH SERVICE CONNECTED ***");
            isServiceConnected = true;
            
            // Register with Nothing Phone 3
            try {
                boolean registrationSuccess = glyphManager.register(Glyph.DEVICE_24111);
                deviceType = "Nothing Phone 3 (24111)";
                Log.i(TAG, "Registration success: " + registrationSuccess);
                
                if (registrationSuccess) {
                    Log.i(TAG, "Opening glyph session...");
                    glyphManager.openSession();
                } else {
                    Log.e(TAG, "Failed to register device");
                }
            } catch (GlyphException e) {
                Log.e(TAG, "Registration error: " + e.getMessage(), e);
            }
        }

        @Override
        public void onServiceDisconnected(android.content.ComponentName componentName) {
            Log.i(TAG, "*** GLYPH SERVICE DISCONNECTED ***");
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
        
        if (!isServiceConnected || glyphManager == null) {
            Log.e(TAG, "Cannot display icon - service not ready");
            return;
        }
        
        try {
            // Create Bitcoin icon pattern for Nothing Phone 3
            Log.i(TAG, "Creating Bitcoin icon glyph pattern");
            
            // Create GlyphFrame using all channels for a Bitcoin icon pattern
            GlyphFrame.Builder builder = glyphManager.getGlyphFrameBuilder();
            
            // For Phone 3, create a pattern using all channels
            GlyphFrame frame = builder
                .buildChannelA()
                .buildChannelB() 
                .buildChannelC()
                .buildPeriod(1000)
                .buildCycles(3)
                .buildInterval(500)
                .build();
            
            // Send frame to display
            glyphManager.toggle(frame);
            Log.i(TAG, "*** BITCOIN ICON FRAME SENT SUCCESSFULLY ***");
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying Bitcoin icon: " + e.getMessage(), e);
        }
    }



    private void displayPrice() {
        Log.i(TAG, "*** DISPLAYING PRICE ***");
        
        if (!isServiceConnected || glyphManager == null) {
            Log.e(TAG, "Cannot display price - service not ready");
            return;
        }
        
        try {
            // Create price display pattern for Nothing Phone 3
            Log.i(TAG, "Creating price display glyph pattern");
            
            // Create GlyphFrame using B channels for price display
            GlyphFrame.Builder builder = glyphManager.getGlyphFrameBuilder();
            
            // For Phone 3, create a price pattern using B channels
            GlyphFrame frame = builder
                .buildChannelB()
                .buildPeriod(2000)
                .buildCycles(1)
                .build();
            
            // Send frame to display
            glyphManager.toggle(frame);
            Log.i(TAG, "*** PRICE FRAME SENT SUCCESSFULLY ***");
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying price: " + e.getMessage(), e);
        }
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
        
        if (glyphManager != null && isServiceConnected) {
            try {
                glyphManager.closeSession();
                glyphManager.unInit();
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