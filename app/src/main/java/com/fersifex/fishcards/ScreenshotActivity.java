package com.fersifex.fishcards;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import com.fersifex.fishcards.image.ImageCreator;
import com.googlecode.tesseract.android.TessBaseAPI;

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
        final ImageCreator imageCreator = new ImageCreator(projection, getScreenSize(), handler);

        final VirtualDisplay virtualDisplay = projection.createVirtualDisplay(VD_NAME, imageCreator.getWidth(), imageCreator.getHeight(),
                getResources().getDisplayMetrics().densityDpi, VD_FLAGS, imageCreator.getSurface(),
                null, handler);

        MediaProjection.Callback callback = new MediaProjection.Callback() {
            @Override
            public void onStop() {
                virtualDisplay.release();

                sendImage(imageCreator);
            }
        };

        imageCreator.setOnStopListener(callback);
    }

    private void sendImage(ImageCreator imageCreator) {
        TessBaseAPI baseAPI = new TessBaseAPI();

        baseAPI.setDebug(true);
        baseAPI.init(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/", "eng");
        baseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT_OSD);
        baseAPI.setImage(imageCreator.getImage());

        imageCreator.getImage().recycle();

        String text = baseAPI.getUTF8Text();

        baseAPI.end();

        Intent intent = new Intent(ScreenshotActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.TEXT_EXTRA, text);
        startActivity(intent);
    }

    private Point getScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);

        return size;
    }

}
