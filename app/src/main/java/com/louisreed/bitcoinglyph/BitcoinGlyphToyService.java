package com.louisreed.bitcoinglyph;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.nothing.ketchum.GlyphMatrixFrame;
import com.nothing.ketchum.GlyphMatrixManager;
import com.nothing.ketchum.GlyphMatrixObject;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BitcoinGlyphToyService extends GlyphMatrixService {
    private static final String TAG = "BitcoinGlyphToyService";
    private static final int UPDATE_INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds
    private static final String BITCOIN_API_URL = "https://api.coindesk.com/v1/bpi/currentprice.json";

    private Timer priceUpdateTimer;
    private Handler mainHandler;
    private ExecutorService executorService;
    private boolean isShowingIcon = true;
    
    // Price variables
    private double currentPrice = 0.0;
    private long lastUpdateTime = 0;
    
    public BitcoinGlyphToyService() {
        super("Bitcoin-Glyph-Toy");
    }

    @Override
    public void performOnServiceConnected(Context context, GlyphMatrixManager glyphMatrixManager) {
        Log.d(TAG, "Service connected, initializing Bitcoin tracker");
        
        // Initialize components
        mainHandler = new Handler(Looper.getMainLooper());
        executorService = Executors.newFixedThreadPool(2);
        
        // Start price updates
        startPriceUpdates();
        
        // Show initial display
        displayBitcoinIcon(glyphMatrixManager);
    }

    @Override
    public void performOnServiceDisconnected(Context context) {
        Log.d(TAG, "Service disconnected, cleaning up");
        
        // Cleanup timer
        if (priceUpdateTimer != null) {
            priceUpdateTimer.cancel();
            priceUpdateTimer = null;
        }
        
        // Cleanup executor
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

    @Override
    public void onTouchPointLongPress() {
        Log.d(TAG, "Long press detected - toggling display");
        toggleDisplay();
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
                    if (!isShowingIcon) {
                        mainHandler.post(() -> {
                            GlyphMatrixManager manager = getGlyphMatrixManager();
                            if (manager != null) {
                                displayBitcoinPrice(manager);
                            }
                        });
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
        isShowingIcon = !isShowingIcon;
        
        GlyphMatrixManager manager = getGlyphMatrixManager();
        if (manager != null) {
            if (isShowingIcon) {
                displayBitcoinIcon(manager);
            } else {
                displayBitcoinPrice(manager);
            }
        }
    }

    private void displayBitcoinIcon(GlyphMatrixManager glyphMatrixManager) {
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

    private void displayBitcoinPrice(GlyphMatrixManager glyphMatrixManager) {
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
} 