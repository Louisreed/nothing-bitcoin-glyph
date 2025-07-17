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

import com.nothing.glyph.matrix.GlyphMatrixManager;
import com.nothing.glyph.matrix.GlyphMatrixFrame;
import com.nothing.glyph.matrix.GlyphMatrixObject;

public class BitcoinGlyphToyService extends Service {
    private static final String TAG = "BitcoinGlyphToyService";
    private static final int UPDATE_INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds
    private static final int MATRIX_SIZE = 25; // 25x25 matrix
    
    private GlyphMatrixManager glyphMatrixManager;
    private Timer priceUpdateTimer;
    private Handler mainHandler;
    private boolean isShowingPrice = false;
    private double currentPrice = 0.0;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "*** BITCOIN GLYPH TOY SERVICE CREATED ***");
        
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize GlyphMatrix Manager
        try {
            glyphMatrixManager = new GlyphMatrixManager(this);
            Log.i(TAG, "GlyphMatrixManager initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize GlyphMatrixManager: " + e.getMessage());
        }
        
        // Start Bitcoin price updates
        startPriceUpdates();
        
        Log.i(TAG, "*** SERVICE INITIALIZATION COMPLETE ***");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "=== SERVICE STARTED ===");
        
        if (intent != null) {
            String action = intent.getAction();
            Log.i(TAG, "Intent action: " + action);
            
            if ("com.nothing.glyph.TOY".equals(action)) {
                Log.i(TAG, "*** HANDLING GLYPH TOY ACTIVATION ***");
                displayBitcoinIcon();
            } else if ("com.nothing.glyph.TOY_LONGPRESS".equals(action)) {
                Log.i(TAG, "*** HANDLING GLYPH TOY LONG PRESS ***");
                toggleDisplay();
            } else {
                Log.i(TAG, "Unknown action: " + action + " - showing Bitcoin icon");
                displayBitcoinIcon();
            }
        } else {
            Log.i(TAG, "No intent - showing Bitcoin icon");
            displayBitcoinIcon();
        }
        
        return START_NOT_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        
        if (priceUpdateTimer != null) {
            priceUpdateTimer.cancel();
            priceUpdateTimer = null;
        }
        
        if (glyphMatrixManager != null) {
            try {
                glyphMatrixManager.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing GlyphMatrixManager: " + e.getMessage());
            }
        }
    }
    
    private void startPriceUpdates() {
        Log.i(TAG, "Starting Bitcoin price updates");
        
        priceUpdateTimer = new Timer();
        priceUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchBitcoinPrice();
            }
        }, 0, UPDATE_INTERVAL);
    }
    
    private void fetchBitcoinPrice() {
        // Simulate fetching price (in real app, you'd make HTTP request)
        currentPrice = 45000 + (Math.random() * 20000); // Random price between 45k-65k
        Log.d(TAG, "Bitcoin price updated: $" + String.format("%.2f", currentPrice));
        
        // Update display if showing price
        if (isShowingPrice) {
            mainHandler.post(this::displayPrice);
        }
    }
    
    private void displayBitcoinIcon() {
        Log.i(TAG, "*** DISPLAYING BITCOIN ICON ***");
        
        if (glyphMatrixManager == null) {
            Log.e(TAG, "GlyphMatrixManager is null");
            return;
        }
        
        try {
            // Create a 25x25 bitmap with Bitcoin symbol
            Bitmap bitcoinBitmap = createBitcoinIconBitmap();
            
            // Convert bitmap to GlyphMatrixObject
            GlyphMatrixObject matrixObject = GlyphMatrixObject.fromBitmap(bitcoinBitmap);
            
            // Create GlyphMatrixFrame with the object
            GlyphMatrixFrame frame = new GlyphMatrixFrame.Builder()
                    .addObject(matrixObject)
                    .setDuration(3000) // 3 seconds
                    .build();
            
            // Display the frame
            glyphMatrixManager.setMatrixFrame(frame);
            
            isShowingPrice = false;
            Log.i(TAG, "*** BITCOIN ICON DISPLAYED SUCCESSFULLY ***");
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying Bitcoin icon: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void displayPrice() {
        Log.i(TAG, "*** DISPLAYING BITCOIN PRICE ***");
        
        if (glyphMatrixManager == null) {
            Log.e(TAG, "GlyphMatrixManager is null");
            return;
        }
        
        try {
            // Create a 25x25 bitmap with price text
            Bitmap priceBitmap = createPriceBitmap();
            
            // Convert bitmap to GlyphMatrixObject
            GlyphMatrixObject matrixObject = GlyphMatrixObject.fromBitmap(priceBitmap);
            
            // Create GlyphMatrixFrame with the object
            GlyphMatrixFrame frame = new GlyphMatrixFrame.Builder()
                    .addObject(matrixObject)
                    .setDuration(5000) // 5 seconds
                    .build();
            
            // Display the frame
            glyphMatrixManager.setMatrixFrame(frame);
            
            isShowingPrice = true;
            Log.i(TAG, "*** BITCOIN PRICE DISPLAYED SUCCESSFULLY ***");
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying Bitcoin price: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void toggleDisplay() {
        Log.i(TAG, "*** TOGGLING DISPLAY MODE ***");
        
        if (isShowingPrice) {
            displayBitcoinIcon();
        } else {
            displayPrice();
        }
    }
    
    private Bitmap createBitcoinIconBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(MATRIX_SIZE, MATRIX_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Clear background
        canvas.drawColor(Color.BLACK);
        
        // Create paint for Bitcoin symbol
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setAntiAlias(true);
        
        // Draw Bitcoin symbol (₿)
        // Outer circle
        canvas.drawCircle(MATRIX_SIZE / 2f, MATRIX_SIZE / 2f, MATRIX_SIZE / 2f - 2, paint);
        
        // Bitcoin "B" shape
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(MATRIX_SIZE / 2f);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        
        // Center the "B" text
        float textX = MATRIX_SIZE / 2f - paint.measureText("B") / 2f;
        float textY = MATRIX_SIZE / 2f + paint.getTextSize() / 3f;
        canvas.drawText("B", textX, textY, paint);
        
        // Add vertical lines for Bitcoin symbol
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        canvas.drawLine(MATRIX_SIZE / 2f, 3, MATRIX_SIZE / 2f, 7, paint);
        canvas.drawLine(MATRIX_SIZE / 2f, MATRIX_SIZE - 7, MATRIX_SIZE / 2f, MATRIX_SIZE - 3, paint);
        
        return bitmap;
    }
    
    private Bitmap createPriceBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(MATRIX_SIZE, MATRIX_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Clear background
        canvas.drawColor(Color.BLACK);
        
        // Create paint for price text
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(6f); // Small text to fit in 25x25
        paint.setAntiAlias(true);
        
        // Format price (e.g., "45K")
        String priceText = String.format("%.0fK", currentPrice / 1000);
        
        // Center the text
        float textX = MATRIX_SIZE / 2f - paint.measureText(priceText) / 2f;
        float textY = MATRIX_SIZE / 2f + paint.getTextSize() / 3f;
        canvas.drawText(priceText, textX, textY, paint);
        
        return bitmap;
    }
} 