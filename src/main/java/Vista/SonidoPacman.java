package Vista;

import javax.sound.sampled.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Motor de sonido — síntesis PCM pura (javax.sound.sampled).
 * Sin archivos externos. Volumen global ajustable en tiempo real.
 */
public class SonidoPacman {

    private static final int SAMPLE_RATE = 44100;

    /** Volumen global 0.0 (mudo) → 1.0 (máximo). */
    private static volatile double volumen = 0.6;

    private static final ExecutorService pool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "Hilo-Audio");
        t.setDaemon(true);
        return t;
    });

    // ── Waka-waka continuo ───────────────────────────────────────────────────
    private static volatile boolean wakaActivo = false;
    private static volatile Thread  hiloWaka   = null;

    // ── API de volumen ───────────────────────────────────────────────────────

    public static double getVolumen() { return volumen; }

    public static void setVolumen(double v) {
        volumen = Math.max(0.0, Math.min(1.0, v));
    }

    // ── Sonidos ──────────────────────────────────────────────────────────────

    public static void iniciarWaka() {
        if (wakaActivo) return;
        wakaActivo = true;
        hiloWaka = new Thread(() -> {
            while (wakaActivo) {
                tono(new double[]{370, 260}, new int[]{55, 55}, 0.30);
                try { Thread.sleep(8); } catch (InterruptedException e) { break; }
            }
        }, "Hilo-Waka");
        hiloWaka.setDaemon(true);
        hiloWaka.start();
    }

    public static void detenerWaka() {
        wakaActivo = false;
        if (hiloWaka != null) hiloWaka.interrupt();
    }

    /** Bip corto de pausa. */
    public static void pausa() {
        pool.submit(() -> tono(new double[]{440, 330}, new int[]{70, 70}, 0.25));
    }

    /** Power-up: acorde brillante ascendente. */
    public static void powerUp() {
        pool.submit(() -> tono(
            new double[]{440, 554, 659, 880},
            new int[]{80,  80,  80, 160},
            0.42
        ));
    }

    /** Pac-Man pierde una vida: glissando descendente corto. */
    public static void muerte() {
        detenerWaka();
        pool.submit(() -> {
            int durMs = 700;
            int n = SAMPLE_RATE * durMs / 1000;
            byte[] buf = new byte[n * 2];
            for (int i = 0; i < n; i++) {
                double t    = (double) i / SAMPLE_RATE;
                double freq = 580 * Math.exp(-4.0 * t);
                double amp  = volumen * 0.55 * Math.exp(-2.2 * t);
                short  s    = pcm(amp * Math.sin(2 * Math.PI * freq * t));
                buf[i*2]   = (byte)(s & 0xFF);
                buf[i*2+1] = (byte)((s >> 8) & 0xFF);
            }
            play(buf, n);
        });
    }

    /**
     * Game Over: melodía descendente dramática de 5 notas,
     * distinta y más larga que la de perder una vida.
     */
    public static void gameOver() {
        detenerWaka();
        pool.submit(() -> {
            // Cuatro notas dramáticas bajando + silencio + nota final grave
            double[] freqs = {392, 349, 311, 277,   0, 196};
            int[]    durs  = {200, 200, 200, 200, 100, 500};
            tono(freqs, durs, 0.50);
        });
    }

    /** Comer fantasma asustado: glissando ascendente. */
    public static void comerFantasma() {
        pool.submit(() -> {
            int durMs = 280;
            int n = SAMPLE_RATE * durMs / 1000;
            byte[] buf = new byte[n * 2];
            double dur = durMs / 1000.0;
            for (int i = 0; i < n; i++) {
                double t    = (double) i / SAMPLE_RATE;
                double freq = 200 + 700 * (t / dur);
                double amp  = volumen * 0.38 * (1 - t / dur);
                short  s    = pcm(amp * Math.sin(2 * Math.PI * freq * t));
                buf[i*2]   = (byte)(s & 0xFF);
                buf[i*2+1] = (byte)((s >> 8) & 0xFF);
            }
            play(buf, n);
        });
    }

    /** Victoria: fanfare alegre ascendente. */
    public static void victoria() {
        detenerWaka();
        pool.submit(() -> tono(
            new double[]{523, 659, 784, 1047, 784, 1047},
            new int[]   {110, 110, 110,  280, 110,  400},
            0.40
        ));
    }

    /** Intro al comenzar a jugar. */
    public static void intro() {
        pool.submit(() -> tono(
            new double[]{330,415,494,330,415,587,494,415,523,659,784,523,659,880},
            new int[]   {110,110,110,110,110,110,110,110,110,110,110,110,110,280},
            0.32
        ));
    }

    // ── Síntesis interna ─────────────────────────────────────────────────────

    private static void tono(double[] freqs, int[] durs, double ampBase) {
        int total = 0;
        for (int d : durs) total += SAMPLE_RATE * d / 1000;
        byte[] buf = new byte[total * 2];
        int idx = 0;
        double fase = 0;

        for (int n = 0; n < freqs.length; n++) {
            int muestras = SAMPLE_RATE * durs[n] / 1000;
            if (muestras == 0) continue;

            // Silencio si freq == 0
            if (freqs[n] == 0) {
                idx += muestras;
                continue;
            }

            double omega = 2 * Math.PI * freqs[n] / SAMPLE_RATE;
            int at = Math.min(muestras / 8, 80);
            int re = Math.min(muestras / 8, 80);

            for (int i = 0; i < muestras; i++) {
                double env = 1.0;
                if (i < at)            env = (double) i / at;
                if (i > muestras - re) env = (double)(muestras - i) / re;

                short s = pcm(volumen * ampBase * env * Math.sin(fase));
                buf[idx*2]   = (byte)(s & 0xFF);
                buf[idx*2+1] = (byte)((s >> 8) & 0xFF);
                fase += omega;
                idx++;
            }
        }
        play(buf, idx);
    }

    private static short pcm(double v) {
        return (short)(Math.max(-1.0, Math.min(1.0, v)) * Short.MAX_VALUE);
    }

    private static void play(byte[] buf, int muestras) {
        if (volumen <= 0.001) return;
        try {
            AudioFormat fmt  = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            DataLine.Info di = new DataLine.Info(SourceDataLine.class, fmt);
            if (!AudioSystem.isLineSupported(di)) return;
            SourceDataLine linea = (SourceDataLine) AudioSystem.getLine(di);
            linea.open(fmt, muestras * 2);
            linea.start();
            linea.write(buf, 0, muestras * 2);
            linea.drain();
            linea.close();
        } catch (LineUnavailableException | IllegalArgumentException ignored) {}
    }
}
