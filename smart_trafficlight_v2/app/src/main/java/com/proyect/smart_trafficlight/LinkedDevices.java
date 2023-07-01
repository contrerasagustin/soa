package com.proyect.smart_trafficlight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class LinkedDevices extends AppCompatActivity {

    private static final String TAG = "DispositivosVinculados";
    ListView idList;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linked_devices);

        requestBluetoothPermission();
    }

    @Override
    public void onResume() {
        super.onResume();
        verifyStateBT();
        mPairedDevicesArrayAdapter = new ArrayAdapter(this, R.layout.found_devices);
        idList = (ListView) findViewById(R.id.idList);
        idList.setAdapter(mPairedDevicesArrayAdapter);
        idList.setOnItemClickListener(mDeviceClickListener);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {

            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            finishAffinity();

            Intent intend = new Intent(LinkedDevices.this, MainActivity.class);
            intend.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(intend);
        }
    };

    @SuppressWarnings("deprecation")
    private void verifyStateBT() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (mBtAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth Activado...");
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(enableBtIntent, 1);
                }
            }
        }
    }

    private boolean hasBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
            return permissionResult == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Si la versión de Android es anterior a Marshmallow, se considera que tienes el permiso.
    }

    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;

    private void requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasBluetoothPermission()) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El usuario ha otorgado el permiso de Bluetooth. Puedes realizar las acciones que necesites.
            } else {
                    // El usuario ha denegado el permiso de Bluetooth. Puedes mostrar un mensaje o tomar otras acciones.
            }
        }
    }

}