package Modelo;

import java.awt.Color;
import java.util.Random;

/**
 * Clase Fantasma. Hereda de Entidad e implementa Runnable para concurrencia.
 * Cada fantasma corre en su propio hilo.
 */
public class Fantasma extends Entidad implements Runnable, Movible {

    public enum EstadoFantasma { NORMAL, ASUSTADO, MUERTO }

    private int inicioX;
    private int inicioY;
    private Color color;
    private String nombre;
    private EstadoFantasma estado;
    private Random random;
    private int timerAsustado;
    private volatile boolean corriendo;
    private JuegoModelo modelo; // referencia al modelo para sincronización

    // Dirección actual del fantasma
    private int[] dirActual = {0, 0}; // {dx, dy}

    public Fantasma(int col, int fila, Color color, String nombre, JuegoModelo modelo) {
        super(col * Laberinto.TAM_CELDA, fila * Laberinto.TAM_CELDA, 2);
        this.inicioX = x;
        this.inicioY = y;
        this.color = color;
        this.nombre = nombre;
        this.estado = EstadoFantasma.NORMAL;
        this.random = new Random();
        this.modelo = modelo;
        this.corriendo = false;
        elegirDireccionAleatoria();
    }

    @Override
    public void run() {
        corriendo = true;
        while (corriendo) {
            try {
                Thread.sleep(estado == EstadoFantasma.ASUSTADO ? 200 : 120);
                if (modelo.getEstadoJuego() == JuegoModelo.EstadoJuego.EN_CURSO) {
                    synchronized (modelo) {
                        mover(modelo.getLaberinto());
                    }
                }
                if (timerAsustado > 0) {
                    timerAsustado--;
                    if (timerAsustado == 0) estado = EstadoFantasma.NORMAL;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                corriendo = false;
            }
        }
    }

    @Override
    public void mover(Laberinto laberinto) {
        // Intenta continuar en la dirección actual; si no puede, elige otra
        int nx = x + dirActual[0] * velocidad;
        int ny = y + dirActual[1] * velocidad;

        if (puedeMoverse(nx, ny, laberinto)) {
            x = nx;
            y = ny;
            // Con cierta probabilidad, cambia de dirección en intersecciones
            if (random.nextInt(15) == 0) elegirDireccionAleatoria();
        } else {
            elegirDireccionAleatoria();
        }

        // Túnel lateral
        if (x < 0) x = (Laberinto.COLUMNAS - 1) * Laberinto.TAM_CELDA;
        if (x >= Laberinto.COLUMNAS * Laberinto.TAM_CELDA) x = 0;
    }

    private void elegirDireccionAleatoria() {
        int[][] dirs = {{0,-1},{0,1},{-1,0},{1,0}};
        // No ir en sentido contrario (evitar rebotes)
        int[] contrario = {-dirActual[0], -dirActual[1]};
        int intentos = 0;
        do {
            int idx = random.nextInt(4);
            dirActual = dirs[idx];
            intentos++;
        } while (dirActual[0] == contrario[0] && dirActual[1] == contrario[1] && intentos < 8);
    }

    @Override
    public void moverArriba(Laberinto l) { dirActual = new int[]{0, -1}; }
    @Override
    public void moverAbajo(Laberinto l) { dirActual = new int[]{0, 1}; }
    @Override
    public void moverIzquierda(Laberinto l) { dirActual = new int[]{-1, 0}; }
    @Override
    public void moverDerecha(Laberinto l) { dirActual = new int[]{1, 0}; }

    @Override
    public boolean puedeMoverse(int nx, int ny, Laberinto laberinto) {
        int col = nx / Laberinto.TAM_CELDA;
        int fila = ny / Laberinto.TAM_CELDA;
        int colDer = (nx + Laberinto.TAM_CELDA - 1) / Laberinto.TAM_CELDA;
        int filaBaj = (ny + Laberinto.TAM_CELDA - 1) / Laberinto.TAM_CELDA;
        return !laberinto.esPared(col, fila) && !laberinto.esPared(colDer, fila)
            && !laberinto.esPared(col, filaBaj) && !laberinto.esPared(colDer, filaBaj);
    }

    @Override
    public void reiniciar() {
        x = inicioX;
        y = inicioY;
        estado = EstadoFantasma.NORMAL;
        timerAsustado = 0;
        elegirDireccionAleatoria();
    }

    public void asustar() {
        if (estado != EstadoFantasma.MUERTO) {
            estado = EstadoFantasma.ASUSTADO;
            timerAsustado = 200;
            // Invertir dirección
            dirActual[0] = -dirActual[0];
            dirActual[1] = -dirActual[1];
        }
    }

    public void detener() {
        corriendo = false;
    }

    public boolean colisionaCon(PacMan pacman) {
        int dx = Math.abs(x - pacman.getX());
        int dy = Math.abs(y - pacman.getY());
        return dx < Laberinto.TAM_CELDA - 4 && dy < Laberinto.TAM_CELDA - 4;
    }

    public Color getColor() { return color; }
    public String getNombre() { return nombre; }
    public EstadoFantasma getEstado() { return estado; }
    public boolean isCorriendo() { return corriendo; }
}
