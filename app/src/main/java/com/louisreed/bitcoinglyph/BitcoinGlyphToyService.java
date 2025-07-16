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
            Log.d(TAG, "Glyph service connected");
            isServiceConnected = true;
            
            // Register with the appropriate device
            try {
                if (Common.is20111()) {
                    mGlyphManager.register(Glyph.DEVICE_20111);
                } else if (Common.is22111()) {
                    mGlyphManager.register(Glyph.DEVICE_22111);
                } else if (Common.is23111()) {
                    mGlyphManager.register(Glyph.DEVICE_23111);
                } else if (Common.is23113()) {
                    mGlyphManager.register(Glyph.DEVICE_23113);
                }
                
                mGlyphManager.openSession();
                Log.d(TAG, "Glyph session opened");
                
                // Show initial Bitcoin icon
                displayBitcoinIcon();
                
            } catch (GlyphException e) {
                Log.e(TAG, "Failed to initialize glyph: " + e.getMessage());
            }
        }
        
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "Glyph service disconnected");
            isServiceConnected = false;
            try {
                if (mGlyphManager != null) {
                    mGlyphManager.closeSession();
                }
            } catch (GlyphException e) {
                Log.e(TAG, "Failed to close glyph session: " + e.getMessage());
            }
        }
    };
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Bitcoin Glyph Toy Service created");
        
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize Glyph Manager
        mGlyphManager = GlyphManager.getInstance(getApplicationContext());
        mGlyphManager.init(mCallback);
        
        startPriceUpdates();
        
        // Log service creation to help with debugging
        Log.i(TAG, "Bitcoin Glyph Toy Service successfully created and initialized");
        Log.i(TAG, "Service package: " + getPackageName());
        Log.i(TAG, "Service class: " + getClass().getName());
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started with intent: " + (intent != null ? intent.getAction() : "null"));
        
        // Handle glyph toy specific intents
        if (intent != null) {
            String action = intent.getAction();
            if ("com.nothing.glyph.TOY".equals(action)) {
                Log.d(TAG, "Glyph toy action received");
                // This is called when the glyph toy is activated
                handleGlyphToyActivation();
            } else if ("com.nothing.glyph.TOY_LONGPRESS".equals(action)) {
                Log.d(TAG, "Glyph toy long press received");
                // Handle long press - toggle between icon and price
                toggleDisplay();
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action) ||
                       "android.intent.action.MY_PACKAGE_REPLACED".equals(action) ||
                       "android.intent.action.PACKAGE_REPLACED".equals(action)) {
                Log.d(TAG, "System startup or package replaced - registering service");
                // Service is being started after boot or package update
                // This helps with service discovery
            }
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
        Log.d(TAG, "Glyph toy activated");
        // This is called when the user activates the glyph toy
        if (isServiceConnected) {
            displayBitcoinIcon();
        }
    }
    
    private void toggleDisplay() {
        Log.d(TAG, "Toggling display mode");
        isShowingPrice = !isShowingPrice;
        
        if (isServiceConnected) {
            if (isShowingPrice) {
                displayPrice();
            } else {
                displayBitcoinIcon();
            }
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
        
        // Create a frame for price display (simplified - just light up some zones)
        GlyphFrame.Builder builder = mGlyphManager.getGlyphFrameBuilder();
        GlyphFrame frame = builder.buildChannelA().buildChannelB().build();
        mGlyphManager.animate(frame);
        
        Log.d(TAG, "Displaying price: $" + String.format("%.0f", currentPrice));
    }
    
    private void displayBitcoinIcon() {
        if (!isServiceConnected || mGlyphManager == null) {
            Log.w(TAG, "Glyph service not connected, cannot display icon");
            return;
        }
        
        // Create a frame for Bitcoin icon display
        GlyphFrame.Builder builder = mGlyphManager.getGlyphFrameBuilder();
        GlyphFrame frame = builder.buildChannelC().build();
        mGlyphManager.toggle(frame);
        
        Log.d(TAG, "Displaying Bitcoin icon");
    }
    
    public void onLongPress() {
        Log.d(TAG, "Long press detected - toggling display mode");
        isShowingPrice = !isShowingPrice;
        
        if (isShowingPrice) {
            displayPrice();
        } else {
            displayBitcoinIcon();
        }
    }
} 