package Controlador;

import Modelo.JuegoModelo;
import Modelo.JuegoModelo.EstadoJuego;
import Modelo.PacMan;
import Vista.JuegoPanel;
import Vista.VentanaPrincipal;

import javax.swing.*;
import java.awt.event.*;

/**
 * Controlador principal del juego (MVC - Controlador).
 * Gestiona eventos de teclado, el temporizador y el flujo del juego.
 */
public class JuegoControlador implements KeyListener {

    private JuegoModelo modelo;
    private VentanaPrincipal ventana;
    private Timer timerJuego; // Hilo de renderizado / lógica principal

    // Tasa de refresco: ~60 fps
    private static final int DELAY_MS = 16;

    public JuegoControlador(JuegoModelo modelo, VentanaPrincipal ventana) {
        this.modelo = modelo;
        this.ventana = ventana;

        // Registrar listener de teclado en el panel
        JuegoPanel panel = ventana.getJuegoPanel();
        panel.setFocusable(true);
        panel.addKeyListener(this);

        // Temporizador principal del juego (usa Swing Timer — hilo EDT)
        timerJuego = new Timer(DELAY_MS, e -> {
            modelo.actualizar();
            panel.repaint();

            // Detener el timer si el juego terminó
            EstadoJuego estado = modelo.getEstadoJuego();
            if (estado == EstadoJuego.VICTORIA || estado == EstadoJuego.GAME_OVER) {
                timerJuego.stop();
            }
        });
    }

    // ─── KeyListener ────────────────────────────────────────────────────────────

    @Override
    public void keyPressed(KeyEvent e) {
        int tecla = e.getKeyCode();
        EstadoJuego estado = modelo.getEstadoJuego();

        // ENTER: iniciar / reanudar
        if (tecla == KeyEvent.VK_ENTER) {
            if (estado == EstadoJuego.INICIO || estado == EstadoJuego.PAUSADO) {
                if (estado == EstadoJuego.INICIO) {
                    modelo.iniciarJuego();
                } else {
                    modelo.pausar();
                }
                if (!timerJuego.isRunning()) timerJuego.start();
                return;
            }
        }

        // P: pausar / reanudar en pleno juego
        if (tecla == KeyEvent.VK_P && estado == EstadoJuego.EN_CURSO) {
            modelo.pausar();
            return;
        }

        // R: reiniciar desde GAME_OVER o VICTORIA
        if (tecla == KeyEvent.VK_R &&
            (estado == EstadoJuego.GAME_OVER || estado == EstadoJuego.VICTORIA)) {
            timerJuego.stop();
            modelo.iniciarJuego();
            timerJuego.start();
            return;
        }

        // Movimiento — solo si el juego está en curso
        if (estado != EstadoJuego.EN_CURSO) return;

        PacMan pacman = modelo.getPacman();
        switch (tecla) {
            case KeyEvent.VK_UP:    case KeyEvent.VK_W:
                pacman.setDireccionPendiente(PacMan.Direccion.ARRIBA);    break;
            case KeyEvent.VK_DOWN:  case KeyEvent.VK_S:
                pacman.setDireccionPendiente(PacMan.Direccion.ABAJO);     break;
            case KeyEvent.VK_LEFT:  case KeyEvent.VK_A:
                pacman.setDireccionPendiente(PacMan.Direccion.IZQUIERDA); break;
            case KeyEvent.VK_RIGHT: case KeyEvent.VK_D:
                pacman.setDireccionPendiente(PacMan.Direccion.DERECHA);   break;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
