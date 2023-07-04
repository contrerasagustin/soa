package com.proyect.smart_trafficlight;

import static android.content.ContentValues.TAG;

import static com.proyect.smart_trafficlight.LinkedDevices.EXTRA_DEVICE_ADDRESS;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LightActivity extends AppCompatActivity {

    private ImageView trafficLight_A;
    private ImageView trafficLight_B;
    private static String address = null;

    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    TextView statusSensorA,statusSensorB;

    private ConnectedThread MyConexionBT;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Map<String, Integer> lights = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);

        //noinspection deprecation
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    char MyCar = (char) msg.obj;

                    if (MyCar == 'V') { //Sem a en verde
                       // tvtMsg.setText("VERDE");
                        statusSensorA.setText("SI");
                        statusSensorB.setText("NO");
                        trafficLight_A.setImageResource(lights.get("V"));
                        trafficLight_B.setImageResource(lights.get("R"));
                    }

                    if (MyCar == 'R') {
                        //tvtMsg.setText("ROJO");
                        statusSensorA.setText("NO");
                        statusSensorB.setText("SI");
                        trafficLight_A.setImageResource(lights.get("R"));
                        trafficLight_B.setImageResource(lights.get("V"));
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        verifyStateBT();


        statusSensorA = findViewById(R.id.statusSensorA);
        statusSensorB = findViewById(R.id.statusSensorB);
        trafficLight_A = findViewById(R.id.trafficLight_A);
        trafficLight_B = findViewById(R.id.trafficLight_B);

        lights.put("V",R.drawable.green);
        lights.put("Y",R.drawable.yellow);
        lights.put("R",R.drawable.red);

        Button backMenu = findViewById(R.id.BackMenu);
       /* backMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "ENVIO 4 A ARDUINO");
                Intent intent = new Intent(LightActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });*/
        backMenu = findViewById(R.id.BackMenu);

        backMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LightActivity.this, MainActivity.class);
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
                startActivity(intent);
            }
        });


        ///
        /*String estadosPalno = "R.Y.T.F";
        String[] estados = estadosPalno.split("\\.");

        TextView statusSensorA = findViewById(R.id.statusSensorA);
        if (estados[2].equals("F"))
            statusSensorA.setText("NO");
        else
            statusSensorA.setText("SI");

        TextView statusSensorB = findViewById(R.id.statusSensorB);
        if (estados[3].equals("F"))
            statusSensorB.setText("NO");
        else
            statusSensorB.setText("SI");

        trafficLight_A.setImageResource(lights.get(estados[0]));
        trafficLight_B.setImageResource(lights.get(estados[1]));*/
        ///

    }

    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        address = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);
        //Setea la direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establece la conexión con el socket Bluetooth.
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                btSocket.connect();
                //Toast.makeText(getBaseContext(), "CONEXION EXITOSA", Toast.LENGTH_SHORT).show();

                //return;
            }

            //btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
            }
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onPause() {
        super.onPause();
        try { // Cuando se sale de la aplicación esta parte permite que no se deje abierto el socket
            btSocket.close();
        } catch (IOException e2) {
        }
    }

    //Comprueba que el dispositivo Bluetooth
    //está disponible y solicita que se active si está desactivado
    @SuppressWarnings("deprecation")
    private void verifyStateBT() {

        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    startActivityForResult(enableBtIntent, 1);
                    //return;
                }

            }
        }
    }
    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] byte_in = new byte[1];
            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    mmInStream.read(byte_in);
                    char ch = (char) byte_in[0];
                    bluetoothIn.obtainMessage(handlerState, ch).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        //Envio de trama
        public void write(String input)
        {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

}