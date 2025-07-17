package com.louisreed.bitcoinglyph;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;

// Using the correct Glyph SDK classes for Nothing Phone 3
import com.nothing.ketchum.Common;
import com.nothing.ketchum.Glyph;
import com.nothing.ketchum.GlyphException;
import com.nothing.ketchum.GlyphFrame;
import com.nothing.ketchum.GlyphManager;

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

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "*** BITCOIN GLYPH TOY SERVICE CREATED ***");
        
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Check for Nothing Phone 3 using Common.is24111()
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
            
            // Register for Phone 3 (24111)
            if (glyphManager.register(Glyph.DEVICE_24111)) {
                Log.i(TAG, "Successfully registered for Phone 3");
                
                try {
                    glyphManager.openSession();
                    Log.i(TAG, "Glyph session opened successfully");
                } catch (GlyphException e) {
                    Log.e(TAG, "Error opening Glyph session: " + e.getMessage(), e);
                }
            } else {
                Log.e(TAG, "Failed to register for Phone 3");
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
            // Phone 3 has: A1-A11 (camera), B1-B5 (top), C1-C20 (main body)
            Log.i(TAG, "Creating Bitcoin icon pattern");
            
            GlyphFrame.Builder builder = glyphManager.getGlyphFrameBuilder();
            
            // Create a Bitcoin pattern using available channels
            // Phone 3a/3a Pro: A1-A11 (indices 20-30), B1-B5 (indices 31-35), C1-C20 (indices 0-19)
            
            // Use A channels (camera strip) for top accent - indices 20, 24, 30
            builder.buildChannel(20).buildChannel(24).buildChannel(30);
            
            // Use B channels (top section) for middle accent - indices 31, 33, 35
            builder.buildChannel(31).buildChannel(33).buildChannel(35);
            
            // Use C channels (main body) for Bitcoin "B" pattern - indices 0-19
            builder.buildChannel(0).buildChannel(1).buildChannel(2)
                   .buildChannel(5).buildChannel(6).buildChannel(7)
                   .buildChannel(10).buildChannel(11).buildChannel(12)
                   .buildChannel(15).buildChannel(16).buildChannel(17);
            
            // Set animation parameters
            builder.buildPeriod(1000)  // 1 second on
                   .buildCycles(3)     // 3 cycles
                   .buildInterval(500); // 0.5 second interval
            
            GlyphFrame frame = builder.build();
            
            // Animate the frame
            glyphManager.animate(frame);
            Log.i(TAG, "*** BITCOIN ICON FRAME ANIMATED SUCCESSFULLY ***");
            
        } catch (GlyphException e) {
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
            Log.i(TAG, "Creating price display pattern");
            
            GlyphFrame.Builder builder = glyphManager.getGlyphFrameBuilder();
            
            // Use B channels (top section) for price indication - indices 31-35
            builder.buildChannel(31).buildChannel(32).buildChannel(33)
                   .buildChannel(34).buildChannel(35);
            
            // Set display parameters
            builder.buildPeriod(2000)  // 2 seconds on
                   .buildCycles(1);    // Single cycle
            
            GlyphFrame frame = builder.build();
            
            // Toggle the frame
            glyphManager.toggle(frame);
            Log.i(TAG, "*** PRICE FRAME TOGGLED SUCCESSFULLY ***");
            
        } catch (GlyphException e) {
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