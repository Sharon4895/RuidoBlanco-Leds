package com.example.myapplication;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import java.util.Random;

public class GeneradorAudio {
    private AudioTrack audioTrack;
    private boolean isPlaying = false;
    private final int sampleRate = 44100;

    // Variables de control
    private float volumenGeneral = 0.5f;
    private double volL = 1.0, volR = 1.0; // Control estéreo por patrón
    private double frecuenciaModulacion = 10.0; // Hz de la gráfica
    private float pitchFactor = 1.0f; // Frecuencia del tono (agudo/grave)
    private boolean esModoRuido = true;

    public void setModo(boolean esRuido) { this.esModoRuido = esRuido; }
    public void setPitch(float pitch) { this.pitchFactor = pitch; }
    public void setFrecuenciaModulacion(double hz) { this.frecuenciaModulacion = hz; }

    public void setVolumenEstereo(double l, double r) {
        this.volL = l;
        this.volR = r;
    }

    public void iniciarAudioEstereo() {
        if (isPlaying) return;

        // Configuramos para STEREO (L y R)
        int bufferSize = AudioTrack.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize, AudioTrack.MODE_STREAM);

        isPlaying = true;

        new Thread(() -> {
            short[] buffer = new short[bufferSize];
            Random random = new Random();
            long sampleCount = 0;

            while (isPlaying) {
                // El buffer estéreo se llena: [L, R, L, R, L, R...]
                for (int i = 0; i < buffer.length; i += 2) {
                    double muestraBase;

                    if (esModoRuido) {
                        // Ruido Blanco (Gaussiano)
                        muestraBase = random.nextGaussian();
                    } else {
                        // Modo Tono: Onda Senoidal (Frecuencia base 440Hz * Pitch)
                        double frecuenciaTono = 440.0 * pitchFactor;
                        double angulo = 2.0 * Math.PI * sampleCount * (frecuenciaTono / sampleRate);
                        muestraBase = Math.sin(angulo);
                    }

                    // Aplicamos la Envolvente Diente de Sierra (Sincronía con LED)
                    double muestrasPorPeriodo = sampleRate / frecuenciaModulacion;
                    double envolvente = (sampleCount % muestrasPorPeriodo) / muestrasPorPeriodo;

                    // Canal Izquierdo (L)
                    buffer[i] = (short) (muestraBase * envolvente * volL * volumenGeneral * Short.MAX_VALUE);

                    // Canal Derecho (R)
                    buffer[i + 1] = (short) (muestraBase * envolvente * volR * volumenGeneral * Short.MAX_VALUE);

                    sampleCount++;
                }

                if (audioTrack != null && audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                    audioTrack.write(buffer, 0, buffer.length);
                }
            }
        }).start();

        audioTrack.play();
    }

    public void detener() {
        isPlaying = false;
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }
}