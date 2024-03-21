package com.vegas.tranquilo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.vegas.tranquilo.R;
import com.vegas.tranquilo.utils.SharedPrefManager;
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity;

public class Splash extends LocaleAwareCompatActivity {

    ImageView splash_logo;
    Animation fade_in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        splash_logo = findViewById(R.id.splash_logo);

        fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        splash_logo.setAnimation(fade_in);


        new Handler().postDelayed(() -> {
            if (SharedPrefManager.getInstance(getApplicationContext()).isLoggedIn()) {
                startActivity(new Intent(Splash.this, Main.class));
                finish();
            } else {
                startActivity(new Intent(Splash.this, Login.class));
                finish();
            }
        }, 3000);


    }



}
