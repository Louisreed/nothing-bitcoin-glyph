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
    private Timer priceUpdateTimer;
    private Handler mainHandler;
    private boolean isShowingIcon = true;
    private boolean isInitialized = false;
    
    // Price variables
    private double currentPrice = 0.0;
    private long lastUpdateTime = 0;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Bitcoin Glyph Toy Service started");
        
        if (intent != null && "com.nothing.glyph.TOY".equals(intent.getAction())) {
            handleToyAction(intent);
        }
        
        return START_STICKY;
    }

    private void handleToyAction(Intent intent) {
        Log.d(TAG, "Handling toy action");
        
        if (!isInitialized) {
            initializeGlyphManager();
        }
        
        if (isInitialized) {
            if (intent.hasExtra("toggle") && intent.getBooleanExtra("toggle", false)) {
                toggleDisplay();
            } else {
                displayBitcoinIcon();
            }
        }
    }

    private void initializeGlyphManager() {
        try {
            glyphManager = GlyphManager.getInstance(getApplicationContext());
            glyphManager.init(new GlyphManager.Callback() {
                @Override
                public void onServiceConnected(android.content.ComponentName componentName) {
                    Log.d(TAG, "Glyph service connected");
                    try {
                        // Register for Phone 3 (24111)
                        if (Common.is24111()) {
                            glyphManager.register(Glyph.DEVICE_24111);
                        } else {
                            glyphManager.register();
                        }
                        glyphManager.openSession();
                        isInitialized = true;
                        
                        // Start price updates
                        startPriceUpdates();
                        
                        // Show initial display
                        displayBitcoinIcon();
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Error initializing glyph manager: " + e.getMessage(), e);
                    }
                }

                @Override
                public void onServiceDisconnected(android.content.ComponentName componentName) {
                    Log.d(TAG, "Glyph service disconnected");
                    isInitialized = false;
                    try {
                        glyphManager.closeSession();
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing session: " + e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating glyph manager: " + e.getMessage(), e);
        }
    }

    private void startPriceUpdates() {
        mainHandler = new Handler(Looper.getMainLooper());
        priceUpdateTimer = new Timer();
        priceUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateBitcoinPrice();
            }
        }, 0, UPDATE_INTERVAL);
    }

    private void updateBitcoinPrice() {
        // TODO: Implement actual Bitcoin price fetching
        // For now, simulate price updates
        currentPrice = 50000 + (Math.random() * 10000); // Simulate price between $50k-$60k
        lastUpdateTime = System.currentTimeMillis();
        
        Log.d(TAG, "Updated Bitcoin price: $" + String.format("%.2f", currentPrice));
        
        // If we're showing price, update the display
        if (!isShowingIcon && isInitialized) {
            mainHandler.post(this::displayBitcoinPrice);
        }
    }

    private void toggleDisplay() {
        isShowingIcon = !isShowingIcon;
        if (isShowingIcon) {
            displayBitcoinIcon();
        } else {
            displayBitcoinPrice();
        }
    }

    private void displayBitcoinIcon() {
        Log.d(TAG, "Displaying Bitcoin icon");
        
        try {
            // Create a frame builder for Phone 3
            GlyphFrame.Builder builder = glyphManager.getGlyphFrameBuilder();
            
            // For Phone 3, we need to create a pattern that works with the matrix display
            // Using the channel indices from the documentation
            // Phone 3a channels: A1-A11 (20-30), B1-B5 (31-35), C1-C20 (0-19)
            
            // Create a Bitcoin "B" pattern using available channels
            // Top horizontal line - A channels
            builder.buildChannel(25).buildChannel(26).buildChannel(27);
            
            // Middle horizontal line - B channels  
            builder.buildChannel(32).buildChannel(33).buildChannel(34);
            
            // Bottom horizontal line and verticals - C channels
            builder.buildChannel(2).buildChannel(3).buildChannel(4)
                   .buildChannel(7).buildChannel(12).buildChannel(17)
                   .buildChannel(10).buildChannel(11).buildChannel(14);
            
            // Set animation properties
            builder.buildPeriod(1000); // 1 second on
            builder.buildCycles(3);    // 3 cycles
            builder.buildInterval(500); // 0.5 second interval
            
            GlyphFrame frame = builder.build();
            
            // Animate the frame
            glyphManager.animate(frame);
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying Bitcoin icon: " + e.getMessage(), e);
        }
    }

    private void displayBitcoinPrice() {
        Log.d(TAG, "Displaying Bitcoin price: $" + String.format("%.2f", currentPrice));
        
        try {
            // Create a frame builder for price display
            GlyphFrame.Builder builder = glyphManager.getGlyphFrameBuilder();
            
            // Use B channels for price indication
            builder.buildChannel(31).buildChannel(32).buildChannel(33)
                   .buildChannel(34).buildChannel(35);
            
            // Set animation properties for price display
            builder.buildPeriod(2000); // 2 seconds on
            builder.buildCycles(1);    // Single cycle
            
            GlyphFrame frame = builder.build();
            
            // Toggle the frame to show price
            glyphManager.toggle(frame);
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying price: " + e.getMessage(), e);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Bitcoin Glyph Toy Service destroyed");
        
        if (priceUpdateTimer != null) {
            priceUpdateTimer.cancel();
        }
        
        if (glyphManager != null && isInitialized) {
            try {
                glyphManager.turnOff();
                glyphManager.closeSession();
                glyphManager.unInit();
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning up glyph manager: " + e.getMessage(), e);
            }
        }
        
        super.onDestroy();
    }
} 