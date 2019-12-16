package com.stirling.sukaldatzensilam.Views;

import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.stirling.sukaldatzensilam.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


import static android.content.ContentValues.TAG;

//I: Search API de Elasticsearch

public class VisualizationFragment extends Fragment
{
    private static final String BASE_URL = "http://10.128.0.104:9200/";

    static SharedPreferences sharedPreferences;
//    private static SharedPreferences.Editor editor = sharedPreferences.edit();


    private String macCurrentCazuela;
    private String mIndice = "";
    private String mAccion = "";


    private String elCorreo = "";
    private String queryJson = "";
    private JSONObject jsonObject;

    private float tempOlla;
    private int lastX;

    private final static int INTERVAL = 3500;

    private PopupWindow popupWindow;
    private Button botonAceptar;
    private Button botonCancelar;

    private int position = -1;

    ImageView MyImageView ;
    int[] imageArray = { R.drawable.vacio, R.drawable.frio, R.drawable.caliente};


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        return inflater.inflate(R.layout.visualization_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        sharedPreferences = this.getActivity().getSharedPreferences("navprefs",
//                Context.MODE_PRIVATE);
        //enPrueba();
        //actualizarTemperatura();

        //Inicializamos el gráfico
//        iniciarGrafico(graphView);

        MyImageView = (ImageView) getActivity().findViewById(R.id.imgTupper);
        cambiarImagen();


        final Handler handler = new Handler();
        /* your code here */
        new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 2 * 1000); // every 2 seconds
                //lo que queremos que haga cada dos segundos
//                actualizarGrafico();
            }
        }.run();

    }

    public void cambiarImagen(){

        /**
         * This timer will call each of the seconds.
         */
        Timer mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // As timer is not a Main/UI thread need to do all UI task on runOnUiThread
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // increase your position so new image will show
                        position++;
                        // check whether position increased to length then set it to 0
                        // so it will show images in circuler
                        if (position >= imageArray.length)
                            position = 0;
                        // Set Image
                        MyImageView.setImageResource(imageArray[position]);
                    }
                });
            }
        }, 0, 3000);
// where 0 is for start now and 3000 (3 second) is for interval to change image as you mentioned in question
    }


}
