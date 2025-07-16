package com.louisreed.bitcoinglyph;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;

import com.nothing.ketchum.Common;
import com.nothing.ketchum.GlyphException;
import com.nothing.ketchum.GlyphFrame;
import com.nothing.ketchum.GlyphManager;
import com.nothing.ketchum.Glyph;

public class BitcoinGlyphToyService extends Service {
    private static final String TAG = "BitcoinGlyphToyService";
    private static final int UPDATE_INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds
    
    private GlyphManager mGlyphManager;
    private Timer priceUpdateTimer;
    private Handler mainHandler;
    private boolean isShowingPrice = false;
    private boolean isServiceConnected = false;
    private double currentPrice = 0.0;
    
    private GlyphManager.Callback mCallback = new GlyphManager.Callback() {
        @Override
        public void onServiceConnected(ComponentName componentName) {
            Log.i(TAG, "*** GLYPH SERVICE CONNECTED ***");
            Log.i(TAG, "Component: " + componentName);
            isServiceConnected = true;
            
            // Register with the appropriate device
            try {
                String deviceType = "Unknown";
                if (Common.is20111()) {
                    mGlyphManager.register(Glyph.DEVICE_20111);
                    deviceType = "Phone 1 (20111)";
                } else if (Common.is22111()) {
                    mGlyphManager.register(Glyph.DEVICE_22111);
                    deviceType = "Phone 2 (22111)";
                } else if (Common.is23111()) {
                    mGlyphManager.register(Glyph.DEVICE_23111);
                    deviceType = "Phone 2a (23111)";
                } else if (Common.is23113()) {
                    mGlyphManager.register(Glyph.DEVICE_23113);
                    deviceType = "Phone 2a Plus (23113)";
                } else {
                    Log.w(TAG, "Unknown device type - trying default registration");
                    mGlyphManager.register(Glyph.DEVICE_20111); // Try default
                    deviceType = "Default (20111)";
                }
                
                Log.i(TAG, "Registered device: " + deviceType);
                
                mGlyphManager.openSession();
                Log.i(TAG, "*** GLYPH SESSION OPENED SUCCESSFULLY ***");
                
                // Show initial Bitcoin icon
                displayBitcoinIcon();
                
            } catch (GlyphException e) {
                Log.e(TAG, "*** GLYPH INITIALIZATION FAILED: " + e.getMessage() + " ***");
                e.printStackTrace();
            }
        }
        
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.w(TAG, "*** GLYPH SERVICE DISCONNECTED ***");
            Log.w(TAG, "Component: " + componentName);
            isServiceConnected = false;
            try {
                if (mGlyphManager != null) {
                    mGlyphManager.closeSession();
                    Log.i(TAG, "Glyph session closed");
                }
            } catch (GlyphException e) {
                Log.e(TAG, "Failed to close glyph session: " + e.getMessage());
            }
        }
    };
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "*** BITCOIN GLYPH TOY SERVICE CREATED ***");
        
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize Glyph Manager
        Log.i(TAG, "Initializing GlyphManager...");
        mGlyphManager = GlyphManager.getInstance(getApplicationContext());
        mGlyphManager.init(mCallback);
        Log.i(TAG, "GlyphManager initialization started");
        
        startPriceUpdates();
        
        // Log service creation to help with debugging
        Log.i(TAG, "*** SERVICE INITIALIZATION COMPLETE ***");
        Log.i(TAG, "Service package: " + getPackageName());
        Log.i(TAG, "Service class: " + getClass().getName());
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "=== SERVICE STARTED ===");
        Log.i(TAG, "Intent: " + (intent != null ? intent.toString() : "null"));
        Log.i(TAG, "Action: " + (intent != null ? intent.getAction() : "null"));
        Log.i(TAG, "Flags: " + flags + ", StartId: " + startId);
        
        if (intent != null && intent.getExtras() != null) {
            Log.i(TAG, "Intent extras: " + intent.getExtras().toString());
        }
        
        // Handle glyph toy specific intents
        if (intent != null) {
            String action = intent.getAction();
            if ("com.nothing.glyph.TOY".equals(action)) {
                Log.i(TAG, "*** GLYPH TOY ACTIVATION ***");
                handleGlyphToyActivation();
            } else if ("com.nothing.glyph.TOY_LONGPRESS".equals(action)) {
                Log.i(TAG, "*** GLYPH TOY LONG PRESS ***");
                toggleDisplay();
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action) ||
                       "android.intent.action.MY_PACKAGE_REPLACED".equals(action) ||
                       "android.intent.action.PACKAGE_REPLACED".equals(action)) {
                Log.i(TAG, "System startup or package replaced - registering service");
            } else {
                Log.i(TAG, "*** UNKNOWN ACTION: " + action + " - trying activation ***");
                handleGlyphToyActivation();
            }
        } else {
            Log.i(TAG, "*** NO INTENT - trying activation ***");
            handleGlyphToyActivation();
        }
        
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        
        if (priceUpdateTimer != null) {
            priceUpdateTimer.cancel();
        }
        
        try {
            if (mGlyphManager != null && isServiceConnected) {
                mGlyphManager.turnOff();
                mGlyphManager.closeSession();
            }
        } catch (GlyphException e) {
            Log.e(TAG, "Failed to cleanup glyph: " + e.getMessage());
        }
        
        if (mGlyphManager != null) {
            mGlyphManager.unInit();
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service bind requested");
        return null;
    }
    
    private void handleGlyphToyActivation() {
        Log.i(TAG, "*** HANDLING GLYPH TOY ACTIVATION ***");
        Log.i(TAG, "Service connected: " + isServiceConnected);
        Log.i(TAG, "GlyphManager exists: " + (mGlyphManager != null));
        
        if (isServiceConnected) {
            Log.i(TAG, "Service connected - displaying Bitcoin icon");
            displayBitcoinIcon();
        } else {
            Log.w(TAG, "*** GLYPH SERVICE NOT CONNECTED - INITIALIZING ***");
            // Try to initialize the glyph manager if not already done
            if (mGlyphManager == null) {
                Log.i(TAG, "Creating new GlyphManager instance");
                mGlyphManager = GlyphManager.getInstance(getApplicationContext());
                mGlyphManager.init(mCallback);
            } else {
                Log.i(TAG, "GlyphManager exists but not connected - reinitializing");
                mGlyphManager.init(mCallback);
            }
        }
    }
    
    private void toggleDisplay() {
        Log.i(TAG, "*** TOGGLING DISPLAY MODE ***");
        Log.i(TAG, "Current mode: " + (isShowingPrice ? "PRICE" : "ICON"));
        
        isShowingPrice = !isShowingPrice;
        
        Log.i(TAG, "New mode: " + (isShowingPrice ? "PRICE" : "ICON"));
        Log.i(TAG, "Service connected: " + isServiceConnected);
        
        if (isServiceConnected) {
            if (isShowingPrice) {
                Log.i(TAG, "Switching to price display");
                displayPrice();
            } else {
                Log.i(TAG, "Switching to icon display");
                displayBitcoinIcon();
            }
        } else {
            Log.e(TAG, "*** CANNOT TOGGLE - SERVICE NOT CONNECTED ***");
        }
    }
    
    private void startPriceUpdates() {
        if (priceUpdateTimer != null) {
            priceUpdateTimer.cancel();
        }
        
        priceUpdateTimer = new Timer();
        priceUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchBitcoinPrice();
            }
        }, 0, UPDATE_INTERVAL);
    }
    
    private void fetchBitcoinPrice() {
        // TODO: Implement actual Bitcoin price fetching from API
        // For now, simulate a price
        currentPrice = 45000.0 + (Math.random() * 10000.0);
        
        mainHandler.post(() -> {
            Log.d(TAG, "Bitcoin price updated: $" + String.format("%.2f", currentPrice));
            if (isShowingPrice) {
                displayPrice();
            }
        });
    }
    
    private void displayPrice() {
        if (!isServiceConnected || mGlyphManager == null) {
            Log.w(TAG, "Glyph service not connected, cannot display price");
            return;
        }
        
        try {
            // Create a frame for price display - different pattern from icon
            GlyphFrame.Builder builder = mGlyphManager.getGlyphFrameBuilder();
            
            // Light up different zones to show this is price mode
            GlyphFrame frame = builder
                .buildChannelA()
                .buildChannelC()
                .buildChannelE()
                .build();
            
            mGlyphManager.animate(frame);
            
            Log.d(TAG, "Displaying price: $" + String.format("%.0f", currentPrice));
        } catch (Exception e) {
            Log.e(TAG, "Failed to display price: " + e.getMessage());
        }
    }
    
    private void displayBitcoinIcon() {
        Log.i(TAG, "*** DISPLAYING BITCOIN ICON ***");
        Log.i(TAG, "Service connected: " + isServiceConnected);
        Log.i(TAG, "GlyphManager exists: " + (mGlyphManager != null));
        
        if (!isServiceConnected || mGlyphManager == null) {
            Log.e(TAG, "*** CANNOT DISPLAY ICON - SERVICE NOT READY ***");
            Log.e(TAG, "Service connected: " + isServiceConnected + ", GlyphManager: " + (mGlyphManager != null));
            return;
        }
        
        try {
            Log.i(TAG, "Creating GlyphFrame...");
            // Create a frame for Bitcoin icon display - light up multiple zones for visibility
            GlyphFrame.Builder builder = mGlyphManager.getGlyphFrameBuilder();
            Log.i(TAG, "Got GlyphFrame builder");
            
            // Try a simple single channel first to test basic functionality
            GlyphFrame frame = builder
                .buildChannelA()
                .build();
            
            Log.i(TAG, "Built GlyphFrame with channel A only");
            
            // Use animate instead of toggle for better visibility
            mGlyphManager.animate(frame);
            
            Log.i(TAG, "*** BITCOIN ICON ANIMATION SENT SUCCESSFULLY ***");
            
            // Also try toggle as a fallback
            Log.i(TAG, "Trying toggle as well...");
            mGlyphManager.toggle(frame);
            
            Log.i(TAG, "*** TOGGLE COMMAND SENT ***");
        } catch (Exception e) {
            Log.e(TAG, "*** FAILED TO DISPLAY BITCOIN ICON ***");
            Log.e(TAG, "Error: " + e.getMessage());
            e.printStackTrace();
            
            // Try turning on all zones at once as a test
            try {
                Log.i(TAG, "Trying turnOn() method...");
                mGlyphManager.turnOn();
                Log.i(TAG, "TurnOn command sent");
            } catch (Exception e2) {
                Log.e(TAG, "TurnOn also failed: " + e2.getMessage());
            }
        }
    }
    
    public void onLongPress() {
        Log.d(TAG, "Long press detected - toggling display mode");
        toggleDisplay();
    }
} 