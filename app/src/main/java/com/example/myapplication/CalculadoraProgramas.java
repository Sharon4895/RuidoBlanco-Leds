package com.example.myapplication;

public class CalculadoraProgramas {

    // Extraemos solo la lógica de la frecuencia (Hz) según el minuto
    public static double obtenerFrecuencia(String programa, long msActual) {
        long min = msActual / 60000;

        switch (programa) {
            case "A0":
                if (min < 5) return mapearD(min, 0, 5, 18.0, 7.0);
                if (min < 12) return 7.0;
                if (min < 15) return mapearD(min, 12, 15, 7.0, 40.0);
                return 40.0;
            case "A1":
                if (min < 7) return mapearD(min, 0, 7, 18.0, 5.0);
                if (min < 20) return 5.0;
                if (min < 22) return mapearD(min, 20, 22, 5.0, 20.0);
                return 20.0;
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

            case "A3": return 20.0; 
            case "A4":
                if (min >= 25) return 0.0;
                double fase4 = (msActual % 240000) / 240000.0; 
                if (fase4 < 0.5) return mapearD((long)(fase4*100), 0, 50, 20.0, 12.0);
                else return mapearD((long)(fase4*100), 50, 100, 12.0, 20.0);
            case "A5":
                if (min >= 30) return 0.0;
                double fase5 = (msActual % 600000) / 600000.0;
                if (fase5 < 0.5) return mapearD((long)(fase5*100), 0, 50, 12.0, 5.0);
                else return mapearD((long)(fase5*100), 50, 100, 5.0, 12.0);
            case "A6":
                if (min >= 45) return 0.0;
                if (min < 8) return mapearD(min, 0, 8, 14.0, 10.0);
                if (min < 16) return mapearD(min, 8, 16, 10.0, 8.0);
                if (min < 24) return mapearD(min, 16, 24, 8.0, 6.0);
                if (min < 36) return mapearD(min, 24, 36, 6.0, 4.0);
                if (min < 40) return mapearD(min, 36, 40, 4.0, 1.0);
                return 1.0;
            case "B0":
                if (min < 8) return mapearD(min, 0, 8, 18.0, 7.83);
                if (min < 38) return 7.83;
                if (min < 40) return mapearD(min, 38, 40, 7.83, 20.0);
                return 20.0;
            case "B1":
                if (min >= 36) return 0.0;
                if (min < 7) return mapearD(min, 0, 7, 16.0, 4.0);
                if (min < 12) return mapearD(min, 7, 12, 4.0, 7.0);
                if (min < 18) return mapearD(min, 12, 18, 7.0, 4.0);
                if (min < 24) return mapearD(min, 18, 24, 4.0, 7.0);
                if (min < 30) return mapearD(min, 24, 30, 7.0, 5.0);
                if (min < 34) return mapearD(min, 30, 34, 5.0, 7.0);
                return mapearD(min, 34, 36, 7.0, 16.0);
            case "B2":
                if (min >= 30) return 0.0;
                if (min < 5) return mapearD(min, 0, 5, 16.0, 33.0);
                if (min < 10) return mapearD(min, 5, 10, 33.0, 7.0);
                if (min < 15) return mapearD(min, 10, 15, 7.0, 18.0);
                if (min < 20) return mapearD(min, 15, 20, 18.0, 7.0);
                if (min < 25) return mapearD(min, 20, 25, 7.0, 16.0);
                return mapearD(min, 25, 30, 16.0, 33.0);
            case "B3":
                if (min >= 45) return 0.0;
                if (min < 10) return mapearD(min, 0, 10, 18.0, 5.0);
                if (min < 15) return mapearD(min, 10, 15, 5.0, 7.0);
                if (min < 20) return mapearD(min, 15, 20, 7.0, 4.0);
                if (min < 30) return mapearD(min, 20, 30, 4.0, 7.0);
                if (min < 40) return mapearD(min, 30, 40, 7.0, 4.0);
                return mapearD(min, 40, 45, 4.0, 18.0);
            case "B4":
                if (min >= 25) return 0.0;
                if (min < 5) return mapearD(min, 0, 5, 14.0, 35.0);
                if (min < 15) return mapearD(min, 5, 15, 35.0, 10.0);
                if (min < 20) return 8.0;
                if (min < 23) return 8.0;
                return mapearD(min, 23, 25, 8.0, 20.0);
            case "B5":
                if (min >= 45) return 0.0;
                if (min < 10) return mapearD(min, 0, 10, 20.0, 5.0);
                if (min < 20) return mapearD(min, 10, 20, 5.0, 7.0);
                if (min < 30) return mapearD(min, 20, 30, 7.0, 5.0);
                if (min < 40) return mapearD(min, 30, 40, 5.0, 7.0);
                return mapearD(min, 40, 45, 7.0, 18.0);
            case "B6":
                if (min >= 45) return 0.0;
                if (min < 10) return mapearD(min, 0, 10, 15.0, 4.0);
                if (min < 40) return 4.0;
                return mapearD(min, 40, 45, 4.0, 15.0);
            case "AB0":
                if (min >= 60) return 0.0;
                if (min < 12) return mapearD(min, 0, 12, 15.0, 3.0);
                if (min < 24) return mapearD(min, 12, 24, 3.0, 5.0);
                if (min < 30) return mapearD(min, 24, 30, 5.0, 3.0);
                if (min < 36) return mapearD(min, 30, 36, 3.0, 6.0);
                if (min < 48) return mapearD(min, 36, 48, 6.0, 4.0);
                if (min < 50) return mapearD(min, 48, 50, 4.0, 5.0);
                return mapearD(min, 50, 60, 5.0, 30.0);
            case "AB1":
                if (min < 4) return mapearD(min, 0, 4, 12.0, 2.0);
                if (min < 14) return 2.0;
                return mapearD(min, 14, 18, 2.0, 15.0);
            case "AB2":
                // Audio: Alerta mental
                if (min < 8) return mapearD(min, 0, 8, 18.0, 6.0);
                if (min < 10) return 6.0;
                if (min < 25) return mapearD(min, 10, 25, 6.0, 30.0);
                return mapearD(min, 25, 40, 30.0, 33.0);
            case "AB3":
                if (min >= 30) return 0.0;
                if (min < 15) return mapearD(min, 0, 15, 31.0, 39.0);
                return mapearD(min, 15, 30, 39.0, 30.0);
            case "AB4":
                if (min >= 75) return 0.0;
                // Audio: Escala Delta profunda 1-2 Hz, luego sube a Alfa alto 16Hz
                if (min < 70) return mapearD(min, 0, 70, 1.0, 2.0);
                return mapearD(min, 70, 75, 2.0, 16.0);
            case "AB5": return 7.83; 
            case "AB6": return 10.0 + (5.0 * Math.sin(msActual / 5000.0));
            default: return 10.0;
        }
    }

    public static int obtenerAmplitud(String programa, long msActual, int brilloMax) {
        long min = msActual / 60000;
        if (programa.equals("AB4")) {
            if (min >= 75) return 0;
            if (min < 16) return (int)mapearD(min, 0, 16, brilloMax, brilloMax * 0.6);
            if (min < 70) return (int)(brilloMax * 0.6);
            return (int)mapearD(min, 70, 75, brilloMax * 0.6, brilloMax);
        }
        if (programa.equals("AB3")) {
            if (min >= 30) return 0;
            return brilloMax; // Amplitud máxima constante según gráfica
        }
        if (programa.equals("AB2")) {
            if (min >= 40) return 0;
            if (min < 8) return (int)mapearD(min, 0, 8, brilloMax, brilloMax * 0.75);
            if (min < 38) return (int)(brilloMax * 0.75);
            return (int)mapearD(min, 38, 40, brilloMax * 0.75, brilloMax);
        }
        if (programa.equals("AB0")) {
            if (min >= 60) return 0;
            if (min < 20) return (int)mapearD(min, 0, 20, brilloMax, brilloMax * 0.6);
            if (min < 50) return (int)(brilloMax * 0.6);
            return (int)mapearD(min, 50, 60, brilloMax * 0.6, brilloMax);
        }
        if (programa.equals("B6")) {
            if (min >= 45) return 0;
            if (min < 10) return (int)mapearD(min, 0, 10, brilloMax, brilloMax * 0.75);
            if (min < 40) return (int)(brilloMax * 0.75);
            return (int)mapearD(min, 40, 45, brilloMax * 0.75, brilloMax);
        }
        if (programa.equals("B5")) {
            if (min >= 45) return 0;
            if (min < 3) return (int)mapearD(min, 0, 3, brilloMax * 0.6, brilloMax);
            if (min < 10) return (int)mapearD(min, 3, 10, brilloMax, brilloMax * 0.75);
            if (min < 40) return (int)(brilloMax * 0.75);
            return (int)mapearD(min, 40, 45, brilloMax * 0.75, brilloMax);
        }
        if (programa.equals("B4")) {
            if (min >= 25) return 0;
            if (min < 10) return (int)mapearD(min, 0, 10, brilloMax * 0.6, brilloMax);
            if (min < 15) return (int)mapearD(min, 10, 15, brilloMax, brilloMax * 0.75);
            if (min < 20) return (int)mapearD(min, 15, 20, brilloMax * 0.75, brilloMax);
            if (min < 23) return (int)mapearD(min, 20, 23, brilloMax, brilloMax * 0.75);
            return (int)mapearD(min, 23, 25, brilloMax * 0.75, brilloMax);
        }
        if (programa.equals("B3")) {
            if (min >= 45) return 0;
            if (min < 4) return (int)mapearD(min, 0, 4, brilloMax * 0.75, brilloMax);
            if (min < 24) return (int)mapearD(min, 4, 24, brilloMax, brilloMax * 0.75);
            if (min < 40) return (int)(brilloMax * 0.75);
            return (int)mapearD(min, 40, 45, brilloMax * 0.75, brilloMax);
        }
        if (programa.equals("B2")) {
            if (min >= 30) return 0;
            return brilloMax;
        }
        if (programa.equals("B1")) {
            if (min >= 36) return 0;
            if (min < 7) return (int)mapearD(min, 0, 7, brilloMax, brilloMax * 0.75);
            if (min < 30) return (int)(brilloMax * 0.75);
            return (int)mapearD(min, 30, 36, brilloMax * 0.75, brilloMax);
        }
        if (programa.equals("B0")) {
            if (min < 6) return (int)mapearD(min, 0, 6, brilloMax, brilloMax * 0.8);
            if (min < 36) return (int)(brilloMax * 0.8);
            if (min < 40) return (int)mapearD(min, 36, 40, brilloMax * 0.8, brilloMax);
            return 0;
        }
        if (programa.equals("A6")) {
            if (min >= 45) return 0;
            return (int)mapearD(min, 0, 45, brilloMax, 0);
        }
        if (programa.equals("A5")) {
            if (min >= 30) return 0;
            double fase = (msActual % 600000) / 600000.0;
            if (fase < 0.5) return (int)mapearD((long)(fase*100), 0, 50, brilloMax, brilloMax * 0.75);
            else return (int)mapearD((long)(fase*100), 50, 100, brilloMax * 0.75, brilloMax);
        }
        if (programa.equals("A4")) {
            if (min >= 25) return 0;
            double fase = (msActual % 240000) / 240000.0;
            if (fase < 0.5) return (int)mapearD((long)(fase*100), 0, 50, brilloMax * 0.75, brilloMax);
            else return (int)mapearD((long)(fase*100), 50, 100, brilloMax, brilloMax * 0.75);
        }
        if (programa.equals("A1")) {
            if (min < 2) return (int)mapearD(min, 0, 2, brilloMax * 0.5, brilloMax);
            if (min < 7) return (int)mapearD(min, 2, 7, brilloMax, brilloMax * 0.6);
            if (min < 20) return (int)(brilloMax * 0.6);
            if (min < 22) return (int)mapearD(min, 20, 22, brilloMax * 0.6, brilloMax);
            return 0;
        }
        if (programa.equals("A0")) {
            if (min < 4) return (int)mapearD(min, 0, 4, brilloMax * 0.5, brilloMax);
            if (min < 6) return (int)mapearD(min, 4, 6, brilloMax, brilloMax * 0.8);
            if (min < 12) return (int)mapearD(min, 6, 12, brilloMax * 0.8, brilloMax * 0.5);
            if (min < 14) return (int)mapearD(min, 12, 14, brilloMax * 0.5, brilloMax);
            if (min < 15) return brilloMax;
            return 0;
        }
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
        int[] brillos = new int[2];

        if (frec <= 0 || brilloMax <= 0) return brillos;

        double frecVisual = frec;
        if (programa.equals("AB4")) {
            // Visual: Baja de 20 Hz a Delta 2 Hz
            if (min < 8) frecVisual = mapearD(min, 0, 8, 20.0, 5.0);
            else if (min < 70) frecVisual = mapearD(min, 8, 70, 5.0, 2.0);
            else frecVisual = mapearD(min, 70, 75, 2.0, 16.0);
        } else if (programa.equals("AB2")) {
            // Estímulo visual en niveles Delta (1-4 Hz)
            if (min < 10) frecVisual = mapearD(min, 0, 10, 18.0, 2.0);
            else if (min < 38) frecVisual = 3.0;
            else frecVisual = mapearD(min, 38, 40, 3.0, 20.0);
        }

        int valorOnda = ondaDienteSierra(msActual, frecVisual, brilloMax);

        int nPatron;
        if (programa.equals("AB4")) {
            nPatron = 5; // Patrón Bloqueado para escape Delta profundo
        } else if (programa.equals("AB3")) {
            nPatron = (min < 15) ? 5 : 2; // BLOQUEADO hasta min 15, luego ALTERNO
        } else if (programa.equals("AB2")) {
            nPatron = 5; // Bloqueado por defecto
        } else if (programa.equals("AB0")) {
            nPatron = 5;
        } else if (programa.equals("B6")) {
            nPatron = 5;
        } else if (programa.equals("B5")) {
            int rot = (int)(min / 5) % 3;
            if (rot == 0) nPatron = 2;
            else if (rot == 1) nPatron = 5;
            else nPatron = 4;
        } else if (programa.equals("B4")) {
            int rotacion = (int)(min / 5) % 3;
            if (rotacion == 0) nPatron = 5;
            else if (rotacion == 1) nPatron = 2;
            else nPatron = 4;
        } else if (programa.equals("B3")) {
            nPatron = 4;
        } else if (programa.equals("B2")) {
            nPatron = 2;
        } else if (programa.equals("B1")) {
            nPatron = 5;
        } else if (programa.equals("B0")) {
            nPatron = ((min / 2) % 2 == 0) ? 5 : 2;
        } else if (programa.equals("A6")) {
            nPatron = 5;
        } else if (programa.equals("A5")) {
            nPatron = 5;
        } else if (programa.equals("A4")) {
            nPatron = 4;
        } else if (programa.equals("A1")) {
            nPatron = (min < 7) ? 2 : 5;
        } else if (programa.equals("A0")) {
            nPatron = 5;
        } else if (programa.equals("A2")) {
            int ciclo = (int)(min / 3);
            if (ciclo % 3 == 0) nPatron = 2;
            else if (ciclo % 3 == 1) nPatron = 5;
            else nPatron = 4;
        } else {
            int ciclo = (int)(min / 3);
            if (ciclo % 3 == 0) nPatron = 2;
            else if (ciclo % 3 == 1) nPatron = 5;
            else nPatron = 4;
        }

        switch (nPatron) {
            case 2: 
                long periodo = (long)(1000 / frecVisual);
                if ((msActual % periodo) < (periodo / 2)) {
                    brillos[0] = valorOnda; brillos[1] = 0;
                } else {
                    brillos[0] = 0; brillos[1] = valorOnda;
                }
                break;
            case 5: 
                brillos[0] = valorOnda;
                brillos[1] = valorOnda;
                break;
            case 4: 
                brillos[0] = valorOnda;
                brillos[1] = ondaDienteSierra(msActual + (long)(1000/(2*frecVisual)), frecVisual, brilloMax);
                break;
        }
        return brillos;
    }
}