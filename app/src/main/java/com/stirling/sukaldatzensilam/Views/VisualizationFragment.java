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
import android.content.pm.PackageManager;
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

import android.app.Service;
//import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
//import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import com.libRG.CustomTextView;
import com.stirling.sukaldatzensilam.Models.BluetoothLE;
import com.stirling.sukaldatzensilam.R;
import com.stirling.sukaldatzensilam.Utils.BleCallback;
import com.stirling.sukaldatzensilam.Utils.BluetoothLEHelper;
import com.stirling.sukaldatzensilam.Utils.Notifications;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;
import static android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED;
import static android.support.v4.content.ContextCompat.getSystemService;
import static java.lang.Thread.sleep;

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
    private String tempString;
    private float mil = 0;
    private Handler handler;
    private int minutosTemp = 0;
    private long millisCounter = 0;
    private String macbt;
    private boolean mScanning;
    Runnable runnable;
    private Handler mHandler;

    /*
        BluetoothDevice selDevice;
        private BleCallback bleCallback;
        BluetoothLEHelper ble;
        */
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothGattCallback mGattCallback ;
    BluetoothGatt mBluetoothGatt;
    BluetoothDevice bDevice;
    private static final int REQUEST_ENABLE_BT = 1;


    private String charUUID = "f547a45c-5264-486a-b107-11db9099ded0"; //"BEB5483E-36E1-4688-B7F5-EA07361B26A8";

    private int position = -1;
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
        scanLeDevice(false);

    }
    // Device scan callback.
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

    private void scanLeDevice(final boolean enable) {
        if (enable && !mScanning) {
//            Utils.toast(ma.getApplicationContext(), "Starting BLE scan...");

            Log.i("scanLeDeviceFrag: ", "Starting BLE Scan....");

            mHandler = new Handler();
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    Utils.toast(ma.getApplicationContext(), "Stopping BLE scan...");

                    Log.i("scanLeDeviceFrag: ", "Stopping BLE scan...");


                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                }
            }, 3*1000);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
//            mBluetoothAdapter.startLeScan(uuids, mLeScanCallback);
        }
        else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }


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
    public int convertByteToInt(byte[] b)
    {
        int value= 0;
        for(int i=0; i<b.length; i++)
            value = (value << 8) | b[i];
        return value;
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

        //SharedPreferences
        preferences = getActivity().getBaseContext().getSharedPreferences("preferencias",
                Context.MODE_PRIVATE);

        final Handler handler = new Handler();
        /* your code here */
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


    //Actualizar temperatura con la obtenida mediante bluetooth
    public void actualizarTemperatura(){
        macbt = preferences.getString("macbt", null);
        /*if(macbt!=null && !mScanning){
            scanLeDevice(true);
        }*/
        tvTemperature.setText("-- ºC");
        tvTemperature.setText(temp + " ºC");
        /*try{
            macbt = preferences.getString("macbt", null);
        }catch (Exception e){
            Log.i("Obteniendo MAC disp. BT:==> ", "MAC: " + macbt);
        }*/
        /*if(macbt == null) { //comprobamos si se ha obtenido la mac del disp. bt
            Log.i("MAC disp bt", "No se ha obtenido ninguna mac " + macbt);
        }else{*/


            /*try{
                if (!ble.isConnected()) { //comprobamos si se ha conectado al disp. bt
                    busquedaBT(macbt);
                    Log.i("busqBT:", "ble.isConected, conectado");
                } else {
                    if(checkIfBleIsConnected(ble)){
                        ble.read(serviceUUID, charUUID);

                    }
                }
            }catch (Exception e){
                Log.e("busquedaBT", "No se ha podido conectar a: " + macbt);
                Log.e("busquedaBT", "Error: " + e);

            }*/
//        }
    }


    //    private BleCallback bleCallbacks(){
//
//        return new BleCallback() {
//
//            @Override
//            public void onBleConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//                super.onBleConnectionStateChange(gatt, status, newState);
//
//                if (newState == BluetoothProfile.STATE_CONNECTED) {
//                    Toast.makeText(getActivity(), "Conectado a serv. Bluetooth Gatt",
//                            Toast.LENGTH_SHORT).show();
//                }
//                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                    Toast.makeText(getActivity(), "Desconectado del serv. Bluetooth Gatt.",
//                            Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onBleServiceDiscovered(BluetoothGatt gatt, int status) {
//                super.onBleServiceDiscovered(gatt, status);
//                if (status != BluetoothGatt.GATT_SUCCESS) {
//                    Log.e("Ble ServiceDiscovered", "onServicesDiscovered received: "
//                            + status);
//                }
//            }
//
//            @Override
//            public void onBleCharacteristicChange(BluetoothGatt gatt,
//                                                  BluetoothGattCharacteristic characteristic) {
//                super.onBleCharacteristicChange(gatt, characteristic);
//                Log.i("BluetoothLEHelper", "onCharacteristicChanged Value: "
//                        + Arrays.toString(characteristic.getValue()));
//            }
//
//            @Override
//            public void onBleRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//                super.onBleRead(gatt, characteristic, status);
//                //Intentar obtener temperatura grabada en característica BLE
//                /*String tempObtenida = Arrays.toString(characteristic.getValue());
//                //Probamos a escribirla por terminal
//                System.out.println("1=======> Valor obtenido BLE: " + tempObtenida);
//                temp = Integer.parseInt(tempObtenida);*/
//            }
//
//        };
//    }
    //Encender contador que funciona si 'true' durante X minutos establecidos en var. minutosTemp.
    public void arrancarTimer(){
        millisCounter = minutosTemp*60*1000; //Trabajamos con millisegundos
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
        try {
            if (temp < temp1) {
                tvTemperature.setBackgroundColor(getContext().getColor(R.color.tempVerde));
            } else if (temp1 < temp && temp < temp2) {
                tvTemperature.setBackgroundColor(getContext().getColor(R.color.tempAmarillo));
            } else if (temp < temp3) {
                tvTemperature.setBackgroundColor(getContext().getColor(R.color.tempRojo));
            } else {
                if(temp==0){
                    tvTemperature.setBackgroundColor(getContext()
                            .getColor(R.color.material_grey300));
                }
                tvTemperature.setBackgroundColor(getContext().getColor(R.color.material_grey300));
            }
        }catch (Exception e){

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


