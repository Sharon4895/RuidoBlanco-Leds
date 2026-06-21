package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class MiCanvasView extends View {
    private Bitmap fondoOriginal;
    private Bitmap frente;
    private Bitmap fondoEscalado;
    private Paint paint;
    private int posicionX = 0;
    private int ultimoAncho = -1;
    private int ultimoAlto = -1;

    // Gyroscope tilt offsets
    private float gyroOffsetX = 0f;
    private float gyroOffsetY = 0f;

    // Particle system and paints
    private java.util.List<Particle> particles = new java.util.ArrayList<>();
    private Paint paintParticula;
    private Paint paintBrillo;

    private static class Particle {
        float x, y;
        float radius;
        float speedY;
        float speedX;
        int alpha;
        int maxAlpha;
        boolean fadingIn;

        Particle(float x, float y, float radius, float speedY, float speedX, int maxAlpha) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.speedY = speedY;
            this.speedX = speedX;
            this.maxAlpha = maxAlpha;
            this.alpha = (int) (Math.random() * maxAlpha);
            this.fadingIn = Math.random() > 0.5;
        }

        void update(int width, int height) {
            y -= speedY;
            x += speedX;

            // Twinkle effect (fade in and out)
            if (fadingIn) {
                alpha += 2;
                if (alpha >= maxAlpha) {
                    alpha = maxAlpha;
                    fadingIn = false;
                }
            } else {
                alpha -= 2;
                if (alpha <= 10) {
                    alpha = 10;
                    fadingIn = true;
                }
            }

            // Reset if out of bounds or completely faded out
            if (y < -radius || x < -radius || x > width + radius) {
                y = height + radius;
                x = (float) (Math.random() * width);
                alpha = 0;
                fadingIn = true;
            }
        }
    }

    public MiCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);

        paintParticula = new Paint();
        paintParticula.setAntiAlias(true);
        paintParticula.setStyle(Paint.Style.FILL);

        paintBrillo = new Paint();
        paintBrillo.setAntiAlias(true);
        paintBrillo.setStyle(Paint.Style.FILL);
        
        try {
            fondoOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.imagen_fondo);
            frente = BitmapFactory.decodeResource(getResources(), R.drawable.imagen_frente);
            
            if (fondoOriginal == null) Log.e("MiCanvasView", "Error: No se pudo cargar imagen_fondo");
            if (frente == null) Log.e("MiCanvasView", "Error: No se pudo cargar imagen_frente");
        } catch (Exception e) {
            Log.e("MiCanvasView", "Excepción al cargar recursos: " + e.getMessage());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (fondoOriginal == null || frente == null) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        if (width > 0 && height > 0 && (width != ultimoAncho || height != ultimoAlto)) {
            ultimoAncho = width;
            ultimoAlto = height;
            // Scale vertically by 1.15 so we can scroll up/down with tilt without gaps
            fondoEscalado = Bitmap.createScaledBitmap(fondoOriginal, (int)(ultimoAncho * 1.6), (int)(ultimoAlto * 1.15), true);
            Log.d("MiCanvasView", "Fondo re-escalado a: " + fondoEscalado.getWidth() + "x" + fondoEscalado.getHeight());

            // Initialize particles when layout size changes/is determined
            particles.clear();
            for (int i = 0; i < 30; i++) {
                float px = (float) (Math.random() * width);
                float py = (float) (Math.random() * height);
                float radius = 6f + (float) (Math.random() * 10f);
                float speedY = 0.4f + (float) (Math.random() * 1.2f);
                float speedX = -0.2f + (float) (Math.random() * 0.4f);
                int maxAlpha = 100 + (int) (Math.random() * 120);
                particles.add(new Particle(px, py, radius, speedY, speedX, maxAlpha));
            }
        }

        // 1. Draw scrolling background with gyroscope tilt shift
        if (fondoEscalado != null) {
            float desplazamientoFondo = -(posicionX * 0.15f);
            float drawFondoX = desplazamientoFondo + gyroOffsetX * 0.4f; // Background shifts with tilt
            float maxScroll = width - fondoEscalado.getWidth();
            if (drawFondoX < maxScroll) drawFondoX = maxScroll;
            if (drawFondoX > 0) drawFondoX = 0;

            float verticalPadding = (fondoEscalado.getHeight() - height) / 2f;
            float drawFondoY = -verticalPadding + gyroOffsetY * 0.4f;

            canvas.drawBitmap(fondoEscalado, drawFondoX, drawFondoY, paint);
        }

        // 2. Update and draw floating particles with minor gyroscope shift (mid-depth)
        for (Particle p : particles) {
            p.update(width, height);
            float drawPX = p.x + gyroOffsetX * 0.1f;
            float drawPY = p.y + gyroOffsetY * 0.1f;
            paintParticula.setColor(android.graphics.Color.argb(p.alpha, 255, 236, 179));
            canvas.drawCircle(drawPX, drawPY, p.radius, paintParticula);
        }

        // 3. Draw glowing aura behind the moving object with inverse gyroscope shift (foreground depth)
        float timeFactor = System.currentTimeMillis() * 0.002f;
        float desplazamientoVertical = (float) Math.sin(timeFactor) * 30f;
        float yPosFrente = (height / 2f) - (frente.getHeight() / 2f) + desplazamientoVertical - gyroOffsetY * 0.8f;
        float drawFrenteX = (float) posicionX - gyroOffsetX * 0.8f;

        float centerX = drawFrenteX + frente.getWidth() / 2f;
        float centerY = yPosFrente + frente.getHeight() / 2f;

        // Outer aura (large, faint)
        paintBrillo.setColor(android.graphics.Color.argb(35, 255, 224, 130));
        canvas.drawCircle(centerX, centerY, frente.getWidth() * 0.8f, paintBrillo);

        // Inner aura (smaller, brighter)
        paintBrillo.setColor(android.graphics.Color.argb(70, 255, 236, 179));
        canvas.drawCircle(centerX, centerY, frente.getWidth() * 0.5f, paintBrillo);

        // 4. Draw the foreground moving object
        canvas.drawBitmap(frente, drawFrenteX, yPosFrente, paint);

        // 5. Keep rendering loop alive for smooth animations
        postInvalidateOnAnimation();
    }

    public void actualizarOffsetsGiroscopio(float x, float y) {
        this.gyroOffsetX = x;
        this.gyroOffsetY = y;
        postInvalidateOnAnimation();
    }

    public void actualizarPosicion(int nuevaX) {
        this.posicionX = nuevaX;
        postInvalidate();
    }
}
