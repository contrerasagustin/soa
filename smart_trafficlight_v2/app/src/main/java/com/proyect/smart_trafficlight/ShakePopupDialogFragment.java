package com.proyect.smart_trafficlight;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ShakePopupDialogFragment extends DialogFragment implements View.OnClickListener, SensorEventListener {

    private Button closeButton;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float accelerationThreshold = 30.0f;

    private int mode;

    public static final int ENABLE_SMART_MODE = 1;
    public static final int DISABLE_SMART_MODE = 2;

    public ShakePopupDialogFragment(int mode){
        this.mode = mode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ShakePopupDialog);
        sensorManager = (SensorManager) requireActivity().getSystemService(requireContext().SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(mode == ENABLE_SMART_MODE){
            Log.i("TAG", "Activar inteligencia");
        }
        if(mode == DISABLE_SMART_MODE){
            Log.i("TAG", "Desactivar inteligencia");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_layout, container, false);

        closeButton = view.findViewById(R.id.btnCancel);
        closeButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnCancel) {
            dismiss();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.i("TAG", "resume");
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    public void onPause() {
        super.onPause();
        // Desregistrar el SensorEventListener cuando la actividad está en pausa
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calcular la aceleración total
            double acceleration = Math.sqrt(x * x + y * y + z * z);

            // Comprobar si se ha detectado un shake basado en el umbral de sacudida
            if (acceleration > accelerationThreshold) {
                if(mode == ENABLE_SMART_MODE){
                    Log.i("TAG", "Activo inteligencia");
                }
                if(mode == DISABLE_SMART_MODE){
                    Log.i("TAG", "Desactivo inteligencia");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No se utiliza en este caso
    }

}
