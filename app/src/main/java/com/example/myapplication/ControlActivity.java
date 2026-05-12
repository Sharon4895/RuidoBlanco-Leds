package com.example.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class ControlActivity extends AppCompatActivity {

    // Componentes UI
    private Button btnBT, btnPlayPause, btnStop, btnRuidoTono, btnPitchUp, btnPitchDown;
    private Button btnA0, btnA1, btnA2, btnA3, btnA4, btnA5, btnA6, btnB0, btnB1, btnB2, btnB3, btnB4, btnB5, btnB6, btnAB0, btnAB1, btnAB2, btnAB3, btnAB4, btnAB5, btnAB6;
    private SeekBar sbVolumen, sbTiempo;
    private TextView tvEstado, tvProgramaActual;
    private VistaGrafica viewGrafica;

    // Variables de Control
    private String programaSeleccionado = "A2";
    private boolean enEjecucion = false;
    private boolean esPausa = false;
    private boolean esModoRuido = true;
    private long tiempoTranscurrido = 0;
    private long ultimoTick = 0;
    private float pitchActual = 1.0f;
    private float volumenGeneral = 0.5f;
    private String usuarioActual = "default";

    // Bluetooth y Audio
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private OutputStream outStream;
    // ASEGÚRATE DE QUE ESTA MAC ES LA CORRECTA DE TU ESP32
    private static final String ESP32_MAC = "6C:C8:40:4E:D5:92"; 
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    private GeneradorAudio generadorAudio;
    private Handler handlerSincronia = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        if (getIntent().hasExtra("USUARIO_ALIAS")) {
            usuarioActual = getIntent().getStringExtra("USUARIO_ALIAS");
        }

        generadorAudio = new GeneradorAudio();
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        inicializarComponentes();
        configurarListeners();
        cargarConfiguracionDeBD();
    }

    private void inicializarComponentes() {
        btnBT = findViewById(R.id.btnBT);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnStop = findViewById(R.id.btnStop);
        btnRuidoTono = findViewById(R.id.btnRuidoTono);
        btnPitchUp = findViewById(R.id.btnPitchUp);
        btnPitchDown = findViewById(R.id.btnPitchDown);

        btnA0 = findViewById(R.id.btnA0);
        btnA1 = findViewById(R.id.btnA1);
        btnA2 = findViewById(R.id.btnA2);
        btnA3 = findViewById(R.id.btnA3);
        btnA4 = findViewById(R.id.btnA4);
        btnA5 = findViewById(R.id.btnA5);
        btnA6 = findViewById(R.id.btnA6);
        btnB0 = findViewById(R.id.btnB0);
        btnB1 = findViewById(R.id.btnB1);
        btnB2 = findViewById(R.id.btnB2);
        btnB3 = findViewById(R.id.btnB3);
        btnB4 = findViewById(R.id.btnB4);
        btnB5 = findViewById(R.id.btnB5);
        btnB6 = findViewById(R.id.btnB6);
        btnAB0 = findViewById(R.id.btnAB0);
        btnAB1 = findViewById(R.id.btnAB1);
        btnAB2 = findViewById(R.id.btnAB2);
        btnAB3 = findViewById(R.id.btnAB3);
        btnAB4 = findViewById(R.id.btnAB4);
        btnAB5 = findViewById(R.id.btnAB5);
        btnAB6 = findViewById(R.id.btnAB6);

        sbVolumen = findViewById(R.id.sbVolumen);
        sbTiempo = findViewById(R.id.sbTiempo);
        tvEstado = findViewById(R.id.tvEstado);
        tvProgramaActual = findViewById(R.id.tvProgramaActual);
        viewGrafica = findViewById(R.id.viewGrafica);
    }

    private void configurarListeners() {
        if (btnBT != null) btnBT.setOnClickListener(v -> verificarPermisosYConectar());

        if (btnPlayPause != null) {
            btnPlayPause.setOnClickListener(v -> {
                if (!enEjecucion) {
                    iniciarPrograma();
                } else {
                    esPausa = !esPausa;
                    btnPlayPause.setText(esPausa ? "REANUDAR" : "PAUSA");
                    if (esPausa) {
                        if (generadorAudio != null) generadorAudio.pausarAudio();
                        handlerSincronia.removeCallbacksAndMessages(null);
                        enviarDatoBT("0,0\n");
                    } else {
                        if (generadorAudio != null) generadorAudio.reanudarAudio();
                        ultimoTick = System.currentTimeMillis();
                        bucleSincronia();
                    }
                }
            });
        }

        if (btnStop != null) btnStop.setOnClickListener(v -> detenerTodo());

        if (btnRuidoTono != null) {
            btnRuidoTono.setOnClickListener(v -> {
                esModoRuido = !esModoRuido;
                if (generadorAudio != null) generadorAudio.setModo(esModoRuido);
                btnRuidoTono.setText(esModoRuido ? "Modo: Ruido Blanco" : "Modo: Tono");
                guardarConfiguracionEnBD();
            });
        }

        if (sbVolumen != null) {
            sbVolumen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    volumenGeneral = progress / 100f;
                    if (generadorAudio != null) generadorAudio.setVolumen(volumenGeneral);
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {
                    guardarConfiguracionEnBD();
                }
            });
        }

        if (sbTiempo != null) {
            sbTiempo.setMax(80); // Aumentado para soportar el programa AB4 de 75 min
            sbTiempo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        tiempoTranscurrido = (long) progress * 60000;
                        if (viewGrafica != null) viewGrafica.actualizarGrafica(0);
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        if (btnPitchUp != null) {
            btnPitchUp.setOnClickListener(v -> {
                pitchActual = Math.min(2.0f, pitchActual + 0.1f);
                if (generadorAudio != null) generadorAudio.setPitch(pitchActual);
                guardarConfiguracionEnBD();
            });
        }

        if (btnPitchDown != null) {
            btnPitchDown.setOnClickListener(v -> {
                pitchActual = Math.max(0.5f, pitchActual - 0.1f);
                if (generadorAudio != null) generadorAudio.setPitch(pitchActual);
                guardarConfiguracionEnBD();
            });
        }

        if (btnA0 != null) btnA0.setOnClickListener(v -> seleccionarPrograma("A0"));
        if (btnA1 != null) btnA1.setOnClickListener(v -> seleccionarPrograma("A1"));
        if (btnA2 != null) btnA2.setOnClickListener(v -> seleccionarPrograma("A2"));
        if (btnA3 != null) btnA3.setOnClickListener(v -> seleccionarPrograma("A3"));
        if (btnA4 != null) btnA4.setOnClickListener(v -> seleccionarPrograma("A4"));
        if (btnA5 != null) btnA5.setOnClickListener(v -> seleccionarPrograma("A5"));
        if (btnA6 != null) btnA6.setOnClickListener(v -> seleccionarPrograma("A6"));
        if (btnB0 != null) btnB0.setOnClickListener(v -> seleccionarPrograma("B0"));
        if (btnB1 != null) btnB1.setOnClickListener(v -> seleccionarPrograma("B1"));
        if (btnB2 != null) btnB2.setOnClickListener(v -> seleccionarPrograma("B2"));
        if (btnB3 != null) btnB3.setOnClickListener(v -> seleccionarPrograma("B3"));
        if (btnB4 != null) btnB4.setOnClickListener(v -> seleccionarPrograma("B4"));
        if (btnB5 != null) btnB5.setOnClickListener(v -> seleccionarPrograma("B5"));
        if (btnB6 != null) btnB6.setOnClickListener(v -> seleccionarPrograma("B6"));
        if (btnAB0 != null) btnAB0.setOnClickListener(v -> seleccionarPrograma("AB0"));
        if (btnAB1 != null) btnAB1.setOnClickListener(v -> seleccionarPrograma("AB1"));
        if (btnAB2 != null) btnAB2.setOnClickListener(v -> seleccionarPrograma("AB2"));
        if (btnAB3 != null) btnAB3.setOnClickListener(v -> seleccionarPrograma("AB3"));
        if (btnAB4 != null) btnAB4.setOnClickListener(v -> seleccionarPrograma("AB4"));
        if (btnAB5 != null) btnAB5.setOnClickListener(v -> seleccionarPrograma("AB5"));
        if (btnAB6 != null) btnAB6.setOnClickListener(v -> seleccionarPrograma("AB6"));
    }

    private void verificarPermisosYConectar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean connectGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
            boolean scanGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;

            if (!connectGranted || !scanGranted) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 
                    REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            }
        }
        conectarBluetooth();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                conectarBluetooth();
            } else {
                Toast.makeText(this, "Permisos de Bluetooth denegados", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void seleccionarPrograma(String id) {
        this.programaSeleccionado = id;
        this.tiempoTranscurrido = 0;
        if (sbTiempo != null) sbTiempo.setProgress(0);
        if (tvProgramaActual != null) tvProgramaActual.setText("Programa: " + id);
        Toast.makeText(this, "Seleccionado: " + id, Toast.LENGTH_SHORT).show();
        guardarConfiguracionEnBD();
    }

    private void iniciarPrograma() {
        enEjecucion = true;
        esPausa = false;
        ultimoTick = System.currentTimeMillis();
        if (btnPlayPause != null) btnPlayPause.setText("PAUSA");
        if (generadorAudio != null) generadorAudio.iniciarAudioEstereo(this);
        bucleSincronia();
    }

    private void bucleSincronia() {
        if (!enEjecucion || esPausa) return;

        long ahora = System.currentTimeMillis();
        tiempoTranscurrido += (ahora - ultimoTick);
        ultimoTick = ahora;

        double frecuenciaHz = CalculadoraProgramas.obtenerFrecuencia(programaSeleccionado, tiempoTranscurrido);
        int ampBase = CalculadoraProgramas.obtenerAmplitud(programaSeleccionado, tiempoTranscurrido, 255);
        int[] brillos = CalculadoraProgramas.obtenerBrillosPorPatron(programaSeleccionado, tiempoTranscurrido, ampBase);

        if (generadorAudio != null) {
            generadorAudio.setFrecuenciaModulacion(frecuenciaHz);
            double volL = (brillos[0] > 0) ? 1.0 : 0.0;
            double volR = (brillos[1] > 0) ? 1.0 : 0.0;
            generadorAudio.setVolumenEstereo(volL * volumenGeneral, volR * volumenGeneral);
        }

        enviarDatoBT(brillos[0] + "," + brillos[1] + "\n");

        if (viewGrafica != null) viewGrafica.actualizarGrafica(brillos[0]);
        if (sbTiempo != null) sbTiempo.setProgress((int)(tiempoTranscurrido / 60000));

        handlerSincronia.postDelayed(this::bucleSincronia, 50);
    }

    private void guardarConfiguracionEnBD() {
        try {
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
            SQLiteDatabase db = admin.getWritableDatabase();
            ContentValues registro = new ContentValues();
            registro.put("volumen_max", sbVolumen != null ? sbVolumen.getProgress() : 50);
            registro.put("ultimo_programa", programaSeleccionado);
            registro.put("modo_audio", esModoRuido ? 0 : 1);
            registro.put("pitch_nivel", pitchActual);

            db.update("usuarios", registro, "alias=?", new String[]{usuarioActual});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarConfiguracionDeBD() {
        try {
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
            SQLiteDatabase db = admin.getReadableDatabase();
            Cursor fila = db.rawQuery("select volumen_max, ultimo_programa, modo_audio, pitch_nivel from usuarios where alias=?", new String[]{usuarioActual});

            if (fila.moveToFirst()) {
                int vol = fila.getInt(0);
                if (sbVolumen != null) sbVolumen.setProgress(vol);
                volumenGeneral = vol / 100f;
                programaSeleccionado = fila.getString(1);
                if (tvProgramaActual != null) tvProgramaActual.setText("Programa: " + programaSeleccionado);
                esModoRuido = (fila.getInt(2) == 0);
                if (btnRuidoTono != null) btnRuidoTono.setText(esModoRuido ? "Modo: Ruido Blanco" : "Modo: Tono");
                pitchActual = fila.getFloat(3);
                if (generadorAudio != null) {
                    generadorAudio.setModo(esModoRuido);
                    generadorAudio.setPitch(pitchActual);
                    generadorAudio.setVolumen(volumenGeneral);
                }
            }
            fila.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void detenerTodo() {
        enEjecucion = false;
        esPausa = false;
        handlerSincronia.removeCallbacksAndMessages(null);
        if (generadorAudio != null) generadorAudio.detener();
        enviarDatoBT("0,0\n");
        guardarConfiguracionEnBD();

        runOnUiThread(() -> {
            if (btnPlayPause != null) btnPlayPause.setText("EJECUTAR PROGRAMA");
            if (sbTiempo != null) sbTiempo.setProgress(0);
            tiempoTranscurrido = 0;
            if (viewGrafica != null) viewGrafica.actualizarGrafica(0);
        });
        Toast.makeText(this, "Programa detenido", Toast.LENGTH_SHORT).show();
    }

    private void conectarBluetooth() {
        if (btAdapter == null) {
            Toast.makeText(this, "Bluetooth no soportado en este dispositivo", Toast.LENGTH_LONG).show();
            return;
        }

        if (!btAdapter.isEnabled()) {
            Toast.makeText(this, "Por favor, activa el Bluetooth", Toast.LENGTH_LONG).show();
            return;
        }

        runOnUiThread(() -> tvEstado.setText("Estado: Conectando..."));

        new Thread(() -> {
            try {
                BluetoothDevice device = btAdapter.getRemoteDevice(ESP32_MAC);
                
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    runOnUiThread(() -> Toast.makeText(this, "Sin permisos de conexión o escaneo", Toast.LENGTH_SHORT).show());
                    return;
                }

                // Intentar conexión segura e insegura como respaldo
                try {
                    btSocket = device.createRfcommSocketToServiceRecord(BT_UUID);
                } catch (Exception e) {
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(BT_UUID);
                }

                btAdapter.cancelDiscovery();
                btSocket.connect();
                outStream = btSocket.getOutputStream();

                runOnUiThread(() -> {
                    if (tvEstado != null) {
                        tvEstado.setText("Estado: Conectado");
                        tvEstado.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                    }
                    Toast.makeText(this, "¡Conectado exitosamente!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    if (tvEstado != null) {
                        tvEstado.setText("Estado: Error de Conexión");
                        tvEstado.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                    }
                    Toast.makeText(this, "No se pudo conectar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
                
                try {
                    if (btSocket != null) btSocket.close();
                } catch (IOException ie) { ie.printStackTrace(); }
            }
        }).start();
    }

    private void enviarDatoBT(String data) {
        if (outStream != null) {
            try {
                outStream.write(data.getBytes());
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detenerTodo();
        try { if (btSocket != null) btSocket.close(); } catch (IOException e) { e.printStackTrace(); }
    }
}