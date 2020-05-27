package com.example.daggerapplication.ui;

import android.os.Bundle;

import com.example.daggerapplication.R;
import com.example.daggerapplication.ui.home.HomeFragment;

import dagger.android.support.DaggerAppCompatActivity;

/**
 * Main Activity, we should extends from Dagger
 */
public class MainActivity extends DaggerAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction().replace(R.id.placeholder, new HomeFragment()).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CompositeDisposable.clear();
    }
}
