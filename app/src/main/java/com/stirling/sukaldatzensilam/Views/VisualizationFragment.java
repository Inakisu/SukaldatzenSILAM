/**
 * Iñaki Zorrilla 2019
 */

package com.stirling.sukaldatzensilam.Views;

import android.app.Activity;
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
import android.content.res.Resources;
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

import com.google.firebase.auth.FirebaseAuth;
import com.libRG.CustomTextView;
import com.stirling.sukaldatzensilam.Models.POJOs.RespuestaU;
import com.stirling.sukaldatzensilam.R;
import com.stirling.sukaldatzensilam.Utils.BluetoothLEHelper;
import com.stirling.sukaldatzensilam.Utils.Constants;
import com.stirling.sukaldatzensilam.Utils.ElasticSearchAPI;
import com.stirling.sukaldatzensilam.Utils.Notifications;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Credentials;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;
import static java.lang.Thread.sleep;

public class VisualizationFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    SharedPreferences preferences;
    public List<BluetoothGattCharacteristic> listaChars = new ArrayList<>();
    private boolean alTAct = false;
    private boolean seguir = false;
    private boolean rCorriendo = false;
    private int temp;
    private String tempString;

    private float mil = 0;
    private Handler handler;
    private int minutosTemp = 0;
    private long millisCounter = 0;
    private String macbt = null;
    private boolean mScanning;
    Runnable runnable;
    private Handler mHandler;
    private int t;
    private boolean girado;
    private boolean lleno;

    private boolean cambioConectado;
    private boolean cambioDesconectado;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothGatt mBluetoothGatt;
    BluetoothDevice bDevice;
    BluetoothGattService gattServiceD;

    private static final int REQUEST_ENABLE_BT = 1;
    private String charUUID = "f547a45c-5264-486a-b107-11db9099ded0"; //"BEB5483E-36E1-4688-B7F5-EA07361B26A8";
    private String charUUID2 = "f547a45c-5264-486a-b107-11db9099ded2"; //"BEB5483E-36E1-4688-B7F5-EA07361B26A8";
    private String charUUID3 = "f547a45c-5264-486a-b107-11db9099ded3"; //"BEB5483E-36E1-4688-B7F5-EA07361B26A8";
    private FirebaseAuth mAuth;
    private Retrofit retrofit;
    private ElasticSearchAPI searchAPI;
    private String mElasticSearchPassword = Constants.elasticPassword;
    private String elCorreo = "";
    private String queryJson = "";
    private JSONObject jsonObject;
    private String correoUsuario;
    public Activity laActivity;

    @BindView(R.id.bSetAlarm) TextView bSetTemperatureAlarm;
    @BindView(R.id.temperatureThreshold) TextView temperatureThreshold;
    @BindView(R.id.bSetTime) TextView bSetTimeAlarm;
    @BindView(R.id.alarmLayout)    LinearLayout llAlarm;
    @BindView(R.id.alarmTimeSeekbar) org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
            seekBarTime;
    @BindView(R.id.alarmTemperatureSeekbar) org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
            seekBarTemp;
    @BindView(R.id.timeAlarm)    TextView timeAlarm;
    @BindView(R.id.imgTupper2) ImageView tupperVacio;
    @BindView(R.id.imgTupper2girado) ImageView tupperVacioGirado;
    @BindView(R.id.imgTupperLlenoFrio) ImageView tupperFrio;
    @BindView(R.id.imgTupperLlenoCaliente) ImageView tupperCaliente;
    @BindView(R.id.temperatureIndicator) CustomTextView tvTemperature;
    @BindView(R.id.imageBtConnect) ImageView imageBtConect;
    private CountDownTimer countDownTimer;

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
        t = 0;
        temp = 0;
        laActivity = getActivity();
        macbt = preferences.getString("macbt", null);
        //Inicializar API
        inicializarAPI();
        //obtener correo del usuario logueado
        mAuth = FirebaseAuth.getInstance();
        correoUsuario = mAuth.getCurrentUser().getEmail();
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
     * Método encargado de escanear dispositivos Bluetooth Low Energy
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
            Log.i("BLEFrag", "Attempting to connect to device " + device.getName() +
                    " (" + device.getAddress() + ")");
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
                    cambioConectado = true;
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    if (macbt != null && !isScanning() && bDevice==null) {
                        scanLeDevice(true);
                    }
                    cambioDesconectado = true;
                    tvTemperature.setText("-- ºC");
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
            BluetoothGattCharacteristic characGiro = null;
            BluetoothGattCharacteristic characLleno = null;

            gattServiceD = services.get(2);

            characLleno = gattServiceD.getCharacteristic(UUID.fromString(charUUID2));
            characGiro = gattServiceD.getCharacteristic(UUID.fromString(charUUID3));
            characTemp = gattServiceD.getCharacteristic(UUID.fromString(charUUID));
            listaChars.add(characTemp);
            listaChars.add(characGiro);
            listaChars.add(characLleno);

            requestCharacteristics(gatt);

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharRead","Entrado === BBBBB");

            if(characteristic.getUuid().toString().equals(charUUID)){
                Log.i("onCharRead","charUUID === BBBBB");
                tempString = characteristic.getStringValue(0);
                temp = Integer.parseInt(tempString);
                Log.i("BLEFragOnCharRead", "Characteristic: " + tempString); //dataInput
            }else if(characteristic.getUuid().toString().equals(charUUID2)){
                Log.i("onCharRead","charUUID2 === BBBBB");

                lleno = characteristic.getStringValue(0).equals("1");
                Log.i("BLEFragOnCharRead", "Characteristic: " + characteristic.getStringValue(0)); //dataInput

            }else if(characteristic.getUuid().toString().equals(charUUID3)){
                Log.i("onCharRead","charUUID3 === BBBBB");
                girado = characteristic.getStringValue(0).equals("1");
                Log.i("BLEFragOnCharRead", "Characteristic: " + characteristic.getStringValue(0)); //dataInput
            }else{
                Log.e("BLE Read: ","Ninguna característica coincide con el UUID " +
                        "proporcionado");
            }

            listaChars.remove(listaChars.get(listaChars.size() - 1));

            if (listaChars.size() > 0) {
                requestCharacteristics(gatt);
            }else{
                //gatt.disconnect();
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
                gatt.discoverServices();
            }
        }
        public void requestCharacteristics(BluetoothGatt gatt) {
            gatt.readCharacteristic(listaChars.get(listaChars.size()-1));
            Log.i("requestCharRead","Llamado === RRRRRRR");
        }
        @Override
        public synchronized void onCharacteristicChanged(BluetoothGatt gatt,
                                                         BluetoothGattCharacteristic characteristic) {

            Log.i("infoFrag", "onCharacteristicChanged");
            gatt.discoverServices();
            Log.i("BLEFragOnCharChanged", "Characteristic: " + tempString);

        }
    };

    private void toastConectado() {
            if(cambioConectado){
                imageBtConect.setImageResource(R.drawable.ic_btconect);
                Toast.makeText(laActivity, "Tupper CONECTADO", Toast.LENGTH_SHORT).show();
                cambioConectado = false;
            }
            if (cambioDesconectado){
                imageBtConect.setImageResource(R.drawable.ic_btdesconect);
                Toast.makeText(laActivity, "Tupper DESCONECTADO", Toast.LENGTH_SHORT).show();
                cambioDesconectado = false;
                temp = 0;
                tvTemperature.setText("-- ºC");
            }
    }

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
        //Inicializamos imágenes
        tupperVacio.setVisibility(View.VISIBLE);
        tupperFrio.setVisibility(View.GONE);
        tupperCaliente.setVisibility(View.GONE);

        //Boton poner alamra temperatura
        bSetTemperatureAlarm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(bSetTemperatureAlarm.getText().toString().equals("Activar")){ //Activar alarma
                    temperatureThreshold.setText(Html.fromHtml("<b>Temperatura límite: </b>" +
                            seekBarTemp.getProgress() + "ºC"));
                    bSetTemperatureAlarm.setText("Desactivar");
                    alTAct = true;
                }else{ //Desactivar alarma
                    alTAct = false;
                    temperatureThreshold.setText(Html.fromHtml("<b> </b>"));
                    bSetTemperatureAlarm.setText("Activar");
                    countDownTimer.cancel();
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
                    seekBarTime.setProgress(0);
                }
            }
        });

        //SharedPreferences
        preferences = getActivity().getBaseContext().getSharedPreferences("preferencias",
                Context.MODE_PRIVATE);

        final Handler handler = new Handler();
        new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 2 * 1000); // cada 2 segundos
                //lo que queremos que haga cada tres segundos
                Log.i("info", "Loop de 3 segundos");
                comprobarAlarmaT(alTAct);
                actualizarTemperatura();
                actualizarColor();
                toastConectado();
                verificarEstado();
                int l = 0;
                int g = 0;
                if(girado)
                    g=1;
                if(lleno)
                    l=1;
                enviarABD(correoUsuario, macbt, temp, g,l);
            }
        }.run();
    }

    public void comprobarAlarmaT(boolean activada){
        if(activada){
            if(temp>=seekBarTemp.getProgress()){
                Notifications.show(getActivity(), VisualizationFragment.class,
                        "Tupper Silam", "Temperatura de consigna alcanzada");
                bSetTemperatureAlarm.setText("Activar");
                alTAct =false;
                seekBarTemp.setProgress(0);
            }
        }
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
        if(temp != 0){
            tvTemperature.setText(temp + " ºC");
        }
    }

    /**
     * Timer utilizado en la alarma por cuenta atrás
     */
    public void arrancarTimer(){
        millisCounter = minutosTemp*60*1000; //Trabajamos con millisegundos
        mil = 0;
        countDownTimer = new unCountDown(millisCounter, 1000).start();
    }

    /**
     * Se actualiza el color del círculo que contiene el valor de la temperatura
     */
    public void actualizarColor(){

        int temp1 = 0;
        int temp2 = 0;
        int temp3 = 0;
        try {
            temp1 = getResources().getInteger(R.integer.tempVerdeMenorQue);
            temp2 = getResources().getInteger(R.integer.tempAmarillaMayVerMenQue);
            temp3 = getResources().getInteger(R.integer.tempRojaMayorQue);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (temp < temp1) {
                tvTemperature.setBackgroundColor(getContext().getColor(R.color.tempAzul));
                if(temp==0) {
                    tvTemperature.setBackgroundColor(getContext()
                            .getColor(R.color.material_grey300));
                }
            } else if (temp1 <= temp && temp < temp2) {
                tvTemperature.setBackgroundColor(getContext().getColor(R.color.tempAmarillo));
            } else if (temp >= temp3) {
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
    /**
    * Se verifica la posición (girado o no) del tupper y si está lleno o vacío
    *
    * */
    public void verificarEstado(){
        //aplicamos los cambios
        if(!girado){ //no está girado
            tupperVacio.setVisibility(View.VISIBLE);
            tupperCaliente.setVisibility(View.GONE);
            tupperFrio.setVisibility(View.GONE);
            tupperVacioGirado.setVisibility(View.GONE);
        }else if(girado && !lleno){ //girado y vacío
            tupperVacio.setVisibility(View.GONE);
            tupperCaliente.setVisibility(View.GONE);
            tupperFrio.setVisibility(View.GONE);
            tupperVacioGirado.setVisibility(View.VISIBLE);
        }else if(girado && lleno && temp < 30){ //girado, lleno y frío
            tupperVacio.setVisibility(View.GONE);
            tupperCaliente.setVisibility(View.GONE);
            tupperFrio.setVisibility(View.VISIBLE);
            tupperVacioGirado.setVisibility(View.GONE);
        }else if(girado && lleno && temp > 30){//girado, lleno y caliente
            tupperVacio.setVisibility(View.GONE);
            tupperCaliente.setVisibility(View.VISIBLE);
            tupperFrio.setVisibility(View.GONE);
            tupperVacioGirado.setVisibility(View.GONE);
        }else{
            //qué? tanto if else que ya ni sé
        }
    }

    private void inicializarAPI(){
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_ELASTICSEARCH)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        searchAPI = retrofit.create(ElasticSearchAPI.class);
    }

    private void enviarABD(String correo, String macdispBT, int temperatura, int girado, int lleno){
        Log.i("enviarABD","Correo:"+ correo + ", MAC: " + macdispBT + ", Tª:" +
                temperatura + ", Girado:" + girado + ", Lleno:" + lleno);
        //Generamos un authentication header para identificarnos contra Elasticsearch
        HashMap<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Authorization", Credentials.basic("android",
                mElasticSearchPassword));
        try {
            //Este es el JSON en el que especificamos los parámetros de la búsqueda
            queryJson = "{\n"+
                    "\"macbt\":\"" + macdispBT + "\",\n" +
                    "\"temperatura\":\"" + temperatura + "\",\n" +
                    "\"girado\":\""+ girado + "\",\n" +
                    "\"lleno\":\"" + lleno + "\",\n" +
                    "\"correousu\":\""+ correo + "\",\n" +
                    "\"timestamp\":\""+ System.currentTimeMillis() + "\"\n" +
                    "}";
            jsonObject = new JSONObject(queryJson);
        }catch (JSONException jerr){
            Log.d("Error: ", jerr.toString());
        }
        //Creamos el body con el JSON
        RequestBody body = RequestBody.create(okhttp3.MediaType
                .parse("application/json; charset=utf-8"),(jsonObject.toString()));
        //Realizamos la llamada mediante la API
        Call<RespuestaU> call = searchAPI.postTupper(headerMap, body);
        call.enqueue(new Callback<RespuestaU>() {
            @Override
            public void onResponse(Call<RespuestaU> call, Response<RespuestaU> response) {
                RespuestaU respuestaU = new RespuestaU();
                String jsonResponse;
                try{
                    Log.d(TAG, "onResponse: server response: " + response.toString());
                    //Si la respuesta es satisfactoria
                    if(response.isSuccessful()){
                        Log.d(TAG, "repsonseBody: "+ response.body().toString());
                        System.out.println(respuestaU.toString());
                        System.out.println(respuestaU.getIndex());
                        Log.d(TAG, " -----------onResponse: la response: " + response.body()
                                .toString());
                    }else{
                        jsonResponse = response.errorBody().string(); //error response body
                        System.out.println("Response body: " + jsonResponse);
                    }
                }catch (NullPointerException e){
                    Log.e(TAG, "onResponse: NullPointerException: " + e.getMessage() );
                }
                catch (IndexOutOfBoundsException e){
                    Log.e(TAG, "onResponse: IndexOutOfBoundsException: " + e.getMessage() );
                }
                catch (IOException e){
                    Log.e(TAG, "onResponse: IOException: " + e.getMessage() );
                }
            }

            @Override
            public void onFailure(Call<RespuestaU> call, Throwable t) {
                Log.e(TAG, "onFailure del POST usuario registrado ");
                System.out.println("El throwable: " + t);
            }
        });
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
                    "Tupper Silam", "El temporizador ha finalizado.");
            bSetTimeAlarm.setText("Activar");
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if(!seguir){
                this.cancel();
                seekBarTime.setMax(30);
                seekBarTime.setProgress(0);
            }
            millisCounter = millisCounter - 1000;
            mil = millisCounter /60 / 1000;
            timeAlarm.setText(Html.fromHtml("<b>Tiempo restante:</b> " +
                    Math.round(mil) + "min."));
            seekBarTime.setProgress(Math.round(millisUntilFinished));
            long timeRemaining = millisUntilFinished;
            seekBarTime.setProgress((int) (timeRemaining));
            //Log.i(TAG, "Time tick: " + millisUntilFinished);
            if(!seguir){
                seekBarTime.setMax(30);
                seekBarTime.setProgress(0);
            }
        }
    }
}


