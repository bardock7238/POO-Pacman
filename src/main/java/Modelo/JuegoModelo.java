package Modelo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo principal del juego (MVC - Modelo).
 * Contiene el estado, las entidades y las reglas del juego.
 */
public class JuegoModelo {

    public enum EstadoJuego { INICIO, EN_CURSO, PAUSADO, VICTORIA, GAME_OVER }

    private Laberinto laberinto;
    private PacMan pacman;
    private List<Fantasma> fantasmas;
    private List<Thread> hilosFantasmas;

    private int puntuacion;
    private int vidas;
    private EstadoJuego estadoJuego;

    private static final int VIDAS_INICIALES = 3;

    public JuegoModelo() {
        laberinto = new Laberinto();
        inicializarEntidades();
        estadoJuego = EstadoJuego.INICIO;
    }

    private void inicializarEntidades() {
        pacman = new PacMan(9, 16);

        fantasmas = new ArrayList<>();
        hilosFantasmas = new ArrayList<>();

        fantasmas.add(new Fantasma(9,  8, Color.RED,                  "Blinky", this));
        fantasmas.add(new Fantasma(8,  8, new Color(255,184,255),     "Pinky",  this));
        fantasmas.add(new Fantasma(10, 8, new Color(0,255,255),       "Inky",   this));
        fantasmas.add(new Fantasma(9, 10, Color.ORANGE,               "Clyde",  this));

        puntuacion = 0;
        vidas = VIDAS_INICIALES;
    }

    public void iniciarJuego() {
        detenerHilosFantasmas();
        laberinto.inicializar();
        inicializarEntidades();
        estadoJuego = EstadoJuego.EN_CURSO;
        iniciarHilosFantasmas();
    }

    public synchronized void actualizar() {
        if (estadoJuego != EstadoJuego.EN_CURSO) return;

        pacman.mover(laberinto);

        int col = pacman.getColumna();
        int fila = pacman.getFila();
        boolean eraPowerUp = laberinto.esPowerUp(col, fila);
        int puntos = laberinto.consumirPellet(col, fila);
        puntuacion += puntos;

        if (eraPowerUp && puntos > 0) {
            for (Fantasma f : fantasmas) f.asustar();
        }

        if (laberinto.todosConsumidos()) {
            estadoJuego = EstadoJuego.VICTORIA;
            detenerHilosFantasmas();
            return;
        }

        if (!pacman.isInvulnerable()) {
            for (Fantasma f : fantasmas) {
                if (!f.isActivo()) continue;
                if (f.colisionaCon(pacman)) {
                    if (f.getEstado() == Fantasma.EstadoFantasma.ASUSTADO) {
                        puntuacion += 200;
                        f.setActivo(false);
                        final Fantasma fantasmaCapturado = f;
                        Thread hiloRespawn = new Thread(() -> {
                            try {
                                long tiempoRestante = 6000;
                                long intervalo = 100;
                                while (tiempoRestante > 0) {
                                    Thread.sleep(intervalo);
                                    if (getEstadoJuego() == EstadoJuego.EN_CURSO) {
                                        tiempoRestante -= intervalo;
                                    }
                                }
                                fantasmaCapturado.reiniciar();
                                fantasmaCapturado.setActivo(true);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }, "Hilo-Respawn-" + f.getNombre());
                        hiloRespawn.setDaemon(true);
                        hiloRespawn.start();
                    } else {
                        perderVida();
                        return;
                    }
                }
            }
        }
    }

    private void perderVida() {
        vidas--;
        if (vidas <= 0) {
            estadoJuego = EstadoJuego.GAME_OVER;
            detenerHilosFantasmas();
        } else {
            pacman.reiniciar();
            for (Fantasma f : fantasmas) f.reiniciar();
        }
    }

    public void pausar() {
        if (estadoJuego == EstadoJuego.EN_CURSO) estadoJuego = EstadoJuego.PAUSADO;
        else if (estadoJuego == EstadoJuego.PAUSADO) estadoJuego = EstadoJuego.EN_CURSO;
    }

    private void iniciarHilosFantasmas() {
        hilosFantasmas.clear();
        for (Fantasma f : fantasmas) {
            Thread hilo = new Thread(f, "Hilo-" + f.getNombre());
            hilo.setDaemon(true);
            hilosFantasmas.add(hilo);
            hilo.start();
        }
    }

    public void detenerHilosFantasmas() {
        for (Fantasma f : fantasmas) f.detener();
        for (Thread t : hilosFantasmas) t.interrupt();
    }

    // Getters
    public Laberinto getLaberinto() { return laberinto; }
    public PacMan getPacman() { return pacman; }
    public List<Fantasma> getFantasmas() { return fantasmas; }
    public int getPuntuacion() { return puntuacion; }
    public int getVidas() { return vidas; }
    public EstadoJuego getEstadoJuego() { return estadoJuego; }
    public void setEstadoJuego(EstadoJuego e) { this.estadoJuego = e; }
}