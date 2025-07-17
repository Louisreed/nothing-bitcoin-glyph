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
import com.nothing.ketchum.GlyphMatrixFrame;
import com.nothing.ketchum.GlyphMatrixManager;
import com.nothing.ketchum.GlyphMatrixObject;
import com.nothing.ketchum.GlyphManager;

public class BitcoinGlyphToyService extends Service {
    private static final String TAG = "BitcoinGlyphToyService";
    private static final int UPDATE_INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds

    private GlyphManager glyphManager;
    private GlyphMatrixManager glyphMatrixManager;
    private Timer priceUpdateTimer;
    private Handler mainHandler;
    private boolean isShowingIcon = true;
    private boolean isInitialized = false;
    private boolean isPhone3 = false;
    
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
            // Check device type to determine API usage
            // Phone 3 (A024/Metroid) uses matrix API, others use channel API
            String model = android.os.Build.MODEL;
            String device = android.os.Build.DEVICE;
            
            Log.d(TAG, "Device model: " + model + ", device: " + device);
            
            // Phone 3 detection: model A024 or device Metroid
            if ("A024".equals(model) || "Metroid".equals(device)) {
                isPhone3 = true;
                Log.d(TAG, "Detected Nothing Phone 3 - using matrix API");
                initializeMatrixManager();
            } else {
                Log.d(TAG, "Detected older Nothing phone - using channel API");
                initializeChannelManager();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing glyph manager: " + e.getMessage(), e);
        }
    }

    private void initializeMatrixManager() {
        try {
            glyphMatrixManager = GlyphMatrixManager.getInstance(getApplicationContext());
            glyphMatrixManager.init(new GlyphMatrixManager.Callback() {
                @Override
                public void onServiceConnected(android.content.ComponentName componentName) {
                    Log.d(TAG, "Glyph Matrix service connected");
                    try {
                        glyphMatrixManager.register();
                        glyphMatrixManager.openSession();
                        isInitialized = true;
                        
                        // Start price updates
                        startPriceUpdates();
                        
                        // Show initial display
                        displayBitcoinIcon();
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Error initializing matrix manager: " + e.getMessage(), e);
                    }
                }

                @Override
                public void onServiceDisconnected(android.content.ComponentName componentName) {
                    Log.d(TAG, "Glyph Matrix service disconnected");
                    isInitialized = false;
                    try {
                        glyphMatrixManager.closeSession();
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing matrix session: " + e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating matrix manager: " + e.getMessage(), e);
        }
    }

    private void initializeChannelManager() {
        try {
            glyphManager = GlyphManager.getInstance(getApplicationContext());
            glyphManager.init(new GlyphManager.Callback() {
                @Override
                public void onServiceConnected(android.content.ComponentName componentName) {
                    Log.d(TAG, "Glyph service connected");
                    try {
                        // Use old channel-based API for other phones
                        if (Common.is20111()) {
                            glyphManager.register(Glyph.DEVICE_20111);
                        } else if (Common.is22111()) {
                            glyphManager.register(Glyph.DEVICE_22111);
                        } else if (Common.is23111()) {
                            glyphManager.register(Glyph.DEVICE_23111);
                        } else if (Common.is23113()) {
                            glyphManager.register(Glyph.DEVICE_23113);
                        } else if (Common.is24111()) {
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
                        Log.e(TAG, "Error initializing channel manager: " + e.getMessage(), e);
                    }
                }

                @Override
                public void onServiceDisconnected(android.content.ComponentName componentName) {
                    Log.d(TAG, "Glyph service disconnected");
                    isInitialized = false;
                    try {
                        glyphManager.closeSession();
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing channel session: " + e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating channel manager: " + e.getMessage(), e);
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
            if (isPhone3) {
                displayBitcoinIconMatrix();
            } else {
                displayBitcoinIconChannels();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying Bitcoin icon: " + e.getMessage(), e);
        }
    }

    private void displayBitcoinIconMatrix() {
        try {
            // Create a matrix object for Phone 3's 25x25 display
            GlyphMatrixObject.Builder matrixBuilder = new GlyphMatrixObject.Builder(getApplicationContext());
            
            // Create a Bitcoin "B" pattern using 25x25 matrix
            // This is a simplified pattern - you can create more complex ones
            boolean[][] matrix = new boolean[25][25];
            
            // Bitcoin "B" pattern
            // Vertical line (left side)
            for (int i = 5; i < 20; i++) {
                matrix[i][8] = true;
                matrix[i][9] = true;
            }
            
            // Top horizontal line
            for (int j = 8; j < 16; j++) {
                matrix[5][j] = true;
                matrix[6][j] = true;
            }
            
            // Middle horizontal line
            for (int j = 8; j < 15; j++) {
                matrix[12][j] = true;
                matrix[13][j] = true;
            }
            
            // Bottom horizontal line
            for (int j = 8; j < 16; j++) {
                matrix[18][j] = true;
                matrix[19][j] = true;
            }
            
            // Right vertical lines
            for (int i = 7; i < 12; i++) {
                matrix[i][15] = true;
                matrix[i][16] = true;
            }
            for (int i = 14; i < 19; i++) {
                matrix[i][15] = true;
                matrix[i][16] = true;
            }
            
            // Convert boolean matrix to GlyphMatrixObject
            GlyphMatrixObject matrixObject = matrixBuilder.build();
            
            // Create a frame with animation properties
            GlyphMatrixFrame.Builder frameBuilder = new GlyphMatrixFrame.Builder(getApplicationContext());
            frameBuilder.buildPeriod(1000); // 1 second on
            frameBuilder.buildCycles(3);    // 3 cycles
            frameBuilder.buildInterval(500); // 0.5 second interval
            
            GlyphMatrixFrame frame = frameBuilder.build();
            
            // Animate the frame
            glyphMatrixManager.animate(matrixObject, frame);
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying Bitcoin icon matrix: " + e.getMessage(), e);
        }
    }

    private void displayBitcoinIconChannels() {
        try {
            // Create a frame builder for older phones (channel-based)
            GlyphFrame.Builder builder = glyphManager.getGlyphFrameBuilder();
            
            // For Phone 3a channels: A1-A11 (20-30), B1-B5 (31-35), C1-C20 (0-19)
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
            Log.e(TAG, "Error displaying Bitcoin icon channels: " + e.getMessage(), e);
        }
    }

    private void displayBitcoinPrice() {
        Log.d(TAG, "Displaying Bitcoin price: $" + String.format("%.2f", currentPrice));
        
        try {
            if (isPhone3) {
                displayBitcoinPriceMatrix();
            } else {
                displayBitcoinPriceChannels();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying price: " + e.getMessage(), e);
        }
    }

    private void displayBitcoinPriceMatrix() {
        try {
            // Create a matrix object for price display
            GlyphMatrixObject.Builder matrixBuilder = new GlyphMatrixObject.Builder(getApplicationContext());
            
            // Create a simple price indicator pattern
            boolean[][] matrix = new boolean[25][25];
            
            // Simple price indicator - could be enhanced to show actual digits
            // For now, just show a pattern that indicates price mode
            for (int i = 10; i < 15; i++) {
                for (int j = 10; j < 15; j++) {
                    matrix[i][j] = true;
                }
            }
            
            GlyphMatrixObject matrixObject = matrixBuilder.build();
            
            // Create a frame with animation properties
            GlyphMatrixFrame.Builder frameBuilder = new GlyphMatrixFrame.Builder(getApplicationContext());
            frameBuilder.buildPeriod(2000); // 2 seconds on
            frameBuilder.buildCycles(1);    // Single cycle
            
            GlyphMatrixFrame frame = frameBuilder.build();
            
            // Toggle the frame to show price
            glyphMatrixManager.toggle(matrixObject, frame);
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying price matrix: " + e.getMessage(), e);
        }
    }

    private void displayBitcoinPriceChannels() {
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
            Log.e(TAG, "Error displaying price channels: " + e.getMessage(), e);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Bitcoin Glyph Toy Service destroyed");
        
        if (priceUpdateTimer != null) {
            priceUpdateTimer.cancel();
        }
        
        if (isPhone3 && glyphMatrixManager != null && isInitialized) {
            try {
                glyphMatrixManager.turnOff();
                glyphMatrixManager.closeSession();
                glyphMatrixManager.unInit();
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning up matrix manager: " + e.getMessage(), e);
            }
        } else if (glyphManager != null && isInitialized) {
            try {
                glyphManager.turnOff();
                glyphManager.closeSession();
                glyphManager.unInit();
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning up channel manager: " + e.getMessage(), e);
            }
        }
        
        super.onDestroy();
    }
} 