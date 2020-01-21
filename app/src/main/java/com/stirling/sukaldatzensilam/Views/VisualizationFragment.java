/**
 * Iñaki Zorrilla 2019
 */

package com.stirling.sukaldatzensilam.Views;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.libRG.CustomTextView;
import com.stirling.sukaldatzensilam.R;
import com.stirling.sukaldatzensilam.Utils.Notifications;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import static java.lang.Thread.sleep;

public class VisualizationFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    SharedPreferences preferences;

    private int NUM_OF_COUNT;
    private boolean seguir = false;
    private boolean rCorriendo = false;
    private int temp;
    private String tempString;
    private float mil = 0;
    private Handler handler;
    private int minutosTemp = 0;
    private long millisCounter = 0;
    private String macbt;
    private boolean mScanning;
    Runnable runnable;
    private Handler mHandler;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothGatt mBluetoothGatt;
    BluetoothDevice bDevice;
    private static final int REQUEST_ENABLE_BT = 1;
    private String charUUID = "f547a45c-5264-486a-b107-11db9099ded0"; //"BEB5483E-36E1-4688-B7F5-EA07361B26A8";

    int[] imageArray = { R.drawable.vacio, R.drawable.frio, R.drawable.caliente };

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences
                preferences = getActivity().getBaseContext().getSharedPreferences("preferencias",
                Context.MODE_PRIVATE);
        macbt = preferences.getString("macbt", null);
        //Checkear que el bt esté encendido, y si no diálogo pidiendo encenderlo
        if(!mBluetoothAdapter.isEnabled()){
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
            alertBuilder.setCancelable(true);
            alertBuilder.setCancelable(true);
            alertBuilder.setMessage("¿Quiere habilitar el Bluetooth?");
            alertBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    mBluetoothAdapter.enable();
                }
            });
            AlertDialog alert = alertBuilder.create();
            alert.show();
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Bluetooth is supported?
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "BluetoothAdapter no soportado", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        macbt = preferences.getString("macbt", null);
        // Bluetooth is enabled?
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if (macbt != null && !isScanning() && bDevice==null) {
            scanLeDevice(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        scanLeDevice(false); //porque se pondrá a escanear la activity BluetoothAct.
    }
    /**
     * Callback, sucede al terminar de escanear
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    new Runnable() {
                        @Override
                        public void run () {
                            Log.i("BLEFrag", "Name: " + device.getName() + " (" + device.getAddress() + ")");
                            String deviceAddress = device.getAddress();
                            if (deviceAddress.equals(macbt)) {
                                connectToDevice(device);
                            }
                        }
                    }.run();
                }
            };

    /**
     * Método encargado de escanear en busca de dispositivos Bluetooth
     * Low Energy.
     *
     * @param enable Parameter 1.
     */
    private void scanLeDevice(final boolean enable) {
        if (enable && !mScanning) {
            Log.i("scanLeDeviceFrag: ", "Starting BLE Scan....");
            mHandler = new Handler();
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i("scanLeDeviceFrag: ", "Stopping BLE scan...");
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, 3*1000);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
        else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /**
     * Método utilizado para conectarse a un dispositivo BT LE.
     *
     * @param device dispositivo BT al que conectarse.
     */
    public void connectToDevice(BluetoothDevice device) {
        if (mBluetoothGatt == null) {
            Log.i("BLEFrag", "Attempting to connect to device " + device.getName() + " (" + device.getAddress() + ")");
            mBluetoothGatt = device.connectGatt(getActivity(), true, gattCallback);
            try {
                sleep(600);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            bDevice = device;
            scanLeDevice(false);// will stop after first device detection
        }
    }

    public boolean isScanning() {
        return mScanning;
    }

    /**
     * Callbacks que suceden en determinados eventos relacionados con el BT LE.
     */
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("BLEFrag", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("BLEFrag", "STATE_CONNECTED");
                    //BluetoothDevice device = gatt.getDevice(); // Get device
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("BLEFrag", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("BLEFrag", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("BLEFrag", "Services: " + services.toString());
            BluetoothGattCharacteristic characTemp = null;
            for(BluetoothGattService ser : services){
                //if(ser.getUuid().equals(serviceUUID)){
                BluetoothGattService gattService = services.get(2);
                characTemp = gattService.getCharacteristic(UUID.fromString(charUUID));
                    try{
                        gatt.readCharacteristic(characTemp);
                        gatt.setCharacteristicNotification(characTemp, true);
                        try {
                            sleep(600);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Log.e("onServDiscFrag", "Error sleep: "+ e);
                        }
                    }catch (Exception e){
                        Log.e("readFrag", "Error: " + e);
                    }
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            // my attempt to read and print characteristics
            Log.i("infoFrag", "onCharacteristicRead");
            tempString = characteristic.getStringValue(0);
            temp = Integer.parseInt(tempString);
            Log.i("BLEFragOnCharRead", "Characteristic: " + tempString); //dataInput
            //gatt.disconnect();
        }
        @Override
        public synchronized void onCharacteristicChanged(BluetoothGatt gatt,
                                                         BluetoothGattCharacteristic characteristic) {

            Log.i("infoFrag", "onCharacteristicChanged");
            gatt.discoverServices();
            tempString = characteristic.getStringValue(0);
            temp = Integer.parseInt(tempString);
            Log.i("BLEFragOnCharChanged", "Characteristic: " + tempString);

        }
    };

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
        /**
         * Listener del botón de alarma por cuenta atrás
         *
         * @return una notificación al terminar la cuenta atrás
         */
        bSetTimeAlarm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(bSetTimeAlarm.getText().toString().equals("Activar")){ //Activar temporizador
                    timeAlarm.setText(Html.fromHtml("<b>Tiempo restante:</b> " +
                            seekBarTime.getProgress() + "min."));
                    if(!rCorriendo){
                        seguir = true;
                    }else{
                        //Detener
                    }
                    minutosTemp = seekBarTime.getProgress();
                    bSetTimeAlarm.setText("Desactivar");
                    seguir = true;
                    arrancarTimer();
                }else{ //Desactivar temporizador
                    bSetTimeAlarm.setText("Activar");
                    seguir = false;
                    timeAlarm.setText(Html.fromHtml(" "));
                }
                timeAlarm.setText(Html.fromHtml("<b>Tiempo restante:</b> " +
                        seekBarTime.getProgress() + "min."));

            }
        });

        //SharedPreferences
        preferences = getActivity().getBaseContext().getSharedPreferences("preferencias",
                Context.MODE_PRIVATE);

        final Handler handler = new Handler();
        new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 3 * 1000); // cada 3 segundos
                //lo que queremos que haga cada tres segundos
                Log.i("info", "Loop de 3 segundos");
                actualizarTemperatura();
                actualizarColor();
            }
        }.run();
    }
    public void ponerANull(){
        macbt = null;
    }


    /**
     * Se actualiza la temperatura mostrada en pantalla según los datos recibidos por BT
     */
    public void actualizarTemperatura(){
        macbt = preferences.getString("macbt", null);
        tvTemperature.setText("-- ºC");
        tvTemperature.setText(temp + " ºC");
    }

    /**
     * Timer utilizado en la alarma por cuenta atrás
     */
    public void arrancarTimer(){
        millisCounter = minutosTemp*60*1000; //Trabajamos con millisegundos
        mil = 0;
        new unCountDown(millisCounter, 1000).start();
    }

    /**
     * Se actualiza el color del círculo que contiene el valor de la temperatura
     */
    public void actualizarColor(){

        int temp1 = getResources().getInteger(R.integer.tempVerdeMenorQue);
        int temp2 = getResources().getInteger(R.integer.tempAmarillaMayVerMenQue);
        int temp3 = getResources().getInteger(R.integer.tempRojaMayorQue);
        try {
            if (temp < temp1) {
                tvTemperature.setBackgroundColor(getContext().getColor(R.color.tempVerde));
                if(temp==0) {
                    tvTemperature.setBackgroundColor(getContext()
                            .getColor(R.color.material_grey300));
                }
            } else if (temp1 <= temp && temp < temp2) {
                tvTemperature.setBackgroundColor(getContext().getColor(R.color.tempAmarillo));
            } else if (temp <= temp3) {
                tvTemperature.setBackgroundColor(getContext().getColor(R.color.tempRojo));
            } else {
                if(temp==0){
                    tvTemperature.setBackgroundColor(getContext()
                            .getColor(R.color.material_grey300));
                }
                tvTemperature.setBackgroundColor(getContext().getColor(R.color.material_grey300));
            }
        }catch (Exception e){
            Log.e("Actual. Color: ", "Excep.: " + e);
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


