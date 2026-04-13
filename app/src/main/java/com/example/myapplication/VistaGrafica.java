package com.example.myapplication; // Revisa que este sea tu paquete correcto

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;

public class VistaGrafica extends View {

    private Paint paintLinea;
    private Paint paintLed;
    private ArrayList<Integer> historialBrillo;
    private int brilloActual = 0;

    public VistaGrafica(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Pincel para la onda tipo osciloscopio
        paintLinea = new Paint();
        paintLinea.setColor(Color.GREEN);
        paintLinea.setStrokeWidth(6f);
        paintLinea.setStyle(Paint.Style.STROKE);
        paintLinea.setAntiAlias(true);

        // Pincel para simular los LEDs
        paintLed = new Paint();
        paintLed.setColor(Color.YELLOW);
        paintLed.setStyle(Paint.Style.FILL);

        historialBrillo = new ArrayList<>();
    }

    // Método que llamaremos cada 100ms desde el ControlActivity
    public void actualizarGrafica(int brillo) {
        this.brilloActual = brillo;
        historialBrillo.add(brillo);

        // Mantener solo los últimos 50 puntos para que la onda "avance" hacia la izquierda
        if (historialBrillo.size() > 50) {
            historialBrillo.remove(0);
        }
        invalidate(); // Le dice a Android que redibuje la pantalla inmediatamente
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        // 1. Dibujar la onda (Diente de Sierra)
        if (historialBrillo.size() > 1) {
            Path path = new Path();
            float anchoPaso = (float) width / 50f; // 50 puntos máximo

            for (int i = 0; i < historialBrillo.size(); i++) {
                float x = i * anchoPaso;
                // Mapear el brillo (0-255) a la altura (Y invertido)
                float y = height - ((historialBrillo.get(i) / 255f) * height);

                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
            canvas.drawPath(path, paintLinea);
        }

        // 2. Dibujar LEDs virtuales (Círculos que brillan sincronizados)
        // La opacidad (Alpha) va de 0 a 255, exacto al PWM del ESP32
        paintLed.setAlpha(brilloActual);
        float radio = 30f;
        canvas.drawCircle(width - 100, 50, radio, paintLed); // LED 1
        canvas.drawCircle(width - 40, 50, radio, paintLed);  // LED 2
    }
}