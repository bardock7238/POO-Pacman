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
        dirActual = new int[]{1, 0};
    }

    @Override
    public void run() {
        corriendo = true;
        while (corriendo) {
            try {
                Thread.sleep(estado == EstadoFantasma.ASUSTADO ? 32 : 16);
                if (modelo.getEstadoJuego() == JuegoModelo.EstadoJuego.EN_CURSO) {
                    synchronized (modelo) {
                        if (activo) mover(modelo.getLaberinto());
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
    int nx = x + dirActual[0] * velocidad;
    int ny = y + dirActual[1] * velocidad;

    if (puedeMoverse(nx, ny, laberinto)) {
        x = nx;
        y = ny;
        if (random.nextInt(30) == 0) elegirNuevaDireccion(laberinto);
    } else {
        elegirNuevaDireccion(laberinto);
    }

    if (x < 0) x = (Laberinto.COLUMNAS - 1) * Laberinto.TAM_CELDA;
    if (x >= Laberinto.COLUMNAS * Laberinto.TAM_CELDA) x = 0;
}

private void elegirNuevaDireccion(Laberinto laberinto) {
    int[][] dirs = {{0,-1},{0,1},{-1,0},{1,0}};
    int[] contrario = {-dirActual[0], -dirActual[1]};

    // Primero intenta cualquier dirección válida que no sea el contrario
    java.util.List<int[]> validas = new java.util.ArrayList<>();
    for (int[] dir : dirs) {
        if (dir[0] == contrario[0] && dir[1] == contrario[1]) continue;
        int nx = x + dir[0] * velocidad;
        int ny = y + dir[1] * velocidad;
        if (puedeMoverse(nx, ny, laberinto)) validas.add(dir);
    }

    if (!validas.isEmpty()) {
        dirActual = validas.get(random.nextInt(validas.size()));
    } else {
        // Si no hay otra opción, permite el contrario
        dirActual = contrario;
    }
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
    dirActual = new int[]{1, 0};
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
