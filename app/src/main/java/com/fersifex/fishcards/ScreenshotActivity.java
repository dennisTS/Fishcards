package com.fersifex.fishcards;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import com.fersifex.fishcards.image.ImageProcessor;

public class ScreenshotActivity extends Activity {

    private static final int REQUEST_SCREENSHOT = 0;
    private static final String TAG = "ScreenshotActivity";
    private static final String VD_NAME = "fishcards_vd";
    private static final int VD_FLAGS =
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

    private final HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName(),
            Process.THREAD_PRIORITY_BACKGROUND);

    private MediaProjectionManager projectionManager;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_SCREENSHOT);
            }
        }, 500);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_SCREENSHOT) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }

        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "No permission", Toast.LENGTH_SHORT);
        }

        makeScreenshot(projectionManager.getMediaProjection(resultCode, data));
    }

    private void makeScreenshot(MediaProjection projection) {
        final ImageProcessor processor = new ImageProcessor(projection, getScreenSize(), handler);

        final VirtualDisplay virtualDisplay = projection.createVirtualDisplay(VD_NAME, processor.getWidth(), processor.getHeight(),
                getResources().getDisplayMetrics().densityDpi, VD_FLAGS, processor.getSurface(),
                null, handler);

        MediaProjection.Callback callback = new MediaProjection.Callback() {
            @Override
            public void onStop() {
                byte[] image = processor.getImage();
                virtualDisplay.release();
            }
        };

        projection.registerCallback(callback, handler);
    }

    private Point getScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);

        return size;
    }

}
