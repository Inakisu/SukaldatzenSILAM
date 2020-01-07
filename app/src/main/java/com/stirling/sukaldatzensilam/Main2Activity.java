package com.stirling.sukaldatzensilam;

import android.os.Bundle;

import android.os.Handler;
import android.text.Html;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;

public class Main2Activity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private int NUM_OF_COUNT;
    private boolean timelapseRunning = false;

    @BindView(R.id.bSetAlarm) TextView bSetTemperatureAlarm;
    @BindView(R.id.temperatureThreshold) TextView temperatureThreshold;
    @BindView(R.id.bSetTime) TextView bSetTimeAlarm;
    @BindView(R.id.alarmLayout)    LinearLayout llAlarm;
    @BindView(R.id.alarmTimeSeekbar) org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
            seekBarTime;
    @BindView(R.id.alarmTemperatureSeekbar) org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
            seekBarTemp;
    @BindView(R.id.timeAlarm) TextView timeAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController,
                mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //Set temperature alarm click listener
        bSetTemperatureAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
//                currentCazuela.setTemperatureAlarm(seekBarTemp.getProgress());
                if(seekBarTemp.getProgress()<1){

                }else {
                    temperatureThreshold.setText(Html.fromHtml("<b>Alarma límite Tª: </b>" +
                            seekBarTemp.getProgress() + "ºC"));
                }
            }
        });

        //Set temperature alarm click listener
        bSetTimeAlarm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//                currentCazuela.setTimeAlarm(seekBarTime.getProgress());
                timeAlarm.setText(Html.fromHtml("<b>Tiempo restante:</b> "
                        + " -- ") + "min.");
                runTimer(seekBarTime.getProgress()*1000);//cambiar por countdown
            }
        });

    }

//    Cosas del temporizador -----0-----------------
    private void runTimer(int tiempo) {
        if (!timelapseRunning) {
            timelapseRunning = true;
        } else {
            timelapseRunning = false;

        }
        if(NUM_OF_COUNT == 0){
            NUM_OF_COUNT = tiempo;
        }
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                timeAlarm.setText(Html.fromHtml("<b>Tiempo restante:</b> "
                        + NUM_OF_COUNT ) + "min.");
                if (!timelapseRunning && NUM_OF_COUNT>0) {
                    NUM_OF_COUNT --;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

//    --------------------0------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
