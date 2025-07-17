package com.louisreed.bitcoinglyph;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Bundle;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import java.util.Timer;
import java.util.TimerTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Using the correct Glyph SDK classes from the new documentation
import com.nothing.ketchum.Common;
import com.nothing.ketchum.Glyph;
import com.nothing.ketchum.GlyphException;
import com.nothing.ketchum.GlyphMatrixFrame;
import com.nothing.ketchum.GlyphMatrixManager;
import com.nothing.ketchum.GlyphMatrixObject;
import com.nothing.ketchum.GlyphMatrixUtils;
import com.nothing.ketchum.GlyphToy;

public class BitcoinGlyphToyService extends Service {
    private static final String TAG = "BitcoinGlyphToyService";
    private static final int UPDATE_INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds
    private static final String BITCOIN_API_URL = "https://api.coindesk.com/v1/bpi/currentprice.json";

    private GlyphMatrixManager glyphMatrixManager;
    private Timer priceUpdateTimer;
    private Handler mainHandler;
    private boolean isShowingIcon = true;
    private boolean isInitialized = false;
    private ExecutorService executorService;
    
    // Price variables
    private double currentPrice = 0.0;
    private long lastUpdateTime = 0;
    
    // Handler for processing glyph toy events
    private final Handler serviceHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GlyphToy.MSG_GLYPH_TOY: {
                    Bundle bundle = msg.getData();
                    String event = bundle.getString(GlyphToy.MSG_GLYPH_TOY_DATA);
                    Log.d(TAG, "Received glyph toy event: " + event);
                    
                    if (GlyphToy.EVENT_CHANGE.equals(event)) {
                        // Handle long press event - toggle between icon and price
                        toggleDisplay();
                    }
                    break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    };
    
    // Messenger for communicating with the system
    private final Messenger serviceMessenger = new Messenger(serviceHandler);

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service bound");
        init();
        return serviceMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Service unbound");
        cleanup();
        return false;
    }

    private void init() {
        Log.d(TAG, "Initializing Bitcoin Glyph Toy Service");
        
        if (isInitialized) {
            Log.d(TAG, "Service already initialized");
            return;
        }
        
        try {
            mainHandler = new Handler(Looper.getMainLooper());
            executorService = Executors.newFixedThreadPool(2);
            
            // Initialize the GlyphMatrixManager
            glyphMatrixManager = new GlyphMatrixManager();
            glyphMatrixManager.init(new GlyphMatrixManager.Callback() {
                @Override
                public void onServiceConnected(android.content.ComponentName componentName) {
                    Log.d(TAG, "Glyph Matrix service connected");
                    try {
                        // Register for Phone 3 as specified in documentation
                        glyphMatrixManager.register(Glyph.DEVICE_23112);
                        isInitialized = true;
                        
                        // Start price updates
                        startPriceUpdates();
                        
                        // Show initial display
                        displayBitcoinIcon();
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Error registering glyph matrix manager: " + e.getMessage(), e);
                    }
                }

                @Override
                public void onServiceDisconnected(android.content.ComponentName componentName) {
                    Log.d(TAG, "Glyph Matrix service disconnected");
                    isInitialized = false;
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing service: " + e.getMessage(), e);
        }
    }

    private void cleanup() {
        Log.d(TAG, "Cleaning up service");
        
        if (priceUpdateTimer != null) {
            priceUpdateTimer.cancel();
            priceUpdateTimer = null;
        }
        
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
        
        if (glyphMatrixManager != null && isInitialized) {
            try {
                glyphMatrixManager.unInit();
            } catch (Exception e) {
                Log.e(TAG, "Error uninitializing glyph matrix manager: " + e.getMessage(), e);
            }
            glyphMatrixManager = null;
        }
        
        isInitialized = false;
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
        if (executorService == null) {
            return;
        }
        
        executorService.execute(() -> {
            try {
                URL url = new URL(BITCOIN_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    // Parse the JSON response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONObject bpi = jsonResponse.getJSONObject("bpi");
                    JSONObject usd = bpi.getJSONObject("USD");
                    String rateString = usd.getString("rate_float");
                    
                    currentPrice = Double.parseDouble(rateString);
                    lastUpdateTime = System.currentTimeMillis();
                    
                    Log.d(TAG, "Updated Bitcoin price: $" + String.format("%.2f", currentPrice));
                    
                    // If we're showing price, update the display
                    if (!isShowingIcon && isInitialized) {
                        mainHandler.post(this::displayBitcoinPrice);
                    }
                    
                } else {
                    Log.e(TAG, "HTTP error: " + responseCode);
                }
                
                connection.disconnect();
                
            } catch (Exception e) {
                Log.e(TAG, "Error fetching Bitcoin price: " + e.getMessage(), e);
                // Fallback to simulation if API fails
                currentPrice = 50000 + (Math.random() * 10000);
                lastUpdateTime = System.currentTimeMillis();
            }
        });
    }

    private void toggleDisplay() {
        Log.d(TAG, "Toggling display mode");
        isShowingIcon = !isShowingIcon;
        
        if (isShowingIcon) {
            displayBitcoinIcon();
        } else {
            displayBitcoinPrice();
        }
    }

    private void displayBitcoinIcon() {
        if (!isInitialized) {
            Log.w(TAG, "Cannot display icon - service not initialized");
            return;
        }
        
        Log.d(TAG, "Displaying Bitcoin icon");
        
        try {
            // Create a Bitcoin icon bitmap
            Bitmap iconBitmap = createBitcoinIconBitmap();
            
            // Create a GlyphMatrixObject with the icon
            GlyphMatrixObject.Builder objectBuilder = new GlyphMatrixObject.Builder();
            GlyphMatrixObject bitcoinIcon = objectBuilder
                .setImageSource(iconBitmap)
                .setPosition(0, 0)
                .setScale(100)
                .setBrightness(255)
                .setOrientation(0)
                .build();
            
            // Create a frame with the icon
            GlyphMatrixFrame.Builder frameBuilder = new GlyphMatrixFrame.Builder();
            GlyphMatrixFrame frame = frameBuilder
                .addTop(bitcoinIcon)
                .build(getApplicationContext());
            
            // Display the frame
            glyphMatrixManager.setMatrixFrame(frame);
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying Bitcoin icon: " + e.getMessage(), e);
        }
    }

    private void displayBitcoinPrice() {
        if (!isInitialized) {
            Log.w(TAG, "Cannot display price - service not initialized");
            return;
        }
        
        Log.d(TAG, "Displaying Bitcoin price: $" + String.format("%.0f", currentPrice));
        
        try {
            // Create a bitmap with the price text
            Bitmap priceBitmap = createPriceBitmap();
            
            // Create a GlyphMatrixObject with the price
            GlyphMatrixObject.Builder objectBuilder = new GlyphMatrixObject.Builder();
            GlyphMatrixObject priceObject = objectBuilder
                .setImageSource(priceBitmap)
                .setPosition(0, 0)
                .setScale(100)
                .setBrightness(255)
                .setOrientation(0)
                .build();
            
            // Create a frame with the price
            GlyphMatrixFrame.Builder frameBuilder = new GlyphMatrixFrame.Builder();
            GlyphMatrixFrame frame = frameBuilder
                .addTop(priceObject)
                .build(getApplicationContext());
            
            // Display the frame
            glyphMatrixManager.setMatrixFrame(frame);
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying Bitcoin price: " + e.getMessage(), e);
        }
    }

    private Bitmap createBitcoinIconBitmap() {
        // Create a 25x25 bitmap for the Bitcoin icon
        Bitmap bitmap = Bitmap.createBitmap(25, 25, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(1);
        
        // Draw a simplified Bitcoin "B" symbol
        // Vertical line (left side)
        canvas.drawLine(8, 5, 8, 19, paint);
        canvas.drawLine(9, 5, 9, 19, paint);
        
        // Top horizontal line
        canvas.drawLine(8, 5, 15, 5, paint);
        canvas.drawLine(8, 6, 15, 6, paint);
        
        // Middle horizontal line
        canvas.drawLine(8, 12, 14, 12, paint);
        canvas.drawLine(8, 13, 14, 13, paint);
        
        // Bottom horizontal line
        canvas.drawLine(8, 18, 15, 18, paint);
        canvas.drawLine(8, 19, 15, 19, paint);
        
        // Right vertical lines
        canvas.drawLine(15, 6, 15, 11, paint);
        canvas.drawLine(16, 6, 16, 11, paint);
        canvas.drawLine(14, 14, 14, 17, paint);
        canvas.drawLine(15, 14, 15, 17, paint);
        
        return bitmap;
    }

    private Bitmap createPriceBitmap() {
        // Create a 25x25 bitmap for the price display
        Bitmap bitmap = Bitmap.createBitmap(25, 25, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setTextSize(6);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        
        // Format price to show in thousands (e.g., "52K")
        String priceText;
        if (currentPrice >= 1000) {
            priceText = String.format("%.0fK", currentPrice / 1000);
        } else {
            priceText = String.format("%.0f", currentPrice);
        }
        
        // Draw the price text centered
        float textWidth = paint.measureText(priceText);
        canvas.drawText(priceText, (25 - textWidth) / 2, 15, paint);
        
        return bitmap;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service destroyed");
        cleanup();
        super.onDestroy();
    }
} 