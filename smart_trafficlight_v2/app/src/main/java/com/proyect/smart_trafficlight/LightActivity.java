package com.proyect.smart_trafficlight;

import static android.content.ContentValues.TAG;

import static com.proyect.smart_trafficlight.LinkedDevices.EXTRA_DEVICE_ADDRESS;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LightActivity extends AppCompatActivity {

    private ImageView trafficLight_A;
    private ImageView trafficLight_B;
    private static String address = null;

    private Map<String, Integer> lights = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);

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
        String estadosPalno = "R.Y.T.F";
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
        trafficLight_B.setImageResource(lights.get(estados[1]));
        ///

    }

    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        address = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);
        //Setea la direccion MAC
        /*BluetoothDevice device = btAdapter.getRemoteDevice(address);

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
        MyConexionBT = new MainActivity.ConnectedThread(btSocket);
        MyConexionBT.start();*/
    }

}