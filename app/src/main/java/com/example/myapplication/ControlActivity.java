package com.example.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class ControlActivity extends AppCompatActivity {

    // Componentes UI
    private Button btnBT, btnPlayPause, btnRuidoTono, btnPitchUp, btnPitchDown;
    private Button btnA2, btnA3, btnB4, btnAB1, btnAB5, btnAB6;
    private SeekBar sbVolumen, sbTiempo;
    private TextView tvEstado;
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

    // Bluetooth y Audio
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private OutputStream outStream;
    private static final String ESP32_MAC = "6C:C8:40:4E:D5:92"; // Cambia por tu MAC
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private GeneradorAudio generadorAudio;
    private Handler handlerSincronia = new Handler();
    private void detenerTodo() {
        // 1. Detenemos las banderas de control
        enEjecucion = false;
        esPausa = false;

        // 2. IMPORTANTE: Quitamos todos los mensajes pendientes del Handler
        // Esto evita que el bucleSincronia se vuelva a llamar a sí mismo
        handlerSincronia.removeCallbacksAndMessages(null);

        // 3. Detenemos el motor de audio
        if (generadorAudio != null) {
            generadorAudio.detener();
        }

        // 4. Apagamos los LEDs del ESP32 enviando ceros
        enviarDatoBT("0,0\n");

        // 5. Resetear UI
        runOnUiThread(() -> {
            btnPlayPause.setText("EJECUTAR PROGRAMA");
            sbTiempo.setProgress(0);
            tiempoTranscurrido = 0;
        });

        Toast.makeText(this, "Programa detenido", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        inicializarComponentes();
        configurarListeners();

        generadorAudio = new GeneradorAudio();
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private void inicializarComponentes() {
        btnBT = findViewById(R.id.btnBT);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnRuidoTono = findViewById(R.id.btnRuidoTono);
        btnPitchUp = findViewById(R.id.btnPitchUp);
        btnPitchDown = findViewById(R.id.btnPitchDown);

        btnA2 = findViewById(R.id.btnA2);
        btnA3 = findViewById(R.id.btnA3);
        // ... inicializa los demás botones de programas igual

        sbVolumen = findViewById(R.id.sbVolumen);
        sbTiempo = findViewById(R.id.sbTiempo); // La nueva barra
        tvEstado = findViewById(R.id.tvEstado);
        viewGrafica = findViewById(R.id.viewGrafica);
    }

    private void configurarListeners() {
        btnBT.setOnClickListener(v -> conectarBluetooth());

        btnPlayPause.setOnClickListener(v -> {
            if (!enEjecucion) {
                iniciarPrograma();
            } else {
                detenerTodo();
            }
        });

        // Cambio de Ruido / Tono
        btnRuidoTono.setOnClickListener(v -> {
            esModoRuido = !esModoRuido;
            generadorAudio.setModo(esModoRuido);
            btnRuidoTono.setText(esModoRuido ? "Modo: Ruido Blanco" : "Modo: Tono");
        });

        // Barra de Tiempo (Fast Forward)
        sbTiempo.setMax(30);
        sbTiempo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tiempoTranscurrido = (long) progress * 60000;
                    Toast.makeText(ControlActivity.this, "Minuto: " + progress, Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Configuración de Pitch
        btnPitchUp.setOnClickListener(v -> {
            pitchActual = Math.min(2.0f, pitchActual + 0.1f);
            generadorAudio.setPitch(pitchActual);
        });
        btnPitchDown.setOnClickListener(v -> {
            pitchActual = Math.max(0.5f, pitchActual - 0.1f);
            generadorAudio.setPitch(pitchActual);
        });

        // Selección de Programas
        btnA2.setOnClickListener(v -> seleccionarPrograma("A2"));
        // ... agregar listeners para A3, B4, etc.
    }

    private void seleccionarPrograma(String id) {
        this.programaSeleccionado = id;
        this.tiempoTranscurrido = 0;
        sbTiempo.setProgress(0);
        Toast.makeText(this, "Programa " + id + " seleccionado", Toast.LENGTH_SHORT).show();
    }

    private void iniciarPrograma() {
        enEjecucion = true;
        esPausa = false;
        ultimoTick = System.currentTimeMillis();
        btnPlayPause.setText("PAUSA");
        generadorAudio.iniciarAudioEstereo(); // Inicia el audio estéreo
        bucleSincronia();
    }

    private void bucleSincronia() {
        if (!enEjecucion || esPausa) return;

        long ahora = System.currentTimeMillis();
        tiempoTranscurrido += (ahora - ultimoTick);
        ultimoTick = ahora;

        // 1. Obtener Frecuencia y Amplitud de la Calculadora
        double frecuenciaHz = CalculadoraProgramas.obtenerFrecuencia(programaSeleccionado, tiempoTranscurrido);
        int ampBase = CalculadoraProgramas.obtenerAmplitud(programaSeleccionado, tiempoTranscurrido, 255);

        // 2. Obtener brillos por patrón [L, R]
        int[] brillos = CalculadoraProgramas.obtenerBrillosPorPatron(programaSeleccionado, tiempoTranscurrido, ampBase);

        // 3. Modulación de Audio Estéreo
        generadorAudio.setFrecuenciaModulacion(frecuenciaHz);
        double volL = (brillos[0] > 0) ? 1.0 : 0.0;
        double volR = (brillos[1] > 0) ? 1.0 : 0.0;
        generadorAudio.setVolumenEstereo(volL * volumenGeneral, volR * volumenGeneral);

        // 4. Envío al ESP32 (Formato: L,R\n)
        enviarDatoBT(brillos[0] + "," + brillos[1] + "\n");

        // 5. Actualizar Interfaz
        viewGrafica.actualizarGrafica(brillos[0]);
        sbTiempo.setProgress((int)(tiempoTranscurrido / 60000));

        handlerSincronia.postDelayed(this::bucleSincronia, 50);
    }

    private void conectarBluetooth() {
        new Thread(() -> {
            try {
                BluetoothDevice device = btAdapter.getRemoteDevice(ESP32_MAC);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return;

                btSocket = device.createRfcommSocketToServiceRecord(BT_UUID);
                btAdapter.cancelDiscovery();
                btSocket.connect();
                outStream = btSocket.getOutputStream();

                runOnUiThread(() -> {
                    tvEstado.setText("Estado: Conectado");
                    tvEstado.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                });
            } catch (IOException e) {
                runOnUiThread(() -> tvEstado.setText("Estado: Error de Conexión"));
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
        enEjecucion = false;
        generadorAudio.detener();
        try { if (btSocket != null) btSocket.close(); } catch (IOException e) { e.printStackTrace(); }
    }
}