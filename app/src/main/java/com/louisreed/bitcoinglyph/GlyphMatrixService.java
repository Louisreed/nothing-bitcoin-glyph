package com.louisreed.bitcoinglyph;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.nothing.ketchum.Glyph;
import com.nothing.ketchum.GlyphMatrixManager;
import com.nothing.ketchum.GlyphToy;

public abstract class GlyphMatrixService extends Service {

    private static final String TAG = "GlyphMatrixService";
    private static final String KEY_DATA = "data";
    
    private final String logTag;
    private GlyphMatrixManager glyphMatrixManager;
    
    private final Handler buttonPressedHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GlyphToy.MSG_GLYPH_TOY: {
                    Bundle data = msg.getData();
                    if (data != null && data.containsKey(KEY_DATA)) {
                        String value = data.getString(KEY_DATA);
                        if (value != null) {
                            switch (value) {
                                case GlyphToy.EVENT_ACTION_DOWN:
                                    onTouchPointPressed();
                                    break;
                                case GlyphToy.EVENT_ACTION_UP:
                                    onTouchPointReleased();
                                    break;
                                case GlyphToy.EVENT_CHANGE:
                                    onTouchPointLongPress();
                                    break;
                            }
                        }
                    }
                    break;
                }
                default:
                    Log.d(TAG, "Message: " + msg.what);
                    super.handleMessage(msg);
                    break;
            }
        }
    };
    
    private final Messenger serviceMessenger = new Messenger(buttonPressedHandler);
    
    private final GlyphMatrixManager.Callback gmmCallback = new GlyphMatrixManager.Callback() {
        @Override
        public void onServiceConnected(ComponentName componentName) {
            if (glyphMatrixManager != null) {
                Log.d(TAG, logTag + ": onServiceConnected");
                glyphMatrixManager.register(Glyph.DEVICE_23112);
                performOnServiceConnected(getApplicationContext(), glyphMatrixManager);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // Handle service disconnection if needed
        }
    };
    
    public GlyphMatrixService(String tag) {
        this.logTag = tag;
    }
    
    @Override
    public ComponentName startService(Intent intent) {
        Log.d(TAG, logTag + ": startService");
        return super.startService(intent);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, logTag + ": onBind");
        
        try {
            glyphMatrixManager = GlyphMatrixManager.getInstance(getApplicationContext());
            if (glyphMatrixManager != null) {
                glyphMatrixManager.init(gmmCallback);
                Log.d(TAG, logTag + ": onBind completed");
            } else {
                Log.e(TAG, logTag + ": Failed to get GlyphMatrixManager instance");
            }
        } catch (Exception e) {
            Log.e(TAG, logTag + ": Error during onBind: " + e.getMessage(), e);
        }
        
        return serviceMessenger.getBinder();
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, logTag + ": onUnbind");
        
        if (glyphMatrixManager != null) {
            Log.d(TAG, logTag + ": onServiceDisconnected");
            performOnServiceDisconnected(getApplicationContext());
            
            try {
                glyphMatrixManager.turnOff();
                glyphMatrixManager.unInit();
            } catch (Exception e) {
                Log.e(TAG, logTag + ": Error during cleanup: " + e.getMessage(), e);
            }
            
            glyphMatrixManager = null;
        }
        
        return false;
    }
    
    public GlyphMatrixManager getGlyphMatrixManager() {
        return glyphMatrixManager;
    }
    
    // Abstract methods to be implemented by subclasses
    public abstract void performOnServiceConnected(Context context, GlyphMatrixManager glyphMatrixManager);
    
    public void performOnServiceDisconnected(Context context) {
        // Default implementation does nothing
    }
    
    public void onTouchPointPressed() {
        // Default implementation does nothing
    }
    
    public void onTouchPointLongPress() {
        // Default implementation does nothing
    }
    
    public void onTouchPointReleased() {
        // Default implementation does nothing
    }
} 