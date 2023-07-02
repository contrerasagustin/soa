package com.proyect.smart_trafficlight;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ControlSemActivity extends AppCompatActivity {
    // Variables y métodos aquí

    private ImageView trafficLightImageView;
    private Button enableSmartButton;
    private Button disableSmartButton;
    private Button yellowButton;
    private Button greenButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_sem);

        // Obtener referencias a los botones
        //backButton = findViewById(R.id.backButton);
        enableSmartButton = findViewById(R.id.btnEnableSmartMode);
        disableSmartButton = findViewById(R.id.btnDisableSmartMode);

        enableSmartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showShakePopupDialogEnable();

            }
        });

        disableSmartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showShakePopupDialogDisable();

            }
        });

        /*
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

         */


        Button backMenu = findViewById(R.id.BackMenu);
        backMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ControlSemActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public void onBackPressed() {
        finish();
    }

    private void showShakePopupDialogEnable() {
        ShakePopupDialogFragment popupDialog = new ShakePopupDialogFragment(ShakePopupDialogFragment.ENABLE_SMART_MODE);
        popupDialog.show(getSupportFragmentManager(), "ShakePopupDialog");
    }
    private void showShakePopupDialogDisable() {
        ShakePopupDialogFragment popupDialog = new ShakePopupDialogFragment(ShakePopupDialogFragment.DISABLE_SMART_MODE);
        popupDialog.show(getSupportFragmentManager(), "ShakePopupDialog");
    }


    // Otros métodos aquí
}
