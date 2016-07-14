package com.fersifex.fishcards.image;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ImageCreator implements ImageReader.OnImageAvailableListener {

    private MediaProjection projection;
    private ImageReader reader;

    private int width;
    private int height;

    private byte[] image;
    private Bitmap.Config bitDepth;
    private MediaProjection.Callback callback;

    public ImageCreator(MediaProjection projection, Point size, Handler handler) {
        this.projection = projection;

        this.width = size.x;
        this.height = size.y;

        this.reader = ImageReader.newInstance(size.x, size.y, PixelFormat.RGBA_8888, 1);
        this.reader.setOnImageAvailableListener(this, handler);

        this.bitDepth = Bitmap.Config.ARGB_8888;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
// TODO Start spinner. Long processing (2s)
        Image image=reader.acquireLatestImage();

        if (image!=null) {
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            int bitmapWidth = width + rowPadding / pixelStride;

            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth,
                    height, bitDepth);

            bitmap.copyPixelsFromBuffer(buffer);

            if (image != null) {
                image.close();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap cropped = Bitmap.createBitmap(bitmap, 0, 0,
                    width, height);

            cropped.compress(Bitmap.CompressFormat.PNG, 100, baos);

            this.image = baos.toByteArray();

            //TODO end of heavy processing.
            //TODO handler.dispatchMessage with image

            if (callback != null) {
                callback.onStop();
            }
            projection.stop();
            reader.close();
        }
    }

    public byte[] getImage() {
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

    public int getBytesPerPixel() {
        if (bitDepth == Bitmap.Config.ARGB_8888) {
            return 4;
        } else {
            throw new IllegalStateException("Bit depth not properly set up");
        }
    }

    public void setOnStopListener(MediaProjection.Callback callback) {
        this.callback = callback;
    }
}
