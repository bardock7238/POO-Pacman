package Controlador;

import Modelo.JuegoModelo;
import Modelo.JuegoModelo.EstadoJuego;
import Modelo.PacMan;
import Vista.JuegoVista;
import Vista.MenuPrincipal;
import Vista.SonidoPacman;
import Vista.VolumenHUD;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * Controlador JavaFX puro del juego (MVC - Controlador).
 * Gestiona input, loop de juego y eventos de sonido.
 */
public class JuegoControladorFX {

    private JuegoModelo modelo;
    private JuegoVista  vista;
    private Canvas      canvas;
    private Stage       escenario;
    private VolumenHUD  volumenHUD;
    private AnimationTimer loop;

    // Tick del modelo: ~50 fps — velocidad cómoda
    private static final long NS_POR_TICK = 20_000_000L;
    private long ultimoTick = 0;

    // Estado anterior para detectar eventos y disparar sonidos
    private int puntuacionAnterior  = 0;
    private int vidasAnteriores     = 3;
    private EstadoJuego estadoAnterior = EstadoJuego.INICIO;
    private boolean wakaSonando     = false;

    public JuegoControladorFX(JuegoModelo modelo, JuegoVista vista,
                               Canvas canvas, Stage escenario, VolumenHUD volumenHUD) {
        this.modelo     = modelo;
        this.vista      = vista;
        this.canvas     = canvas;
        this.escenario  = escenario;
        this.volumenHUD = volumenHUD;

        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed(this::manejarTecla);

        loop = new AnimationTimer() {
            @Override
            public void handle(long ahora) {
                if (ahora - ultimoTick >= NS_POR_TICK) {
                    ultimoTick = ahora;
                    modelo.actualizar();
                    procesarSonidos();
                }
                GraphicsContext gc = canvas.getGraphicsContext2D();
                vista.render(gc);
            }
        };
    }

    public void iniciar() {
        canvas.requestFocus();
        loop.start();
    }

    public void detener() {
        loop.stop();
        SonidoPacman.detenerWaka();
        modelo.detenerHilosFantasmas();
    }

    // ── Eventos de sonido ─────────────────────────────────────────────────────

    private void procesarSonidos() {
        EstadoJuego estado    = modelo.getEstadoJuego();
        int         puntuacion = modelo.getPuntuacion();
        int         vidas      = modelo.getVidas();
        PacMan.Direccion dir   = modelo.getPacman().getDireccion();

        // Cambio de estado
        if (estado != estadoAnterior) {
            switch (estado) {
                case EN_CURSO:
                    if (estadoAnterior == EstadoJuego.INICIO) SonidoPacman.intro();
                    break;
                case VICTORIA:
                    SonidoPacman.detenerWaka();
                    wakaSonando = false;
                    SonidoPacman.victoria();
                    Vista.Tablero.guardarPuntaje("Jugador", puntuacion);
                    break;
                case GAME_OVER:
                    SonidoPacman.detenerWaka();
                    wakaSonando = false;
                    SonidoPacman.gameOver();
                    Vista.Tablero.guardarPuntaje("Jugador", puntuacion);
                    break;
                default:
                    break;
            }
            estadoAnterior = estado;
        }

        if (estado != EstadoJuego.EN_CURSO) {
            if (wakaSonando) { SonidoPacman.detenerWaka(); wakaSonando = false; }
            vidasAnteriores    = vidas;
            puntuacionAnterior = puntuacion;
            return;
        }

        // Perdió vida
        if (vidas < vidasAnteriores) {
            SonidoPacman.detenerWaka();
            wakaSonando = false;
            SonidoPacman.muerte();
            vidasAnteriores    = vidas;
            puntuacionAnterior = puntuacion;
            return;
        }
        vidasAnteriores = vidas;

        // Comió algo
        if (puntuacion > puntuacionAnterior) {
            int diff = puntuacion - puntuacionAnterior;
            if      (diff >= 200) SonidoPacman.comerFantasma();
            else if (diff >= 50)  SonidoPacman.powerUp();
            // pellet normal cubierto por waka-waka
            puntuacionAnterior = puntuacion;
        }

        // Waka-waka: solo si Pac-Man se mueve
        boolean moviendose = dir != PacMan.Direccion.NINGUNA;
        if (moviendose && !wakaSonando) {
            SonidoPacman.iniciarWaka();
            wakaSonando = true;
        } else if (!moviendose && wakaSonando) {
            SonidoPacman.detenerWaka();
            wakaSonando = false;
        }
    }

    // ── Teclado ───────────────────────────────────────────────────────────────

    private void manejarTecla(KeyEvent e) {
        KeyCode     tecla  = e.getCode();
        EstadoJuego estado = modelo.getEstadoJuego();

        // ── Volumen: flechas arriba/abajo cuando el HUD está visible,
        //    o con V para mostrarlo / subirlo ──────────────────────────
        if (tecla == KeyCode.V) {
            volumenHUD.mostrar();
            return;
        }
        if (tecla == KeyCode.UP && volumenHUD.isVisible()) {
            volumenHUD.subir();
            return;
        }
        if (tecla == KeyCode.DOWN && volumenHUD.isVisible()) {
            volumenHUD.bajar();
            return;
        }

        // ── Controles del juego ───────────────────────────────────────
        if (tecla == KeyCode.ENTER) {
            if (estado == EstadoJuego.INICIO) {
                modelo.iniciarJuego();
            } else if (estado == EstadoJuego.PAUSADO) {
                modelo.pausar();
            }
            return;
        }

        if (tecla == KeyCode.P &&
            (estado == EstadoJuego.EN_CURSO || estado == EstadoJuego.PAUSADO)) {
            modelo.pausar();
            if (estado == EstadoJuego.EN_CURSO) {
                SonidoPacman.detenerWaka();
                wakaSonando = false;
                SonidoPacman.pausa();
            }
            return;
        }

        if (tecla == KeyCode.R &&
            (estado == EstadoJuego.PAUSADO ||
             estado == EstadoJuego.GAME_OVER ||
             estado == EstadoJuego.VICTORIA)) {
            SonidoPacman.detenerWaka();
            wakaSonando        = false;
            puntuacionAnterior = 0;
            vidasAnteriores    = 3;
            estadoAnterior     = EstadoJuego.INICIO;
            modelo.iniciarJuego();
            return;
        }

        if (tecla == KeyCode.ESCAPE && estado == EstadoJuego.PAUSADO) {
            detener();
            escenario.close();
            MenuPrincipal.mostrarMenu();
            return;
        }

        if (estado != EstadoJuego.EN_CURSO) return;

        // ── Movimiento ────────────────────────────────────────────────
        PacMan pacman = modelo.getPacman();
        switch (tecla) {
            case UP:    if (!volumenHUD.isVisible()) pacman.setDireccionPendiente(PacMan.Direccion.ARRIBA);    break;
            case DOWN:  if (!volumenHUD.isVisible()) pacman.setDireccionPendiente(PacMan.Direccion.ABAJO);     break;
            case LEFT:  pacman.setDireccionPendiente(PacMan.Direccion.IZQUIERDA); break;
            case RIGHT: pacman.setDireccionPendiente(PacMan.Direccion.DERECHA);   break;
            case W:     pacman.setDireccionPendiente(PacMan.Direccion.ARRIBA);    break;
            case S:     pacman.setDireccionPendiente(PacMan.Direccion.ABAJO);     break;
            case A:     pacman.setDireccionPendiente(PacMan.Direccion.IZQUIERDA); break;
            case D:     pacman.setDireccionPendiente(PacMan.Direccion.DERECHA);   break;
            default:    break;
        }
    }
}
