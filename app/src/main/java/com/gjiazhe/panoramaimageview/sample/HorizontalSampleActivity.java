package com.gjiazhe.panoramaimageview.sample;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.gjiazhe.panoramaimageview.PanoramaImageView;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class HorizontalSampleActivity extends AppCompatActivity {

    private PanoramaImageView mPanoramaImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_sample);

        mPanoramaImageView = findViewById(R.id.panorama_image_view);

        Glide.with(this)
             .asBitmap()
             .load(Uri.parse(
                     "https://images.pexels.com/photos/356830/pexels-photo-356830.jpeg?cs=srgb&dl=architecture-bay-bridge-356830.jpg&fm=jpg"))
             .into(new CustomViewTarget<PanoramaImageView, Bitmap>(mPanoramaImageView) {
                 @Override
                 public void onLoadFailed(@Nullable Drawable errorDrawable) {

                 }

                 @Override
                 public void onResourceReady(@NonNull Bitmap resource,
                                             @Nullable Transition<? super Bitmap> transition) {
                     mPanoramaImageView.setImageBitmap(resource);
                 }


                 @Override
                 protected void onResourceCleared(@Nullable Drawable placeholder) {

                 }
             });

        mPanoramaImageView.startAutoScrolling();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mPanoramaImageView.stopAutoScrolling();
            }
        }, 2000);
    }
}
