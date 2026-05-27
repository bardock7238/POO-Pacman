package model;

/**
 * Interfaz que define el comportamiento de movimiento.
 * Aplica el principio de abstracción mediante interfaces.
 */
public interface Movible {
    void moverArriba(Laberinto laberinto);
    void moverAbajo(Laberinto laberinto);
    void moverIzquierda(Laberinto laberinto);
    void moverDerecha(Laberinto laberinto);
    boolean puedeMoverse(int nuevaX, int nuevaY, Laberinto laberinto);
}
