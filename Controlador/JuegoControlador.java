package Controlador;

import Modelo.JuegoModelo;
import Modelo.JuegoModelo.EstadoJuego;
import Modelo.PacMan;
import Vista.JuegoPanel;
import Vista.VentanaPrincipal;
import java.awt.event.*;
import javax.swing.*;

public class JuegoControlador implements KeyListener {

    private JuegoModelo modelo;
    private VentanaPrincipal ventana;
    private Timer timerJuego;

    private static final int DELAY_MS = 16;

    public JuegoControlador(JuegoModelo modelo, VentanaPrincipal ventana) {
        this.modelo = modelo;
        this.ventana = ventana;

        JuegoPanel panel = ventana.getJuegoPanel();
        panel.setFocusable(true);
        panel.addKeyListener(this);

        timerJuego = new Timer(DELAY_MS, e -> {
            modelo.actualizar();
            panel.repaint();

            EstadoJuego estado = modelo.getEstadoJuego();
            if (estado == EstadoJuego.VICTORIA || estado == EstadoJuego.GAME_OVER) {
                timerJuego.stop();
                // Guardar puntaje automáticamente al terminar
                Vista.Tablero.guardarPuntaje("Jugador", modelo.getPuntuacion());
            }
        });
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int tecla = e.getKeyCode();
        EstadoJuego estado = modelo.getEstadoJuego();

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

        if (tecla == KeyEvent.VK_P && (estado == EstadoJuego.EN_CURSO || estado == EstadoJuego.PAUSADO)) {
            modelo.pausar();
            return;
        }

        if (tecla == KeyEvent.VK_R && estado == EstadoJuego.PAUSADO) {
            timerJuego.stop();
            modelo.iniciarJuego();
            timerJuego.start();
            return;
        }

        if (tecla == KeyEvent.VK_ESCAPE && estado == EstadoJuego.PAUSADO) {
            timerJuego.stop();
            modelo.detenerHilosFantasmas();
            ventana.dispose();
            Vista.MenuPrincipal.mostrarMenu();
            return;
        }

        if (tecla == KeyEvent.VK_R &&
            (estado == EstadoJuego.GAME_OVER || estado == EstadoJuego.VICTORIA)) {
            timerJuego.stop();
            modelo.iniciarJuego();
            timerJuego.start();
            return;
        }

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