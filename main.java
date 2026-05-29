import Controlador.JuegoControlador;
import Modelo.JuegoModelo;
import Vista.VentanaPrincipal;
import javax.swing.SwingUtilities;


public class main {
    public static void main(String[] args) {
        // Ejecutar en el hilo de Swing (EDT) — buena práctica con Swing
        SwingUtilities.invokeLater(() -> {
            JuegoModelo modelo    = new JuegoModelo();
            VentanaPrincipal vista = new VentanaPrincipal(modelo);
            new JuegoControlador(modelo, vista);
            // El foco inicial va al panel para que reciba teclas inmediatamente
            vista.getJuegoPanel().requestFocusInWindow();
        });
    }
}
