package com.proyect.smart_trafficlight;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class LightActivity extends AppCompatActivity {

    private ImageView trafficLight_A;
    private ImageView trafficLight_B;

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
        backMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "ENVIO 4 A ARDUINO");
                Intent intent = new Intent(LightActivity.this, MenuActivity.class);
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
}