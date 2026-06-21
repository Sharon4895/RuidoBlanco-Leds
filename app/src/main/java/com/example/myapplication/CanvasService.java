package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class CanvasService extends Service {

    private boolean corriendo = false;
    private int posicionX = -200; 
    private Thread hiloServicio;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("CanvasService", "Servicio iniciado");
        if (!corriendo) {
            corriendo = true;

            hiloServicio = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (corriendo) {
                        try {
                            Thread.sleep(30); 
                            posicionX += 8;   

                            if (posicionX > 1200) {
                                posicionX = -400; 
                            }

                            Intent broadcastIntent = new Intent("ACTUALIZAR_CANVAS");
                            broadcastIntent.setPackage(getPackageName()); 
                            broadcastIntent.putExtra("posicionX", posicionX);
                            sendBroadcast(broadcastIntent);

                        } catch (InterruptedException e) {
                            Log.e("CanvasService", "Hilo interrumpido");
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            });
            hiloServicio.start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("CanvasService", "Servicio destruido");
        corriendo = false;
        if (hiloServicio != null) {
            hiloServicio.interrupt();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
