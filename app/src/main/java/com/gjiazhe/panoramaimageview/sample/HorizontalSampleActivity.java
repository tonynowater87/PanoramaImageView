package com.gjiazhe.panoramaimageview.sample;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gjiazhe.panoramaimageview.GyroscopeObserver;
import com.gjiazhe.panoramaimageview.PanoramaImageView;
import com.gjiazhe.panoramaimageview.util.DoNothingBitmapTransformation;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class HorizontalSampleActivity extends AppCompatActivity {

    private GyroscopeObserver gyroscopeObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_sample);

        gyroscopeObserver = new GyroscopeObserver();

        PanoramaImageView panoramaImageView = findViewById(R.id.panorama_image_view);
        panoramaImageView.setGyroscopeObserver(gyroscopeObserver);
        Glide.with(this)
             .setDefaultRequestOptions(RequestOptions.bitmapTransform(new DoNothingBitmapTransformation()))
             .load(Uri.parse("https://cdn.pixabay.com/photo/2018/03/27/17/23/the-beach-3266660_1280.jpg"))
             .into(panoramaImageView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gyroscopeObserver.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gyroscopeObserver.unregister();
    }
}
