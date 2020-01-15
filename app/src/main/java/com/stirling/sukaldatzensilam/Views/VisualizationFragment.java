package com.stirling.sukaldatzensilam.Views;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.libRG.CustomTextView;
import com.stirling.sukaldatzensilam.Models.BluetoothLE;
import com.stirling.sukaldatzensilam.R;
import com.stirling.sukaldatzensilam.Utils.BleCallback;
import com.stirling.sukaldatzensilam.Utils.BluetoothLEHelper;
import com.stirling.sukaldatzensilam.Utils.Notifications;

import java.util.ArrayList;
import java.util.Arrays;
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

    SharedPreferences preferences;

    private int NUM_OF_COUNT;
    private boolean seguir = false;
    private boolean rCorriendo = false;
    private int temp;
    private float mil = 0;
    private Handler handler;
    private int minutosTemp = 0;
    private long millisCounter = 0;
    private String macbt;
    Runnable runnable;

    BluetoothDevice selDevice;
    private BleCallback bleCallback;
    BluetoothLEHelper ble;

    private ArrayList<String> arListEncont;
    private ArrayList<BluetoothLE> arBLEEncont;

    private String serviceUUID = getActivity().getResources()
            .getString(R.string.SERVICE_UUID_VALORTEMP);
    private String charUUID = getActivity().getResources()
            .getString(R.string.CHARACTERISTIC_UUID_VALORTEMP);

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

        ble = new BluetoothLEHelper(getActivity());

        //inicializ. array de dispositivos encontrados
        arListEncont = new ArrayList<String>();

        bleCallback = new BleCallback(){

            @Override
            public void onBleConnectionStateChange(BluetoothGatt gatt, int status, int newState){
                super.onBleConnectionStateChange(gatt, status, newState);

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Toast.makeText(getActivity(),"Conectado a serv. Bluetooth Gatt",
                            Toast.LENGTH_SHORT).show();
                }
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Toast.makeText(getActivity(),"Desconectado del serv. Bluetooth Gatt.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBleServiceDiscovered(BluetoothGatt gatt, int status) {
                super.onBleServiceDiscovered(gatt, status);
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.e("Ble ServiceDiscovered","onServicesDiscovered received: "
                            + status);
                }
            }

            @Override
            public void onBleCharacteristicChange(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic) {
                super.onBleCharacteristicChange(gatt, characteristic);
                Log.i("BluetoothLEHelper","onCharacteristicChanged Value: "
                        + Arrays.toString(characteristic.getValue()));
            }

            @Override
            public void onBleRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onBleRead(gatt, characteristic, status);
                //Intentar obtener temperatura grabada en característica BLE
                String tempObtenida = Arrays.toString(characteristic.getValue());
                //Probamos a escribirla por terminal
                System.out.println("1=======> Valor obtenido BLE: " + tempObtenida);
                temp = Integer.parseInt(tempObtenida);
            }


        };



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

        final Handler handler = new Handler();
        /* your code here */
        new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 3 * 1000); // cada 3 segundos
                //lo que queremos que haga cada tres segundos
                actualizarTemperatura();
                actualizarColor();
            }
        }.run();

        //SharedPreferences
        preferences = getActivity().getBaseContext().getSharedPreferences("preferencias",
                Context.MODE_PRIVATE);


    }
    //Encender contador que funciona si 'true' durante X minutos establecidos en var. minutosTemp.
    public void arrancarTimer(){
        millisCounter = minutosTemp*60*1000; //Trabajamos con millisegundos
        mil = 0;
        new unCountDown(millisCounter, 1000).start();
    }
    //Detener el timer
    public void pararTimer(){
    }

    //Búsqueda disp. bt
    public void busquedaBT(){
        if(ble.isReadyForScan()){
            Handler mHandler = new Handler();
            //comienza el escaneo
            ble.scanLeDevice(true);
            //Cuando finaliza el escaneo;
            mHandler.postDelayed(() -> {
                //Obtenemos lista de dispositvios encontrados
                arBLEEncont = ble.getListDevices();
                //Convertimos a String para poder mostrarlos en la ListView de disp. encont.
            }, ble.getScanPeriod());

            for (BluetoothLE bte : arBLEEncont){
                if(bte.getMacAddress().equals(macbt)){ //compr. si coincide con el que queremos con.
                    selDevice = bte.getDevice();
                    break;
                }
            }
            if(selDevice!=null){
                ble.connect(selDevice, bleCallback); //conectamos con el disp.
            }
        }
    }

    //Actualizar temperatura con la obtenida mediante bluetooth
    public void actualizarTemperatura(){
        tvTemperature.setText("-- ºC");
        temp = 0;
        macbt = "";
        try{
            macbt = preferences.getString("macbt", null);
        }catch (Exception e){
            Log.i("Obteniendo MAC disp. BT:==> ", "MAC: " + macbt);
        }
        if(macbt.equals("")) { //comprobamos si se ha obtenido la mac del disp. bt
            Log.i("MAC disp bt", "No se ha obtenido ninguna mac " + macbt);
        }else{
            if (!ble.isConnected()) { //comprobamos si se ha conectado al disp. bt
                busquedaBT();
            } else {
                if(checkIfBleIsConnected(ble)){
                    ble.read(serviceUUID,charUUID);

                }
            }
        }
    }
    //Actualiza el color del círculo en el que se muestra la temperatura
    public void actualizarColor(){

        int temp1 = getResources().getInteger(R.integer.tempVerdeMenorQue);
        int temp2 = getResources().getInteger(R.integer.tempAmarillaMayVerMenQue);
        int temp3 = getResources().getInteger(R.integer.tempRojaMayorQue);
        try {
            if (temp < temp1) {
                tvTemperature.setBackgroundColor(getContext().getColor(R.color.tempVerde));
            } else if (temp1 < temp && temp < temp2) {
                tvTemperature.setBackgroundColor(getContext().getColor(R.color.tempAmarillo));
            } else if (temp < temp3) {
                tvTemperature.setBackgroundColor(getContext().getColor(R.color.tempRojo));
            } else {
                tvTemperature.setBackgroundColor(getContext().getColor(R.color.material_grey300));
            }
        }catch (Exception e){

        }
    }

    private boolean checkIfBleIsConnected(BluetoothLEHelper bluetoothLEHelper){
        if(bluetoothLEHelper.isConnected()){
            Log.i("isConnected","---->El dispositivo está conectado<----");
            return true;
        }else{
            Log.i("isConnected", "--->El dispositivo no está conectado<---");
            return false;
        }
    }

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


