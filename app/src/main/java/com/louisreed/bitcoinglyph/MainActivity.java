package com.louisreed.bitcoinglyph;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        TextView instructionText = findViewById(R.id.instruction_text);
        instructionText.setText("Bitcoin Glyph Toy is installed!\n\n" +
                "To use:\n" +
                "1. Go to Settings > Glyph Interface > Glyph toys\n" +
                "2. Enable 'Bitcoin Tracker'\n" +
                "3. Press the Glyph button on the back to cycle to Bitcoin\n" +
                "4. Long press to toggle between icon and price display\n\n" +
                "The toy will automatically fetch live Bitcoin prices every 5 minutes.");
    }
} 