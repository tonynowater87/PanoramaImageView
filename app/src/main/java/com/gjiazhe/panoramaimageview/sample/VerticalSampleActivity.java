package com.gjiazhe.panoramaimageview.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.gjiazhe.panoramaimageview.GyroscopeObserver;
import com.gjiazhe.panoramaimageview.PanoramaImageView;

public class VerticalSampleActivity extends AppCompatActivity {

    private GyroscopeObserver gyroscopeObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertical_sample);

        gyroscopeObserver = new GyroscopeObserver();

        PanoramaImageView panoramaImageView = findViewById(R.id.panorama_image_view);
        //panoramaImageView.setGyroscopeObserver(gyroscopeObserver);
        panoramaImageView.startAutoScrolling();
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
