package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class CanvasActivity extends AppCompatActivity implements SensorEventListener {

    private MiCanvasView miCanvas;
    private Button btnIniciarServicio;
    private CanvasReceiver canvasReceiver;
    private boolean isServiceRunning = false;

    // Gyroscope variables
    private SensorManager sensorManager;
    private Sensor gyroSensor;
    private float gyroAngleX = 0f;
    private float gyroAngleY = 0f;
    private long lastTimestamp = 0;

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas);

        miCanvas = findViewById(R.id.miCanvas);
        btnIniciarServicio = findViewById(R.id.btnIniciarServicio);

        btnIniciarServicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentService = new Intent(CanvasActivity.this, CanvasService.class);
                if (isServiceRunning) {
                    Log.d("CanvasActivity", "Deteniendo servicio...");
                    stopService(intentService);
                    btnIniciarServicio.setText("Iniciar Servicio Canvas");
                    btnIniciarServicio.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                    isServiceRunning = false;
                } else {
                    Log.d("CanvasActivity", "Iniciando servicio...");
                    startService(intentService);
                    btnIniciarServicio.setText("Detener Servicio Canvas");
                    btnIniciarServicio.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                    isServiceRunning = true;
                }
            }
        });

        // Configurar Receiver para recibir actualizaciones de posición
        canvasReceiver = new CanvasReceiver();
        IntentFilter filter = new IntentFilter("ACTUALIZAR_CANVAS");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(canvasReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(canvasReceiver, filter);
        }

        // Initialize Sensor Manager and Gyroscope
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && gyroSensor != null) {
            sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
            lastTimestamp = 0; // Reset timestamp on resume
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intentService = new Intent(this, CanvasService.class);
        stopService(intentService);
        if (canvasReceiver != null) {
            unregisterReceiver(canvasReceiver);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if (lastTimestamp != 0) {
                float dT = (event.timestamp - lastTimestamp) * 1.0f / 1000000000.0f; // nanoseconds to seconds
                
                // values[0] is X-axis (tilt forward/backward) -> controls Y offset
                // values[1] is Y-axis (tilt left/right) -> controls X offset
                gyroAngleX += event.values[1] * dT * 200f; // Scale sensitivity
                gyroAngleY += event.values[0] * dT * 200f;

                // Clamp to prevent moving too far (max offset = 60 pixels)
                gyroAngleX = Math.max(-60f, Math.min(60f, gyroAngleX));
                gyroAngleY = Math.max(-60f, Math.min(60f, gyroAngleY));

                // Decay factor (slow return to center to eliminate gyro drift)
                gyroAngleX *= 0.96f;
                gyroAngleY *= 0.96f;

                if (miCanvas != null) {
                    miCanvas.actualizarOffsetsGiroscopio(gyroAngleX, gyroAngleY);
                }
            }
            lastTimestamp = event.timestamp;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private class CanvasReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("ACTUALIZAR_CANVAS".equals(intent.getAction())) {
                int nuevaX = intent.getIntExtra("posicionX", 0);
                Log.d("CanvasReceiver", "Posición recibida: " + nuevaX);
                miCanvas.actualizarPosicion(nuevaX);
            }
        }
    }
}
