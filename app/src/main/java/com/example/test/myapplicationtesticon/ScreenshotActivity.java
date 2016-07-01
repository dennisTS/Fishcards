package com.example.test.myapplicationtesticon;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class ScreenshotActivity extends Activity {

    private static final int REQUEST_SCREENSHOT = 0;
    private static final String TAG = "MainActivity";
    private static final String VD_NAME = "fishcards_vd";
    private static final int VD_FLAGS =
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

    private final HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName(),
            Process.THREAD_PRIORITY_BACKGROUND);

    private MediaProjectionManager projectionManager;
    private MediaProjection projection;
    private VirtualDisplay virtualDisplay;
    private Handler handler;
    private ImageReader imageReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screenshot);

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

        List<CharSequence> list = data.getCharSequenceArrayListExtra(MEDIA_PROJECTION_SERVICE);
        boolean isList = false;


        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "No permission", Toast.LENGTH_SHORT);
        }

        projection = projectionManager.getMediaProjection(resultCode, data);

        Display display = getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);

        imageReader = ImageReader.newInstance(size.x, size.y, PixelFormat.RGBA_8888, 1);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image=imageReader.acquireLatestImage();

                if (image!=null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * size.x;
                    int bitmapWidth = size.x + rowPadding / pixelStride;

                    Bitmap bitmap = Bitmap.createBitmap(bitmapWidth,
                            size.y, Bitmap.Config.ARGB_8888);

                    bitmap.copyPixelsFromBuffer(buffer);

                    if (image != null) {
                        image.close();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Bitmap cropped = Bitmap.createBitmap(bitmap, 0, 0,
                            size.x, size.y);

                    cropped.compress(Bitmap.CompressFormat.PNG, 100, baos);

                    byte[] newPng = baos.toByteArray();

                    File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)+"/file.png");
                    FileOutputStream fos;
                    try {
                        fos = new FileOutputStream(file);

                        fos.write(newPng);
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    projection.stop();
                    imageReader.close();
                    finish();
                }
            }
        }, handler);

        virtualDisplay = projection.createVirtualDisplay(VD_NAME, size.x, size.y,
                getResources().getDisplayMetrics().densityDpi, VD_FLAGS, imageReader.getSurface(),
                null, handler);

        MediaProjection.Callback callback = new MediaProjection.Callback() {
            @Override
            public void onStop() {
                virtualDisplay.release();
            }
        };

        projection.registerCallback(callback, handler);
    }

}
