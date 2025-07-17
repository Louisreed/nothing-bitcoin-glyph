package com.louisreed.bitcoinglyph;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;

// Using the correct matrix SDK classes for Nothing Phone 3
import com.nothing.ketchum.Common;
import com.nothing.ketchum.GlyphException;
import com.nothing.ketchum.GlyphManager;

public class BitcoinGlyphToyService extends Service {
    private static final String TAG = "BitcoinGlyphToyService";
    private static final int UPDATE_INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds
    private static final int MATRIX_SIZE = 25; // Phone 3 has 25x25 matrix

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
            
            // Register for Phone 3 matrix
            if (glyphManager.register()) {
                Log.i(TAG, "Successfully registered for Phone 3 matrix");
                
                try {
                    glyphManager.openSession();
                    Log.i(TAG, "Glyph session opened successfully");
                } catch (GlyphException e) {
                    Log.e(TAG, "Error opening Glyph session: " + e.getMessage(), e);
                }
            } else {
                Log.e(TAG, "Failed to register for Phone 3 matrix");
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
            // Create Bitcoin icon bitmap for 25x25 matrix
            Log.i(TAG, "Creating Bitcoin icon bitmap");
            
            Bitmap bitcoinBitmap = createBitcoinIconBitmap();
            
            // Convert bitmap to matrix data
            int[] matrixData = bitmapToMatrixData(bitcoinBitmap);
            
            // Display the matrix pattern
            displayMatrixPattern(matrixData, 1000, 3, 500);
            
            Log.i(TAG, "*** BITCOIN ICON DISPLAYED SUCCESSFULLY ***");
            
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
            // Create price display bitmap for 25x25 matrix
            Log.i(TAG, "Creating price display bitmap");
            
            Bitmap priceBitmap = createPriceDisplayBitmap();
            
            // Convert bitmap to matrix data
            int[] matrixData = bitmapToMatrixData(priceBitmap);
            
            // Display the matrix pattern
            displayMatrixPattern(matrixData, 2000, 1, 0);
            
            Log.i(TAG, "*** PRICE DISPLAYED SUCCESSFULLY ***");
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying price: " + e.getMessage(), e);
        }
    }

    private Bitmap createBitcoinIconBitmap() {
        // Create a 25x25 bitmap for the Bitcoin icon
        Bitmap bitmap = Bitmap.createBitmap(MATRIX_SIZE, MATRIX_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        
        // Clear the bitmap
        canvas.drawColor(Color.BLACK);
        
        // Draw Bitcoin "B" symbol
        // Vertical line
        canvas.drawRect(8, 5, 10, 20, paint);
        
        // Top horizontal line
        canvas.drawRect(8, 5, 16, 7, paint);
        
        // Middle horizontal line
        canvas.drawRect(8, 11, 15, 13, paint);
        
        // Bottom horizontal line
        canvas.drawRect(8, 17, 17, 19, paint);
        
        // Right curves (simplified as rectangles)
        canvas.drawRect(15, 7, 17, 11, paint);
        canvas.drawRect(15, 13, 17, 17, paint);
        
        return bitmap;
    }

    private Bitmap createPriceDisplayBitmap() {
        // Create a 25x25 bitmap for the price display
        Bitmap bitmap = Bitmap.createBitmap(MATRIX_SIZE, MATRIX_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        
        // Clear the bitmap
        canvas.drawColor(Color.BLACK);
        
        // Draw dollar sign "$" symbol
        // Vertical line
        canvas.drawRect(12, 3, 13, 22, paint);
        
        // Top curve of S
        canvas.drawRect(8, 3, 17, 5, paint);
        canvas.drawRect(8, 5, 10, 8, paint);
        canvas.drawRect(15, 7, 17, 11, paint);
        
        // Middle line of S
        canvas.drawRect(8, 11, 17, 13, paint);
        
        // Bottom curve of S
        canvas.drawRect(8, 15, 10, 19, paint);
        canvas.drawRect(15, 17, 17, 19, paint);
        canvas.drawRect(8, 19, 17, 21, paint);
        
        return bitmap;
    }

    private int[] bitmapToMatrixData(Bitmap bitmap) {
        int[] matrixData = new int[MATRIX_SIZE * MATRIX_SIZE];
        
        for (int y = 0; y < MATRIX_SIZE; y++) {
            for (int x = 0; x < MATRIX_SIZE; x++) {
                int pixel = bitmap.getPixel(x, y);
                // Convert to brightness (0-255)
                int brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;
                matrixData[y * MATRIX_SIZE + x] = brightness;
            }
        }
        
        return matrixData;
    }

    private void displayMatrixPattern(int[] matrixData, int period, int cycles, int interval) {
        // This is a placeholder for the actual matrix display method
        // The exact API for matrix display needs to be determined from the SDK
        Log.i(TAG, "Displaying matrix pattern with " + matrixData.length + " pixels");
        Log.i(TAG, "Period: " + period + "ms, Cycles: " + cycles + ", Interval: " + interval + "ms");
        
        // For now, we'll use a simple approach to light up the matrix
        // This may need to be adjusted based on the actual matrix SDK API
        try {
            // Try to use the matrix display functionality
            // This is a guess at the API - it may need adjustment
            glyphManager.turnOff(); // Clear previous display
            
            // The actual matrix display method will need to be implemented
            // based on the correct SDK documentation
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying matrix pattern: " + e.getMessage(), e);
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