package com.stirling.sukaldatzensilam.Views;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.stirling.sukaldatzensilam.Models.BluetoothLE;
import com.stirling.sukaldatzensilam.R;
import com.stirling.sukaldatzensilam.Utils.BleCallback;
import com.stirling.sukaldatzensilam.Utils.BluetoothLEHelper;
import com.stirling.sukaldatzensilam.Utils.Constants;
import com.stirling.sukaldatzensilam.Utils.ElasticSearchAPI;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BluetoothActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    BluetoothLEHelper ble;

    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothSocket btsocker;

    private final static int REQUEST_ENABLE_BT = 1;
    private ListView dispEncontrados;
    private ListView dispVinculados;
    private ProgressBar progressBar2;
    private Button botonBuscar;
    private ArrayList<String> arListEncont;
    private ArrayList<BluetoothLE> arBLEEncont;
    private ArrayAdapter<String> arrayAdapterDispEncontrados;
    BluetoothDevice selDevice;
    private BleCallback bleCallback;
    private Retrofit retrofit;
    private ElasticSearchAPI searchAPI;
    private String tempObtenida;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getBaseContext().getSharedPreferences("preferencias",
                Context.MODE_PRIVATE);
        setContentView(R.layout.activity_bluetooth);
        ble = new BluetoothLEHelper(this   );
        auth = FirebaseAuth.getInstance();

        //Verificamos que el Bluetooth esté encendido, y si no lo está, pedimos encenderlo
        if(adapter == null || !adapter.isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        arListEncont = new ArrayList<String>();
        //Inicializamos elementos de la interfaz
        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        botonBuscar = (Button) findViewById(R.id.buscarButton);
        botonBuscar.setVisibility(View.VISIBLE);
        progressBar2.setVisibility(View.GONE);

        //Pedimos permisos en runtime, aparte de en el AndroidMAnifest, en caso de ser necesario
        solicitarPermisos();

        //Inicializamos list views para poder ir añadiendo
        dispEncontrados = (ListView) findViewById(R.id.listView1);
        dispVinculados = (ListView) findViewById(R.id.listView2);

        //Adapters para poder pasar a las ListViews desde arrays
        arrayAdapterDispEncontrados = new ArrayAdapter<String>(this,
                R.layout.text1, arListEncont);

        //Hacemos visible nuestro dispositivo
        hacerVisible();

        bleCallback = new BleCallback(){

            @Override
            public void onBleConnectionStateChange(BluetoothGatt gatt, int status, int newState){
                super.onBleConnectionStateChange(gatt, status, newState);

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    runOnUiThread(() -> Toast.makeText(BluetoothActivity.this,
                            "Conectado a serv. Bluetooth Gatt", Toast.LENGTH_SHORT).show());
                }

                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    runOnUiThread(() -> Toast.makeText(BluetoothActivity.this,
                            "Desconectado del serv. Bluetooth Gatt.",
                            Toast.LENGTH_SHORT).show());
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
                tempObtenida = Arrays.toString(characteristic.getValue());
                //Probamos a escribirla por terminal
                System.out.println("=======> Valor obtenido BLE: " + tempObtenida);
            }


        };

        botonBuscar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Se comprueba si está listo para scannear
                if(ble.isReadyForScan()){
                    Handler mHandler = new Handler();
                    //comienza el escaneo
                    progressBar2.setVisibility(View.VISIBLE);
                    botonBuscar.setVisibility(View.GONE);
                    ble.scanLeDevice(true);
                    //Cuando finaliza el escaneo;
                    mHandler.postDelayed(() -> {
                       progressBar2.setVisibility(View.GONE);
                       botonBuscar.setVisibility(View.VISIBLE);
                       //Obtenemos lista de dispositivos encontrados
                       arBLEEncont = ble.getListDevices();
                       //Convertimos a String para poder mostrarlos en la ListView de disp. encont.
                        deDeviceAString(arBLEEncont);
                    }, ble.getScanPeriod());
                    dispEncontrados.setAdapter(arrayAdapterDispEncontrados);
                }
                //Register for broadcast when a device is discovered.
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            }
        });

        dispEncontrados.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Detenemos la animación de la progressbar
                progressBar2.setVisibility(View.GONE);
                final String info = ((TextView) view).getText().toString();

                //Obtener la dirección MAC del dispositivo cuando hacemos click en él
                String dirMAC = info.substring(info.length()-17);

                //Conectar al dispositivo
                for (BluetoothLE bte : arBLEEncont){
                    if(bte.getMacAddress().equals(dirMAC)){
                        selDevice = bte.getDevice();
                        break;
                    }
                }
                ble.connect(selDevice,bleCallback);
                Log.i("Conectando con device","esto va después de ble.connect");

                //Comprobamos conexión con el dispositivo
                if(!checkIfBleIsConnected(ble)){
                    Toast.makeText(BluetoothActivity.this,
                            "No se ha podido conectar con el dispositivo",
                            Toast.LENGTH_SHORT).show();
                }else{
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("macbt", selDevice.getAddress());
                    editor.commit();
                    ble.disconnect();
                    Intent intentA = new Intent (dispEncontrados.getContext(), MainUserActivity.class);
                    intentA.putExtra("btdevice", selDevice);
                    startActivity(intentA);

                    finish();
                }

            }
        });
    }

    //Transformar disp. Bluetooth a información en String
    private ArrayList<String> deDeviceAString(ArrayList<BluetoothLE> arrayEncBTDevice){
        ArrayList<String> arrayEncString = new ArrayList<String>();
        for(BluetoothLE bluetoothLE : arrayEncBTDevice){
            String aString = bluetoothLE.getName() +"\n"+bluetoothLE.getMacAddress();
            //arrayEncString.add(aString);
            arListEncont.add(aString);
        }
        dispEncontrados.setAdapter(arrayAdapterDispEncontrados);
        //arrayAdapterDispEncontrados.notifyDataSetChanged();
        return arrayEncString;
    }

    /**
     * Se solicitan los permisos necesarios para la utilización de funciones bluetooth
     * Solicitados: ubicación
     */
    private void solicitarPermisos() {
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
            switch (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                case PackageManager.PERMISSION_DENIED:
                    ((TextView) new AlertDialog.Builder(this)
                            .setTitle("Runtime Permissions up ahead")
                            .setMessage(Html.fromHtml("<p>Para ver dispositivos " +
                                    "bluetooth cercanos pulse \"Permitir\" en el popup de " +
                                    "permisos.</p><p>Para más información " +
                                    " <a href=\"http://developer.android.com/about/versions/" +
                                    "marshmallow/android-6.0-changes.html#behavior-hardware-id\">" +
                                    "pulse aquí.</a>.</p>"))
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (ContextCompat.checkSelfPermission(getBaseContext(),
                                            Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                            PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(BluetoothActivity.this,
                                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                                    }
                                }
                            })
                            .show()
                            .findViewById(android.R.id.message))
                            .setMovementMethod(LinkMovementMethod.getInstance());       // Make the link clickable. Needs to be called after show(), in order to generate hyperlinks
                    break;
                case PackageManager.PERMISSION_GRANTED:
                    break;
            }
        }
    }

    /**
     * Hacemos nuestro dispositivo visible a otros dispositivos Bluetooth durante 400 segundos
     */
    private void hacerVisible() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 400);
        startActivity(discoverableIntent);
        Log.i("Log", "Discoverable ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Desconectamos el dispositivo BLE y detenemos scanneo
        Log.i("ble", "Disp. BLE desconectado y scanneo false desde activity BluetootActivity!!");
        ble.scanLeDevice(false);
        ble.disconnect();
    }

    private boolean checkIfBleIsConnected(BluetoothLEHelper bluetoothLEHelper){
        if(bluetoothLEHelper.isConnected()){
            Log.i("isConnectedBTAct","---->El dispositivo está conectado<----");
            return true;
        }else{
            Log.i("isConnectedBTAct", "--->El dispositivo no está conectado<---");
            return false;
        }
    }

}
