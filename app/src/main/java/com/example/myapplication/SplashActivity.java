package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Duración de la pantalla de carga (3 segundos)
        int tiempoCarga = 3000; 

        new Handler().postDelayed(() -> {
            // Ir a la actividad principal (MainActivity)
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Cerramos la pantalla de carga para que no se pueda volver atrás
        }, tiempoCarga);
    }
}