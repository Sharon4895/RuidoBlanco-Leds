package com.example.myapplication;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import java.util.Random;

public class GeneradorAudio {
    private AudioTrack audioTrack;
    private MediaPlayer mediaPlayer;
    private volatile boolean isPlaying = false;
    private volatile boolean isPaused = false;
    private final int sampleRate = 44100;

    // Variables de control
    private float volumenGeneral = 0.5f;
    private double volL = 1.0, volR = 1.0; 
    private double frecuenciaModulacion = 10.0; 
    private float pitchFactor = 1.0f; 
    private boolean esModoRuido = true;

    private double faseTono = 0;
    private double faseModulacion = 0;

    public void setModo(boolean esRuido) { this.esModoRuido = esRuido; }
    public void setPitch(float pitch) { this.pitchFactor = pitch; }
    public void setFrecuenciaModulacion(double hz) { this.frecuenciaModulacion = hz; }

    public void setVolumen(float vol) {
        this.volumenGeneral = vol;
        // Actualizar volumen de la música de fondo si está activa
        if (mediaPlayer != null) {
            float volMusica = vol * 0.4f; // La música de fondo es un poco más suave
            mediaPlayer.setVolume(volMusica, volMusica);
        }
    }

    public void setVolumenEstereo(double l, double r) {
        this.volL = l;
        this.volR = r;
    }

    public void pausarAudio() {
        isPaused = true;
        if (audioTrack != null && audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            audioTrack.pause();
            audioTrack.flush();
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void reanudarAudio() {
        isPaused = false;
        if (audioTrack != null && audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            audioTrack.play();
        }
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    public void iniciarAudioEstereo(Context context) {
        if (isPlaying) {
            reanudarAudio();
            return;
        }

        // Iniciar Música de Fondo
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(context, R.raw.meditacion_fondo);
            mediaPlayer.setLooping(true);
            float volMusica = volumenGeneral * 0.4f;
            mediaPlayer.setVolume(volMusica, volMusica);
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Iniciar Ruido Blanco / Tono
        int bufferSize = AudioTrack.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize, AudioTrack.MODE_STREAM);

        isPlaying = true;
        isPaused = false;
        faseTono = 0;
        faseModulacion = 0;

        new Thread(() -> {
            short[] buffer = new short[bufferSize];
            Random random = new Random();

            while (isPlaying) {
                if (isPaused) {
                    try { Thread.sleep(50); } catch (InterruptedException e) { break; }
                    continue;
                }

                for (int i = 0; i < buffer.length; i += 2) {
                    double muestraBase;
                    if (esModoRuido) {
                        muestraBase = random.nextGaussian() * 0.5;
                    } else {
                        double frecuenciaTono = 440.0 * pitchFactor;
                        muestraBase = Math.sin(faseTono);
                        faseTono += 2.0 * Math.PI * frecuenciaTono / sampleRate;
                        if (faseTono > 2.0 * Math.PI) faseTono -= 2.0 * Math.PI;
                    }

                    double envolvente = faseModulacion / (2.0 * Math.PI);
                    faseModulacion += 2.0 * Math.PI * frecuenciaModulacion / sampleRate;
                    if (faseModulacion > 2.0 * Math.PI) faseModulacion -= 2.0 * Math.PI;

                    buffer[i] = (short) (muestraBase * envolvente * volL * volumenGeneral * Short.MAX_VALUE);
                    buffer[i + 1] = (short) (muestraBase * envolvente * volR * volumenGeneral * Short.MAX_VALUE);
                }

                if (audioTrack != null && audioTrack.getState() == AudioTrack.STATE_INITIALIZED && isPlaying && !isPaused) {
                    try {
                        audioTrack.write(buffer, 0, buffer.length);
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        }).start();

        audioTrack.play();
    }

    public void detener() {
        isPlaying = false;
        isPaused = false;
        if (audioTrack != null) {
            try {
                audioTrack.pause();
                audioTrack.flush();
                audioTrack.stop();
                audioTrack.release();
            } catch (Exception e) { e.printStackTrace(); }
            audioTrack = null;
        }
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) { e.printStackTrace(); }
            mediaPlayer = null;
        }
    }
}