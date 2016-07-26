package com.fersifex.fishcards.image;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.view.Surface;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;

import java.nio.ByteBuffer;

public class ImageCreator implements ImageReader.OnImageAvailableListener {

    public static final int PIXEL_LIMIT = 650000;
    public static final int DENOMINATOR = 2;
    private static final Bitmap.Config BIT_DEPTH = Bitmap.Config.ARGB_8888;

    private MediaProjection projection;
    private MediaProjection.Callback callback;

    private ImageReader reader;

    private int width;
    private int height;
    private Pix image;

    public ImageCreator(MediaProjection projection, Point size, Handler handler) {
        setSize(size);

        this.projection = projection;

        this.reader = ImageReader.newInstance(this.width, this.height, PixelFormat.RGBA_8888, 1);
        this.reader.setOnImageAvailableListener(this, handler);
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image=reader.acquireLatestImage();

        if (image!=null) {
            Bitmap cropped = Bitmap.createBitmap(getScaledBitmap(image), 0, 0,
                    width, height);

            this.image = ReadFile.readBitmap(cropped);
            cropped.recycle();

            if (callback != null) {
                callback.onStop();
            }
            projection.stop();
            reader.close();
        }
    }

    private Bitmap getScaledBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        int bitmapWidth = width + rowPadding / pixelStride;

        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth,
                height, BIT_DEPTH);

        bitmap.copyPixelsFromBuffer(buffer);
        image.close();
        return bitmap;
    }

    private void setSize(Point size) {
        this.width = size.x;
        this.height = size.y;

        while(this.width * this.height > PIXEL_LIMIT) {
            this.width = this.width / DENOMINATOR;
            this.height = this.height / DENOMINATOR;
        }
    }

    public Pix getImage() {
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Surface getSurface() {
        return reader.getSurface();
    }

    public void setOnStopListener(MediaProjection.Callback callback) {
        this.callback = callback;
    }
}
