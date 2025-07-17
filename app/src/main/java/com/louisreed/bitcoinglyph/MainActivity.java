package com.louisreed.bitcoinglyph;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        TextView instructionText = findViewById(R.id.instruction_text);
        instructionText.setText("Bitcoin Glyph Toy for Nothing Phone 3 is installed!\n\n" +
                "To use:\n" +
                "1. Go to Settings > Glyph Interface > Glyph toys\n" +
                "2. Enable 'Bitcoin Tracker'\n" +
                "3. Press the Glyph button on the back to cycle to Bitcoin\n" +
                "4. Long press to toggle between icon and price display\n\n" +
                "The toy will automatically fetch live Bitcoin prices every 5 minutes.\n\n" +
                "⚠️ This app is designed exclusively for Nothing Phone 3 with Glyph Matrix display.");
        
        // Start the glyph toy service to make it discoverable
        try {
            Intent serviceIntent = new Intent(this, BitcoinGlyphToyService.class);
            // Don't auto-start the service - let the glyph system manage it
            // startService(serviceIntent);
            Log.d(TAG, "Bitcoin Glyph Toy Service ready for glyph system");
        } catch (Exception e) {
            Log.e(TAG, "Failed to prepare Bitcoin Glyph Toy Service: " + e.getMessage());
        }
    }
} 