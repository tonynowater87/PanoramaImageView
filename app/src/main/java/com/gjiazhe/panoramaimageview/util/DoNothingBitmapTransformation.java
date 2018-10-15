package com.gjiazhe.panoramaimageview.util;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.nio.charset.Charset;
import java.security.MessageDigest;

public class DoNothingBitmapTransformation extends BitmapTransformation {

    private static final String ID = "com.bumptech.glide.transformations.FillSpace";
    private static final byte[] ID_BYTES = ID.getBytes(Charset.forName("UTF-8"));

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        if (toTransform.getWidth() == outWidth && toTransform.getHeight() == outHeight) {
            return toTransform;
        }

        return Bitmap.createScaledBitmap(toTransform, outWidth, outHeight, /*filter=*/ true);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DoNothingBitmapTransformation;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}
