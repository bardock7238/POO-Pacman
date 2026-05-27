package view;

import model.JuegoModelo;

import javax.swing.*;

/**
 * Ventana principal del juego (MVC - Vista).
 * Construye el JFrame y aloja el JuegoPanel.
 */
public class VentanaPrincipal extends JFrame {

    private JuegoPanel juegoPanel;

    public VentanaPrincipal(JuegoModelo modelo) {
        super("Pac-Man — POO 2026");
        juegoPanel = new JuegoPanel(modelo);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(juegoPanel);
        pack();
        setLocationRelativeTo(null); // Centrar en pantalla
        setVisible(true);
    }

    public JuegoPanel getJuegoPanel() { return juegoPanel; }
}
