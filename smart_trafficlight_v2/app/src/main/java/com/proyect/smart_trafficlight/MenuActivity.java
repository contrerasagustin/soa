package com.proyect.smart_trafficlight;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MenuActivity extends AppCompatActivity {

    private MainActivity.ConnectedThread MyConexionBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button goToLights = findViewById(R.id.GoToLights);
        goToLights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "ENVIO 1 A ARDUINO");
                MyConexionBT.write("1");
                Intent intent = new Intent(MenuActivity.this, LightActivity.class);
                startActivity(intent);
            }
        });

        Button connectArduino = findViewById(R.id.ConnectLight);
        connectArduino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, LinkedDevices.class);
                startActivity(intent);
            }
        });

        Button controlLights = findViewById(R.id.ControlLight);
        controlLights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "ENVIO 5 A ARDUINO");
                MyConexionBT.write("5");
                //Intent intent = new Intent(MainActivity.this, SemAActivity.class);
                //startActivity(intent);
            }
        });





    }
}