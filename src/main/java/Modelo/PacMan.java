package Modelo;

/**
 * Clase PacMan. Hereda de Entidad e implementa Movible.
 * Aplica herencia y polimorfismo.
 */
public class PacMan extends Entidad implements Movible {

    public enum Direccion { ARRIBA, ABAJO, IZQUIERDA, DERECHA, NINGUNA }

    private int inicioX;
    private int inicioY;
    private Direccion direccion;
    private Direccion direccionPendiente;
    private int anguloBoca;
    private boolean bocaAbriendo;
    private boolean invulnerable;
    private int timerInvulnerable;

    public PacMan(int col, int fila) {
        super(col * Laberinto.TAM_CELDA, fila * Laberinto.TAM_CELDA, 3);
        this.inicioX = x;
        this.inicioY = y;
        this.direccion = Direccion.NINGUNA;
        this.direccionPendiente = Direccion.NINGUNA;
        this.anguloBoca = 45;
        this.bocaAbriendo = false;
    }

    @Override
    public void mover(Laberinto laberinto) {
        // Intentar aplicar la dirección pendiente si es válida
        if (direccionPendiente != Direccion.NINGUNA) {
            if (puedeMoverseEnDireccion(direccionPendiente, laberinto)) {
                direccion = direccionPendiente;
                direccionPendiente = Direccion.NINGUNA;
            }
        }

        if (invulnerable) {
            timerInvulnerable--;
            if (timerInvulnerable <= 0) invulnerable = false;
        }

        // Mover según dirección actual
        switch (direccion) {
            case ARRIBA:    moverArriba(laberinto); break;
            case ABAJO:     moverAbajo(laberinto); break;
            case IZQUIERDA: moverIzquierda(laberinto); break;
            case DERECHA:   moverDerecha(laberinto); break;
            default: break;
        }

        // Animación de boca
        if (bocaAbriendo) {
            anguloBoca += 5;
            if (anguloBoca >= 45) bocaAbriendo = false;
        } else {
            anguloBoca -= 5;
            if (anguloBoca <= 5) bocaAbriendo = true;
        }
    }

    private boolean puedeMoverseEnDireccion(Direccion dir, Laberinto laberinto) {
        int nx = x, ny = y;
        switch (dir) {
            case ARRIBA:    ny -= velocidad; break;
            case ABAJO:     ny += velocidad; break;
            case IZQUIERDA: nx -= velocidad; break;
            case DERECHA:   nx += velocidad; break;
        }
        int col = nx / Laberinto.TAM_CELDA;
        int fila = ny / Laberinto.TAM_CELDA;
        int colDer = (nx + Laberinto.TAM_CELDA - 1) / Laberinto.TAM_CELDA;
        int filaBaj = (ny + Laberinto.TAM_CELDA - 1) / Laberinto.TAM_CELDA;
        return !laberinto.esPared(col, fila) && !laberinto.esPared(colDer, fila)
            && !laberinto.esPared(col, filaBaj) && !laberinto.esPared(colDer, filaBaj);
    }

    @Override
    public void moverArriba(Laberinto laberinto) {
        int ny = y - velocidad;
        if (puedeMoverse(x, ny, laberinto)) y = ny;
    }

    @Override
    public void moverAbajo(Laberinto laberinto) {
        int ny = y + velocidad;
        if (puedeMoverse(x, ny, laberinto)) y = ny;
    }

    @Override
    public void moverIzquierda(Laberinto laberinto) {
        int nx = x - velocidad;
        if (puedeMoverse(nx, y, laberinto)) x = nx;
        // Túnel lateral
        if (x < 0) x = (Laberinto.COLUMNAS - 1) * Laberinto.TAM_CELDA;
    }

    @Override
    public void moverDerecha(Laberinto laberinto) {
        int nx = x + velocidad;
        if (puedeMoverse(nx, y, laberinto)) x = nx;
        // Túnel lateral
        if (x >= Laberinto.COLUMNAS * Laberinto.TAM_CELDA)
            x = 0;
    }

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
        direccion = Direccion.NINGUNA;
        direccionPendiente = Direccion.NINGUNA;
        invulnerable = true;
        timerInvulnerable = 120;
    }

    /**
     * Retorna la columna del centro de PacMan.
     */
    public int getColumna() {
        return (x + Laberinto.TAM_CELDA / 2) / Laberinto.TAM_CELDA;
    }

    /**
     * Retorna la fila del centro de PacMan.
     */
    public int getFila() {
        return (y + Laberinto.TAM_CELDA / 2) / Laberinto.TAM_CELDA;
    }

    public Direccion getDireccion() { return direccion; }
    public void setDireccion(Direccion d) { this.direccion = d; }
    public void setDireccionPendiente(Direccion d) { this.direccionPendiente = d; }
    public int getAnguloBoca() { return anguloBoca; }
    public boolean isInvulnerable() { return invulnerable; }
}
