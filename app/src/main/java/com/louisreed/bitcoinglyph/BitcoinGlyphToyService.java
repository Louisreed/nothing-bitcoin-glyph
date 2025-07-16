package com.louisreed.bitcoinglyph;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;

public class BitcoinGlyphToyService extends Service {
    private static final String TAG = "BitcoinGlyphToyService";
    private static final int UPDATE_INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds
    
    private Timer priceUpdateTimer;
    private Handler mainHandler;
    private boolean isShowingPrice = false;
    private double currentPrice = 0.0;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Bitcoin Glyph Toy Service created");
        mainHandler = new Handler(Looper.getMainLooper());
        startPriceUpdates();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        if (priceUpdateTimer != null) {
            priceUpdateTimer.cancel();
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
            updateGlyphDisplay();
        });
    }
    
    private void updateGlyphDisplay() {
        // TODO: Implement actual Glyph Matrix SDK integration
        // This is a placeholder for the actual glyph display logic
        Log.d(TAG, "Updating glyph display - showing " + (isShowingPrice ? "price" : "icon"));
        
        if (isShowingPrice) {
            displayPrice();
        } else {
            displayBitcoinIcon();
        }
    }
    
    private void displayPrice() {
        // TODO: Display price on glyph matrix
        Log.d(TAG, "Displaying price: $" + String.format("%.0f", currentPrice));
    }
    
    private void displayBitcoinIcon() {
        // TODO: Display Bitcoin icon on glyph matrix
        Log.d(TAG, "Displaying Bitcoin icon");
    }
    
    public void onLongPress() {
        // Toggle between price and icon display
        isShowingPrice = !isShowingPrice;
        Log.d(TAG, "Long press detected - toggling display mode");
        updateGlyphDisplay();
    }
} 