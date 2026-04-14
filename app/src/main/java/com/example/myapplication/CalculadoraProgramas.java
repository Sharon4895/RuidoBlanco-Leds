package com.example.myapplication;

public class CalculadoraProgramas {

    // Extraemos solo la lógica de la frecuencia (Hz) según el minuto
    public static double obtenerFrecuencia(String programa, long msActual) {
        long min = msActual / 60000;

        switch (programa) {
            case "A2":
                if (min < 3) return mapearD(min, 0, 3, 8.0, 30.0);
                if (min < 6) return mapearD(min, 3, 6, 30.0, 6.0);
                if (min < 9) return mapearD(min, 6, 9, 6.0, 20.0);
                if (min < 12) return mapearD(min, 9, 12, 20.0, 4.0);
                if (min < 15) return mapearD(min, 12, 15, 4.0, 18.0);
                if (min < 18) return mapearD(min, 15, 18, 18.0, 6.0);
                if (min < 21) return mapearD(min, 18, 21, 6.0, 24.0);
                if (min < 24) return mapearD(min, 21, 24, 24.0, 10.0);
                if (min < 27) return mapearD(min, 24, 27, 10.0, 22.0);
                if (min < 29) return mapearD(min, 27, 29, 22.0, 6.0);
                return mapearD(min, 29, 30, 6.0, 30.0);

            case "A3": return 20.0; // Onda Beta constante según PDF
            case "B4": return 12.0; // Sintonía general (Alfa alto)
            case "AB1":
                if (min < 4) return mapearD(min, 0, 4, 12.0, 2.0);
                if (min < 14) return 2.0;
                return mapearD(min, 14, 18, 2.0, 15.0);
            case "AB5": return 7.83; // Frecuencia Shumann (Relajación profunda)
            case "AB6": return 10.0 + (5.0 * Math.sin(msActual / 5000.0)); // Caleidoscopio (Variación lenta)
            default: return 10.0;
        }
    }

    public static int obtenerAmplitud(String programa, long msActual, int brilloMax) {
        long min = msActual / 60000;
        if (programa.equals("A2")) {
            if (min < 3) return (int)mapearD(min, 0, 3, brilloMax, brilloMax * 0.75);
            if (min < 28) return (int)(brilloMax * 0.75);
            return (int)mapearD(min, 28, 30, brilloMax * 0.75, brilloMax);
        }
        return brilloMax;
    }

    public static int ondaDienteSierra(long msActual, double frecuenciaHz, int brilloMax) {
        if (frecuenciaHz <= 0) return 0;
        long periodoMs = (long) (1000.0 / frecuenciaHz);
        return (int) (((msActual % periodoMs) * brilloMax) / periodoMs);
    }

    public static double mapearD(long x, long in_min, long in_max, double out_min, double out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
    public static int[] obtenerBrillosPorPatron(String programa, long msActual, int brilloMax) {
        long min = msActual / 60000;
        double frec = obtenerFrecuencia(programa, msActual);
        int[] brillos = new int[2]; // [0] = Izquierdo, [1] = Derecho

        // Obtenemos el valor base de la onda diente de sierra
        int valorOnda = ondaDienteSierra(msActual, frec, brilloMax);

        // Determinamos el patrón (Para A2 según tu PDF)
        int nPatron = 2;
        int ciclo = (int)(min / 3);
        if (ciclo % 3 == 0) nPatron = 2;
        else if (ciclo % 3 == 1) nPatron = 5;
        else nPatron = 4;

        switch (nPatron) {
            case 2: // LOCKED EYE-EAR (Pero la tabla dice sincronizado a ojos, alterno izquierda a derecha)
                // Lógica de alternancia: si estamos en la primera mitad del periodo, prende L, si no R
                long periodo = (long)(1000 / frec);
                if ((msActual % periodo) < (periodo / 2)) {
                    brillos[0] = valorOnda; brillos[1] = 0;
                } else {
                    brillos[0] = 0; brillos[1] = valorOnda;
                }
                break;

            case 5: // LOCKED EYES (Ambos al mismo tiempo)
                brillos[0] = valorOnda;
                brillos[1] = valorOnda;
                break;

            case 4: // ALTERNATE EYES
                // Uno sube mientras el otro ya bajó o viceversa
                brillos[0] = valorOnda;
                // El derecho desfasado 180 grados
                brillos[1] = ondaDienteSierra(msActual + (long)(1000/(2*frec)), frec, brilloMax);
                break;
        }
        return brillos;
    }
}