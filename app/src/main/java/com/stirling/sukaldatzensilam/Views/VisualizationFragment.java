package com.stirling.sukaldatzensilam.Views;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.libRG.CustomTextView;
import com.stirling.sukaldatzensilam.R;
import com.stirling.sukaldatzensilam.Utils.Notifications;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VisualizationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VisualizationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VisualizationFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private int NUM_OF_COUNT;
    private boolean seguir = false;
    private boolean rCorriendo = false;
    private float temp;
    private float mil = 0;
    private Handler handler;
    private int minutosTemp = 0;
    private long millisCounter = 0;
    Runnable runnable;

    private int position = -1;
    int[] imageArray = { R.drawable.vacio, R.drawable.frio, R.drawable.caliente };
    String [] tempArray = {"0ºC", "20ºC", "70ºC"};

    @BindView(R.id.bSetAlarm) TextView bSetTemperatureAlarm;
    @BindView(R.id.temperatureThreshold) TextView temperatureThreshold;
    @BindView(R.id.bSetTime) TextView bSetTimeAlarm;
    @BindView(R.id.alarmLayout)    LinearLayout llAlarm;
    @BindView(R.id.alarmTimeSeekbar) org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
            seekBarTime;
    @BindView(R.id.alarmTemperatureSeekbar) org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
            seekBarTemp;
    @BindView(R.id.timeAlarm)    TextView timeAlarm;
    @BindView(R.id.imgTupper2) ImageView MyImageView;
    @BindView(R.id.temperatureIndicator) CustomTextView tvTemperature;



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VisualizationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VisualizationFragment newInstance(String param1, String param2) {
        VisualizationFragment fragment = new VisualizationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }*/

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.visualization_fragment, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstancestate){
        super.onViewCreated(view, savedInstancestate);

        ButterKnife.bind(this, view);

        //Limpiar temperatura anterior
        tvTemperature.setText("-- ºC");

        //Boton poner alamra temperatura
        bSetTemperatureAlarm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(bSetTemperatureAlarm.getText().equals("Activar")){ //Activar alarma
                    temperatureThreshold.setText(Html.fromHtml("<b>Temperatura límite: </b>" +
                            seekBarTemp.getProgress() + "ºC"));
                    bSetTemperatureAlarm.setText("Desactivar");
                }else{ //Desactivar alarma
                    temperatureThreshold.setText(Html.fromHtml("<b> </b>"));
                    bSetTemperatureAlarm.setText("Activar");
                }
            }
        });
        //Boton poner temporizador
        bSetTimeAlarm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(bSetTimeAlarm.getText().toString().equals("Activar")){ //Activar temporizador
                    timeAlarm.setText(Html.fromHtml("<b>Tiempo restante:</b> " +
                            seekBarTime.getProgress() + "min."));
                    if(!rCorriendo){
                        seguir = true;
                    }else{
                        pararTimer();
                    }
                    minutosTemp = seekBarTime.getProgress();
                    bSetTimeAlarm.setText("Desactivar");
                    seguir = true;
                    arrancarTimer();
                }else{ //Desactivar temporizador
                    pararTimer();
                    bSetTimeAlarm.setText("Activar");
                    seguir = false;
                    timeAlarm.setText(Html.fromHtml(" "));
                }
                timeAlarm.setText(Html.fromHtml("<b>Tiempo restante:</b> " +
                        seekBarTime.getProgress() + "min."));

            }
        });

    }
    //Encender contador que funciona si 'true' durante X minutos establecidos en var. minutosTemp.
    public void arrancarTimer(){
        millisCounter = minutosTemp*60*1000; //Trabajamos con millisegundos
//        handler = new Handler();
//        seguir = true;
       /* runnable = new Runnable() {
            public void run(){
                handler.postDelayed(this, 1000);

                mil = 0;
                try {
                    if(millisCounter <=1000){
                            pararTimer();
                    }else{
                        if(seguir){
                            millisCounter = millisCounter - 1000;
                            mil = millisCounter /60 / 1000;
                            seekBarTime.setProgress(Math.round(mil));
                        }else{
                            pararTimer();
                        }
                    }

                    timeAlarm.setText(Html.fromHtml("<b>Tiempo restante:</b> " +
                            Math.round(mil) + "min."));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };*/
       mil = 0;
        new unCountDown(millisCounter, 1000).start();
    }
    //Detener el timer
    public void pararTimer(){
    }
    //Actualiza el color del círculo en el que se muestra la temperatura
    public void actualizarColor(){

        int temp1 = getResources().getInteger(R.integer.tempVerdeMenorQue);
        int temp2 = getResources().getInteger(R.integer.tempAmarillaMayVerMenQue);
        int temp3 = getResources().getInteger(R.integer.tempRojaMayorQue);
        if(temp < temp1){
            tvTemperature.setBackgroundColor(getContext().getColor(R.color.tempVerde));
        }else if(temp1 < temp && temp < temp2){
            tvTemperature.setBackgroundColor(getContext().getColor(R.color.tempAmarillo));
        }else if(temp < temp3){
            tvTemperature.setBackgroundColor(getContext().getColor(R.color.tempRojo));
        }else{
            tvTemperature.setBackgroundColor(getContext().getColor(R.color.material_grey300));
        }
    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public class unCountDown extends CountDownTimer {

        public unCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            seekBarTime.setMax((int) millisInFuture);
        }

        @Override
        public void onFinish() {
            timeAlarm.setText(Html.fromHtml("<b>Tiempo restante:</b> " +
                     "Finalizado"));
            seekBarTime.setMax(30);

            /*//Este intent se abrirá cuando se pulse la notificación
            Intent intentNotif = new Intent(getActivity(), MainUserActivity.class);
            PendingIntent pendingintent = PendingIntent
                    .getActivity(getActivity(), 1001, intentNotif, 0);
            //Notificación
            NotificationCompat.Builder builder = new NotificationCompat
                    .Builder(getActivity(), CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher_iconolla_foreground_round)
                    .setContentTitle("SukaldatzenSILAM")
                    .setContentText("Temporizador finalizado.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSound(RingtoneManager.getDefaultUri((RingtoneManager.TYPE_ALARM)))
                    .setVibrate(new long[]{ 500,500,500,500})
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentIntent(pendingintent);

            //Construimos notificación con las características del builder
            Notification notification = builder.build();

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat
                    .from(getActivity());
            //Disparamos notificación
            notificationManagerCompat.notify(NOTIFICATION_ID, notification);*/

            Notifications.show(getActivity(), VisualizationFragment.class,
                    "Temporizador tupper", "El temporizador ha finalizado.");
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if(!seguir){
                this.cancel();
                seekBarTime.setMax(30);
            }
            millisCounter = millisCounter - 1000;
            mil = millisCounter /60 / 1000;
            timeAlarm.setText(Html.fromHtml("<b>Tiempo restante:</b> " +
                    Math.round(mil) + "min."));
            seekBarTime.setProgress(Math.round(millisUntilFinished));
            long timeRemaining = millisUntilFinished;
            seekBarTime.setProgress((int) (timeRemaining));
            //Log.i(TAG, "Time tick: " + millisUntilFinished);
        }
    }
}


