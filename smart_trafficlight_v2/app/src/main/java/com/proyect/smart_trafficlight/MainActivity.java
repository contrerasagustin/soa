package com.proyect.smart_trafficlight;

import static com.proyect.smart_trafficlight.LinkedDevices.EXTRA_DEVICE_ADDRESS;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener
{

    EditText edtTextOut;
    ImageButton btnSend, btnSendDefault;
    Button btnDisconnect, enableSmartButton, disableSmartButton, viewSemaphores, backMenu, btnCancel;
    TextView inst;
    private StringBuilder recDataString = new StringBuilder();

    //-------------------------------------------
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    private static String address = null;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float accelerationThreshold = 30.0f;
    //-------------------------------------------

    private int mode;

    public static final int ENABLE_SMART_MODE = 1;
    public static final int DISABLE_SMART_MODE = 2;
    public static final int NO_SHAKE = 3;

    @Override
    @SuppressLint({"HandlerLeak", "MissingInflatedId"})
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //noinspection deprecation
        bluetoothIn = new Handler()
        {
            public void handleMessage(android.os.Message msg)
            {

                String TAG = "handlerState";
                if (msg.what == handlerState)
                {

                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("~");
                    Log.i(TAG, readMessage);
                    //cuando recibo toda una linea la muestro en el layout
                    if (endOfLineIndex > 0)
                    {
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);

                        dataInPrint = " ";
                    }
                    recDataString.delete(0, recDataString.length());      //clear all string data

                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        verifyStateBT();

        inst = findViewById(R.id.Instruc);
        enableSmartButton = findViewById(R.id.btnEnableSmartMode);
        disableSmartButton = findViewById(R.id.btnDisableSmartMode);
        viewSemaphores = findViewById(R.id.GoToLights);
        btnCancel = findViewById(R.id.btnCancelar);
        btnCancel.setVisibility(View.INVISIBLE);
        inst.setVisibility(View.INVISIBLE);
        mode = NO_SHAKE;

        btnCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                enableSmartButton.setVisibility(View.VISIBLE);
                disableSmartButton.setVisibility(View.VISIBLE);
                viewSemaphores.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.INVISIBLE);
                inst.setVisibility(View.INVISIBLE);

                mode = NO_SHAKE;
            }
        });

        viewSemaphores.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, LightActivity.class);
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
                startActivity(intent);
            }
        });

        enableSmartButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                enableSmartButton.setVisibility(View.INVISIBLE);
                disableSmartButton.setVisibility(View.INVISIBLE);
                viewSemaphores.setVisibility(View.INVISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                inst.setVisibility(View.VISIBLE);
                mode = ENABLE_SMART_MODE;

            }
        });

        disableSmartButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                enableSmartButton.setVisibility(View.INVISIBLE);
                disableSmartButton.setVisibility(View.INVISIBLE);
                viewSemaphores.setVisibility(View.INVISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                inst.setVisibility(View.VISIBLE);
                mode = DISABLE_SMART_MODE;

            }
        });

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
        {
        }
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume()
    {
        super.onResume();

        Intent intent = getIntent();
        address = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);
        //Setea la direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e)
        {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establece la conexión con el socket Bluetooth.
        try
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
            {

                btSocket.connect();
            }

        } catch (IOException e)
        {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
            }
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(this);
        try
        { // Cuando se sale de la aplicación esta parte permite que no se deje abierto el socket
            btSocket.close();
        } catch (IOException e2)
        {
        }
    }

    //Comprueba que el dispositivo Bluetooth
    //está disponible y solicita que se active si está desactivado
    @SuppressWarnings("deprecation")
    private void verifyStateBT()
    {

        if (btAdapter == null)
        {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else
        {
            if (btAdapter.isEnabled())
            {
            } else
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                {
                    startActivityForResult(enableBtIntent, 1);
                }

            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calcular la aceleración total
            double acceleration = Math.sqrt(x * x + y * y + z * z);

            // Comprobar si se ha detectado un shake basado en el umbral de sacudida
            if (acceleration > accelerationThreshold)
            {
                if (mode == ENABLE_SMART_MODE)
                {
                    MyConexionBT.write("S");
                }
                if (mode == DISABLE_SMART_MODE)
                {
                    MyConexionBT.write("N");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {
    }

    //Crea la clase que permite crear el evento de conexion
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
            } catch (IOException e)
            {
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            //byte[] byte_in = new byte[1];
            byte[] buffer = new byte[256];
            int bytes;
            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true)
            {
                try
                {

                    //se leen los datos del Bluethoot
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);

                    //se muestran en el layout de la activity, utilizando el handler del hilo
                    // principal antes mencionado
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();


                } catch (IOException e)
                {
                    break;
                }
            }
        }

        //Envio de trama
        public void write(String input)
        {
            try
            {
                mmOutStream.write(input.getBytes());
            } catch (IOException e)
            {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}