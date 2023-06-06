package org.satochip.satodimeapp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.satochip.satodimeapp.MainActivity;
import org.satochip.satodimeapp.R;
import org.satochip.satodimeapp.Utils;

import io.paperdb.Paper;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Paper.init(this);

        initAll();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Paper.book().read("first_time", false)){
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                    finish();
                }else{
                    Paper.book().write("first_time", true);
                    startActivity(new Intent(SplashScreen.this, OnBoardingActivity.class));
                    finish();
                }
            }
        },2000);
    }

    private void initAll() {
        ImageView splashLogo = findViewById(R.id.logo_splash);
        if(Utils.isDark){
            splashLogo.setImageResource(R.drawable.splash_screen_white_logo);
        }
    }
}